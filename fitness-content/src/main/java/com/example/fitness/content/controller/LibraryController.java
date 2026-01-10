package com.example.fitness.content.controller;

import com.example.fitness.api.dto.MoveDTO;
import com.example.fitness.common.result.Result;
import com.example.fitness.content.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {
    private final LibraryService libraryService;

    @GetMapping
    public Result<Map<String, Object>> getLibrary(
            @RequestParam(required = false, defaultValue = "novice") String difficultyLevel) {
        return Result.success(libraryService.getLibraryByDifficulty(difficultyLevel));
    }

    @PostMapping
    public Result<Void> addItemToLibrary(@RequestBody Map<String, Object> request) {
        // Mock implementation
        return Result.success(null);
    }
}
