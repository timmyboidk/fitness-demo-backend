package com.example.fitness;

import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import com.redis.testcontainers.RedisContainer;

/**
 * 集成测试基类
 * 启动 MySQL, Redis, Kafka 容器（使用优化的小型镜像）
 * 注意：需要 Docker 运行环境，Docker 不可用时测试将被跳过
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EnabledIf("isDockerAvailable")
public abstract class AbstractIntegrationTest {

    /**
     * 检查 Docker 是否可用
     */
    static boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    // 使用单例容器，确保所有测试类共享同一组容器
    private static MySQLContainer<?> mysql;
    private static RedisContainer redis;
    private static KafkaContainer kafka;

    static {
        // 使用较小的镜像版本
        mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("fitness_db")
                .withUsername("root")
                .withPassword("root")
                .withInitScript("sql/schema.sql")
                .withReuse(true);

        redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"))
                .withReuse(true);

        kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"))
                .withReuse(true);

        // 启动容器
        mysql.start();
        redis.start();
        kafka.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getFirstMappedPort());

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
    }
}
