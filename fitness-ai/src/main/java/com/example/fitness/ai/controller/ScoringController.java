package com.example.fitness.ai.controller;

import com.example.fitness.api.dto.ScoringRequest;
import com.example.fitness.api.dto.ScoringResponse;
import com.example.fitness.common.result.Result;
import com.example.fitness.ai.service.ScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 评分控制器 - 提供动作评分接口及模型版本管理
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "AI核心模块", description = "AI动作评分和模型管理")
public class ScoringController {
    private final ScoringService scoringService;

    /**
     * 动作评分接口
     * 
     * @param request 包含动作ID和关键点数据
     * @return 返回评分结果（分数值及建议）
     */
    @Operation(summary = "AI实时动作评分", description = "根据用户姿态关键点数据计算动作得分并返回反馈建议")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "429", description = "AI服务限流")
    })
    @PostMapping("/ai/score")
    public Result<ScoringResponse> score(@RequestBody ScoringRequest request) {
        return Result.success(scoringService.calculateScore(request));
    }

    /**
     * 获取最新 AI 模型版本
     * 
     * @param platform       平台（iOS/Android）
     * @param currentVersion 当前版本
     * @return 返回更新信息及下载地址
     */
    @Operation(summary = "获取最新AI模型", description = "检查是否有新版本的AI模型可供下载")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "操作成功")
    })
    @GetMapping("/core/models/latest")
    public Result<Map<String, Object>> getLatestModel(
            @Parameter(description = "平台类型：ios, android") @RequestParam String platform,
            @Parameter(description = "当前客户端模型版本") @RequestParam String currentVersion) {
        Map<String, Object> response = new HashMap<>();
        response.put("hasUpdate", true);
        Map<String, Object> data = new HashMap<>();
        data.put("version", "1.1.0");
        data.put("downloadUrl", "https://oss.fitness.com/models/pose_v1.1_" + platform + ".onnx");
        data.put("md5", "a3f8...");
        data.put("forceUpdate", false);
        data.put("releaseNotes", "Optimized for latest devices.");
        response.put("data", data);
        return Result.success(response);
    }
}
