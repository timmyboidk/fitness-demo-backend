package com.example.fitness.data.consumer;

import com.example.fitness.data.mapper.UserStatsMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DataCollectionConsumer Kafka 消费者单元测试
 * 
 * <p>
 * 覆盖以下场景:
 * </p>
 * <ul>
 * <li>非评分事件安全忽略</li>
 * <li>边界值处理 (unknown userId, null score)</li>
 * <li>异常处理</li>
 * </ul>
 * 
 * @since JDK 21
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DataCollectionConsumer 单元测试")
class DataCollectionConsumerTest {

    @Mock
    private UserStatsMapper userStatsMapper;

    private ObjectMapper objectMapper;
    private DataCollectionConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        consumer = new DataCollectionConsumer(userStatsMapper, objectMapper);
    }

    @Nested
    @DisplayName("边界值和异常处理测试")
    class EdgeCasesAndExceptionHandling {

        @Test
        @DisplayName("consume - userId 为 'unknown' 不调用 Mapper")
        void consume_UnknownUserId_SkipsUpdate() {
            // 包含 "score" 关键词但 userId 为 unknown
            String json = "{\"userId\":\"unknown\",\"score\":50,\"moveId\":\"m_squat\"}";
            consumer.consume(json);

            verify(userStatsMapper, never()).incrementScore(anyLong(), anyInt());
        }

        @Test
        @DisplayName("consume - 非评分事件消息安全忽略 (不含 score 字段)")
        void consume_NonScoringEvent_IgnoresSafely() {
            String nonScoringJson = "{\"eventType\":\"login\",\"userId\":\"123\"}";

            consumer.consume(nonScoringJson);

            verify(userStatsMapper, never()).incrementScore(anyLong(), anyInt());
        }

        @Test
        @DisplayName("consume - 无效 JSON 不抛出异常")
        void consume_InvalidJson_HandlesGracefully() {
            String invalidJson = "{ this is not valid json }";

            // 不应抛出异常
            consumer.consume(invalidJson);

            verify(userStatsMapper, never()).incrementScore(anyLong(), anyInt());
        }

        @Test
        @DisplayName("consume - 空字符串消息安全处理")
        void consume_EmptyString_HandlesGracefully() {
            consumer.consume("");

            verify(userStatsMapper, never()).incrementScore(anyLong(), anyInt());
        }

        @Test
        @DisplayName("consume - 包含 score 关键词但格式不符的 JSON")
        void consume_MalformedScoreJson_HandlesGracefully() {
            // 包含 "score" 但不是有效的 ScoringResultEvent 格式
            String json = "{\"some_score\":\"text\",\"data\":123}";

            consumer.consume(json);

            verify(userStatsMapper, never()).incrementScore(anyLong(), anyInt());
        }

        @Test
        @DisplayName("consume - 字符串类型消息处理")
        void consume_StringMessage_ProcessedAsString() {
            // 消息是一个字符串，不包含 score
            consumer.consume("simple text message");

            verify(userStatsMapper, never()).incrementScore(anyLong(), anyInt());
        }

        @Test
        @DisplayName("consume - null userId 不调用 Mapper")
        void consume_NullUserId_SkipsUpdate() {
            String json = "{\"userId\":null,\"score\":50,\"moveId\":\"m_squat\"}";
            consumer.consume(json);

            verify(userStatsMapper, never()).incrementScore(anyLong(), anyInt());
        }
    }

    @Nested
    @DisplayName("对象类型消息测试")
    class ObjectMessageTests {

        @Test
        @DisplayName("consume - 非字符串对象消息被序列化处理")
        void consume_NonStringObject_SerializesToJson() {
            // 创建一个简单的 Map 来模拟对象类型消息
            java.util.Map<String, Object> message = new java.util.HashMap<>();
            message.put("eventType", "click");
            message.put("element", "button");

            // 不包含 score 关键词，不会触发处理
            consumer.consume(message);

            verify(userStatsMapper, never()).incrementScore(anyLong(), anyInt());
        }

        @Test
        @DisplayName("consume - null 消息安全处理")
        void consume_NullMessage_HandlesGracefully() {
            // ObjectMapper.writeValueAsString(null) 返回 "null" 字符串
            consumer.consume(null);

            verify(userStatsMapper, never()).incrementScore(anyLong(), anyInt());
        }
    }
}
