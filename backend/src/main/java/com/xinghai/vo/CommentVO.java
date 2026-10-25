package com.xinghai.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论 VO（含评论者信息）
 */
@Data
public class CommentVO {

    private Long id;

    private Long videoId;

    private Long userId;

    private String nickname;

    private String avatar;

    /** 父评论ID：0=一级评论 */
    private Long parentId;

    private String content;

    private Integer likeCount;

    private Boolean liked;

    private LocalDateTime createTime;
}
