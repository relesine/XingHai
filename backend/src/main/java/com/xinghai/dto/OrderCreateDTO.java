package com.xinghai.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 创建订单请求
 */
@Data
public class OrderCreateDTO {

    @NotNull(message = "商品不能为空")
    private Long productId;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1")
    @Max(value = 999, message = "单笔购买数量不能超过999")
    private Integer quantity;

    @NotBlank(message = "收货地址不能为空")
    @Size(max = 200, message = "收货地址不能超过200字")
    private String address;
}
