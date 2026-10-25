package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 店铺表实体
 */
@Data
@TableName("biz_shop")
public class BizShop {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 店主用户ID（创作者） */
    private Long userId;

    private String shopName;

    private String logo;

    private String description;

    /** 状态：0已关闭 1营业中 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
