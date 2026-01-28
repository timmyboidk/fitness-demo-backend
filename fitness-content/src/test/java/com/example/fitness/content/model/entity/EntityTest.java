package com.example.fitness.content.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实体类单元测试
 * 测试 Lombok 生成的 getter/setter/equals/hashCode/toString 方法，确保覆盖率
 */
@DisplayName("实体类单元测试")
class EntityTest {

    @Test
    @DisplayName("Move 实体 - 测试所有字段的 getter/setter")
    void testMove_GettersSetters() {
        Move move = new Move();
        LocalDateTime now = LocalDateTime.now();

        move.setId("m_test");
        move.setName("测试动作");
        move.setDifficulty("novice");
        move.setModelUrl("https://example.com/model.onnx");
        move.setScoringConfigJson("{\"threshold\":0.8}");
        move.setCreatedAt(now);

        assertEquals("m_test", move.getId());
        assertEquals("测试动作", move.getName());
        assertEquals("novice", move.getDifficulty());
        assertEquals("https://example.com/model.onnx", move.getModelUrl());
        assertEquals("{\"threshold\":0.8}", move.getScoringConfigJson());
        assertEquals(now, move.getCreatedAt());
    }

    @Test
    @DisplayName("Move 实体 - 测试 equals 和 hashCode")
    void testMove_EqualsHashCode() {
        Move move1 = new Move();
        move1.setId("m_same");
        move1.setName("相同动作");

        Move move2 = new Move();
        move2.setId("m_same");
        move2.setName("相同动作");

        Move move3 = new Move();
        move3.setId("m_different");

        assertEquals(move1, move2);
        assertEquals(move1.hashCode(), move2.hashCode());
        assertNotEquals(move1, move3);
    }

    @Test
    @DisplayName("Move 实体 - 测试 toString")
    void testMove_ToString() {
        Move move = new Move();
        move.setId("m_str");
        move.setName("ToString测试");

        String str = move.toString();
        assertTrue(str.contains("m_str"));
        assertTrue(str.contains("ToString测试"));
    }

    @Test
    @DisplayName("Session 实体 - 测试所有字段的 getter/setter")
    void testSession_GettersSetters() {
        Session session = new Session();
        LocalDateTime now = LocalDateTime.now();

        session.setId(100L);
        session.setName("HIIT训练");
        session.setDifficulty("skilled");
        session.setDuration(30);
        session.setCoverUrl("https://example.com/cover.jpg");
        session.setCreatedAt(now);

        assertEquals(100L, session.getId());
        assertEquals("HIIT训练", session.getName());
        assertEquals("skilled", session.getDifficulty());
        assertEquals(30, session.getDuration());
        assertEquals("https://example.com/cover.jpg", session.getCoverUrl());
        assertEquals(now, session.getCreatedAt());
    }

    @Test
    @DisplayName("Session 实体 - 测试 equals 和 hashCode")
    void testSession_EqualsHashCode() {
        Session session1 = new Session();
        session1.setId(1L);
        session1.setName("训练1");

        Session session2 = new Session();
        session2.setId(1L);
        session2.setName("训练1");

        Session session3 = new Session();
        session3.setId(2L);

        assertEquals(session1, session2);
        assertEquals(session1.hashCode(), session2.hashCode());
        assertNotEquals(session1, session3);
    }

    @Test
    @DisplayName("Session 实体 - 测试 toString")
    void testSession_ToString() {
        Session session = new Session();
        session.setId(999L);
        session.setName("力量训练");

        String str = session.toString();
        assertTrue(str.contains("999"));
        assertTrue(str.contains("力量训练"));
    }

    @Test
    @DisplayName("UserLibrary 实体 - 测试所有字段的 getter/setter")
    void testUserLibrary_GettersSetters() {
        UserLibrary userLibrary = new UserLibrary();
        LocalDateTime now = LocalDateTime.now();

        userLibrary.setId(1L);
        userLibrary.setUserId(123L); // userId 是 Long 类型
        userLibrary.setItemId("m_squat");
        userLibrary.setItemType("move");
        userLibrary.setCreatedAt(now);

        assertEquals(1L, userLibrary.getId());
        assertEquals(123L, userLibrary.getUserId());
        assertEquals("m_squat", userLibrary.getItemId());
        assertEquals("move", userLibrary.getItemType());
        assertEquals(now, userLibrary.getCreatedAt());
    }

    @Test
    @DisplayName("UserLibrary 实体 - 测试 equals 和 hashCode")
    void testUserLibrary_EqualsHashCode() {
        UserLibrary lib1 = new UserLibrary();
        lib1.setId(1L);
        lib1.setUserId(100L);

        UserLibrary lib2 = new UserLibrary();
        lib2.setId(1L);
        lib2.setUserId(100L);

        UserLibrary lib3 = new UserLibrary();
        lib3.setId(2L);

        assertEquals(lib1, lib2);
        assertEquals(lib1.hashCode(), lib2.hashCode());
        assertNotEquals(lib1, lib3);
    }

    @Test
    @DisplayName("UserLibrary 实体 - 测试 toString")
    void testUserLibrary_ToString() {
        UserLibrary lib = new UserLibrary();
        lib.setUserId(999L);
        lib.setItemId("m_test");

        String str = lib.toString();
        assertTrue(str.contains("999"));
        assertTrue(str.contains("m_test"));
    }

    @Test
    @DisplayName("SessionMove 实体 - 测试所有字段的 getter/setter")
    void testSessionMove_GettersSetters() {
        SessionMove sessionMove = new SessionMove();
        LocalDateTime now = LocalDateTime.now();

        sessionMove.setId(10L);
        sessionMove.setSessionId(100L);
        sessionMove.setMoveId(200L); // moveId 是 Long 类型
        sessionMove.setSortOrder(1);
        sessionMove.setDurationSeconds(30);
        sessionMove.setCreatedAt(now);

        assertEquals(10L, sessionMove.getId());
        assertEquals(100L, sessionMove.getSessionId());
        assertEquals(200L, sessionMove.getMoveId());
        assertEquals(1, sessionMove.getSortOrder());
        assertEquals(30, sessionMove.getDurationSeconds());
        assertEquals(now, sessionMove.getCreatedAt());
    }

    @Test
    @DisplayName("SessionMove 实体 - 测试 equals 和 hashCode")
    void testSessionMove_EqualsHashCode() {
        SessionMove sm1 = new SessionMove();
        sm1.setId(1L);
        sm1.setSessionId(100L);

        SessionMove sm2 = new SessionMove();
        sm2.setId(1L);
        sm2.setSessionId(100L);

        SessionMove sm3 = new SessionMove();
        sm3.setId(2L);

        assertEquals(sm1, sm2);
        assertEquals(sm1.hashCode(), sm2.hashCode());
        assertNotEquals(sm1, sm3);
    }

    @Test
    @DisplayName("SessionMove 实体 - 测试 toString")
    void testSessionMove_ToString() {
        SessionMove sm = new SessionMove();
        sm.setMoveId(300L);
        sm.setDurationSeconds(45);

        String str = sm.toString();
        assertTrue(str.contains("300"));
        assertTrue(str.contains("45"));
    }
}
