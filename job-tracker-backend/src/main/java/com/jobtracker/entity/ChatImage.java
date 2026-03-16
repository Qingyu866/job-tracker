package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天图片实体类
 * <p>
 * 存储用户上传的图片信息，包括文件路径、元数据等
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
@TableName("chat_images")
public class ChatImage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图片ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的消息ID
     */
    @TableField("message_id")
    private Long messageId;

    /**
     * 所属会话ID（冗余字段，用于安全校验）
     * <p>
     * 通过校验图片所属会话与请求会话是否一致，防止越权访问
     * </p>
     */
    @TableField("session_id")
    private Long sessionId;

    /**
     * 服务器内部相对路径
     * <p>
     * 例如：chat/2026-03-15/uuid-xxx.jpg
     * </p>
     */
    @TableField("file_path")
    private String filePath;

    /**
     * MIME 类型
     * <p>
     * 例如：image/jpeg, image/png
     * </p>
     */
    @TableField("mime_type")
    private String mimeType;

    /**
     * 原始文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 图片宽度（像素）
     */
    @TableField("width")
    private Integer width;

    /**
     * 图片高度（像素）
     */
    @TableField("height")
    private Integer height;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
