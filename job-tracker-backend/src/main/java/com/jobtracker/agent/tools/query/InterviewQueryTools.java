package com.jobtracker.agent.tools.query;

import com.jobtracker.agent.tools.shared.ToolConstants;
import com.jobtracker.agent.tools.shared.ToolHelper;
import com.jobtracker.agent.tools.shared.ToolResult;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.service.ApplicationService;
import com.jobtracker.service.CompanyService;
import com.jobtracker.service.InterviewService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 面试查询工具类
 * <p>
 * 提供 LangChain4j Agent 可调用的面试查询方法
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewQueryTools {

    private final InterviewService interviewService;
    private final ApplicationService applicationService;
    private final CompanyService companyService;
    private final ToolHelper toolHelper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 根据ID获取面试详情
     */
    @Tool("""
        [查询] 获取面试详情

        适用场景：用户想查看某个具体面试的详细信息

        参数：
        - interviewId: 面试ID（必填，数字类型）

        返回：面试的详细信息
        """)
    public ToolResult getInterviewById(Long interviewId) {
        log.info("AI调用：获取面试详情 id={}", interviewId);

        if (interviewId == null) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "面试ID不能为空");
        }

        InterviewRecord interview = interviewService.getById(interviewId);
        if (interview == null) {
            return ToolResult.error(ToolConstants.ERR_NOT_FOUND, "面试不存在，ID: " + interviewId);
        }

        // 获取关联的申请信息
        JobApplication app = applicationService.getById(interview.getApplicationId());
        String companyJob = app != null ?
            toolHelper.getCompanyName(app.getCompanyId()) + " - " + app.getJobTitle() : "未知申请";

        StringBuilder info = new StringBuilder();
        info.append("**面试详情**\n\n");
        info.append(String.format("- 关联申请：%s\n", companyJob));
        info.append(String.format("- 面试类型：%s\n", ToolConstants.getInterviewTypeDescription(interview.getInterviewType())));
        info.append(String.format("- 面试时间：%s\n", interview.getInterviewDate().format(DATE_FORMATTER)));
        info.append(String.format("- 状态：%s\n", ToolConstants.getInterviewStatusDescription(interview.getStatus())));

        if (interview.getInterviewerName() != null) {
            info.append(String.format("- 面试官：%s", interview.getInterviewerName()));
            if (interview.getInterviewerTitle() != null) {
                info.append(String.format("（%s）", interview.getInterviewerTitle()));
            }
            info.append("\n");
        }

        if (interview.getDurationMinutes() != null) {
            info.append(String.format("- 时长：%d 分钟\n", interview.getDurationMinutes()));
        }

        if (interview.getRating() != null) {
            info.append(String.format("- 评分：%d/5\n", interview.getRating()));
        }

        if (interview.getFeedback() != null && !interview.getFeedback().isBlank()) {
            info.append(String.format("- 反馈：%s\n", interview.getFeedback()));
        }

        if (interview.getTechnicalQuestions() != null && !interview.getTechnicalQuestions().isBlank()) {
            info.append(String.format("- 技术问题：%s\n", interview.getTechnicalQuestions()));
        }

        return ToolResult.success(info.toString(), interview);
    }

    /**
     * 获取面试列表
     */
    @Tool("""
        [查询] 获取面试列表

        适用场景：
        - 用户想查看所有或部分面试
        - 按状态筛选面试
        - 查看某个申请的面试

        参数（全部可选）：
        - applicationId: 按申请ID筛选
        - status: 按状态筛选 SCHEDULED/COMPLETED/CANCELLED/NO_SHOW
        - days: 查询最近N天的面试

        返回：面试列表
        """)
    public ToolResult listInterviews(Long applicationId, String status, Integer days) {
        log.info("AI调用：查询面试列表 applicationId={}, status={}, days={}", applicationId, status, days);

        List<InterviewRecord> interviews;

        if (applicationId != null) {
            interviews = interviewService.listByApplicationId(applicationId);
        } else if (status != null && !status.isBlank()) {
            interviews = interviewService.listByStatus(status);
        } else if (days != null && days > 0) {
            interviews = interviewService.listRecent(days);
        } else {
            interviews = interviewService.list();
        }

        if (interviews.isEmpty()) {
            return ToolResult.info("没有找到符合条件的面试记录", List.of());
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("📋 找到 %d 个面试记录：\n\n", interviews.size()));

        for (InterviewRecord interview : interviews) {
            JobApplication app = applicationService.getById(interview.getApplicationId());
            String companyJob = app != null ?
                toolHelper.getCompanyName(app.getCompanyId()) + " - " + app.getJobTitle() : "未知申请";

            summary.append(String.format("- **%s**\n", companyJob));
            summary.append(String.format("  - 类型：%s | 时间：%s | 状态：%s\n",
                ToolConstants.getInterviewTypeDescription(interview.getInterviewType()),
                interview.getInterviewDate().format(DATE_FORMATTER),
                ToolConstants.getInterviewStatusDescription(interview.getStatus())
            ));
        }

        return ToolResult.info(summary.toString(), interviews);
    }

    /**
     * 搜索面试（关键字优先）
     */
    @Tool("""
        [查询] 搜索面试

        适用场景：
        - 用户说"查看字节的面试"
        - 用户说"明天的面试有哪些"
        - 用户说"最近的面试"

        参数：
        - keyword: 搜索关键词（公司名、职位名、或时间关键词如"明天"、"本周"）

        返回：匹配的面试列表
        """)
    public ToolResult searchInterviews(String keyword) {
        log.info("AI调用：搜索面试 keyword={}", keyword);

        if (keyword == null || keyword.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供搜索关键词");
        }

        return toolHelper.smartMatchInterview(keyword);
    }

    /**
     * 获取即将进行的面试
     */
    @Tool("""
        [查询] 获取即将进行的面试

        适用场景：用户说"接下来有什么面试"、"查看待进行的面试"

        返回：状态为 SCHEDULED 且时间在未来的面试列表
        """)
    public ToolResult getUpcomingInterviews() {
        log.info("AI调用：获取即将进行的面试");

        List<InterviewRecord> interviews = interviewService.listUpcoming();

        if (interviews.isEmpty()) {
            return ToolResult.info("暂无即将进行的面试", List.of());
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("📅 即将进行的面试（%d个）：\n\n", interviews.size()));

        for (InterviewRecord interview : interviews) {
            JobApplication app = applicationService.getById(interview.getApplicationId());
            String companyJob = app != null ?
                toolHelper.getCompanyName(app.getCompanyId()) + " - " + app.getJobTitle() : "未知申请";

            summary.append(String.format("- **%s**\n", companyJob));
            summary.append(String.format("  - %s面试 | %s\n",
                ToolConstants.getInterviewTypeDescription(interview.getInterviewType()),
                interview.getInterviewDate().format(DATE_FORMATTER)
            ));
        }

        return ToolResult.info(summary.toString(), interviews);
    }

    /**
     * 获取需要跟进的面试
     */
    @Tool("""
        [查询] 获取需要跟进的面试

        适用场景：用户说"哪些面试需要跟进"、"有什么需要关注的"

        返回：标记为需要跟进的面试列表
        """)
    public ToolResult getFollowUpRequiredInterviews() {
        log.info("AI调用：获取需要跟进的面试");

        List<InterviewRecord> interviews = interviewService.listFollowUpRequired();

        if (interviews.isEmpty()) {
            return ToolResult.info("暂无需要跟进的面试", List.of());
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("⚠️ 需要跟进的面试（%d个）：\n\n", interviews.size()));

        for (InterviewRecord interview : interviews) {
            JobApplication app = applicationService.getById(interview.getApplicationId());
            String companyJob = app != null ?
                toolHelper.getCompanyName(app.getCompanyId()) + " - " + app.getJobTitle() : "未知申请";

            summary.append(String.format("- **%s** | %s | %s\n",
                companyJob,
                ToolConstants.getInterviewTypeDescription(interview.getInterviewType()),
                interview.getInterviewDate().format(DATE_FORMATTER)
            ));
        }

        return ToolResult.info(summary.toString(), interviews);
    }
}
