package com.xinghai.controller;

import com.xinghai.common.Result;
import com.xinghai.service.UserService;
import com.xinghai.vo.UserVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户接口：资料、关注/粉丝列表、公开信息
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 当前登录用户完整资料 */
    @GetMapping("/profile")
    public Result<UserVO> profile() {
        return Result.ok(userService.getCurrentUser());
    }

    /** 公开用户信息（未登录可访问） */
    @GetMapping("/info/{userId}")
    public Result<UserVO> info(@PathVariable Long userId) {
        return Result.ok(userService.getPublicInfo(userId));
    }

    /** 修改个人资料 */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, Object> body) {
        userService.updateProfile(
                (String) body.get("nickname"),
                (String) body.get("signature"),
                body.get("gender") == null ? null : Integer.valueOf(body.get("gender").toString()),
                (String) body.get("location"));
        return Result.ok();
    }

    /** 修改头像 */
    @PutMapping("/avatar")
    public Result<Void> updateAvatar(@RequestBody Map<String, String> body) {
        userService.updateAvatar(body.get("avatar"));
        return Result.ok();
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> body) {
        userService.updatePassword(body.get("oldPassword"), body.get("newPassword"));
        return Result.ok();
    }

    /** 某用户的关注列表 */
    @GetMapping("/{userId}/following")
    public Result<List<UserVO>> following(@PathVariable Long userId) {
        return Result.ok(userService.listFollowing(userId));
    }

    /** 某用户的粉丝列表 */
    @GetMapping("/{userId}/fans")
    public Result<List<UserVO>> fans(@PathVariable Long userId) {
        return Result.ok(userService.listFans(userId));
    }
}
