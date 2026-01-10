package com.example.fitness.api.dto;

import lombok.Data;
import java.util.Map;

/**
 * 评分请求负载 - 客户端上传的动作数据
 */
@Data
public class ScoringRequest {
    private String moveId; // 动作 ID
    private Map<String, Object> data; // 关键点数据，例如 { "keypoints": [...] }
}
