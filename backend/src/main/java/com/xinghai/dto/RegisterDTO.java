package com.xinghai.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 注册请求
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度需在3-20之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在6-32之间")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 32, message = "昵称长度不能超过32")
    private String nickname;

    /** 所在城市（可选，用于同城频道） */
    @Size(max = 50, message = "城市信息过长")
    private String location;
}
