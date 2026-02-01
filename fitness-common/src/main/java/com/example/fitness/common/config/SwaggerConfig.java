package com.example.fitness.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Swagger/OpenAPI 文档配置类
 * 
 * <p>
 * 配置 API 文档的基本信息、安全认证方案等。
 * 仅在 dev 和 test 环境下启用，生产环境不暴露 API 文档。
 * 
 * <p>
 * 访问地址：
 * <ul>
 * <li>Swagger UI: /swagger-ui.html</li>
 * <li>OpenAPI JSON: /v3/api-docs</li>
 * </ul>
 * 
 * @author fitness-team
 * @since 1.0.0
 */
@Configuration
@Profile({ "dev", "test" }) // 仅在 dev, test 环境开启文档
public class SwaggerConfig {

        /**
         * 配置 OpenAPI 文档规范
         * 
         * <p>
         * 定义 API 文档的元信息和 JWT Bearer Token 认证方案。
         * 所有需要认证的接口会自动添加 "Authorization: Bearer {token}" 请求头。
         * 
         * @return OpenAPI 配置对象
         */
        @Bean
        public OpenAPI fitnessOpenAPI() {
                return new OpenAPI()
                                // 配置 API 基本信息
                                .info(new Info()
                                                .title("Fitness 健身平台后端 API")
                                                .description("Fitness 应用程序后端 API 接口文档")
                                                .version("v1.0.0")
                                                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                                // 全局安全要求，所有接口默认需要 BearerAuth
                                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                                // 配置 JWT Bearer Token 认证方案
                                .components(new Components()
                                                .addSecuritySchemes("BearerAuth",
                                                                new SecurityScheme()
                                                                                .name("BearerAuth")
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")));
        }
}
