package com.example.fitness.common.config;

import com.example.fitness.common.handler.EncryptTypeHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 加密配置类
 * 初始化数据库加密处理器的密钥
 */
@Component
public class EncryptionConfig implements InitializingBean {

    @Value("${app.encrypt.key:fitness-demo-key}")
    private String encryptKey;

    @Override
    public void afterPropertiesSet() throws Exception {
        EncryptTypeHandler.setKey(encryptKey);
    }
}
