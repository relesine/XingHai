package com.xinghai.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 编辑视频请求（创作者修改自己的作品，仅允许改展示类字段）
 */
@Data
public class VideoUpdateDTO {

    /** 标题（为空表示不修改） */
    @Size(max = 128, message = "视频标题不能超过128字")
    private String title;

    /** 简介 */
    @Size(max = 512, message = "视频简介不能超过512字")
    private String description;

    /** 封面URL */
    private String coverUrl;

    /** 时长（秒） */
    private Integer duration;

    /** 发布位置 */
    @Size(max = 50, message = "位置信息过长")
    private String geoLocation;
}
