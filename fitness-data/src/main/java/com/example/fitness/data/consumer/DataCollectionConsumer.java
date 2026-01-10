package com.example.fitness.data.consumer;

import lombok.extern.slf4j.Slf4j;
import com.example.fitness.api.dto.ScoringResultEvent;
import com.example.fitness.data.mapper.UserStatsMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka 消费者 - 监听前端事件流并将数据模拟同步到数据仓库（如 Doris）及 MySQL
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataCollectionConsumer {

    private final UserStatsMapper userStatsMapper;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "frontend_event_stream", groupId = "fitness-data-group")
    public void consume(Object message) {
        log.info("从 Kafka 消费到事件: {}", message);

        try {
            // 1. 解析消息
            String json;
            if (message instanceof String) {
                json = (String) message;
            } else {
                json = objectMapper.writeValueAsString(message);
            }
            // 尝试解析为 ScoringResultEvent, 如果解析失败可能是其他事件类型，需兼容处理
            // 这里假定主要是评分事件
            if (json.contains("score")) {
                ScoringResultEvent event = objectMapper.readValue(json, ScoringResultEvent.class);
                handleScoringEvent(event);
            }

        } catch (Exception e) {
            log.error("处理 Kafka 消息失败: {}", e.getMessage());
        }
    }

    private void handleScoringEvent(ScoringResultEvent event) {
        // 2. MySQL 持久化 (核心指标)
        try {
            if (event.getUserId() != null && !"unknown".equals(event.getUserId())) {
                Long userId = Long.valueOf(event.getUserId());
                if (event.getScore() != null) {
                    userStatsMapper.incrementScore(userId, event.getScore());
                    log.info("用户 {} 积分已更新: +{}", userId, event.getScore());
                }
            }
        } catch (NumberFormatException e) {
            log.warn("UserId 非法: {}", event.getUserId());
        }

        // 3. 异步写入大数据 (Doris) - 模拟
        // 在生产环境中，这里可能使用 JDBC Template 批量写入 Doris 或者写入另一个 Topic
        log.info("模拟将数据同步至 Doris... [Doris 表名: dwd_action_score, move_id: {}, score: {}]",
                event.getMoveId(), event.getScore());
    }
}
