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

        // ===== 新增：评估用户回答 =====
        // 获取上一轮的问题（用于构建评估上下文）
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

                // 调用评审专家评估（现在返回 EvaluationResult 对象）
                com.jobtracker.agent.interview.dto.EvaluationResult evaluationResult =
                        agents.evaluator().evaluate(evaluationContext);

                // 从 EvaluationResult 对象创建评分记录
                MockInterviewEvaluation evaluation = createEvaluationFromResult(
                        sessionId,
                        session.getCurrentRound(),
                        lastQuestion.getContent(),
                        request.getContent(),
                        evaluationResult
                );

                log.debug("创建评分记录成功，会话: {}, 轮次: {}, 分数: {}",
                        sessionId, session.getCurrentRound(), evaluation.getTotalScore());

                // ===== 新增：每轮对话后持久化 Agent 记忆 =====
                // 防止 Agent 在面试结束前丢失
                try {
                    agentFactory.persistMemories(sessionId);
                    log.debug("持久化 Agent 记忆成功，会话: {}, 轮次: {}", sessionId, session.getCurrentRound());
                } catch (Exception e) {
                    log.warn("持久化 Agent 记忆失败，会话: {}, 轮次: {}", sessionId, session.getCurrentRound(), e);
                }

            } catch (dev.langchain4j.service.output.OutputParsingException e) {
                // LLM 返回了 Markdown 代码块，尝试清理后重试
                log.warn("Agent 返回格式错误（可能是 Markdown 代码块），尝试清理后重新解析，会话: {}, 轮次: {}",
                        sessionId, session.getCurrentRound());

                try {
                    // 从异常信息中提取原始内容并清理
                    String rawContent = e.getMessage();
                    if (rawContent != null && (rawContent.contains("```json") || rawContent.contains("```"))) {
                        String cleanedContent = cleanMarkdownCodeBlocks(rawContent);

                        // 使用 ObjectMapper 手动解析
                        com.fasterxml.jackson.databind.ObjectMapper mapper =
                                new com.fasterxml.jackson.databind.ObjectMapper();
                        com.jobtracker.agent.interview.dto.EvaluationResult evaluationResult =
                                mapper.readValue(cleanedContent, com.jobtracker.agent.interview.dto.EvaluationResult.class);

                        // 从 EvaluationResult 对象创建评分记录
                        MockInterviewEvaluation evaluation = createEvaluationFromResult(
                                sessionId,
                                session.getCurrentRound(),
                                lastQuestion.getContent(),
                                request.getContent(),
                                evaluationResult
                        );

                        log.info("清理 Markdown 标记后成功解析，会话: {}, 轮次: {}, 分数: {}",
                                sessionId, session.getCurrentRound(), evaluation.getTotalScore());

                        // 持久化 Agent 记忆
                        try {
                            agentFactory.persistMemories(sessionId);
                        } catch (Exception persistException) {
                            log.warn("持久化 Agent 记忆失败", persistException);
                        }
                    }
                } catch (Exception cleaningException) {
                    log.error("清理 Markdown 标记后仍然解析失败，跳过本轮评分，会话: {}, 轮次: {}",
                            sessionId, session.getCurrentRound(), cleaningException);
                }
            } catch (Exception e) {
                log.warn("评估回答失败，跳过本轮评分，会话: {}, 轮次: {}",
                        sessionId, session.getCurrentRound(), e);
            }
        }

        // 副面试官决定下一步（现在返回 NextStepDecision 对象）
        String nextStepContext = buildNextStepContext(session, request.getContent());
        com.jobtracker.agent.interview.dto.NextStepDecision nextStepDecision;

        try {
            nextStepDecision = agents.viceInterviewer().decideNextStep(nextStepContext);
        } catch (dev.langchain4j.service.output.OutputParsingException e) {
            // LLM 返回了 Markdown 代码块，尝试清理后重试
            log.warn("decideNextStep 返回格式错误（可能是 Markdown 代码块），尝试清理后重新解析，会话: {}",
                    sessionId);

            try {
                String rawContent = e.getMessage();
                if (rawContent != null && (rawContent.contains("```json") || rawContent.contains("```"))) {
                    String cleanedContent = cleanMarkdownCodeBlocks(rawContent);

                    com.fasterxml.jackson.databind.ObjectMapper mapper =
                            new com.fasterxml.jackson.databind.ObjectMapper();
                    nextStepDecision = mapper.readValue(
                            cleanedContent,
                            com.jobtracker.agent.interview.dto.NextStepDecision.class
                    );

                    log.info("清理 Markdown 标记后成功解析 NextStepDecision，会话: {}", sessionId);
                } else {
                    // 无法恢复，使用默认决策
                    log.error("无法解析 NextStepDecision，使用默认决策继续面试，会话: {}", sessionId);
                    nextStepDecision = com.jobtracker.agent.interview.dto.NextStepDecision.builder()
                            .action(com.jobtracker.agent.interview.dto.NextStepDecision.Action.NEXT_QUESTION)
                            .nextTopic("请继续自我介绍")
                            .topicSource(com.jobtracker.agent.interview.dto.NextStepDecision.TopicSource.GENERAL)
                            .reason("解析失败，使用默认话题")
                            .questionType(com.jobtracker.agent.interview.dto.NextStepDecision.QuestionType.OPEN_ENDED)
                            .difficulty(3)
                            .build();
                }
            } catch (Exception cleaningException) {
                log.error("清理 Markdown 标记后仍然解析失败 NextStepDecision，使用默认决策，会话: {}",
                        sessionId, cleaningException);
                nextStepDecision = com.jobtracker.agent.interview.dto.NextStepDecision.builder()
                        .action(com.jobtracker.agent.interview.dto.NextStepDecision.Action.NEXT_QUESTION)
                        .nextTopic("请继续自我介绍")
                        .topicSource(com.jobtracker.agent.interview.dto.NextStepDecision.TopicSource.GENERAL)
                        .reason("解析失败，使用默认话题")
                        .questionType(com.jobtracker.agent.interview.dto.NextStepDecision.QuestionType.OPEN_ENDED)
                        .difficulty(3)
                        .build();
            }
        }

        // 主面试官生成问题
        String questionContext = buildQuestionContext(session, nextStepDecision);
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
        // 1. 结束面试状态
        interviewService.finishInterview(sessionId);
        MockInterviewSession session = interviewService.getSession(sessionId);
        if (session == null) {
            return Result.error("会话不存在");
        }

        // 2. 获取 Agent 组
        InterviewAgentFactory.InterviewAgents agents = agentFactory.getAgents(sessionId);
        if (agents == null) {
            log.warn("Agent 未初始化，使用简化报告生成，会话ID: {}", sessionId);
            return finishInterviewSimple(session, sessionId);
        }

        // 3. 获取所有评分记录（用于构建上下文）
        List<MockInterviewEvaluation> evaluations = evaluationService.getSessionEvaluations(sessionId);

        // 4. 构建完整报告生成上下文
        String reportContext = buildReportContext(session, evaluations);

        try {
            // 5. 调用评审专家生成可信度分析（现在返回 List<SkillCredibility>）
            List<com.jobtracker.agent.interview.dto.SkillCredibility> credibilityList =
                    agents.evaluator().generateCredibilityAnalysis(reportContext);

            // 序列化为 JSON 字符串存储
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String credibilityAnalysisJson = mapper.writeValueAsString(credibilityList);
            session.setResumeGapAnalysis(credibilityAnalysisJson);

            // 6. 计算总体可信度评分（现在返回 CredibilityScoreResult 对象）
            com.jobtracker.agent.interview.dto.CredibilityScoreResult credibilityScoreResult =
                    agents.evaluator().calculateCredibilityScore(reportContext);
            Double credibilityScore = credibilityScoreResult.getCredibilityScore();
            session.setResumeCredibilityScore(BigDecimal.valueOf(credibilityScore));

            // 7. 调用副面试官生成改进建议（现在返回 List<ImprovementSuggestion>）
            List<com.jobtracker.agent.interview.dto.ImprovementSuggestion> suggestions =
                    agents.viceInterviewer().generateSuggestions(reportContext);

            // 序列化为 JSON 字符串存储
            String suggestionsJson = mapper.writeValueAsString(suggestions);
            session.setImprovementSuggestions(suggestionsJson);

            // 8. 调用主面试官生成面试总结
            String summary = agents.mainInterviewer().generateSummary(reportContext);
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
     * 构建报告生成上下文
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

        // 简历快照
        context.append("# 简历信息\n");
        if (session.getResumeSnapshot() != null) {
            context.append(session.getResumeSnapshot().toString());
        }
        context.append("\n");

        // JD 要求
        context.append("# 岗位要求\n");
        if (session.getJdSnapshot() != null) {
            context.append(session.getJdSnapshot().toString());
        }
        context.append("\n");

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
                agents.evaluator().evaluate(context);

        // 从 EvaluationResult 对象创建评分记录
        MockInterviewEvaluation evaluation = createEvaluationFromResult(
                sessionId,
                request.getRoundNumber(),
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
     * 构建提问上下文
     */
    private String buildQuestionContext(
            MockInterviewSession session,
            com.jobtracker.agent.interview.dto.NextStepDecision nextStepDecision
    ) {
        return String.format("""
                下一步决策: %s
                选题来源: %s
                选择原因: %s
                难度等级: %d

                岗位: %s
                级别: %s
                简历快照: %s
                JD 快照: %s
                """,
                nextStepDecision.getNextTopic(),
                nextStepDecision.getTopicSource(),
                nextStepDecision.getReason(),
                nextStepDecision.getDifficulty(),
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
     * 构建单轮评估上下文
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

                # 简历快照
                %s

                # JD 要求
                %s
                """,
                roundNumber,
                question,
                userAnswer,
                session.getResumeSnapshot() != null ? session.getResumeSnapshot() : "无",
                session.getJdSnapshot() != null ? session.getJdSnapshot() : "无"
        );
    }

    /**
     * 从 EvaluationResult 对象创建评分记录
     * <p>
     * LangChain4j 会自动将 Agent 返回的 JSON 反序列化为 EvaluationResult 对象
     * </p>
     */
    private MockInterviewEvaluation createEvaluationFromResult(
            String sessionId,
            Integer roundNumber,
            String question,
            String userAnswer,
            com.jobtracker.agent.interview.dto.EvaluationResult evaluationResult
    ) {
        try {
            com.jobtracker.agent.interview.dto.ScoreDetail scores = evaluationResult.getScores();

            // 创建评分记录
            MockInterviewEvaluation evaluation = MockInterviewEvaluation.builder()
                    .sessionId(sessionId)
                    .roundNumber(roundNumber)
                    .skillId(0L)  // 0 表示通用技能，未分类
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
                    .build();

            return evaluationService.createEvaluation(evaluation);

        } catch (Exception e) {
            log.warn("从 EvaluationResult 创建评分记录失败，使用默认值，会话: {}, 轮次: {}",
                    sessionId, roundNumber, e);

            // 创建失败，使用默认值
            MockInterviewEvaluation evaluation = MockInterviewEvaluation.builder()
                    .sessionId(sessionId)
                    .roundNumber(roundNumber)
                    .skillId(0L)  // 0 表示通用技能
                    .skillName("通用")
                    .questionText(question)
                    .userAnswer(userAnswer)
                    .technicalScore(BigDecimal.valueOf(2.5))
                    .logicScore(BigDecimal.valueOf(2.5))
                    .depthScore(BigDecimal.valueOf(2.5))
                    .totalScore(BigDecimal.valueOf(7.5))
                    .feedback("评估结果处理失败")
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
