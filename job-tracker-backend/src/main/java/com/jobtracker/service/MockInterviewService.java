package com.jobtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jobtracker.entity.*;
import com.jobtracker.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 模拟面试服务
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockInterviewService {

    private final MockInterviewSessionMapper sessionMapper;
    private final InterviewMessageMapper messageMapper;
    private final MockInterviewEvaluationMapper evaluationMapper;
    private final UserResumeMapper resumeMapper;
    private final ApplicationMapper applicationMapper;
    private final CompanyMapper companyMapper;

    // ==================== 面试会话操作 ====================

    /**
     * 创建面试会话
     */
    @Transactional
    public MockInterviewSession createSession(Long applicationId, Long userId) {
        // 获取申请信息
        JobApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("申请不存在: " + applicationId);
        }

        // 获取公司信息
        Company company = null;
        if (application.getCompanyId() != null) {
            company = companyMapper.selectById(application.getCompanyId());
        }

        // 生成会话ID
        String sessionId = UUID.randomUUID().toString();

        // 创建会话
        MockInterviewSession session = MockInterviewSession.builder()
                .sessionId(sessionId)
                .applicationId(applicationId)
                .userId(userId)
                .resumeId(application.getResumeId())
                .companyId(application.getCompanyId())
                .jobTitle(application.getJobTitle())
                .seniorityLevel(application.getSeniorityLevel())
                .state(MockInterviewSession.InterviewState.INIT.name())
                .currentRound(0)
                .totalRounds(5)
                .createdAt(LocalDateTime.now())
                .build();

        sessionMapper.insert(session);
        log.info("创建面试会话成功，ID: {}, 申请: {}", sessionId, applicationId);

        return session;
    }

    /**
     * 获取会话
     */
    public MockInterviewSession getSession(String sessionId) {
        return sessionMapper.selectById(sessionId);
    }

    /**
     * 更新会话状态
     */
    @Transactional
    public void updateSessionState(String sessionId, MockInterviewSession.InterviewState newState) {
        MockInterviewSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setState(newState.name());
            sessionMapper.updateById(session);
        }
    }

    /**
     * 更新会话
     */
    @Transactional
    public void updateSession(MockInterviewSession session) {
        sessionMapper.updateById(session);
    }

    /**
     * 开始面试
     */
    @Transactional
    public void startInterview(String sessionId) {
        MockInterviewSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setState(MockInterviewSession.InterviewState.WELCOME.name());
            session.setStartedAt(LocalDateTime.now());
            sessionMapper.updateById(session);
            log.info("面试开始，会话ID: {}", sessionId);
        }
    }

    /**
     * 结束面试
     */
    @Transactional
    public void finishInterview(String sessionId) {
        MockInterviewSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setState(MockInterviewSession.InterviewState.FINISHED.name());
            session.setFinishedAt(LocalDateTime.now());
            if (session.getStartedAt() != null) {
                session.setDurationSeconds((int) java.time.Duration.between(
                        session.getStartedAt(),
                        session.getFinishedAt()
                ).getSeconds());
            }
            sessionMapper.updateById(session);
            log.info("面试结束，会话ID: {}, 耗时: {}秒", sessionId, session.getDurationSeconds());
        }
    }

    /**
     * 进入下一轮
     */
    @Transactional
    public void nextRound(String sessionId) {
        MockInterviewSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setCurrentRound(session.getCurrentRound() + 1);
            session.setState(MockInterviewSession.InterviewState.TECHNICAL_QA.name());
            sessionMapper.updateById(session);
            log.info("进入下一轮，会话ID: {}, 轮次: {}", sessionId, session.getCurrentRound());
        }
    }

    /**
     * 获取用户的所有面试会话
     */
    public List<MockInterviewSession> getUserSessions(Long userId) {
        return sessionMapper.selectList(
                new LambdaQueryWrapper<MockInterviewSession>()
                        .eq(MockInterviewSession::getUserId, userId)
                        .orderByDesc(MockInterviewSession::getCreatedAt)
        );
    }

    // ==================== 消息记录操作 ====================

    /**
     * 添加消息
     */
    @Transactional
    public InterviewMessage addMessage(InterviewMessage message) {
        messageMapper.insert(message);
        return message;
    }

    /**
     * 获取会话的所有消息
     */
    public List<InterviewMessage> getSessionMessages(String sessionId) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
                        .orderByAsc(InterviewMessage::getRoundNumber)
                        .orderByAsc(InterviewMessage::getSequenceInRound)
        );
    }

    /**
     * 获取当前轮次的消息
     */
    public List<InterviewMessage> getRoundMessages(String sessionId, Integer roundNumber) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
                        .eq(InterviewMessage::getRoundNumber, roundNumber)
                        .orderByAsc(InterviewMessage::getSequenceInRound)
        );
    }

    // ==================== 评分操作 ====================

    /**
     * 保存评分
     */
    @Transactional
    public MockInterviewEvaluation saveEvaluation(MockInterviewEvaluation evaluation) {
        evaluationMapper.insert(evaluation);

        // 更新会话总分（简单平均）
        MockInterviewSession session = sessionMapper.selectById(evaluation.getSessionId());
        if (session != null) {
            List<MockInterviewEvaluation> evaluations = getSessionEvaluations(evaluation.getSessionId());
            double avgScore = evaluations.stream()
                    .mapToDouble(e -> e.getTotalScore() != null ? e.getTotalScore().doubleValue() : 0)
                    .average()
                    .orElse(0);
            session.setTotalScore(java.math.BigDecimal.valueOf(avgScore));
            sessionMapper.updateById(session);
        }

        return evaluation;
    }

    /**
     * 获取会话的所有评分
     */
    public List<MockInterviewEvaluation> getSessionEvaluations(String sessionId) {
        return evaluationMapper.selectList(
                new LambdaQueryWrapper<MockInterviewEvaluation>()
                        .eq(MockInterviewEvaluation::getSessionId, sessionId)
                        .orderByAsc(MockInterviewEvaluation::getRoundNumber)
        );
    }

    // ==================== 简历快照操作 ====================

    /**
     * 生成简历快照
     */
    public String generateResumeSnapshot(Long resumeId) {
        UserResume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            return null;
        }

        // TODO: 实现完整的快照生成逻辑
        // 包括：基本信息、技能、项目、工作经历
        return String.format("{\"work_years\": %d, \"position\": \"%s\"}",
                resume.getWorkYears() != null ? resume.getWorkYears() : 0,
                resume.getCurrentPosition() != null ? resume.getCurrentPosition() : "");
    }
}
