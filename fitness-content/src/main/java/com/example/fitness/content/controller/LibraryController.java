package com.example.fitness.content.controller;

import com.example.fitness.api.dto.LibraryResponse;
import com.example.fitness.common.result.Result;
import com.example.fitness.content.service.LibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 动作库控制器 - 管理健身动作内容
 */
@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
@Tag(name = "内容库模块", description = "健身动作和课程内容管理")
public class LibraryController {
    private final LibraryService libraryService;

    /**
     * 获取动作库列表
     * 
     * @param difficultyLevel 难度等级过滤
     */
    @Operation(summary = "同步库数据", description = "获取健身动作和课程库列表，支持按难度等级筛选")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @GetMapping
    public Result<LibraryResponse> getLibrary(
            @Parameter(description = "难度等级：novice, skilled, expert") @RequestParam(required = false, defaultValue = "novice") String difficultyLevel) {
        return Result.success(libraryService.getLibrary(difficultyLevel));
    }

    /**
     * 添加动作到个人库
     */
    @Operation(summary = "添加到个人库", description = "将动作或课程添加到用户的个人收藏")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
    public Result<Void> addItemToLibrary(@RequestBody Map<String, Object> request) {
        libraryService.addItemToLibrary(request);
        return Result.success(null);
    }
}
