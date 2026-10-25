package com.xinghai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xinghai.entity.SysPermission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限表 Mapper
 */
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 查询角色拥有的全部权限（多表联查）
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON rp.permission_id = p.id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = 0")
    List<SysPermission> listByRoleId(@Param("roleId") Long roleId);
}
