package com.jobtracker.agent.interview;

import com.jobtracker.agent.interview.dto.NextStepDecision;
import com.jobtracker.agent.interview.dto.ImprovementSuggestion;
import com.jobtracker.dto.QuestionPlanDTO;

import java.util.List;

/**
 * 副面试官 Agent
 * <p>
 * 职责：选题、去重、状态流转
 * 重点：平衡 JD 要求 vs 简历内容
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
public interface ViceInterviewerAgent {

    /**
     * 生成完整的考察计划（25轮）
     * <p>
     * 在创建会话时调用，生成所有轮次的考察计划
     * LangChain4j 会自动将 AI 返回的 JSON 解析为 List&lt;QuestionPlanDTO&gt;
     * </p>
     *
     * @param context 上下文信息（包含 JD 和简历数据）
     * @return 考察计划列表
     */
    List<QuestionPlanDTO> generateQuestionPlan(String context);

    /**
     * 决定下一个步骤
     *
     * @param context 上下文信息（包含 JD、简历、已考察知识点、用户最新回答等）
     * @return 下一步决策对象（LangChain4j 会自动处理序列化）
     */
    NextStepDecision decideNextStep(String context);

    /**
     * 生成改进建议
     * <p>
     * 在面试结束时调用，基于整个面试过程的表现
     * 生成针对性的改进建议和学习资源推荐
     * </p>
     *
     * @param context 完整的面试上下文（包含所有对话记录、评分结果等）
     * @return 改进建议列表（LangChain4j 会自动处理序列化）
     */
    List<ImprovementSuggestion> generateSuggestions(String context);
}
