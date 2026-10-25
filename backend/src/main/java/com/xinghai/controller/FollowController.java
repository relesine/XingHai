package com.xinghai.controller;

import com.xinghai.common.Result;
import com.xinghai.service.FollowService;
import org.springframework.web.bind.annotation.*;

/**
 * 关注接口：关注/取关
 */
@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    /** 关注 */
    @PostMapping("/{userId}")
    public Result<Void> follow(@PathVariable Long userId) {
        followService.follow(userId);
        return Result.ok();
    }

    /** 取关 */
    @DeleteMapping("/{userId}")
    public Result<Void> unfollow(@PathVariable Long userId) {
        followService.unfollow(userId);
        return Result.ok();
    }
}
