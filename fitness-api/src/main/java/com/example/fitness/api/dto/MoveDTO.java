package com.example.fitness.api.dto;

import lombok.Data;
import java.util.Map;

/**
 * 运动动作 DTO
 * 描述单个动作的信息，包括模型路径和评分配置。
 */
@Data
public class MoveDTO {
    private String id;
    private String name;
    private String modelUrl;
    private Map<String, Object> scoringConfig;
}
