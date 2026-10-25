package com.xinghai.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：生成令牌 + 解析令牌
 * <p>JWT（JSON Web Token）：一串包含用户身份信息并带签名的令牌，
 * 服务端生成后交给前端保存，前端每次请求带上，服务端验签即可确认身份（无需服务端存会话）。</p>
 */
@Component
public class JwtUtil {

    @Value("${xinghai.jwt.secret}")
    private String secret;

    @Value("${xinghai.jwt.expire}")
    private Long expire;

    /** 根据密钥字符串构造 HS256 签名密钥 */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roles    角色串（USER,CREATOR,ADMIN）
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String username, String roles) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expire))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析并校验令牌
     *
     * @param token JWT 字符串
     * @return Claims 载荷；校验失败或过期抛异常
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
