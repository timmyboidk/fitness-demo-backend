package com.example.fitness.ai.service;

import com.example.fitness.api.dto.ScoringRequest;
import com.example.fitness.api.dto.ScoringResponse;

public interface ScoringService {
    /**
     * 计算动作评分并发送异步事件到 Kafka
     */
    ScoringResponse calculateScore(ScoringRequest request);
}
