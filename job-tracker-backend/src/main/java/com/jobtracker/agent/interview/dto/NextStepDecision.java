package com.jobtracker.agent.interview.dto;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 副面试官决策结果
 * <p>
 * 用于副面试官 Agent 决定下一步面试流程
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextStepDecision {

    /**
     * 动作类型
     */
    @Description("下一步动作类型：继续提问、结束面试、切换到HR面试")
    private Action action;

    /**
     * 下一个选题
     */
    @Description("下一个要提问的话题，例如：Redis集群方案、Spring Boot自动配置原理")
    private String nextTopic;

    /**
     * 选题来源
     */
    @Description("选题来源分类：项目深挖、技能验证、JD要求覆盖、通用问题")
    private TopicSource topicSource;

    /**
     * 选择该选题的原因
     */
    @Description("选择该选题的原因，例如：简历声称精通Redis，需要验证深度")
    private String reason;

    /**
     * 问题类型
     */
    @Description("问题类型分类：项目深挖、技能验证、JD要求覆盖、通用问题、开放性问题")
    private QuestionType questionType;

    /**
     * 难度等级（1-5）
     */
    @Description("问题难度等级，范围1-5，1最简单，5最难")
    private Integer difficulty;

    /**
     * 动作类型枚举
     */
    public enum Action {
        /**
         * 继续提问
         */
        @Description("继续提问：进入下一轮面试提问")
        NEXT_QUESTION,

        /**
         * 结束面试
         */
        @Description("结束面试：结束技术面试，准备生成报告")
        FINISH_INTERVIEW,

        /**
         * 切换到 HR 面试
         */
        @Description("切换到HR面试：技术面试结束，切换到HR相关问题")
        SWITCH_TO_HR
    }

    /**
     * 选题来源枚举
     */
    public enum TopicSource {
        /**
         * 项目深挖
         */
        @Description("项目深挖：基于候选人简历中的项目经验进行深入提问")
        PROJECT_DEEP_DIVE,

        /**
         * 技能验证
         */
        @Description("技能验证：验证候选人简历中声称的技能水平")
        SKILL_VERIFICATION,

        /**
         * JD 要求覆盖
         */
        @Description("JD要求覆盖：覆盖JD中要求但简历未提及的技能")
        JD_REQUIREMENT,

        /**
         * 通用问题
         */
        @Description("通用问题：自我介绍、职业规划等通用性问题")
        GENERAL
    }

    /**
     * 问题类型枚举
     */
    public enum QuestionType {
        /**
         * 项目深挖
         */
        @Description("项目深挖：关于项目经验的深入问题")
        PROJECT_DEEP_DIVE,

        /**
         * 技能验证
         */
        @Description("技能验证：验证具体技能水平的问题")
        SKILL_VERIFICATION,

        /**
         * JD 要求覆盖
         */
        @Description("JD要求覆盖：JD要求但简历未提及的技能问题")
        JD_REQUIREMENT,

        /**
         * 通用问题
         */
        @Description("通用问题：自我介绍、职业规划等")
        GENERAL,

        /**
         * 开放性问题
         */
        @Description("开放性问题：没有固定答案的开放式问题")
        OPEN_ENDED
    }
}
