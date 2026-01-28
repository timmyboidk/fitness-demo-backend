package com.example.fitness.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SwaggerConfig 配置类单元测试
 * 直接测试配置方法的返回值，验证 OpenAPI 配置的正确性
 * 
 * <p>
 * 遵循 JDK 21 最佳实践，使用 AssertJ 流式断言提高可读性
 * </p>
 */
@DisplayName("SwaggerConfig 配置类单元测试")
class SwaggerConfigTest {

    private final SwaggerConfig swaggerConfig = new SwaggerConfig();

    @Test
    @DisplayName("fitnessOpenAPI - Bean 方法返回非空 OpenAPI 实例")
    void fitnessOpenAPI_ReturnsNonNullInstance() {
        OpenAPI openAPI = swaggerConfig.fitnessOpenAPI();

        assertThat(openAPI).isNotNull();
    }

    @Test
    @DisplayName("fitnessOpenAPI - 验证 API 标题配置正确")
    void fitnessOpenAPI_HasCorrectTitle() {
        OpenAPI openAPI = swaggerConfig.fitnessOpenAPI();

        Info info = openAPI.getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getTitle()).isEqualTo("Fitness 健身平台后端 API");
    }

    @Test
    @DisplayName("fitnessOpenAPI - 验证 API 版本配置正确")
    void fitnessOpenAPI_HasCorrectVersion() {
        OpenAPI openAPI = swaggerConfig.fitnessOpenAPI();

        assertThat(openAPI.getInfo().getVersion()).isEqualTo("v1.0.0");
    }

    @Test
    @DisplayName("fitnessOpenAPI - 验证 API 描述配置正确")
    void fitnessOpenAPI_HasCorrectDescription() {
        OpenAPI openAPI = swaggerConfig.fitnessOpenAPI();

        assertThat(openAPI.getInfo().getDescription())
                .isEqualTo("Fitness 应用程序后端 API 接口文档");
    }

    @Test
    @DisplayName("fitnessOpenAPI - 验证许可证配置正确")
    void fitnessOpenAPI_HasCorrectLicense() {
        OpenAPI openAPI = swaggerConfig.fitnessOpenAPI();

        assertThat(openAPI.getInfo().getLicense()).isNotNull();
        assertThat(openAPI.getInfo().getLicense().getName()).isEqualTo("Apache 2.0");
        assertThat(openAPI.getInfo().getLicense().getUrl()).isEqualTo("http://springdoc.org");
    }

    @Test
    @DisplayName("fitnessOpenAPI - 验证安全要求配置正确")
    void fitnessOpenAPI_HasSecurityRequirement() {
        OpenAPI openAPI = swaggerConfig.fitnessOpenAPI();

        assertThat(openAPI.getSecurity()).isNotNull();
        assertThat(openAPI.getSecurity()).hasSize(1);
        // 使用 JDK 21 Sequenced Collections API
        assertThat(openAPI.getSecurity().getFirst().containsKey("BearerAuth")).isTrue();
    }

    @Test
    @DisplayName("fitnessOpenAPI - 验证 BearerAuth 安全方案配置正确")
    void fitnessOpenAPI_HasCorrectSecurityScheme() {
        OpenAPI openAPI = swaggerConfig.fitnessOpenAPI();

        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("BearerAuth");

        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("BearerAuth");
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
    }

    @Test
    @DisplayName("fitnessOpenAPI - 验证所有核心属性一次性校验")
    void fitnessOpenAPI_AllPropertiesCorrect() {
        OpenAPI openAPI = swaggerConfig.fitnessOpenAPI();

        assertThat(openAPI)
                .satisfies(api -> {
                    assertThat(api.getInfo().getTitle()).contains("Fitness");
                    assertThat(api.getInfo().getVersion()).startsWith("v");
                    assertThat(api.getSecurity()).isNotEmpty();
                    assertThat(api.getComponents().getSecuritySchemes()).isNotEmpty();
                });
    }

    @Test
    @DisplayName("fitnessOpenAPI - 安全方案名称验证")
    void fitnessOpenAPI_SecuritySchemeName() {
        OpenAPI openAPI = swaggerConfig.fitnessOpenAPI();

        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("BearerAuth");
        assertThat(scheme.getName()).isEqualTo("BearerAuth");
    }
}
