package com.example.fitness.api.dto;

import lombok.Data;
import java.util.List;

/**
 * 内容库响应 DTO
 *
 * <p>
 * 作为 {@code GET /api/library} 接口的响应数据载荷，
 * 包含按难度等级过滤后的动作列表和训练课程列表。
 */
@Data
public class LibraryResponse {

    /** 健身动作列表 */
    private List<MoveDTO> moves;

    /** 训练课程列表 */
    private List<SessionDTO> sessions;
}
