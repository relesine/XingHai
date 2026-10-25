package com.xinghai.vo;

import lombok.Data;

import java.util.List;

/**
 * 登录成功返回：令牌 + 用户基本信息 + 角色列表
 */
@Data
public class LoginVO {

    private String token;

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    /** 角色编码列表：USER / CREATOR / ADMIN */
    private List<String> roles;
}
