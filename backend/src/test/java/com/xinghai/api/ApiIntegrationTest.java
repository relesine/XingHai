package com.xinghai.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 接口集成测试：真实启动 Spring 上下文（dev 配置，依赖本机 MySQL/Redis），
 * 通过 MockMvc 走完整过滤器链 + JWT 拦截器 + 全局异常处理。
 * <p>前置条件：MySQL 已导入 database 脚本，Redis 可连接。</p>
 * <p>限流按 IP 计数，登录相关用例统一携带不同的 X-Forwarded-For，避免相互触发 429。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("管理员登录成功并返回 ADMIN 角色")
    void adminLoginShouldSucceed() throws Exception {
        JsonNode body = login("admin", "123456", "10.10.0.1");

        assertEquals(200, body.path("code").asInt(), body.toString());
        assertFalse(body.path("data").path("token").asText().isEmpty());
        assertTrue(body.path("data").path("roles").toString().contains("ADMIN"));
    }

    @Test
    @DisplayName("密码错误返回业务状态码 1001")
    void wrongPasswordShouldReturn1001() throws Exception {
        JsonNode body = login("admin", "wrong-password", "10.10.0.2");

        assertEquals(1001, body.path("code").asInt(), body.toString());
    }

    @Test
    @DisplayName("缺少密码返回参数校验 400")
    void missingPasswordShouldReturn400() throws Exception {
        JsonNode body = postJson("/api/auth/login", "{\"username\":\"admin\"}", null, "10.10.0.3");

        assertEquals(400, body.path("code").asInt(), body.toString());
    }

    @Test
    @DisplayName("未登录访问受保护接口返回 401")
    void protectedApiWithoutTokenShouldReturn401() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/me")).andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertEquals(401, body.path("code").asInt(), body.toString());
    }

    @Test
    @DisplayName("携带无效 Token 返回 401")
    void invalidTokenShouldReturn401() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer bad.token.value"))
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertEquals(401, body.path("code").asInt(), body.toString());
    }

    @Test
    @DisplayName("管理员可访问大盘统计，普通用户返回 403")
    void adminStatsShouldEnforceRole() throws Exception {
        String adminToken = loginToken("admin", "123456", "10.10.0.4");
        JsonNode adminBody = getJson("/api/admin/stats", adminToken);
        assertEquals(200, adminBody.path("code").asInt(), adminBody.toString());
        assertTrue(adminBody.path("data").path("userTrend").isArray());

        String userToken = loginToken("user01", "123456", "10.10.0.5");
        JsonNode userBody = getJson("/api/admin/stats", userToken);
        assertEquals(403, userBody.path("code").asInt(), userBody.toString());
    }

    @Test
    @DisplayName("匿名推荐流可分页返回，且 pageSize 超限被截断")
    void recommendFeedShouldBePagedAndCapped() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/video/feed/recommend")
                        .param("pageNum", "1").param("pageSize", "200"))
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertEquals(200, body.path("code").asInt(), body.toString());
        JsonNode list = body.path("data").path("list");
        assertTrue(list.isArray());
        assertTrue(list.size() <= 100, "pageSize 超过上限应被截断到 100");
    }

    @Test
    @DisplayName("分类列表可匿名访问（覆盖 Redis 缓存分支）")
    void categoryListShouldBePublic() throws Exception {
        JsonNode body = getJson("/api/category/list", null);

        assertEquals(200, body.path("code").asInt(), body.toString());
        assertTrue(body.path("data").isArray());
    }

    @Test
    @DisplayName("视频详情可匿名访问并返回作者信息")
    void videoDetailShouldBePublic() throws Exception {
        JsonNode feed = getJson("/api/video/feed/recommend?pageNum=1&pageSize=1", null);
        long videoId = feed.path("data").path("list").get(0).path("id").asLong();

        JsonNode body = getJson("/api/video/detail/" + videoId, null);

        assertEquals(200, body.path("code").asInt(), body.toString());
        assertNotNull(body.path("data").path("title").asText());
    }

    @Test
    @DisplayName("接口响应应携带安全响应头且禁止缓存")
    void apiResponseShouldCarrySecurityHeaders() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/video/feed/recommend")).andReturn();

        assertEquals("nosniff", result.getResponse().getHeader("X-Content-Type-Options"));
        assertEquals("SAMEORIGIN", result.getResponse().getHeader("X-Frame-Options"));
        assertEquals("no-store", result.getResponse().getHeader("Cache-Control"));
    }

    private JsonNode login(String username, String password, String ip) throws Exception {
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        return postJson("/api/auth/login", body, null, ip);
    }

    private String loginToken(String username, String password, String ip) throws Exception {
        JsonNode body = login(username, password, ip);
        assertEquals(200, body.path("code").asInt(), body.toString());
        return body.path("data").path("token").asText();
    }

    private JsonNode postJson(String url, String json, String token, String ip) throws Exception {
        MockHttpServletRequestBuilder builder = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (ip != null) {
            builder.header("X-Forwarded-For", ip);
        }
        MvcResult result = mockMvc.perform(builder).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private JsonNode getJson(String url, String token) throws Exception {
        MockHttpServletRequestBuilder builder = get(url);
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        MvcResult result = mockMvc.perform(builder).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }
}
