package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关注表实体
 */
@Data
@TableName("biz_follow")
public class BizFollow {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 粉丝用户ID（谁关注） */
    private Long userId;

    /** 被关注用户ID */
    private Long followUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
