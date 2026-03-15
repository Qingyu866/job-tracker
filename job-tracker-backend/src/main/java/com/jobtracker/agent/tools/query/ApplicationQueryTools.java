package com.jobtracker.agent.tools.query;

import com.jobtracker.agent.tools.ToolCallTrace;
import com.jobtracker.agent.tools.shared.ToolConstants;
import com.jobtracker.agent.tools.shared.ToolHelper;
import com.jobtracker.agent.tools.shared.ToolResult;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.service.ApplicationService;
import com.jobtracker.service.CompanyService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 求职申请查询工具类
 * <p>
 * 提供 LangChain4j Agent 可调用的申请查询方法
 * 所有方法使用统一的 ToolResult 返回格式
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationQueryTools {

    private final ApplicationService applicationService;
    private final CompanyService companyService;
    private final ToolHelper toolHelper;
    private final ToolCallTrace toolCallTrace;

    /**
     * 根据ID获取申请详情
     */
    @Tool("""
        [查询] 获取求职申请详情

        适用场景：用户想查看某个具体申请的完整信息

        参数：
        - applicationId: 申请ID（必填，数字类型）

        返回：申请的详细信息，包括公司、职位、状态、面试记录等
        """)
    public ToolResult getApplicationById(Long applicationId) {
        long startTime = System.currentTimeMillis();
        log.info("AI调用：获取申请详情 id={}", applicationId);

        ToolResult result;
        try {
            if (applicationId == null) {
                result = ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "申请ID不能为空");
            } else {
                JobApplication app = applicationService.getById(applicationId);
                if (app == null) {
                    result = ToolResult.error(ToolConstants.ERR_NOT_FOUND, "申请不存在，ID: " + applicationId);
                } else {
                    // 构建详细信息
                    StringBuilder info = new StringBuilder();
                    info.append("**申请详情**\n\n");
                    info.append(String.format("- 公司：%s\n", toolHelper.getCompanyName(app.getCompanyId())));
                    info.append(String.format("- 职位：%s\n", app.getJobTitle()));
                    info.append(String.format("- 状态：%s\n", ToolConstants.getStatusDescription(app.getStatus())));
                    if (app.getJobType() != null) {
                        info.append(String.format("- 类型：%s\n", app.getJobType()));
                    }
                    if (app.getWorkLocation() != null) {
                        info.append(String.format("- 地点：%s\n", app.getWorkLocation()));
                    }
                    if (app.getSalaryMin() != null || app.getSalaryMax() != null) {
                        info.append(String.format("- 薪资：%s - %s\n",
                            app.getSalaryMin() != null ? app.getSalaryMin() : "面议",
                            app.getSalaryMax() != null ? app.getSalaryMax() : "面议"));
                    }
                    info.append(String.format("- 优先级：%d/10\n", app.getPriority() != null ? app.getPriority() : 5));
                    if (app.getApplicationDate() != null) {
                        info.append(String.format("- 申请日期：%s\n", app.getApplicationDate()));
                    }
                    if (app.getNotes() != null && !app.getNotes().isBlank()) {
                        info.append(String.format("- 备注：%s\n", app.getNotes()));
                    }

                    result = ToolResult.success(info.toString(), app);
                }
            }

            // 记录工具调用
            ToolCallTrace.record("getApplicationById", applicationId, result, startTime);
            return result;
        } catch (Exception e) {
            // 记录失败
            ToolCallTrace.recordFailure("getApplicationById", applicationId, e, startTime);
            throw e;
        }
    }

    /**
     * 获取申请列表
     */
    @Tool("""
        [查询] 获取求职申请列表

        适用场景：
        - 用户想查看所有或部分申请
        - 按状态筛选申请
        - 搜索特定职位

        参数：
        - status: 按状态筛选（可选）
          WISHLIST/APPLIED/INTERVIEW/OFFER/REJECTED/WITHDRAWN
        - keyword: 按职位关键词搜索（可选）
        - limit: 返回数量限制（可选，默认10，最大50）

        返回：申请列表摘要
        """)
    public ToolResult listApplications(String status, String keyword, Integer limit) {
        log.info("AI调用：查询申请列表 status={}, keyword={}", status, keyword);

        limit = limit != null ? Math.min(limit, ToolConstants.MAX_PAGE_SIZE) : ToolConstants.DEFAULT_PAGE_SIZE;

        List<JobApplication> applications;

        if (keyword != null && !keyword.isBlank()) {
            applications = applicationService.searchByJobTitle(keyword);
        } else if (status != null && !status.isBlank()) {
            applications = applicationService.listByStatus(status);
        } else {
            applications = applicationService.list();
        }

        // 限制返回数量
        if (applications.size() > limit) {
            applications = applications.subList(0, limit);
        }

        if (applications.isEmpty()) {
            return ToolResult.info("没有找到符合条件的申请", List.of());
        }

        // 构建摘要列表
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("📋 找到 %d 个申请：\n\n", applications.size()));

        for (JobApplication app : applications) {
            summary.append(String.format("- **%s - %s** | 状态: %s | 优先级: %d\n",
                toolHelper.getCompanyName(app.getCompanyId()),
                app.getJobTitle(),
                ToolConstants.getStatusDescription(app.getStatus()),
                app.getPriority() != null ? app.getPriority() : 5
            ));
        }

        return ToolResult.info(summary.toString(), applications);
    }

    /**
     * 搜索申请（关键字优先）
     */
    @Tool("""
        [查询] 搜索求职申请

        适用场景：用户说"帮我找一下字节的申请"、"查看前端相关的申请"

        参数：
        - keyword: 搜索关键词（公司名或职位名）

        返回：匹配的申请列表
        """)
    public ToolResult searchApplications(String keyword) {
        log.info("AI调用：搜索申请 keyword={}", keyword);

        if (keyword == null || keyword.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供搜索关键词");
        }

        return toolHelper.smartMatchApplication(keyword);
    }

    /**
     * 获取高优先级申请
     */
    @Tool("""
        [查询] 获取高优先级申请

        适用场景：用户说"我要重点关注哪些申请"、"有哪些高优先级的"

        返回：优先级 >= 8 的申请列表
        """)
    public ToolResult getHighPriorityApplications() {
        log.info("AI调用：获取高优先级申请");

        List<JobApplication> applications = applicationService.listHighPriority();

        if (applications.isEmpty()) {
            return ToolResult.info("暂无高优先级申请", List.of());
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("🔥 高优先级申请（%d个）：\n\n", applications.size()));

        for (JobApplication app : applications) {
            summary.append(String.format("- **%s - %s** | 状态: %s | 优先级: %d\n",
                toolHelper.getCompanyName(app.getCompanyId()),
                app.getJobTitle(),
                ToolConstants.getStatusDescription(app.getStatus()),
                app.getPriority()
            ));
        }

        return ToolResult.info(summary.toString(), applications);
    }

    /**
     * 获取最近申请
     */
    @Tool("""
        [查询] 获取最近的求职申请

        适用场景：用户说"最近投了哪些"、"查看最近一周的申请"

        参数：
        - days: 最近N天（可选，默认7天）

        返回：最近N天内的申请列表
        """)
    public ToolResult getRecentApplications(Integer days) {
        log.info("AI调用：获取最近申请 days={}", days);

        days = days != null && days > 0 ? days : 7;

        java.time.LocalDate endDate = java.time.LocalDate.now();
        java.time.LocalDate startDate = endDate.minusDays(days);

        List<JobApplication> applications = applicationService.listByDateRange(startDate, endDate);

        if (applications.isEmpty()) {
            return ToolResult.info(String.format("最近%d天没有申请记录", days), List.of());
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("📅 最近%d天的申请（%d个）：\n\n", days, applications.size()));

        for (JobApplication app : applications) {
            summary.append(String.format("- **%s - %s** | %s | 状态: %s\n",
                toolHelper.getCompanyName(app.getCompanyId()),
                app.getJobTitle(),
                app.getApplicationDate() != null ? app.getApplicationDate() : "未投递",
                ToolConstants.getStatusDescription(app.getStatus())
            ));
        }

        return ToolResult.info(summary.toString(), applications);
    }
}
