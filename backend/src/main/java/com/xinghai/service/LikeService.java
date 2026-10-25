package com.xinghai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xinghai.common.ResultCode;
import com.xinghai.entity.BizComment;
import com.xinghai.entity.BizLike;
import com.xinghai.entity.BizVideo;
import com.xinghai.exception.BusinessException;
import com.xinghai.mapper.BizCommentMapper;
import com.xinghai.mapper.BizLikeMapper;
import com.xinghai.mapper.BizVideoMapper;
import com.xinghai.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 点赞服务：视频/评论点赞与取消
 * <p>防重复核心：数据库 (user_id, target_type, target_id) 唯一索引 + 事务。
 * 点赞时先查记录，有则恢复（逻辑删除置0），无则插入；取消则逻辑删除（置1）。</p>
 */
@Service
public class LikeService {

    private final BizLikeMapper likeMapper;
    private final BizVideoMapper videoMapper;
    private final BizCommentMapper commentMapper;

    public LikeService(BizLikeMapper likeMapper, BizVideoMapper videoMapper, BizCommentMapper commentMapper) {
        this.likeMapper = likeMapper;
        this.videoMapper = videoMapper;
        this.commentMapper = commentMapper;
    }

    /**
     * 点赞（幂等）：返回是否第一次点赞
     */
    @Transactional
    public boolean like(String targetType, Long targetId) {
        checkTarget(targetType, targetId);
        Long userId = UserContext.getUserId();

        BizLike exist = likeMapper.selectOne(new LambdaQueryWrapper<BizLike>()
                .eq(BizLike::getUserId, userId)
                .eq(BizLike::getTargetType, targetType)
                .eq(BizLike::getTargetId, targetId));
        if (exist != null) {
            // 记录存在（deleted=0，selectOne 已自动过滤逻辑删除）说明已点赞/已收藏，防重复
            throw new BusinessException("collect".equals(targetType) ? ResultCode.COLLECT_REPEAT : ResultCode.LIKE_REPEAT);
        }
        // 首次点赞：先物理清理历史逻辑删除残留（避免占用唯一索引导致插入冲突），再插入新记录
        likeMapper.deleteByUserAndTarget(userId, targetType, targetId);
        BizLike like = new BizLike();
        like.setUserId(userId);
        like.setTargetType(targetType);
        like.setTargetId(targetId);
        likeMapper.insert(like);
        incrCount(targetType, targetId, 1);
        return true;
    }

    /**
     * 取消点赞（幂等）：物理删除记录（@TableLogic 下 updateById 不会更新 deleted 字段）
     */
    @Transactional
    public void unlike(String targetType, Long targetId) {
        Long userId = UserContext.getUserId();
        BizLike exist = likeMapper.selectOne(new LambdaQueryWrapper<BizLike>()
                .eq(BizLike::getUserId, userId)
                .eq(BizLike::getTargetType, targetType)
                .eq(BizLike::getTargetId, targetId));
        if (exist == null) {
            throw new BusinessException(ResultCode.LIKE_REPEAT); // 未点赞
        }
        likeMapper.deleteByUserAndTarget(userId, targetType, targetId);
        incrCount(targetType, targetId, -1);
    }

    /** 校验点赞目标存在且合法 */
    private void checkTarget(String targetType, Long targetId) {
        if ("video".equals(targetType) || "collect".equals(targetType)) {
            BizVideo v = videoMapper.selectById(targetId);
            if (v == null || v.getStatus() != 1) {
                throw new BusinessException(ResultCode.VIDEO_UNAVAILABLE);
            }
        } else if ("comment".equals(targetType)) {
            BizComment c = commentMapper.selectById(targetId);
            if (c == null) {
                throw new BusinessException(ResultCode.TARGET_NOT_FOUND);
            }
        } else {
            throw new BusinessException("不支持的点赞类型");
        }
    }

    /** 同步计数 */
    private void incrCount(String targetType, Long targetId, int delta) {
        if ("video".equals(targetType)) {
            videoMapper.update(null, new LambdaUpdateWrapper<BizVideo>()
                    .eq(BizVideo::getId, targetId)
                    .setSql("like_count = like_count + " + delta));
        } else if ("collect".equals(targetType)) {
            videoMapper.update(null, new LambdaUpdateWrapper<BizVideo>()
                    .eq(BizVideo::getId, targetId)
                    .setSql("collect_count = collect_count + " + delta));
        } else if ("comment".equals(targetType)) {
            commentMapper.update(null, new LambdaUpdateWrapper<BizComment>()
                    .eq(BizComment::getId, targetId)
                    .setSql("like_count = like_count + " + delta));
        }
    }

    /** 我点赞过的视频ID列表 */
    public List<Long> myLikedVideoIds() {
        return myTargetIds("video");
    }

    /** 我收藏过的视频ID列表 */
    public List<Long> myCollectedVideoIds() {
        return myTargetIds("collect");
    }

    /** 按目标类型查我互动过的视频ID列表（点赞 video / 收藏 collect） */
    private List<Long> myTargetIds(String targetType) {
        return likeMapper.selectList(new LambdaQueryWrapper<BizLike>()
                        .eq(BizLike::getUserId, UserContext.getUserId())
                        .eq(BizLike::getTargetType, targetType))
                .stream().map(BizLike::getTargetId).collect(Collectors.toList());
    }
}
