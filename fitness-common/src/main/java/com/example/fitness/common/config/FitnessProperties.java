package com.example.fitness.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 健身应用通用配置类
 * 映射配置文件中 'app' 前缀的属性，提供类型安全并消除 IDE 的 'Unknown property' 警告。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class FitnessProperties {

    /** JWT 认证配置 */
    private Jwt jwt = new Jwt();

    /** 数据加密配置 */
    private Encrypt encrypt = new Encrypt();

    @Data
    public static class Jwt {
        /** JWT 密钥 (最小 256 位) */
        private String secret;
        /** 过期时间 (秒) */
        private Long expiration;
    }

    @Data
    public static class Encrypt {
        /** AES 加密密钥 */
        private String key;
    }
}
