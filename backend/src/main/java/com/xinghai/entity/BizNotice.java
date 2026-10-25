package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告表实体
 */
@Data
@TableName("biz_notice")
public class BizNotice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    /** 状态：0下线 1发布中 */
    private Integer status;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
