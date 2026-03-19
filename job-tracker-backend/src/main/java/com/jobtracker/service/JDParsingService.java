package com.jobtracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * JD 智能解析服务
 * <p>
 * 职责：
 * 1. 从 JD 文本中提取结构化技能要求
 * 2. 区分核心技能和加分技能
 * 3. 为模拟面试提供技能验证基准
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JDParsingService {

    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;

    /**
     * 解析 JD 文本，提取技能要求
     *
     * @param jdText JD 文本
     * @return 解析结果
     */
    public JDParseResult parseJD(String jdText) {
        String prompt = buildJDParsePrompt(jdText);

        try {
            // 调用 LLM 解析
            String aiResponse = chatModel.chat(prompt);

            // 清理可能的 Markdown 标记
            String cleanedResponse = cleanMarkdownCodeBlocks(aiResponse);

            // 解析 JSON 响应
            JDParseResult result = objectMapper.readValue(
                    cleanedResponse,
                    JDParseResult.class
            );

            log.info("JD 解析成功：{} 个核心技能，{} 个加分技能",
                    result.getCoreSkills().size(),
                    result.getOptionalSkills().size());

            return result;

        } catch (Exception e) {
            log.error("JD 解析失败，使用默认值", e);
            // 返回空的解析结果
            return JDParseResult.builder()
                    .coreSkills(List.of())
                    .optionalSkills(List.of())
                    .minWorkYears(0)
                    .preferredWorkYears(0)
                    .build();
        }
    }

    /**
     * 清理 Markdown 代码块标记
     */
    private String cleanMarkdownCodeBlocks(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return text
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*$", "")
                .trim();
    }

    /**
     * 构建 JD 解析提示词
     */
    private String buildJDParsePrompt(String jdText) {
        return String.format("""
                # 任务
                你是一个专业的 JD（职位描述）解析专家。请从以下 JD 文本中提取技能要求。

                # JD 文本
                %s

                # 提取要求

                ## 1. 核心技能（core_skills）
                提取 JD 中明确要求必须掌握的技能，每个技能包含：
                - skill_name: 技能名称
                - requirement_level: 要求程度（必须是以下之一：精通、熟练掌握、了解、熟悉）
                - importance: 重要性（1-5，5最重要）
                - description: 技能描述

                ## 2. 加分技能（optional_skills）
                提取 JD 中提到的"加分"、"优先"等非必须的技能，格式同上。

                ## 3. 工作年限要求
                - min_work_years: 最低工作年限（整数）
                - preferred_work_years: 期望工作年限（整数）

                # ⚠️ 输出格式要求
                **严禁使用 Markdown 代码块！**
                - ❌ 错误示例：```json {...}```
                - ✅ 正确示例：{"core_skills": [...], ...}
                - 直接输出纯 JSON，不要有任何标记

                # 输出示例
                {
                  "core_skills": [
                    {
                      "skill_name": "Java",
                      "requirement_level": "精通",
                      "importance": 5,
                      "description": "扎实的 Java 基础，熟悉 JVM 原理"
                    },
                    {
                      "skill_name": "Spring Boot",
                      "requirement_level": "熟练掌握",
                      "importance": 5,
                      "description": "熟练使用 Spring Boot 进行微服务开发"
                    },
                    {
                      "skill_name": "Redis",
                      "requirement_level": "熟练掌握",
                      "importance": 4,
                      "description": "熟悉 Redis 的缓存、持久化、集群模式"
                    },
                    {
                      "skill_name": "MySQL",
                      "requirement_level": "熟练掌握",
                      "importance": 4,
                      "description": "熟悉 MySQL 索引优化、事务隔离级别"
                    }
                  ],
                  "optional_skills": [
                    {
                      "skill_name": "Kafka",
                      "requirement_level": "了解",
                      "importance": 3,
                      "description": "了解消息队列的基本原理和使用场景"
                    },
                    {
                      "skill_name": "Docker",
                      "requirement_level": "了解",
                      "importance": 2,
                      "description": "了解容器化部署"
                    }
                  ],
                  "min_work_years": 3,
                  "preferred_work_years": 5
                }
                """, jdText);
    }

    /**
     * JD 解析结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class JDParseResult {
        /**
         * 核心技能列表
         */
        private List<SkillRequirement> coreSkills;

        /**
         * 加分技能列表
         */
        private List<SkillRequirement> optionalSkills;

        /**
         * 最低工作年限
         */
        private Integer minWorkYears;

        /**
         * 期望工作年限
         */
        private Integer preferredWorkYears;
    }

    /**
     * 技能要求
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SkillRequirement {
        /**
         * 技能名称
         */
        private String skillName;

        /**
         * 要求程度
         */
        private String requirementLevel;

        /**
         * 重要性（1-5）
         */
        private Integer importance;

        /**
         * 技能描述
         */
        private String description;
    }
}
