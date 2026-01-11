package com.example.fitness.content;

import com.example.fitness.content.mapper.MoveMapper;
import com.example.fitness.content.mapper.UserLibraryMapper;
import com.example.fitness.content.model.entity.UserLibrary;
import com.example.fitness.content.service.impl.LibraryServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LibraryServiceTest {

    @Mock
    private MoveMapper moveMapper;

    @Mock
    private UserLibraryMapper userLibraryMapper;

    @Mock
    private com.example.fitness.content.mapper.SessionMapper sessionMapper;

    @InjectMocks
    private LibraryServiceImpl libraryService;

    @Test
    public void testAddItemToLibrary() {
        Map<String, Object> req = new HashMap<>();
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", "1");
        payload.put("itemId", "m_squat");
        payload.put("itemType", "move");
        req.put("payload", payload);

        libraryService.addItemToLibrary(req);

        verify(userLibraryMapper, times(1)).insert(any(UserLibrary.class));
    }

    @Test
    public void testAddItemToLibrary_NullPayload() {
        Map<String, Object> req = new HashMap<>();
        try {
            libraryService.addItemToLibrary(req);
        } catch (com.example.fitness.common.exception.BusinessException e) {
            // PARAM_ERROR
        }
    }

    @Test
    public void testAddItemToLibrary_MissingFields() {
        Map<String, Object> req = new HashMap<>();
        Map<String, Object> payload = new HashMap<>();
        req.put("payload", payload);
        try {
            libraryService.addItemToLibrary(req);
        } catch (com.example.fitness.common.exception.BusinessException e) {
            // PARAM_ERROR
        }
    }

    @Test
    public void testGetLibrary_MalformedJson() {
        com.example.fitness.content.model.entity.Move move = new com.example.fitness.content.model.entity.Move();
        move.setId("m1");
        move.setDifficulty("novice");
        move.setScoringConfigJson("{bad_json");

        org.mockito.Mockito.when(moveMapper.selectList(any())).thenReturn(java.util.Collections.singletonList(move));
        org.mockito.Mockito.when(sessionMapper.selectList(any())).thenReturn(java.util.Collections.emptyList());

        com.example.fitness.api.dto.LibraryResponse response = libraryService.getLibrary("novice");

        // Should contain empty map in scoringConfig
        com.example.fitness.api.dto.MoveDTO dto = response.getMoves().get(0);
        org.junit.jupiter.api.Assertions.assertTrue(dto.getScoringConfig().isEmpty());
    }
}
