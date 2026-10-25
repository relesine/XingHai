package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频分类表实体
 */
@Data
@TableName("biz_category")
public class BizCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer sort;

    /** 状态：0停用 1启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
