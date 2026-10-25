package com.xinghai.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 发布视频请求
 */
@Data
public class VideoPublishDTO {

    @NotBlank(message = "视频标题不能为空")
    @Size(max = 128, message = "视频标题不能超过128字")
    private String title;

    @Size(max = 512, message = "视频简介不能超过512字")
    private String description;

    @NotNull(message = "请选择视频分类")
    private Long categoryId;

    /** 标签ID列表（用于推荐算法） */
    private List<Long> tagIds;

    /** 视频文件URL（由上传接口返回） */
    @NotBlank(message = "请先上传视频文件")
    private String videoUrl;

    /** 封面URL */
    private String coverUrl;

    /** 时长（秒） */
    private Integer duration;

    /** 是否公开：1公开 0私密 */
    private Integer isPublic;

    /** 是否允许评论：1允许 0禁止 */
    private Integer allowComment;

    /** 发布位置 */
    @Size(max = 50, message = "位置信息过长")
    private String geoLocation;
}
