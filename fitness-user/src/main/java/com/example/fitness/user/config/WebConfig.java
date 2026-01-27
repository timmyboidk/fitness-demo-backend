package com.example.fitness.user.config;

import com.example.fitness.user.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 * 注册拦截器与跨域配置
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册登录拦截器，拦截所有请求，排除特定路径
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有路径
                .excludePathPatterns(
                        "/api/auth/**", // 登录/注册接口，不需要认证
                        "/api/docs/**", // 自定义文档路径（如有）
                        "/swagger-ui.html", // Swagger UI 入口
                        "/swagger-ui/**", // Swagger UI 静态资源
                        "/v3/api-docs/**", // OpenAPI JSON 定义
                        "/webjars/**", // WebJars 资源
                        "/actuator/**", // Spring Boot Actuator 监控
                        "/error", // 错误页面
                        "/favicon.ico" // 图标
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 全局跨域配置
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 允许所有来源（生产环境建议指定具体域名）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
