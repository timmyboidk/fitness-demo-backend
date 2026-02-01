package com.example.fitness.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.fitness.content.model.entity.UserLibrary;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户训练库 Mapper 接口
 * 
 * <p>
 * 提供用户训练库数据的持久化操作，包括：
 * <ul>
 * <li>查询用户收藏的训练动作</li>
 * <li>添加/删除收藏项</li>
 * <li>批量查询用户训练库列表</li>
 * </ul>
 * 
 * <p>
 * 继承 MyBatis-Plus BaseMapper，自动获得 CRUD 基础方法。
 * 
 * @author fitness-team
 * @since 1.0.0
 * @see UserLibrary
 */
@Mapper
public interface UserLibraryMapper extends BaseMapper<UserLibrary> {
}
