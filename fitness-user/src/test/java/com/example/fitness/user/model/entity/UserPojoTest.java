package com.example.fitness.user.model.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;

class UserPojoTest {

    @Test
    void testUserPojo() {
        User user1 = new User();
        user1.setId(1L);
        user1.setPhone("13800000000");
        user1.setNickname("test");
        user1.setPassword("pwd");
        user1.setOpenId("openId");
        user1.setSessionKey("key");
        user1.setDifficultyLevel("novice");
        user1.setAvatar("avatar");
        user1.setTotalScore(100);
        user1.setTotalDuration(200);
        LocalDateTime now = LocalDateTime.now();
        user1.setCreatedAt(now);
        user1.setUpdatedAt(now);

        assertThat(user1.getId()).isEqualTo(1L);
        assertThat(user1.getPhone()).isEqualTo("13800000000");
        assertThat(user1.getNickname()).isEqualTo("test");
        assertThat(user1.getPassword()).isEqualTo("pwd");
        assertThat(user1.getOpenId()).isEqualTo("openId");
        assertThat(user1.getSessionKey()).isEqualTo("key");
        assertThat(user1.getDifficultyLevel()).isEqualTo("novice");
        assertThat(user1.getAvatar()).isEqualTo("avatar");
        assertThat(user1.getTotalScore()).isEqualTo(100);
        assertThat(user1.getTotalDuration()).isEqualTo(200);
        assertThat(user1.getCreatedAt()).isEqualTo(now);
        assertThat(user1.getUpdatedAt()).isEqualTo(now);

        User user2 = new User();
        user2.setId(1L);
        user2.setPhone("13800000000");
        user2.setNickname("test");
        user2.setPassword("pwd");
        user2.setOpenId("openId");
        user2.setSessionKey("key");
        user2.setDifficultyLevel("novice");
        user2.setAvatar("avatar");
        user2.setTotalScore(100);
        user2.setTotalDuration(200);
        user2.setCreatedAt(now);
        user2.setUpdatedAt(now);

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        assertThat(user1.toString()).contains("13800000000");
    }
}
