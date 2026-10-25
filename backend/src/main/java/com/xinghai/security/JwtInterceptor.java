package com.xinghai.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinghai.common.Result;
import com.xinghai.common.ResultCode;
import com.xinghai.entity.SysRole;
import com.xinghai.entity.SysUser;
import com.xinghai.service.SysRoleService;
import com.xinghai.service.UserService;
import com.xinghai.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 登录认证 + RBAC 角色鉴权 拦截器
 * <p>执行顺序：先验登录（解析Token）→ 再查角色（@RequireRole 注解）→ 通过则放行并写入 UserContext</p>
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final SysRoleService sysRoleService;
    private final ObjectMapper objectMapper;

    @Value("${xinghai.jwt.header}")
    private String header;

    @Value("${xinghai.jwt.prefix}")
    private String prefix;

    public JwtInterceptor(JwtUtil jwtUtil, UserService userService,
                          SysRoleService sysRoleService, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.sysRoleService = sysRoleService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行非控制器请求（如静态资源）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String uri = request.getRequestURI();
        boolean optional = isOptionalAuth(uri);

        String token = request.getHeader(header);
        if (token == null || token.isEmpty()) {
            // 免登录接口：未登录直接放行（匿名访问）；其余接口要求登录
            if (optional) {
                return true;
            }
            return writeUnauthorized(response, "未登录，请先登录");
        }
        if (token.startsWith(prefix)) {
            token = token.substring(prefix.length());
        }

        Claims claims;
        try {
            claims = jwtUtil.parseToken(token);
        } catch (Exception e) {
            log.warn("Token 校验失败: {}", e.getMessage());
            // 免登录接口：token 无效按匿名放行（如详情页可匿名浏览）；其余接口拒绝
            if (optional) {
                return true;
            }
            return writeUnauthorized(response, "登录已过期，请重新登录");
        }

        Long userId = Long.valueOf(claims.getSubject());
        // 校验用户是否存在且未被禁用（防止用户删除/禁用后旧令牌仍可用）
        SysUser user = userService.getById(userId);
        if (user == null || user.getStatus() == 0) {
            return writeUnauthorized(response, "账号不存在或已被禁用");
        }

        List<SysRole> roles = sysRoleService.listByUserId(userId);
        // 精确匹配：按逗号拆分角色编码，避免 contains 子串误判
        java.util.Set<String> roleCodeSet = roles.stream()
                .map(SysRole::getCode)
                .collect(Collectors.toSet());

        // RBAC 角色鉴权：类级或方法级标注了 @RequireRole 时校验
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            RequireRole requireRole = hm.getMethodAnnotation(RequireRole.class);
            if (requireRole == null) {
                requireRole = hm.getBeanType().getAnnotation(RequireRole.class);
            }
            if (requireRole != null && requireRole.value().length > 0) {
                boolean pass = false;
                for (String role : requireRole.value()) {
                    if (roleCodeSet.contains(role)) {
                        pass = true;
                        break;
                    }
                }
                if (!pass) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(objectMapper.writeValueAsString(Result.fail(ResultCode.FORBIDDEN)));
                    return false;
                }
            }
        }

        UserContext.set(userId, user.getUsername(), String.join(",", roleCodeSet));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束清理 ThreadLocal，防止线程复用串号
        UserContext.clear();
    }

    /** 输出 401 统一结构 */
    private boolean writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(ResultCode.UNAUTHORIZED.getCode(), message)));
        return false;
    }

    /**
     * 是否免登录接口（可匿名访问；若请求带合法 token 也解析并写入 UserContext，
     * 使详情页/信息流等接口在登录后能正确返回 liked/collected/followed 等用户状态字段）
     */
    private boolean isOptionalAuth(String uri) {
        String[] optionalPrefixes = {
                "/api/auth/login", "/api/auth/register",
                "/api/video/feed", "/api/video/detail", "/api/video/related",
                "/api/video/search", "/api/video/category", "/api/video/user",
                "/api/category", "/api/tag", "/api/notice", "/api/advert",
                "/api/user/info", "/api/comment/list", "/api/live/list",
                "/api/product/list", "/api/product/detail", "/api/shop/list", "/api/shop/detail"
        };
        for (String p : optionalPrefixes) {
            if (uri.startsWith(p)) {
                return true;
            }
        }
        return false;
    }
}
