package com.jobtracker.controller;

import com.jobtracker.common.result.Result;
import com.jobtracker.entity.Company;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.service.ApplicationService;
import com.jobtracker.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公司信息控制器
 * <p>
 * 提供公司信息的 CRUD 操作接口
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-18
 */
@Slf4j
@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final ApplicationService applicationService;

    /**
     * 获取所有公司
     *
     * @return 公司列表
     */
    @GetMapping
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
    @GetMapping("/{id}")
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
    @GetMapping("/name")
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
     * 模糊搜索公司
     *
     * @param keyword 搜索关键词
     * @return 公司列表
     */
    @GetMapping("/search")
    public Result<List<Company>> searchCompanies(@RequestParam String keyword) {
        try {
            List<Company> companies = companyService.searchByName(keyword);
            return Result.success("查询成功", companies);
        } catch (Exception e) {
            log.error("搜索公司失败：keyword={}", keyword, e);
            return Result.error("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 创建公司
     *
     * @param company 公司信息
     * @return 操作结果
     */
    @PostMapping
    public Result<Long> createCompany(@RequestBody Company company) {
        try {
            Company result = companyService.createOrUpdate(company);
            return Result.success("创建成功", result.getId());
        } catch (Exception e) {
            log.error("创建公司失败", e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新公司信息
     *
     * @param id 公司ID
     * @param company 更新的公司信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateCompany(
            @PathVariable Long id,
            @RequestBody Company company
    ) {
        try {
            Company existing = companyService.getById(id);
            if (existing == null) {
                return Result.error("公司不存在");
            }

            company.setId(id);
            company.setCreatedAt(existing.getCreatedAt()); // 保留创建时间
            boolean success = companyService.updateById(company);

            if (success) {
                log.info("公司更新成功：id={}", id);
                return Result.success("更新成功", true);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新公司失败：id={}", id, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除公司（保护性删除）
     * <p>
     * 如果该公司下有关联的申请记录，则阻止删除并提示用户
     * </p>
     *
     * @param id 公司ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCompany(@PathVariable Long id) {
        try {
            Company company = companyService.getById(id);
            if (company == null) {
                return Result.error("公司不存在");
            }

            // 检查是否有关联的申请记录
            List<JobApplication> applications = applicationService.listByCompanyId(id);
            if (!applications.isEmpty()) {
                return Result.error("该公司下有 " + applications.size() + " 条申请记录，请先删除申请记录");
            }

            // 执行逻辑删除
            boolean success = companyService.removeById(id);
            if (success) {
                log.info("公司删除成功：id={}", id);
                return Result.success("删除成功", true);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除公司失败：id={}", id, e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
