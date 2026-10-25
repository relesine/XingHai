package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表实体（电商核心业务）
 */
@Data
@TableName("biz_order")
public class BizOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号（唯一） */
    private String orderNo;

    /** 买家用户ID */
    private Long userId;

    private Long shopId;

    private Long productId;

    /** 商品名称快照 */
    private String productName;

    /** 商品主图快照 */
    private String productCover;

    /** 成交单价快照 */
    private BigDecimal price;

    private Integer quantity;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 状态：0待付款 1已付款 2已发货 3已完成 4已取消 */
    private Integer status;

    private String address;

    private LocalDateTime payTime;

    private LocalDateTime shipTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
