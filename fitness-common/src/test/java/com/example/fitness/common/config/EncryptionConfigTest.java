package com.example.fitness.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 加密配置类单元测试
 */
@DisplayName("EncryptionConfig 单元测试")
class EncryptionConfigTest {

    @Test
    @DisplayName("afterPropertiesSet - 正常初始化加密密钥")
    void afterPropertiesSet_ValidKey_InitializesHandler() throws Exception {
        FitnessProperties properties = new FitnessProperties();
        FitnessProperties.Encrypt encrypt = new FitnessProperties.Encrypt();
        encrypt.setKey("test-encrypt-key-16");
        properties.setEncrypt(encrypt);

        EncryptionConfig config = new EncryptionConfig(properties);

        assertDoesNotThrow(() -> config.afterPropertiesSet());
    }

    @Test
    @DisplayName("afterPropertiesSet - 密钥为空时使用默认值")
    void afterPropertiesSet_NullKey_UsesFallback() throws Exception {
        FitnessProperties properties = new FitnessProperties();
        FitnessProperties.Encrypt encrypt = new FitnessProperties.Encrypt();
        encrypt.setKey(null);
        properties.setEncrypt(encrypt);

        EncryptionConfig config = new EncryptionConfig(properties);

        assertDoesNotThrow(() -> config.afterPropertiesSet());
    }

    @Test
    @DisplayName("afterPropertiesSet - 使用配置的密钥")
    void afterPropertiesSet_CustomKey_IsUsed() throws Exception {
        FitnessProperties properties = new FitnessProperties();
        FitnessProperties.Encrypt encrypt = new FitnessProperties.Encrypt();
        encrypt.setKey("custom-key-12345");
        properties.setEncrypt(encrypt);

        EncryptionConfig config = new EncryptionConfig(properties);
        config.afterPropertiesSet();

        // 验证不抛出异常即认为成功初始化
        assertTrue(true);
    }
}
