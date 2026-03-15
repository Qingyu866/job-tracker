package com.jobtracker.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jobtracker.common.result.Result;
import com.jobtracker.dto.ApplicationDetailDTO;
import com.jobtracker.dto.ApplicationExcelDTO;
import com.jobtracker.dto.ApplicationLogDTO;
import com.jobtracker.entity.Company;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.constants.ApplicationStatus;
import com.jobtracker.dto.InterviewProgress;
import com.jobtracker.service.*;
import com.jobtracker.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据查询控制器
 * <p>
 * 提供 HTTP REST API 接口，用于查询和管理求职数据
 * 作为 WebSocket 的备用接口
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/data")
@RequiredArgsConstructor
public class DataController {

    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final CompanyService companyService;
    private final ApplicationLogService applicationLogService;
    private final ExportService exportService;
    private final StatusTransitionService statusTransitionService;
    private final InterviewManagementService interviewManagementService;

    // ==================== 求职申请相关接口 ====================

    /**
     * 获取所有求职申请
     *
     * @return 申请列表
     */
    @GetMapping("/applications")
    public Result<List<JobApplication>> getAllApplications() {
        try {
            List<JobApplication> applications = applicationService.list();
            return Result.success("查询成功", applications);
        } catch (Exception e) {
            log.error("获取申请列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取求职申请
     *
     * @param id 申请ID
     * @return 申请详情
     */
    @GetMapping("/applications/{id}")
    public Result<JobApplication> getApplicationById(@PathVariable Long id) {
        try {
            JobApplication application = applicationService.getById(id);
            if (application == null) {
                return Result.error("申请不存在");
            }
            return Result.success("查询成功", application);
        } catch (Exception e) {
            log.error("获取申请详情失败：id={}", id, e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 根据状态获取求职申请
     *
     * @param status 申请状态
     * @return 申请列表
     */
    @GetMapping("/applications/status/{status}")
    public Result<List<JobApplication>> getApplicationsByStatus(@PathVariable String status) {
        try {
            List<JobApplication> applications = applicationService.listByStatus(status);
            return Result.success("查询成功", applications);
        } catch (Exception e) {
            log.error("按状态查询申请失败：status={}", status, e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询求职申请
     *
     * @param pageNum  当前页
     * @param pageSize 每页大小
     * @param status   申请状态（可选）
     * @return 分页结果
     */
    @GetMapping("/applications/page")
    public Result<IPage<JobApplication>> getApplicationsPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status
    ) {
        try {
            Page<JobApplication> page = new Page<>(pageNum, pageSize);
            IPage<JobApplication> result;
            if (status != null && !status.trim().isEmpty()) {
                result = applicationService.pageByStatus(pageNum, pageSize, status);
            } else {
                result = applicationService.page(page);
            }
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("分页查询申请失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 更新申请状态
     * <p>
     * 使用状态转换服务验证状态转换的合法性
     * </p>
     *
     * @param id       申请ID
     * @param status   新状态
     * @return 操作结果
     */
    @PutMapping("/applications/{id}/status")
    public Result<String> updateApplicationStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        try {
            ApplicationStatus newStatus = ApplicationStatus.fromCode(status);
            if (newStatus == null) {
                return Result.error("无效的状态代码: " + status);
            }

            // 使用状态转换服务（包含验证和联动）
            statusTransitionService.transitionApplicationStatus(id, newStatus);
            return Result.success("状态更新成功");
        } catch (BusinessException e) {
            log.warn("状态转换失败：id={}, status={}, reason={}", id, status, e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新申请状态失败：id={}, status={}", id, status, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 创建求职申请
     *
     * @param application 申请信息
     * @return 操作结果
     */
    @PostMapping("/applications")
    public Result<Long> createApplication(@RequestBody JobApplication application) {
        try {
            boolean success = applicationService.save(application);
            if (success) {
                // 获取公司名称用于日志记录
                String companyName = "未知公司";
                if (application.getCompanyId() != null) {
                    Company company = companyService.getById(application.getCompanyId());
                    if (company != null) {
                        companyName = company.getName();
                    }
                }

                // 记录申请创建日志
                applicationLogService.createApplicationCreatedLog(
                    application.getId(),
                    application.getJobTitle(),
                    companyName
                );
                return Result.success("创建成功", application.getId());
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建申请失败", e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新求职申请（完整更新）
     *
     * @param id 申请ID
     * @param application 更新的申请信息
     * @return 操作结果
     */
    @PutMapping("/applications/{id}")
    public Result<Boolean> updateApplication(
            @PathVariable Long id,
            @RequestBody JobApplication application
    ) {
        try {
            JobApplication existing = applicationService.getById(id);
            if (existing == null) {
                return Result.error("申请不存在");
            }

            application.setId(id);
            application.setCreatedAt(existing.getCreatedAt()); // 保留创建时间
            boolean success = applicationService.updateById(application);

            if (success) {
                log.info("申请更新成功：id={}", id);
                return Result.success("更新成功", true);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新申请失败：id={}", id, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除求职申请（保护性删除）
     * <p>
     * 如果该申请下有关联的面试记录，则阻止删除并提示用户
     * </p>
     *
     * @param id 申请ID
     * @return 操作结果
     */
    @DeleteMapping("/applications/{id}")
    public Result<Boolean> deleteApplication(@PathVariable Long id) {
        try {
            JobApplication application = applicationService.getById(id);
            if (application == null) {
                return Result.error("申请不存在");
            }

            // 检查是否有关联的面试记录
            List<InterviewRecord> interviews = interviewService.listByApplicationId(id);
            if (!interviews.isEmpty()) {
                return Result.error("该申请下有 " + interviews.size() + " 条面试记录，请先删除面试记录");
            }

            // 执行逻辑删除
            boolean success = applicationService.removeById(id);
            if (success) {
                log.info("申请删除成功：id={}", id);
                return Result.success("删除成功", true);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除申请失败：id={}", id, e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    // ==================== 面试记录相关接口 ====================

    /**
     * 获取所有面试记录
     *
     * @return 面试记录列表
     */
    @GetMapping("/interviews")
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
     * 根据申请ID获取面试记录
     *
     * @param applicationId 申请ID
     * @return 面试记录列表
     */
    @GetMapping("/interviews/application/{applicationId}")
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
    @GetMapping("/interviews/upcoming")
    public Result<List<InterviewRecord>> getUpcomingInterviews() {
        try {
            List<InterviewRecord> interviews = interviewService.listUpcoming();
            return Result.success("查询成功", interviews);
        } catch (Exception e) {
            log.error("获取即将进行的面试失败", e);
            return Result.error("查询失败：" + e.getMessage());
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
    @PostMapping("/interviews")
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
     * 标记面试为已完成
     * <p>
     * 使用 InterviewManagementService 完成面试，支持面试结果评估和状态联动
     * </p>
     *
     * @param id 面试记录ID
     * @param requestBody 包含评分、反馈和结果的请求体
     * @return 操作结果
     */
    @PostMapping("/interviews/{id}/complete")
    public Result<Boolean> completeInterview(
        @PathVariable Long id,
        @RequestBody java.util.Map<String, Object> requestBody
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
    @PutMapping("/interviews/{id}/feedback")
    public Result<Boolean> updateFeedback(
        @PathVariable Long id,
        @RequestBody java.util.Map<String, String> requestBody
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
    @PutMapping("/interviews/{id}/technical-questions")
    public Result<Boolean> updateTechnicalQuestions(
        @PathVariable Long id,
        @RequestBody java.util.Map<String, String> requestBody
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
    @PutMapping("/interviews/{id}/cancel")
    public Result<Boolean> cancelInterview(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> requestBody
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
    @PutMapping("/interviews/{id}/no-show")
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
     * 设置跟进标记
     *
     * @param id 面试记录ID
     * @param requestBody 包含跟进标记的请求体
     * @return 操作结果
     */
    @PutMapping("/interviews/{id}/follow-up")
    public Result<Boolean> setFollowUpRequired(
        @PathVariable Long id,
        @RequestBody java.util.Map<String, Boolean> requestBody
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
    @GetMapping("/applications/{applicationId}/interview-progress")
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
    @GetMapping("/applications/{applicationId}/current-interview")
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
     * 开始面试（将状态从 SCHEDULED 改为 IN_PROGRESS）
     *
     * @param id 面试记录ID
     * @return 操作结果
     */
    @PutMapping("/interviews/{id}/start")
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
    @PutMapping("/interviews/{id}/mark-final")
    public Result<Boolean> markAsFinalInterview(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Boolean> requestBody
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
    @PostMapping("/interviews/{id}/reschedule")
    public Result<InterviewRecord> rescheduleInterview(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> requestBody
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
     * 更新面试记录（完整更新）
     *
     * @param id 面试记录ID
     * @param interview 更新的面试记录信息
     * @return 操作结果
     */
    @PutMapping("/interviews/{id}")
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
    @DeleteMapping("/interviews/{id}")
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

    // ==================== 公司相关接口 ====================

    /**
     * 获取所有公司
     *
     * @return 公司列表
     */
    @GetMapping("/companies")
    public Result<List<Company>> getAllCompanies() {
        try {
            List<Company> companies = companyService.list();
            return Result.success("查询成功", companies);
        } catch (Exception e) {
            log.error("获取公司列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 模糊搜索公司
     *
     * @param keyword 搜索关键词
     * @return 公司列表
     */
    @GetMapping("/companies/search")
    public Result<List<Company>> searchCompanies(@RequestParam String keyword) {
        try {
            List<Company> companies = companyService.searchByName(keyword);
            return Result.success("查询成功", companies);
        } catch (Exception e) {
            log.error("搜索公司失败：keyword={}", keyword, e);
            return Result.error("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取公司
     *
     * @param id 公司ID
     * @return 公司详情
     */
    @GetMapping("/companies/{id}")
    public Result<Company> getCompanyById(@PathVariable Long id) {
        try {
            Company company = companyService.getById(id);
            if (company == null) {
                return Result.error("公司不存在");
            }
            return Result.success("查询成功", company);
        } catch (Exception e) {
            log.error("获取公司详情失败：id={}", id, e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 根据名称获取公司
     *
     * @param name 公司名称
     * @return 公司详情
     */
    @GetMapping("/companies/name")
    public Result<Company> getCompanyByName(@RequestParam String name) {
        try {
            Company company = companyService.getByName(name);
            if (company == null) {
                return Result.error("公司不存在");
            }
            return Result.success("查询成功", company);
        } catch (Exception e) {
            log.error("获取公司详情失败：name={}", name, e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 创建公司
     *
     * @param company 公司信息
     * @return 操作结果
     */
    @PostMapping("/companies")
    public Result<Long> createCompany(@RequestBody Company company) {
        try {
            Company result = companyService.createOrUpdate(company);
            return Result.success("创建成功", result.getId());
        } catch (Exception e) {
            log.error("创建公司失败", e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新公司信息
     *
     * @param id 公司ID
     * @param company 更新的公司信息
     * @return 操作结果
     */
    @PutMapping("/companies/{id}")
    public Result<Boolean> updateCompany(
            @PathVariable Long id,
            @RequestBody Company company
    ) {
        try {
            Company existing = companyService.getById(id);
            if (existing == null) {
                return Result.error("公司不存在");
            }

            company.setId(id);
            company.setCreatedAt(existing.getCreatedAt()); // 保留创建时间
            boolean success = companyService.updateById(company);

            if (success) {
                log.info("公司更新成功：id={}", id);
                return Result.success("更新成功", true);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新公司失败：id={}", id, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除公司（保护性删除）
     * <p>
     * 如果该公司下有关联的申请记录，则阻止删除并提示用户
     * </p>
     *
     * @param id 公司ID
     * @return 操作结果
     */
    @DeleteMapping("/companies/{id}")
    public Result<Boolean> deleteCompany(@PathVariable Long id) {
        try {
            Company company = companyService.getById(id);
            if (company == null) {
                return Result.error("公司不存在");
            }

            // 检查是否有关联的申请记录
            List<JobApplication> applications = applicationService.listByCompanyId(id);
            if (!applications.isEmpty()) {
                return Result.error("该公司下有 " + applications.size() + " 条申请记录，请先删除申请记录");
            }

            // 执行逻辑删除
            boolean success = companyService.removeById(id);
            if (success) {
                log.info("公司删除成功：id={}", id);
                return Result.success("删除成功", true);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除公司失败：id={}", id, e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    // ==================== 日志相关接口 ====================

    /**
     * 获取所有日志（按时间倒序，带申请和公司信息）
     *
     * @return 日志列表
     */
    @GetMapping("/logs")
    public Result<List<ApplicationLogDTO>> getAllLogs() {
        try {
            List<com.jobtracker.entity.ApplicationLog> logs = applicationLogService.listRecent(100);

            // 构建DTO列表，包含申请和公司信息
            List<ApplicationLogDTO> dtoList = logs.stream()
                .map(log -> {
                    ApplicationLogDTO dto = new ApplicationLogDTO();
                    dto.setLog(log);

                    // 获取申请信息
                    JobApplication application = applicationService.getById(log.getApplicationId());
                    dto.setApplication(application);

                    // 获取公司信息
                    if (application != null && application.getCompanyId() != null) {
                        Company company = companyService.getById(application.getCompanyId());
                        dto.setCompany(company);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

            return Result.success("查询成功", dtoList);
        } catch (Exception e) {
            log.error("获取日志列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 根据申请ID获取日志（带申请和公司信息）
     *
     * @param applicationId 申请ID
     * @return 日志列表
     */
    @GetMapping("/applications/{applicationId}/logs")
    public Result<List<ApplicationLogDTO>> getLogsByApplicationId(@PathVariable Long applicationId) {
        try {
            List<com.jobtracker.entity.ApplicationLog> logs = applicationLogService.listByApplicationId(applicationId);

            // 构建DTO列表
            List<ApplicationLogDTO> dtoList = new ArrayList<>();
            JobApplication application = applicationService.getById(applicationId);
            Company company = null;

            if (application != null && application.getCompanyId() != null) {
                company = companyService.getById(application.getCompanyId());
            }

            for (com.jobtracker.entity.ApplicationLog log : logs) {
                ApplicationLogDTO dto = new ApplicationLogDTO();
                dto.setLog(log);
                dto.setApplication(application);
                dto.setCompany(company);
                dtoList.add(dto);
            }

            return Result.success("查询成功", dtoList);
        } catch (Exception e) {
            log.error("获取申请日志失败：applicationId={}", applicationId, e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    // ==================== 统计相关接口 ====================

    /**
     * 获取统计数据
     *
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public Result<List<Object>> getStatistics() {
        try {
            List<Object> stats = applicationService.countByStatus();
            return Result.success("查询成功", stats);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取高优先级申请
     *
     * @return 高优先级申请列表
     */
    @GetMapping("/applications/high-priority")
    public Result<List<JobApplication>> getHighPriorityApplications() {
        try {
            List<JobApplication> applications = applicationService.listHighPriority();
            return Result.success("查询成功", applications);
        } catch (Exception e) {
            log.error("获取高优先级申请失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    // ==================== 聚合查询接口 ====================

    /**
     * 获取申请详情（聚合信息）
     * <p>
     * 返回申请的完整信息，包括：
     * - 申请基本信息
     * - 关联的公司信息
     * - 所有面试记录
     * - 最近操作日志
     * - 统计信息
     * </p>
     *
     * @param id 申请ID
     * @return 申请详情聚合DTO
     */
    @GetMapping("/applications/{id}/detail")
    public Result<ApplicationDetailDTO> getApplicationDetail(@PathVariable Long id) {
        try {
            ApplicationDetailDTO detail = exportService.getApplicationDetail(id);
            if (detail == null) {
                return Result.error("申请不存在");
            }
            return Result.success("查询成功", detail);
        } catch (Exception e) {
            log.error("获取申请详情失败：id={}", id, e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    // ==================== 数据导出接口 ====================

    /**
     * 导出数据为Excel格式
     * <p>
     * 使用EasyExcel导出所有求职申请数据
     * </p>
     *
     * @param response HTTP响应
     */
    @GetMapping("/export/excel")
    public void exportExcel(HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("求职记录_" + LocalDate.now(), StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            // 获取数据并导出
            List<ApplicationExcelDTO> data = exportService.getExcelExportData();

            EasyExcel.write(response.getOutputStream(), ApplicationExcelDTO.class)
                    .sheet("求职记录")
                    .doWrite(data);

            log.info("Excel导出成功：共 {} 条记录", data.size());
        } catch (Exception e) {
            log.error("Excel导出失败", e);
        }
    }

    /**
     * 导出数据为JSON格式
     *
     * @return 所有申请的详情列表
     */
    @GetMapping("/export/json")
    public Result<List<ApplicationDetailDTO>> exportJson() {
        try {
            List<ApplicationDetailDTO> data = exportService.getJsonExportData();
            log.info("JSON导出成功：共 {} 条记录", data.size());
            return Result.success("导出成功", data);
        } catch (Exception e) {
            log.error("JSON导出失败", e);
            return Result.error("导出失败：" + e.getMessage());
        }
    }

    /**
     * 搜索求职申请（多字段）
     *
     * @param keyword 搜索关键词
     * @return 申请列表
     */
    @GetMapping("/applications/search")
    public Result<List<JobApplication>> searchApplications(@RequestParam String keyword) {
        try {
            List<JobApplication> applications = applicationService.searchApplications(keyword);
            return Result.success("查询成功", applications);
        } catch (Exception e) {
            log.error("搜索申请失败：keyword={}", keyword, e);
            return Result.error("搜索失败：" + e.getMessage());
        }
    }
}
