package com.example.fitness.content;

import com.example.fitness.content.service.impl.LibraryServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class LibraryServiceTest {

    private final LibraryServiceImpl libraryService = new LibraryServiceImpl();

    @Test
    public void testGetLibraryByDifficulty() {
        Map<String, Object> result = libraryService.getLibraryByDifficulty("expert");
        List<?> moves = (List<?>) result.get("moves");
        Assertions.assertFalse(moves.isEmpty());
        // Add more assertions based on actual implementation
    }
}
