package com.example.fitness.content;

import com.example.fitness.content.controller.LibraryController;
import com.example.fitness.content.service.LibraryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryController.class)
public class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibraryService libraryService;

    @Test
    public void testGetLibrary() throws Exception {
        com.example.fitness.api.dto.LibraryResponse mockResponse = new com.example.fitness.api.dto.LibraryResponse();
        mockResponse.setMoves(java.util.Collections.emptyList());

        Mockito.when(libraryService.getLibrary(anyString())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/library")
                .param("difficulty", "novice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.moves").isArray());
    }
}
