package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品表实体
 */
@Data
@TableName("biz_product")
public class BizProduct {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;

    private String productName;

    private String coverUrl;

    private String description;

    private BigDecimal price;

    /** 库存 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

    /** 状态：0下架 1上架 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
