package com.example.fitness.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.fitness.content.model.entity.SessionMove;
import org.apache.ibatis.annotations.Mapper;

/**
 * 训练课程动作关联 Mapper 接口
 * 
 * <p>
 * 提供训练课程与动作之间关联关系的持久化操作，包括：
 * <ul>
 * <li>查询某个课程包含的所有动作</li>
 * <li>批量添加/删除课程动作关联</li>
 * <li>更新动作在课程中的顺序</li>
 * </ul>
 * 
 * <p>
 * 继承 MyBatis-Plus BaseMapper，自动获得 CRUD 基础方法。
 * 
 * @author fitness-team
 * @since 1.0.0
 * @see SessionMove
 */
@Mapper
public interface SessionMoveMapper extends BaseMapper<SessionMove> {
}
