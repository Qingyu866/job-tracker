package com.jobtracker.constants;

import java.util.Collections;
import java.util.Set;

/**
 * 申请状态枚举（扩展版）
 * <p>
 * 定义求职申请的所有可能状态，用于追踪申请进度
 * 支持状态阶段分组和状态转换验证
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 1.0.0
 */
public enum ApplicationStatus {

    // ========== 初始阶段 ==========
    /**
     * 意愿清单 - 尚未申请，仅在关注中
     */
    WISHLIST("WISHLIST", "意愿清单", Stage.INITIAL),

    // ========== 流程阶段 ==========
    /**
     * 已申请 - 已提交申请，等待回复
     */
    APPLIED("APPLIED", "已申请", Stage.APPLIED),

    /**
     * 简历筛选中 - HR正在筛选简历（新增）
     */
    SCREENING("SCREENING", "简历筛选中", Stage.IN_PROGRESS),

    // ========== 面试阶段 ==========
    /**
     * 面试中 - 正在进行面试流程
     */
    INTERVIEW("INTERVIEW", "面试中", Stage.IN_PROGRESS),

    /**
     * 终面中 - 已进入最后一轮面试（新增）
     */
    FINAL_ROUND("FINAL_ROUND", "终面中", Stage.IN_PROGRESS),

    // ========== Offer 阶段 ==========
    /**
     * 已收到Offer - 收到录用通知
     */
    OFFERED("OFFERED", "已收到Offer", Stage.OFFER),

    // ========== 终态 ==========
    /**
     * 已接受Offer - 已接受录用通知（新增）
     */
    ACCEPTED("ACCEPTED", "已接受Offer", Stage.COMPLETED),

    /**
     * 已拒绝Offer - 主动拒绝了Offer（新增）
     */
    DECLINED("DECLINED", "已拒绝Offer", Stage.COMPLETED),

    /**
     * Offer已过期 - Offer超时未处理（新增）
     */
    EXPIRED("EXPIRED", "Offer已过期", Stage.COMPLETED),

    /**
     * 已被拒绝 - 申请被公司拒绝
     */
    REJECTED("REJECTED", "已被拒绝", Stage.COMPLETED),

    /**
     * 已撤回 - 主动撤回申请
     */
    WITHDRAWN("WITHDRAWN", "已撤回", Stage.COMPLETED);

    // 兼容旧状态代码
    private static final String LEGACY_OFFER = "OFFER";

    private final String code;
    private final String description;
    private final Stage stage;

    ApplicationStatus(String code, String description, Stage stage) {
        this.code = code;
        this.description = description;
        this.stage = stage;
    }

    /**
     * 状态阶段枚举
     */
    public enum Stage {
        /** 初始阶段 */
        INITIAL,
        /** 已申请 */
        APPLIED,
        /** 进行中（筛选/面试） */
        IN_PROGRESS,
        /** Offer阶段 */
        OFFER,
        /** 已完成（终态） */
        COMPLETED
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
     * 获取状态阶段
     *
     * @return 状态阶段
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * 是否为终态
     *
     * @return 如果是终态返回 true
     */
    public boolean isTerminal() {
        return stage == Stage.COMPLETED;
    }

    /**
     * 是否允许创建面试
     * 只有在"已进入流程"的状态才能安排面试
     *
     * @return 如果允许创建面试返回 true
     */
    public boolean canScheduleInterview() {
        return this == SCREENING
            || this == INTERVIEW
            || this == FINAL_ROUND
            || this == OFFERED;
    }

    /**
     * 获取不允许创建面试的原因
     *
     * @return 原因描述，如果允许创建则返回 null
     */
    public String getInterviewDisabledReason() {
        if (canScheduleInterview()) {
            return null;
        }
        return switch (this) {
            case WISHLIST -> "尚未投递申请，无法安排面试。请先投递申请。";
            case APPLIED -> "申请刚提交，请等待 HR 筛选后再安排面试。";
            case ACCEPTED -> "已接受 Offer，无需再安排面试。";
            case DECLINED -> "已拒绝 Offer，无法安排面试。";
            case EXPIRED -> "Offer 已过期，无法安排面试。";
            case REJECTED -> "申请已被拒绝，无法安排面试。";
            case WITHDRAWN -> "已撤回申请，无法安排面试。";
            default -> "当前状态（" + description + "）无法安排面试。";
        };
    }

    /**
     * 是否可以转换到目标状态
     *
     * @param target 目标状态
     * @return 如果转换合法返回 true
     */
    public boolean canTransitionTo(ApplicationStatus target) {
        Set<ApplicationStatus> allowed = TransitionRules.APPLICATION_TRANSITIONS.get(this);
        return allowed != null && allowed.contains(target);
    }

    /**
     * 判断当前状态是否在指定状态之前（按流程顺序）
     *
     * @param other 另一个状态
     * @return 如果当前状态在 other 之前返回 true
     */
    public boolean isBefore(ApplicationStatus other) {
        return this.stage.ordinal() < other.stage.ordinal();
    }

    /**
     * 根据代码获取枚举值
     *
     * @param code 状态代码
     * @return 对应的枚举值，如果不存在则返回 null
     */
    public static ApplicationStatus fromCode(String code) {
        if (code == null) return null;

        // 兼容旧的 OFFER 状态
        if (LEGACY_OFFER.equals(code)) {
            return OFFERED;
        }

        for (ApplicationStatus status : ApplicationStatus.values()) {
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
