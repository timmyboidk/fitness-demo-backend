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
}
