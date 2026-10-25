package com.xinghai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinghai.common.ResultCode;
import com.xinghai.entity.BizFollow;
import com.xinghai.exception.BusinessException;
import com.xinghai.mapper.BizFollowMapper;
import com.xinghai.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 关注服务：关注/取关（幂等，唯一索引 + 逻辑删除实现）
 */
@Service
public class FollowService {

    private final BizFollowMapper followMapper;

    public FollowService(BizFollowMapper followMapper) {
        this.followMapper = followMapper;
    }

    /** 关注（幂等） */
    @Transactional
    public void follow(Long targetUserId) {
        Long userId = UserContext.getUserId();
        if (userId.equals(targetUserId)) {
            throw new BusinessException("不能关注自己");
        }
        BizFollow exist = followMapper.selectOne(new LambdaQueryWrapper<BizFollow>()
                .eq(BizFollow::getUserId, userId)
                .eq(BizFollow::getFollowUserId, targetUserId));
        if (exist != null) {
            // 记录存在（deleted=0，selectOne 已自动过滤逻辑删除）说明已关注，防重复
            throw new BusinessException(ResultCode.FOLLOW_EXISTS);
        }
        // 首次关注：先物理清理历史逻辑删除残留（避免占用唯一索引导致插入冲突），再插入
        followMapper.deleteByUserAndTarget(userId, targetUserId);
        BizFollow follow = new BizFollow();
        follow.setUserId(userId);
        follow.setFollowUserId(targetUserId);
        followMapper.insert(follow);
    }

    /** 取关（幂等）：物理删除（@TableLogic 下 updateById 不会更新 deleted 字段） */
    @Transactional
    public void unfollow(Long targetUserId) {
        Long userId = UserContext.getUserId();
        BizFollow exist = followMapper.selectOne(new LambdaQueryWrapper<BizFollow>()
                .eq(BizFollow::getUserId, userId)
                .eq(BizFollow::getFollowUserId, targetUserId));
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOLLOW);
        }
        followMapper.deleteByUserAndTarget(userId, targetUserId);
    }
}
