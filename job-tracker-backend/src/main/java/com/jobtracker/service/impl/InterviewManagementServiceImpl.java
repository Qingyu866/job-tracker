package com.jobtracker.service.impl;

import com.jobtracker.common.exception.BusinessException;
import com.jobtracker.constants.InterviewStatus;
import com.jobtracker.dto.InterviewProgress;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.mapper.ApplicationMapper;
import com.jobtracker.mapper.InterviewMapper;
import com.jobtracker.service.ApplicationLogService;
import com.jobtracker.service.InterviewManagementService;
import com.jobtracker.service.StatusTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试管理服务实现类（支持多轮面试）
 * <p>
 * 提供多轮面试的完整管理功能
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewManagementServiceImpl implements InterviewManagementService {

    private final InterviewMapper interviewMapper;
    private final ApplicationMapper applicationMapper;
    private final StatusTransitionService statusTransitionService;
    private final ApplicationLogService applicationLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterviewRecord scheduleNextRound(Long applicationId, String interviewType,
                                              LocalDateTime interviewDate, String interviewerName,
                                              String interviewerTitle, String notes) {
        // 验证是否可以安排面试
        if (!statusTransitionService.canScheduleInterview(applicationId)) {
            String reason = statusTransitionService.getInterviewDisabledReason(applicationId);
            throw new BusinessException(reason);
        }

        // 获取当前轮次数
        List<InterviewRecord> existingInterviews = interviewMapper.selectByApplicationId(applicationId);
        int nextRound = existingInterviews.size() + 1;

        // 判断是否为终面（HR面通常为终面）
        boolean isFinal = "HR".equals(interviewType);

        InterviewRecord interview = new InterviewRecord();
        interview.setApplicationId(applicationId);
        interview.setInterviewType(interviewType);
        interview.setInterviewDate(interviewDate);
        interview.setRoundNumber(nextRound);
        interview.setIsFinal(isFinal);
        interview.setInterviewerName(interviewerName);
        interview.setInterviewerTitle(interviewerTitle);
        interview.setNotes(notes);
        interview.setStatus(InterviewStatus.SCHEDULED.getCode());

        interviewMapper.insert(interview);

        // 记录日志
        String interviewDateStr = interviewDate != null ? interviewDate.toString() : "待定";
        applicationLogService.createInterviewScheduledLog(applicationId, interviewType, interviewDateStr);

        // 触发状态联动
        statusTransitionService.onInterviewScheduled(applicationId);

        log.info("安排面试: applicationId={}, round={}, type={}, interviewId={}",
            applicationId, nextRound, interviewType, interview.getId());

        return interview;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterviewRecord scheduleNextRound(Long applicationId, String interviewType,
                                              LocalDateTime interviewDate) {
        return scheduleNextRound(applicationId, interviewType, interviewDate, null, null, null);
    }

    @Override
    public List<InterviewRecord> getInterviewsByApplication(Long applicationId) {
        List<InterviewRecord> interviews = interviewMapper.selectByApplicationId(applicationId);
        // 按轮次升序排列
        interviews.sort((a, b) -> {
            if (a.getRoundNumber() == null) return 1;
            if (b.getRoundNumber() == null) return -1;
            return a.getRoundNumber().compareTo(b.getRoundNumber());
        });
        return interviews;
    }

    @Override
    public InterviewRecord getCurrentInterview(Long applicationId) {
        List<InterviewRecord> interviews = interviewMapper.selectByApplicationId(applicationId);
        return interviews.stream()
            .filter(i -> {
                InterviewStatus status = InterviewStatus.fromCode(i.getStatus());
                return status == InterviewStatus.SCHEDULED || status == InterviewStatus.IN_PROGRESS;
            })
            .findFirst()
            .orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startInterview(Long interviewId) {
        statusTransitionService.transitionInterviewStatus(interviewId, InterviewStatus.IN_PROGRESS);
        log.info("面试开始: interviewId={}", interviewId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeInterview(Long interviewId, String result, Integer rating, String feedback) {
        InterviewRecord interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException("面试记录不存在: " + interviewId);
        }

        // 验证评分
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new BusinessException("评分必须在 1-5 之间");
        }

        // 验证结果
        if (!"PASSED".equals(result) && !"FAILED".equals(result)) {
            throw new BusinessException("面试结果必须是 PASSED 或 FAILED");
        }

        // 先更新为 COMPLETED 状态
        interview.setStatus(InterviewStatus.COMPLETED.getCode());
        interview.setResult(result);
        interview.setRating(rating);
        interview.setFeedback(feedback);
        interview.setUpdatedAt(LocalDateTime.now());
        interviewMapper.updateById(interview);

        // 然后根据结果转换状态
        InterviewStatus newStatus = "PASSED".equals(result) ? InterviewStatus.PASSED : InterviewStatus.FAILED;
        statusTransitionService.transitionInterviewStatus(interviewId, newStatus);

        // 记录日志
        applicationLogService.createInterviewCompletedLog(
            interview.getApplicationId(),
            interview.getInterviewType(),
            rating
        );

        log.info("面试完成: interviewId={}, result={}, rating={}", interviewId, result, rating);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelInterview(Long interviewId, String reason) {
        InterviewRecord interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException("面试记录不存在: " + interviewId);
        }

        interview.setNotes(interview.getNotes() != null
            ? interview.getNotes() + "\n取消原因: " + reason
            : "取消原因: " + reason);

        statusTransitionService.transitionInterviewStatus(interviewId, InterviewStatus.CANCELLED);

        // 记录日志
        applicationLogService.createInterviewCancelledLog(
            interview.getApplicationId(),
            interview.getInterviewType()
        );

        log.info("面试取消: interviewId={}, reason={}", interviewId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsNoShow(Long interviewId) {
        InterviewRecord interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException("面试记录不存在: " + interviewId);
        }

        statusTransitionService.transitionInterviewStatus(interviewId, InterviewStatus.NO_SHOW);

        // 记录日志
        applicationLogService.createInterviewNoShowLog(
            interview.getApplicationId(),
            interview.getInterviewType()
        );

        log.info("面试未参加: interviewId={}", interviewId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterviewRecord rescheduleInterview(Long interviewId, LocalDateTime newInterviewDate) {
        InterviewRecord interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException("面试记录不存在: " + interviewId);
        }

        InterviewStatus currentStatus = InterviewStatus.fromCode(interview.getStatus());
        if (currentStatus != InterviewStatus.CANCELLED && currentStatus != InterviewStatus.NO_SHOW) {
            throw new BusinessException("只有已取消或未参加的面试可以重新安排");
        }

        // 创建新的面试记录
        return scheduleNextRound(
            interview.getApplicationId(),
            interview.getInterviewType(),
            newInterviewDate,
            interview.getInterviewerName(),
            interview.getInterviewerTitle(),
            "重新安排的面试（原面试ID: " + interviewId + "）"
        );
    }

    @Override
    public InterviewProgress getProgress(Long applicationId) {
        List<InterviewRecord> interviews = getInterviewsByApplication(applicationId);

        InterviewProgress progress = new InterviewProgress();
        progress.setTotalRounds(interviews.size());

        int completedRounds = 0;
        int passedRounds = 0;
        int currentRound = 0;
        boolean allPassed = true;
        boolean hasFailed = false;

        for (InterviewRecord interview : interviews) {
            InterviewStatus status = InterviewStatus.fromCode(interview.getStatus());

            if (status != null) {
                if (status.isTerminal()) {
                    completedRounds++;
                }

                if (status == InterviewStatus.PASSED) {
                    passedRounds++;
                } else if (status == InterviewStatus.FAILED) {
                    hasFailed = true;
                    allPassed = false;
                } else if (status != InterviewStatus.CANCELLED && status != InterviewStatus.NO_SHOW) {
                    allPassed = false;
                }

                if ((status == InterviewStatus.SCHEDULED || status == InterviewStatus.IN_PROGRESS)
                    && currentRound == 0) {
                    currentRound = interview.getRoundNumber() != null ? interview.getRoundNumber() : 0;
                }
            }
        }

        progress.setCompletedRounds(completedRounds);
        progress.setPassedRounds(passedRounds);
        progress.setCurrentRound(currentRound);
        progress.setAllPassed(allPassed && interviews.size() > 0 && passedRounds == interviews.size());
        progress.setHasFailed(hasFailed);
        progress.setProgressText(generateProgressText(progress));

        return progress;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterviewRecord updateInterviewInfo(Long interviewId, LocalDateTime interviewDate,
                                                String interviewerName, String interviewerTitle,
                                                Integer durationMinutes, String notes) {
        InterviewRecord interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException("面试记录不存在: " + interviewId);
        }

        if (interviewDate != null) {
            interview.setInterviewDate(interviewDate);
        }
        if (interviewerName != null) {
            interview.setInterviewerName(interviewerName);
        }
        if (interviewerTitle != null) {
            interview.setInterviewerTitle(interviewerTitle);
        }
        if (durationMinutes != null) {
            interview.setDurationMinutes(durationMinutes);
        }
        if (notes != null) {
            interview.setNotes(notes);
        }

        interview.setUpdatedAt(LocalDateTime.now());
        interviewMapper.updateById(interview);

        log.info("更新面试信息: interviewId={}", interviewId);
        return interview;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsFinalInterview(Long interviewId, boolean isFinal) {
        InterviewRecord interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException("面试记录不存在: " + interviewId);
        }

        interview.setIsFinal(isFinal);
        interview.setUpdatedAt(LocalDateTime.now());
        interviewMapper.updateById(interview);

        log.info("标记面试为终面: interviewId={}, isFinal={}", interviewId, isFinal);
    }

    /**
     * 生成进度描述文本
     *
     * @param progress 面试进度
     * @return 进度描述
     */
    private String generateProgressText(InterviewProgress progress) {
        if (progress.getTotalRounds() == 0) {
            return "暂无面试安排";
        }

        if (progress.isAllPassed()) {
            return "全部面试通过，等待结果";
        }

        if (progress.isHasFailed()) {
            return "面试未通过";
        }

        if (progress.getCurrentRound() > 0) {
            return String.format("第%d轮面试进行中", progress.getCurrentRound());
        }

        return String.format("面试进度: %d/%d", progress.getCompletedRounds(), progress.getTotalRounds());
    }
}
