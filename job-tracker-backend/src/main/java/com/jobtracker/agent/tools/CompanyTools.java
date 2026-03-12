package com.jobtracker.agent.tools;

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
    @Tool("创建或更新公司信息。参数：公司名称、行业类型、公司规模、地址、官网、描述")
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
    @Tool("查询公司列表。参数：行业筛选、地址筛选、名称关键词")
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
                return "📋 没有找到符合条件的公司";
            }

            // 格式化输出
            StringBuilder sb = new StringBuilder();
            sb.append("📋 找到 ").append(companies.size()).append(" 家公司：\n\n");

            for (Company company : companies) {
                sb.append(String.format("""
                        **%s** (ID: %d)
                        - 行业：%s
                        - 规模：%s
                        - 地址：%s
                        """,
                        company.getName(),
                        company.getId(),
                        company.getIndustry() != null ? company.getIndustry() : "未指定",
                        company.getSize() != null ? company.getSize() : "未指定",
                        company.getLocation() != null ? company.getLocation() : "未指定"
                ));

                if (company.getWebsite() != null && !company.getWebsite().trim().isEmpty()) {
                    sb.append("- 官网：").append(company.getWebsite()).append("\n");
                }
                if (company.getDescription() != null && !company.getDescription().trim().isEmpty()) {
                    sb.append("- 描述：").append(company.getDescription()).append("\n");
                }
                sb.append("\n");
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("查询公司失败", e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }
}
