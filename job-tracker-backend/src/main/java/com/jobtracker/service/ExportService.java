package com.jobtracker.service;

import com.jobtracker.dto.ApplicationDetailDTO;
import com.jobtracker.dto.ApplicationDetailDTO.InterviewStatistics;
import com.jobtracker.dto.ApplicationExcelDTO;
import com.jobtracker.entity.ApplicationLog;
import com.jobtracker.entity.Company;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.entity.JobApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据导出服务
 *
 * @author Job Tracker Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final ApplicationService applicationService;
    private final CompanyService companyService;
    private final InterviewService interviewService;
    private final ApplicationLogService applicationLogService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取申请详情聚合数据
     */
    public ApplicationDetailDTO getApplicationDetail(Long applicationId) {
        JobApplication application = applicationService.getById(applicationId);
        if (application == null) {
            return null;
        }

        Company company = fetchCompany(application.getCompanyId());
        List<InterviewRecord> interviews = interviewService.listByApplicationId(applicationId);
        List<ApplicationLog> logs = applicationLogService.listByApplicationId(applicationId);

        return ApplicationDetailDTO.builder()
                .application(application)
                .company(company)
                .interviews(interviews)
                .logs(logs.stream().limit(20).collect(Collectors.toList()))
                .statistics(calculateStatistics(interviews))
                .build();
    }

    /**
     * 获取Excel导出数据
     */
    public List<ApplicationExcelDTO> getExcelExportData() {
        List<JobApplication> applications = applicationService.list();

        return applications.stream()
                .map(this::toExcelDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取JSON导出数据
     */
    public List<ApplicationDetailDTO> getJsonExportData() {
        List<JobApplication> applications = applicationService.list();

        return applications.stream()
                .map(app -> getApplicationDetail(app.getId()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    private Company fetchCompany(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return companyService.getById(companyId);
    }

    private InterviewStatistics calculateStatistics(List<InterviewRecord> interviews) {
        int completed = (int) interviews.stream()
                .filter(i -> "COMPLETED".equals(i.getStatus()))
                .count();

        int scheduled = (int) interviews.stream()
                .filter(i -> "SCHEDULED".equals(i.getStatus()))
                .count();

        double avgRating = interviews.stream()
                .filter(i -> "COMPLETED".equals(i.getStatus()))
                .filter(i -> i.getRating() != null)
                .mapToInt(InterviewRecord::getRating)
                .average()
                .orElse(0.0);

        return InterviewStatistics.builder()
                .total(interviews.size())
                .completed(completed)
                .scheduled(scheduled)
                .averageRating(avgRating)
                .build();
    }

    private ApplicationExcelDTO toExcelDTO(JobApplication app) {
        Company company = fetchCompany(app.getCompanyId());
        List<InterviewRecord> interviews = interviewService.listByApplicationId(app.getId());

        return ApplicationExcelDTO.builder()
                .id(app.getId())
                .jobTitle(app.getJobTitle())
                .companyName(company != null ? company.getName() : "")
                .status(translateStatus(app.getStatus()))
                .jobType(translateJobType(app.getJobType()))
                .workLocation(app.getWorkLocation() != null ? app.getWorkLocation() : "")
                .salaryRange(formatSalaryRange(app))
                .applicationDate(app.getApplicationDate() != null ? app.getApplicationDate().format(DATE_FORMATTER) : "")
                .priority(app.getPriority())
                .interviewCount(interviews.size())
                .notes(app.getNotes() != null ? app.getNotes() : "")
                .createdAt(app.getCreatedAt() != null ? app.getCreatedAt().format(DATETIME_FORMATTER) : "")
                .build();
    }

    private String formatSalaryRange(JobApplication app) {
        if (app.getSalaryMin() == null || app.getSalaryMax() == null) {
            return "";
        }
        return String.format("%.0fK-%.0fK",
                app.getSalaryMin().divide(new java.math.BigDecimal("1000")).doubleValue(),
                app.getSalaryMax().divide(new java.math.BigDecimal("1000")).doubleValue());
    }

    private String translateStatus(String status) {
        if (status == null) return "";
        return switch (status) {
            case "WISHLIST" -> "待投递";
            case "APPLIED" -> "已投递";
            case "INTERVIEW" -> "面试中";
            case "OFFER" -> "已录用";
            case "REJECTED" -> "已拒绝";
            case "WITHDRAWN" -> "已撤回";
            default -> status;
        };
    }

    private String translateJobType(String jobType) {
        if (jobType == null) return "";
        return switch (jobType) {
            case "FULL_TIME" -> "全职";
            case "PART_TIME" -> "兼职";
            case "CONTRACT" -> "合同";
            case "INTERNSHIP" -> "实习";
            default -> jobType;
        };
    }
}
