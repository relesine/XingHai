package com.xinghai.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 全局安全响应头过滤器
 * <p>为所有响应补充基础安全头，降低 MIME 嗅探、点击劫持、信息泄露等风险。
 * 不启用严格 CSP，避免影响视频/图片等静态资源播放。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!response.containsHeader("X-Content-Type-Options")) {
            response.setHeader("X-Content-Type-Options", "nosniff");
        }
        if (!response.containsHeader("X-Frame-Options")) {
            response.setHeader("X-Frame-Options", "SAMEORIGIN");
        }
        if (!response.containsHeader("Referrer-Policy")) {
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        }
        if (!response.containsHeader("X-XSS-Protection")) {
            response.setHeader("X-XSS-Protection", "1; mode=block");
        }
        if (!response.containsHeader("Permissions-Policy")) {
            response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
        }
        // 接口响应不缓存，避免敏感数据被浏览器/代理缓存
        if (request.getRequestURI().startsWith("/api/")) {
            response.setHeader("Cache-Control", "no-store");
        }
        filterChain.doFilter(request, response);
    }
}
