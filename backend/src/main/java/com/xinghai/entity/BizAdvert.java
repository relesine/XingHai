package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 广告表实体
 */
@Data
@TableName("biz_advert")
public class BizAdvert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String imageUrl;

    private String linkUrl;

    /** 投放位置：home / follow / live */
    private String position;

    /** 状态：0停用 1投放中 */
    private Integer status;

    private Integer sort;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
