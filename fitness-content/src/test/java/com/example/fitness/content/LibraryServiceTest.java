package com.example.fitness.content;

import com.example.fitness.api.dto.LibraryResponse;
import com.example.fitness.content.mapper.MoveMapper;
import com.example.fitness.content.service.impl.LibraryServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LibraryServiceTest {

    @Mock
    private MoveMapper moveMapper;

    @InjectMocks
    private LibraryServiceImpl libraryService;

    @Test
    public void testGetLibrary() {
        // Setup mock if needed (returns empty list by default which is fine for null
        // check or empty check)
        LibraryResponse result = libraryService.getLibrary("expert");
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getMoves());
    }
}
