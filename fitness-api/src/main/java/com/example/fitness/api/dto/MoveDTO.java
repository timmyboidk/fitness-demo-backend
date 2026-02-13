package com.example.fitness.api.dto;

import lombok.Data;
import java.util.Map;

/**
 * 健身动作 DTO
 *
 * <p>
 * 对应数据库中的 {@code move} 表，用于向客户端传输动作基本信息
 * 和 AI 评分相关配置。
 */
@Data
public class MoveDTO {

    /** 动作唯一标识（如 {@code "m_squat"}） */
    private String id;

    /** 动作中文名称（如 {@code "深蹲"}） */
    private String name;

    /** ONNX AI 模型下载地址 */
    private String modelUrl;

    /** 评分配置参数（从数据库 JSON 反序列化），如 {@code {"angleThreshold": 20}} */
    private Map<String, Object> scoringConfig;
}
