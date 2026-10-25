package com.xinghai.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户信息 VO（对外展示，不含密码）
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private Integer gender;

    private String phone;

    private String email;

    private String signature;

    private String location;

    /** 是否认证创作者 */
    private Boolean creator;

    /** 角色编码列表 */
    private List<String> roles;

    /** 关注数 */
    private Long followCount;

    /** 粉丝数 */
    private Long fansCount;

    /** 作品数 */
    private Long videoCount;

    /** 当前登录用户是否已关注该用户（仅在查看他人主页时回填） */
    private Boolean followed;

    /** 上传配额（字节） */
    private Long quotaTotal;

    /** 已用配额（字节） */
    private Long quotaUsed;
}
