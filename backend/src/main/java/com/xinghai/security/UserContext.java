package com.xinghai.security;

/**
 * 当前登录用户上下文（ThreadLocal 实现）
 * <p>JwtInterceptor 放行后写入当前用户ID；Controller/Service 通过该工具读取，避免每个方法传参。</p>
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLES = new ThreadLocal<>();

    public static void set(Long userId, String username, String roles) {
        USER_ID.set(userId);
        USERNAME.set(username);
        ROLES.set(roles);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    /** 角色编码串（逗号分隔，如 USER,CREATOR） */
    public static String getRoles() {
        return ROLES.get();
    }

    /** 判断是否包含某角色（按逗号精确匹配，避免子串误判，如 USER 误匹配 CREATOR） */
    public static boolean hasRole(String roleCode) {
        String roles = ROLES.get();
        if (roles == null || roleCode == null) {
            return false;
        }
        for (String code : roles.split(",")) {
            if (code.trim().equals(roleCode)) {
                return true;
            }
        }
        return false;
    }

    /** 请求结束必须清理，防止线程池复用导致用户串号 */
    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        ROLES.remove();
    }
}
