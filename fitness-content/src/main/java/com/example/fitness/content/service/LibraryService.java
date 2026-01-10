package com.example.fitness.content.service;

import com.example.fitness.api.dto.LibraryResponse;
import java.util.Map;

public interface LibraryService {
    LibraryResponse getLibrary(String difficultyLevel);

    void addItemToLibrary(Map<String, Object> request);
}
