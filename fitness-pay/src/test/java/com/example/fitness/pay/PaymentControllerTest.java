package com.example.fitness.pay;

import com.example.fitness.pay.controller.PaymentController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 支付控制器单元测试
 * 测试会员凭证校验接口的各种场景
 */
@WebMvcTest(PaymentController.class)
@DisplayName("支付控制器测试")
@SuppressWarnings("null") // Suppress null type safety warnings from MockMvc and Hamcrest matchers
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("会员凭证校验测试")
    class VerifyReceiptTests {

        @Test
        @DisplayName("年度订阅 - 正常校验成功")
        void verifyReceipt_YearlyPlan_Success() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", "user_123");
            request.put("planId", "yearly");
            request.put("receipt", "mock_receipt_data");
            request.put("platform", "ios");

            mockMvc.perform(post("/api/pay/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isVip").value(true))
                    .andExpect(jsonPath("$.data.planName").value("年度订阅"))
                    .andExpect(jsonPath("$.data.expireTime").isNumber());
        }

        @Test
        @DisplayName("月度订阅 - 正常校验成功")
        void verifyReceipt_MonthlyPlan_Success() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", "user_456");
            request.put("planId", "monthly");
            request.put("receipt", "monthly_receipt");
            request.put("platform", "android");

            mockMvc.perform(post("/api/pay/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isVip").value(true))
                    .andExpect(jsonPath("$.data.planName").value("月度订阅"));
        }

        @Test
        @DisplayName("季度订阅 - 正常校验成功")
        void verifyReceipt_QuarterlyPlan_Success() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", "user_789");
            request.put("planId", "quarterly");
            request.put("receipt", "quarterly_receipt");
            request.put("platform", "ios");

            mockMvc.perform(post("/api/pay/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isVip").value(true))
                    .andExpect(jsonPath("$.data.planName").value("季度订阅"));
        }

        @Test
        @DisplayName("planId为null时 - 默认使用年度订阅")
        void verifyReceipt_NullPlanId_DefaultsToYearly() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", "user_abc");
            request.put("receipt", "default_receipt");
            request.put("platform", "ios");
            // planId 不设置

            mockMvc.perform(post("/api/pay/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.planName").value("年度订阅"));
        }

        @Test
        @DisplayName("未知planId - 默认使用年度订阅")
        void verifyReceipt_UnknownPlanId_DefaultsToYearly() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", "user_xyz");
            request.put("planId", "unknown_plan");
            request.put("receipt", "unknown_receipt");
            request.put("platform", "android");

            mockMvc.perform(post("/api/pay/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.planName").value("年度订阅"));
        }

        @Test
        @DisplayName("空请求体 - 应正常处理null值")
        void verifyReceipt_EmptyRequest_HandlesNullValues() throws Exception {
            Map<String, Object> request = new HashMap<>();
            // 全部字段为空

            mockMvc.perform(post("/api/pay/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isVip").value(true)) // 当前模拟实现始终返回成功
                    .andExpect(jsonPath("$.data.planName").value("年度订阅")); // 默认计划
        }

        @Test
        @DisplayName("过期时间验证 - 应约为一年后")
        void verifyReceipt_ExpireTime_ShouldBeAboutOneYearLater() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", "user_time");
            request.put("planId", "yearly");
            request.put("receipt", "time_receipt");
            request.put("platform", "ios");

            long beforeRequest = System.currentTimeMillis();

            mockMvc.perform(post("/api/pay/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.expireTime",
                            greaterThan(beforeRequest + 364L * 24 * 60 * 60 * 1000)))
                    .andExpect(jsonPath("$.data.expireTime",
                            lessThan(beforeRequest + 366L * 24 * 60 * 60 * 1000)));
        }
    }
}
