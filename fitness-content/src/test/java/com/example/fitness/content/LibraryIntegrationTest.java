package com.example.fitness.content;

import com.example.fitness.content.mapper.MoveMapper;
import com.example.fitness.content.mapper.UserLibraryMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LibraryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserLibraryMapper userLibraryMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        userLibraryMapper.delete(null);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetLibraryAndAddItem() throws Exception {
        // 1. Get Library
        mockMvc.perform(get("/api/library")
                .param("difficultyLevel", "novice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 2. Add Item to Library
        Map<String, Object> req = new HashMap<>();
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", "1");
        payload.put("itemId", "m_squat");
        payload.put("itemType", "move");
        req.put("payload", payload);

        mockMvc.perform(post("/api/library")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
