package com.example.fitness.data.controller;

import com.example.fitness.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataCollectionController {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "frontend_event_stream";

    @PostMapping("/collect")
    public Result<Void> collect(@RequestBody Map<String, Object> request) {
        log.info("Received collection request: {}", request);
        try {
            kafkaTemplate.send(TOPIC, request);
            log.info("Successfully sent data collection event to Kafka");
        } catch (Exception e) {
            log.error("Failed to send collection event to Kafka: {}", e.getMessage());
        }
        return Result.success(null);
    }
}
