package com.xinghai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinghai.entity.SysRole;
import com.xinghai.entity.SysUserRole;
import com.xinghai.mapper.SysRoleMapper;
import com.xinghai.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色服务：查询用户角色、绑定角色
 */
@Service
public class SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    public SysRoleService(SysRoleMapper roleMapper, SysUserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    /** 查询用户拥有的全部角色 */
    public List<SysRole> listByUserId(Long userId) {
        return roleMapper.listByUserId(userId);
    }

    /** 按编码查询角色（用于注册时绑定默认角色） */
    public SysRole getByCode(String code) {
        return roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, code));
    }

    /** 给用户绑定角色 */
    public void bindUserRole(Long userId, Long roleId) {
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        userRoleMapper.insert(ur);
    }

    /** 全部角色 */
    public List<SysRole> listAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));
    }
}
