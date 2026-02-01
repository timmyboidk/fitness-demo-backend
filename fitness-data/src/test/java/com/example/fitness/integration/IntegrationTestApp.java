package com.example.fitness.integration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.example.fitness.ControllerTestApp;

/**
 * 集成测试专用应用入口
 * 
 * <p>
 * 用于 fitness-data 模块的完整集成测试，配置了：
 * <ul>
 * <li>MyBatis Mapper 扫描路径</li>
 * <li>排除 ControllerTestApp 避免冲突</li>
 * <li>启用完整的 Spring 上下文</li>
 * </ul>
 * 
 * <p>
 * 适用场景：
 * <ul>
 * <li>需要真实数据库连接的测试（Testcontainers）</li>
 * <li>端到端的 API 集成测试</li>
 * <li>验证数据持久化逻辑</li>
 * </ul>
 * 
 * @author fitness-team
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.example.fitness", excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestApp.class))
@MapperScan("com.example.fitness.data.mapper")
public class IntegrationTestApp {
}
