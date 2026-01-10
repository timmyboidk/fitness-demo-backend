package com.example.fitness.content.controller;

import com.example.fitness.api.dto.LibraryResponse;
import com.example.fitness.common.result.Result;
import com.example.fitness.content.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 动作库控制器 - 管理健身动作内容
 */
@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {
    private final LibraryService libraryService;

    /**
     * 获取动作库列表
     * 
     * @param difficultyLevel 难度等级过滤
     */
    @GetMapping
    public Result<LibraryResponse> getLibrary(
            @RequestParam(required = false, defaultValue = "novice") String difficultyLevel) {
        return Result.success(libraryService.getLibrary(difficultyLevel));
    }

    /**
     * 添加动作到个人库
     */
    @PostMapping
    public Result<Void> addItemToLibrary(@RequestBody Map<String, Object> request) {
        libraryService.addItemToLibrary(request);
        return Result.success(null);
    }
}
