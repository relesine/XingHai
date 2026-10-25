package com.xinghai.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 举报请求
 */
@Data
public class ReportDTO {

    @NotBlank(message = "举报对象类型不能为空")
    private String targetType;

    @NotNull(message = "举报对象ID不能为空")
    private Long targetId;

    @NotBlank(message = "举报原因不能为空")
    @Size(max = 100, message = "举报原因不能超过100字")
    private String reason;

    @Size(max = 500, message = "举报详情不能超过500字")
    private String detail;
}
