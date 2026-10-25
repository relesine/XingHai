package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频表实体（核心业务表）
 */
@Data
@TableName("biz_video")
public class BizVideo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布者用户ID */
    private Long userId;

    private String title;

    private String description;

    private String videoUrl;

    private String coverUrl;

    /** 时长（秒） */
    private Integer duration;

    private Long categoryId;

    /** 状态：0待审核 1已发布 2已下架 3审核拒绝 */
    private Integer status;

    /** 审核拒绝原因 */
    private String rejectReason;

    private Long viewCount;

    private Long likeCount;

    private Long commentCount;

    private Long shareCount;

    private Long collectCount;

    /** 是否公开：1公开 0私密 */
    private Integer isPublic;

    /** 是否允许评论：1允许 0禁止 */
    private Integer allowComment;

    /** 发布位置（同城频道） */
    private String geoLocation;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
