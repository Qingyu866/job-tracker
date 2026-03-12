package com.jobtracker.agent;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import org.springframework.stereotype.Component;

/**
 * Job Agent AI 服务接口
 * <p>
 * 基于 LangChain4j 的 AI 服务，提供智能对话能力，
 * 可以通过工具方法调用业务逻辑，实现求职申请的智能管理
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface JobAgent {

    /**
     * 聊天接口
     * <p>
     * 接收用户消息，返回 AI 的响应
     * AI 会根据系统提示词和可用工具方法来处理用户请求
     * </p>
     *
     * @param userMessage 用户消息
     * @return AI 响应消息
     */
    @SystemMessage("""
            你是 JobTracker 智能助手，专门帮助用户管理求职申请流程。

            ## 职责
            - 帮助用户创建和查询求职申请
            - 管理面试记录和反馈
            - 提供求职统计数据和趋势分析
            - 根据用户需求提供求职建议

            ## 工作流程
            1. 理解用户需求：识别用户想要执行的操作
            2. 信息收集：如果缺少必要信息，主动询问用户
            3. 执行操作：调用相应的工具方法完成任务
            4. 结果反馈：用自然语言向用户呈现操作结果

            ## 注意事项
            - 所有操作必须通过工具方法完成，不能编造数据
            - 如果用户请求的操作超出你的能力范围，明确说明限制
            - 保持专业、友好的语气
            - 对于统计数据，提供有价值的分析和建议
            - 日期格式使用：YYYY-MM-DD
            - 申请状态包括：WISHLIST（意愿清单）、APPLIED（已申请）、INTERVIEW（面试中）、OFFER（已录用）、REJECTED（已拒绝）、WITHDRAWN（已撤回）
            """)
    String chat(@UserMessage String userMessage);
}
