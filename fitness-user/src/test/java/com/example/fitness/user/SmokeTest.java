package com.example.fitness.user;

import com.example.fitness.FitnessApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.condition.EnabledIf;
import org.testcontainers.DockerClientFactory;

/**
 * 冒烟测试 - 验证核心 API 端点在测试环境下的可用性
 * 
 * <p>
 * 注意：这些测试在没有完整中间件的情况下运行，
 * 因此验证的是 API 端点的可访问性，而非完整功能
 * </p>
 * 
 * @since JDK 21, Spring Boot 3.x
 */
@SpringBootTest(classes = FitnessApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("API 冒烟测试")
@EnabledIf("isDockerAvailable")
class SmokeTest {

    /**
     * 检查 Docker 是否可用
     */
    static boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    // ==================== 认证模块冒烟测试 ====================

    @Test
    @DisplayName("POST /api/auth/login - 登录端点可访问并返回 JSON 响应")
    void loginEndpoint_ReturnsJsonResponse() throws Exception {
        String loginRequest = """
                {
                    "phone": "13800138000",
                    "code": "123456",
                    "type": "phone"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        // 验证响应包含基本结构
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("success");
        assertThat(responseBody).contains("code");
    }

    @Test
    @DisplayName("POST /api/auth/login - 空请求体返回响应")
    void loginEndpoint_EmptyBody_ReturnsResponse() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== AI 模块冒烟测试 ====================

    @Test
    @DisplayName("GET /api/core/models/latest - AI 模型版本端点可访问")
    void aiModelVersionEndpoint_ReturnsJsonResponse() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/core/models/latest")
                .param("platform", "ios")
                .param("currentVersion", "1.0.0"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        // 验证响应包含基本结构
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("success");
    }

    // ==================== 响应格式验证 ====================

    @Test
    @DisplayName("所有 API 响应包含标准字段: success, code")
    void allApiResponses_ContainStandardFields() throws Exception {
        String loginRequest = """
                {
                    "phone": "13800138001",
                    "code": "654321",
                    "type": "phone"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andReturn();

        String response = result.getResponse().getContentAsString();

        // 验证标准响应字段存在
        assertThat(response).contains("\"success\"");
        assertThat(response).contains("\"code\"");
    }

    @Test
    @DisplayName("API Content-Type 是 JSON")
    void apiResponses_AreJson() throws Exception {
        String loginRequest = """
                {
                    "phone": "13800138002",
                    "code": "111111",
                    "type": "phone"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
