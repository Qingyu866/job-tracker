package com.jobtracker.agent.tools.command;

import com.jobtracker.agent.tools.shared.ToolConstants;
import com.jobtracker.agent.tools.shared.ToolHelper;
import com.jobtracker.agent.tools.shared.ToolResult;
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

/**
 * 求职申请命令工具类（关键字优先设计）
 * <p>
 * 提供 LangChain4j Agent 可调用的申请操作方法
 * 所有更新/删除操作使用 keyword 参数替代 ID
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationCommandTools {

    private final ApplicationService applicationService;
    private final CompanyService companyService;
    private final ToolHelper toolHelper;

    /**
     * 创建求职申请
     */
    @Tool("""
        [创建] 新建求职申请

        适用场景：用户想要记录一个新的求职申请

        必填参数：
        - companyName: 公司名称
        - jobTitle: 职位名称

        可选参数：
        - jobType: 工作类型 FULL_TIME/PART_TIME/CONTRACT/INTERNSHIP
        - workLocation: 工作地点
        - salaryMin: 薪资下限（数字）
        - salaryMax: 薪资上限（数字）
        - status: 初始状态 WISHLIST（默认）/APPLIED
        - priority: 优先级 1-10（默认5）
        - notes: 备注信息

        返回：创建结果，包含新申请ID
        """)
    public ToolResult createApplication(
            String companyName,
            String jobTitle,
            String jobType,
            String workLocation,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            String status,
            Integer priority,
            String notes
    ) {
        log.info("AI调用：创建申请 company={}, job={}", companyName, jobTitle);

        // 参数校验
        if (companyName == null || companyName.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "公司名称不能为空");
        }
        if (jobTitle == null || jobTitle.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "职位名称不能为空");
        }

        try {
            // 查找或创建公司
            Company company = companyService.getByName(companyName);
            if (company == null) {
                company = Company.builder().name(companyName).build();
                companyService.save(company);
                log.info("自动创建公司：{}", companyName);
            }

            // 创建申请
            JobApplication app = new JobApplication();
            app.setCompanyId(company.getId());
            app.setJobTitle(jobTitle);
            app.setJobType(jobType);
            app.setWorkLocation(workLocation);
            app.setSalaryMin(salaryMin);
            app.setSalaryMax(salaryMax);
            app.setStatus(status != null ? status : ToolConstants.STATUS_WISHLIST);
            app.setPriority(priority != null ? priority : ToolConstants.DEFAULT_PRIORITY);
            app.setNotes(notes);

            if (ToolConstants.STATUS_APPLIED.equals(app.getStatus())) {
                app.setApplicationDate(LocalDate.now());
            }

            applicationService.save(app);

            return ToolResult.success(
                String.format("✅ 成功创建申请：%s - %s（ID: %d）", companyName, jobTitle, app.getId()),
                app
            );
        } catch (Exception e) {
            log.error("创建申请失败", e);
            return ToolResult.error(ToolConstants.ERR_CREATE_FAILED, "创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新申请状态（关键字优先）
     */
    @Tool("""
        [更新] 通过关键词修改求职申请状态

        适用场景：
        - 用户说"把字节跳动的申请改为面试中"
        - 用户说"腾讯的申请拿到offer了"
        - 用户说"阿里巴巴那个不用跟了，撤回吧"

        参数：
        - keyword: 公司名称或职位关键词（必填）
          例如："字节跳动"、"前端工程师"、"字节-前端"
        - newStatus: 新状态（必填）
          WISHLIST/APPLIED/INTERVIEW/OFFER/REJECTED/WITHDRAWN

        返回：
        - 唯一匹配：直接更新，返回成功
        - 多个匹配：返回列表，提示用户选择
        - 无匹配：返回错误
        """)
    public ToolResult updateApplicationStatus(String keyword, String newStatus) {
        log.info("AI调用：更新申请状态 keyword={}, status={}", keyword, newStatus);

        if (keyword == null || keyword.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供公司名称或职位关键词");
        }
        if (newStatus == null || newStatus.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "新状态不能为空");
        }

        try {
            // 智能匹配申请
            ToolResult matchResult = toolHelper.smartMatchApplication(keyword);

            if (!matchResult.isSuccess()) {
                return matchResult; // 无匹配，返回错误
            }

            // 检查是否需要用户选择
            if (matchResult.isMultipleMatch()) {
                return ToolResult.multipleMatch(
                    "找到多个申请，请告诉我具体是哪个？\n" + toolHelper.formatApplicationList((List<JobApplication>) matchResult.getData()),
                    matchResult.getData()
                );
            }

            // 唯一匹配，执行更新
            JobApplication app = (JobApplication) matchResult.getData();
            applicationService.updateStatus(app.getId(), newStatus);

            return ToolResult.success(
                String.format("✅ 状态已更新：%s - %s → %s",
                    toolHelper.getCompanyName(app.getCompanyId()),
                    app.getJobTitle(),
                    ToolConstants.getStatusDescription(newStatus))
            );
        } catch (IllegalArgumentException e) {
            return ToolResult.error(ToolConstants.ERR_UPDATE_FAILED, e.getMessage());
        } catch (Exception e) {
            log.error("更新状态失败", e);
            return ToolResult.error(ToolConstants.ERR_UPDATE_FAILED, "更新失败：" + e.getMessage());
        }
    }

    /**
     * 更新申请详细信息（关键字优先）
     */
    @Tool("""
        [更新] 通过关键词修改求职申请详细信息

        适用场景：修改申请的备注、优先级等信息

        参数：
        - keyword: 公司名称或职位关键词（必填）
        - priority: 优先级 1-10（可选）
        - notes: 备注信息（可选）

        返回：更新结果
        """)
    public ToolResult updateApplicationDetails(
            String keyword,
            Integer priority,
            String notes
    ) {
        log.info("AI调用：更新申请详情 keyword={}", keyword);

        if (keyword == null || keyword.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供公司名称或职位关键词");
        }

        try {
            ToolResult matchResult = toolHelper.smartMatchApplication(keyword);

            if (!matchResult.isSuccess()) {
                return matchResult;
            }

            if (matchResult.isMultipleMatch()) {
                return ToolResult.multipleMatch(
                    "找到多个申请，请告诉我具体是哪个？\n" + toolHelper.formatApplicationList((List<JobApplication>) matchResult.getData()),
                    matchResult.getData()
                );
            }

            JobApplication app = (JobApplication) matchResult.getData();

            if (priority != null) {
                app.setPriority(priority);
            }
            if (notes != null) {
                app.setNotes(notes);
            }

            applicationService.updateById(app);

            return ToolResult.success(
                String.format("✅ 已更新：%s - %s",
                    toolHelper.getCompanyName(app.getCompanyId()),
                    app.getJobTitle())
            );
        } catch (Exception e) {
            log.error("更新申请失败", e);
            return ToolResult.error(ToolConstants.ERR_UPDATE_FAILED, "更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除申请（关键字优先）
     */
    @Tool("""
        [删除] 通过关键词删除求职申请

        ⚠️ 注意：
        - 如果申请下有面试记录，需要先删除面试记录
        - 删除操作不可恢复，请确认

        参数：
        - keyword: 公司名称或职位关键词（必填）

        返回：删除结果或确认提示
        """)
    public ToolResult deleteApplication(String keyword) {
        log.info("AI调用：删除申请 keyword={}", keyword);

        if (keyword == null || keyword.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供公司名称或职位关键词");
        }

        try {
            ToolResult matchResult = toolHelper.smartMatchApplication(keyword);

            if (!matchResult.isSuccess()) {
                return matchResult;
            }

            if (matchResult.isMultipleMatch()) {
                return ToolResult.multipleMatch(
                    "找到多个申请，请告诉我具体要删除哪个？\n" + toolHelper.formatApplicationList((List<JobApplication>) matchResult.getData()),
                    matchResult.getData()
                );
            }

            JobApplication app = (JobApplication) matchResult.getData();
            String displayName = toolHelper.getCompanyName(app.getCompanyId()) + " - " + app.getJobTitle();

            boolean success = applicationService.removeById(app.getId());
            if (success) {
                return ToolResult.success("✅ 已删除：" + displayName);
            } else {
                return ToolResult.error(ToolConstants.ERR_DELETE_FAILED, "删除失败");
            }
        } catch (Exception e) {
            log.error("删除申请失败", e);
            return ToolResult.error(ToolConstants.ERR_DELETE_FAILED, "删除失败：" + e.getMessage());
        }
    }
}
