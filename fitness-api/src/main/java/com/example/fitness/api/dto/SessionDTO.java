package com.example.fitness.api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 训练课程 DTO
 */
@Data
@Builder
public class SessionDTO {
    private String id;
    private String name;
    private String difficulty;
    private Integer duration;
    private String coverUrl;

    /**
     * 课程包含的动作列表（详情页使用）
     */
    private List<MoveDTO> moves;
}
