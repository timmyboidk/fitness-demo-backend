package com.example.fitness.ai.service.impl;

import com.example.fitness.api.dto.ScoringRequest;
import com.example.fitness.api.dto.ScoringResponse;
import com.example.fitness.ai.service.ScoringService;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Random;

/**
 * 用户运动评分服务实现类
 * 模拟 AI 评分算法生成分数，并将评分事件异步发送至消息队列。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserScoringServiceImpl implements ScoringService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "frontend_event_stream";

    /**
     * 计算评分逻辑：目前为 Mock 实现，并同步发送至 Kafka
     */
    @Override
    @SuppressWarnings("null")
    public ScoringResponse calculateScore(ScoringRequest request) {
        if (request.getMoveId() == null || request.getMoveId().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 模拟评分逻辑：随机生成 60-100 之间的分数
        int score = new Random().nextInt(40) + 60;

        ScoringResponse response = ScoringResponse.builder()
                .success(true)
                .score(score)
                .feedback(new ArrayList<>())
                .build();

        // 异步发送到 Kafka 供数据收集或后续处理（如 Doris 摄取）
        try {
            String moveId = request.getMoveId() != null ? request.getMoveId() : "unknown";
            kafkaTemplate.send(TOPIC, moveId, request);
            log.info("已将评分事件发送至 Kafka, 动作 ID: {}", request.getMoveId());
        } catch (Exception e) {
            log.error("发送 Kafka 失败: {}", e.getMessage());
            // 如果 Kafka 发送失败是关键路径，则抛出异常
            throw new BusinessException(ErrorCode.KAFKA_SEND_ERROR);
        }

        return response;
    }
}
