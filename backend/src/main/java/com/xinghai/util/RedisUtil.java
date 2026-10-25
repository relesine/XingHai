package com.xinghai.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 通用缓存工具：封装常用操作，业务层无需直接接触 RedisTemplate
 */
@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 写入字符串（带过期时间，单位秒） */
    public void set(String key, Object value, long seconds) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    /** 写入字符串（永不过期） */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /** 读取 */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /** 删除单个 key */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /** 批量删除 key */
    public void delete(java.util.Collection<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /** 按通配符（如 cache:tag:list:*）查询所有匹配的 key */
    public java.util.Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /** 自增（不存在则从0开始） */
    public long increment(String key) {
        Long v = redisTemplate.opsForValue().increment(key);
        return v == null ? 0 : v;
    }

    /** 自增（带过期时间） */
    public long increment(String key, long seconds) {
        Long v = redisTemplate.opsForValue().increment(key);
        if (v != null && v == 1) {
            expire(key, seconds);
        }
        return v == null ? 0 : v;
    }

    /** 设置过期时间（秒） */
    public void expire(String key, long seconds) {
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    /** Set 添加成员（用于去重集合） */
    public boolean sadd(String key, Object... values) {
        Long v = redisTemplate.opsForSet().add(key, values);
        return v != null && v > 0;
    }

    /** 判断 Set 是否包含成员 */
    public boolean sisMember(String key, Object value) {
        Boolean v = redisTemplate.opsForSet().isMember(key, value);
        return Boolean.TRUE.equals(v);
    }

    /** 判断 key 是否存在 */
    public boolean exists(String key) {
        Boolean v = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(v);
    }
}
