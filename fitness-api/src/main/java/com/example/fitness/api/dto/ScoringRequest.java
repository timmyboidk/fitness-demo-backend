package com.example.fitness.api.dto;

import lombok.Data;
import java.util.Map;

/**
 * AI 评分请求 DTO
 *
 * <p>
 * 用于 {@code POST /api/ai/score} 接口，客户端将实时采集的
 * 人体关键点数据提交至后端进行动作相似度评分。
 *
 * @see ScoringResponse
 */
@Data
public class ScoringRequest {

    /** 正在执行的健身动作 ID（如 {@code "m_squat"}），不能为空 */
    private String moveId;

    /**
     * 关键点数据负载，需包含以下字段：
     * <ul>
     * <li>{@code keypoints} — {@code List<Map>}，每个元素含 {@code x}, {@code y},
     * {@code score}</li>
     * <li>{@code userId} — 用户 ID（可选，用于异步事件归属）</li>
     * </ul>
     */
    private Map<String, Object> data;
}
