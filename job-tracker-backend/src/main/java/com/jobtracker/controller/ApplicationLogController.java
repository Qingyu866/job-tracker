package com.jobtracker.controller;

import com.jobtracker.common.result.Result;
import com.jobtracker.dto.ApplicationLogDTO;
import com.jobtracker.entity.ApplicationLog;
import com.jobtracker.entity.Company;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.service.ApplicationLogService;
import com.jobtracker.service.ApplicationService;
import com.jobtracker.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 申请日志控制器
 * <p>
 * 提供申请操作日志的查询接口
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-18
 */
@Slf4j
@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class ApplicationLogController {

    private final ApplicationLogService applicationLogService;
    private final ApplicationService applicationService;
    private final CompanyService companyService;

    /**
     * 获取所有日志（按时间倒序，带申请和公司信息）
     *
     * @return 日志列表
     */
    @GetMapping
    public Result<List<ApplicationLogDTO>> getAllLogs() {
        try {
            List<ApplicationLog> logs = applicationLogService.listRecent(100);

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
    @GetMapping("/applications/{applicationId}")
    public Result<List<ApplicationLogDTO>> getLogsByApplicationId(@PathVariable Long applicationId) {
        try {
            List<ApplicationLog> logs = applicationLogService.listByApplicationId(applicationId);

            // 构建DTO列表
            List<ApplicationLogDTO> dtoList = new ArrayList<>();
            JobApplication application = applicationService.getById(applicationId);
            Company company = null;

            if (application != null && application.getCompanyId() != null) {
                company = companyService.getById(application.getCompanyId());
            }

            for (ApplicationLog log : logs) {
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
}
