package com.jobtracker.constants;

/**
 * 面试状态枚举
 * <p>
 * 定义面试记录的所有可能状态，支持面试流程的完整追踪
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 1.0.0
 */
public enum InterviewStatus {

    /**
     * 已安排 - 面试已预约
     */
    SCHEDULED("SCHEDULED", "已安排"),

    /**
     * 进行中 - 面试正在进行（新增）
     */
    IN_PROGRESS("IN_PROGRESS", "进行中"),

    /**
     * 已完成 - 面试结束，待评估
     */
    COMPLETED("COMPLETED", "已完成"),

    /**
     * 已通过 - 面试评估通过（新增）
     */
    PASSED("PASSED", "已通过"),

    /**
     * 未通过 - 面试评估未通过（新增）
     */
    FAILED("FAILED", "未通过"),

    /**
     * 已取消 - 面试被取消
     */
    CANCELLED("CANCELLED", "已取消"),

    /**
     * 未参加 - 候选人未出席
     */
    NO_SHOW("NO_SHOW", "未参加");

    private final String code;
    private final String description;

    InterviewStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取状态代码
     *
     * @return 状态代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 是否为终态
     * 终态包括：PASSED, FAILED, CANCELLED, NO_SHOW
     *
     * @return 如果是终态返回 true
     */
    public boolean isTerminal() {
        return this == PASSED
            || this == FAILED
            || this == CANCELLED
            || this == NO_SHOW;
    }

    /**
     * 是否为成功状态
     *
     * @return 如果是通过返回 true
     */
    public boolean isSuccess() {
        return this == PASSED;
    }

    /**
     * 是否为失败状态
     *
     * @return 如果是失败或未参加返回 true
     */
    public boolean isFailure() {
        return this == FAILED || this == NO_SHOW;
    }

    /**
     * 是否可以转换到目标状态
     *
     * @param target 目标状态
     * @return 如果转换合法返回 true
     */
    public boolean canTransitionTo(InterviewStatus target) {
        java.util.Set<InterviewStatus> allowed = TransitionRules.INTERVIEW_TRANSITIONS.get(this);
        return allowed != null && allowed.contains(target);
    }

    /**
     * 根据代码获取枚举值
     *
     * @param code 状态代码
     * @return 对应的枚举值，如果不存在则返回 null
     */
    public static InterviewStatus fromCode(String code) {
        if (code == null) return null;
        for (InterviewStatus status : InterviewStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 验证状态代码是否有效
     *
     * @param code 状态代码
     * @return 如果有效返回 true，否则返回 false
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}
