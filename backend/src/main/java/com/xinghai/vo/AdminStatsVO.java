package com.xinghai.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 管理后台数据大盘 VO
 */
@Data
public class AdminStatsVO {

    /** 用户总数 */
    private Long userCount;

    /** 创作者数量 */
    private Long creatorCount;

    /** 视频总数 */
    private Long videoCount;

    /** 待审核视频数 */
    private Long pendingVideoCount;

    /** 订单总数 */
    private Long orderCount;

    /** 总成交金额 */
    private java.math.BigDecimal totalSales;

    /** 今日新增用户 */
    private Long todayNewUser;

    /** 今日新增视频 */
    private Long todayNewVideo;

    /** 近7天新增用户趋势 */
    private List<Map<String, Object>> userTrend;

    /** 近7天新增视频趋势 */
    private List<Map<String, Object>> videoTrend;

    /** 分类视频分布（饼图用） */
    private List<Map<String, Object>> categoryDist;

    /** 待处理举报数 */
    private Long pendingReport;
}
