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
    @TableId(type = IdType.AUTO)
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
     */
    public enum MessageRole {
        /**
         * AI/面试官
         */
        ASSISTANT,

        /**
         * 用户/候选人
         */
        USER
    }
}
