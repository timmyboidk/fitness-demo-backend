package com.example.fitness.data.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataCollectionConsumer {

    @KafkaListener(topics = "frontend_event_stream", groupId = "fitness-data-group")
    public void consume(Object message) {
        log.info("Consumed event from Kafka: {}", message);
        log.info("Simulating Doris ingestion for event... [Doris: dwd_action_score]");
    }
}
