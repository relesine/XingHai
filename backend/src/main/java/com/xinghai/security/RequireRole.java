package com.xinghai.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解：标注在 Controller 方法上，声明访问所需角色
 * <p>示例：@RequireRole({"CREATOR"}) 表示只有认证创作者可访问</p>
 * <p>与 JwtInterceptor 配合实现按钮级/接口级权限控制</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /** 允许访问的角色编码数组（满足其一即可） */
    String[] value();
}
