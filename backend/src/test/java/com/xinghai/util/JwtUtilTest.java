package com.xinghai.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 单元测试：验证令牌生成、解析与过期校验
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "XingHai-Unit-Test-Secret-Key-At-Least-32-Bytes-Long");
        ReflectionTestUtils.setField(jwtUtil, "expire", 60_000L);
    }

    @Test
    void generateAndParseToken() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN,USER");
        assertNotNull(token);

        Claims claims = jwtUtil.parseToken(token);
        assertEquals("1", claims.getSubject());
        assertEquals("admin", claims.get("username", String.class));
        assertEquals("ADMIN,USER", claims.get("roles", String.class));
    }

    @Test
    void expiredTokenShouldThrow() {
        ReflectionTestUtils.setField(jwtUtil, "expire", -1_000L);
        String token = jwtUtil.generateToken(2L, "user01", "USER");
        assertThrows(Exception.class, () -> jwtUtil.parseToken(token));
    }

    @Test
    void tamperedTokenShouldThrow() {
        String token = jwtUtil.generateToken(3L, "creator01", "CREATOR");
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertThrows(Exception.class, () -> jwtUtil.parseToken(tampered));
    }
}
