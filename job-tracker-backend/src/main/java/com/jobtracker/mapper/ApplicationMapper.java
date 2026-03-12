package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jobtracker.entity.JobApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 求职申请数据访问层接口
 * <p>
 * 基于 MyBatis Plus 的 BaseMapper，提供对求职申请表（job_applications）的 CRUD 操作
 * 包含自定义查询方法以满足特定业务需求
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface ApplicationMapper extends BaseMapper<JobApplication> {

    /**
     * 根据公司ID查询所有申请
     *
     * @param companyId 公司ID
     * @return 申请列表
     */
    @Select("SELECT * FROM job_applications WHERE company_id = #{companyId} AND deleted = 0")
    List<JobApplication> selectByCompanyId(@Param("companyId") Long companyId);

    /**
     * 根据状态查询申请
     *
     * @param status 申请状态
     * @return 申请列表
     */
    @Select("SELECT * FROM job_applications WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<JobApplication> selectByStatus(@Param("status") String status);

    /**
     * 根据状态分页查询申请
     *
     * @param page 分页对象
     * @param status 申请状态
     * @return 分页结果
     */
    @Select("SELECT * FROM job_applications WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    IPage<JobApplication> selectPageByStatus(Page<JobApplication> page, @Param("status") String status);

    /**
     * 查询指定日期范围内的申请
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 申请列表
     */
    @Select("SELECT * FROM job_applications WHERE application_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0 ORDER BY application_date DESC")
    List<JobApplication> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据优先级查询申请
     *
     * @param priority 优先级
     * @return 申请列表
     */
    @Select("SELECT * FROM job_applications WHERE priority = #{priority} AND deleted = 0 ORDER BY created_at DESC")
    List<JobApplication> selectByPriority(@Param("priority") Integer priority);

    /**
     * 查询高优先级申请（优先级 >= 8）
     *
     * @return 高优先级申请列表
     */
    @Select("SELECT * FROM job_applications WHERE priority >= 8 AND deleted = 0 ORDER BY priority DESC, created_at DESC")
    List<JobApplication> selectHighPriorityApplications();

    /**
     * 统计各状态的申请数量
     *
     * @return 状态统计列表（每个元素包含 status 和 count）
     */
    @Select("SELECT status, COUNT(*) as count FROM job_applications WHERE deleted = 0 GROUP BY status")
    List<Object> countByStatus();

    /**
     * 根据职位名称模糊搜索
     *
     * @param keyword 关键词
     * @return 申请列表
     */
    @Select("SELECT * FROM job_applications WHERE job_title LIKE CONCAT('%', #{keyword}, '%') AND deleted = 0 ORDER BY created_at DESC")
    List<JobApplication> searchByJobTitle(@Param("keyword") String keyword);
}
