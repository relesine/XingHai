package com.xinghai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinghai.common.PageResult;
import com.xinghai.entity.*;
import com.xinghai.mapper.*;
import com.xinghai.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台服务：用户管理、角色权限管理
 */
@Service
public class AdminService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final UserService userService;

    public AdminService(SysUserMapper userMapper, SysRoleMapper roleMapper,
                        SysPermissionMapper permissionMapper, SysUserRoleMapper userRoleMapper,
                        SysRolePermissionMapper rolePermissionMapper, UserService userService) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userService = userService;
    }

    /** 用户分页列表（可按用户名/昵称搜索） */
    public PageResult<UserVO> userList(String keyword, Long pageNum, Long pageSize) {
        Page<SysUser> page = userMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysUser>()
                        .and(keyword != null && !keyword.isBlank(),
                                w -> w.like(SysUser::getUsername, keyword)
                                        .or().like(SysUser::getNickname, keyword))
                        .orderByDesc(SysUser::getCreateTime));
        return PageResult.of(page.convert(userService::toVO));
    }

    /** 角色列表 + 每个角色拥有的权限编码（权限管理页用） */
    public List<Map<String, Object>> roleListWithPermissions() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId))
                .stream().map(role -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", role.getId());
                    m.put("code", role.getCode());
                    m.put("name", role.getName());
                    m.put("remark", role.getRemark());
                    List<String> perms = permissionMapper.listByRoleId(role.getId())
                            .stream().map(SysPermission::getCode).collect(Collectors.toList());
                    m.put("permissions", perms);
                    return m;
                }).collect(Collectors.toList());
    }

    /** 全部权限点 */
    public List<SysPermission> permissionList() {
        return permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getSort));
    }

    /** 给角色重新授权（先清后插） */
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));
        if (permissionIds != null) {
            for (Long pid : permissionIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(pid);
                rolePermissionMapper.insert(rp);
            }
        }
    }

    /** 给用户分配角色（管理员操作，如认证创作者） */
    @Transactional
    public void assignRole(Long userId, Long roleId) {
        Long exist = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, roleId));
        if (exist == 0) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }
}
