package com.xinghai.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频 VO（信息流/详情通用，含作者信息、标签、当前用户点赞状态）
 */
@Data
public class VideoVO {

    private Long id;

    private Long userId;

    /** 作者昵称 */
    private String authorName;

    /** 作者头像 */
    private String authorAvatar;

    private String title;

    private String description;

    private String videoUrl;

    private String coverUrl;

    private Integer duration;

    private Long categoryId;

    private String categoryName;

    /** 状态：0待审核 1已发布 2已下架 3审核拒绝 */
    private Integer status;

    private String rejectReason;

    private Long viewCount;

    private Long likeCount;

    private Long commentCount;

    private Long shareCount;

    private Long collectCount;

    private Integer isPublic;

    private Integer allowComment;

    private String geoLocation;

    /** 标签列表 */
    private List<String> tags;

    /** 当前登录用户是否已点赞 */
    private Boolean liked;

    /** 当前登录用户是否已关注作者 */
    private Boolean followed;

    /** 当前登录用户是否已收藏 */
    private Boolean collected;

    private LocalDateTime createTime;
}
