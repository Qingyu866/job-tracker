package com.jobtracker.controller;

import com.jobtracker.common.result.Result;
import com.jobtracker.dto.InterviewProgress;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.service.*;
import com.jobtracker.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 面试记录控制器
 * <p>
 * 提供面试记录的 CRUD 和状态管理接口
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-18
 */
@Slf4j
@RestController
@RequestMapping("/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final InterviewManagementService interviewManagementService;

    /**
     * 获取所有面试记录
     *
     * @return 面试记录列表
     */
    @GetMapping
    public Result<List<InterviewRecord>> getAllInterviews() {
        try {
            List<InterviewRecord> interviews = interviewService.list();
            return Result.success("查询成功", interviews);
        } catch (Exception e) {
            log.error("获取面试列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取面试记录
     *
     * @param id 面试记录ID
     * @return 面试记录详情
     */
    @GetMapping("/{id}")
    public Result<InterviewRecord> getInterviewById(@PathVariable Long id) {
        try {
            InterviewRecord interview = interviewService.getById(id);
            if (interview == null) {
                return Result.error("面试记录不存在");
            }
            return Result.success("查询成功", interview);
        } catch (Exception e) {
            log.error("获取面试记录详情失败：id={}", id, e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 根据申请ID获取面试记录
     *
     * @param applicationId 申请ID
     * @return 面试记录列表
     */
    @GetMapping("/application/{applicationId}")
    public Result<List<InterviewRecord>> getInterviewsByApplicationId(@PathVariable Long applicationId) {
        try {
            List<InterviewRecord> interviews = interviewService.listByApplicationId(applicationId);
            return Result.success("查询成功", interviews);
        } catch (Exception e) {
            log.error("按申请ID查询面试失败：applicationId={}", applicationId, e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取即将进行的面试
     *
     * @return 面试记录列表
     */
    @GetMapping("/upcoming")
    public Result<List<InterviewRecord>> getUpcomingInterviews() {
        try {
            List<InterviewRecord> interviews = interviewService.listUpcoming();
            return Result.success("查询成功", interviews);
        } catch (Exception e) {
            log.error("获取即将进行的面试失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取申请的面试进度
     * <p>
     * 返回多轮面试的进度信息，包括：
     * - 总轮次数
     * - 已完成轮次
     * - 通过轮次
     * - 当前进行中的轮次
     * - 进度文本描述
     * </p>
     *
     * @param applicationId 申请ID
     * @return 面试进度信息
     */
    @GetMapping("/applications/{applicationId}/progress")
    public Result<InterviewProgress> getInterviewProgress(@PathVariable Long applicationId) {
        try {
            InterviewProgress progress = interviewManagementService.getProgress(applicationId);
            return Result.success("查询成功", progress);
        } catch (Exception e) {
            log.error("获取面试进度失败：applicationId={}", applicationId, e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取申请的当前面试
     *
     * @param applicationId 申请ID
     * @return 当前进行中或已安排的面试
     */
    @GetMapping("/applications/{applicationId}/current")
    public Result<InterviewRecord> getCurrentInterview(@PathVariable Long applicationId) {
        try {
            InterviewRecord interview = interviewManagementService.getCurrentInterview(applicationId);
            if (interview == null) {
                return Result.success("暂无进行中的面试", null);
            }
            return Result.success("查询成功", interview);
        } catch (Exception e) {
            log.error("获取当前面试失败：applicationId={}", applicationId, e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 创建面试记录
     * <p>
     * 使用 InterviewManagementService 安排面试，自动处理轮次和状态联动
     * </p>
     *
     * @param interview 面试记录
     * @return 操作结果
     */
    @PostMapping
    public Result<Long> createInterview(@RequestBody InterviewRecord interview) {
        try {
            // 使用面试管理服务安排面试（支持多轮面试和状态联动）
            InterviewRecord saved = interviewManagementService.scheduleNextRound(
                interview.getApplicationId(),
                interview.getInterviewType(),
                interview.getInterviewDate(),
                interview.getInterviewerName(),
                interview.getInterviewerTitle(),
                interview.getNotes()
            );
            return Result.success("创建成功", saved.getId());
        } catch (BusinessException e) {
            log.warn("创建面试记录失败：applicationId={}, reason={}",
                interview.getApplicationId(), e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建面试记录失败", e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新面试记录（完整更新）
     *
     * @param id 面试记录ID
     * @param interview 更新的面试记录信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateInterview(
            @PathVariable Long id,
            @RequestBody InterviewRecord interview
    ) {
        try {
            InterviewRecord existing = interviewService.getById(id);
            if (existing == null) {
                return Result.error("面试记录不存在");
            }

            interview.setId(id);
            interview.setCreatedAt(existing.getCreatedAt()); // 保留创建时间
            boolean success = interviewService.updateById(interview);

            if (success) {
                log.info("面试记录更新成功：id={}", id);
                return Result.success("更新成功", true);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新面试记录失败：id={}", id, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除面试记录（逻辑删除）
     *
     * @param id 面试记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteInterview(@PathVariable Long id) {
        try {
            InterviewRecord interview = interviewService.getById(id);
            if (interview == null) {
                return Result.error("面试记录不存在");
            }

            // 执行逻辑删除
            boolean success = interviewService.removeById(id);
            if (success) {
                log.info("面试记录删除成功：id={}", id);
                return Result.success("删除成功", true);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除面试记录失败：id={}", id, e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 标记面试为已完成
     * <p>
     * 使用 InterviewManagementService 完成面试，支持面试结果评估和状态联动
     * </p>
     *
     * @param id 面试记录ID
     * @param requestBody 包含评分、反馈和结果的请求体
     * @return 操作结果
     */
    @PostMapping("/{id}/complete")
    public Result<Boolean> completeInterview(
        @PathVariable Long id,
        @RequestBody Map<String, Object> requestBody
    ) {
        try {
            Integer rating = requestBody.containsKey("rating") ?
                ((Number) requestBody.get("rating")).intValue() : null;
            String feedback = requestBody.containsKey("feedback") ?
                (String) requestBody.get("feedback") : null;
            String result = requestBody.containsKey("result") ?
                (String) requestBody.get("result") : null;

            // 使用面试管理服务完成面试（支持结果评估和状态联动）
            interviewManagementService.completeInterview(id, result, rating, feedback);
            return Result.success("面试已标记为完成", true);
        } catch (BusinessException e) {
            log.warn("完成面试失败：id={}, reason={}", id, e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("标记面试完成失败：id={}", id, e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 更新面试反馈
     *
     * @param id 面试记录ID
     * @param requestBody 包含反馈内容的请求体
     * @return 操作结果
     */
    @PutMapping("/{id}/feedback")
    public Result<Boolean> updateFeedback(
        @PathVariable Long id,
        @RequestBody Map<String, String> requestBody
    ) {
        try {
            String feedback = requestBody.get("feedback");
            boolean success = interviewService.updateFeedback(id, feedback);
            if (success) {
                return Result.success("反馈已更新", true);
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            log.error("更新面试反馈失败：id={}", id, e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 更新技术问题记录
     *
     * @param id 面试记录ID
     * @param requestBody 包含技术问题的请求体
     * @return 操作结果
     */
    @PutMapping("/{id}/technical-questions")
    public Result<Boolean> updateTechnicalQuestions(
        @PathVariable Long id,
        @RequestBody Map<String, String> requestBody
    ) {
        try {
            String technicalQuestions = requestBody.get("technicalQuestions");
            boolean success = interviewService.updateTechnicalQuestions(id, technicalQuestions);
            if (success) {
                return Result.success("技术问题已更新", true);
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            log.error("更新技术问题失败：id={}", id, e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 取消面试
     * <p>
     * 使用 InterviewManagementService 取消面试
     * </p>
     *
     * @param id 面试记录ID
     * @param requestBody 包含取消原因的请求体（可选）
     * @return 操作结果
     */
    @PutMapping("/{id}/cancel")
    public Result<Boolean> cancelInterview(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody
    ) {
        try {
            String reason = requestBody != null ? requestBody.get("reason") : null;
            interviewManagementService.cancelInterview(id, reason);
            return Result.success("面试已取消", true);
        } catch (BusinessException e) {
            log.warn("取消面试失败：id={}, reason={}", id, e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("取消面试失败：id={}", id, e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 标记面试为未参加
     * <p>
     * 使用 InterviewManagementService 标记未参加
     * </p>
     *
     * @param id 面试记录ID
     * @return 操作结果
     */
    @PutMapping("/{id}/no-show")
    public Result<Boolean> markAsNoShow(@PathVariable Long id) {
        try {
            interviewManagementService.markAsNoShow(id);
            return Result.success("已标记为未参加", true);
        } catch (BusinessException e) {
            log.warn("标记面试未参加失败：id={}, reason={}", id, e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("标记面试未参加失败：id={}", id, e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 开始面试（将状态从 SCHEDULED 改为 IN_PROGRESS）
     *
     * @param id 面试记录ID
     * @return 操作结果
     */
    @PutMapping("/{id}/start")
    public Result<Boolean> startInterview(@PathVariable Long id) {
        try {
            interviewManagementService.startInterview(id);
            return Result.success("面试已开始", true);
        } catch (BusinessException e) {
            log.warn("开始面试失败：id={}, reason={}", id, e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("开始面试失败：id={}", id, e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 标记面试为终面
     *
     * @param id 面试记录ID
     * @param requestBody 包含 isFinal 的请求体
     * @return 操作结果
     */
    @PutMapping("/{id}/mark-final")
    public Result<Boolean> markAsFinalInterview(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> requestBody
    ) {
        try {
            Boolean isFinal = requestBody.get("isFinal");
            if (isFinal == null) {
                isFinal = true;
            }
            interviewManagementService.markAsFinalInterview(id, isFinal);
            return Result.success("终面标记已更新", true);
        } catch (BusinessException e) {
            log.warn("标记终面失败：id={}, reason={}", id, e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("标记终面失败：id={}", id, e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 重新安排面试
     *
     * @param id 原面试记录ID
     * @param requestBody 包含新面试时间的请求体
     * @return 新面试记录
     */
    @PostMapping("/{id}/reschedule")
    public Result<InterviewRecord> rescheduleInterview(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody
    ) {
        try {
            String newDateStr = requestBody.get("newInterviewDate");
            if (newDateStr == null || newDateStr.isEmpty()) {
                return Result.error("请提供新的面试时间");
            }
            java.time.LocalDateTime newDate = java.time.LocalDateTime.parse(newDateStr);
            InterviewRecord newInterview = interviewManagementService.rescheduleInterview(id, newDate);
            return Result.success("面试已重新安排", newInterview);
        } catch (BusinessException e) {
            log.warn("重新安排面试失败：id={}, reason={}", id, e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("重新安排面试失败：id={}", id, e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 设置跟进标记
     *
     * @param id 面试记录ID
     * @param requestBody 包含跟进标记的请求体
     * @return 操作结果
     */
    @PutMapping("/{id}/follow-up")
    public Result<Boolean> setFollowUpRequired(
        @PathVariable Long id,
        @RequestBody Map<String, Boolean> requestBody
    ) {
        try {
            Boolean followUpRequired = requestBody.get("followUpRequired");
            boolean success = interviewService.setFollowUpRequired(id, followUpRequired);
            if (success) {
                return Result.success("跟进标记已更新", true);
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            log.error("设置跟进标记失败：id={}", id, e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }
}
