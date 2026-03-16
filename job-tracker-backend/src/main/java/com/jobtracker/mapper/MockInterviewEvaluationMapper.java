package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.MockInterviewEvaluation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试评分记录 Mapper
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Mapper
public interface MockInterviewEvaluationMapper extends BaseMapper<MockInterviewEvaluation> {
}
