package com.jobtracker.dto;

import com.jobtracker.entity.ChatImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片附件 DTO
 * <p>
 * 返回给前端的图片信息，包含访问 URL
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
public class ImageAttachment {

    /**
     * 图片ID（用于访问接口）
     */
    private Long id;

    /**
     * 前端访问 URL
     * <p>
     * 格式：/api/chat/images/{id}?sessionId={sessionKey}
     * </p>
     */
    private String publicUrl;

    /**
     * MIME 类型
     */
    private String mimeType;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 图片宽度（像素）
     */
    private Integer width;

    /**
     * 图片高度（像素）
     */
    private Integer height;

    /**
     * 服务器内部文件路径（用于后端读取文件）
     * <p>
     * 例如：chat/2026-03-15/uuid-xxx.jpg
     * </p>
     */
    private String filePath;

    /**
     * 从 ChatImage 实体创建 DTO
     *
     * @param chatImage  图片实体
     * @param sessionKey 会话标识
     * @return ImageAttachment DTO
     */
    public static ImageAttachment fromEntity(ChatImage chatImage, String sessionKey) {
        return ImageAttachment.builder()
                .id(chatImage.getId())
                .publicUrl("/api/chat/images/" + chatImage.getId() + "?sessionId=" + sessionKey)
                .mimeType(chatImage.getMimeType())
                .fileName(chatImage.getFileName())
                .fileSize(chatImage.getFileSize())
                .width(chatImage.getWidth())
                .height(chatImage.getHeight())
                .filePath(chatImage.getFilePath())  // 添加文件路径
                .build();
    }
}
