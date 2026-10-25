package com.xinghai.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 发表评论请求
 */
@Data
public class CommentDTO {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;

    /** 回复的父评论ID（0=一级评论） */
    private Long parentId = 0L;
}
