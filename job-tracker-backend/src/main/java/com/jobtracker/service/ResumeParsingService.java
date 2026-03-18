package com.jobtracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 简历智能解析服务
 * <p>
 * 职责：
 * 1. 从简历文本中提取结构化信息（技能、项目、工作年限）
 * 2. 为模拟面试提供高质量的上下文
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeParsingService {

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper;

    /**
     * 解析简历文本，提取结构化信息
     *
     * @param resumeText 简历文本（可以是纯文本或 OCR 识别结果）
     * @return 解析结果
     */
    public ResumeParseResult parseResume(String resumeText) {
        String prompt = buildResumeParsePrompt(resumeText);

        try {
            // 调用 LLM 解析
            String aiResponse = chatModel.generate(prompt);

            // 解析 JSON 响应
            ResumeParseResult result = objectMapper.readValue(
                    aiResponse,
                    ResumeParseResult.class
            );

            log.info("简历解析成功：{} 个技能，{} 个项目，工作年限 {} 年",
                    result.getSkills().size(),
                    result.getProjects().size(),
                    result.getWorkYears());

            return result;

        } catch (Exception e) {
            log.error("简历解析失败，使用默认值", e);
            // 返回空的解析结果
            return ResumeParseResult.builder()
                    .workYears(0)
                    .currentPosition("未知")
                    .skills(List.of())
                    .projects(List.of())
                    .workExperiences(List.of())
                    .build();
        }
    }

    /**
     * 构建简历解析提示词
     */
    private String buildResumeParsePrompt(String resumeText) {
        return String.format("""
                # 任务
                你是一个专业的简历解析专家。请从以下简历文本中提取结构化信息。

                # 简历文本
                %s

                # 提取要求

                ## 1. 基本信息
                - work_years: 工作年限（整数，单位：年）
                - current_position: 当前职位（字符串）

                ## 2. 技能列表（skills）
                提取简历中提到的所有技术技能，每个技能包含：
                - skill_name: 技能名称（例如：Redis、Spring Boot）
                - level: 熟练度（必须是以下之一：精通、熟练掌握、了解、略知）
                - years: 使用年限（整数，单位：年）
                - projects_used: 在哪些项目中使用过（数组）

                ## 3. 项目经验（projects）
                提取简历中的所有项目，每个项目包含：
                - project_name: 项目名称
                - role: 担任角色
                - duration: 项目时长（例如：3个月、1年）
                - tech_stack: 使用的技术栈（数组）
                - description: 项目描述（1-2句话）
                - responsibilities: 主要职责（数组）
                - achievements: 项目成就（数组）

                ## 4. 工作经历（work_experiences）
                提取工作经历，每段经历包含：
                - company: 公司名称
                - position: 职位
                - duration: 工作时长
                - description: 工作描述

                # ⚠️ 输出格式要求
                **严禁使用 Markdown 代码块！**
                - ❌ 错误示例：```json {...}```
                - ✅ 正确示例：{"work_years": 3, ...}
                - 直接输出纯 JSON，不要有任何标记
                - 不要包含 ```json 或 ``` 标记

                # 输出示例
                {
                  "work_years": 3,
                  "current_position": "Java后端工程师",
                  "skills": [
                    {
                      "skill_name": "Redis",
                      "level": "熟练掌握",
                      "years": 2,
                      "projects_used": ["电商秒杀系统", "订单管理系统"]
                    },
                    {
                      "skill_name": "Spring Boot",
                      "level": "精通",
                      "years": 3,
                      "projects_used": ["电商秒杀系统"]
                    }
                  ],
                  "projects": [
                    {
                      "project_name": "电商秒杀系统",
                      "role": "后端开发工程师",
                      "duration": "6个月",
                      "tech_stack": ["Java", "Spring Boot", "Redis", "Kafka", "MySQL"],
                      "description": "一个支持高并发的商品秒杀系统",
                      "responsibilities": [
                        "负责库存扣减模块的设计与开发",
                        "优化 Redis 缓存策略，提升系统性能"
                      ],
                      "achievements": [
                        "系统支持 QPS 10000+",
                        "库存扣减响应时间降低到 50ms"
                      ]
                    }
                  ],
                  "work_experiences": [
                    {
                      "company": "阿里巴巴集团",
                      "position": "Java后端工程师",
                      "duration": "2021.07 - 至今",
                      "description": "负责电商平台核心业务模块的开发"
                    }
                  ]
                }
                """, resumeText);
    }

    /**
     * 简历解析结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResumeParseResult {
        /**
         * 工作年限
         */
        private Integer workYears;

        /**
         * 当前职位
         */
        private String currentPosition;

        /**
         * 技能列表
         */
        private List<SkillInfo> skills;

        /**
         * 项目经验
         */
        private List<ProjectInfo> projects;

        /**
         * 工作经历
         */
        private List<WorkExperience> workExperiences;
    }

    /**
     * 技能信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SkillInfo {
        private String skillName;
        private String level;
        private Integer years;
        private List<String> projectsUsed;
    }

    /**
     * 项目信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProjectInfo {
        private String projectName;
        private String role;
        private String duration;
        private List<String> techStack;
        private String description;
        private List<String> responsibilities;
        private List<String> achievements;
    }

    /**
     * 工作经历
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WorkExperience {
        private String company;
        private String position;
        private String duration;
        private String description;
    }
}
