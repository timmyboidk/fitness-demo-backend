package com.example.fitness.common.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ErrorCode 枚举类单元测试
 */
@DisplayName("ErrorCode 枚举单元测试")
class ErrorCodeTest {

    @Test
    @DisplayName("SUCCESS - 成功码为200")
    void success_HasCorrectCodeAndMessage() {
        assertEquals(200, ErrorCode.SUCCESS.getCode());
        assertEquals("操作成功", ErrorCode.SUCCESS.getMessage());
    }

    @Test
    @DisplayName("PARAM_ERROR - 参数错误码为400")
    void paramError_HasCorrectCodeAndMessage() {
        assertEquals(400, ErrorCode.PARAM_ERROR.getCode());
        assertEquals("参数错误", ErrorCode.PARAM_ERROR.getMessage());
    }

    @Test
    @DisplayName("UNAUTHORIZED - 未授权码为401")
    void unauthorized_HasCorrectCodeAndMessage() {
        assertEquals(401, ErrorCode.UNAUTHORIZED.getCode());
        assertEquals("未授权", ErrorCode.UNAUTHORIZED.getMessage());
    }

    @Test
    @DisplayName("USER_NOT_FOUND - 用户不存在码为1001")
    void userNotFound_HasCorrectCodeAndMessage() {
        assertEquals(1001, ErrorCode.USER_NOT_FOUND.getCode());
        assertEquals("用户不存在", ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("INTERNAL_SERVER_ERROR - 服务器错误码为500")
    void internalServerError_HasCorrectCodeAndMessage() {
        assertEquals(500, ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        assertEquals("系统内部错误", ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    @Test
    @DisplayName("NOT_FOUND - 资源不存在码为404")
    void notFound_HasCorrectCodeAndMessage() {
        assertEquals(404, ErrorCode.NOT_FOUND.getCode());
        assertEquals("资源不存在", ErrorCode.NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("FORBIDDEN - 禁止访问码为403")
    void forbidden_HasCorrectCodeAndMessage() {
        assertEquals(403, ErrorCode.FORBIDDEN.getCode());
        assertEquals("禁止访问", ErrorCode.FORBIDDEN.getMessage());
    }

    @Test
    @DisplayName("KAFKA_SEND_ERROR - Kafka发送错误码为501")
    void kafkaSendError_HasCorrectCodeAndMessage() {
        assertEquals(501, ErrorCode.KAFKA_SEND_ERROR.getCode());
        assertEquals("消息队列发送失败", ErrorCode.KAFKA_SEND_ERROR.getMessage());
    }

    @Test
    @DisplayName("遍历所有枚举值验证非空")
    void allValues_HaveNonNullCodeAndMessage() {
        for (ErrorCode code : ErrorCode.values()) {
            assertNotNull(code.getCode());
            assertNotNull(code.getMessage());
            assertFalse(code.getMessage().isEmpty());
        }
    }

    @Test
    @DisplayName("valueOf - 可以通过名称获取枚举")
    void valueOf_ReturnsCorrectEnum() {
        assertEquals(ErrorCode.SUCCESS, ErrorCode.valueOf("SUCCESS"));
        assertEquals(ErrorCode.PARAM_ERROR, ErrorCode.valueOf("PARAM_ERROR"));
    }
}
