package com.example.fitness.common.config;

import com.example.fitness.common.handler.EncryptTypeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * 加密配置类
 * 初始化数据库加密处理器的密钥
 */
@Component
@RequiredArgsConstructor
public class EncryptionConfig implements InitializingBean {

    private final FitnessProperties fitnessProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        String key = fitnessProperties.getEncrypt().getKey();
        if (key == null) {
            key = "fitness-demo-key"; // Fallback
        }
        EncryptTypeHandler.setKey(key);
    }
}
