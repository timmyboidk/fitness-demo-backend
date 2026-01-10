package com.example.fitness.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成和解析 JWT Token。
 */
@Component
public class JwtUtil {

    @Value("${app.jwt.secret:fitness-demo-secret-key-fitness-demo-secret-key}")
    private String secret;

    @Value("${app.jwt.expiration:86400}")
    private long expiration; // 默认 24 小时

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成 Token
     * 
     * @param userId 用户 ID
     * @return JWT Token 字符串
     */
    public String generateToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证 Token 并获取 Claims
     * 
     * @param token JWT Token
     * @return Claims 对象
     */
    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 中获取用户 ID
     * 
     * @param token JWT Token
     * @return 用户 ID
     */
    public String getUserIdFromToken(String token) {
        return validateToken(token).getSubject();
    }
}
