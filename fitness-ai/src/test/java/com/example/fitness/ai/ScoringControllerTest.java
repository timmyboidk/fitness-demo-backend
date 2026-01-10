package com.example.fitness.ai;

import com.example.fitness.ai.controller.ScoringController;
import com.example.fitness.ai.service.ScoringService;
import com.example.fitness.api.dto.ScoringRequest;
import com.example.fitness.api.dto.ScoringResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScoringController.class)
public class ScoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScoringService scoringService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testScore() throws Exception {
        ScoringResponse mockResponse = new ScoringResponse();
        mockResponse.setSuccess(true);
        mockResponse.setScore(90);

        Mockito.when(scoringService.calculateScore(any())).thenReturn(mockResponse);

        ScoringRequest request = new ScoringRequest();
        request.setMoveId("m1");

        mockMvc.perform(post("/api/ai/score")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(90));
    }
}
