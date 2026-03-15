package com.jobtracker.agent.tools.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.agent.tools.shared.ToolConstants;
import com.jobtracker.agent.tools.shared.ToolResult;
import com.jobtracker.entity.Company;
import com.jobtracker.service.CompanyService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 公司查询工具类
 * <p>
 * 提供 LangChain4j Agent 可调用的公司查询方法
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyQueryTools {

    private final CompanyService companyService;
    private final ObjectMapper objectMapper;

    /**
     * 根据ID获取公司详情
     */
    @Tool("""
        [查询] 获取公司详情

        适用场景：用户想查看某个公司的详细信息

        参数：
        - companyId: 公司ID（必填，数字类型）

        返回：公司的详细信息
        """)
    public ToolResult getCompanyById(Long companyId) {
        log.info("AI调用：获取公司详情 id={}", companyId);

        if (companyId == null) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "公司ID不能为空");
        }

        Company company = companyService.getById(companyId);
        if (company == null) {
            return ToolResult.error(ToolConstants.ERR_NOT_FOUND, "公司不存在，ID: " + companyId);
        }

        StringBuilder info = new StringBuilder();
        info.append("**公司详情**\n\n");
        info.append(String.format("- 名称：%s\n", company.getName()));
        if (company.getIndustry() != null) {
            info.append(String.format("- 行业：%s\n", company.getIndustry()));
        }
        if (company.getSize() != null) {
            info.append(String.format("- 规模：%s\n", company.getSize()));
        }
        if (company.getLocation() != null) {
            info.append(String.format("- 地址：%s\n", company.getLocation()));
        }
        if (company.getWebsite() != null) {
            info.append(String.format("- 官网：%s\n", company.getWebsite()));
        }
        if (company.getDescription() != null) {
            info.append(String.format("- 描述：%s\n", company.getDescription()));
        }

        return ToolResult.success(info.toString(), company);
    }

    /**
     * 获取公司列表
     */
    @Tool("""
        [查询] 获取公司列表

        适用场景：
        - 用户想查看所有公司
        - 按行业或地区筛选

        参数（全部可选）：
        - industry: 按行业筛选
        - location: 按地址筛选
        - limit: 返回数量限制（默认20）

        返回：公司列表
        """)
    public ToolResult listCompanies(String industry, String location, Integer limit) {
        log.info("AI调用：查询公司列表 industry={}, location={}", industry, location);

        limit = limit != null ? Math.min(limit, ToolConstants.MAX_PAGE_SIZE) : 20;

        List<Company> companies;

        if (industry != null && !industry.isBlank()) {
            companies = companyService.listByIndustry(industry);
        } else if (location != null && !location.isBlank()) {
            companies = companyService.listByLocation(location);
        } else {
            companies = companyService.list();
        }

        if (companies.size() > limit) {
            companies = companies.subList(0, limit);
        }

        if (companies.isEmpty()) {
            return ToolResult.info("没有找到符合条件的公司", List.of());
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("🏢 找到 %d 家公司：\n\n", companies.size()));

        for (Company company : companies) {
            summary.append(String.format("- **%s**", company.getName()));
            if (company.getIndustry() != null) {
                summary.append(String.format(" | %s", company.getIndustry()));
            }
            if (company.getLocation() != null) {
                summary.append(String.format(" | %s", company.getLocation()));
            }
            summary.append("\n");
        }

        return ToolResult.info(summary.toString(), companies);
    }

    /**
     * 搜索公司
     */
    @Tool("""
        [查询] 搜索公司

        适用场景：用户说"帮我找一下字节"、"查看阿里相关的公司"

        参数：
        - keyword: 公司名称关键词

        返回：匹配的公司列表
        """)
    public ToolResult searchCompanies(String keyword) {
        log.info("AI调用：搜索公司 keyword={}", keyword);

        if (keyword == null || keyword.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供搜索关键词");
        }

        List<Company> companies = companyService.searchByName(keyword);

        if (companies.isEmpty()) {
            return ToolResult.error(ToolConstants.ERR_NOT_FOUND, "未找到匹配的公司：" + keyword);
        }

        if (companies.size() == 1) {
            Company company = companies.get(0);
            return ToolResult.success(
                String.format("找到公司：%s", company.getName()),
                company
            );
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("🔍 找到 %d 家公司：\n\n", companies.size()));

        for (int i = 0; i < companies.size(); i++) {
            Company company = companies.get(i);
            summary.append(String.format("%d. %s\n", i + 1, company.getName()));
        }

        return ToolResult.multipleMatch(summary.toString(), companies);
    }
}
