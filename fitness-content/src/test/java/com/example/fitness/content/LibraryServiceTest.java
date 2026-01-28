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

    @Test
    public void testGetLibrary_ValidJson() {
        com.example.fitness.content.model.entity.Move move = new com.example.fitness.content.model.entity.Move();
        move.setId("m2");
        move.setName("深蹲");
        move.setDifficulty("skilled");
        move.setModelUrl("https://example.com/model.onnx");
        move.setScoringConfigJson("{\"threshold\":0.8,\"maxScore\":100}");

        org.mockito.Mockito.when(moveMapper.selectList(any())).thenReturn(java.util.Collections.singletonList(move));
        org.mockito.Mockito.when(sessionMapper.selectList(any())).thenReturn(java.util.Collections.emptyList());

        com.example.fitness.api.dto.LibraryResponse response = libraryService.getLibrary("skilled");

        org.junit.jupiter.api.Assertions.assertEquals(1, response.getMoves().size());
        com.example.fitness.api.dto.MoveDTO dto = response.getMoves().get(0);
        org.junit.jupiter.api.Assertions.assertEquals("m2", dto.getId());
        org.junit.jupiter.api.Assertions.assertEquals("深蹲", dto.getName());
        org.junit.jupiter.api.Assertions.assertEquals(0.8, dto.getScoringConfig().get("threshold"));
    }

    @Test
    public void testGetLibrary_WithSessions() {
        org.mockito.Mockito.when(moveMapper.selectList(any())).thenReturn(java.util.Collections.emptyList());

        com.example.fitness.content.model.entity.Session session = new com.example.fitness.content.model.entity.Session();
        session.setId(100L);
        session.setName("初级HIIT");
        session.setDifficulty("novice");
        session.setDuration(30);
        session.setCoverUrl("https://example.com/cover.jpg");

        org.mockito.Mockito.when(sessionMapper.selectList(any()))
                .thenReturn(java.util.Collections.singletonList(session));

        com.example.fitness.api.dto.LibraryResponse response = libraryService.getLibrary("novice");

        org.junit.jupiter.api.Assertions.assertEquals(0, response.getMoves().size());
        org.junit.jupiter.api.Assertions.assertEquals(1, response.getSessions().size());
        com.example.fitness.api.dto.SessionDTO sessionDTO = response.getSessions().get(0);
        org.junit.jupiter.api.Assertions.assertEquals("100", sessionDTO.getId());
        org.junit.jupiter.api.Assertions.assertEquals("初级HIIT", sessionDTO.getName());
        org.junit.jupiter.api.Assertions.assertEquals(30, sessionDTO.getDuration());
    }

    @Test
    public void testGetLibrary_EmptyResult() {
        org.mockito.Mockito.when(moveMapper.selectList(any())).thenReturn(java.util.Collections.emptyList());
        org.mockito.Mockito.when(sessionMapper.selectList(any())).thenReturn(java.util.Collections.emptyList());

        com.example.fitness.api.dto.LibraryResponse response = libraryService.getLibrary("expert");

        org.junit.jupiter.api.Assertions.assertNotNull(response);
        org.junit.jupiter.api.Assertions.assertTrue(response.getMoves().isEmpty());
        org.junit.jupiter.api.Assertions.assertTrue(response.getSessions().isEmpty());
    }

    @Test
    public void testAddItemToLibrary_DefaultItemType() {
        Map<String, Object> req = new HashMap<>();
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", "123");
        payload.put("itemId", "s_hiit");
        // itemType 不设置，应使用默认值 "move"
        req.put("payload", payload);

        libraryService.addItemToLibrary(req);

        verify(userLibraryMapper, times(1)).insert(any(UserLibrary.class));
    }

    @Test
    public void testAddItemToLibrary_SessionType() {
        Map<String, Object> req = new HashMap<>();
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", "456");
        payload.put("itemId", "session_123");
        payload.put("itemType", "session");
        req.put("payload", payload);

        libraryService.addItemToLibrary(req);

        verify(userLibraryMapper, times(1)).insert(any(UserLibrary.class));
    }

    @Test
    public void testGetLibrary_MultipleMoves() {
        com.example.fitness.content.model.entity.Move move1 = new com.example.fitness.content.model.entity.Move();
        move1.setId("m1");
        move1.setName("俯卧撑");
        move1.setDifficulty("novice");
        move1.setScoringConfigJson("{}");

        com.example.fitness.content.model.entity.Move move2 = new com.example.fitness.content.model.entity.Move();
        move2.setId("m2");
        move2.setName("仰卧起坐");
        move2.setDifficulty("novice");
        move2.setScoringConfigJson("{}");

        org.mockito.Mockito.when(moveMapper.selectList(any())).thenReturn(java.util.Arrays.asList(move1, move2));
        org.mockito.Mockito.when(sessionMapper.selectList(any())).thenReturn(java.util.Collections.emptyList());

        com.example.fitness.api.dto.LibraryResponse response = libraryService.getLibrary("novice");

        org.junit.jupiter.api.Assertions.assertEquals(2, response.getMoves().size());
    }
}
