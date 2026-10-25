package com.xinghai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xinghai.entity.BizFollow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 关注表 Mapper
 * <p>物理删除方法用于「取消关注」：实体 deleted 字段是 @TableLogic 逻辑删除，
 * updateById 不会更新该字段，且逻辑删除记录会占用 (user_id,follow_user_id)
 * 唯一索引导致重新关注冲突，因此取关直接物理删除该行。</p>
 */
public interface BizFollowMapper extends BaseMapper<BizFollow> {

    /** 物理删除某用户对某目标的关注记录 */
    @Delete("DELETE FROM biz_follow WHERE user_id = #{userId} AND follow_user_id = #{targetUserId}")
    int deleteByUserAndTarget(@Param("userId") Long userId,
                              @Param("targetUserId") Long targetUserId);
}
