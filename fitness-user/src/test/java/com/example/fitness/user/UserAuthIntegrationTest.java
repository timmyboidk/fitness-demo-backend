package com.example.fitness.user;

import com.example.fitness.AbstractIntegrationTest;
import com.example.fitness.api.dto.LoginRequest;
import com.example.fitness.user.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public class UserAuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        userMapper.delete(null);
    }

    @Test
    public void testFullOnboardingFlow() throws Exception {
        // 1. Login
        LoginRequest loginReq = new LoginRequest();
        loginReq.setType("login_phone");
        loginReq.setPhone("13912345678");

        mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 2. Onboarding
        Map<String, Object> onboardingReq = new HashMap<>();
        onboardingReq.put("userId", "1"); // Assuming ID 1 for first user in fresh DB or from response
        onboardingReq.put("difficultyLevel", "expert");

        mockMvc.perform(post("/api/auth/onboarding")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(onboardingReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scoringTolerance").value(5));
    }
}
