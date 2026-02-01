package com.example.fitness;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 控制器单元测试专用应用入口
 * 
 * <p>
 * 用于 fitness-data 模块中控制器层的单元测试，
 * 排除了数据源自动配置，避免测试时需要连接真实数据库。
 * 
 * <p>
 * 适用场景：
 * <ul>
 * <li>使用 @WebMvcTest 进行控制器层测试</li>
 * <li>Mock Service 层依赖</li>
 * <li>无需数据库连接的快速测试</li>
 * </ul>
 * 
 * @author fitness-team
 * @since 1.0.0
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class ControllerTestApp {
}
