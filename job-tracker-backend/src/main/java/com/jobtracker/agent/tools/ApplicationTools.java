package com.jobtracker.agent.tools;

import com.jobtracker.entity.Company;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.service.ApplicationService;
import com.jobtracker.service.CompanyService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 求职申请工具类
 * <p>
 * 提供 LangChain4j Agent 可以调用的求职申请相关方法
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
public class ApplicationTools {

    private final ApplicationService applicationService;
    private final CompanyService companyService;

    /**
     * 创建新的求职申请
     * <p>
     * 根据提供的信息创建新的求职申请记录
     * 如果公司不存在，会先创建公司信息
     * </p>
     *
     * @param companyName    公司名称（必填）
     * @param jobTitle       职位名称（必填）
     * @param jobDescription 职位描述（可选）
     * @param jobType        工作类型（可选，如：全职/兼职/实习）
     * @param workLocation   工作地点（可选）
     * @param salaryMinStr   薪资下限字符串（可选）
     * @param salaryMaxStr   薪资上限字符串（可选）
     * @param jobUrl         职位链接（可选）
     * @param status         申请状态（可选，默认：WISHLIST）
     * @param notes          备注信息（可选）
     * @param priorityInt    优先级（可选，1-10，默认：5）
     * @return 操作结果的描述性消息
     */
    @Tool("创建新的求职申请。参数：公司名称、职位名称、职位描述、工作类型、工作地点、薪资下限、薪资上限、职位链接、申请状态、备注、优先级")
    public String createApplication(
            String companyName,
            String jobTitle,
            String jobDescription,
            String jobType,
            String workLocation,
            String salaryMinStr,
            String salaryMaxStr,
            String jobUrl,
            String status,
            String notes,
            Integer priorityInt
    ) {
        try {
            log.info("AI 调用创建申请：company={}, jobTitle={}", companyName, jobTitle);

            // 参数验证
            if (companyName == null || companyName.trim().isEmpty()) {
                return "创建失败：公司名称不能为空";
            }
            if (jobTitle == null || jobTitle.trim().isEmpty()) {
                return "创建失败：职位名称不能为空";
            }

            // 查找或创建公司
            Long companyId;
            Company existingCompany = companyService.getByName(companyName);
            if (existingCompany != null) {
                companyId = existingCompany.getId();
            } else {
                Company newCompany = Company.builder()
                        .name(companyName)
                        .build();
                companyService.save(newCompany);
                companyId = newCompany.getId();
            }

            // 创建申请
            JobApplication application = new JobApplication();
            application.setCompanyId(companyId);
            application.setJobTitle(jobTitle);
            application.setJobDescription(jobDescription);
            application.setJobType(jobType);
            application.setWorkLocation(workLocation);
            application.setJobUrl(jobUrl);
            application.setStatus(status != null ? status : "WISHLIST");
            application.setNotes(notes);
            application.setPriority(priorityInt != null ? priorityInt : 5);
            application.setApplicationDate("APPLIED".equals(application.getStatus()) ? LocalDate.now() : null);

            // 解析薪资
            if (salaryMinStr != null && !salaryMinStr.trim().isEmpty()) {
                try {
                    application.setSalaryMin(new BigDecimal(salaryMinStr));
                } catch (NumberFormatException e) {
                    log.warn("薪资下限解析失败：{}", salaryMinStr);
                }
            }
            if (salaryMaxStr != null && !salaryMaxStr.trim().isEmpty()) {
                try {
                    application.setSalaryMax(new BigDecimal(salaryMaxStr));
                } catch (NumberFormatException e) {
                    log.warn("薪资上限解析失败：{}", salaryMaxStr);
                }
            }

            boolean success = applicationService.save(application);
            if (success) {
                return String.format("✅ 成功创建求职申请：%s - %s（ID: %d）",
                        companyName, jobTitle, application.getId());
            } else {
                return "❌ 创建失败：保存申请时出错";
            }
        } catch (Exception e) {
            log.error("创建申请失败", e);
            return "❌ 创建失败：" + e.getMessage();
        }
    }

    /**
     * 更新申请状态
     * <p>
     * 将指定的求职申请更新到新的状态
     * </p>
     *
     * @param applicationId 申请ID
     * @param newStatus     新状态（WISHLIST/APPLIED/INTERVIEW/OFFER/REJECTED/WITHDRAWN）
     * @return 操作结果的描述性消息
     */
    @Tool("更新求职申请的状态。参数：申请ID、新状态")
    public String updateApplicationStatus(Long applicationId, String newStatus) {
        try {
            log.info("AI 调用更新状态：applicationId={}, newStatus={}", applicationId, newStatus);

            if (applicationId == null) {
                return "更新失败：申请ID不能为空";
            }

            boolean success = applicationService.updateStatus(applicationId, newStatus);
            if (success) {
                return String.format("✅ 成功更新申请状态：ID %d -> %s", applicationId, newStatus);
            } else {
                return String.format("❌ 更新失败：申请 ID %d 不存在", applicationId);
            }
        } catch (IllegalArgumentException e) {
            return "❌ 更新失败：" + e.getMessage();
        } catch (Exception e) {
            log.error("更新状态失败", e);
            return "❌ 更新失败：" + e.getMessage();
        }
    }

    /**
     * 查询求职申请列表
     * <p>
     * 根据条件查询求职申请，支持多种筛选方式
     * </p>
     *
     * @param status       按状态筛选（可选）
     * @param priorityInt  按优先级筛选（可选）
     * @param days         查询最近N天的申请（可选）
     * @param keyword      按职位名称模糊搜索（可选）
     * @return 查询结果的描述性消息
     */
    @Tool("查询求职申请列表。参数：状态筛选、优先级筛选、最近N天、职位关键词")
    public String queryApplications(
            String status,
            Integer priorityInt,
            Integer days,
            String keyword
    ) {
        try {
            log.info("AI 调用查询申请：status={}, priority={}, days={}, keyword={}",
                    status, priorityInt, days, keyword);

            List<JobApplication> applications;

            // 根据参数选择查询方式
            if (keyword != null && !keyword.trim().isEmpty()) {
                applications = applicationService.searchByJobTitle(keyword);
            } else if (status != null && !status.trim().isEmpty()) {
                applications = applicationService.listByStatus(status);
            } else if (priorityInt != null) {
                applications = applicationService.listByPriority(priorityInt);
            } else if (days != null && days > 0) {
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(days);
                applications = applicationService.listByDateRange(startDate, endDate);
            } else {
                applications = applicationService.list();
            }

            if (applications.isEmpty()) {
                return "📋 没有找到符合条件的求职申请";
            }

            // 格式化输出
            StringBuilder sb = new StringBuilder();
            sb.append("📋 找到 ").append(applications.size()).append(" 个求职申请：\n\n");

            for (JobApplication app : applications) {
                sb.append(String.format("""
                        **%s - %s**
                        - 状态：%s
                        - 类型：%s
                        - 地点：%s
                        - 薪资：%s - %s
                        - 优先级：%d
                        - 申请日期：%s
                        """,
                        app.getJobTitle(),
                        getCompanyName(app.getCompanyId()),
                        app.getStatus(),
                        app.getJobType() != null ? app.getJobType() : "未指定",
                        app.getWorkLocation() != null ? app.getWorkLocation() : "未指定",
                        app.getSalaryMin() != null ? app.getSalaryMin() : "未公开",
                        app.getSalaryMax() != null ? app.getSalaryMax() : "未公开",
                        app.getPriority() != null ? app.getPriority() : 5,
                        app.getApplicationDate() != null ? app.getApplicationDate() : "未申请"
                ));
                if (app.getNotes() != null && !app.getNotes().trim().isEmpty()) {
                    sb.append("- 备注：").append(app.getNotes()).append("\n");
                }
                sb.append("\n");
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("查询申请失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 获取求职统计数据
     * <p>
     * 统计各状态的申请数量，并提供总体分析
     * </p>
     *
     * @return 统计结果的描述性消息
     */
    @Tool("获取求职统计数据和趋势分析")
    public String getStatistics() {
        try {
            log.info("AI 调用统计数据");

            List<?> stats = applicationService.countByStatus();
            List<JobApplication> allApplications = applicationService.list();

            int total = allApplications.size();
            int highPriority = applicationService.listHighPriority().size();

            StringBuilder sb = new StringBuilder();
            sb.append("📊 **求职统计报告**\n\n");
            sb.append("**总体概况**\n");
            sb.append("- 总申请数：").append(total).append("\n");
            sb.append("- 高优先级申请：").append(highPriority).append("\n\n");

            sb.append("**状态分布**\n");
            for (Object stat : stats) {
                Map<?, ?> map = (Map<?, ?>) stat;
                sb.append(String.format("- %s：%d 个\n",
                        map.get("status"), map.get("count")));
            }

            sb.append("\n**建议**\n");
            if (highPriority > 0) {
                sb.append("- 你有 ").append(highPriority).append(" 个高优先级申请，建议重点跟进\n");
            }
            long appliedCount = allApplications.stream()
                    .filter(a -> "APPLIED".equals(a.getStatus()))
                    .count();
            if (appliedCount > 5) {
                sb.append("- 有 ").append(appliedCount).append(" 个申请待回复，建议准备面试\n");
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return "❌ 获取统计数据失败：" + e.getMessage();
        }
    }

    /**
     * 辅助方法：根据公司ID获取公司名称
     *
     * @param companyId 公司ID
     * @return 公司名称，如果不存在返回"未知公司"
     */
    private String getCompanyName(Long companyId) {
        if (companyId == null) {
            return "未知公司";
        }
        try {
            com.jobtracker.entity.Company company = companyService.getById(companyId);
            return company != null ? company.getName() : "未知公司";
        } catch (Exception e) {
            log.warn("获取公司名称失败：companyId={}", companyId, e);
            return "未知公司";
        }
    }
}
