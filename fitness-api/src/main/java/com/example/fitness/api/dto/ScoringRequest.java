package com.example.fitness.api.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ScoringRequest {
    private String moveId;
    private Map<String, Object> data; // e.g., { "keypoints": [...] }
}
