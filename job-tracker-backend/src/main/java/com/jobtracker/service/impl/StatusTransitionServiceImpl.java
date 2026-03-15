package com.jobtracker.service.impl;

import com.jobtracker.common.exception.BusinessException;
import com.jobtracker.constants.ApplicationStatus;
import com.jobtracker.constants.InterviewStatus;
import com.jobtracker.constants.TransitionRules;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.mapper.ApplicationMapper;
import com.jobtracker.mapper.InterviewMapper;
import com.jobtracker.service.ApplicationLogService;
import com.jobtracker.service.StatusTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 状态转换验证服务实现类
 * <p>
 * 实现申请状态和面试状态的转换验证、执行以及联动处理
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatusTransitionServiceImpl implements StatusTransitionService {

    private final ApplicationMapper applicationMapper;
    private final InterviewMapper interviewMapper;
    private final ApplicationLogService applicationLogService;

    @Override
    public boolean canTransition(ApplicationStatus from, ApplicationStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return TransitionRules.canTransition(from, to);
    }

    @Override
    public boolean canTransition(InterviewStatus from, InterviewStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return TransitionRules.canTransition(from, to);
    }

    @Override
    public List<ApplicationStatus> getNextPossibleStatuses(ApplicationStatus current) {
        if (current == null) {
            return Collections.emptyList();
        }
        return TransitionRules.getNextPossibleStatuses(current).stream()
            .sorted((a, b) -> a.getStage().ordinal() - b.getStage().ordinal())
            .toList();
    }

    @Override
    public List<InterviewStatus> getNextPossibleInterviewStatuses(InterviewStatus current) {
        if (current == null) {
            return Collections.emptyList();
        }
        return TransitionRules.getNextPossibleStatuses(current).stream()
            .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transitionApplicationStatus(Long applicationId, ApplicationStatus newStatus) {
        JobApplication app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException("申请不存在: " + applicationId);
        }

        ApplicationStatus currentStatus = ApplicationStatus.fromCode(app.getStatus());
        if (currentStatus == null) {
            throw new BusinessException("无效的当前状态: " + app.getStatus());
        }

        // 验证转换是否合法
        if (!canTransition(currentStatus, newStatus)) {
            throw new BusinessException(
                String.format("不允许从 %s 转换到 %s", currentStatus.getDescription(), newStatus.getDescription())
            );
        }

        // 记录旧状态
        String oldStatus = app.getStatus();

        // 执行转换
        app.setStatus(newStatus.getCode());
        app.setUpdatedAt(LocalDateTime.now());
        applicationMapper.updateById(app);

        // 记录状态变更日志
        applicationLogService.createStatusChangeLog(
            applicationId,
            oldStatus,
            newStatus.getCode()
        );

        log.info("申请状态变更: applicationId={}, {} → {}",
            applicationId, currentStatus.getDescription(), newStatus.getDescription());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transitionInterviewStatus(Long interviewId, InterviewStatus newStatus) {
        InterviewRecord interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException("面试记录不存在: " + interviewId);
        }

        InterviewStatus currentStatus = InterviewStatus.fromCode(interview.getStatus());
        if (currentStatus == null) {
            throw new BusinessException("无效的当前面试状态: " + interview.getStatus());
        }

        // 验证转换是否合法
        if (!canTransition(currentStatus, newStatus)) {
            throw new BusinessException(
                String.format("不允许从 %s 转换到 %s", currentStatus.getDescription(), newStatus.getDescription())
            );
        }

        // 执行转换
        interview.setStatus(newStatus.getCode());
        interview.setUpdatedAt(LocalDateTime.now());
        interviewMapper.updateById(interview);

        // 联动更新申请状态
        handleInterviewStatusChange(interview, newStatus);

        log.info("面试状态变更: interviewId={}, {} → {}",
            interviewId, currentStatus.getDescription(), newStatus.getDescription());
    }

    @Override
    public void onInterviewScheduled(Long applicationId) {
        JobApplication app = applicationMapper.selectById(applicationId);
        if (app == null) {
            return;
        }

        ApplicationStatus currentStatus = ApplicationStatus.fromCode(app.getStatus());
        if (currentStatus == null) {
            return;
        }

        // 只有在 INTERVIEW 之前的状态才更新
        if (currentStatus.isBefore(ApplicationStatus.INTERVIEW)) {
            transitionApplicationStatus(applicationId, ApplicationStatus.INTERVIEW);
        }
    }

    @Override
    public boolean canScheduleInterview(Long applicationId) {
        JobApplication app = applicationMapper.selectById(applicationId);
        if (app == null) {
            return false;
        }

        ApplicationStatus status = ApplicationStatus.fromCode(app.getStatus());
        if (status == null) {
            return false;
        }

        return status.canScheduleInterview();
    }

    @Override
    public String getInterviewDisabledReason(Long applicationId) {
        JobApplication app = applicationMapper.selectById(applicationId);
        if (app == null) {
            return "申请不存在";
        }

        ApplicationStatus status = ApplicationStatus.fromCode(app.getStatus());
        if (status == null) {
            return "无效的申请状态";
        }

        return status.getInterviewDisabledReason();
    }

    /**
     * 面试状态变更联动处理
     * <p>
     * 根据面试状态变更自动更新申请状态
     * </p>
     *
     * @param interview 面试记录
     * @param newStatus 新的面试状态
     */
    private void handleInterviewStatusChange(InterviewRecord interview, InterviewStatus newStatus) {
        Long applicationId = interview.getApplicationId();

        switch (newStatus) {
            case SCHEDULED -> {
                // 安排面试 → 检查是否需要更新申请状态
                JobApplication app = applicationMapper.selectById(applicationId);
                if (app != null) {
                    ApplicationStatus currentStatus = ApplicationStatus.fromCode(app.getStatus());
                    if (currentStatus != null && currentStatus.isBefore(ApplicationStatus.INTERVIEW)) {
                        transitionApplicationStatus(applicationId, ApplicationStatus.INTERVIEW);
                    }
                }
            }

            case IN_PROGRESS -> {
                // 面试进行中 - 无需特殊处理
                log.debug("面试进行中: interviewId={}", interview.getId());
            }

            case PASSED -> {
                // 面试通过 → 检查是否还有后续面试
                boolean hasMore = hasUpcomingInterviews(applicationId, interview.getId());
                if (!hasMore) {
                    log.info("所有面试已完成，等待最终结果: applicationId={}", applicationId);
                    // 检查是否为终面，如果是终面通过可以进入 FINAL_ROUND 或等待 OFFER
                    if (Boolean.TRUE.equals(interview.getIsFinal())) {
                        log.info("终面已通过，等待Offer: applicationId={}", applicationId);
                    }
                }
            }

            case FAILED -> {
                // 面试未通过 → 判断流程是否结束
                if (isFinalInterview(interview)) {
                    // 终面未通过 → 直接拒绝
                    log.info("终面未通过，申请被拒绝: applicationId={}", applicationId);
                    transitionApplicationStatus(applicationId, ApplicationStatus.REJECTED);
                } else {
                    // 非终面未通过 - 取决于是否有后续面试机会
                    boolean hasMore = hasUpcomingInterviews(applicationId, interview.getId());
                    if (!hasMore) {
                        // 没有其他面试安排，标记为拒绝
                        log.info("面试未通过且无后续安排，申请被拒绝: applicationId={}", applicationId);
                        transitionApplicationStatus(applicationId, ApplicationStatus.REJECTED);
                    }
                }
            }

            case NO_SHOW -> {
                // 未参加 → 记录但不自动改变申请状态
                log.info("面试未参加: interviewId={}, applicationId={}", interview.getId(), applicationId);
            }

            case CANCELLED -> {
                // 面试取消 → 检查是否还有其他面试
                log.info("面试已取消: interviewId={}", interview.getId());
            }

            case COMPLETED -> {
                // 面试完成，等待评估
                log.info("面试已完成，等待评估: interviewId={}", interview.getId());
            }

            default -> log.debug("面试状态变更: {} -> {}", interview.getId(), newStatus);
        }
    }

    /**
     * 检查是否还有待处理/已安排的面试
     *
     * @param applicationId       申请ID
     * @param excludeInterviewId  排除的面试ID（当前面试）
     * @return 如果还有待处理的面试返回 true
     */
    private boolean hasUpcomingInterviews(Long applicationId, Long excludeInterviewId) {
        List<InterviewRecord> interviews = interviewMapper.selectByApplicationId(applicationId);
        return interviews.stream()
            .filter(i -> !i.getId().equals(excludeInterviewId))
            .anyMatch(i -> {
                InterviewStatus status = InterviewStatus.fromCode(i.getStatus());
                return status == InterviewStatus.SCHEDULED || status == InterviewStatus.IN_PROGRESS;
            });
    }

    /**
     * 判断是否为最后一轮面试
     *
     * @param interview 面试记录
     * @return 如果是终面返回 true
     */
    private boolean isFinalInterview(InterviewRecord interview) {
        // 优先使用 isFinal 字段
        if (Boolean.TRUE.equals(interview.getIsFinal())) {
            return true;
        }
        // 如果没有设置 isFinal，则根据面试类型判断（HR面通常为终面）
        return "HR".equals(interview.getInterviewType());
    }
}
