package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 求职申请实体类
 * <p>
 * 对应数据库表：job_applications
 * 存储求职申请的详细信息，包括职位、薪资、状态等
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@TableName("job_applications")
public class JobApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申请ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 公司ID（外键，关联 companies 表）
     */
    @TableField("company_id")
    private Long companyId;

    /**
     * 职位名称（必填）
     */
    @TableField("job_title")
    private String jobTitle;

    /**
     * 职位描述
     */
    @TableField("job_description")
    private String jobDescription;

    /**
     * 工作类型（全职/兼职/实习/合同）
     */
    @TableField("job_type")
    private String jobType;

    /**
     * 工作地点
     */
    @TableField("work_location")
    private String workLocation;

    /**
     * 薪资下限
     */
    @TableField("salary_min")
    private BigDecimal salaryMin;

    /**
     * 薪资上限
     */
    @TableField("salary_max")
    private BigDecimal salaryMax;

    /**
     * 薪资货币（默认：CNY）
     */
    @TableField("salary_currency")
    private String salaryCurrency;

    /**
     * 职位链接（如：招聘网站URL）
     */
    @TableField("job_url")
    private String jobUrl;

    /**
     * 申请状态（WISHLIST/APPLIED/INTERVIEW/OFFER/REJECTED/WITHDRAWN）
     */
    private String status;

    /**
     * 申请日期
     */
    @TableField("application_date")
    private LocalDate applicationDate;

    /**
     * 备注信息
     */
    private String notes;

    /**
     * 优先级（1-10，10最高）
     */
    private Integer priority;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记（0:未删除, 1:已删除）
     */
    @TableLogic
    private Integer deleted;

    // ==================== 模拟面试相关字段（需要执行 V9 迁移脚本后生效）====================

    /**
     * 关联的简历ID
     */
    @TableField("resume_id")
    private Long resumeId;

    /**
     * 岗位级别：JUNIOR/MIDDLE/SENIOR/LEAD
     */
    @TableField("seniority_level")
    private String seniorityLevel;

    /**
     * 简历快照（面试时的简历状态）
     */
    @TableField("resume_snapshot")
    private String resumeSnapshot;

    /**
     * 岗位技能要求（从JD解析或手动添加）
     */
    @TableField("skills_required")
    private String skillsRequired;

    /**
     * 是否已准备模拟面试
     */
    @TableField("interview_prepared")
    private Boolean interviewPrepared;

    /**
     * 模拟面试次数
     */
    @TableField("mock_interview_count")
    private Integer mockInterviewCount;

    /**
     * 最佳模拟面试成绩
     */
    @TableField("best_mock_score")
    private java.math.BigDecimal bestMockScore;
}
