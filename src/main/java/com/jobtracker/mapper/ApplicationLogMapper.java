package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.ApplicationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 申请日志数据访问层接口
 * <p>
 * 基于 MyBatis Plus 的 BaseMapper，提供对申请日志表（application_logs）的 CRUD 操作
 * 包含自定义查询方法以满足日志追踪需求
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface ApplicationLogMapper extends BaseMapper<ApplicationLog> {

    /**
     * 根据申请ID查询所有日志
     *
     * @param applicationId 申请ID
     * @return 日志列表
     */
    @Select("SELECT * FROM application_logs WHERE application_id = #{applicationId} AND deleted = 0 ORDER BY created_at DESC")
    List<ApplicationLog> selectByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 根据日志类型查询
     *
     * @param logType 日志类型
     * @return 日志列表
     */
    @Select("SELECT * FROM application_logs WHERE log_type = #{logType} AND deleted = 0 ORDER BY created_at DESC")
    List<ApplicationLog> selectByLogType(@Param("logType") String logType);

    /**
     * 根据申请ID和日志类型查询
     *
     * @param applicationId 申请ID
     * @param logType 日志类型
     * @return 日志列表
     */
    @Select("SELECT * FROM application_logs WHERE application_id = #{applicationId} AND log_type = #{logType} AND deleted = 0 ORDER BY created_at DESC")
    List<ApplicationLog> selectByApplicationIdAndType(@Param("applicationId") Long applicationId, @Param("logType") String logType);

    /**
     * 查询最近的日志记录
     *
     * @param limit 限制数量
     * @return 最近日志列表
     */
    @Select("SELECT * FROM application_logs WHERE deleted = 0 ORDER BY created_at DESC LIMIT #{limit}")
    List<ApplicationLog> selectRecentLogs(@Param("limit") int limit);

    /**
     * 根据记录者查询日志
     *
     * @param loggedBy 记录者（SYSTEM/USER）
     * @return 日志列表
     */
    @Select("SELECT * FROM application_logs WHERE logged_by = #{loggedBy} AND deleted = 0 ORDER BY created_at DESC")
    List<ApplicationLog> selectByLoggedBy(@Param("loggedBy") String loggedBy);
}
