package com.example.fitness.common.util;

import com.example.fitness.common.config.FitnessProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT 工具类单元测试
 * 测试 Token 生成、验证和解析功能
 */
@DisplayName("JwtUtil 单元测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private FitnessProperties fitnessProperties;

    @BeforeEach
    void setUp() {
        fitnessProperties = new FitnessProperties();
        FitnessProperties.Jwt jwtConfig = new FitnessProperties.Jwt();
        jwtConfig.setSecret("test-secret-key-which-is-at-least-32-characters-long");
        jwtConfig.setExpiration(3600L); // 1小时
        fitnessProperties.setJwt(jwtConfig);

        jwtUtil = new JwtUtil(fitnessProperties);
    }

    @Nested
    @DisplayName("Token 生成测试")
    class GenerateTokenTests {

        @Test
        @DisplayName("正常用户 ID - 应成功生成 Token")
        void generateToken_ValidUserId_Success() {
            String userId = "12345";
            String token = jwtUtil.generateToken(userId);

            assertNotNull(token);
            assertTrue(token.length() > 0);
            assertTrue(token.contains(".")); // JWT 格式包含点号分隔
        }

        @Test
        @DisplayName("长用户 ID - 应成功生成 Token")
        void generateToken_LongUserId_Success() {
            String userId = "user_" + "a".repeat(100);
            String token = jwtUtil.generateToken(userId);

            assertNotNull(token);
        }

        @Test
        @DisplayName("特殊字符用户 ID - 应成功生成 Token")
        void generateToken_SpecialCharacters_Success() {
            String userId = "user@test.com";
            String token = jwtUtil.generateToken(userId);

            assertNotNull(token);
        }
    }

    @Nested
    @DisplayName("Token 验证测试")
    class ValidateTokenTests {

        @Test
        @DisplayName("有效 Token - 应成功解析 Claims")
        void validateToken_ValidToken_ReturnsClaims() {
            String userId = "user_123";
            String token = jwtUtil.generateToken(userId);

            Claims claims = jwtUtil.validateToken(token);

            assertNotNull(claims);
            assertEquals(userId, claims.getSubject());
            assertNotNull(claims.getIssuedAt());
            assertNotNull(claims.getExpiration());
        }

        @Test
        @DisplayName("无效 Token 格式 - 应抛出异常")
        void validateToken_InvalidFormat_ThrowsException() {
            String invalidToken = "not-a-valid-jwt";

            assertThrows(MalformedJwtException.class, () -> jwtUtil.validateToken(invalidToken));
        }

        @Test
        @DisplayName("错误签名 Token - 应抛出异常")
        void validateToken_WrongSignature_ThrowsException() {
            String tamperedToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NSIsImlhdCI6MTYwMDAwMDAwMH0.invalid_signature";

            assertThrows(SignatureException.class, () -> jwtUtil.validateToken(tamperedToken));
        }
    }

    @Nested
    @DisplayName("从 Token 获取用户 ID 测试")
    class GetUserIdFromTokenTests {

        @Test
        @DisplayName("有效 Token - 应返回正确用户 ID")
        void getUserIdFromToken_ValidToken_ReturnsUserId() {
            String userId = "user_abc123";
            String token = jwtUtil.generateToken(userId);

            String extractedUserId = jwtUtil.getUserIdFromToken(token);

            assertEquals(userId, extractedUserId);
        }

        @Test
        @DisplayName("数字用户 ID - 应正确解析")
        void getUserIdFromToken_NumericUserId_ReturnsCorrectly() {
            String userId = "999";
            String token = jwtUtil.generateToken(userId);

            assertEquals(userId, jwtUtil.getUserIdFromToken(token));
        }
    }

    @Nested
    @DisplayName("密钥回退测试")
    class SecretFallbackTests {

        @Test
        @DisplayName("空密钥 - 应使用默认密钥")
        void generateToken_NullSecret_UsesFallback() {
            FitnessProperties propsWithNullSecret = new FitnessProperties();
            FitnessProperties.Jwt jwtConfig = new FitnessProperties.Jwt();
            jwtConfig.setSecret(null);
            jwtConfig.setExpiration(3600L);
            propsWithNullSecret.setJwt(jwtConfig);

            JwtUtil utilWithNull = new JwtUtil(propsWithNullSecret);
            String token = utilWithNull.generateToken("test_user");

            assertNotNull(token);
        }

        @Test
        @DisplayName("短密钥 - 应使用默认密钥")
        void generateToken_ShortSecret_UsesFallback() {
            FitnessProperties propsWithShortSecret = new FitnessProperties();
            FitnessProperties.Jwt jwtConfig = new FitnessProperties.Jwt();
            jwtConfig.setSecret("short");
            jwtConfig.setExpiration(3600L);
            propsWithShortSecret.setJwt(jwtConfig);

            JwtUtil utilWithShort = new JwtUtil(propsWithShortSecret);
            String token = utilWithShort.generateToken("test_user");

            assertNotNull(token);
        }
    }

    @Nested
    @DisplayName("过期时间回退测试")
    class ExpirationFallbackTests {

        @Test
        @DisplayName("空过期时间 - 应使用默认过期时间")
        void generateToken_NullExpiration_UsesFallback() {
            FitnessProperties propsWithNullExp = new FitnessProperties();
            FitnessProperties.Jwt jwtConfig = new FitnessProperties.Jwt();
            jwtConfig.setSecret("test-secret-key-which-is-at-least-32-characters-long");
            jwtConfig.setExpiration(null);
            propsWithNullExp.setJwt(jwtConfig);

            JwtUtil utilWithNullExp = new JwtUtil(propsWithNullExp);
            String token = utilWithNullExp.generateToken("test_user");

            assertNotNull(token);
            Claims claims = utilWithNullExp.validateToken(token);
            // 默认过期时间是 86400 秒
            assertTrue(claims.getExpiration().getTime() > System.currentTimeMillis());
        }
    }
}
