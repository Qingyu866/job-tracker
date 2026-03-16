package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.ChatImage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天图片 Mapper
 * <p>
 * 提供 ChatImage 实体的数据库操作
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-15
 */
@Mapper
public interface ChatImageMapper extends BaseMapper<ChatImage> {
    // MyBatis Plus 提供基础 CRUD 方法，无需额外定义
}
