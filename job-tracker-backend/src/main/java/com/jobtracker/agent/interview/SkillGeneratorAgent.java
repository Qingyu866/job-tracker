package com.jobtracker.agent.interview;

import com.jobtracker.agent.interview.dto.SkillTagListResponse;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 技能生成 Agent
 * <p>
 * 职责：根据技能名称生成完整的技能标签信息
 * <p>
 * LangChain4j 会自动将 AI 返回的 JSON 解析为 SkillTagListResponse
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-19
 */
public interface SkillGeneratorAgent {

    /**
     * 生成技能标签
     * <p>
     * LangChain4j 会自动将 AI 返回的 JSON 解析为 SkillTagListResponse
     * </p>
     *
     * @param skillNames 技能名称列表
     * @return 技能标签列表响应
     */
    @SystemMessage(fromResource = "/prompts/system/interview/skill-generator.txt")
    SkillTagListResponse generateSkillTags(@UserMessage String skillNames);
}
