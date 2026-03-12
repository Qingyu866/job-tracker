package com.jobtracker.agent.tools;

import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.service.InterviewService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 面试记录工具类
 * <p>
 * 提供 LangChain4j Agent 可以调用的面试记录相关方法
 * 使用 @Tool 注解标记可被 AI 调用的方法
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewTools {

    private final InterviewService interviewService;

    /**
     * 创建面试记录
     * <p>
     * 为指定的求职申请创建新的面试记录
     * </p>
     *
     * @param applicationId       申请ID（必填）
     * @param interviewType       面试类型（PHONE/VIDEO/ONSITE/TECHNICAL/HR）
     * @param interviewDateStr    面试时间字符串（格式：yyyy-MM-dd HH:mm）
     * @param interviewerName     面试官姓名（可选）
     * @param interviewerTitle    面试官职位（可选）
     * @param durationMinutesInt  面试时长（分钟，可选）
     * @return 操作结果的描述性消息
     */
    @Tool("创建面试记录。参数：申请ID、面试类型、面试时间（格式：yyyy-MM-dd HH:mm，例如：2026-03-13 09:00）、面试官姓名、面试官职位、面试时长")
    public String createInterview(
            Long applicationId,
            String interviewType,
            String interviewDateStr,
            String interviewerName,
            String interviewerTitle,
            Integer durationMinutesInt
    ) {
        try {
            log.info("AI 调用创建面试：applicationId={}, type={}", applicationId, interviewType);

            if (applicationId == null) {
                return "创建失败：申请ID不能为空";
            }
            if (interviewDateStr == null || interviewDateStr.trim().isEmpty()) {
                return "创建失败：面试时间不能为空";
            }

            InterviewRecord record = new InterviewRecord();
            record.setApplicationId(applicationId);
            record.setInterviewType(interviewType != null ? interviewType : "TECHNICAL");
            record.setInterviewerName(interviewerName);
            record.setInterviewerTitle(interviewerTitle);
            record.setDurationMinutes(durationMinutesInt);
            record.setStatus("SCHEDULED");

            // 解析时间
            try {
                String dateToParse = interviewDateStr;
                // 智能判断：如果只传了日期（长度小于等于11），自动补充默认时间 09:00
                if (interviewDateStr.length() <= 11) {
                    dateToParse = interviewDateStr + " 09:00";
                    log.info("检测到只传了日期，自动补充时间为：{}", dateToParse);
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                record.setInterviewDate(LocalDateTime.parse(dateToParse, formatter));
            } catch (Exception e) {
                log.warn("时间解析失败，尝试其他格式：{}", interviewDateStr);
                try {
                    record.setInterviewDate(LocalDateTime.parse(interviewDateStr));
                } catch (Exception e2) {
                    return "创建失败：时间格式错误，请使用 yyyy-MM-dd HH:mm 格式";
                }
            }

            boolean success = interviewService.save(record);
            if (success) {
                return String.format("✅ 成功创建面试记录：申请ID %d，%s面试，时间：%s",
                        applicationId,
                        record.getInterviewType(),
                        record.getInterviewDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            } else {
                return "❌ 创建失败：保存面试记录时出错";
            }
        } catch (Exception e) {
            log.error("创建面试失败", e);
            return "❌ 创建失败：" + e.getMessage();
        }
    }

    /**
     * 更新面试记录
     * <p>
     * 更新面试的状态、评分、反馈等信息
     * </p>
     *
     * @param interviewId         面试记录ID（必填）
     * @param status              新状态（可选：SCHEDULED/COMPLETED/CANCELLED/NO_SHOW）
     * @param ratingInt           评分（可选，1-5分）
     * @param feedback            反馈（可选）
     * @param technicalQuestions 技术问题记录（可选）
     * @param interviewDateStr    面试时间（可选，格式：yyyy-MM-dd HH:mm）
     * @return 操作结果的描述性消息
     */
    @Tool("更新面试记录。参数：面试ID、状态、评分、反馈、技术问题、面试时间（格式：yyyy-MM-dd HH:mm，例如：2026-03-13 09:00）")
    public String updateInterview(
            Long interviewId,
            String status,
            Integer ratingInt,
            String feedback,
            String technicalQuestions,
            String interviewDateStr
    ) {
        try {
            log.info("AI 调用更新面试：interviewId={}, status={}", interviewId, status);

            if (interviewId == null) {
                return "更新失败：面试ID不能为空";
            }

            InterviewRecord record = interviewService.getById(interviewId);
            if (record == null) {
                return String.format("❌ 更新失败：面试记录 ID %d 不存在", interviewId);
            }

            // 根据状态使用不同的更新方法
            if ("COMPLETED".equals(status)) {
                boolean success = interviewService.markAsCompleted(interviewId, ratingInt, feedback);
                if (success) {
                    String result = String.format("✅ 面试已标记为完成：ID %d", interviewId);
                    if (ratingInt != null) {
                        result += String.format("，评分：%d/5", ratingInt);
                    }
                    return result;
                } else {
                    return "❌ 更新失败";
                }
            } else if ("CANCELLED".equals(status)) {
                boolean success = interviewService.cancelInterview(interviewId);
                return success ? "✅ 面试已取消" : "❌ 更新失败";
            } else if ("NO_SHOW".equals(status)) {
                boolean success = interviewService.markAsNoShow(interviewId);
                return success ? "✅ 已标记为未参加" : "❌ 更新失败";
            } else {
                // 其他更新
                if (status != null) {
                    record.setStatus(status);
                }
                if (feedback != null) {
                    record.setFeedback(feedback);
                }
                if (technicalQuestions != null) {
                    record.setTechnicalQuestions(technicalQuestions);
                }
                // 解析面试时间（使用系统默认时区，避免时区转换问题）
                if (interviewDateStr != null && !interviewDateStr.trim().isEmpty()) {
                    try {
                        DateTimeFormatter formatter;
                        String dateToParse = interviewDateStr;
                        // 智能判断：如果只传了日期（长度小于等于11），自动补充默认时间 09:00
                        if (interviewDateStr.length() <= 11) {
                            dateToParse = interviewDateStr + " 09:00";
                            log.info("检测到只传了日期，自动补充时间为：{}", dateToParse);
                        }
                        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        record.setInterviewDate(LocalDateTime.parse(dateToParse, formatter));
                        log.info("更新面试时间：{}", dateToParse);
                    } catch (Exception e) {
                        log.warn("时间解析失败，保持原时间：{}", interviewDateStr, e);
                    }
                }
                boolean success = interviewService.updateById(record);
                return success ? "✅ 更新成功" : "❌ 更新失败";
            }
        } catch (Exception e) {
            log.error("更新面试失败", e);
            return "❌ 更新失败：" + e.getMessage();
        }
    }

    /**
     * 查询面试记录
     * <p>
     * 根据条件查询面试记录
     * </p>
     *
     * @param applicationId 按申请ID筛选（可选）
     * @param status        按状态筛选（可选）
     * @param days          查询最近N天的面试（可选）
     * @return 查询结果的描述性消息
     */
    @Tool("查询面试记录。参数：申请ID、状态筛选、最近N天")
    public String queryInterviews(
            Long applicationId,
            String status,
            Integer days
    ) {
        try {
            log.info("AI 调用查询面试：applicationId={}, status={}, days={}",
                    applicationId, status, days);

            List<InterviewRecord> interviews;

            if (applicationId != null) {
                interviews = interviewService.listByApplicationId(applicationId);
            } else if (status != null && !status.trim().isEmpty()) {
                interviews = interviewService.listByStatus(status);
            } else if (days != null && days > 0) {
                interviews = interviewService.listRecent(days);
            } else {
                interviews = interviewService.list();
            }

            if (interviews.isEmpty()) {
                return "📋 没有找到符合条件的面试记录";
            }

            // 格式化输出
            StringBuilder sb = new StringBuilder();
            sb.append("📋 找到 ").append(interviews.size()).append(" 个面试记录：\n\n");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (InterviewRecord interview : interviews) {
                sb.append(String.format("""
                        **面试 #%d** - %s
                        - 申请ID：%d
                        - 时间：%s
                        - 面试官：%s %s
                        - 时长：%d 分钟
                        - 状态：%s
                        """,
                        interview.getId(),
                        interview.getInterviewType(),
                        interview.getApplicationId(),
                        interview.getInterviewDate().format(formatter),
                        interview.getInterviewerName() != null ? interview.getInterviewerName() : "未指定",
                        interview.getInterviewerTitle() != null ? interview.getInterviewerTitle() : "",
                        interview.getDurationMinutes() != null ? interview.getDurationMinutes() : 0,
                        interview.getStatus()
                ));

                if (interview.getRating() != null) {
                    sb.append("- 评分：").append(interview.getRating()).append("/5\n");
                }
                if (interview.getFeedback() != null && !interview.getFeedback().trim().isEmpty()) {
                    sb.append("- 反馈：").append(interview.getFeedback()).append("\n");
                }
                if (interview.getTechnicalQuestions() != null && !interview.getTechnicalQuestions().trim().isEmpty()) {
                    sb.append("- 技术问题：").append(interview.getTechnicalQuestions()).append("\n");
                }
                sb.append("\n");
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("查询面试失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }
}
