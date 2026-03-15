package com.jobtracker.agent.tools.shared;

import com.jobtracker.entity.Company;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.service.ApplicationService;
import com.jobtracker.service.CompanyService;
import com.jobtracker.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 工具辅助类 - 提供智能匹配逻辑
 * <p>
 * 核心功能：
 * 1. 根据关键字智能匹配申请/面试/公司
 * 2. 处理单匹配/多匹配/无匹配场景
 * 3. 提供格式化输出
 * </p>
 *
 * <h3>智能匹配策略</h3>
 * <pre>
 * 用户输入: "字节跳动的申请"
 *      ↓
 * 匹配流程:
 *   1. 精确匹配公司名 → 找到公司 → 查询该公司的所有申请
 *   2. 如果1失败，模糊匹配公司名 → 找到多个公司 → 合并所有申请
 *   3. 如果2失败，搜索职位名
 *   4. 如果3失败，尝试组合搜索（公司-职位）
 *      ↓
 * 结果处理:
 *   - 唯一匹配: 返回单个对象
 *   - 多个匹配: 返回列表，标记需要用户选择
 *   - 无匹配: 返回错误
 * </pre>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolHelper {

    private final ApplicationService applicationService;
    private final CompanyService companyService;
    private final InterviewService interviewService;

    // ========== 申请匹配 ==========

    /**
     * 智能匹配申请
     * <p>
     * 搜索策略（按优先级）：
     * 1. 精确匹配公司名
     * 2. 模糊匹配公司名
     * 3. 匹配职位名
     * 4. 组合匹配（公司-职位，如 "字节-前端"）
     * </p>
     *
     * @param keyword 关键词（公司名、职位名、或组合）
     * @return 匹配结果
     */
    public ToolResult smartMatchApplication(String keyword) {
        log.debug("智能匹配申请: keyword={}", keyword);

        if (keyword == null || keyword.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供搜索关键词");
        }

        List<JobApplication> matches = new ArrayList<>();
        String normalizedKeyword = keyword.trim();

        // 1. 尝试精确匹配公司名
        Company company = companyService.getByName(normalizedKeyword);
        if (company != null) {
            List<JobApplication> apps = applicationService.listByCompanyId(company.getId());
            if (!apps.isEmpty()) {
                matches.addAll(apps);
                log.debug("精确匹配公司名: {} -> {} 个申请", normalizedKeyword, apps.size());
            }
        }

        // 2. 模糊搜索公司名
        if (matches.isEmpty()) {
            List<Company> companies = companyService.searchByName(normalizedKeyword);
            for (Company c : companies) {
                matches.addAll(applicationService.listByCompanyId(c.getId()));
            }
            if (!matches.isEmpty()) {
                log.debug("模糊匹配公司名: {} -> {} 个申请", normalizedKeyword, matches.size());
            }
        }

        // 3. 搜索职位名
        if (matches.isEmpty()) {
            List<JobApplication> apps = applicationService.searchByJobTitle(normalizedKeyword);
            if (!apps.isEmpty()) {
                matches.addAll(apps);
                log.debug("职位名匹配: {} -> {} 个申请", normalizedKeyword, apps.size());
            }
        }

        // 4. 组合搜索（如 "字节-前端"、"阿里 前端"）
        if (matches.isEmpty()) {
            matches.addAll(searchByCombinedKeyword(normalizedKeyword));
        }

        // 处理结果
        return handleMatchResult(matches, "申请", normalizedKeyword);
    }

    /**
     * 组合搜索（公司 + 职位）
     */
    private List<JobApplication> searchByCombinedKeyword(String keyword) {
        List<JobApplication> results = new ArrayList<>();

        // 尝试 "-" 分隔
        if (keyword.contains("-")) {
            String[] parts = keyword.split("-", 2);
            results.addAll(searchByCompanyAndJob(parts[0].trim(), parts[1].trim()));
        }

        // 尝试空格分隔
        if (results.isEmpty() && keyword.contains(" ")) {
            String[] parts = keyword.split("\\s+", 2);
            results.addAll(searchByCompanyAndJob(parts[0].trim(), parts[1].trim()));
        }

        return results;
    }

    /**
     * 根据公司和职位组合搜索
     */
    private List<JobApplication> searchByCompanyAndJob(String companyKeyword, String jobKeyword) {
        List<JobApplication> results = new ArrayList<>();

        List<Company> companies = companyService.searchByName(companyKeyword);
        for (Company c : companies) {
            List<JobApplication> apps = applicationService.listByCompanyId(c.getId());
            for (JobApplication app : apps) {
                if (app.getJobTitle() != null && app.getJobTitle().toLowerCase().contains(jobKeyword.toLowerCase())) {
                    results.add(app);
                }
            }
        }

        return results;
    }

    // ========== 面试匹配 ==========

    /**
     * 智能匹配面试
     * <p>
     * 搜索策略：
     * 1. 时间关键词（今天、明天、后天、本周、下周、最近）
     * 2. 通过公司名/职位名找到申请，再找面试
     * </p>
     *
     * @param keyword 关键词（公司名、职位名、或时间）
     * @return 匹配结果
     */
    public ToolResult smartMatchInterview(String keyword) {
        log.debug("智能匹配面试: keyword={}", keyword);

        if (keyword == null || keyword.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供搜索关键词");
        }

        List<InterviewRecord> matches = new ArrayList<>();
        String normalizedKeyword = keyword.trim().toLowerCase();

        // 1. 尝试时间关键词
        if (isTimeKeyword(normalizedKeyword)) {
            matches.addAll(searchInterviewsByTimeKeyword(normalizedKeyword));
            log.debug("时间关键词匹配: {} -> {} 个面试", normalizedKeyword, matches.size());
        }

        // 2. 通过申请匹配
        if (matches.isEmpty()) {
            ToolResult appResult = smartMatchApplication(keyword);
            if (appResult.isSuccess() && appResult.getData() != null) {
                matches.addAll(findInterviewsFromApplicationResult(appResult));
            }
        }

        return handleMatchResult(matches, "面试", keyword);
    }

    /**
     * 从申请匹配结果中查找面试
     */
    private List<InterviewRecord> findInterviewsFromApplicationResult(ToolResult appResult) {
        List<InterviewRecord> interviews = new ArrayList<>();

        if (appResult.getData() instanceof JobApplication app) {
            // 单个申请
            interviews.addAll(interviewService.listByApplicationId(app.getId()));
        } else if (appResult.getData() instanceof List<?> list) {
            // 多个申请
            for (Object obj : list) {
                if (obj instanceof JobApplication appItem) {
                    interviews.addAll(interviewService.listByApplicationId(appItem.getId()));
                }
            }
        }

        return interviews;
    }

    /**
     * 判断是否为时间关键词
     */
    private boolean isTimeKeyword(String keyword) {
        if (keyword == null) return false;
        String lower = keyword.toLowerCase();
        return lower.contains("今天") || lower.contains("明天") ||
               lower.contains("后天") || lower.contains("昨天") ||
               lower.contains("本周") || lower.contains("下周") ||
               lower.contains("最近") || lower.contains("即将");
    }

    /**
     * 根据时间关键词搜索面试
     */
    private List<InterviewRecord> searchInterviewsByTimeKeyword(String keyword) {
        LocalDate today = LocalDate.now();

        if (keyword.contains("今天")) {
            return findInterviewsByDate(today);
        } else if (keyword.contains("明天")) {
            return findInterviewsByDate(today.plusDays(1));
        } else if (keyword.contains("后天")) {
            return findInterviewsByDate(today.plusDays(2));
        } else if (keyword.contains("昨天")) {
            return findInterviewsByDate(today.minusDays(1));
        } else if (keyword.contains("本周")) {
            LocalDate weekEnd = today.plusDays(7 - today.getDayOfWeek().getValue());
            return findInterviewsByDateRange(today, weekEnd);
        } else if (keyword.contains("下周")) {
            LocalDate weekStart = today.plusDays(8 - today.getDayOfWeek().getValue());
            LocalDate weekEnd = weekStart.plusDays(6);
            return findInterviewsByDateRange(weekStart, weekEnd);
        } else if (keyword.contains("最近") || keyword.contains("即将")) {
            return interviewService.listRecent(7);
        }

        return new ArrayList<>();
    }

    /**
     * 查找指定日期的面试
     */
    private List<InterviewRecord> findInterviewsByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return interviewService.listByDateRange(start, end);
    }

    /**
     * 查找日期范围内的面试
     */
    private List<InterviewRecord> findInterviewsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return interviewService.listByDateRange(start, end);
    }

    // ========== 公司匹配 ==========

    /**
     * 智能匹配公司
     *
     * @param keyword 公司名称关键词
     * @return 匹配结果
     */
    public ToolResult smartMatchCompany(String keyword) {
        log.debug("智能匹配公司: keyword={}", keyword);

        if (keyword == null || keyword.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供公司名称");
        }

        List<Company> matches = new ArrayList<>();

        // 1. 精确匹配
        Company exact = companyService.getByName(keyword.trim());
        if (exact != null) {
            matches.add(exact);
        }

        // 2. 模糊匹配
        if (matches.isEmpty()) {
            matches.addAll(companyService.searchByName(keyword.trim()));
        }

        return handleMatchResult(matches, "公司", keyword);
    }

    // ========== 通用方法 ==========

    /**
     * 处理匹配结果
     *
     * @param matches     匹配列表
     * @param entityType  实体类型名称
     * @param keyword     搜索关键词
     * @return 统一格式的结果
     */
    private <T> ToolResult handleMatchResult(List<T> matches, String entityType, String keyword) {
        if (matches.isEmpty()) {
            return ToolResult.error(
                ToolConstants.ERR_NOT_FOUND,
                String.format("未找到%s：\"%s\"", entityType, keyword)
            );
        }

        if (matches.size() == 1) {
            return ToolResult.success("找到唯一匹配", matches.get(0));
        }

        // 多个匹配，返回列表供选择
        return ToolResult.multipleMatch(
            String.format("找到 %d 个%s，请选择：", matches.size(), entityType),
            matches
        );
    }

    // ========== 辅助方法 ==========

    /**
     * 获取公司名称（辅助方法）
     *
     * @param companyId 公司ID
     * @return 公司名称，如果不存在返回"未知公司"
     */
    public String getCompanyName(Long companyId) {
        if (companyId == null) return "未知公司";
        Company company = companyService.getById(companyId);
        return company != null ? company.getName() : "未知公司";
    }

    /**
     * 格式化申请列表（用于多匹配提示）
     *
     * @param applications 申请列表
     * @return 格式化字符串
     */
    public String formatApplicationList(List<JobApplication> applications) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < applications.size(); i++) {
            JobApplication app = applications.get(i);
            sb.append(String.format("%d. %s - %s (状态: %s)\n",
                i + 1,
                getCompanyName(app.getCompanyId()),
                app.getJobTitle(),
                ToolConstants.getStatusDescription(app.getStatus())
            ));
        }
        return sb.toString();
    }

    /**
     * 格式化面试列表（用于多匹配提示）
     *
     * @param interviews 面试列表
     * @return 格式化字符串
     */
    public String formatInterviewList(List<InterviewRecord> interviews) {
        StringBuilder sb = new StringBuilder();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (int i = 0; i < interviews.size(); i++) {
            InterviewRecord interview = interviews.get(i);
            // 获取关联的申请信息
            JobApplication app = applicationService.getById(interview.getApplicationId());
            String companyJob = app != null ?
                getCompanyName(app.getCompanyId()) + " - " + app.getJobTitle() : "未知申请";

            sb.append(String.format("%d. %s | %s面试 | %s\n",
                i + 1,
                companyJob,
                ToolConstants.getInterviewTypeDescription(interview.getInterviewType()),
                interview.getInterviewDate().format(formatter)
            ));
        }
        return sb.toString();
    }
}
