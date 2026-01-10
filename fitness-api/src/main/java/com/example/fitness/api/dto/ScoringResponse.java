package com.example.fitness.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 评分响应 DTO
 * 包含动作评分、是否成功以及反馈信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringResponse {
    /** 是否评分成功 */
    private boolean success;

    /** 评分分数 (0-100) */
    private Integer score;

    /** 反馈建议列表 */
    private List<Object> feedback;
}
