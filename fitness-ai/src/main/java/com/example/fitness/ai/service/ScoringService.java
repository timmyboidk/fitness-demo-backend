package com.example.fitness.ai.service;

import com.example.fitness.api.dto.ScoringRequest;
import com.example.fitness.api.dto.ScoringResponse;

public interface ScoringService {
    ScoringResponse calculateScore(ScoringRequest request);
}
