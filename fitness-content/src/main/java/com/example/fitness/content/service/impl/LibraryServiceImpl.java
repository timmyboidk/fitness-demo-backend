package com.example.fitness.content.service.impl;

import com.example.fitness.api.dto.MoveDTO;
import com.example.fitness.content.service.LibraryService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class LibraryServiceImpl implements LibraryService {

    @Override
    public Map<String, Object> getLibraryByDifficulty(String difficulty) {
        Map<String, Object> response = new HashMap<>();

        MoveDTO squat = new MoveDTO();
        squat.setId("m_squat");
        squat.setName("Squat");
        squat.setModelUrl("https://oss.fitness.com/models/squat_" + difficulty + ".onnx");

        Map<String, Object> config = new HashMap<>();
        config.put("angleThreshold", "expert".equals(difficulty) ? 5 : 20);
        config.put("holdTime", 2);
        squat.setScoringConfig(config);

        response.put("moves", Arrays.asList(squat));
        response.put("sessions", Arrays.asList());

        return response;
    }
}
