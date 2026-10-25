package com.xinghai.service;

import com.xinghai.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 限流服务单元测试：首次计数设置窗口、超限拒绝、Redis 故障降级放行
 */
class RateLimitServiceTest {

    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> ops = mock(ValueOperations.class);
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final RateLimitService service = new RateLimitService(redisTemplate);

    @Test
    @DisplayName("窗口内首次请求应设置过期时间")
    void firstRequestShouldSetExpire() {
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.increment("xinghai:rate:login:127.0.0.1")).thenReturn(1L);

        service.check("login", "127.0.0.1", 10, 60);

        verify(redisTemplate).expire("xinghai:rate:login:127.0.0.1", Duration.ofSeconds(60));
    }

    @Test
    @DisplayName("超过阈值应抛出 429 业务异常")
    void overLimitShouldThrow429() {
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.increment(anyString())).thenReturn(11L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.check("login", "10.0.0.1", 10, 60));

        assertEquals(429, ex.getCode());
    }

    @Test
    @DisplayName("Redis 不可用时应降级放行，不阻断业务")
    void redisFailureShouldDegrade() {
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("redis down"));

        assertDoesNotThrow(() -> service.check("login", "10.0.0.2", 10, 60));
    }
}
