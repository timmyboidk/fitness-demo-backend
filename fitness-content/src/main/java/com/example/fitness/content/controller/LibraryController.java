package com.example.fitness.content.controller;

import com.example.fitness.api.dto.LibraryResponse;
import com.example.fitness.common.result.Result;
import com.example.fitness.content.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {
    private final LibraryService libraryService;

    @GetMapping
    public Result<LibraryResponse> getLibrary(
            @RequestParam(required = false, defaultValue = "novice") String difficultyLevel) {
        return Result.success(libraryService.getLibrary(difficultyLevel));
    }

    @PostMapping
    public Result<Void> addItemToLibrary(@RequestBody Map<String, Object> request) {
        libraryService.addItemToLibrary(request);
        return Result.success(null);
    }
}
