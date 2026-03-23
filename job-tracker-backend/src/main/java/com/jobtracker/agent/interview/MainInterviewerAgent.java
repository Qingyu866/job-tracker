package com.jobtracker.agent.interview;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 主面试官 Agent
 * <p>
 * 职责：基于 JD + 简历进行提问，深度挖掘项目经历和声称技能
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
public interface MainInterviewerAgent {

    /**
     * 根据上下文提问
     *
     * @param context 上下文信息（包含 JD、简历、已考察知识点等）
     * @return 生成的问题
     */
    @SystemMessage(fromResource = "/prompts/system/interview/main-interviewer.txt")
    String askQuestion(
        @UserMessage String context,
        @V("current_date") String currentDate,
        @V("current_time") String currentTime,
        @V("context") String basicContext,
        @V("resume_snapshot") String resumeSnapshot,
        @V("jd_snapshot") String jdSnapshot
    );

    /**
     * 生成面试总结
     * <p>
     * 在面试结束时调用，对整个面试过程进行总结
     * 包括：整体表现、优势亮点、待改进方面、推荐方向等
     * </p>
     *
     * @param context 完整的面试上下文（包含所有对话记录、评分结果、简历信息等）
     * @return 面试总结文本（纯文本，200-500 字）
     */
    @SystemMessage(fromResource = "/prompts/system/interview/main-interviewer.txt")
    String generateSummary(
        @UserMessage String context,
        @V("current_date") String currentDate,
        @V("current_time") String currentTime,
        @V("context") String basicContext,
        @V("resume_snapshot") String resumeSnapshot,
        @V("jd_snapshot") String jdSnapshot
    );
}
