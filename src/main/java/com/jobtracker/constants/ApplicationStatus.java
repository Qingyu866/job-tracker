package com.jobtracker.constants;

/**
 * 申请状态枚举
 * <p>
 * 定义求职申请的所有可能状态，用于追踪申请进度
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
public enum ApplicationStatus {

    /**
     * 意愿清单 - 尚未申请，仅在关注中
     */
    WISHLIST("WISHLIST", "意愿清单"),

    /**
     * 已申请 - 已提交申请，等待回复
     */
    APPLIED("APPLIED", "已申请"),

    /**
     * 面试中 - 已安排或正在进行面试
     */
    INTERVIEW("INTERVIEW", "面试中"),

    /**
     * 已录用 - 收到录用通知
     */
    OFFER("OFFER", "已录用"),

    /**
     * 已拒绝 - 申请被拒绝
     */
    REJECTED("REJECTED", "已拒绝"),

    /**
     * 已撤回 - 主动撤回申请
     */
    WITHDRAWN("WITHDRAWN", "已撤回");

    private final String code;
    private final String description;

    ApplicationStatus(String code, String description) {
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
     * 根据代码获取枚举值
     *
     * @param code 状态代码
     * @return 对应的枚举值，如果不存在则返回 null
     */
    public static ApplicationStatus fromCode(String code) {
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
