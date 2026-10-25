package com.xinghai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xinghai.entity.SysRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色表 Mapper
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询用户拥有的全部角色（多表联查：sys_user_role -> sys_role）
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<SysRole> listByUserId(@Param("userId") Long userId);
}
