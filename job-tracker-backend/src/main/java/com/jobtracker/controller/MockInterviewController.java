package com.jobtracker.controller;

import com.jobtracker.dto.StartInterviewRequest;
import com.jobtracker.context.UserContext;
import com.jobtracker.common.result.Result;
import com.jobtracker.entity.*;
import com.jobtracker.agent.*;
import com.jobtracker.agent.interview.dto.ImprovementSuggestionListResponse;
import com.jobtracker.agent.interview.dto.SkillCredibilityListResponse;
import com.jobtracker.mapper.ApplicationMapper;
import com.jobtracker.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    private final CompanyService companyService;

    /**
     * 创建面试会话
     * POST /api/mock-interview/start
     */
    @PostMapping("/start")
    public Result<MockInterviewSession> startInterview(@RequestBody StartInterviewRequest request) {
        // todo 用户的简历id 没有添加？
        // 从 Token 获取当前用户 ID
        Long userId = UserContext.getCurrentUserId();

        MockInterviewSession session = interviewService.createSession(
                request.getApplicationId(),
                request.getResumeId(),
                userId
        );

        // 创建 Agent 组
        agentFactory.createAgents(session);

        // 开始面试
        interviewService.startInterview(session.getSessionId());

        // 生成并保存问题计划（轮数由配置文件决定）
        try {
            int planCount = interviewService.generateAndSaveQuestionPlans(session.getSessionId());
            log.info("生成问题计划成功: sessionId={}, planCount={}", session.getSessionId(), planCount);
        } catch (Exception e) {
            log.error("生成问题计划失败: sessionId={}", session.getSessionId(), e);
            // 计划生成失败不影响会话创建，后续可以手动重试
        }

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
//    @Deprecated(since = "2026-03-17", forRemoval = true)
//    @GetMapping("/sessions/user/{userId}")
//    public Result<List<MockInterviewSession>> getUserSessions(@PathVariable Long userId) {
//        List<MockInterviewSession> sessions = interviewService.getUserSessions(userId);
//        return Result.success(sessions);
//    }

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

        // ===== 评估上一轮用户回答 =====
        InterviewMessage lastQuestion = messageService.getLastQuestion(sessionId);
        if (lastQuestion != null) {
            try {
                // 构建评估上下文
                String evaluationContext = buildEvaluationContextForRound(
                        session,
                        lastQuestion.getContent(),
                        request.getContent(),
                        session.getCurrentRound()
                );

                // 调用评审专家评估
                com.jobtracker.agent.interview.dto.EvaluationResult evaluationResult =
                        agents.evaluator().evaluate(
                                evaluationContext,              // @UserMessage
                                agents.basicContext(),          // @V("context")
                                agents.resumeSnapshot(),        // @V("resume_snapshot")
                                agents.jdSnapshot()             // @V("jd_snapshot")
                        );

                // 从 EvaluationResult 对象创建评分记录
                MockInterviewEvaluation evaluation = createEvaluationFromResult(
                        session,
                        lastQuestion.getContent(),
                        request.getContent(),
                        evaluationResult
                );

                log.debug("创建评分记录成功，会话: {}, 轮次: {}, 分数: {}",
                        sessionId, session.getCurrentRound(), evaluation.getTotalScore());

                // 持久化 Agent 记忆
                try {
                    agentFactory.persistMemories(sessionId);
                    log.debug("持久化 Agent 记忆成功，会话: {}, 轮次: {}", sessionId, session.getCurrentRound());
                } catch (Exception e) {
                    log.warn("持久化 Agent 记忆失败，会话: {}, 轮次: {}", sessionId, session.getCurrentRound(), e);
                }

            } catch (Exception e) {
                log.warn("评估回答失败，跳过本轮评分，会话: {}, 轮次: {}",
                        sessionId, session.getCurrentRound(), e);
            }
        }

        // ===== 按照预生成的计划执行 =====
        // 获取下一个待执行的计划
        com.jobtracker.entity.MockInterviewEvaluation nextPlan = interviewService.getNextPendingPlan(sessionId);

        if (nextPlan == null) {
            // 没有更多计划，返回提示信息
            log.info("没有更多问题计划，面试已结束，会话ID: {}", sessionId);
            return Result.error("所有问题已完成，请点击结束面试按钮查看报告");
        }

        // 更新计划状态为 IN_PROGRESS
        interviewService.updatePlanStatus(nextPlan.getEvaluationId(), "IN_PROGRESS");

        // 更新会话的当前计划ID
        session.setCurrentPlanId(nextPlan.getEvaluationId());
        interviewService.updateSession(session);

        // ===== 根据计划生成问题 =====
        String questionContext = buildQuestionContextFromPlan(session, nextPlan);
        String question = agents.mainInterviewer().askQuestion(
                questionContext,                  // @UserMessage
                java.time.LocalDate.now().toString(),  // @V("current_date")
                java.time.LocalTime.now().toString(),  // @V("current_time")
                agents.basicContext(),            // @V("context")
                agents.resumeSnapshot(),          // @V("resume_snapshot")
                agents.jdSnapshot()               // @V("jd_snapshot")
        );

        // 保存面试官问题
        InterviewMessage aiMessage = messageService.addQuestion(
                sessionId,
                session.getCurrentRound() + 1,
                question,
                nextPlan.getSkillId(),
                nextPlan.getSkillName()
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
        // 1. 结束面试状态
        interviewService.finishInterview(sessionId);
        MockInterviewSession session = interviewService.getSession(sessionId);
        if (session == null) {
            return Result.error("会话不存在");
        }

        // 2. 删除所有待执行的计划（PENDING 状态）
        int deletedCount = evaluationService.deletePendingPlans(sessionId);
        log.info("清理待执行计划完成，会话ID: {}, 删除数量: {}", sessionId, deletedCount);

        // 3. 获取 Agent 组
        InterviewAgentFactory.InterviewAgents agents = agentFactory.getAgents(sessionId);
        if (agents == null) {
            log.warn("Agent 未初始化，使用简化报告生成，会话ID: {}", sessionId);
            return finishInterviewSimple(session, sessionId);
        }

        // 4. 获取所有评分记录（用于构建上下文）
        List<MockInterviewEvaluation> evaluations = evaluationService.getSessionEvaluations(sessionId);

        // 4. 构建完整报告生成上下文
        String reportContext = buildReportContext(session, evaluations);

        try {
            // 5. 调用评审专家生成可信度分析
            SkillCredibilityListResponse credibilityResponse =
                    agents.evaluator().generateCredibilityAnalysis(
                            reportContext,               // @UserMessage
                            agents.basicContext(),       // @V("context")
                            agents.resumeSnapshot(),     // @V("resume_snapshot")
                            agents.jdSnapshot()          // @V("jd_snapshot")
                    );
            List<com.jobtracker.agent.interview.dto.SkillCredibility> credibilityList =
                    credibilityResponse != null ? credibilityResponse.getCredibilities() : null;

            // 序列化为 JSON 字符串存储
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String credibilityAnalysisJson = mapper.writeValueAsString(credibilityList);
            session.setResumeGapAnalysis(credibilityAnalysisJson);

            // 6. 计算总体可信度评分
            com.jobtracker.agent.interview.dto.CredibilityScoreResult credibilityScoreResult =
                    agents.evaluator().calculateCredibilityScore(
                            reportContext,               // @UserMessage
                            agents.basicContext(),       // @V("context")
                            agents.resumeSnapshot(),     // @V("resume_snapshot")
                            agents.jdSnapshot()          // @V("jd_snapshot")
                    );
            Double credibilityScore = credibilityScoreResult.getCredibilityScore();
            session.setResumeCredibilityScore(BigDecimal.valueOf(credibilityScore));

            // 7. 调用副面试官生成改进建议
            ImprovementSuggestionListResponse suggestionsResponse =
                    agents.viceInterviewer().generateSuggestions(
                            reportContext,               // @UserMessage
                            agents.basicContext(),       // @V("context")
                            agents.resumeSnapshot(),     // @V("resume_snapshot")
                            agents.jdSnapshot()          // @V("jd_snapshot")
                    );
            List<com.jobtracker.agent.interview.dto.ImprovementSuggestion> suggestions =
                    suggestionsResponse != null ? suggestionsResponse.getSuggestions() : null;

            // 序列化为 JSON 字符串存储
            String suggestionsJson = mapper.writeValueAsString(suggestions);
            session.setImprovementSuggestions(suggestionsJson);

            // 8. 调用主面试官生成面试总结
            String summary = agents.mainInterviewer().generateSummary(
                    reportContext,                  // @UserMessage
                    java.time.LocalDate.now().toString(),  // @V("current_date")
                    java.time.LocalTime.now().toString(),  // @V("current_time")
                    agents.basicContext(),          // @V("context")
                    agents.resumeSnapshot(),        // @V("resume_snapshot")
                    agents.jdSnapshot()             // @V("jd_snapshot")
            );
            session.setFeedbackSummary(summary);

            // 9. 计算总体评分（基于所有 evaluation）
            BigDecimal totalScore = evaluationService.calculateTotalScore(sessionId);
            session.setTotalScore(totalScore);

            // 10. 持久化所有 Agent 的记忆到数据库
            agentFactory.persistMemories(sessionId);

            // 11. 更新会话（保存所有报告字段）
            interviewService.updateSession(session);

            log.info("面试报告生成完成，会话ID: {}, 总分: {}, 可信度: {}",
                    sessionId, totalScore, credibilityScore);

            return Result.success("面试已结束，报告生成成功", session);

        } catch (Exception e) {
            log.error("生成面试报告失败，使用简化方案，会话ID: {}", sessionId, e);
            return finishInterviewSimple(session, sessionId);
        }
    }

    /**
     * 简化版面试结束（用于 Agent 不可用时）
     */
    private Result<MockInterviewSession> finishInterviewSimple(MockInterviewSession session, String sessionId) {
        // 删除所有待执行的计划（PENDING 状态）
        int deletedCount = evaluationService.deletePendingPlans(sessionId);
        log.info("清理待执行计划完成（简化版），会话ID: {}, 删除数量: {}", sessionId, deletedCount);

        // 计算总体评分
        BigDecimal totalScore = evaluationService.calculateTotalScore(sessionId);
        session.setTotalScore(totalScore);

        // 生成简单总结
        String summary = evaluationService.generateSummary(sessionId);
        session.setFeedbackSummary(summary);

        // 设置默认可信度评分（基于总分）
        double credibilityRatio = totalScore.doubleValue() / 10.0;
        session.setResumeCredibilityScore(BigDecimal.valueOf(credibilityRatio));

        // 设置默认的 JSON 字段（空数组）
        session.setResumeGapAnalysis("[]");
        session.setImprovementSuggestions("[]");

        // 持久化记忆
        agentFactory.persistMemories(sessionId);

        // 更新会话
        interviewService.updateSession(session);

        log.info("面试报告生成完成（简化版），会话ID: {}, 总分: {}", sessionId, totalScore);

        return Result.success("面试已结束，报告生成成功", session);
    }

    /**
     * 暂停面试
     * POST /api/mock-interview/sessions/{sessionId}/pause
     */
    @PostMapping("/sessions/{sessionId}/pause")
    public Result<Void> pauseInterview(@PathVariable String sessionId) {
        return interviewService.pauseInterview(sessionId);
    }

    /**
     * 恢复面试
     * POST /api/mock-interview/sessions/{sessionId}/resume
     */
    @PostMapping("/sessions/{sessionId}/resume")
    public Result<Void> resumeInterview(@PathVariable String sessionId) {
        return interviewService.resumeInterview(sessionId);
    }

    /**
     * 获取面试进度
     * GET /api/mock-interview/sessions/{sessionId}/progress
     */
    @GetMapping("/sessions/{sessionId}/progress")
    public Result<com.jobtracker.dto.MockInterviewProgress> getProgress(@PathVariable String sessionId) {
        try {
            com.jobtracker.dto.MockInterviewProgress progress = interviewService.getProgress(sessionId);
            return Result.success(progress);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 构建报告生成上下文（优化版）
     * <p>
     * 注意：简历信息和 JD 要求已在系统提示词中提供，无需重复传递
     * </p>
     */
    private String buildReportContext(MockInterviewSession session, List<MockInterviewEvaluation> evaluations) {
        StringBuilder context = new StringBuilder();

        // 查询公司名称
        String companyName = "未知";
        if (session.getCompanyId() != null) {
            Company company = companyService.getById(session.getCompanyId());
            if (company != null) {
                companyName = company.getName();
            }
        }

        // 基本信息
        context.append("# 面试基本信息\n");
        context.append(String.format("- 公司: %s\n", companyName));
        context.append(String.format("- 岗位: %s\n", session.getJobTitle()));
        context.append(String.format("- 级别: %s\n", session.getSeniorityLevel()));
        context.append(String.format("- 面试轮次: %d\n", session.getCurrentRound()));
        context.append("\n");

        // 注意：简历快照和 JD 快照已在系统提示词中提供，无需重复传递

        // 已考察技能
        context.append("# 已考察技能\n");
        if (session.getSkillsCovered() != null) {
            context.append(session.getSkillsCovered().toString());
        }
        context.append("\n");

        // 评分记录汇总
        if (evaluations != null && !evaluations.isEmpty()) {
            context.append("# 评分记录汇总\n");
            for (MockInterviewEvaluation eval : evaluations) {
                context.append(String.format("## 轮次 %d - %s\n", eval.getRoundNumber(), eval.getSkillName()));
                context.append(String.format("- 技术分: %s\n", eval.getTechnicalScore()));
                context.append(String.format("- 逻辑分: %s\n", eval.getLogicScore()));
                context.append(String.format("- 深度分: %s\n", eval.getDepthScore()));
                context.append(String.format("- 总分: %s\n", eval.getTotalScore()));
                if (eval.getFeedback() != null) {
                    context.append(String.format("- 反馈: %s\n", eval.getFeedback()));
                }
                context.append("\n");
            }
        }

        return context.toString();
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

        // 评估回答（现在返回 EvaluationResult 对象）
        String context = buildEvaluationContext(session, request);
        com.jobtracker.agent.interview.dto.EvaluationResult evaluationResult =
                agents.evaluator().evaluate(
                        context,                      // @UserMessage
                        agents.basicContext(),         // @V("context")
                        agents.resumeSnapshot(),       // @V("resume_snapshot")
                        agents.jdSnapshot()            // @V("jd_snapshot")
                );

        // 从 EvaluationResult 对象创建评分记录
        MockInterviewEvaluation evaluation = createEvaluationFromResult(
                session,
                request.getQuestion(),
                request.getAnswer(),
                evaluationResult
        );

        evaluationService.createEvaluation(evaluation);

        return Result.success("评估完成", evaluation);
    }

    /**
     * 构建下一步决策上下文
     */
    private String buildNextStepContext(MockInterviewSession session, String userAnswer) {
        // 获取最近的对话历史，用于判断是否陷入死循环
        List<InterviewMessage> recentMessages = messageService.getSessionMessages(session.getSessionId());
        int last10MessagesCount = recentMessages.size() > 10 ? 10 : recentMessages.size();

        // 统计最近的副面试官决策
        String recentTopics = "";
        if (!recentMessages.isEmpty()) {
            StringBuilder topics = new StringBuilder();
            int count = 0;
            for (int i = recentMessages.size() - 1; i >= 0 && count < 5; i--) {
                InterviewMessage msg = recentMessages.get(i);
                if ("ASSISTANT".equals(msg.getRole()) && msg.getContent() != null) {
                    topics.append(msg.getContent()).append("\n");
                    count++;
                }
            }
            recentTopics = topics.toString();
        }

        return String.format("""
                用户最新回答: %s
                已考察知识点: %s
                待考察知识点: %s

                # 最近 5 轮对话历史
                %s

                # 统计信息
                - 当前轮次: %d
                - 总消息数: %d
                """,
                userAnswer,
                session.getSkillsCovered() != null ? session.getSkillsCovered() : "无",
                session.getSkillsPending() != null ? session.getSkillsPending() : "无",
                recentTopics,
                session.getCurrentRound(),
                last10MessagesCount
        );
    }

    /**
     * 构建提问上下文（优化版）
     * <p>
     * 注意：简历信息和JD要求已在系统提示词中提供，无需重复传递
     * 此方法未使用，保留作为备用实现
     * </p>
     */
    private String buildQuestionContext(
            MockInterviewSession session,
            com.jobtracker.agent.interview.dto.NextStepDecision nextStepDecision
    ) {
        return String.format("""
                # 下一步决策
                - 选题: %s
                - 选题来源: %s
                - 选择原因: %s
                - 难度等级: %d

                # 面试信息
                - 岗位: %s
                - 级别: %s

                # 注意事项
                - 简历信息和 JD 要求已在系统提示词中提供
                """,
                nextStepDecision.getNextTopic(),
                nextStepDecision.getTopicSource(),
                nextStepDecision.getReason(),
                nextStepDecision.getDifficulty(),
                session.getJobTitle(),
                session.getSeniorityLevel()
        );
    }

    /**
     * 构建评估上下文（优化版）
     * <p>
     * 注意：简历信息和 JD 要求已在系统提示词中提供，无需重复传递
     * </p>
     */
    private String buildEvaluationContext(MockInterviewSession session, EvaluationRequest request) {
        return String.format("""
                # 评估任务
                请评估候选人的回答

                # 问题
                %s

                # 用户回答
                %s

                # 注意事项
                - 简历信息和 JD 要求已在系统提示词中提供
                - 请基于系统提示词中的简历信息，评估回答的真实性
                """,
                request.getQuestion(),
                request.getAnswer()
        );
    }

    /**
     * 构建单轮评估上下文（优化版）
     * <p>
     * 注意：简历信息和 JD 要求已在系统提示词中提供，无需重复传递
     * </p>
     */
    private String buildEvaluationContextForRound(
            MockInterviewSession session,
            String question,
            String userAnswer,
            Integer roundNumber
    ) {
        return String.format("""
                # 评估任务
                请评估候选人的第 %d 轮回答

                # 问题
                %s

                # 用户回答
                %s

                # 注意事项
                - 简历信息和 JD 要求已在系统提示词中提供
                - 请基于系统提示词中的简历信息，评估回答的真实性
                - 关注回答与简历声称的匹配度
                """,
                roundNumber,
                question,
                userAnswer
        );
    }

    /**
     * 从计划构建提问上下文（优化版）
     * <p>
     * 根据预生成的问题计划，构建主面试官生成问题的上下文
     * 注意：简历信息已在系统提示词中提供，无需重复传递
     * </p>
     */
    private String buildQuestionContextFromPlan(
            MockInterviewSession session,
            com.jobtracker.entity.MockInterviewEvaluation plan
    ) {
        return String.format("""
                # 任务
                请根据以下计划生成一个面试问题。

                # 问题计划
                - 技能: %s
                - 选题来源: %s
                - 问题类型: %s
                - 难度: %d
                - 上下文信息: %s
                - 选择原因: %s

                # 面试信息
                - 岗位: %s
                - 级别: %s
                - 当前轮次: %d

                # 要求
                1. 问题必须符合计划中的技能和难度要求
                2. 问题类型要匹配（开放性问题/技术问题/情景问题）
                3. 根据上下文信息调整问题细节
                4. 简历信息已在系统提示词中提供，请结合简历中的项目经验生成问题
                """,
                plan.getSkillName() != null ? plan.getSkillName() : "通用",
                plan.getTopicSource() != null ? plan.getTopicSource() : "UNKNOWN",
                plan.getQuestionType() != null ? plan.getQuestionType() : "OPEN_ENDED",
                plan.getPlannedDifficulty() != null ? plan.getPlannedDifficulty() : 3,
                plan.getContextInfo() != null ? plan.getContextInfo() : "无",
                plan.getReason() != null ? plan.getReason() : "无",
                session.getJobTitle(),
                session.getSeniorityLevel(),
                session.getCurrentRound()
        );
    }

    /**
     * 从 EvaluationResult 对象创建评分记录
     * <p>
     * ⭐ 关键：根据 session.getCurrentPlanId() 更新对应的计划记录
     * </p>
     */
    private MockInterviewEvaluation createEvaluationFromResult(
            MockInterviewSession session,
            String question,
            String userAnswer,
            com.jobtracker.agent.interview.dto.EvaluationResult evaluationResult
    ) {
        try {
            com.jobtracker.agent.interview.dto.ScoreDetail scores = evaluationResult.getScores();

            // ⭐ 关键修改：根据 currentPlanId 查找并更新已存在的计划记录
            Long currentPlanId = session.getCurrentPlanId();

            if (currentPlanId != null) {
                // 根据 planId 获取计划记录
                MockInterviewEvaluation existingPlan = evaluationService.getEvaluationById(currentPlanId);

                if (existingPlan != null) {
                    // 更新已存在的计划记录
                    existingPlan.setQuestionText(question);
                    existingPlan.setUserAnswer(userAnswer);
                    existingPlan.setTechnicalScore(BigDecimal.valueOf(
                            scores != null ? scores.getTechnical() : 2.5));
                    existingPlan.setLogicScore(BigDecimal.valueOf(
                            scores != null ? scores.getLogic() : 2.5));
                    existingPlan.setDepthScore(BigDecimal.valueOf(
                            scores != null ? scores.getDepth() : 2.5));
                    existingPlan.setTotalScore(BigDecimal.valueOf(
                            evaluationResult.getTotalScore() != null ?
                                    evaluationResult.getTotalScore() : 7.5));
                    existingPlan.setFeedback(evaluationResult.getFeedback());
                    existingPlan.setSuggestion(evaluationResult.getSuggestion());
                    existingPlan.setPlanStatus("COMPLETED");  // 更新状态为已完成
                    existingPlan.setEvaluatedAt(java.time.LocalDateTime.now());

                    // 保存更新
                    evaluationService.updateEvaluation(existingPlan);

                    log.info("更新评分记录成功（计划记录）: sessionId={}, planId={}, skill={}, score={}",
                            session.getSessionId(), currentPlanId, existingPlan.getSkillName(), existingPlan.getTotalScore());

                    return existingPlan;
                } else {
                    log.warn("未找到 planId={} 的计划记录，会话: {}", currentPlanId, session.getSessionId());
                }
            }

            // 如果找不到 currentPlanId（不应该发生），则创建新的评分记录
            log.warn("未找到 currentPlanId，创建新的评分记录，会话: {}, round: {}",
                    session.getSessionId(), session.getCurrentRound());

            MockInterviewEvaluation evaluation = MockInterviewEvaluation.builder()
                    .sessionId(session.getSessionId())
                    .roundNumber(session.getCurrentRound())
                    .skillId(0L)
                    .skillName("通用")
                    .questionText(question)
                    .userAnswer(userAnswer)
                    .technicalScore(BigDecimal.valueOf(
                            scores != null ? scores.getTechnical() : 2.5))
                    .logicScore(BigDecimal.valueOf(
                            scores != null ? scores.getLogic() : 2.5))
                    .depthScore(BigDecimal.valueOf(
                            scores != null ? scores.getDepth() : 2.5))
                    .totalScore(BigDecimal.valueOf(
                            evaluationResult.getTotalScore() != null ?
                                    evaluationResult.getTotalScore() : 7.5))
                    .feedback(evaluationResult.getFeedback())
                    .suggestion(evaluationResult.getSuggestion())
                    .planStatus("COMPLETED")
                    .evaluatedAt(java.time.LocalDateTime.now())
                    .build();

            return evaluationService.createEvaluation(evaluation);

        } catch (Exception e) {
            log.warn("从 EvaluationResult 创建评分记录失败，使用默认值，会话: {}, 轮次: {}",
                    session.getSessionId(), session.getCurrentRound(), e);

            // 创建失败，使用默认值
            MockInterviewEvaluation evaluation = MockInterviewEvaluation.builder()
                    .sessionId(session.getSessionId())
                    .roundNumber(session.getCurrentRound())
                    .skillId(0L)
                    .skillName("通用")
                    .questionText(question)
                    .userAnswer(userAnswer)
                    .technicalScore(BigDecimal.valueOf(2.5))
                    .logicScore(BigDecimal.valueOf(2.5))
                    .depthScore(BigDecimal.valueOf(2.5))
                    .totalScore(BigDecimal.valueOf(7.5))
                    .feedback("评估结果处理失败")
                    .planStatus("COMPLETED")
                    .evaluatedAt(java.time.LocalDateTime.now())
                    .build();

            return evaluationService.createEvaluation(evaluation);
        }
    }

    /**
     * 清理 Markdown 代码块标记
     * <p>
     * 用于处理 LLM 返回的包含 ```json ... ``` 标记的 JSON 内容
     * </p>
     *
     * @param text 原始文本
     * @return 清理后的纯 JSON 文本
     */
    private String cleanMarkdownCodeBlocks(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 移除 Markdown 代码块标记
        String cleaned = text
                .replaceAll("```json\\s*", "")   // 移除开头的 ```json
                .replaceAll("```\\s*$", "")      // 移除结尾的 ```
                .trim();

        log.debug("清理 Markdown 标记前长度: {}, 清理后长度: {}, 减少字符数: {}",
                text.length(), cleaned.length(), text.length() - cleaned.length());

        return cleaned;
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
