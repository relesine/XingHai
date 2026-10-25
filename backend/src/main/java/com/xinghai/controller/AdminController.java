package com.xinghai.controller;

import com.xinghai.common.PageResult;
import com.xinghai.common.Result;
import com.xinghai.entity.SysPermission;
import com.xinghai.security.RequireRole;
import com.xinghai.service.AdminService;
import com.xinghai.service.StatService;
import com.xinghai.service.UserService;
import com.xinghai.vo.AdminStatsVO;
import com.xinghai.vo.UserVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理后台接口：数据大盘、用户管理、角色权限管理
 */
@RestController
@RequestMapping("/api/admin")
@RequireRole("ADMIN")
public class AdminController {

    private final StatService statService;
    private final AdminService adminService;
    private final UserService userService;

    public AdminController(StatService statService, AdminService adminService, UserService userService) {
        this.statService = statService;
        this.adminService = adminService;
        this.userService = userService;
    }

    /** 数据大盘 */
    @GetMapping("/stats")
    public Result<AdminStatsVO> stats() {
        return Result.ok(statService.adminStats());
    }

    /** 用户列表 */
    @GetMapping("/users")
    public Result<PageResult<UserVO>> users(@RequestParam(required = false) String keyword,
                                            @RequestParam(defaultValue = "1") long pageNum,
                                            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(adminService.userList(keyword, pageNum, pageSize));
    }

    /** 禁用/启用用户 */
    @PutMapping("/user/status/{userId}")
    public Result<Void> changeStatus(@PathVariable Long userId, @RequestBody Map<String, Integer> body) {
        userService.changeStatus(userId, body.get("status"));
        return Result.ok();
    }

    /** 给用户分配角色（如认证创作者） */
    @PostMapping("/user/role")
    public Result<Void> assignRole(@RequestBody Map<String, Long> body) {
        adminService.assignRole(body.get("userId"), body.get("roleId"));
        return Result.ok();
    }

    /** 角色列表（含权限） */
    @GetMapping("/roles")
    public Result<List<Map<String, Object>>> roles() {
        return Result.ok(adminService.roleListWithPermissions());
    }

    /** 全部权限点 */
    @GetMapping("/permissions")
    public Result<List<SysPermission>> permissions() {
        return Result.ok(adminService.permissionList());
    }

    /** 角色授权（覆盖式） */
    @PostMapping("/role/permissions")
    public Result<Void> assignPermissions(@RequestBody Map<String, Object> body) {
        Long roleId = Long.valueOf(body.get("roleId").toString());
        @SuppressWarnings("unchecked")
        List<Long> permissionIds = (List<Long>) body.get("permissionIds");
        adminService.assignPermissions(roleId, permissionIds);
        return Result.ok();
    }
}
