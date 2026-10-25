package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论表实体
 */
@Data
@TableName("biz_comment")
public class BizComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long videoId;

    private Long userId;

    /** 父评论ID：0=一级评论，>0=回复 */
    private Long parentId;

    private String content;

    private Integer likeCount;

    /** 状态：0隐藏 1正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
