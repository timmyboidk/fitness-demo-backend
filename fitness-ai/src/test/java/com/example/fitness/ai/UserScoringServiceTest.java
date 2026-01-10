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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * 用户评分服务单元测试
 * 覆盖余弦相似度计算和 Kafka 事件发送
 */
public class UserScoringServiceTest {

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testCalculateScore_PerfectMatch() {
        // 1. Mock Kafka
        KafkaTemplate kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        Mockito.when(kafkaTemplate.send(any(String.class), any(), any()))
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
        Mockito.verify(kafkaTemplate).send(eq("frontend_event_stream"), eq("m_squat"), eventCaptor.capture());

        ScoringResultEvent event = eventCaptor.getValue();
        Assertions.assertEquals(100, event.getScore());
        Assertions.assertEquals("m_squat", event.getMoveId());
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testCalculateScore_NoKeypoints() {
        KafkaTemplate kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        UserScoringServiceImpl service = new UserScoringServiceImpl(kafkaTemplate);

        ScoringRequest req = new ScoringRequest();
        req.setMoveId("m_squat");
        req.setData(new HashMap<>()); // Empty data

        ScoringResponse response = service.calculateScore(req);
        Assertions.assertFalse(response.isSuccess());
        Assertions.assertEquals(0, response.getScore());
    }
}
