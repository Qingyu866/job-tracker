package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.ResumeProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 简历项目经历 Mapper
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Mapper
public interface ResumeProjectMapper extends BaseMapper<ResumeProject> {

    /**
     * 批量插入项目经历
     *
     * @param projects 项目经历列表
     */
    void insertBatch(@Param("list") List<ResumeProject> projects);
}
