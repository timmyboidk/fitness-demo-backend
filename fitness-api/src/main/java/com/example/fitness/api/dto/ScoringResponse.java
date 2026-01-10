package com.example.fitness.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringResponse {
    private boolean success;
    private Integer score;
    private List<Object> feedback;
}
