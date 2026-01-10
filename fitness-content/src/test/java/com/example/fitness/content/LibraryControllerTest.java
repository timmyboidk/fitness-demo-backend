package com.example.fitness.content;

import com.example.fitness.content.controller.LibraryController;
import com.example.fitness.content.service.LibraryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryController.class)
public class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LibraryService libraryService;

    @Test
    public void testGetLibrary() throws Exception {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("items", "mock_items");

        Mockito.when(libraryService.getLibraryByDifficulty(anyString())).thenReturn(mockData);

        mockMvc.perform(get("/api/library")
                .param("difficulty", "novice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").value("mock_items"));
    }
}
