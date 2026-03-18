package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.ResumeSkill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 简历技能 Mapper
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Mapper
public interface ResumeSkillMapper extends BaseMapper<ResumeSkill> {

    /**
     * 批量插入技能
     *
     * @param skills 技能列表
     */
    void insertBatch(@Param("list") List<ResumeSkill> skills);
}
