package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 直播表实体（基础模拟，高阶对接真实推流）
 */
@Data
@TableName("biz_live_room")
public class BizLiveRoom {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String coverUrl;

    /** 状态：0未开播 1直播中 2已结束 */
    private Integer status;

    /** 观看人数（模拟） */
    private Integer viewerCount;

    /** 点赞数（模拟） */
    private Integer likeCount;

    /** 推流地址（高阶预留） */
    private String pushUrl;

    /** 播放地址（高阶预留） */
    private String playUrl;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
