package com.jobtracker.agent.tools.command;

import com.jobtracker.agent.tools.shared.ToolConstants;
import com.jobtracker.agent.tools.shared.ToolHelper;
import com.jobtracker.agent.tools.shared.ToolResult;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.service.InterviewService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 面试命令工具类（关键字优先设计）
 * <p>
 * 提供 LangChain4j Agent 可调用的面试操作方法
 * 所有操作支持通过公司名、职位名或时间关键词来定位面试
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewCommandTools {

    private final InterviewService interviewService;
    private final ToolHelper toolHelper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 创建面试记录（快速创建）
     */
    @Tool("""
        [创建] 为求职申请创建面试记录（快速创建）

        适用场景：
        - 用户说"给字节跳动的申请加个面试，明天下午2点"
        - 用户说"阿里巴巴下周二有面试"

        必填参数：
        - keyword: 公司名称或职位关键词（用于匹配申请）
          例如："字节跳动"、"前端工程师"、"字节-前端"
        - interviewDate: 面试时间（格式：yyyy-MM-dd HH:mm，如"2026-03-15 14:00"）
                          支持自然语言描述（如"明天下午2点"）

        说明：
        - 默认面试类型为"技术面试"
        - 其他信息（面试官等）可通过更新工具补充
        - 返回：创建结果
        """)
    public ToolResult createInterview(
            String keyword,
            String interviewDate
    ) {
        log.info("AI调用：创建面试 keyword={}, date={}", keyword, interviewDate);

        if (keyword == null || keyword.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供公司名称或职位关键词");
        }
        if (interviewDate == null || interviewDate.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "面试时间不能为空");
        }

        try {
            // 智能匹配申请
            ToolResult matchResult = toolHelper.smartMatchApplication(keyword);

            if (!matchResult.isSuccess()) {
                return ToolResult.error(ToolConstants.ERR_NOT_FOUND,
                    String.format("未找到申请：\"%s\"，请先创建申请", keyword));
            }

            // 检查是否需要用户选择
            if (matchResult.isMultipleMatch()) {
                @SuppressWarnings("unchecked")
                List<JobApplication> apps = (List<JobApplication>) matchResult.getData();
                return ToolResult.multipleMatch(
                    "找到多个申请，请告诉我具体是哪个？\n" + toolHelper.formatApplicationList(apps),
                    apps
                );
            }

            JobApplication app = (JobApplication) matchResult.getData();

            // 验证申请状态是否允许创建面试
            if (!ToolConstants.canScheduleInterview(app.getStatus())) {
                String reason = ToolConstants.getInterviewDisabledReason(app.getStatus());
                return ToolResult.error(ToolConstants.ERR_PROTECTED, reason);
            }

            // 创建面试记录（使用默认值）
            InterviewRecord record = new InterviewRecord();
            record.setApplicationId(app.getId());
            record.setInterviewType(ToolConstants.INTERVIEW_TYPE_TECHNICAL);  // 默认技术面试
            record.setInterviewerName(null);
            record.setInterviewerTitle(null);
            record.setDurationMinutes(null);
            record.setStatus(ToolConstants.INTERVIEW_SCHEDULED);

            // 解析时间
            try {
                String dateToParse = interviewDate;
                if (interviewDate.length() <= 11) {
                    dateToParse = interviewDate + " 09:00";
                }
                record.setInterviewDate(LocalDateTime.parse(dateToParse, DATE_FORMATTER));
            } catch (Exception e) {
                return ToolResult.error(ToolConstants.ERR_INVALID_FORMAT,
                    "时间格式错误，请使用 yyyy-MM-dd HH:mm 格式");
            }

            interviewService.save(record);

            String companyName = toolHelper.getCompanyName(app.getCompanyId());
            return ToolResult.success(
                String.format("✅ 成功创建面试：%s - %s，时间：%s",
                    companyName,
                    app.getJobTitle(),
                    record.getInterviewDate().format(DATE_FORMATTER))
            );
        } catch (Exception e) {
            log.error("创建面试失败", e);
            return ToolResult.error(ToolConstants.ERR_CREATE_FAILED, "创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新面试记录（关键字优先）
     */
    @Tool("""
        [更新] 修改面试记录

        适用场景：
        - 用户说"把字节跳动的面试改到后天"
        - 用户说"明天的面试完成了，感觉不错，打4分"
        - 用户说"取消阿里巴巴的面试"

        参数：
        - keyword: 公司名称、职位或时间关键词（必填）
          例如："字节跳动"、"明天的面试"、"阿里巴巴-前端"
        - status: 新状态（可选）
          SCHEDULED/COMPLETED/CANCELLED/NO_SHOW
        - rating: 评分 1-5（可选，面试完成后填写）
        - feedback: 面试反馈（可选）
        - interviewDate: 新的面试时间（可选）
          格式：yyyy-MM-dd HH:mm

        返回：更新结果
        """)
    public ToolResult updateInterview(
            String keyword,
            String status,
            Integer rating,
            String feedback,
            String interviewDate
    ) {
        log.info("AI调用：更新面试 keyword={}, status={}", keyword, status);

        if (keyword == null || keyword.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供面试关键词");
        }

        try {
            // 智能匹配面试
            ToolResult matchResult = toolHelper.smartMatchInterview(keyword);

            if (!matchResult.isSuccess()) {
                return matchResult;
            }

            // 检查是否需要用户选择
            if (matchResult.isMultipleMatch()) {
                @SuppressWarnings("unchecked")
                List<InterviewRecord> interviews = (List<InterviewRecord>) matchResult.getData();
                return ToolResult.multipleMatch(
                    "找到多个面试，请告诉我具体是哪个？\n" + toolHelper.formatInterviewList(interviews),
                    interviews
                );
            }

            InterviewRecord record = (InterviewRecord) matchResult.getData();

            // 根据状态更新
            if (ToolConstants.INTERVIEW_COMPLETED.equals(status)) {
                interviewService.markAsCompleted(record.getId(), rating, feedback);
                String result = "✅ 面试已标记为完成";
                if (rating != null) {
                    result += String.format("，评分：%d/5", rating);
                }
                return ToolResult.success(result);
            } else if (ToolConstants.INTERVIEW_CANCELLED.equals(status)) {
                interviewService.cancelInterview(record.getId());
                return ToolResult.success("✅ 面试已取消");
            } else if (ToolConstants.INTERVIEW_NO_SHOW.equals(status)) {
                interviewService.markAsNoShow(record.getId());
                return ToolResult.success("✅ 已标记为未参加");
            } else {
                // 通用更新
                if (status != null) {
                    record.setStatus(status);
                }
                if (feedback != null) {
                    record.setFeedback(feedback);
                }
                if (interviewDate != null) {
                    try {
                        String dateToParse = interviewDate.length() <= 11 ? interviewDate + " 09:00" : interviewDate;
                        record.setInterviewDate(LocalDateTime.parse(dateToParse, DATE_FORMATTER));
                    } catch (Exception e) {
                        return ToolResult.error(ToolConstants.ERR_INVALID_FORMAT, "时间格式错误");
                    }
                }
                interviewService.updateById(record);
                return ToolResult.success("✅ 面试更新成功");
            }
        } catch (Exception e) {
            log.error("更新面试失败", e);
            return ToolResult.error(ToolConstants.ERR_UPDATE_FAILED, "更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除面试记录（关键字优先）
     */
    @Tool("""
        [删除] 删除面试记录

        ⚠️ 注意：删除操作不可恢复

        参数：
        - keyword: 面试关键词（必填）
          例如："字节跳动的面试"、"明天的面试"

        返回：删除结果
        """)
    public ToolResult deleteInterview(String keyword) {
        log.info("AI调用：删除面试 keyword={}", keyword);

        if (keyword == null || keyword.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供面试关键词");
        }

        try {
            ToolResult matchResult = toolHelper.smartMatchInterview(keyword);

            if (!matchResult.isSuccess()) {
                return matchResult;
            }

            if (matchResult.isMultipleMatch()) {
                @SuppressWarnings("unchecked")
                List<InterviewRecord> interviews = (List<InterviewRecord>) matchResult.getData();
                return ToolResult.multipleMatch(
                    "找到多个面试，请告诉我具体要删除哪个？\n" + toolHelper.formatInterviewList(interviews),
                    interviews
                );
            }

            InterviewRecord record = (InterviewRecord) matchResult.getData();
            interviewService.removeById(record.getId());

            return ToolResult.success("✅ 面试已删除");
        } catch (Exception e) {
            log.error("删除面试失败", e);
            return ToolResult.error(ToolConstants.ERR_DELETE_FAILED, "删除失败：" + e.getMessage());
        }
    }
}
