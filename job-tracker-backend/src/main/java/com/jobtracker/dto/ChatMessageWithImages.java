package com.jobtracker.dto;

import com.jobtracker.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息（含图片附件）DTO
 * <p>
 * 用于返回给前端的完整消息，包含关联的图片信息
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageWithImages {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 角色：user/assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 图片附件列表
     */
    private List<ImageAttachment> images;

    /**
     * 从 ChatMessage 实体创建 DTO
     *
     * @param message 消息实体
     * @return ChatMessageWithImages DTO
     */
    public static ChatMessageWithImages fromEntity(ChatMessage message) {
        return ChatMessageWithImages.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .images(null)  // 需要额外查询
                .build();
    }

    /**
     * 从 ChatMessage 实体创建 DTO（含图片）
     *
     * @param message 消息实体
     * @param images  图片列表
     * @return ChatMessageWithImages DTO
     */
    public static ChatMessageWithImages fromEntity(ChatMessage message, List<ImageAttachment> images) {
        return ChatMessageWithImages.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .images(images)
                .build();
    }
}
