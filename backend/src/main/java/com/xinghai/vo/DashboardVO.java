package com.xinghai.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 创作者数据看板 VO
 */
@Data
public class DashboardVO {

    /** 累计播放 */
    private Long totalViews;

    /** 累计获赞 */
    private Long totalLikes;

    /** 粉丝总数 */
    private Long totalFans;

    /** 作品总数 */
    private Long totalVideos;

    /** 总销售额 */
    private java.math.BigDecimal totalSales;

    /** 今日新增播放 */
    private Long todayViews;

    /** 今日新增获赞 */
    private Long todayLikes;

    /** 今日新增评论 */
    private Long todayComments;

    /** 近7天趋势（日期 -> 播放量），供前端画折线图 */
    private List<Map<String, Object>> trend;
}
