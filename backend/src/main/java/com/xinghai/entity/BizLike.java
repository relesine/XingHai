package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞表实体（点赞/收藏通用，取消点赞用逻辑删除）
 */
@Data
@TableName("biz_like")
public class BizLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 目标类型：video / comment */
    private String targetType;

    private Long targetId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
