package com.example.fitness.data.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka 消费者 - 监听前端事件流并将数据模拟同步到数据仓库（如 Doris）
 */
@Slf4j
@Component
public class DataCollectionConsumer {

    @KafkaListener(topics = "frontend_event_stream", groupId = "fitness-data-group")
    public void consume(Object message) {
        log.info("从 Kafka 消费到事件: {}", message);
        log.info("模拟将数据同步至 Doris... [Doris 表名: dwd_action_score]");
    }
}
