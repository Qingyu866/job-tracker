package com.jobtracker.agent.tools.command;

import com.jobtracker.agent.tools.shared.ToolConstants;
import com.jobtracker.agent.tools.shared.ToolHelper;
import com.jobtracker.agent.tools.shared.ToolResult;
import com.jobtracker.entity.Company;
import com.jobtracker.service.CompanyService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 公司命令工具类（关键字优先设计）
 * <p>
 * 提供 LangChain4j Agent 可调用的公司操作方法
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyCommandTools {

    private final CompanyService companyService;
    private final ToolHelper toolHelper;

    /**
     * 创建公司
     */
    @Tool("""
        [创建] 新建公司信息

        适用场景：用户想要添加一个新公司到系统

        必填参数：
        - name: 公司名称

        可选参数：
        - industry: 行业类型
        - size: 公司规模（如：100-499人）
        - location: 公司地址
        - website: 公司官网
        - description: 公司描述

        返回：创建结果
        """)
    public ToolResult createCompany(
            String name,
            String industry,
            String size,
            String location,
            String website,
            String description
    ) {
        log.info("AI调用：创建公司 name={}", name);

        if (name == null || name.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "公司名称不能为空");
        }

        try {
            // 检查是否已存在
            Company existing = companyService.getByName(name);
            if (existing != null) {
                return ToolResult.error(ToolConstants.ERR_CREATE_FAILED,
                    String.format("公司已存在：%s（ID: %d）", name, existing.getId()));
            }

            Company company = Company.builder()
                    .name(name)
                    .industry(industry)
                    .size(size)
                    .location(location)
                    .website(website)
                    .description(description)
                    .build();

            companyService.save(company);

            return ToolResult.success(
                String.format("✅ 成功创建公司：%s（ID: %d）", name, company.getId()),
                company
            );
        } catch (Exception e) {
            log.error("创建公司失败", e);
            return ToolResult.error(ToolConstants.ERR_CREATE_FAILED, "创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新公司信息（关键字优先）
     */
    @Tool("""
        [更新] 修改公司信息

        适用场景：用户说"更新一下字节跳动的信息"、"把阿里的行业改成互联网"

        参数：
        - keyword: 公司名称关键词（必填）
        - industry: 行业类型（可选）
        - size: 公司规模（可选）
        - location: 公司地址（可选）
        - website: 公司官网（可选）
        - description: 公司描述（可选）

        返回：更新结果
        """)
    public ToolResult updateCompany(
            String keyword,
            String industry,
            String size,
            String location,
            String website,
            String description
    ) {
        log.info("AI调用：更新公司 keyword={}", keyword);

        if (keyword == null || keyword.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供公司名称");
        }

        try {
            ToolResult matchResult = toolHelper.smartMatchCompany(keyword);

            if (!matchResult.isSuccess()) {
                return matchResult;
            }

            if (matchResult.isMultipleMatch()) {
                @SuppressWarnings("unchecked")
                List<Company> companies = (List<Company>) matchResult.getData();
                StringBuilder sb = new StringBuilder("找到多个公司，请告诉我具体是哪个？\n");
                for (int i = 0; i < companies.size(); i++) {
                    sb.append(String.format("%d. %s\n", i + 1, companies.get(i).getName()));
                }
                return ToolResult.multipleMatch(sb.toString(), companies);
            }

            Company company = (Company) matchResult.getData();

            // 更新字段
            if (industry != null) company.setIndustry(industry);
            if (size != null) company.setSize(size);
            if (location != null) company.setLocation(location);
            if (website != null) company.setWebsite(website);
            if (description != null) company.setDescription(description);

            companyService.updateById(company);

            return ToolResult.success(String.format("✅ 已更新公司信息：%s", company.getName()));
        } catch (Exception e) {
            log.error("更新公司失败", e);
            return ToolResult.error(ToolConstants.ERR_UPDATE_FAILED, "更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除公司（关键字优先）
     */
    @Tool("""
        [删除] 删除公司

        ⚠️ 注意：
        - 如果公司下有申请记录，需要先删除申请
        - 删除操作不可恢复

        参数：
        - keyword: 公司名称关键词（必填）

        返回：删除结果
        """)
    public ToolResult deleteCompany(String keyword) {
        log.info("AI调用：删除公司 keyword={}", keyword);

        if (keyword == null || keyword.isBlank()) {
            return ToolResult.error(ToolConstants.ERR_PARAM_MISSING, "请提供公司名称");
        }

        try {
            ToolResult matchResult = toolHelper.smartMatchCompany(keyword);

            if (!matchResult.isSuccess()) {
                return matchResult;
            }

            if (matchResult.isMultipleMatch()) {
                @SuppressWarnings("unchecked")
                List<Company> companies = (List<Company>) matchResult.getData();
                StringBuilder sb = new StringBuilder("找到多个公司，请告诉我具体要删除哪个？\n");
                for (int i = 0; i < companies.size(); i++) {
                    sb.append(String.format("%d. %s\n", i + 1, companies.get(i).getName()));
                }
                return ToolResult.multipleMatch(sb.toString(), companies);
            }

            Company company = (Company) matchResult.getData();

            boolean success = companyService.removeById(company.getId());
            if (success) {
                return ToolResult.success(String.format("✅ 已删除公司：%s", company.getName()));
            } else {
                return ToolResult.error(ToolConstants.ERR_DELETE_FAILED, "删除失败");
            }
        } catch (Exception e) {
            log.error("删除公司失败", e);
            return ToolResult.error(ToolConstants.ERR_DELETE_FAILED, "删除失败：" + e.getMessage());
        }
    }
}
