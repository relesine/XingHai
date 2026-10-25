package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 视频-标签关联表实体
 */
@Data
@TableName("biz_video_tag")
public class BizVideoTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long videoId;

    private Long tagId;
}
