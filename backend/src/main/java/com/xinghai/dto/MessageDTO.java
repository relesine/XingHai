package com.xinghai.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 发送私信请求
 */
@Data
public class MessageDTO {

    @NotNull(message = "接收者不能为空")
    private Long toUserId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1024, message = "单条消息不能超过1024字")
    private String content;
}
