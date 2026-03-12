package com.jobtracker.agent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.entity.Company;
import com.jobtracker.service.CompanyService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 公司信息工具类
 * <p>
 * 提供 LangChain4j Agent 可以调用的公司信息相关方法
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
public class CompanyTools {

    private final CompanyService companyService;
    private final ObjectMapper objectMapper;

    /**
     * 创建公司信息
     * <p>
     * 创建新的公司记录，如果公司已存在则更新现有记录
     * </p>
     *
     * @param name        公司名称（必填）
     * @param industry    行业类型（可选）
     * @param size        公司规模（可选，如：100-499人）
     * @param location    公司地址（可选）
     * @param website     公司官网（可选）
     * @param description 公司描述（可选）
     * @return 操作结果的描述性消息
     */
    @Tool("""
            【仅用于创建/添加新公司】当用户想要添加一个新公司到系统时使用。
            适用场景：用户明确说"创建"、"添加"、"新增"公司时才调用此工具。

            不要与查询混淆！如果用户说"查看"、"有哪些"、"列出"等，应使用 queryCompanies 工具。

            参数：公司名称（必填）、行业类型、公司规模、地址、官网、描述
            """)
    public String createCompany(
            String name,
            String industry,
            String size,
            String location,
            String website,
            String description
    ) {
        try {
            log.info("AI 调用创建公司：name={}", name);

            if (name == null || name.trim().isEmpty()) {
                return "创建失败：公司名称不能为空";
            }

            Company company = Company.builder()
                    .name(name)
                    .industry(industry)
                    .size(size)
                    .location(location)
                    .website(website)
                    .description(description)
                    .build();

            Company result = companyService.createOrUpdate(company);

            return String.format("✅ 成功%s公司信息：%s（ID: %d）",
                    result.getId().equals(company.getId()) ? "创建" : "更新",
                    name,
                    result.getId());
        } catch (Exception e) {
            log.error("创建公司失败", e);
            return "❌ 创建失败：" + e.getMessage();
        }
    }

    /**
     * 查询公司列表
     * <p>
     * 根据条件查询公司信息
     * </p>
     *
     * @param industry 按行业筛选（可选）
     * @param location 按地址筛选（可选）
     * @param keyword  按名称模糊搜索（可选）
     * @return 查询结果的描述性消息
     */
    @Tool("""
            【查看/搜索已有公司】当用户想要查看、浏览、搜索系统中的公司时使用。
            适用场景：用户说"查看"、"有哪些"、"列出"、"搜索"、"看看"等关键词时调用此工具。

            注意：这是查询工具，不是创建工具！

            参数（全部可选）：按行业筛选、按地址筛选、按名称关键词搜索
            如果不提供任何参数，将返回所有公司

            返回格式：JSON 数组，包含所有匹配公司的详细信息
            """)
    public String queryCompanies(
            String industry,
            String location,
            String keyword
    ) {
        try {
            log.info("AI 调用查询公司：industry={}, location={}, keyword={}",
                    industry, location, keyword);

            List<Company> companies;

            if (keyword != null && !keyword.trim().isEmpty()) {
                companies = companyService.searchByName(keyword);
            } else if (industry != null && !industry.trim().isEmpty()) {
                companies = companyService.listByIndustry(industry);
            } else if (location != null && !location.trim().isEmpty()) {
                companies = companyService.listByLocation(location);
            } else {
                companies = companyService.list();
            }

            if (companies.isEmpty()) {
                return "[]";
            }

            // 使用 ObjectMapper 序列化为 JSON
            return objectMapper.writeValueAsString(companies);
        } catch (Exception e) {
            log.error("查询公司失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }
}
