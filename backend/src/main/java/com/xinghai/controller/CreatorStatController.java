package com.xinghai.controller;

import com.xinghai.common.Result;
import com.xinghai.security.RequireRole;
import com.xinghai.service.StatService;
import com.xinghai.vo.DashboardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 创作者数据看板接口
 */
@RestController
@RequestMapping("/api/creator")
public class CreatorStatController {

    private final StatService statService;

    public CreatorStatController(StatService statService) {
        this.statService = statService;
    }

    /** 创作者数据看板（累计+今日+近7天趋势） */
    @GetMapping("/dashboard")
    @RequireRole("CREATOR")
    public Result<DashboardVO> dashboard() {
        return Result.ok(statService.creatorDashboard());
    }
}
