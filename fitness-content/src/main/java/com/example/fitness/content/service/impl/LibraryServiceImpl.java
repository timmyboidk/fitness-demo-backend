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

/**
 * 健身内容库服务实现类
 * 负责健身动作在数据库中的查询、转换以及用户个人库的管理。
 */
@Service
@RequiredArgsConstructor
public class LibraryServiceImpl implements LibraryService {

    private final MoveMapper moveMapper;
    private final UserLibraryMapper userLibraryMapper;
    private final com.example.fitness.content.mapper.SessionMapper sessionMapper;

    /**
     * 根据难度等级获取动作库
     */
    @Override
    public LibraryResponse getLibrary(String difficulty) {
        // 1. 查询动作 (Moves)
        List<Move> moves = moveMapper.selectList(new LambdaQueryWrapper<Move>()
                .eq(Move::getDifficulty, difficulty));
        List<MoveDTO> moveDTOs = moves.stream().map(this::convertToDTO).collect(Collectors.toList());

        // 2. 查询课程 (Sessions)
        List<com.example.fitness.content.model.entity.Session> sessions = sessionMapper.selectList(
                new LambdaQueryWrapper<com.example.fitness.content.model.entity.Session>()
                        .eq(com.example.fitness.content.model.entity.Session::getDifficulty, difficulty));

        List<com.example.fitness.api.dto.SessionDTO> sessionDTOs = sessions.stream()
                .map(this::convertToSessionDTO)
                .collect(Collectors.toList());

        LibraryResponse response = new LibraryResponse();
        response.setMoves(moveDTOs);
        response.setSessions(sessionDTOs);
        return response;
    }

    /**
     * 将 Session 实体转换为 SessionDTO
     *
     * <p>
     * 列表接口中 {@code moves} 字段返回空列表，需通过详情接口单独填充。
     *
     * @param session 数据库 Session 实体
     * @return 前端可用的 SessionDTO
     */
    private com.example.fitness.api.dto.SessionDTO convertToSessionDTO(
            com.example.fitness.content.model.entity.Session session) {
        return com.example.fitness.api.dto.SessionDTO.builder()
                .id(String.valueOf(session.getId()))
                .name(session.getName())
                .difficulty(session.getDifficulty())
                .duration(session.getDuration())
                .coverUrl(session.getCoverUrl())
                // 列表接口暂不填充具体的动作列表，需单独调用详情接口
                .moves(Collections.emptyList())
                .build();
    }

    /**
     * 将 Move 实体转换为 MoveDTO
     *
     * <p>
     * 包含 {@code scoringConfigJson} 的 JSON 反序列化处理，
     * 解析失败时降级返回空 Map，不阻断主流程。
     *
     * @param move 数据库 Move 实体
     * @return 前端可用的 MoveDTO
     */
    private MoveDTO convertToDTO(Move move) {
        MoveDTO dto = new MoveDTO();
        dto.setId(move.getId());
        dto.setName(move.getName());
        dto.setModelUrl(move.getModelUrl());
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
