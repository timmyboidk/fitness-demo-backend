package com.example.fitness.content.model.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;

class ContentPojoTest {

    @Test
    void testMove() {
        Move m1 = new Move();
        m1.setId("m1");
        m1.setName("pushup");
        m1.setDifficulty("novice");
        m1.setModelUrl("url");
        m1.setScoringConfigJson("{}");
        m1.setCreatedAt(LocalDateTime.now());

        assertThat(m1.getId()).isEqualTo("m1");
        assertThat(m1.getName()).isEqualTo("pushup");
        assertThat(m1.getDifficulty()).isEqualTo("novice");
        assertThat(m1.getModelUrl()).isEqualTo("url");
        assertThat(m1.getScoringConfigJson()).isEqualTo("{}");

        Move m2 = new Move();
        m2.setId("m1");
        m2.setName("pushup");
        m2.setDifficulty("novice");
        m2.setModelUrl("url");
        m2.setScoringConfigJson("{}");
        m2.setCreatedAt(m1.getCreatedAt());

        assertThat(m1).isEqualTo(m2);
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        assertThat(m1.toString()).contains("pushup");
    }

    @Test
    void testSession() {
        Session s = new Session();
        s.setId(1L);
        s.setName("Full Body");
        s.setDifficulty("expert");
        s.setDuration(30);
        s.setCoverUrl("img");

        assertThat(s.getId()).isEqualTo(1L);
        assertThat(s.getName()).isEqualTo("Full Body");
        assertThat(s.getDifficulty()).isEqualTo("expert");
        assertThat(s.getDuration()).isEqualTo(30);
        assertThat(s.getCoverUrl()).isEqualTo("img");

        assertThat(s.toString()).contains("Full Body");
    }

    @Test
    void testSessionMove() {
        SessionMove sm = new SessionMove();
        sm.setId(1L);
        sm.setSessionId(10L);
        sm.setMoveId(20L);
        sm.setSortOrder(1);

        assertThat(sm.getId()).isEqualTo(1L);
        assertThat(sm.getSessionId()).isEqualTo(10L);
        assertThat(sm.getMoveId()).isEqualTo(20L);
        assertThat(sm.getSortOrder()).isEqualTo(1);
    }
}
