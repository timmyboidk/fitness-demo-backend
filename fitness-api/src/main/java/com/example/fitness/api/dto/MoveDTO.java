package com.example.fitness.api.dto;

import lombok.Data;
import java.util.Map;

@Data
public class MoveDTO {
    private String id;
    private String name;
    private String modelUrl;
    private Map<String, Object> scoringConfig;
}
