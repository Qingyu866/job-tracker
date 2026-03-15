package com.jobtracker.service;

import com.jobtracker.dto.InterviewProgress;
import com.jobtracker.entity.InterviewRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试管理服务接口（支持多轮面试）
 * <p>
 * 提供多轮面试的管理功能，包括安排面试、完成面试、查询面试进度等
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 1.0.0
 */
public interface InterviewManagementService {

    /**
     * 安排下一轮面试
     *
     * @param applicationId 申请ID
     * @param interviewType 面试类型（PHONE/VIDEO/ONSITE/TECHNICAL/HR）
     * @param interviewDate 面试时间
     * @param interviewerName 面试官姓名（可选）
     * @param interviewerTitle 面试官职位（可选）
     * @param notes 备注（可选）
     * @return 新建的面试记录
     * @throws com.jobtracker.common.exception.BusinessException 如果无法安排面试
     */
    InterviewRecord scheduleNextRound(Long applicationId, String interviewType,
                                       LocalDateTime interviewDate, String interviewerName,
                                       String interviewerTitle, String notes);

    /**
     * 安排下一轮面试（简化版）
     *
     * @param applicationId 申请ID
     * @param interviewType 面试类型
     * @param interviewDate 面试时间
     * @return 新建的面试记录
     */
    InterviewRecord scheduleNextRound(Long applicationId, String interviewType, LocalDateTime interviewDate);

    /**
     * 获取申请的所有面试（按轮次排序）
     *
     * @param applicationId 申请ID
     * @return 面试记录列表，按轮次升序排列
     */
    List<InterviewRecord> getInterviewsByApplication(Long applicationId);

    /**
     * 获取当前进行中的面试
     *
     * @param applicationId 申请ID
     * @return 当前进行中或已安排的面试，如果没有则返回 null
     */
    InterviewRecord getCurrentInterview(Long applicationId);

    /**
     * 开始面试（将状态从 SCHEDULED 改为 IN_PROGRESS）
     *
     * @param interviewId 面试记录ID
     * @throws com.jobtracker.common.exception.BusinessException 如果状态转换不合法
     */
    void startInterview(Long interviewId);

    /**
     * 完成面试并记录结果
     * <p>
     * 该方法会：
     * 1. 更新面试状态为 COMPLETED
     * 2. 记录面试结果、评分和反馈
     * 3. 根据结果自动转换为 PASSED 或 FAILED
     * 4. 触发申请状态联动
     * </p>
     *
     * @param interviewId 面试记录ID
     * @param result      面试结果（PASSED/FAILED）
     * @param rating      面试评分（1-5分，可选）
     * @param feedback    面试反馈（可选）
     * @throws com.jobtracker.common.exception.BusinessException 如果状态转换不合法
     */
    void completeInterview(Long interviewId, String result, Integer rating, String feedback);

    /**
     * 取消面试
     *
     * @param interviewId 面试记录ID
     * @param reason      取消原因（可选）
     * @throws com.jobtracker.common.exception.BusinessException 如果状态转换不合法
     */
    void cancelInterview(Long interviewId, String reason);

    /**
     * 标记面试为未参加
     *
     * @param interviewId 面试记录ID
     * @throws com.jobtracker.common.exception.BusinessException 如果状态转换不合法
     */
    void markAsNoShow(Long interviewId);

    /**
     * 重新安排面试（针对已取消或未参加的面试）
     *
     * @param interviewId   原面试记录ID
     * @param newInterviewDate 新的面试时间
     * @return 新的面试记录
     * @throws com.jobtracker.common.exception.BusinessException 如果原面试状态不允许重新安排
     */
    InterviewRecord rescheduleInterview(Long interviewId, LocalDateTime newInterviewDate);

    /**
     * 获取面试进度
     *
     * @param applicationId 申请ID
     * @return 面试进度信息
     */
    InterviewProgress getProgress(Long applicationId);

    /**
     * 更新面试信息
     *
     * @param interviewId 面试记录ID
     * @param interviewDate 面试时间（可选，不更新传 null）
     * @param interviewerName 面试官姓名（可选，不更新传 null）
     * @param interviewerTitle 面试官职位（可选，不更新传 null）
     * @param durationMinutes 面试时长（可选，不更新传 null）
     * @param notes 备注（可选，不更新传 null）
     * @return 更新后的面试记录
     */
    InterviewRecord updateInterviewInfo(Long interviewId, LocalDateTime interviewDate,
                                         String interviewerName, String interviewerTitle,
                                         Integer durationMinutes, String notes);

    /**
     * 标记面试为终面
     *
     * @param interviewId 面试记录ID
     * @param isFinal     是否为终面
     */
    void markAsFinalInterview(Long interviewId, boolean isFinal);
}
