package com.example.fitness.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.fitness.content.model.entity.Session;
import org.apache.ibatis.annotations.Mapper;

/**
 * 训练课程 Mapper 接口
 * 
 * <p>
 * 提供训练课程（Session）数据的持久化操作，包括：
 * <ul>
 * <li>按难度等级查询课程列表</li>
 * <li>根据课程 ID 查询详情</li>
 * <li>课程的创建、更新和删除</li>
 * </ul>
 * 
 * <p>
 * 继承 MyBatis-Plus BaseMapper，自动获得 CRUD 基础方法。
 * 
 * @author fitness-team
 * @since 1.0.0
 * @see Session
 */
@Mapper
public interface SessionMapper extends BaseMapper<Session> {
}
