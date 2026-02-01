package com.example.fitness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.example.fitness.common.aspect.IdempotentAspect;
import com.example.fitness.common.aspect.RateLimitAspect;

/**
 * fitness-content 模块测试应用入口
 * 
 * <p>
 * 专门为 fitness-content 模块的集成测试配置的 Spring Boot 应用。
 * 排除了依赖 Redis 的切面组件，因为该模块的测试使用 H2 内存数据库，
 * 不需要启动 Redis 服务。
 * 
 * <p>
 * 排除的组件：
 * <ul>
 * <li>{@link IdempotentAspect} - 幂等性切面，需要 Redis</li>
 * <li>{@link RateLimitAspect} - 限流切面，需要 Redis</li>
 * </ul>
 * 
 * @author fitness-team
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.example.fitness", excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        IdempotentAspect.class, RateLimitAspect.class }))
public class TestApplication {

    /**
     * 测试应用主入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
