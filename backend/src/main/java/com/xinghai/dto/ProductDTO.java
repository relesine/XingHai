package com.xinghai.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 商品新增/编辑请求（创作者商家端）
 */
@Data
public class ProductDTO {

    /** 编辑时必填（后端按路径取值，这里非强制） */
    private Long id;

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称不能超过100字")
    private String productName;

    /** 商品主图URL（由上传接口返回） */
    private String coverUrl;

    @Size(max = 500, message = "商品描述不能超过500字")
    private String description;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    @javax.validation.constraints.Min(value = 0, message = "库存不能为负数")
    private Integer stock;
}
