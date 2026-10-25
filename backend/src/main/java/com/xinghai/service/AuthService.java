package com.xinghai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinghai.common.ResultCode;
import com.xinghai.dto.LoginDTO;
import com.xinghai.dto.RegisterDTO;
import com.xinghai.entity.SysRole;
import com.xinghai.entity.SysUser;
import com.xinghai.exception.BusinessException;
import com.xinghai.mapper.SysUserMapper;
import com.xinghai.util.JwtUtil;
import com.xinghai.vo.LoginVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务：注册 + 登录 + 注销
 */
@Service
public class AuthService {

    /** 普通用户默认上传配额：10GB（字节） */
    private static final long DEFAULT_QUOTA = 10L * 1024 * 1024 * 1024;

    private final SysUserMapper userMapper;
    private final SysRoleService roleService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(SysUserMapper userMapper, SysRoleService roleService, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.roleService = roleService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 注册：校验用户名唯一 → 创建用户（默认10GB配额、USER角色）→ 返回自动登录令牌
     */
    @Transactional
    public LoginVO register(RegisterDTO dto) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        // 密码 BCrypt 加密存储（单向不可逆，数据库泄露也无法直接还原明文）
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setLocation(dto.getLocation());
        user.setQuotaTotal(DEFAULT_QUOTA);
        user.setQuotaUsed(0L);
        user.setStatus(1);
        user.setGender(0);
        userMapper.insert(user);

        SysRole userRole = roleService.getByCode("USER");
        roleService.bindUserRole(user.getId(), userRole.getId());

        return buildLoginVO(user);
    }

    /**
     * 登录：校验用户名密码 → 校验账号状态 → 生成令牌
     */
    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            // 不区分"用户名不存在"与"密码错误"，防止恶意探测账号
            throw new BusinessException(ResultCode.LOGIN_FAIL);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        return buildLoginVO(user);
    }

    /** 组装登录结果：令牌 + 用户信息 + 角色 */
    private LoginVO buildLoginVO(SysUser user) {
        List<SysRole> roles = roleService.listByUserId(user.getId());
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRoles(roles.stream().map(SysRole::getCode).collect(Collectors.toList()));
        String roleStr = roles.stream().map(SysRole::getCode).collect(Collectors.joining(","));
        vo.setToken(jwtUtil.generateToken(user.getId(), user.getUsername(), roleStr));
        return vo;
    }
}
