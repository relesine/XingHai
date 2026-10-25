package com.xinghai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xinghai.entity.BizLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 点赞表 Mapper
 * <p>物理删除方法用于「取消点赞/收藏」：实体 deleted 字段是 @TableLogic 逻辑删除，
 * updateById 不会更新该字段，且逻辑删除记录会占用 (user_id,target_type,target_id)
 * 唯一索引导致重新点赞冲突，因此取消操作直接物理删除该行。</p>
 */
public interface BizLikeMapper extends BaseMapper<BizLike> {

    /** 物理删除某用户对某目标的互动记录（点赞 video / 收藏 collect / 评论 comment） */
    @Delete("DELETE FROM biz_like WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    int deleteByUserAndTarget(@Param("userId") Long userId,
                              @Param("targetType") String targetType,
                              @Param("targetId") Long targetId);
}
