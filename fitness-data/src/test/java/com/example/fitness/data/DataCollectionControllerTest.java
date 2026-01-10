package com.example.fitness.data;

import com.example.fitness.data.controller.DataCollectionController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DataCollectionController.class)
public class DataCollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private CircuitBreakerFactory circuitBreakerFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SuppressWarnings("unchecked")
    public void testCollect() throws Exception {
        // Mock CircuitBreaker
        CircuitBreaker cb = mock(CircuitBreaker.class);
        when(circuitBreakerFactory.create(anyString())).thenReturn(cb);
        when(cb.run(any(Supplier.class), any(Function.class))).thenAnswer(invocation -> {
            Supplier<Object> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        Map<String, Object> payload = new HashMap<>();
        payload.put("sessionId", "s1");
        payload.put("items", "mock");

        mockMvc.perform(post("/api/data/collect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
