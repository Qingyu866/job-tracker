package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.UserResume;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户简历 Mapper
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Mapper
public interface UserResumeMapper extends BaseMapper<UserResume> {
}
