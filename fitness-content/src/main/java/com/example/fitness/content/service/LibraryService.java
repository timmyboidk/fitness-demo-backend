package com.example.fitness.content.service;

import com.example.fitness.api.dto.LibraryResponse;
import java.util.Map;

/**
 * 健身内容库服务接口
 * 定义了获取动作库和添加项目到个人库的操作。
 */
public interface LibraryService {
    /**
     * 根据难度等级获取动作库列表
     * 
     * @param difficultyLevel 难度等级 (novice, skilled, expert)
     * @return 包含动作列表的响应 DTO
     */
    LibraryResponse getLibrary(String difficultyLevel);

    /**
     * 将项目（动作或课程）添加到用户的个人内容库
     * 
     * @param request 包含用户 ID 和项目 ID 的请求负载
     */
    void addItemToLibrary(Map<String, Object> request);
}
