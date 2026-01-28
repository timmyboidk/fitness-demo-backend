package com.example.fitness.data;

import com.example.fitness.api.dto.ScoringResultEvent;
import com.example.fitness.data.mapper.UserStatsMapper;
import com.example.fitness.data.model.entity.UserStats;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import com.redis.testcontainers.RedisContainer;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * 数据链路集成测试
 * 验证：Kafka 消息生产 -> Consumer 消费 -> 数据库更新
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName("数据处理链路集成测试")
class DataPipelineIT {

    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.3.0")
            .withDatabaseName("fitness_data_db")
            .withUsername("root")
            .withPassword("root")
            .withInitScript("sql/schema.sql");

    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    static final RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7.2-alpine"));

    @BeforeAll
    static void startContainers() {
        mysql.start();
        kafka.start();
        redis.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getFirstMappedPort());
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private UserStatsMapper userStatsMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("完整数据链路：发送评分事件 -> 更新用户积分")
    void shouldProcessScoringEventAndUpdateDb() throws Exception {
        Long userId = 10086L;
        Integer scoreToAdd = 95;
        Integer initialScore = 10;

        // 0. 预置数据：创建用户记录，初始积分为 10
        UserStats initialUser = new UserStats();
        initialUser.setId(userId);
        initialUser.setTotalScore(initialScore);
        userStatsMapper.insert(initialUser);

        // 1. 构造评分事件
        ScoringResultEvent event = ScoringResultEvent.builder()
                .userId(String.valueOf(userId))
                .moveId("m_squat")
                .score(scoreToAdd)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        // 2. 发送 Kafka 消息 (序列化为 JSON 字符串)
        String jsonMessage = objectMapper.writeValueAsString(event);
        kafkaTemplate.send("frontend_event_stream", jsonMessage);

        // 3. 等待并验证数据库更新 (最多等待 10 秒)
        // 预期分数 = 初始分数 + 新增分数
        int expectedScore = initialScore + scoreToAdd;

        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(1000))
                .untilAsserted(() -> {
                    // 查询最新状态
                    UserStats stats = userStatsMapper.selectById(userId);
                    assertThat(stats).isNotNull();

                    // 验证分数是否增加
                    // 注意：DataCollectionConsumer 中使用 total_score = IFNULL(total_score, 0) + #{score}
                    assertThat(stats.getTotalScore()).as("用户总积分应更新").isEqualTo(expectedScore);
                });
    }
}
