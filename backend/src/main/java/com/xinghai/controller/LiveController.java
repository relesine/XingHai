package com.xinghai.controller;

import com.xinghai.common.Result;
import com.xinghai.security.RequireRole;
import com.xinghai.service.LiveService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 直播接口（基础模拟）
 */
@RestController
@RequestMapping("/api/live")
public class LiveController {

    private final LiveService liveService;

    public LiveController(LiveService liveService) {
        this.liveService = liveService;
    }

    /** 直播间列表（未登录可看） */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(liveService.list());
    }

    /** 开播（创作者） */
    @PostMapping("/start")
    @RequireRole("CREATOR")
    public Result<Long> start(@RequestBody Map<String, String> body) {
        return Result.ok(liveService.start(body.get("title")));
    }

    /** 关播（主播或管理员） */
    @PostMapping("/stop/{roomId}")
    @RequireRole({"CREATOR", "ADMIN"})
    public Result<Void> stop(@PathVariable Long roomId) {
        liveService.stop(roomId);
        return Result.ok();
    }

    /** 模拟热度波动 */
    @PostMapping("/simulate")
    @RequireRole("ADMIN")
    public Result<Void> simulate() {
        liveService.simulate();
        return Result.ok();
    }
}
