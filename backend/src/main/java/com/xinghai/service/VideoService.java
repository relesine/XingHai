package com.xinghai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinghai.common.PageResult;
import com.xinghai.common.ResultCode;
import com.xinghai.dto.VideoPublishDTO;
import com.xinghai.dto.VideoUpdateDTO;
import com.xinghai.entity.*;
import com.xinghai.exception.BusinessException;
import com.xinghai.mapper.*;
import com.xinghai.security.UserContext;
import com.xinghai.util.RedisUtil;
import com.xinghai.vo.VideoVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 视频服务：发布、信息流（推荐/关注/同城/搜索）、详情、审核流转
 */
@Service
public class VideoService {

    private final BizVideoMapper videoMapper;
    private final BizVideoTagMapper videoTagMapper;
    private final BizTagMapper tagMapper;
    private final BizCategoryMapper categoryMapper;
    private final BizLikeMapper likeMapper;
    private final BizFollowMapper followMapper;
    private final SysUserMapper userMapper;
    private final RedisUtil redisUtil;

    public VideoService(BizVideoMapper videoMapper, BizVideoTagMapper videoTagMapper,
                        BizTagMapper tagMapper, BizCategoryMapper categoryMapper,
                        BizLikeMapper likeMapper, BizFollowMapper followMapper,
                        SysUserMapper userMapper, RedisUtil redisUtil) {
        this.videoMapper = videoMapper;
        this.videoTagMapper = videoTagMapper;
        this.tagMapper = tagMapper;
        this.categoryMapper = categoryMapper;
        this.likeMapper = likeMapper;
        this.followMapper = followMapper;
        this.userMapper = userMapper;
        this.redisUtil = redisUtil;
    }

    /**
     * 发布视频：写入视频记录（初始状态=0待审核）+ 绑定标签
     * <p>审核流转：0待审核 →（通过）→ 1已发布 /（拒绝）→ 3审核拒绝；已发布可被管理员下架=2</p>
     */
    @Transactional
    public Long publish(VideoPublishDTO dto) {
        BizVideo video = new BizVideo();
        video.setUserId(UserContext.getUserId());
        video.setTitle(dto.getTitle());
        video.setDescription(dto.getDescription());
        video.setVideoUrl(dto.getVideoUrl());
        video.setCoverUrl(dto.getCoverUrl());
        video.setDuration(dto.getDuration() == null ? 0 : dto.getDuration());
        video.setCategoryId(dto.getCategoryId());
        video.setStatus(0);
        video.setIsPublic(dto.getIsPublic() == null ? 1 : dto.getIsPublic());
        video.setAllowComment(dto.getAllowComment() == null ? 1 : dto.getAllowComment());
        video.setGeoLocation(dto.getGeoLocation());
        video.setViewCount(0L);
        video.setLikeCount(0L);
        video.setCommentCount(0L);
        video.setShareCount(0L);
        video.setCollectCount(0L);
        videoMapper.insert(video);

        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            for (Long tagId : dto.getTagIds()) {
                BizVideoTag vt = new BizVideoTag();
                vt.setVideoId(video.getId());
                vt.setTagId(tagId);
                videoTagMapper.insert(vt);
            }
        }
        return video.getId();
    }

    /**
     * 审核：管理员操作。pass=通过(0→1) reject=拒绝(0→3) off=下架(1→2) up=上架(2→1)
     */
    @Transactional
    public void audit(Long videoId, String action, String rejectReason) {
        BizVideo video = getVideoOrThrow(videoId);
        switch (action) {
            case "pass" -> {
                if (video.getStatus() != 0) {
                    throw new BusinessException(ResultCode.VIDEO_STATUS_ERROR);
                }
                video.setStatus(1);
                video.setRejectReason(null);
            }
            case "reject" -> {
                if (video.getStatus() != 0) {
                    throw new BusinessException(ResultCode.VIDEO_STATUS_ERROR);
                }
                video.setStatus(3);
                video.setRejectReason(rejectReason == null ? "未通过平台审核" : rejectReason);
            }
            case "off" -> {
                if (video.getStatus() != 1) {
                    throw new BusinessException(ResultCode.VIDEO_STATUS_ERROR);
                }
                video.setStatus(2);
            }
            case "up" -> {
                if (video.getStatus() != 2) {
                    throw new BusinessException(ResultCode.VIDEO_STATUS_ERROR);
                }
                video.setStatus(1);
                video.setRejectReason(null);
            }
            default -> throw new BusinessException("不支持的审核动作: " + action);
        }
        videoMapper.updateById(video);
    }

    /** 删除自己的视频（或管理员删除） */
    @Transactional
    public void delete(Long videoId) {
        BizVideo video = getVideoOrThrow(videoId);
        Long current = UserContext.getUserId();
        boolean isAdmin = UserContext.hasRole("ADMIN");
        if (!video.getUserId().equals(current) && !isAdmin) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        videoMapper.deleteById(videoId);
        videoTagMapper.delete(new LambdaQueryWrapper<BizVideoTag>().eq(BizVideoTag::getVideoId, videoId));
    }

    /** 设置作品可见性（创作者权限：公开/私密） */
    public void changeVisibility(Long videoId, Integer isPublic) {
        BizVideo video = getVideoOrThrow(videoId);
        if (!video.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (isPublic != 0 && isPublic != 1) {
            throw new BusinessException("可见性参数不合法");
        }
        video.setIsPublic(isPublic);
        videoMapper.updateById(video);
    }

    /**
     * 推荐流（简化标签推荐算法）：
     * <p>1. 登录用户：取 TA 点过赞的视频标签 → 统计标签权重 → 候选视频按
     *    「标签重合分 + 热度分 + 时间衰减」加权排序；
     * 2. 未登录：直接按热度（浏览量）排序。</p>
     */
    public PageResult<VideoVO> recommend(long pageNum, long pageSize) {
        Long userId = UserContext.getUserId();
        List<BizVideo> candidates = listPublished();

        if (userId == null) {
            // 未登录：热门兜底
            candidates.sort((a, b) -> Long.compare(b.getViewCount(), a.getViewCount()));
            return toPage(candidates, pageNum, pageSize);
        }

        // 一次性批量加载所有候选视频的标签（避免打分时逐个查库的 N+1）
        Map<Long, List<Long>> candidateTagMap = batchTagIds(
                candidates.stream().map(BizVideo::getId).collect(Collectors.toList()));

        Map<Long, Integer> tagWeight = new HashMap<>();
        List<BizLike> likes = likeMapper.selectList(new LambdaQueryWrapper<BizLike>()
                .eq(BizLike::getUserId, userId).eq(BizLike::getTargetType, "video"));
        List<Long> likedVideoIds = likes.stream().map(BizLike::getTargetId).collect(Collectors.toList());
        Map<Long, List<Long>> likedVideoTagMap = batchTagIds(likedVideoIds);
        for (Long likedVideoId : likedVideoIds) {
            for (Long tagId : likedVideoTagMap.getOrDefault(likedVideoId, Collections.emptyList())) {
                tagWeight.merge(tagId, 10, Integer::sum);
            }
        }
        // 冷启动：兴趣为空时，用热门标签（hot值前3）兜底
        if (tagWeight.isEmpty()) {
            List<BizTag> hotTags = tagMapper.selectList(new LambdaQueryWrapper<BizTag>()
                    .orderByDesc(BizTag::getHot).last("LIMIT 3"));
            for (BizTag t : hotTags) {
                tagWeight.put(t.getId(), 5);
            }
        }

        long now = System.currentTimeMillis();
        List<VideoScore> scored = new ArrayList<>();
        for (BizVideo v : candidates) {
            List<Long> tags = candidateTagMap.getOrDefault(v.getId(), Collections.emptyList());
            int overlap = 0;
            for (Long t : tags) {
                overlap += tagWeight.getOrDefault(t, 0);
            }
            if (overlap == 0 && !tagWeight.isEmpty()) {
                continue; // 与用户兴趣完全无关，过滤
            }
            // 热度分：浏览量/100；时间衰减：每过1天扣5分（最多扣40）
            long ageDays = (now - v.getCreateTime().atZone(java.time.ZoneId.systemDefault())
                    .toInstant().toEpochMilli()) / 86400000L;
            double timeDecay = Math.max(0, 40 - ageDays * 5);
            double score = overlap + v.getViewCount() / 100.0 + timeDecay;
            scored.add(new VideoScore(v, score));
        }
        scored.sort((a, b) -> Double.compare(b.score, a.score));
        List<BizVideo> ordered = scored.stream().map(s -> s.video).collect(Collectors.toList());
        return toPage(ordered, pageNum, pageSize);
    }

    /** 关注流：我关注的人发布的已审核视频 */
    public PageResult<VideoVO> followFeed(long pageNum, long pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return toPage(List.of(), pageNum, pageSize);
        }
        List<Long> followIds = followMapper.selectList(new LambdaQueryWrapper<BizFollow>()
                        .eq(BizFollow::getUserId, userId))
                .stream().map(BizFollow::getFollowUserId).collect(Collectors.toList());
        if (followIds.isEmpty()) {
            return toPage(List.of(), pageNum, pageSize);
        }
        Page<BizVideo> page = videoMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizVideo>()
                        .in(BizVideo::getUserId, followIds)
                        .eq(BizVideo::getStatus, 1)
                        .eq(BizVideo::getIsPublic, 1)
                        .orderByDesc(BizVideo::getCreateTime));
        return toPageResult(page);
    }

    /** 同城流：按发布位置过滤 */
    public PageResult<VideoVO> nearby(String city, long pageNum, long pageSize) {
        if (city == null || city.isBlank()) {
            if (UserContext.getUserId() != null) {
                SysUser user = userMapper.selectById(UserContext.getUserId());
                city = user == null ? null : user.getLocation();
            }
        }
        if (city == null || city.isBlank()) {
            return toPage(List.of(), pageNum, pageSize);
        }
        Page<BizVideo> page = videoMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizVideo>()
                        .eq(BizVideo::getGeoLocation, city)
                        .eq(BizVideo::getStatus, 1)
                        .eq(BizVideo::getIsPublic, 1)
                        .orderByDesc(BizVideo::getCreateTime));
        return toPageResult(page);
    }

    /** 搜索：标题/简介模糊匹配 + 标签匹配 */
    public PageResult<VideoVO> search(String keyword, long pageNum, long pageSize) {
        if (keyword == null || keyword.isBlank()) {
            return recommend(pageNum, pageSize);
        }
        // 先按标签匹配出相关视频ID
        List<BizTag> matchedTags = tagMapper.selectList(new LambdaQueryWrapper<BizTag>()
                .like(BizTag::getName, keyword));
        Set<Long> tagVideoIds = new HashSet<>();
        for (BizTag tag : matchedTags) {
            tagVideoIds.addAll(videoTagMapper.selectList(new LambdaQueryWrapper<BizVideoTag>()
                            .eq(BizVideoTag::getTagId, tag.getId()))
                    .stream().map(BizVideoTag::getVideoId).collect(Collectors.toList()));
        }
        Page<BizVideo> page = videoMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizVideo>()
                        .eq(BizVideo::getStatus, 1)
                        .eq(BizVideo::getIsPublic, 1)
                        .and(w -> w.like(BizVideo::getTitle, keyword)
                                .or().like(BizVideo::getDescription, keyword)
                                .or().in(!tagVideoIds.isEmpty(), BizVideo::getId, tagVideoIds))
                        .orderByDesc(BizVideo::getCreateTime));
        return toPageResult(page);
    }

    /** 分类浏览 */
    public PageResult<VideoVO> byCategory(Long categoryId, long pageNum, long pageSize) {
        Page<BizVideo> page = videoMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizVideo>()
                        .eq(BizVideo::getCategoryId, categoryId)
                        .eq(BizVideo::getStatus, 1)
                        .eq(BizVideo::getIsPublic, 1)
                        .orderByDesc(BizVideo::getCreateTime));
        return toPageResult(page);
    }

    /**
     * 视频详情 + 浏览量去重统计
     * <p>去重策略：Redis Set 记录「视频ID+日期 → 用户ID」，同一用户当天只计一次；
     * 未登录用户按"匿名"当天只计一次。</p>
     */
    public VideoVO detail(Long videoId) {
        BizVideo video = getVideoOrThrow(videoId);
        // 不可见校验：非发布状态 或 私密作品 且 非本人/管理员
        boolean canView = video.getStatus() == 1 && video.getIsPublic() == 1;
        Long current = UserContext.getUserId();
        boolean isOwnerOrAdmin = (current != null && video.getUserId().equals(current))
                || (current != null && UserContext.hasRole("ADMIN"));
        if (!canView && !isOwnerOrAdmin) {
            throw new BusinessException(ResultCode.VIDEO_UNAVAILABLE);
        }

        // 浏览量去重（Redis Set）
        String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String viewerKey = current == null ? "anon" : String.valueOf(current);
        String dedupKey = "xh:view:" + videoId + ":" + dateStr;
        boolean firstView = redisUtil.sadd(dedupKey, viewerKey);
        if (firstView) {
            redisUtil.expire(dedupKey, 86400 * 2); // 2天过期，防 Redis 膨胀
            videoMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<BizVideo>()
                    .eq(BizVideo::getId, videoId)
                    .setSql("view_count = view_count + 1"));
            video.setViewCount(video.getViewCount() + 1);
        }
        return toVO(video);
    }

    /** 我的作品列表（含审核状态） */
    public PageResult<VideoVO> mine(long pageNum, long pageSize) {
        Long userId = UserContext.getUserId();
        Page<BizVideo> page = videoMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizVideo>()
                        .eq(BizVideo::getUserId, userId)
                        .orderByDesc(BizVideo::getCreateTime));
        return toPageResult(page);
    }

    /**
     * 静默详情：查询视频并转 VO，不增加浏览量、不校验可见性。
     * 用于「我点赞/收藏的列表」——下架或删除的视频自动跳过（返回 null），避免整个列表报错。
     */
    public VideoVO detailQuiet(Long videoId) {
        BizVideo video = videoMapper.selectById(videoId);
        return video == null ? null : toVO(video);
    }

    /** 某用户公开作品（个人主页） */
    public PageResult<VideoVO> userVideos(Long userId, long pageNum, long pageSize) {
        Page<BizVideo> page = videoMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizVideo>()
                        .eq(BizVideo::getUserId, userId)
                        .eq(BizVideo::getStatus, 1)
                        .eq(BizVideo::getIsPublic, 1)
                        .orderByDesc(BizVideo::getCreateTime));
        return toPageResult(page);
    }

    /** 管理端：按状态筛选视频 */
    public PageResult<VideoVO> adminList(Integer status, String keyword, long pageNum, long pageSize) {
        Page<BizVideo> page = videoMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizVideo>()
                        .eq(status != null, BizVideo::getStatus, status)
                        .and(keyword != null && !keyword.isBlank(),
                                w -> w.like(BizVideo::getTitle, keyword).or().like(BizVideo::getDescription, keyword))
                        .orderByDesc(BizVideo::getCreateTime));
        return toPageResult(page);
    }

    /**
     * 相关推荐：同分类下已发布公开的视频，按热度（浏览量+点赞数）排序，排除自己。
     * 用于视频播放页「猜你喜欢」列表。
     */
    public List<VideoVO> related(Long videoId, int limit) {
        BizVideo current = getVideoOrThrow(videoId);
        List<BizVideo> list = videoMapper.selectList(new LambdaQueryWrapper<BizVideo>()
                .eq(BizVideo::getCategoryId, current.getCategoryId())
                .eq(BizVideo::getStatus, 1)
                .eq(BizVideo::getIsPublic, 1)
                .ne(BizVideo::getId, videoId));
        list.sort((a, b) -> Long.compare(
                b.getViewCount() + b.getLikeCount(),
                a.getViewCount() + a.getLikeCount()));
        return toVOList(list.stream().limit(limit).collect(Collectors.toList()));
    }

    /**
     * 编辑作品（仅作者本人）：可修改标题/简介/封面/时长/位置。
     * 审核状态与互动数据不允许修改，防止绕过审核。
     */
    @Transactional
    public void update(Long videoId, VideoUpdateDTO dto) {
        BizVideo video = getVideoOrThrow(videoId);
        if (!video.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            video.setTitle(dto.getTitle().trim());
        }
        if (dto.getDescription() != null) {
            video.setDescription(dto.getDescription());
        }
        if (dto.getCoverUrl() != null && !dto.getCoverUrl().isBlank()) {
            video.setCoverUrl(dto.getCoverUrl());
        }
        if (dto.getDuration() != null) {
            video.setDuration(dto.getDuration());
        }
        if (dto.getGeoLocation() != null) {
            video.setGeoLocation(dto.getGeoLocation());
        }
        videoMapper.updateById(video);
    }

    /** 已发布公开视频全量 */
    private List<BizVideo> listPublished() {
        return videoMapper.selectList(new LambdaQueryWrapper<BizVideo>()
                .eq(BizVideo::getStatus, 1)
                .eq(BizVideo::getIsPublic, 1));
    }

    /** 批量查询多个视频的标签ID：一次 IN 查询，返回 videoId -> 标签ID列表（消除 N+1） */
    private Map<Long, List<Long>> batchTagIds(List<Long> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<BizVideoTag> rels = videoTagMapper.selectList(new LambdaQueryWrapper<BizVideoTag>()
                .in(BizVideoTag::getVideoId, videoIds));
        Map<Long, List<Long>> map = new HashMap<>();
        for (BizVideoTag rel : rels) {
            map.computeIfAbsent(rel.getVideoId(), k -> new ArrayList<>()).add(rel.getTagId());
        }
        return map;
    }

    /** 查询单个视频标签ID（保留给少量单条场景使用） */
    public List<Long> listTagIds(Long videoId) {
        return videoTagMapper.selectList(new LambdaQueryWrapper<BizVideoTag>()
                        .eq(BizVideoTag::getVideoId, videoId))
                .stream().map(BizVideoTag::getTagId).collect(Collectors.toList());
    }

    /** 单条实体转 VO（复用批量逻辑，避免重复代码） */
    public VideoVO toVO(BizVideo v) {
        return toVOList(List.of(v)).get(0);
    }

    /**
     * 批量实体转 VO（性能优化核心）：
     * <p>旧实现对每个视频都单独查「作者 / 分类 / 每个标签 / 点赞 / 关注 / 收藏」，
     * 一页 10 条会产生 60+ 次 SQL（典型 N+1）。
     * 本方法改为一次性批量加载作者、分类、标签关联、标签、当前用户互动状态，
     * 整页查询数从「O(N)」降到固定常数次。</p>
     */
    private List<VideoVO> toVOList(List<BizVideo> videos) {
        List<VideoVO> result = new ArrayList<>();
        if (videos == null || videos.isEmpty()) {
            return result;
        }

        // 批量加载作者（一次 IN 查询）
        Set<Long> authorIds = videos.stream()
                .map(BizVideo::getUserId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SysUser> authorMap = authorIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(authorIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));

        // 批量加载分类（一次 IN 查询）
        Set<Long> categoryIds = videos.stream()
                .map(BizVideo::getCategoryId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, BizCategory> categoryMap = categoryIds.isEmpty() ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(BizCategory::getId, c -> c, (a, b) -> a));

        // 批量加载视频-标签关联（一次 IN 查询）
        Set<Long> videoIds = videos.stream().map(BizVideo::getId).collect(Collectors.toSet());
        List<BizVideoTag> rels = videoTagMapper.selectList(new LambdaQueryWrapper<BizVideoTag>()
                .in(BizVideoTag::getVideoId, videoIds));
        Map<Long, List<Long>> videoTagIdMap = new HashMap<>();
        Set<Long> tagIds = new HashSet<>();
        for (BizVideoTag rel : rels) {
            videoTagIdMap.computeIfAbsent(rel.getVideoId(), k -> new ArrayList<>()).add(rel.getTagId());
            tagIds.add(rel.getTagId());
        }

        // 批量加载标签（一次 IN 查询）
        Map<Long, BizTag> tagMap = tagIds.isEmpty() ? Collections.emptyMap()
                : tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(BizTag::getId, t -> t, (a, b) -> a));

        // 批量加载当前登录用户的互动状态（点赞 / 收藏 / 关注各一次 IN 查询）
        Long current = UserContext.getUserId();
        Set<Long> likedVideoIds = Collections.emptySet();
        Set<Long> collectedVideoIds = Collections.emptySet();
        Set<Long> followedAuthorIds = Collections.emptySet();
        if (current != null) {
            likedVideoIds = likeMapper.selectList(new LambdaQueryWrapper<BizLike>()
                            .eq(BizLike::getUserId, current)
                            .eq(BizLike::getTargetType, "video")
                            .in(BizLike::getTargetId, videoIds)).stream()
                    .map(BizLike::getTargetId).collect(Collectors.toSet());
            collectedVideoIds = likeMapper.selectList(new LambdaQueryWrapper<BizLike>()
                            .eq(BizLike::getUserId, current)
                            .eq(BizLike::getTargetType, "collect")
                            .in(BizLike::getTargetId, videoIds)).stream()
                    .map(BizLike::getTargetId).collect(Collectors.toSet());
            followedAuthorIds = followMapper.selectList(new LambdaQueryWrapper<BizFollow>()
                            .eq(BizFollow::getUserId, current)
                            .in(BizFollow::getFollowUserId, authorIds)).stream()
                    .map(BizFollow::getFollowUserId).collect(Collectors.toSet());
        }

        for (BizVideo v : videos) {
            result.add(buildVO(v, authorMap, categoryMap, tagMap, videoTagIdMap,
                    current, likedVideoIds, collectedVideoIds, followedAuthorIds));
        }
        return result;
    }

    /** 基于已批量加载好的数据组装单个 VO */
    private VideoVO buildVO(BizVideo v, Map<Long, SysUser> authorMap, Map<Long, BizCategory> categoryMap,
                            Map<Long, BizTag> tagMap, Map<Long, List<Long>> videoTagIdMap,
                            Long current, Set<Long> likedVideoIds, Set<Long> collectedVideoIds,
                            Set<Long> followedAuthorIds) {
        VideoVO vo = new VideoVO();
        vo.setId(v.getId());
        vo.setUserId(v.getUserId());
        vo.setTitle(v.getTitle());
        vo.setDescription(v.getDescription());
        vo.setVideoUrl(v.getVideoUrl());
        vo.setCoverUrl(v.getCoverUrl());
        vo.setDuration(v.getDuration());
        vo.setCategoryId(v.getCategoryId());
        vo.setStatus(v.getStatus());
        vo.setRejectReason(v.getRejectReason());
        vo.setViewCount(v.getViewCount());
        vo.setLikeCount(v.getLikeCount());
        vo.setCommentCount(v.getCommentCount());
        vo.setShareCount(v.getShareCount());
        vo.setCollectCount(v.getCollectCount());
        vo.setIsPublic(v.getIsPublic());
        vo.setAllowComment(v.getAllowComment());
        vo.setGeoLocation(v.getGeoLocation());
        vo.setCreateTime(v.getCreateTime());

        // 作者信息（取自内存 Map，无需查库）
        SysUser author = authorMap.get(v.getUserId());
        if (author != null) {
            vo.setAuthorName(author.getNickname());
            vo.setAuthorAvatar(author.getAvatar());
        }
        BizCategory cate = categoryMap.get(v.getCategoryId());
        if (cate != null) {
            vo.setCategoryName(cate.getName());
        }
        // 标签名（取自内存 Map）
        vo.setTags(videoTagIdMap.getOrDefault(v.getId(), Collections.emptyList()).stream()
                .map(tagMap::get).filter(Objects::nonNull)
                .map(BizTag::getName).collect(Collectors.toList()));

        // 当前用户状态（取自内存 Set）
        vo.setLiked(current != null && likedVideoIds.contains(v.getId()));
        vo.setCollected(current != null && collectedVideoIds.contains(v.getId()));
        vo.setFollowed(current != null && followedAuthorIds.contains(v.getUserId()));
        return vo;
    }

    /** 数据库分页结果转 VO（批量补作者/分类/标签/状态，避免 N+1） */
    private PageResult<VideoVO> toPageResult(Page<BizVideo> page) {
        PageResult<VideoVO> result = new PageResult<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setList(toVOList(page.getRecords()));
        return result;
    }

    /** 内存分页（用于推荐流的内存排序场景），返回当页并批量转 VO */
    private PageResult<VideoVO> toPage(List<BizVideo> list, long pageNum, long pageSize) {
        long total = list.size();
        int from = (int) Math.min((pageNum - 1) * pageSize, total);
        int to = (int) Math.min(from + pageSize, total);
        List<VideoVO> vos = toVOList(list.subList(from, to));
        PageResult<VideoVO> result = new PageResult<>();
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotal(total);
        result.setPages((total + pageSize - 1) / pageSize);
        result.setList(vos);
        return result;
    }

    private BizVideo getVideoOrThrow(Long videoId) {
        BizVideo video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new BusinessException(ResultCode.VIDEO_NOT_FOUND);
        }
        return video;
    }

    /** 打分内部类 */
    private record VideoScore(BizVideo video, double score) {
    }
}
