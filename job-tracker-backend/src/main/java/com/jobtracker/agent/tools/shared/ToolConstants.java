package com.jobtracker.agent.tools.shared;

/**
 * 工具常量定义
 * <p>
 * 集中管理所有工具使用的常量，包括：
 * - 申请状态
 * - 面试状态和类型
 * - 工作类型
 * - 错误代码
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-14
 */
public final class ToolConstants {

    private ToolConstants() {}

    // ========== 申请状态 ==========
    public static final String STATUS_WISHLIST = "WISHLIST";
    public static final String STATUS_APPLIED = "APPLIED";
    public static final String STATUS_SCREENING = "SCREENING";       // 新增：简历筛选中
    public static final String STATUS_INTERVIEW = "INTERVIEW";
    public static final String STATUS_FINAL_ROUND = "FINAL_ROUND";   // 新增：终面中
    public static final String STATUS_OFFERED = "OFFERED";           // 改名：已收到Offer
    public static final String STATUS_ACCEPTED = "ACCEPTED";         // 新增：已接受Offer
    public static final String STATUS_DECLINED = "DECLINED";         // 新增：已拒绝Offer
    public static final String STATUS_EXPIRED = "EXPIRED";           // 新增：Offer过期
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_WITHDRAWN = "WITHDRAWN";

    // 兼容旧状态
    public static final String STATUS_OFFER = "OFFER";  // 保留兼容

    /** 申请状态中文描述 */
    public static final String STATUS_DESC_WISHLIST = "意愿清单";
    public static final String STATUS_DESC_APPLIED = "已申请";
    public static final String STATUS_DESC_SCREENING = "筛选中";
    public static final String STATUS_DESC_INTERVIEW = "面试中";
    public static final String STATUS_DESC_FINAL_ROUND = "终面中";
    public static final String STATUS_DESC_OFFERED = "已收到Offer";
    public static final String STATUS_DESC_ACCEPTED = "已接受";
    public static final String STATUS_DESC_DECLINED = "已拒绝Offer";
    public static final String STATUS_DESC_EXPIRED = "Offer过期";
    public static final String STATUS_DESC_REJECTED = "已被拒绝";
    public static final String STATUS_DESC_WITHDRAWN = "已撤回";

    // 兼容旧状态
    public static final String STATUS_DESC_OFFER = "已录用";

    // ========== 面试创建权限 ==========
    /** 允许创建面试的状态列表 */
    public static final java.util.Set<String> INTERVIEW_ALLOWED_STATUSES = java.util.Set.of(
        STATUS_SCREENING,    // 筛选中 - 可能随时被安排面试
        STATUS_INTERVIEW,    // 面试中 - 可以安排更多轮
        STATUS_FINAL_ROUND,  // 终面中 - 可以安排 HR 面等
        STATUS_OFFERED       // 已收到Offer - 可能有后续沟通
    );

    // ========== 面试状态 ==========
    public static final String INTERVIEW_SCHEDULED = "SCHEDULED";
    public static final String INTERVIEW_COMPLETED = "COMPLETED";
    public static final String INTERVIEW_CANCELLED = "CANCELLED";
    public static final String INTERVIEW_NO_SHOW = "NO_SHOW";

    /** 面试状态中文描述 */
    public static final String INTERVIEW_DESC_SCHEDULED = "已安排";
    public static final String INTERVIEW_DESC_COMPLETED = "已完成";
    public static final String INTERVIEW_DESC_CANCELLED = "已取消";
    public static final String INTERVIEW_DESC_NO_SHOW = "未参加";

    // ========== 面试类型 ==========
    public static final String INTERVIEW_TYPE_PHONE = "PHONE";
    public static final String INTERVIEW_TYPE_VIDEO = "VIDEO";
    public static final String INTERVIEW_TYPE_ONSITE = "ONSITE";
    public static final String INTERVIEW_TYPE_TECHNICAL = "TECHNICAL";
    public static final String INTERVIEW_TYPE_HR = "HR";

    /** 面试类型中文描述 */
    public static final String INTERVIEW_TYPE_DESC_PHONE = "电话面试";
    public static final String INTERVIEW_TYPE_DESC_VIDEO = "视频面试";
    public static final String INTERVIEW_TYPE_DESC_ONSITE = "现场面试";
    public static final String INTERVIEW_TYPE_DESC_TECHNICAL = "技术面试";
    public static final String INTERVIEW_TYPE_DESC_HR = "HR面试";

    // ========== 工作类型 ==========
    public static final String JOB_TYPE_FULL_TIME = "FULL_TIME";
    public static final String JOB_TYPE_PART_TIME = "PART_TIME";
    public static final String JOB_TYPE_CONTRACT = "CONTRACT";
    public static final String JOB_TYPE_INTERNSHIP = "INTERNSHIP";

    /** 工作类型中文描述 */
    public static final String JOB_TYPE_DESC_FULL_TIME = "全职";
    public static final String JOB_TYPE_DESC_PART_TIME = "兼职";
    public static final String JOB_TYPE_DESC_CONTRACT = "合同工";
    public static final String JOB_TYPE_DESC_INTERNSHIP = "实习";

    // ========== 错误代码 ==========
    public static final String ERR_PARAM_MISSING = "PARAM_MISSING";
    public static final String ERR_NOT_FOUND = "NOT_FOUND";
    public static final String ERR_CREATE_FAILED = "CREATE_FAILED";
    public static final String ERR_UPDATE_FAILED = "UPDATE_FAILED";
    public static final String ERR_DELETE_FAILED = "DELETE_FAILED";
    public static final String ERR_INVALID_FORMAT = "INVALID_FORMAT";
    public static final String ERR_PROTECTED = "PROTECTED";
    public static final String ERR_MULTIPLE_MATCH = "MULTIPLE_MATCH";

    // ========== 默认值 ==========
    public static final int DEFAULT_PRIORITY = 5;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 50;

    // ========== 工具描述模板 ==========
    public static final String DESC_TEMPLATE_QUERY = "[查询] %s";
    public static final String DESC_TEMPLATE_CREATE = "[创建] %s";
    public static final String DESC_TEMPLATE_UPDATE = "[更新] %s";
    public static final String DESC_TEMPLATE_DELETE = "[删除] %s";

    /**
     * 获取申请状态的中文描述
     */
    public static String getStatusDescription(String status) {
        if (status == null) return "未知";
        return switch (status) {
            case STATUS_WISHLIST -> STATUS_DESC_WISHLIST;
            case STATUS_APPLIED -> STATUS_DESC_APPLIED;
            case STATUS_SCREENING -> STATUS_DESC_SCREENING;
            case STATUS_INTERVIEW -> STATUS_DESC_INTERVIEW;
            case STATUS_FINAL_ROUND -> STATUS_DESC_FINAL_ROUND;
            case STATUS_OFFERED, STATUS_OFFER -> STATUS_DESC_OFFERED;
            case STATUS_ACCEPTED -> STATUS_DESC_ACCEPTED;
            case STATUS_DECLINED -> STATUS_DESC_DECLINED;
            case STATUS_EXPIRED -> STATUS_DESC_EXPIRED;
            case STATUS_REJECTED -> STATUS_DESC_REJECTED;
            case STATUS_WITHDRAWN -> STATUS_DESC_WITHDRAWN;
            default -> status;
        };
    }

    /**
     * 判断状态是否允许创建面试
     */
    public static boolean canScheduleInterview(String status) {
        return INTERVIEW_ALLOWED_STATUSES.contains(status);
    }

    /**
     * 获取不允许创建面试的原因
     */
    public static String getInterviewDisabledReason(String status) {
        if (canScheduleInterview(status)) {
            return null;
        }
        return switch (status) {
            case STATUS_WISHLIST -> "尚未投递申请，无法安排面试。请先投递申请。";
            case STATUS_APPLIED -> "申请刚提交，请等待 HR 筛选后再安排面试。";
            case STATUS_ACCEPTED -> "已接受 Offer，无需再安排面试。";
            case STATUS_DECLINED -> "已拒绝 Offer，无法安排面试。";
            case STATUS_EXPIRED -> "Offer 已过期，无法安排面试。";
            case STATUS_REJECTED -> "申请已被拒绝，无法安排面试。";
            case STATUS_WITHDRAWN -> "已撤回申请，无法安排面试。";
            default -> "当前状态（" + getStatusDescription(status) + "）无法安排面试。";
        };
    }

    /**
     * 获取面试状态的中文描述
     */
    public static String getInterviewStatusDescription(String status) {
        if (status == null) return "未知";
        return switch (status) {
            case INTERVIEW_SCHEDULED -> INTERVIEW_DESC_SCHEDULED;
            case INTERVIEW_COMPLETED -> INTERVIEW_DESC_COMPLETED;
            case INTERVIEW_CANCELLED -> INTERVIEW_DESC_CANCELLED;
            case INTERVIEW_NO_SHOW -> INTERVIEW_DESC_NO_SHOW;
            default -> status;
        };
    }

    /**
     * 获取面试类型的中文描述
     */
    public static String getInterviewTypeDescription(String type) {
        if (type == null) return "未知";
        return switch (type) {
            case INTERVIEW_TYPE_PHONE -> INTERVIEW_TYPE_DESC_PHONE;
            case INTERVIEW_TYPE_VIDEO -> INTERVIEW_TYPE_DESC_VIDEO;
            case INTERVIEW_TYPE_ONSITE -> INTERVIEW_TYPE_DESC_ONSITE;
            case INTERVIEW_TYPE_TECHNICAL -> INTERVIEW_TYPE_DESC_TECHNICAL;
            case INTERVIEW_TYPE_HR -> INTERVIEW_TYPE_DESC_HR;
            default -> type;
        };
    }
}
