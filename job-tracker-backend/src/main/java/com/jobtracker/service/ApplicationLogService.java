package com.jobtracker.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jobtracker.entity.ApplicationLog;
import com.jobtracker.mapper.ApplicationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 申请日志服务类
 * <p>
 * 提供申请日志管理的业务逻辑，包括：
 * - 基础 CRUD 操作（继承自 ServiceImpl）
 * - 日志记录
 * - 日志查询和筛选
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
public class ApplicationLogService extends ServiceImpl<ApplicationLogMapper, ApplicationLog> {

    /**
     * 根据申请ID查询所有日志
     *
     * @param applicationId 申请ID
     * @return 日志列表
     */
    public List<ApplicationLog> listByApplicationId(Long applicationId) {
        log.info("查询申请日志：applicationId={}", applicationId);
        return baseMapper.selectByApplicationId(applicationId);
    }

    /**
     * 根据日志类型查询
     *
     * @param logType 日志类型
     * @return 日志列表
     */
    public List<ApplicationLog> listByLogType(String logType) {
        log.info("按类型查询日志：logType={}", logType);
        return baseMapper.selectByLogType(logType);
    }

    /**
     * 根据申请ID和日志类型查询
     *
     * @param applicationId 申请ID
     * @param logType 日志类型
     * @return 日志列表
     */
    public List<ApplicationLog> listByApplicationIdAndType(Long applicationId, String logType) {
        log.info("查询申请日志：applicationId={}, logType={}", applicationId, logType);
        return baseMapper.selectByApplicationIdAndType(applicationId, logType);
    }

    /**
     * 查询最近的日志记录
     *
     * @param limit 限制数量
     * @return 最近日志列表
     */
    public List<ApplicationLog> listRecent(int limit) {
        log.info("查询最近日志：limit={}", limit);
        return baseMapper.selectRecentLogs(limit);
    }

    /**
     * 根据记录者查询日志
     *
     * @param loggedBy 记录者（SYSTEM/USER）
     * @return 日志列表
     */
    public List<ApplicationLog> listByLoggedBy(String loggedBy) {
        log.info("按记录者查询日志：loggedBy={}", loggedBy);
        return baseMapper.selectByLoggedBy(loggedBy);
    }

    /**
     * 创建状态变更日志
     *
     * @param applicationId 申请ID
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @return 保存的日志实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ApplicationLog createStatusChangeLog(Long applicationId, String oldStatus, String newStatus) {
        log.info("创建状态变更日志：applicationId={}, {} -> {}", applicationId, oldStatus, newStatus);

        ApplicationLog log = new ApplicationLog();
        log.setApplicationId(applicationId);
        log.setLogType("STATUS_CHANGE");
        log.setLogTitle("状态变更");
        log.setLogContent(String.format("申请状态从 %s 变更为 %s", oldStatus, newStatus));
        log.setLoggedBy("SYSTEM");

        save(log);
        return log;
    }

    /**
     * 创建面试安排日志
     *
     * @param applicationId 申请ID
     * @param interviewType 面试类型
     * @param interviewDate 面试时间
     * @return 保存的日志实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ApplicationLog createInterviewScheduledLog(Long applicationId, String interviewType, String interviewDate) {
        log.info("创建面试安排日志：applicationId={}, type={}, date={}", applicationId, interviewType, interviewDate);

        ApplicationLog log = new ApplicationLog();
        log.setApplicationId(applicationId);
        log.setLogType("INTERVIEW_SCHEDULED");
        log.setLogTitle("面试已安排");
        log.setLogContent(String.format("已安排 %s 面试，时间：%s", interviewType, interviewDate));
        log.setLoggedBy("SYSTEM");

        save(log);
        return log;
    }

    /**
     * 创建反馈日志
     *
     * @param applicationId 申请ID
     * @param feedback 反馈内容
     * @return 保存的日志实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ApplicationLog createFeedbackLog(Long applicationId, String feedback) {
        log.info("创建反馈日志：applicationId={}", applicationId);

        ApplicationLog log = new ApplicationLog();
        log.setApplicationId(applicationId);
        log.setLogType("FEEDBACK_RECEIVED");
        log.setLogTitle("收到反馈");
        log.setLogContent(feedback);
        log.setLoggedBy("SYSTEM");

        save(log);
        return log;
    }

    /**
     * 创建备注日志
     *
     * @param applicationId 申请ID
     * @param notes 备注内容
     * @return 保存的日志实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ApplicationLog createNoteAddedLog(Long applicationId, String notes) {
        log.info("创建备注日志：applicationId={}", applicationId);

        ApplicationLog log = new ApplicationLog();
        log.setApplicationId(applicationId);
        log.setLogType("NOTE_ADDED");
        log.setLogTitle("添加备注");
        log.setLogContent(notes);
        log.setLoggedBy("USER");

        save(log);
        return log;
    }

    /**
     * 创建文档上传日志
     *
     * @param applicationId 申请ID
     * @param documentName 文档名称
     * @return 保存的日志实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ApplicationLog createDocumentUploadedLog(Long applicationId, String documentName) {
        log.info("创建文档上传日志：applicationId={}, document={}", applicationId, documentName);

        ApplicationLog log = new ApplicationLog();
        log.setApplicationId(applicationId);
        log.setLogType("DOCUMENT_UPLOADED");
        log.setLogTitle("文档已上传");
        log.setLogContent(String.format("已上传文档：%s", documentName));
        log.setLoggedBy("USER");

        save(log);
        return log;
    }

    /**
     * 创建申请创建日志
     *
     * @param applicationId 申请ID
     * @param jobTitle 职位名称
     * @param companyName 公司名称
     * @return 保存的日志实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ApplicationLog createApplicationCreatedLog(Long applicationId, String jobTitle, String companyName) {
        log.info("创建申请创建日志：applicationId={}, jobTitle={}, company={}", applicationId, jobTitle, companyName);

        ApplicationLog log = new ApplicationLog();
        log.setApplicationId(applicationId);
        log.setLogType("APPLICATION_CREATED");
        log.setLogTitle("创建申请");
        log.setLogContent(String.format("创建了新申请：%s - %s", companyName, jobTitle));
        log.setLoggedBy("USER");

        save(log);
        return log;
    }

    /**
     * 创建申请提交日志
     *
     * @param applicationId 申请ID
     * @param jobTitle 职位名称
     * @return 保存的日志实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ApplicationLog createApplicationSubmittedLog(Long applicationId, String jobTitle) {
        log.info("创建申请提交日志：applicationId={}, jobTitle={}", applicationId, jobTitle);

        ApplicationLog log = new ApplicationLog();
        log.setApplicationId(applicationId);
        log.setLogType("APPLICATION_SUBMITTED");
        log.setLogTitle("提交申请");
        log.setLogContent(String.format("已提交申请：%s", jobTitle));
        log.setLoggedBy("USER");

        save(log);
        return log;
    }

    /**
     * 创建面试完成日志
     *
     * @param applicationId 申请ID
     * @param interviewType 面试类型
     * @param rating 评分
     * @return 保存的日志实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ApplicationLog createInterviewCompletedLog(Long applicationId, String interviewType, Integer rating) {
        log.info("创建面试完成日志：applicationId={}, type={}, rating={}", applicationId, interviewType, rating);

        ApplicationLog log = new ApplicationLog();
        log.setApplicationId(applicationId);
        log.setLogType("INTERVIEW_COMPLETED");
        log.setLogTitle("面试完成");
        log.setLogContent(String.format("已完成 %s 面试，评分：%d", interviewType, rating));
        log.setLoggedBy("SYSTEM");

        save(log);
        return log;
    }

    /**
     * 创建面试取消日志
     *
     * @param applicationId 申请ID
     * @param interviewType 面试类型
     * @return 保存的日志实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ApplicationLog createInterviewCancelledLog(Long applicationId, String interviewType) {
        log.info("创建面试取消日志：applicationId={}, type={}", applicationId, interviewType);

        ApplicationLog log = new ApplicationLog();
        log.setApplicationId(applicationId);
        log.setLogType("INTERVIEW_CANCELLED");
        log.setLogTitle("面试已取消");
        log.setLogContent(String.format("已取消 %s 面试", interviewType));
        log.setLoggedBy("USER");

        save(log);
        return log;
    }

    /**
     * 创建面试未参加日志
     *
     * @param applicationId 申请ID
     * @param interviewType 面试类型
     * @return 保存的日志实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ApplicationLog createInterviewNoShowLog(Long applicationId, String interviewType) {
        log.info("创建面试未参加日志：applicationId={}, type={}", applicationId, interviewType);

        ApplicationLog log = new ApplicationLog();
        log.setApplicationId(applicationId);
        log.setLogType("INTERVIEW_NO_SHOW");
        log.setLogTitle("未参加面试");
        log.setLogContent(String.format("未参加 %s 面试", interviewType));
        log.setLoggedBy("SYSTEM");

        save(log);
        return log;
    }
}
