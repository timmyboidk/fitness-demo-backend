package com.example.fitness.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 评分结果事件 DTO
 * 用于 Kafka 消息传递
 */
@Data
@Builder
public class ScoringResultEvent {
    private String userId;
    private String moveId;
    private Integer score;
    private Map<String, Object> extraData;
    private LocalDateTime timestamp;
}
