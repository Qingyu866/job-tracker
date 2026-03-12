package com.jobtracker.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.mapper.InterviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试记录服务类
 * <p>
 * 提供面试记录管理的业务逻辑，包括：
 * - 基础 CRUD 操作（继承自 ServiceImpl）
 * - 面试状态管理
 * - 面试查询和筛选
 * - 面试跟进管理
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
public class InterviewService extends ServiceImpl<InterviewMapper, InterviewRecord> {

    private final ApplicationLogService applicationLogService;

    /**
     * 根据申请ID查询所有面试记录
     *
     * @param applicationId 申请ID
     * @return 面试记录列表
     */
    public List<InterviewRecord> listByApplicationId(Long applicationId) {
        log.info("查询申请的面试记录：applicationId={}", applicationId);
        return baseMapper.selectByApplicationId(applicationId);
    }

    /**
     * 查询指定日期范围内的面试
     *
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 面试记录列表
     */
    public List<InterviewRecord> listByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("查询日期范围内的面试：startDate={}, endDate={}", startDate, endDate);
        return baseMapper.selectByDateRange(startDate, endDate);
    }

    /**
     * 查询即将进行的面试（状态为 SCHEDULED）
     *
     * @return 即将进行的面试列表
     */
    public List<InterviewRecord> listUpcoming() {
        log.info("查询即将进行的面试");
        return baseMapper.selectUpcomingInterviews();
    }

    /**
     * 根据状态查询面试记录
     *
     * @param status 面试状态
     * @return 面试记录列表
     */
    public List<InterviewRecord> listByStatus(String status) {
        log.info("按状态查询面试：status={}", status);
        return baseMapper.selectByStatus(status);
    }

    /**
     * 查询需要跟进的面试
     *
     * @return 需要跟进的面试列表
     */
    public List<InterviewRecord> listFollowUpRequired() {
        log.info("查询需要跟进的面试");
        return baseMapper.selectFollowUpRequired();
    }

    /**
     * 查询最近N天内的面试
     *
     * @param days 天数
     * @return 面试记录列表
     */
    public List<InterviewRecord> listRecent(int days) {
        log.info("查询最近{}天的面试", days);
        return baseMapper.selectRecentInterviews(days);
    }

    /**
     * 标记面试为已完成
     *
     * @param id 面试记录ID
     * @param rating 面试评分（1-5分）
     * @param feedback 面试反馈
     * @return 更新成功返回 true，否则返回 false
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsCompleted(Long id, Integer rating, String feedback) {
        log.info("标记面试为已完成：id={}, rating={}", id, rating);

        InterviewRecord record = getById(id);
        if (record == null) {
            log.error("面试记录不存在：id={}", id);
            return false;
        }

        if (rating != null && (rating < 1 || rating > 5)) {
            log.error("无效的评分：{}", rating);
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        record.setStatus("COMPLETED");
        record.setRating(rating);
        record.setFeedback(feedback);

        boolean result = updateById(record);

        if (result) {
            log.info("面试标记为已完成：id={}", id);
            // 记录面试完成日志
            applicationLogService.createInterviewCompletedLog(
                record.getApplicationId(),
                record.getInterviewType(),
                rating
            );
            // 如果有反馈，记录反馈日志
            if (feedback != null && !feedback.isEmpty()) {
                applicationLogService.createFeedbackLog(record.getApplicationId(), feedback);
            }
        }

        return result;
    }

    /**
     * 取消面试
     *
     * @param id 面试记录ID
     * @return 更新成功返回 true，否则返回 false
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelInterview(Long id) {
        log.info("取消面试：id={}", id);

        InterviewRecord record = getById(id);
        if (record == null) {
            log.error("面试记录不存在：id={}", id);
            return false;
        }

        record.setStatus("CANCELLED");
        boolean result = updateById(record);

        if (result) {
            log.info("面试已取消：id={}", id);
            // 记录面试取消日志
            applicationLogService.createInterviewCancelledLog(
                record.getApplicationId(),
                record.getInterviewType()
            );
        }

        return result;
    }

    /**
     * 标记面试为未参加
     *
     * @param id 面试记录ID
     * @return 更新成功返回 true，否则返回 false
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsNoShow(Long id) {
        log.info("标记面试为未参加：id={}", id);

        InterviewRecord record = getById(id);
        if (record == null) {
            log.error("面试记录不存在：id={}", id);
            return false;
        }

        record.setStatus("NO_SHOW");
        boolean result = updateById(record);

        if (result) {
            log.info("面试标记为未参加：id={}", id);
            // 记录面试未参加日志
            applicationLogService.createInterviewNoShowLog(
                record.getApplicationId(),
                record.getInterviewType()
            );
        }

        return result;
    }

    /**
     * 设置需要跟进标记
     *
     * @param id 面试记录ID
     * @param followUpRequired 是否需要跟进
     * @return 更新成功返回 true，否则返回 false
     */
    public boolean setFollowUpRequired(Long id, boolean followUpRequired) {
        log.info("设置面试跟进标记：id={}, followUpRequired={}", id, followUpRequired);

        InterviewRecord record = getById(id);
        if (record == null) {
            log.error("面试记录不存在：id={}", id);
            return false;
        }

        record.setFollowUpRequired(followUpRequired);
        return updateById(record);
    }

    /**
     * 更新面试反馈
     *
     * @param id 面试记录ID
     * @param feedback 反馈内容
     * @return 更新成功返回 true，否则返回 false
     */
    public boolean updateFeedback(Long id, String feedback) {
        log.info("更新面试反馈：id={}", id);

        InterviewRecord record = getById(id);
        if (record == null) {
            log.error("面试记录不存在：id={}", id);
            return false;
        }

        record.setFeedback(feedback);
        return updateById(record);
    }

    /**
     * 更新技术问题记录
     *
     * @param id 面试记录ID
     * @param technicalQuestions 技术问题记录
     * @return 更新成功返回 true，否则返回 false
     */
    public boolean updateTechnicalQuestions(Long id, String technicalQuestions) {
        log.info("更新技术问题记录：id={}", id);

        InterviewRecord record = getById(id);
        if (record == null) {
            log.error("面试记录不存在：id={}", id);
            return false;
        }

        record.setTechnicalQuestions(technicalQuestions);
        return updateById(record);
    }
}
