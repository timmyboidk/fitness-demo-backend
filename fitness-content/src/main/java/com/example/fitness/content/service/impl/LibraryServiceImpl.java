package com.example.fitness.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.fitness.api.dto.LibraryResponse;
import com.example.fitness.api.dto.MoveDTO;
import com.example.fitness.content.mapper.MoveMapper;
import com.example.fitness.content.mapper.UserLibraryMapper;
import com.example.fitness.content.model.entity.Move;
import com.example.fitness.content.model.entity.UserLibrary;
import com.example.fitness.content.service.LibraryService;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.result.ErrorCode;
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

    /**
     * 根据难度等级获取动作库
     */
    @Override
    public LibraryResponse getLibrary(String difficulty) {
        // 从数据库查询
        List<Move> moves = moveMapper.selectList(new LambdaQueryWrapper<Move>()
                .eq(Move::getDifficulty, difficulty));

        // 将实体类转换为 DTO
        List<MoveDTO> moveDTOs = moves.stream().map(this::convertToDTO).collect(Collectors.toList());

        LibraryResponse response = new LibraryResponse();
        response.setMoves(moveDTOs);
        response.setSessions(Collections.emptyList()); // 目前 mock 课程数据
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

    /**
     * 收藏动作到个人动作库
     */
    @Override
    public void addItemToLibrary(Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) request.get("payload");
        if (payload == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Object userIdObj = payload.get("userId");
        Object itemIdObj = payload.get("itemId");

        if (userIdObj == null || itemIdObj == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        UserLibrary userLibrary = new UserLibrary();
        userLibrary.setUserId(Long.valueOf(String.valueOf(userIdObj)));
        userLibrary.setItemId(String.valueOf(itemIdObj));
        userLibrary.setItemType((String) payload.getOrDefault("itemType", "move"));
        userLibraryMapper.insert(userLibrary);
    }
}
