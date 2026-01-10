package com.example.fitness.ai.service.impl;

import com.example.fitness.api.dto.ScoringRequest;
import com.example.fitness.api.dto.ScoringResponse;
import com.example.fitness.ai.service.ScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserScoringServiceImpl implements ScoringService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "frontend_event_stream";

    @Override
    public ScoringResponse calculateScore(ScoringRequest request) {
        // Mock scoring logic
        int score = new Random().nextInt(40) + 60; // 60-100

        ScoringResponse response = ScoringResponse.builder()
                .success(true)
                .score(score)
                .feedback(new ArrayList<>())
                .build();

        // Asynchronously send to Kafka for data collection/Doris
        try {
            kafkaTemplate.send(TOPIC, request.getMoveId(), request);
            log.info("Sent scoring event to Kafka for move: {}", request.getMoveId());
        } catch (Exception e) {
            log.error("Failed to send to Kafka: {}", e.getMessage());
        }

        return response;
    }
}
