package com.xinghai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinghai.entity.*;
import com.xinghai.mapper.*;
import com.xinghai.security.UserContext;
import com.xinghai.vo.AdminStatsVO;
import com.xinghai.vo.DashboardVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务：创作者看板 + 管理后台大盘
 */
@Service
public class StatService {

    private final BizVideoMapper videoMapper;
    private final BizLikeMapper likeMapper;
    private final BizFollowMapper followMapper;
    private final BizOrderMapper orderMapper;
    private final BizShopMapper shopMapper;
    private final BizCreatorStatMapper creatorStatMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final BizCommentMapper commentMapper;
    private final BizReportMapper reportMapper;
    private final BizCategoryMapper categoryMapper;

    public StatService(BizVideoMapper videoMapper, BizLikeMapper likeMapper, BizFollowMapper followMapper,
                       BizOrderMapper orderMapper, BizShopMapper shopMapper, BizCreatorStatMapper creatorStatMapper,
                       SysUserMapper userMapper, SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper,
                       BizCommentMapper commentMapper, BizReportMapper reportMapper,
                       BizCategoryMapper categoryMapper) {
        this.videoMapper = videoMapper;
        this.likeMapper = likeMapper;
        this.followMapper = followMapper;
        this.orderMapper = orderMapper;
        this.shopMapper = shopMapper;
        this.creatorStatMapper = creatorStatMapper;
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.commentMapper = commentMapper;
        this.reportMapper = reportMapper;
        this.categoryMapper = categoryMapper;
    }

    /**
     * 创作者数据看板：累计指标 + 今日指标 + 近7天趋势（biz_creator_stat）
     */
    public DashboardVO creatorDashboard() {
        Long userId = UserContext.getUserId();
        DashboardVO vo = new DashboardVO();

        // 累计指标（实时统计）
        List<BizVideo> myVideos = videoMapper.selectList(new LambdaQueryWrapper<BizVideo>()
                .eq(BizVideo::getUserId, userId));
        long totalViews = myVideos.stream().mapToLong(BizVideo::getViewCount).sum();
        long totalLikes = myVideos.stream().mapToLong(BizVideo::getLikeCount).sum();
        vo.setTotalViews(totalViews);
        vo.setTotalLikes(totalLikes);
        vo.setTotalVideos((long) myVideos.size());
        vo.setTotalFans(followMapper.selectCount(new LambdaQueryWrapper<BizFollow>()
                .eq(BizFollow::getFollowUserId, userId)));

        BizShop shop = shopMapper.selectOne(new LambdaQueryWrapper<BizShop>().eq(BizShop::getUserId, userId));
        if (shop != null) {
            List<BizOrder> paidOrders = orderMapper.selectList(new LambdaQueryWrapper<BizOrder>()
                    .eq(BizOrder::getShopId, shop.getId())
                    .in(BizOrder::getStatus, 1, 2, 3));
            vo.setTotalSales(paidOrders.stream()
                    .map(BizOrder::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        } else {
            vo.setTotalSales(BigDecimal.ZERO);
        }

        // 今日指标（从按天统计表取）
        LocalDate today = LocalDate.now();
        BizCreatorStat todayStat = creatorStatMapper.selectOne(new LambdaQueryWrapper<BizCreatorStat>()
                .eq(BizCreatorStat::getUserId, userId)
                .eq(BizCreatorStat::getStatDate, today));
        vo.setTodayViews(todayStat == null ? 0L : todayStat.getViews());
        vo.setTodayLikes(todayStat == null ? 0L : todayStat.getLikes());
        vo.setTodayComments(todayStat == null ? 0L : todayStat.getComments());

        // 近7天趋势（补零对齐）
        List<Map<String, Object>> trend = new ArrayList<>();
        List<BizCreatorStat> stats = creatorStatMapper.selectList(new LambdaQueryWrapper<BizCreatorStat>()
                .eq(BizCreatorStat::getUserId, userId)
                .ge(BizCreatorStat::getStatDate, today.minusDays(6))
                .orderByAsc(BizCreatorStat::getStatDate));
        Map<LocalDate, BizCreatorStat> map = new HashMap<>();
        for (BizCreatorStat s : stats) {
            map.put(s.getStatDate(), s);
        }
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            BizCreatorStat s = map.get(d);
            Map<String, Object> item = new HashMap<>();
            item.put("date", d.format(DateTimeFormatter.ISO_LOCAL_DATE));
            item.put("views", s == null ? 0 : s.getViews());
            item.put("likes", s == null ? 0 : s.getLikes());
            item.put("fans", s == null ? 0 : s.getFans());
            trend.add(item);
        }
        vo.setTrend(trend);
        return vo;
    }

    /**
     * 管理后台大盘：总量指标 + 趋势 + 分类分布
     */
    public AdminStatsVO adminStats() {
        AdminStatsVO vo = new AdminStatsVO();

        vo.setUserCount(userMapper.selectCount(null));
        // 创作者数量：绑定 CREATOR 角色的用户数
        SysRole creatorRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, "CREATOR"));
        vo.setCreatorCount(creatorRole == null ? 0L :
                userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, creatorRole.getId())));
        vo.setVideoCount(videoMapper.selectCount(null));
        vo.setPendingVideoCount(videoMapper.selectCount(new LambdaQueryWrapper<BizVideo>()
                .eq(BizVideo::getStatus, 0)));
        vo.setOrderCount(orderMapper.selectCount(null));
        List<BizOrder> paid = orderMapper.selectList(new LambdaQueryWrapper<BizOrder>()
                .in(BizOrder::getStatus, 1, 2, 3));
        vo.setTotalSales(paid.stream().map(BizOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setPendingReport(reportMapper.selectCount(new LambdaQueryWrapper<BizReport>()
                .eq(BizReport::getStatus, 0)));

        LocalDate today = LocalDate.now();
        vo.setTodayNewUser(userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .ge(SysUser::getCreateTime, today.atStartOfDay())));
        vo.setTodayNewVideo(videoMapper.selectCount(new LambdaQueryWrapper<BizVideo>()
                .ge(BizVideo::getCreateTime, today.atStartOfDay())));

        vo.setUserTrend(trendByDay(today, "user"));
        vo.setVideoTrend(trendByDay(today, "video"));

        List<Map<String, Object>> catDist = new ArrayList<>();
        for (BizCategory cate : categoryMapper.selectList(null)) {
            Long count = videoMapper.selectCount(new LambdaQueryWrapper<BizVideo>()
                    .eq(BizVideo::getCategoryId, cate.getId()));
            if (count > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", cate.getName());
                item.put("value", count);
                catDist.add(item);
            }
        }
        vo.setCategoryDist(catDist);
        return vo;
    }

    /** 近7天每日新增数量（user/video 通用，type=user|video） */
    private List<Map<String, Object>> trendByDay(LocalDate today, String type) {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            Long count;
            if ("user".equals(type)) {
                count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                        .ge(SysUser::getCreateTime, d.atStartOfDay())
                        .lt(SysUser::getCreateTime, d.plusDays(1).atStartOfDay()));
            } else {
                count = videoMapper.selectCount(new LambdaQueryWrapper<BizVideo>()
                        .ge(BizVideo::getCreateTime, d.atStartOfDay())
                        .lt(BizVideo::getCreateTime, d.plusDays(1).atStartOfDay()));
            }
            Map<String, Object> item = new HashMap<>();
            item.put("date", d.format(f));
            item.put("value", count == null ? 0 : count);
            result.add(item);
        }
        return result;
    }
}
