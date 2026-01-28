package com.example.fitness.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 首次使用设置 DTO
 */
@Data
public class OnboardingRequest implements Serializable {

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 难度等级: novice, skilled, expert
     */
    private String difficultyLevel;
}
