package com.example.fitness.common.util;

import com.example.fitness.common.config.FitnessProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class JwtUtil {

    private final FitnessProperties fitnessProperties;

    private Key getSigningKey() {
        String secret = fitnessProperties.getJwt().getSecret();
        if (secret == null || secret.length() < 32) {
            secret = "fitness-demo-secret-key-fitness-demo-secret-key"; // Fallback for dev
        }
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    private long getExpiration() {
        Long exp = fitnessProperties.getJwt().getExpiration();
        return exp != null ? exp : 86400;
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
                .setExpiration(new Date(System.currentTimeMillis() + getExpiration() * 1000))
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
