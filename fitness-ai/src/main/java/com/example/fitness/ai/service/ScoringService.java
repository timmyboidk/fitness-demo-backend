package com.example.fitness.ai.service;

import com.example.fitness.api.dto.ScoringRequest;
import com.example.fitness.api.dto.ScoringResponse;

/**
 * AI 评分服务接口
 * 处理动作关键点数据，计算准确度得分。
 */
public interface ScoringService {
    /**
     * 计算动作评分并发送异步事件到 Kafka
     */
    ScoringResponse calculateScore(ScoringRequest request);
}
