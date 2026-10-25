package com.xinghai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表实体
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** 密码（BCrypt加密存储，永不返回前端） */
    private String password;

    private String nickname;

    private String avatar;

    /** 性别：0未知 1男 2女 */
    private Integer gender;

    private String phone;

    private String email;

    private String signature;

    /** 所在城市（同城频道用） */
    private String location;

    /** 上传总配额（字节） */
    private Long quotaTotal;

    /** 已用配额（字节） */
    private Long quotaUsed;

    /** 账号状态：0禁用 1正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
