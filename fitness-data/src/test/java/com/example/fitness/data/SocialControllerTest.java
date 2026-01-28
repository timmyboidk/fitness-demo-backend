package com.example.fitness.data;

import com.example.fitness.data.controller.SocialController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 社交控制器单元测试
 * 测试用户统计、排行榜和动态广场接口
 */
@WebMvcTest(SocialController.class)
@DisplayName("社交控制器测试")
class SocialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("用户训练统计测试")
    class UserStatsTests {

        @Test
        @DisplayName("获取用户统计 - 应返回完整统计数据")
        void getUserStats_ReturnsCompleteStats() throws Exception {
            mockMvc.perform(get("/api/user/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.weeklyDuration").value(120))
                    .andExpect(jsonPath("$.data.totalCalories").value(1500))
                    .andExpect(jsonPath("$.data.completionRate").value(85))
                    .andExpect(jsonPath("$.data.history").isArray())
                    .andExpect(jsonPath("$.data.history[0].date").value("2024-01-20"))
                    .andExpect(jsonPath("$.data.history[0].duration").value(30));
        }
    }

    @Nested
    @DisplayName("排行榜测试")
    class LeaderboardTests {

        @Test
        @DisplayName("获取周排行榜 - 默认类型")
        void getLeaderboard_DefaultWeekly() throws Exception {
            mockMvc.perform(get("/api/social/leaderboard"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].rank").value(1))
                    .andExpect(jsonPath("$.data[0].nickname").value("训练营课代表"))
                    .andExpect(jsonPath("$.data[0].score").value(12500))
                    .andExpect(jsonPath("$.data[1].rank").value(2));
        }

        @Test
        @DisplayName("获取日排行榜 - type=daily")
        void getLeaderboard_DailyType() throws Exception {
            mockMvc.perform(get("/api/social/leaderboard")
                    .param("type", "daily"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("获取月排行榜 - type=monthly")
        void getLeaderboard_MonthlyType() throws Exception {
            mockMvc.perform(get("/api/social/leaderboard")
                    .param("type", "monthly"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("动态广场测试")
    class SocialFeedTests {

        @Test
        @DisplayName("获取动态列表 - 应返回用户动态")
        void getSocialFeed_ReturnsFeedEntries() throws Exception {
            mockMvc.perform(get("/api/social/feed"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value("f_1"))
                    .andExpect(jsonPath("$.data[0].user").value("Jack"))
                    .andExpect(jsonPath("$.data[0].content").value("完成了 HIIT 训练"))
                    .andExpect(jsonPath("$.data[0].time").value("5分钟前"));
        }
    }
}
