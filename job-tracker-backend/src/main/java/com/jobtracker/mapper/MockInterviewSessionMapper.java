package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.MockInterviewSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模拟面试会话 Mapper
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Mapper
public interface MockInterviewSessionMapper extends BaseMapper<MockInterviewSession> {
}
