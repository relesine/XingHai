package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报表实体
 */
@Data
@TableName("biz_report")
public class BizReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 举报人ID */
    private Long userId;

    /** 举报对象类型：video / comment / user */
    private String targetType;

    private Long targetId;

    /** 举报原因分类 */
    private String reason;

    private String detail;

    /** 处理状态：0待处理 1已处理 2已驳回 */
    private Integer status;

    /** 处理人（管理员）ID */
    private Long handleUserId;

    private LocalDateTime handleTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
