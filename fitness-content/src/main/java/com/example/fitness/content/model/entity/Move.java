package com.example.fitness.content.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 健身动作实体类
 *
 * <p>
 * 对应数据库 {@code move} 表。每条记录代表一个可训练的健身动作，
 * 包含动作名称、适用难度、AI 模型地址和评分配置等信息。
 */
@Data
@TableName("move")
public class Move {

    /**
     * 动作唯一标识（如 {@code "m_squat"}）
     * <p>
     * 使用手动输入（{@link IdType#INPUT}）而非自增
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 动作名称（如 {@code "深蹲"}） */
    private String name;

    /** 难度等级（{@code novice} / {@code skilled} / {@code expert}） */
    private String difficulty;

    /** ONNX AI 模型下载地址 */
    private String modelUrl;

    /** 评分配置 JSON 字符串（查询时由服务层反序列化为 {@code Map}） */
    private String scoringConfigJson;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
