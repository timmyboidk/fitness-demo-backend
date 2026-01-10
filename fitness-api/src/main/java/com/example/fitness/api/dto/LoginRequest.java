package com.example.fitness.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录请求 DTO
 * 用于封装用户通过手机号或微信登录时的请求参数。
 */
@Data
public class LoginRequest implements Serializable {

    /**
     * 手机号 (用于手机号登录)
     */
    private String phone;

    /**
     * 验证码/微信Code (用于两种登录模式)
     */
    private String code;

    /**
     * 登录类型: phone / wechat
     */
    @NotBlank(message = "登录类型不能为空")
    private String type;

    /**
     * 难度等级 (Onboarding)
     */
    private String difficultyLevel;

    /**
     * 用户ID (Onboarding)
     */
    private String userId;
}
