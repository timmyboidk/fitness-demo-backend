package com.example.fitness.user;

import com.example.fitness.common.aspect.IdempotentAspect;
import com.example.fitness.common.aspect.RateLimitAspect;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FullUserScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RateLimitAspect rateLimitAspect;

    @MockitoBean
    private IdempotentAspect idempotentAspect;

    @MockitoBean
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @org.mockito.Mock
    private org.springframework.data.redis.core.ValueOperations<String, String> valueOperations;

    @org.junit.jupiter.api.BeforeEach
    public void setup() {
        org.mockito.Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private static String authToken;
    private static String userId;

    @Test
    @Order(1)
    public void testUserLogin() throws Exception {
        String loginJson = "{\"type\":\"login_phone\",\"code\":\"1234\",\"phone\":\"13800000000\"}";

        MvcResult result = mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println("LOGIN RESPONSE: " + content);

        JsonNode root = objectMapper.readTree(content);
        if (!root.path("success").asBoolean()) {
            System.err.println("LOGIN FAILED. Message: " + root.path("message").asText());
        }

        authToken = root.path("data").path("token").asText();
        userId = root.path("data").path("id").asText();

        System.out.println("Got Token: " + authToken);
    }

    @Test
    @Order(2)
    public void testOnboarding() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("userId", userId);
        req.put("difficultyLevel", "expert");
        System.out.println("DEBUG: testOnboarding userId=" + userId);

        mockMvc.perform(post("/api/auth/onboarding")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
