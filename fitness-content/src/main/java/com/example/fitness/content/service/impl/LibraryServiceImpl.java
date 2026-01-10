package com.example.fitness.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.fitness.api.dto.LibraryResponse;
import com.example.fitness.api.dto.MoveDTO;
import com.example.fitness.content.mapper.MoveMapper;
import com.example.fitness.content.mapper.UserLibraryMapper;
import com.example.fitness.content.model.entity.Move;
import com.example.fitness.content.model.entity.UserLibrary;
import com.example.fitness.content.service.LibraryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibraryServiceImpl implements LibraryService {

    private final MoveMapper moveMapper;
    private final UserLibraryMapper userLibraryMapper;

    @Override
    public LibraryResponse getLibrary(String difficulty) {
        // Query DB
        List<Move> moves = moveMapper.selectList(new LambdaQueryWrapper<Move>()
                .eq(Move::getDifficulty, difficulty));

        // Map Entity to DTO
        List<MoveDTO> moveDTOs = moves.stream().map(this::convertToDTO).collect(Collectors.toList());

        LibraryResponse response = new LibraryResponse();
        response.setMoves(moveDTOs);
        response.setSessions(Collections.emptyList()); // Mock sessions for now
        return response;
    }

    private MoveDTO convertToDTO(Move move) {
        MoveDTO dto = new MoveDTO();
        dto.setId(move.getId());
        dto.setName(move.getName());
        dto.setModelUrl(move.getModelUrl());
        // Simple manual JSON parsing or use ObjectMapper (injected)
        // For simplicity/robustness in demo, we can just hardcode or parse simply if
        // needed.
        // Assuming scoringConfigJson is valid JSON.
        // Using a simple Map for config in DTO?
        // MoveDTO definition: private Map<String, Object> scoringConfig;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> config = new ObjectMapper().readValue(move.getScoringConfigJson(), Map.class);
            dto.setScoringConfig(config);
        } catch (Exception e) {
            dto.setScoringConfig(Collections.emptyMap());
        }
        return dto;
    }

    @Override
    public void addItemToLibrary(Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) request.get("payload");
        UserLibrary userLibrary = new UserLibrary();
        userLibrary.setUserId(Long.valueOf(String.valueOf(payload.get("userId"))));
        userLibrary.setItemId((String) payload.get("itemId"));
        userLibrary.setItemType((String) payload.get("itemType"));
        userLibraryMapper.insert(userLibrary);
    }
}
