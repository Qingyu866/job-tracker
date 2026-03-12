package com.jobtracker.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jobtracker.entity.Company;
import com.jobtracker.mapper.CompanyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公司服务类
 * <p>
 * 提供公司信息管理的业务逻辑，包括：
 * - 基础 CRUD 操作（继承自 ServiceImpl）
 * - 公司名称查重
 * - 批量查询
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
public class CompanyService extends ServiceImpl<CompanyMapper, Company> {

    /**
     * 根据公司名称查询公司
     *
     * @param name 公司名称
     * @return 公司实体，如果不存在则返回 null
     */
    public Company getByName(String name) {
        log.info("查询公司：name={}", name);
        return lambdaQuery()
                .eq(Company::getName, name)
                .one();
    }

    /**
     * 检查公司名称是否已存在
     *
     * @param name 公司名称
     * @return 如果存在返回 true，否则返回 false
     */
    public boolean existsByName(String name) {
        return lambdaQuery()
                .eq(Company::getName, name)
                .exists();
    }

    /**
     * 根据行业查询公司列表
     *
     * @param industry 行业类型
     * @return 公司列表
     */
    public List<Company> listByIndustry(String industry) {
        log.info("按行业查询公司：industry={}", industry);
        return lambdaQuery()
                .eq(Company::getIndustry, industry)
                .list();
    }

    /**
     * 根据地址查询公司列表
     *
     * @param location 公司地址
     * @return 公司列表
     */
    public List<Company> listByLocation(String location) {
        log.info("按地址查询公司：location={}", location);
        return lambdaQuery()
                .eq(Company::getLocation, location)
                .list();
    }

    /**
     * 根据名称模糊搜索公司
     *
     * @param keyword 关键词
     * @return 公司列表
     */
    public List<Company> searchByName(String keyword) {
        log.info("模糊搜索公司：keyword={}", keyword);
        return lambdaQuery()
                .like(Company::getName, keyword)
                .list();
    }

    /**
     * 创建或更新公司（根据名称判断是否存在）
     * <p>
     * 如果公司名称已存在则更新，否则创建新公司
     * </p>
     *
     * @param company 公司实体
     * @return 保存后的公司实体
     */
    public Company createOrUpdate(Company company) {
        Company existing = getByName(company.getName());
        if (existing != null) {
            log.info("公司已存在，更新信息：id={}, name={}", existing.getId(), company.getName());
            company.setId(existing.getId());
            updateById(company);
            return company;
        } else {
            log.info("创建新公司：name={}", company.getName());
            save(company);
            return company;
        }
    }
}
