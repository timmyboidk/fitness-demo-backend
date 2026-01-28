package com.example.fitness.data.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实体类单元测试
 * 测试 Lombok 生成的 getter/setter/equals/hashCode/toString 方法
 */
@DisplayName("Data 模块实体类单元测试")
class UserStatsTest {

    @Test
    @DisplayName("UserStats 实体 - 测试所有字段的 getter/setter")
    void testUserStats_GettersSetters() {
        UserStats stats = new UserStats();
        LocalDateTime now = LocalDateTime.now();

        stats.setId(100L);
        stats.setTotalScore(5000);
        stats.setTotalDuration(3600);
        stats.setUpdatedAt(now);

        assertEquals(100L, stats.getId());
        assertEquals(5000, stats.getTotalScore());
        assertEquals(3600, stats.getTotalDuration());
        assertEquals(now, stats.getUpdatedAt());
    }

    @Test
    @DisplayName("UserStats 实体 - 测试 equals 和 hashCode")
    void testUserStats_EqualsHashCode() {
        UserStats stats1 = new UserStats();
        stats1.setId(1L);
        stats1.setTotalScore(100);

        UserStats stats2 = new UserStats();
        stats2.setId(1L);
        stats2.setTotalScore(100);

        UserStats stats3 = new UserStats();
        stats3.setId(2L);
        stats3.setTotalScore(200);

        assertEquals(stats1, stats2);
        assertEquals(stats1.hashCode(), stats2.hashCode());
        assertNotEquals(stats1, stats3);
    }

    @Test
    @DisplayName("UserStats 实体 - 测试 toString")
    void testUserStats_ToString() {
        UserStats stats = new UserStats();
        stats.setId(999L);
        stats.setTotalScore(12345);
        stats.setTotalDuration(7200);

        String str = stats.toString();
        assertTrue(str.contains("999"));
        assertTrue(str.contains("12345"));
        assertTrue(str.contains("7200"));
    }

    @Test
    @DisplayName("UserStats 实体 - 测试默认值为 null")
    void testUserStats_DefaultValues() {
        UserStats stats = new UserStats();

        assertNull(stats.getId());
        assertNull(stats.getTotalScore());
        assertNull(stats.getTotalDuration());
        assertNull(stats.getUpdatedAt());
    }

    @Test
    @DisplayName("UserStats 实体 - 边界值测试")
    void testUserStats_BoundaryValues() {
        UserStats stats = new UserStats();

        // 测试最大值
        stats.setTotalScore(Integer.MAX_VALUE);
        stats.setTotalDuration(Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, stats.getTotalScore());
        assertEquals(Integer.MAX_VALUE, stats.getTotalDuration());

        // 测试0值
        stats.setTotalScore(0);
        stats.setTotalDuration(0);

        assertEquals(0, stats.getTotalScore());
        assertEquals(0, stats.getTotalDuration());
    }
}
