package com.example.fitness.ai;

import com.example.fitness.ai.service.impl.UserScoringServiceImpl;
import com.example.fitness.api.dto.ScoringRequest;
import com.example.fitness.api.dto.ScoringResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

public class UserScoringServiceTest {

    @Test
    public void testCalculateScore() {
        KafkaTemplate<String, Object> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        Mockito.when(kafkaTemplate.send(anyString(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        UserScoringServiceImpl service = new UserScoringServiceImpl(kafkaTemplate);

        ScoringRequest req = new ScoringRequest();
        req.setMoveId("move_1");

        ScoringResponse response = service.calculateScore(req);
        Assertions.assertTrue(response.isSuccess());

        // Verify Kafka send was called
        Mockito.verify(kafkaTemplate).send(eq("frontend_event_stream"), eq("move_1"), eq(req));
    }
}
