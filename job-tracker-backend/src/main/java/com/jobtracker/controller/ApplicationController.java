package com.jobtracker.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jobtracker.common.result.Result;
import com.jobtracker.entity.Company;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.constants.ApplicationStatus;
import com.jobtracker.service.*;
import com.jobtracker.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 求职申请控制器
 * <p>
 * 提供求职申请的 CRUD 操作接口
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-18
 */
@Slf4j
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final StatusTransitionService statusTransitionService;
    private final ApplicationLogService applicationLogService;
    private final CompanyService companyService;
    private final InterviewService interviewService;

    /**
     * 获取所有求职申请
     *
     * @return 申请列表
     */
    @GetMapping
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
    @GetMapping("/{id}")
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
    @GetMapping("/status/{status}")
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
    @GetMapping("/page")
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
     * 获取高优先级申请
     *
     * @return 高优先级申请列表
     */
    @GetMapping("/high-priority")
    public Result<List<JobApplication>> getHighPriorityApplications() {
        try {
            List<JobApplication> applications = applicationService.listHighPriority();
            return Result.success("查询成功", applications);
        } catch (Exception e) {
            log.error("获取高优先级申请失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 更新申请状态
     * <p>
     * 使用状态转换服务验证状态转换的合法性
     * </p>
     *
     * @param id     申请ID
     * @param status 新状态
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
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
    @PostMapping
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
    @PutMapping("/{id}")
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
    @DeleteMapping("/{id}")
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
}
