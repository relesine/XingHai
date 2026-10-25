package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签表实体
 */
@Data
@TableName("biz_tag")
public class BizTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long categoryId;

    /** 热度值（推荐算法参考） */
    private Integer hot;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
