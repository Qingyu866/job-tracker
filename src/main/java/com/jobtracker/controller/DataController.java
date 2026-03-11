package com.jobtracker.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jobtracker.common.result.Result;
import com.jobtracker.entity.Company;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.service.ApplicationService;
import com.jobtracker.service.CompanyService;
import com.jobtracker.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final CompanyService companyService;

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
            boolean success = applicationService.updateStatus(id, status);
            if (success) {
                return Result.success("状态更新成功");
            } else {
                return Result.error("状态更新失败");
            }
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
                return Result.success("创建成功", application.getId());
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建申请失败", e);
            return Result.error("创建失败：" + e.getMessage());
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
     *
     * @param interview 面试记录
     * @return 操作结果
     */
    @PostMapping("/interviews")
    public Result<Long> createInterview(@RequestBody InterviewRecord interview) {
        try {
            boolean success = interviewService.save(interview);
            if (success) {
                return Result.success("创建成功", interview.getId());
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建面试记录失败", e);
            return Result.error("创建失败：" + e.getMessage());
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
}
