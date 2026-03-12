package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.InterviewRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试记录数据访问层接口
 * <p>
 * 基于 MyBatis Plus 的 BaseMapper，提供对面试记录表（interview_records）的 CRUD 操作
 * 包含自定义查询方法以满足面试管理需求
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface InterviewMapper extends BaseMapper<InterviewRecord> {

    /**
     * 根据申请ID查询所有面试记录
     *
     * @param applicationId 申请ID
     * @return 面试记录列表
     */
    @Select("SELECT * FROM interview_records WHERE application_id = #{applicationId} AND deleted = 0 ORDER BY interview_date DESC")
    List<InterviewRecord> selectByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 查询指定日期范围内的面试
     *
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 面试记录列表
     */
    @Select("SELECT * FROM interview_records WHERE interview_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0 ORDER BY interview_date ASC")
    List<InterviewRecord> selectByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 查询即将进行的面试（状态为 SCHEDULED）
     *
     * @return 即将进行的面试列表
     */
    @Select("SELECT * FROM interview_records WHERE status = 'SCHEDULED' AND deleted = 0 ORDER BY interview_date ASC")
    List<InterviewRecord> selectUpcomingInterviews();

    /**
     * 查询指定状态的面试记录
     *
     * @param status 面试状态
     * @return 面试记录列表
     */
    @Select("SELECT * FROM interview_records WHERE status = #{status} AND deleted = 0 ORDER BY interview_date DESC")
    List<InterviewRecord> selectByStatus(@Param("status") String status);

    /**
     * 查询需要跟进的面试
     *
     * @return 需要跟进的面试列表
     */
    @Select("SELECT * FROM interview_records WHERE follow_up_required = 1 AND deleted = 0 ORDER BY interview_date DESC")
    List<InterviewRecord> selectFollowUpRequired();

    /**
     * 查询最近7天内的面试
     *
     * @param days 天数
     * @return 面试记录列表
     */
    @Select("SELECT * FROM interview_records WHERE interview_date >= DATE_SUB(NOW(), INTERVAL #{days} DAY) AND deleted = 0 ORDER BY interview_date DESC")
    List<InterviewRecord> selectRecentInterviews(@Param("days") int days);
}
