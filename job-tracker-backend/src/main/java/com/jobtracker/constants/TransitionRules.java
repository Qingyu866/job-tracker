package com.jobtracker.constants;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 状态转换规则定义
 * <p>
 * 定义申请状态和面试状态的合法转换规则
 * 用于状态转换验证，确保业务流程正确性
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 1.0.0
 */
public final class TransitionRules {

    private TransitionRules() {}

    /**
     * 申请状态合法转换映射
     * <p>
     * Key: 当前状态
     * Value: 可转换到的状态集合
     * </p>
     */
    public static final Map<ApplicationStatus, Set<ApplicationStatus>> APPLICATION_TRANSITIONS =
        Map.ofEntries(
            // WISHLIST 可转换到
            Map.entry(ApplicationStatus.WISHLIST,
                Set.of(ApplicationStatus.APPLIED, ApplicationStatus.WITHDRAWN)),

            // APPLIED 可转换到
            Map.entry(ApplicationStatus.APPLIED,
                Set.of(ApplicationStatus.SCREENING, ApplicationStatus.INTERVIEW,
                       ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN)),

            // SCREENING 可转换到
            Map.entry(ApplicationStatus.SCREENING,
                Set.of(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED,
                       ApplicationStatus.WITHDRAWN)),

            // INTERVIEW 可转换到
            Map.entry(ApplicationStatus.INTERVIEW,
                Set.of(ApplicationStatus.FINAL_ROUND, ApplicationStatus.OFFERED,
                       ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN)),

            // FINAL_ROUND 可转换到
            Map.entry(ApplicationStatus.FINAL_ROUND,
                Set.of(ApplicationStatus.OFFERED, ApplicationStatus.REJECTED,
                       ApplicationStatus.WITHDRAWN)),

            // OFFERED 可转换到
            Map.entry(ApplicationStatus.OFFERED,
                Set.of(ApplicationStatus.ACCEPTED, ApplicationStatus.DECLINED,
                       ApplicationStatus.EXPIRED)),

            // 终态：无法转换（空集合）
            Map.entry(ApplicationStatus.ACCEPTED, Collections.emptySet()),
            Map.entry(ApplicationStatus.DECLINED, Collections.emptySet()),
            Map.entry(ApplicationStatus.EXPIRED, Collections.emptySet()),
            Map.entry(ApplicationStatus.REJECTED, Collections.emptySet()),
            Map.entry(ApplicationStatus.WITHDRAWN, Collections.emptySet())
        );

    /**
     * 面试状态合法转换映射
     * <p>
     * Key: 当前状态
     * Value: 可转换到的状态集合
     * </p>
     */
    public static final Map<InterviewStatus, Set<InterviewStatus>> INTERVIEW_TRANSITIONS =
        Map.ofEntries(
            // SCHEDULED 可转换到
            Map.entry(InterviewStatus.SCHEDULED,
                Set.of(InterviewStatus.IN_PROGRESS, InterviewStatus.COMPLETED,
                       InterviewStatus.CANCELLED, InterviewStatus.NO_SHOW)),

            // IN_PROGRESS 可转换到
            Map.entry(InterviewStatus.IN_PROGRESS,
                Set.of(InterviewStatus.COMPLETED, InterviewStatus.CANCELLED)),

            // COMPLETED 可转换到（需要评估结果）
            Map.entry(InterviewStatus.COMPLETED,
                Set.of(InterviewStatus.PASSED, InterviewStatus.FAILED)),

            // CANCELLED 可以重新安排
            Map.entry(InterviewStatus.CANCELLED,
                Set.of(InterviewStatus.SCHEDULED)),

            // NO_SHOW 可以重新安排
            Map.entry(InterviewStatus.NO_SHOW,
                Set.of(InterviewStatus.SCHEDULED)),

            // 终态：无法转换
            Map.entry(InterviewStatus.PASSED, Collections.emptySet()),
            Map.entry(InterviewStatus.FAILED, Collections.emptySet())
        );

    /**
     * 验证申请状态转换是否合法
     *
     * @param from 当前状态
     * @param to   目标状态
     * @return 如果转换合法返回 true
     */
    public static boolean canTransition(ApplicationStatus from, ApplicationStatus to) {
        if (from == null || to == null) return false;
        Set<ApplicationStatus> allowed = APPLICATION_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    /**
     * 验证面试状态转换是否合法
     *
     * @param from 当前状态
     * @param to   目标状态
     * @return 如果转换合法返回 true
     */
    public static boolean canTransition(InterviewStatus from, InterviewStatus to) {
        if (from == null || to == null) return false;
        Set<InterviewStatus> allowed = INTERVIEW_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    /**
     * 获取申请的下一个可能状态列表
     *
     * @param current 当前状态
     * @return 可转换到的状态列表
     */
    public static Set<ApplicationStatus> getNextPossibleStatuses(ApplicationStatus current) {
        if (current == null) return Collections.emptySet();
        return APPLICATION_TRANSITIONS.getOrDefault(current, Collections.emptySet());
    }

    /**
     * 获取面试的下一个可能状态列表
     *
     * @param current 当前状态
     * @return 可转换到的状态列表
     */
    public static Set<InterviewStatus> getNextPossibleStatuses(InterviewStatus current) {
        if (current == null) return Collections.emptySet();
        return INTERVIEW_TRANSITIONS.getOrDefault(current, Collections.emptySet());
    }
}
