package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.ResumeWorkExperience;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 简历工作经历 Mapper
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Mapper
public interface ResumeWorkExperienceMapper extends BaseMapper<ResumeWorkExperience> {

    /**
     * 批量插入工作经历
     *
     * @param experiences 工作经历列表
     */
    void insertBatch(@Param("list") List<ResumeWorkExperience> experiences);
}
