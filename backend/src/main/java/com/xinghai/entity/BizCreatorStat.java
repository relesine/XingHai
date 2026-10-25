package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 创作者数据统计表实体（按天汇总，支撑数据看板图表）
 */
@Data
@TableName("biz_creator_stat")
public class BizCreatorStat {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate statDate;

    /** 当日新增浏览量 */
    private Long views;

    /** 当日新增点赞数 */
    private Long likes;

    /** 当日粉丝数（存量快照） */
    private Long fans;

    /** 当日新增评论数 */
    private Long comments;

    /** 当日成交金额 */
    private BigDecimal salesAmount;

    private LocalDateTime createTime;
}
