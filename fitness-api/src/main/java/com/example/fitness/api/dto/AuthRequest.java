package com.example.fitness.api.dto;

import lombok.Data;
import java.util.Map;

/**
 * 通用认证请求 DTO
 * 适用于不同类型的登录或认证请求。
 */
@Data
public class AuthRequest {
    private String type; // login_phone | login_wechat
    private Map<String, Object> payload;
}
