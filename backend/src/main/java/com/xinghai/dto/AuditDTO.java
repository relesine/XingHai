package com.xinghai.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 视频审核请求（管理员）
 */
@Data
public class AuditDTO {

    @NotNull(message = "视频ID不能为空")
    private Long videoId;

    /** 审核动作：pass=通过 reject=拒绝 off=下架 up=上架 */
    @NotBlank(message = "审核动作不能为空")
    private String action;

    /** 拒绝原因（拒绝时必填） */
    @Size(max = 255, message = "拒绝原因不能超过255字")
    private String rejectReason;
}
