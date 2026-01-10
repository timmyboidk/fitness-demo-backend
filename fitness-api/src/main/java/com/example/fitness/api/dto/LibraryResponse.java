package com.example.fitness.api.dto;

import lombok.Data;
import java.util.List;

/**
 * 动作库响应 DTO
 * 包含动作列表和训练会话列表。
 */
@Data
public class LibraryResponse {
    private List<MoveDTO> moves;
    private List<SessionDTO> sessions;
}
