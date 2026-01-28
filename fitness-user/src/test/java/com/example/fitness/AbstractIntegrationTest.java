package com.example.fitness;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import com.redis.testcontainers.RedisContainer;

/**
 * 集成测试基类
 * 启动 MySQL, Redis, Kafka 容器
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.3.0")
            .withDatabaseName("fitness_db")
            .withUsername("root")
            .withPassword("root")
            .withInitScript("sql/schema.sql");

    static final RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7.2-alpine"));

    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @BeforeAll
    static void startContainers() {
        mysql.start();
        redis.start();
        kafka.start();

        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", String.valueOf(redis.getFirstMappedPort()));
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
