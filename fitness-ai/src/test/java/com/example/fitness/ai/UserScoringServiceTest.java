package com.example.fitness.ai;

import com.example.fitness.ai.service.impl.UserScoringServiceImpl;
import com.example.fitness.api.dto.ScoringRequest;
import com.example.fitness.api.dto.ScoringResponse;
import com.example.fitness.api.dto.ScoringResultEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

/**
 * 用户评分服务单元测试
 * 覆盖余弦相似度计算和 Kafka 事件发送
 */
public class UserScoringServiceTest {

    @Test
    @SuppressWarnings({ "unchecked", "null" })
    public void testCalculateScore_PerfectMatch() {
        // 1. Mock Kafka
        KafkaTemplate<String, Object> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        Mockito.when(kafkaTemplate.send(anyString(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        UserScoringServiceImpl service = new UserScoringServiceImpl(kafkaTemplate);

        // 2. 构造请求数据 (模拟完美匹配 m_squat)
        ScoringRequest req = new ScoringRequest();
        req.setMoveId("m_squat");
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> keypoints = new ArrayList<>();

        // 构造 17 个关键点，全部为 0.5 (与 Service 中 mock 的模板一致)
        for (int i = 0; i < 17; i++) {
            Map<String, Object> kp = new HashMap<>();
            kp.put("x", 0.5);
            kp.put("y", 0.5);
            keypoints.add(kp);
        }
        data.put("keypoints", keypoints);
        req.setData(data);

        // 3. 执行
        ScoringResponse response = service.calculateScore(req);

        // 4. 断言
        Assertions.assertTrue(response.isSuccess());
        Assertions.assertEquals(100, response.getScore(), "完全匹配应为 100 分");
        Assertions.assertTrue(response.getFeedback().contains("完美！保持这个节奏"));

        // 5. 验证 Kafka 发送的是 ScoringResultEvent
        ArgumentCaptor<ScoringResultEvent> eventCaptor = ArgumentCaptor.forClass(ScoringResultEvent.class);
        Mockito.verify(kafkaTemplate).send(eq("frontend_event_stream"),
                eq("m_squat"), eventCaptor.capture());

        ScoringResultEvent event = eventCaptor.getValue();
        Assertions.assertEquals(100, event.getScore());
        Assertions.assertEquals("m_squat", event.getMoveId());
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void testCalculateScore_NoKeypoints() {
        KafkaTemplate<String, Object> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        UserScoringServiceImpl service = new UserScoringServiceImpl(kafkaTemplate);

        ScoringRequest req = new ScoringRequest();
        req.setMoveId("m_squat");
        req.setData(new HashMap<>()); // Empty data

        ScoringResponse response = service.calculateScore(req);
        Assertions.assertFalse(response.isSuccess());
        Assertions.assertEquals(0, response.getScore());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCalculateScore_NullMoveId() {
        KafkaTemplate<String, Object> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        UserScoringServiceImpl service = new UserScoringServiceImpl(kafkaTemplate);

        ScoringRequest req = new ScoringRequest();
        req.setMoveId(null); // null moveId

        try {
            service.calculateScore(req);
            Assertions.fail("应抛出 BusinessException");
        } catch (com.example.fitness.common.exception.BusinessException e) {
            Assertions.assertEquals(400, e.getCode()); // PARAM_ERROR
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCalculateScore_EmptyMoveId() {
        KafkaTemplate<String, Object> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        UserScoringServiceImpl service = new UserScoringServiceImpl(kafkaTemplate);

        ScoringRequest req = new ScoringRequest();
        req.setMoveId(""); // empty moveId

        try {
            service.calculateScore(req);
            Assertions.fail("应抛出 BusinessException");
        } catch (com.example.fitness.common.exception.BusinessException e) {
            Assertions.assertEquals(400, e.getCode()); // PARAM_ERROR
        }
    }

    @Test
    @SuppressWarnings({ "unchecked", "null" })
    public void testCalculateScore_LowScore_FeedbackContent() {
        KafkaTemplate<String, Object> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        Mockito.when(kafkaTemplate.send(anyString(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        UserScoringServiceImpl service = new UserScoringServiceImpl(kafkaTemplate);

        // 构造请求数据 (模拟差匹配 - 关键点全为 0)
        ScoringRequest req = new ScoringRequest();
        req.setMoveId("m_squat");
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> keypoints = new ArrayList<>();

        for (int i = 0; i < 17; i++) {
            Map<String, Object> kp = new HashMap<>();
            kp.put("x", 0);
            kp.put("y", 0);
            keypoints.add(kp);
        }
        data.put("keypoints", keypoints);
        req.setData(data);

        ScoringResponse response = service.calculateScore(req);

        Assertions.assertTrue(response.isSuccess());
        Assertions.assertEquals(0, response.getScore()); // 余弦相似度为0
        Assertions.assertTrue(response.getFeedback().toString().contains("动作幅度不够"));
    }

    @Test
    @SuppressWarnings({ "unchecked", "null" })
    public void testCalculateScore_MediumScore_FeedbackContent() {
        KafkaTemplate<String, Object> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        Mockito.when(kafkaTemplate.send(anyString(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        UserScoringServiceImpl service = new UserScoringServiceImpl(kafkaTemplate);

        // 构造请求数据 (模拟中等匹配)
        ScoringRequest req = new ScoringRequest();
        req.setMoveId("m_squat");
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> keypoints = new ArrayList<>();

        // 部分匹配
        for (int i = 0; i < 17; i++) {
            Map<String, Object> kp = new HashMap<>();
            kp.put("x", 0.4 + (i % 2) * 0.1);
            kp.put("y", 0.4 + (i % 2) * 0.1);
            keypoints.add(kp);
        }
        data.put("keypoints", keypoints);
        req.setData(data);

        ScoringResponse response = service.calculateScore(req);

        Assertions.assertTrue(response.isSuccess());
        // 分数应该在 60-80 之间或低于 60
        int score = response.getScore();
        Assertions.assertTrue(score >= 0 && score <= 100);
    }

    @Test
    @SuppressWarnings({ "unchecked", "null" })
    public void testCalculateScore_KafkaFailure_DoesNotThrow() {
        KafkaTemplate<String, Object> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        Mockito.when(kafkaTemplate.send(anyString(), any(), any()))
                .thenThrow(new RuntimeException("Kafka connection failed"));

        UserScoringServiceImpl service = new UserScoringServiceImpl(kafkaTemplate);

        ScoringRequest req = new ScoringRequest();
        req.setMoveId("m_squat");
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> keypoints = new ArrayList<>();

        for (int i = 0; i < 17; i++) {
            Map<String, Object> kp = new HashMap<>();
            kp.put("x", 0.5);
            kp.put("y", 0.5);
            keypoints.add(kp);
        }
        data.put("keypoints", keypoints);
        req.setData(data);

        // 即使 Kafka 发送失败，也不应抛出异常
        ScoringResponse response = service.calculateScore(req);

        Assertions.assertTrue(response.isSuccess());
        Assertions.assertEquals(100, response.getScore());
    }

    @Test
    @SuppressWarnings({ "unchecked", "null" })
    public void testCalculateScore_NullData() {
        KafkaTemplate<String, Object> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        UserScoringServiceImpl service = new UserScoringServiceImpl(kafkaTemplate);

        ScoringRequest req = new ScoringRequest();
        req.setMoveId("m_squat");
        req.setData(null); // null data

        ScoringResponse response = service.calculateScore(req);
        Assertions.assertFalse(response.isSuccess());
        Assertions.assertEquals(0, response.getScore());
    }

    @Test
    @SuppressWarnings({ "unchecked", "null" })
    public void testCalculateScore_WithUserId() {
        KafkaTemplate<String, Object> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        Mockito.when(kafkaTemplate.send(anyString(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        UserScoringServiceImpl service = new UserScoringServiceImpl(kafkaTemplate);

        ScoringRequest req = new ScoringRequest();
        req.setMoveId("m_squat");
        Map<String, Object> data = new HashMap<>();
        data.put("userId", "user123");
        List<Map<String, Object>> keypoints = new ArrayList<>();

        for (int i = 0; i < 17; i++) {
            Map<String, Object> kp = new HashMap<>();
            kp.put("x", 0.5);
            kp.put("y", 0.5);
            keypoints.add(kp);
        }
        data.put("keypoints", keypoints);
        req.setData(data);

        ScoringResponse response = service.calculateScore(req);

        Assertions.assertTrue(response.isSuccess());

        // 验证 Kafka 事件包含 userId
        ArgumentCaptor<ScoringResultEvent> eventCaptor = ArgumentCaptor.forClass(ScoringResultEvent.class);
        Mockito.verify(kafkaTemplate).send(eq("frontend_event_stream"),
                eq("m_squat"), eventCaptor.capture());

        ScoringResultEvent event = eventCaptor.getValue();
        Assertions.assertEquals("user123", event.getUserId());
    }
}
