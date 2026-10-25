package com.xinghai.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置：key 用字符串序列化（便于查看），value 用 JSON 序列化（跨语言可读）
 * <p>注意：实体含 LocalDateTime，必须注册 JavaTimeModule（jsr310），
 * 否则缓存实体时会报 "Java 8 date/time type not supported"。</p>
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key / hash key 使用 String 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // value 使用 JSON 序列化，并显式注册 JavaTimeModule 以支持 LocalDateTime
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(buildJsonSerializer());
        template.setHashValueSerializer(buildJsonSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /** 构造支持 Java8 时间类型、带类型信息的 JSON 序列化器 */
    private GenericJackson2JsonRedisSerializer buildJsonSerializer() {
        ObjectMapper om = new ObjectMapper();
        // 仅按字段序列化，避免依赖 getter 命名
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 支持 LocalDateTime 等 Java8 时间类型
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 写入类型信息，保证反序列化能还原为原始实体（如 BizCategory 而非 LinkedHashMap）
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(om);
    }
}
