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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AI 评分控制器单元测试
 */
@WebMvcTest(ScoringController.class)
public class ScoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScoringService scoringService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试动作评分接口
     */
    @Test
    @SuppressWarnings("null")
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

    /**
     * 测试获取最新AI模型版本 - iOS平台
     */
    @Test
    public void testGetLatestModel_iOS() throws Exception {
        mockMvc.perform(get("/api/core/models/latest")
                .param("platform", "ios")
                .param("currentVersion", "1.0.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasUpdate").value(true))
                .andExpect(jsonPath("$.data.data.version").value("1.1.0"))
                .andExpect(
                        jsonPath("$.data.data.downloadUrl").value("https://oss.fitness.com/models/pose_v1.1_ios.onnx"))
                .andExpect(jsonPath("$.data.data.forceUpdate").value(false))
                .andExpect(jsonPath("$.data.data.releaseNotes").value("Optimized for latest devices."));
    }

    /**
     * 测试获取最新AI模型版本 - Android平台
     */
    @Test
    public void testGetLatestModel_Android() throws Exception {
        mockMvc.perform(get("/api/core/models/latest")
                .param("platform", "android")
                .param("currentVersion", "1.0.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasUpdate").value(true))
                .andExpect(jsonPath("$.data.data.version").value("1.1.0"))
                .andExpect(jsonPath("$.data.data.downloadUrl")
                        .value("https://oss.fitness.com/models/pose_v1.1_android.onnx"))
                .andExpect(jsonPath("$.data.data.md5").value("a3f8..."));
    }
}
