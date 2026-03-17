package com.jobtracker.controller;

import com.jobtracker.dto.StartInterviewRequest;
import com.jobtracker.context.UserContext;
import com.jobtracker.common.result.Result;
import com.jobtracker.entity.*;
import com.jobtracker.agent.*;
import com.jobtracker.mapper.ApplicationMapper;
import com.jobtracker.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模拟面试 API 控制器
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@RestController
@RequestMapping("/mock-interview")
@RequiredArgsConstructor
public class MockInterviewController {

    private final MockInterviewService interviewService;
    private final InterviewMessageService messageService;
    private final EvaluationService evaluationService;
    private final InterviewAgentFactory agentFactory;
    private final UserResumeService resumeService;
    private final ApplicationMapper applicationMapper;

    /**
     * 创建面试会话
     * POST /api/mock-interview/start
     */
    @PostMapping("/start")
    public Result<MockInterviewSession> startInterview(@RequestBody StartInterviewRequest request) {
        // 从 Token 获取当前用户 ID
        Long userId = UserContext.getCurrentUserId();

        MockInterviewSession session = interviewService.createSession(
                request.getApplicationId(),
                userId
        );

        // 创建 Agent 组
        InterviewAgentFactory.InterviewAgents agents = agentFactory.createAgents(session);

        interviewService.startInterview(session.getSessionId());

        log.info("创建面试会话成功: sessionId={}, userId={}, applicationId={}",
                session.getSessionId(), userId, request.getApplicationId());

        return Result.success("面试会话创建成功", session);
    }

    /**
     * 获取会话详情
     * GET /api/mock-interview/sessions/{sessionId}
     */
    @GetMapping("/sessions/{sessionId}")
    public Result<MockInterviewSession> getSession(@PathVariable String sessionId) {
        MockInterviewSession session = interviewService.getSession(sessionId);
        if (session == null) {
            return Result.error("会话不存在");
        }
        return Result.success(session);
    }

    /**
     * 获取当前用户的面试会话列表
     * GET /api/mock-interview/sessions/my
     */
    @GetMapping("/sessions/my")
    public Result<List<MockInterviewSession>> getMySessions() {
        Long userId = UserContext.getCurrentUserId();
        List<MockInterviewSession> sessions = interviewService.getUserSessions(userId);
        return Result.success(sessions);
    }

    /**
     * 获取用户的面试会话列表（已废弃，请使用 /sessions/my）
     * GET /api/mock-interview/sessions/user/{userId}
     * @deprecated 请使用 /sessions/my
     */
    @Deprecated(since = "2026-03-17", forRemoval = true)
    @GetMapping("/sessions/user/{userId}")
    public Result<List<MockInterviewSession>> getUserSessions(@PathVariable Long userId) {
        List<MockInterviewSession> sessions = interviewService.getUserSessions(userId);
        return Result.success(sessions);
    }

    /**
     * 发送消息（用户回答）
     * POST /api/mock-interview/sessions/{sessionId}/message
     */
    @PostMapping("/sessions/{sessionId}/message")
    public Result<InterviewMessage> sendMessage(
            @PathVariable String sessionId,
            @RequestBody MessageRequest request
    ) {
        // 保存用户消息
        InterviewMessage userMessage = messageService.addAnswer(
                sessionId,
                request.getRoundNumber(),
                request.getContent()
        );

        // 获取会话
        MockInterviewSession session = interviewService.getSession(sessionId);
        if (session == null) {
            return Result.error("会话不存在");
        }

        // 获取 Agent
        InterviewAgentFactory.InterviewAgents agents = agentFactory.getAgents(sessionId);
        if (agents == null) {
            agents = agentFactory.createAgents(session);
        }

        // 副面试官决定下一步
        String nextStepContext = buildNextStepContext(session, request.getContent());
        String nextStep = agents.viceInterviewer().decideNextStep(nextStepContext);

        // 主面试官生成问题
        String questionContext = buildQuestionContext(session, nextStep);
        String question = agents.mainInterviewer().askQuestion(questionContext);

        // 保存面试官问题
        InterviewMessage aiMessage = messageService.addQuestion(
                sessionId,
                session.getCurrentRound() + 1,
                question,
                null,  // skillId
                null   // skillName
        );

        interviewService.nextRound(sessionId);

        return Result.success(aiMessage);
    }

    /**
     * 获取会话消息列表
     * GET /api/mock-interview/sessions/{sessionId}/messages
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<InterviewMessage>> getMessages(@PathVariable String sessionId) {
        List<InterviewMessage> messages = messageService.getSessionMessages(sessionId);
        return Result.success(messages);
    }

    /**
     * 结束面试
     * POST /api/mock-interview/sessions/{sessionId}/finish
     */
    @PostMapping("/sessions/{sessionId}/finish")
    public Result<MockInterviewSession> finishInterview(@PathVariable String sessionId) {
        interviewService.finishInterview(sessionId);

        MockInterviewSession session = interviewService.getSession(sessionId);

        // 生成面试总结
        String summary = evaluationService.generateSummary(sessionId);
        // TODO: 保存总结到会话

        // 持久化所有 Agent 的记忆到数据库
        agentFactory.persistMemories(sessionId);
        log.info("面试结束，已持久化所有 Agent 记忆，会话ID: {}", sessionId);

        return Result.success("面试已结束", session);
    }

    /**
     * 获取会话评分列表
     * GET /api/mock-interview/sessions/{sessionId}/evaluations
     */
    @GetMapping("/sessions/{sessionId}/evaluations")
    public Result<List<MockInterviewEvaluation>> getEvaluations(@PathVariable String sessionId) {
        List<MockInterviewEvaluation> evaluations = evaluationService.getSessionEvaluations(sessionId);
        return Result.success(evaluations);
    }

    /**
     * 评估单轮回答
     * POST /api/mock-interview/sessions/{sessionId}/evaluate
     */
    @PostMapping("/sessions/{sessionId}/evaluate")
    public Result<MockInterviewEvaluation> evaluateAnswer(
            @PathVariable String sessionId,
            @RequestBody EvaluationRequest request
    ) {
        MockInterviewSession session = interviewService.getSession(sessionId);
        if (session == null) {
            return Result.error("会话不存在");
        }

        // 获取 Agent
        InterviewAgentFactory.InterviewAgents agents = agentFactory.getAgents(sessionId);
        if (agents == null) {
            return Result.error("Agent 未初始化");
        }

        // 评估回答
        String context = buildEvaluationContext(session, request);
        String evaluationResult = agents.evaluator().evaluate(context);

        // TODO: 解析 JSON 并保存评分
        // 当前简化处理
        MockInterviewEvaluation evaluation = new MockInterviewEvaluation();
        evaluation.setSessionId(sessionId);
        evaluation.setRoundNumber(request.getRoundNumber());
        evaluation.setQuestionText(request.getQuestion());
        evaluation.setUserAnswer(request.getAnswer());
        evaluation.setTotalScore(java.math.BigDecimal.valueOf(7.0));
        evaluation.setFeedback(evaluationResult);

        evaluationService.createEvaluation(evaluation);

        return Result.success("评估完成", evaluation);
    }

    /**
     * 构建下一步决策上下文
     */
    private String buildNextStepContext(MockInterviewSession session, String userAnswer) {
        return String.format("""
                用户最新回答: %s
                已考察知识点: %s
                待考察知识点: %s
                """,
                userAnswer,
                session.getSkillsCovered(),
                session.getSkillsPending()
        );
    }

    /**
     * 构建提问上下文
     */
    private String buildQuestionContext(MockInterviewSession session, String nextStep) {
        return String.format("""
                下一步决策: %s
                岗位: %s
                级别: %s
                简历快照: %s
                JD 快照: %s
                """,
                nextStep,
                session.getJobTitle(),
                session.getSeniorityLevel(),
                session.getResumeSnapshot(),
                session.getJdSnapshot()
        );
    }

    /**
     * 构建评估上下文
     */
    private String buildEvaluationContext(MockInterviewSession session, EvaluationRequest request) {
        return String.format("""
                问题: %s
                用户回答: %s
                简历快照: %s
                JD 要求: %s
                """,
                request.getQuestion(),
                request.getAnswer(),
                session.getResumeSnapshot(),
                session.getJdSnapshot()
        );
    }

    /**
     * 发送消息请求
     */
    @lombok.Data
    public static class MessageRequest {
        private String content;
        private Integer roundNumber;
    }

    /**
     * 评估请求
     */
    @lombok.Data
    public static class EvaluationRequest {
        private Integer roundNumber;
        private String question;
        private String answer;
    }
}
