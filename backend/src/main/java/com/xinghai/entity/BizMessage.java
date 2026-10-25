package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信表实体
 */
@Data
@TableName("biz_message")
public class BizMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    /** 消息类型：text / image / system */
    private String msgType;

    /** 是否已读：0未读 1已读 */
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
