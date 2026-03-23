package com.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考察计划 DTO
 * <p>
 * 用于 ViceInterviewerAgent 返回的考察计划
 * LangChain4j 会自动将 AI 返回的 JSON 解析为该对象
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionPlanDTO {

    /**
     * 轮次（从1开始，最大值由配置文件决定）
     */
    private Integer roundNumber;

    /**
     * 要考察的技能名称
     */
    private String skillName;

    /**
     * 选题来源：PROJECT_DEEP_DIVE, SKILL_VERIFICATION, JD_REQUIREMENT, GENERAL
     */
    private String topicSource;

    /**
     * 问题类型：PROJECT_DEEP_DIVE, SKILL_VERIFICATION, JD_REQUIREMENT, GENERAL, OPEN_ENDED
     */
    private String questionType;

    /**
     * 难度等级（1-5）
     */
    private Integer difficulty;

    /**
     * 上下文信息（例如：简历声称精通Redis）
     */
    private String contextInfo;

    /**
     * 选择该技能的原因
     */
    private String reason;
}
