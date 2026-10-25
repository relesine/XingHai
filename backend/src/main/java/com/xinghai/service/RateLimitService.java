package com.xinghai.service;

import com.xinghai.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 基于 Redis 的固定窗口限流
 * <p>用于登录、注册等敏感入口，防止暴力破解与恶意刷接口。
 * Redis 不可用时自动降级放行，保证业务可用性。</p>
 */
@Slf4j
@Service
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 计数并校验
     *
     * @param scope         业务标识（如 login / register）
     * @param identity      访问者标识（通常为客户端 IP）
     * @param limit         窗口内允许的最大次数
     * @param windowSeconds 窗口长度（秒）
     */
    public void check(String scope, String identity, int limit, int windowSeconds) {
        String key = "xinghai:rate:" + scope + ":" + identity;
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }
            if (count != null && count > limit) {
                throw new BusinessException(429, "操作过于频繁，请稍后再试");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // 限流组件异常不应阻断正常业务
            log.warn("限流组件不可用，已放行请求: {}", e.getMessage());
        }
    }
}
