package com.example.fitness;

import com.example.fitness.api.dto.ScoringRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.example.fitness.common.util.JwtUtil;

@AutoConfigureMockMvc
public class CoreIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    public void testLibraryFlow() throws Exception {
        String token = jwtUtil.generateToken("1");
        mockMvc.perform(get("/api/library")
                .header("Authorization", "Bearer " + token)
                .param("difficulty", "novice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.moves").isArray());
    }

    @Test
    public void testScoringFlow() throws Exception {
        String token = jwtUtil.generateToken("1");
        ScoringRequest req = new ScoringRequest();
        req.setMoveId("m_squat");

        mockMvc.perform(post("/api/ai/score")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").isNumber());
    }
}
