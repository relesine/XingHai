package com.xinghai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinghai.common.PageResult;
import com.xinghai.common.ResultCode;
import com.xinghai.dto.CommentDTO;
import com.xinghai.entity.BizComment;
import com.xinghai.entity.BizLike;
import com.xinghai.entity.BizVideo;
import com.xinghai.entity.SysUser;
import com.xinghai.exception.BusinessException;
import com.xinghai.mapper.BizCommentMapper;
import com.xinghai.mapper.BizLikeMapper;
import com.xinghai.mapper.BizVideoMapper;
import com.xinghai.mapper.SysUserMapper;
import com.xinghai.security.UserContext;
import com.xinghai.vo.CommentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 评论服务：发表、列表、删除
 */
@Service
public class CommentService {

    private final BizCommentMapper commentMapper;
    private final BizVideoMapper videoMapper;
    private final SysUserMapper userMapper;
    private final BizLikeMapper likeMapper;

    public CommentService(BizCommentMapper commentMapper, BizVideoMapper videoMapper,
                          SysUserMapper userMapper, BizLikeMapper likeMapper) {
        this.commentMapper = commentMapper;
        this.videoMapper = videoMapper;
        this.userMapper = userMapper;
        this.likeMapper = likeMapper;
    }

    /**
     * 发表评论：视频必须已发布；作者可关闭评论
     */
    @Transactional
    public Long add(Long videoId, CommentDTO dto) {
        BizVideo video = videoMapper.selectById(videoId);
        if (video == null || video.getStatus() != 1) {
            throw new BusinessException(ResultCode.VIDEO_UNAVAILABLE);
        }
        if (video.getAllowComment() == 0) {
            throw new BusinessException("该视频已关闭评论");
        }
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            BizComment parent = commentMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException(ResultCode.TARGET_NOT_FOUND);
            }
        }
        BizComment comment = new BizComment();
        comment.setVideoId(videoId);
        comment.setUserId(UserContext.getUserId());
        comment.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1);
        commentMapper.insert(comment);

        videoMapper.update(null, new LambdaUpdateWrapper<BizVideo>()
                .eq(BizVideo::getId, videoId)
                .setSql("comment_count = comment_count + 1"));
        return comment.getId();
    }

    /** 评论列表（一级评论在前，回复跟随其后，前端按 parentId 渲染树） */
    public PageResult<CommentVO> list(Long videoId, long pageNum, long pageSize) {
        Page<BizComment> page = commentMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizComment>()
                        .eq(BizComment::getVideoId, videoId)
                        .eq(BizComment::getStatus, 1)
                        .orderByAsc(BizComment::getCreateTime));
        return toPageResult(page);
    }

    /** 删除评论（本人或管理员） */
    @Transactional
    public void delete(Long commentId) {
        BizComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.TARGET_NOT_FOUND);
        }
        Long current = UserContext.getUserId();
        boolean isAdmin = UserContext.hasRole("ADMIN");
        if (!comment.getUserId().equals(current) && !isAdmin) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        commentMapper.deleteById(commentId);
        videoMapper.update(null, new LambdaUpdateWrapper<BizVideo>()
                .eq(BizVideo::getId, comment.getVideoId())
                .setSql("comment_count = GREATEST(comment_count - 1, 0)"));
    }

    /** 单条评论转 VO（复用批量逻辑） */
    private CommentVO toVO(BizComment c) {
        return toVOList(java.util.List.of(c)).get(0);
    }

    /**
     * 批量转 VO：一次性加载本页评论的用户信息与当前用户点赞状态，
     * 避免「每条评论查一次用户、查一次点赞」的 N+1。
     */
    private java.util.List<CommentVO> toVOList(java.util.List<BizComment> comments) {
        java.util.List<CommentVO> result = new java.util.ArrayList<>();
        if (comments == null || comments.isEmpty()) {
            return result;
        }
        // 批量加载用户（一次 IN 查询）
        java.util.Set<Long> userIds = comments.stream()
                .map(BizComment::getUserId).filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, SysUser> userMap = userIds.isEmpty() ? java.util.Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(java.util.stream.Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));
        // 批量加载当前用户对这些评论的点赞（一次 IN 查询）
        Long current = UserContext.getUserId();
        java.util.Set<Long> likedIds = java.util.Collections.emptySet();
        if (current != null) {
            java.util.Set<Long> commentIds = comments.stream()
                    .map(BizComment::getId).collect(java.util.stream.Collectors.toSet());
            likedIds = likeMapper.selectList(new LambdaQueryWrapper<BizLike>()
                            .eq(BizLike::getUserId, current)
                            .eq(BizLike::getTargetType, "comment")
                            .in(BizLike::getTargetId, commentIds)).stream()
                    .map(BizLike::getTargetId).collect(java.util.stream.Collectors.toSet());
        }
        for (BizComment c : comments) {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setVideoId(c.getVideoId());
            vo.setUserId(c.getUserId());
            vo.setParentId(c.getParentId());
            vo.setContent(c.getContent());
            vo.setLikeCount(c.getLikeCount());
            vo.setCreateTime(c.getCreateTime());
            SysUser user = userMap.get(c.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
            vo.setLiked(current != null && likedIds.contains(c.getId()));
            result.add(vo);
        }
        return result;
    }

    /** 分页结果转换（批量补用户与点赞状态） */
    private PageResult<CommentVO> toPageResult(Page<BizComment> page) {
        PageResult<CommentVO> result = new PageResult<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setList(toVOList(page.getRecords()));
        return result;
    }
}
