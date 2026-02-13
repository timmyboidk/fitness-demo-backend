package com.example.fitness.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 评分结果事件 DTO
 *
 * <p>
 * 用于 Kafka Topic {@code frontend_event_stream} 的异步消息传递。
 * 由 AI 评分服务（生产者）发送，数据采集消费者接收后更新用户统计。
 *
 * @see com.example.fitness.api.dto.ScoringRequest
 */
@Data
@Builder
public class ScoringResultEvent {

    /** 执行动作的用户 ID（可能为 {@code "unknown"} 若请求中未携带） */
    private String userId;

    /** 被评分的健身动作 ID */
    private String moveId;

    /** 评分结果（0 ~ 100） */
    private Integer score;

    /** 附加数据（如 {@code {"duration": 5}}） */
    private Map<String, Object> extraData;

    /** 评分时间戳 */
    private LocalDateTime timestamp;
}
