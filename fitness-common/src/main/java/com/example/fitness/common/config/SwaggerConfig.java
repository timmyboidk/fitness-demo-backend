package com.example.fitness.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({ "dev", "test" }) // 仅在 dev, test 环境开启文档
public class SwaggerConfig {

    @Bean
    public OpenAPI fitnessOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Fitness Backend API")
                        .description("Fitness application backend API documentation")
                        .version("v1.0.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
