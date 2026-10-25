package com.xinghai.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单 VO
 */
@Data
public class OrderVO {

    private Long id;

    private String orderNo;

    private Long productId;

    private String productName;

    private String productCover;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal totalAmount;

    /** 0待付款 1已付款 2已发货 3已完成 4已取消 */
    private Integer status;

    private String address;

    /** 买家昵称（店铺侧展示用） */
    private String buyerName;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime shipTime;
}
