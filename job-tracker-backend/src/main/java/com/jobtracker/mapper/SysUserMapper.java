package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper
 *
 * @author Job Tracker Team
 * @since 2026-03-17
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
