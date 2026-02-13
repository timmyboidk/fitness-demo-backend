package com.example.fitness.api.dto;

import lombok.Data;
import java.util.Map;

/**
 * 通用认证请求 DTO
 *
 * <p>
 * 底层抽象的通用认证请求，通过 {@code type} 和 {@code payload}
 * 动态路由到不同的认证处理逻辑。适用于不同类型的登录或认证请求。
 */
@Data
public class AuthRequest {

    /** 认证类型标识：{@code "login_phone"} / {@code "login_wechat"} */
    private String type;

    /** 依认证类型不同，携带 {@code phone}/{@code code} 等动态字段 */
    private Map<String, Object> payload;
}
