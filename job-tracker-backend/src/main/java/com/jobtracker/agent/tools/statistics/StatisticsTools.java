package com.jobtracker.agent.tools.statistics;

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

import java.util.List;
import java.util.Map;

/**
 * 统计工具类
 * <p>
 * 提供 LangChain4j Agent 可调用的统计分析方法
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsTools {

    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final CompanyService companyService;
    private final ToolHelper toolHelper;

    /**
     * 获取求职统计数据
     */
    @Tool("""
        [统计] 获取求职统计数据和趋势分析

        适用场景：
        - 用户说"给我看看整体情况"
        - 用户说"统计一下我的求职进度"
        - 用户说"帮我分析一下"

        返回：各状态申请数量、面试数量、高优先级申请等统计信息
        """)
    public ToolResult getApplicationStatistics() {
        log.info("AI调用：获取求职统计");

        List<?> stats = applicationService.countByStatus();
        List<JobApplication> allApplications = applicationService.list();
        List<InterviewRecord> allInterviews = interviewService.list();

        int total = allApplications.size();
        int totalInterviews = allInterviews.size();
        int highPriority = applicationService.listHighPriority().size();
        int upcomingInterviews = interviewService.listUpcoming().size();

        StringBuilder sb = new StringBuilder();
        sb.append("📊 **求职统计报告**\n\n");

        sb.append("**总体概况**\n");
        sb.append(String.format("- 总申请数：%d\n", total));
        sb.append(String.format("- 总面试数：%d\n", totalInterviews));
        sb.append(String.format("- 高优先级申请：%d\n", highPriority));
        sb.append(String.format("- 即将进行的面试：%d\n\n", upcomingInterviews));

        sb.append("**状态分布**\n");
        for (Object stat : stats) {
            Map<?, ?> map = (Map<?, ?>) stat;
            String status = (String) map.get("status");
            Long count = (Long) map.get("count");
            sb.append(String.format("- %s：%d 个\n",
                ToolConstants.getStatusDescription(status), count));
        }

        sb.append("\n**建议**\n");
        if (highPriority > 0) {
            sb.append(String.format("- 🔥 你有 %d 个高优先级申请，建议重点跟进\n", highPriority));
        }
        if (upcomingInterviews > 0) {
            sb.append(String.format("- 📅 有 %d 个即将进行的面试，记得准备\n", upcomingInterviews));
        }

        long wishlistCount = allApplications.stream()
                .filter(a -> ToolConstants.STATUS_WISHLIST.equals(a.getStatus()))
                .count();
        if (wishlistCount > 3) {
            sb.append(String.format("- 💡 有 %d 个待投递申请，可以开始投递了\n", wishlistCount));
        }

        long appliedCount = allApplications.stream()
                .filter(a -> ToolConstants.STATUS_APPLIED.equals(a.getStatus()))
                .count();
        if (appliedCount > 5) {
            sb.append(String.format("- ⏳ 有 %d 个已投递申请待回复，建议准备面试\n", appliedCount));
        }

        return ToolResult.info(sb.toString(), Map.of(
            "totalApplications", total,
            "totalInterviews", totalInterviews,
            "highPriority", highPriority,
            "upcomingInterviews", upcomingInterviews,
            "statusDistribution", stats
        ));
    }

    /**
     * 获取面试统计
     */
    @Tool("""
        [统计] 获取面试统计数据

        适用场景：
        - 用户说"统计一下面试情况"
        - 用户说"我面试了多少次了"

        返回：面试完成率、平均评分等统计信息
        """)
    public ToolResult getInterviewStatistics() {
        log.info("AI调用：获取面试统计");

        List<InterviewRecord> allInterviews = interviewService.list();

        if (allInterviews.isEmpty()) {
            return ToolResult.info("暂无面试记录", Map.of("total", 0));
        }

        long completed = allInterviews.stream()
                .filter(i -> ToolConstants.INTERVIEW_COMPLETED.equals(i.getStatus()))
                .count();
        long scheduled = allInterviews.stream()
                .filter(i -> ToolConstants.INTERVIEW_SCHEDULED.equals(i.getStatus()))
                .count();
        long cancelled = allInterviews.stream()
                .filter(i -> ToolConstants.INTERVIEW_CANCELLED.equals(i.getStatus()))
                .count();

        // 计算平均评分
        double avgRating = allInterviews.stream()
                .filter(i -> i.getRating() != null && ToolConstants.INTERVIEW_COMPLETED.equals(i.getStatus()))
                .mapToInt(InterviewRecord::getRating)
                .average()
                .orElse(0.0);

        // 统计各类型面试
        Map<String, Long> typeCount = allInterviews.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    i -> i.getInterviewType() != null ? i.getInterviewType() : "UNKNOWN",
                    java.util.stream.Collectors.counting()
                ));

        StringBuilder sb = new StringBuilder();
        sb.append("📊 **面试统计报告**\n\n");

        sb.append("**总体概况**\n");
        sb.append(String.format("- 总面试数：%d\n", allInterviews.size()));
        sb.append(String.format("- 已完成：%d (%.1f%%)\n", completed, completed * 100.0 / allInterviews.size()));
        sb.append(String.format("- 已安排：%d\n", scheduled));
        sb.append(String.format("- 已取消：%d\n", cancelled));

        if (avgRating > 0) {
            sb.append(String.format("- 平均评分：%.1f/5\n", avgRating));
        }

        sb.append("\n**面试类型分布**\n");
        typeCount.forEach((type, count) -> {
            sb.append(String.format("- %s：%d 次\n",
                ToolConstants.getInterviewTypeDescription(type), count));
        });

        return ToolResult.info(sb.toString(), Map.of(
            "total", allInterviews.size(),
            "completed", completed,
            "scheduled", scheduled,
            "cancelled", cancelled,
            "averageRating", avgRating,
            "typeDistribution", typeCount
        ));
    }
}
