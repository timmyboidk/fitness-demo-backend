package com.example.fitness.ai.controller;

import com.example.fitness.api.dto.ScoringRequest;
import com.example.fitness.api.dto.ScoringResponse;
import com.example.fitness.common.result.Result;
import com.example.fitness.ai.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScoringController {
    private final ScoringService scoringService;

    @PostMapping("/ai/score")
    public Result<ScoringResponse> score(@RequestBody ScoringRequest request) {
        return Result.success(scoringService.calculateScore(request));
    }

    @GetMapping("/core/models/latest")
    public Result<Map<String, Object>> getLatestModel(@RequestParam String platform,
            @RequestParam String currentVersion) {
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
