package com.example.fitness.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FitnessProperties 配置类单元测试
 */
@DisplayName("FitnessProperties 单元测试")
class FitnessPropertiesTest {

    @Test
    @DisplayName("JWT 配置 - getter/setter")
    void jwt_GettersSetters_WorkCorrectly() {
        FitnessProperties properties = new FitnessProperties();
        FitnessProperties.Jwt jwt = new FitnessProperties.Jwt();

        jwt.setSecret("test-jwt-secret");
        jwt.setExpiration(7200L);
        properties.setJwt(jwt);

        assertEquals("test-jwt-secret", properties.getJwt().getSecret());
        assertEquals(7200L, properties.getJwt().getExpiration());
    }

    @Test
    @DisplayName("Encrypt 配置 - getter/setter")
    void encrypt_GettersSetters_WorkCorrectly() {
        FitnessProperties properties = new FitnessProperties();
        FitnessProperties.Encrypt encrypt = new FitnessProperties.Encrypt();

        encrypt.setKey("test-aes-key-123");
        properties.setEncrypt(encrypt);

        assertEquals("test-aes-key-123", properties.getEncrypt().getKey());
    }

    @Test
    @DisplayName("默认实例 - 嵌套对象使用默认初始化")
    void newInstance_NestedObjectsHaveDefaultValues() {
        FitnessProperties properties = new FitnessProperties();

        // FitnessProperties 初始化时 jwt 和 encrypt 已被初始化为空对象
        assertNotNull(properties.getJwt());
        assertNotNull(properties.getEncrypt());
        // 但内部字段为 null
        assertNull(properties.getJwt().getSecret());
        assertNull(properties.getJwt().getExpiration());
        assertNull(properties.getEncrypt().getKey());
    }

    @Test
    @DisplayName("Jwt 嵌套类 - 可独立创建和设置")
    void jwt_IndependentCreation() {
        FitnessProperties.Jwt jwt = new FitnessProperties.Jwt();

        jwt.setSecret("my-secret");
        jwt.setExpiration(86400L);

        assertEquals("my-secret", jwt.getSecret());
        assertEquals(86400L, jwt.getExpiration());
    }

    @Test
    @DisplayName("Encrypt 嵌套类 - 可独立创建和设置")
    void encrypt_IndependentCreation() {
        FitnessProperties.Encrypt encrypt = new FitnessProperties.Encrypt();

        encrypt.setKey("my-aes-key");

        assertEquals("my-aes-key", encrypt.getKey());
    }

    @Test
    @DisplayName("空字符串值也能正常设置")
    void emptyStringValues_CanBeSet() {
        FitnessProperties.Jwt jwt = new FitnessProperties.Jwt();
        jwt.setSecret("");

        FitnessProperties.Encrypt encrypt = new FitnessProperties.Encrypt();
        encrypt.setKey("");

        assertEquals("", jwt.getSecret());
        assertEquals("", encrypt.getKey());
    }

    @Test
    @DisplayName("Jwt expiration 为 null 时")
    void jwt_NullExpiration() {
        FitnessProperties.Jwt jwt = new FitnessProperties.Jwt();
        jwt.setExpiration(null);

        assertNull(jwt.getExpiration());
    }
}
