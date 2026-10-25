package com.xinghai.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * 安全响应头过滤器单元测试：接口响应补安全头且不缓存，静态资源不受 no-store 影响
 */
class SecurityHeadersFilterTest {

    private final SecurityHeadersFilter filter = new SecurityHeadersFilter();
    private final FilterChain chain = mock(FilterChain.class);

    @Test
    @DisplayName("接口请求应补齐安全头并禁止缓存")
    void apiRequestShouldAddSecurityHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/video/feed");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals("SAMEORIGIN", response.getHeader("X-Frame-Options"));
        assertEquals("strict-origin-when-cross-origin", response.getHeader("Referrer-Policy"));
        assertEquals("1; mode=block", response.getHeader("X-XSS-Protection"));
        assertEquals("geolocation=(), microphone=(), camera=()", response.getHeader("Permissions-Policy"));
        assertEquals("no-store", response.getHeader("Cache-Control"));
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("静态资源不设置 no-store，避免影响封面/视频缓存")
    void staticRequestShouldNotDisableCache() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/upload/cover/v1.svg");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertNull(response.getHeader("Cache-Control"));
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
    }
}
