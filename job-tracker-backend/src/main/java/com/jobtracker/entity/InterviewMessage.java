package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 面试对话记录实体
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("mock_interview_messages")
public class InterviewMessage {

    /**
     * 消息ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long messageId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 轮次号
     */
    private Integer roundNumber;

    /**
     * 轮内序号（1=问题，2=回答，3=追问...）
     */
    private Integer sequenceInRound;

    /**
     * 角色：ASSISTANT/USER
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 关联的知识点ID
     */
    private Long skillId;

    /**
     * 知识点名称（冗余，便于查询）
     */
    private String skillName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 消息角色枚举
     * <p>
     * 定义面试对话中消息的发送者类型
     * </p>
     */
    public enum MessageRole {
        /**
         * AI 面试官
         * <p>
         * 由 AI Agent 生成的消息，包括：
         * </p>
         * <ul>
         *   <li><b>主面试官</b>：提出的技术问题</li>
         *   <li><b>副面试官</b>：决策和选题（内部使用，不对用户展示）</li>
         *   <li><b>评审专家</b>：评估结果（内部使用，不对用户展示）</li>
         * </ul>
         * <p>
         * <b>存储值</b>: "ASSISTANT"
         * </p>
         * <p>
         * <b>特点</b>:
         * </p>
         * <ul>
         *   <li>由 LangChain4j Agent 生成</li>
         *   <li>存储在独立的 ChatMemory 中</li>
         *   <li>包含问题的 skillId 和 skillName（可选）</li>
         * </ul>
         */
        ASSISTANT,

        /**
         * 用户/候选人
         * <p>
         * 用户（候选人）发送的回答
         * </p>
         * <p>
         * <b>存储值</b>: "USER"
         * </p>
         * <p>
         * <b>特点</b>:
         * </p>
         * <ul>
         *   <li>由用户通过前端界面输入</li>
         *   <li>会被三个 Agent 同时处理</li>
         *   <li>触发评审专家的评估流程</li>
         * </ul>
         * <p>
         * <b>触发流程</b>:
         * </p>
         * <ol>
         *   <li>用户发送消息</li>
         *   <li>保存为 USER 消息</li>
         *   <li>评审专家评估（生成评分记录）</li>
         *   <li>副面试官决策下一步</li>
         *   <li>主面试官生成新问题</li>
         *   <li>保存为 ASSISTANT 消息</li>
         * </ol>
         */
        USER
    }
}
