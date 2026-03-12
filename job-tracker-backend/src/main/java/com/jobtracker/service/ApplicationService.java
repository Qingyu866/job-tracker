package com.jobtracker.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jobtracker.constants.ApplicationStatus;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.mapper.ApplicationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 求职申请服务类
 * <p>
 * 提供求职申请管理的业务逻辑，包括：
 * - 基础 CRUD 操作（继承自 ServiceImpl）
 * - 状态变更管理
 * - 优先级管理
 * - 高级查询（按状态、日期范围、优先级等）
 * - 统计分析
 * - 自动日志记录
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService extends ServiceImpl<ApplicationMapper, JobApplication> {

    private final ApplicationLogService applicationLogService;

    /**
     * 根据公司ID查询所有申请
     *
     * @param companyId 公司ID
     * @return 申请列表
     */
    public List<JobApplication> listByCompanyId(Long companyId) {
        log.info("查询公司的所有申请：companyId={}", companyId);
        return baseMapper.selectByCompanyId(companyId);
    }

    /**
     * 根据状态查询申请
     *
     * @param status 申请状态
     * @return 申请列表
     */
    public List<JobApplication> listByStatus(String status) {
        log.info("按状态查询申请：status={}", status);
        return baseMapper.selectByStatus(status);
    }

    /**
     * 根据状态分页查询申请
     *
     * @param pageNum 当前页
     * @param pageSize 每页大小
     * @param status 申请状态
     * @return 分页结果
     */
    public IPage<JobApplication> pageByStatus(int pageNum, int pageSize, String status) {
        log.info("分页查询申请：pageNum={}, pageSize={}, status={}", pageNum, pageSize, status);
        Page<JobApplication> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPageByStatus(page, status);
    }

    /**
     * 查询指定日期范围内的申请
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 申请列表
     */
    public List<JobApplication> listByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("查询日期范围内的申请：startDate={}, endDate={}", startDate, endDate);
        return baseMapper.selectByDateRange(startDate, endDate);
    }

    /**
     * 根据优先级查询申请
     *
     * @param priority 优先级
     * @return 申请列表
     */
    public List<JobApplication> listByPriority(Integer priority) {
        log.info("按优先级查询申请：priority={}", priority);
        return baseMapper.selectByPriority(priority);
    }

    /**
     * 查询高优先级申请（优先级 >= 8）
     *
     * @return 高优先级申请列表
     */
    public List<JobApplication> listHighPriority() {
        log.info("查询高优先级申请");
        return baseMapper.selectHighPriorityApplications();
    }

    /**
     * 根据职位名称模糊搜索
     *
     * @param keyword 关键词
     * @return 申请列表
     */
    public List<JobApplication> searchByJobTitle(String keyword) {
        log.info("搜索申请：keyword={}", keyword);
        return baseMapper.searchByJobTitle(keyword);
    }

    /**
     * 更新申请状态
     *
     * @param id 申请ID
     * @param newStatus 新状态
     * @return 更新成功返回 true，否则返回 false
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, String newStatus) {
        log.info("更新申请状态：id={}, newStatus={}", id, newStatus);

        // 验证状态是否有效
        if (!ApplicationStatus.isValid(newStatus)) {
            log.error("无效的申请状态：{}", newStatus);
            throw new IllegalArgumentException("Invalid application status: " + newStatus);
        }

        JobApplication application = getById(id);
        if (application == null) {
            log.error("申请不存在：id={}", id);
            return false;
        }

        String oldStatus = application.getStatus();
        application.setStatus(newStatus);
        boolean result = updateById(application);

        if (result) {
            log.info("申请状态更新成功：id={}, {} -> {}", id, oldStatus, newStatus);
            // 记录状态变更日志
            applicationLogService.createStatusChangeLog(id, oldStatus, newStatus);
        }

        return result;
    }

    /**
     * 更新申请优先级
     *
     * @param id 申请ID
     * @param priority 新优先级（1-10）
     * @return 更新成功返回 true，否则返回 false
     */
    public boolean updatePriority(Long id, Integer priority) {
        log.info("更新申请优先级：id={}, priority={}", id, priority);

        if (priority < 1 || priority > 10) {
            log.error("无效的优先级：{}", priority);
            throw new IllegalArgumentException("Priority must be between 1 and 10");
        }

        JobApplication application = getById(id);
        if (application == null) {
            log.error("申请不存在：id={}", id);
            return false;
        }

        application.setPriority(priority);
        return updateById(application);
    }

    /**
     * 统计各状态的申请数量
     *
     * @return 状态统计列表
     */
    public List<Object> countByStatus() {
        log.info("统计各状态申请数量");
        return baseMapper.countByStatus();
    }

    /**
     * 提交申请（从 WISHLIST 变更为 APPLIED）
     *
     * @param id 申请ID
     * @return 操作成功返回 true，否则返回 false
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean submitApplication(Long id) {
        log.info("提交申请：id={}", id);

        JobApplication application = getById(id);
        if (application == null) {
            log.error("申请不存在：id={}", id);
            return false;
        }

        if (!ApplicationStatus.WISHLIST.getCode().equals(application.getStatus())) {
            log.warn("申请状态不是 WISHLIST，无法提交：id={}, status={}", id, application.getStatus());
            return false;
        }

        application.setStatus(ApplicationStatus.APPLIED.getCode());
        application.setApplicationDate(LocalDate.now());
        boolean result = updateById(application);

        if (result) {
            log.info("申请提交成功：id={}", id);
            // 记录申请提交日志
            applicationLogService.createApplicationSubmittedLog(id, application.getJobTitle());
        }

        return result;
    }

    /**
     * 撤回申请
     *
     * @param id 申请ID
     * @return 操作成功返回 true，否则返回 false
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean withdrawApplication(Long id) {
        log.info("撤回申请：id={}", id);

        return updateStatus(id, ApplicationStatus.WITHDRAWN.getCode());
    }
}
