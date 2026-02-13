package com.example.fitness.api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 训练课程 DTO
 *
 * <p>
 * 描述单个训练课程的基本信息。在列表接口中 {@code moves} 为空列表，
 * 需通过详情接口获取完整的动作列表。
 */
@Data
@Builder
public class SessionDTO {

    /** 课程唯一标识 */
    private String id;

    /** 课程名称（如 {@code "晨间唤醒"}） */
    private String name;

    /** 课程难度等级（{@code novice} / {@code skilled} / {@code expert}） */
    private String difficulty;

    /** 预估训练时长（分钟） */
    private Integer duration;

    /** 课程封面图片 URL */
    private String coverUrl;

    /**
     * 课程包含的动作列表（详情页使用，列表页为空）
     */
    private List<MoveDTO> moves;
}
