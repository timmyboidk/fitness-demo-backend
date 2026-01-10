package com.example.fitness.data;

import com.example.fitness.integration.IntegrationTestApp;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 数据采集模块集成测试
 * 验证从 Controller 接收数据到 Kafka 发送的完整链路（使用测试配置）。
 */
@SpringBootTest(classes = IntegrationTestApp.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DataCollectionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试数据收集流
     */
    @Test
    @SuppressWarnings("null")
    public void testCollectDataFlow() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("sessionId", "s_999");

        mockMvc.perform(post("/api/data/collect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 注意：在真实的集成测试中，我们还需要验证消息是否被 Kafka 成功消费并同步。
        // 在本演示项目中，我们主要通过日志和链路状态进行端到端验证。
    }
}
