package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.InterviewMemory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 面试记忆 Mapper
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-17
 */
@Mapper
public interface InterviewMemoryMapper extends BaseMapper<InterviewMemory> {

    /**
     * 根据 session_id 删除记忆记录
     *
     * @param sessionId 模拟面试会话ID
     * @return 删除的行数
     */
    int deleteBySessionId(@Param("session_id") String sessionId);
}
