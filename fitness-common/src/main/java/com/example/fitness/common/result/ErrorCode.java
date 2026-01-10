package com.example.fitness.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    // --- 成功 ---
    SUCCESS(200, "操作成功"),

    // --- 系统错误 (5xx) ---
    INTERNAL_SERVER_ERROR(500, "系统内部错误"),
    KAFKA_SEND_ERROR(501, "消息队列发送失败"),

    // --- 通用业务错误 (4xx) ---
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    // --- 用户模块错误 (1000+) ---
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ID_REQUIRED(1002, "用户 ID 必填"),
    INVALID_LOGIN_TYPE(1003, "不支持的登录类型"),
    LOGIN_FAILED(1004, "登录失败"),

    // --- 动作与库模块错误 (2000+) ---
    MOVE_NOT_FOUND(2001, "健身动作不存在"),
    LIBRARY_ITEM_ALREADY_EXISTS(2002, "该项目已在库中"),

    // --- AI 模块错误 (3000+) ---
    SCORING_FAILED(3001, "AI 评分计算失败"),
    MODEL_NOT_FOUND(3002, "模型版本不存在");

    private final Integer code;
    private final String message;
}
