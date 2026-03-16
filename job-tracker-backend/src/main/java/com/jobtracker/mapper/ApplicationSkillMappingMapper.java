package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.ApplicationSkillMapping;
import org.apache.ibatis.annotations.Mapper;

/**
 * 申请-技能关联 Mapper
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Mapper
public interface ApplicationSkillMappingMapper extends BaseMapper<ApplicationSkillMapping> {
}
