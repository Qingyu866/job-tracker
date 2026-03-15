package com.jobtracker.service;

import com.jobtracker.constants.ApplicationStatus;
import com.jobtracker.constants.InterviewStatus;

import java.util.List;

/**
 * 状态转换验证服务接口
 * <p>
 * 提供申请状态和面试状态的转换验证、执行以及联动处理功能
 * 确保状态转换符合业务规则，并自动处理相关联动逻辑
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 1.0.0
 */
public interface StatusTransitionService {

    /**
     * 验证申请状态转换是否合法
     *
     * @param from 当前状态
     * @param to   目标状态
     * @return 如果转换合法返回 true
     */
    boolean canTransition(ApplicationStatus from, ApplicationStatus to);

    /**
     * 验证面试状态转换是否合法
     *
     * @param from 当前状态
     * @param to   目标状态
     * @return 如果转换合法返回 true
     */
    boolean canTransition(InterviewStatus from, InterviewStatus to);

    /**
     * 获取申请的下一个可能状态列表
     *
     * @param current 当前状态
     * @return 可转换到的状态列表
     */
    List<ApplicationStatus> getNextPossibleStatuses(ApplicationStatus current);

    /**
     * 获取面试的下一个可能状态列表
     *
     * @param current 当前状态
     * @return 可转换到的状态列表
     */
    List<InterviewStatus> getNextPossibleInterviewStatuses(InterviewStatus current);

    /**
     * 执行申请状态转换（带验证）
     *
     * @param applicationId 申请ID
     * @param newStatus     新状态
     * @throws com.jobtracker.common.exception.BusinessException 如果转换不合法
     */
    void transitionApplicationStatus(Long applicationId, ApplicationStatus newStatus);

    /**
     * 执行面试状态转换（带联动）
     * <p>
     * 面试状态变更时会自动触发申请状态的联动更新
     * </p>
     *
     * @param interviewId 面试记录ID
     * @param newStatus   新状态
     * @throws com.jobtracker.common.exception.BusinessException 如果转换不合法
     */
    void transitionInterviewStatus(Long interviewId, InterviewStatus newStatus);

    /**
     * 当面试被安排时触发的状态联动
     * <p>
     * 如果申请状态在 INTERVIEW 阶段之前，自动更新为 INTERVIEW
     * </p>
     *
     * @param applicationId 申请ID
     */
    void onInterviewScheduled(Long applicationId);

    /**
     * 验证申请是否可以安排面试
     *
     * @param applicationId 申请ID
     * @return 如果可以安排面试返回 true
     */
    boolean canScheduleInterview(Long applicationId);

    /**
     * 获取申请无法安排面试的原因
     *
     * @param applicationId 申请ID
     * @return 原因描述，如果可以安排则返回 null
     */
    String getInterviewDisabledReason(Long applicationId);
}
