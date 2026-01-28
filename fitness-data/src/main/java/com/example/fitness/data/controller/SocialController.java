package com.example.fitness.data.controller;

import com.example.fitness.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 社交与统计控制器 - 提供用户训练统计、排行榜和动态广场接口
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "社交统计模块", description = "用户训练统计、排行榜和社交动态")
public class SocialController {

    /**
     * 获取用户训练统计
     * 
     * @return 返回用户训练统计数据
     */
    @Operation(summary = "获取训练统计", description = "获取用户的周训练时长、总消耗卡路里、完成率及训练历史")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @GetMapping("/user/stats")
    public Result<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("weeklyDuration", 120);
        stats.put("totalCalories", 1500);
        stats.put("completionRate", 85);

        List<Map<String, Object>> history = new ArrayList<>();
        Map<String, Object> historyEntry = new HashMap<>();
        historyEntry.put("date", "2024-01-20");
        historyEntry.put("duration", 30);
        history.add(historyEntry);
        stats.put("history", history);

        return Result.success(stats);
    }

    /**
     * 获取排行榜
     * 
     * @param type 排行榜类型 (daily, weekly, monthly)
     * @return 返回排名列表
     */
    @Operation(summary = "获取排行榜", description = "获取用户排名列表，支持按日、周、月筛选")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @GetMapping("/social/leaderboard")
    public Result<List<Map<String, Object>>> getLeaderboard(
            @Parameter(description = "排行榜类型：daily, weekly, monthly") @RequestParam(defaultValue = "weekly") String type) {
        List<Map<String, Object>> leaderboard = new ArrayList<>();

        Map<String, Object> entry1 = new HashMap<>();
        entry1.put("rank", 1);
        entry1.put("nickname", "训练营课代表");
        entry1.put("score", 12500);
        entry1.put("avatar", "https://example.com/avatar1.jpg");
        leaderboard.add(entry1);

        Map<String, Object> entry2 = new HashMap<>();
        entry2.put("rank", 2);
        entry2.put("nickname", "周六坚持健身");
        entry2.put("score", 11800);
        entry2.put("avatar", "https://example.com/avatar2.jpg");
        leaderboard.add(entry2);

        return Result.success(leaderboard);
    }

    /**
     * 获取动态广场
     * 
     * @return 返回社交动态列表
     */
    @Operation(summary = "动态广场", description = "获取社区用户动态feed流")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @GetMapping("/social/feed")
    public Result<List<Map<String, Object>>> getSocialFeed() {
        List<Map<String, Object>> feed = new ArrayList<>();

        Map<String, Object> feedEntry = new HashMap<>();
        feedEntry.put("id", "f_1");
        feedEntry.put("user", "Jack");
        feedEntry.put("content", "完成了 HIIT 训练");
        feedEntry.put("time", "5分钟前");
        feed.add(feedEntry);

        return Result.success(feed);
    }
}
