package com.jobtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jobtracker.entity.ChatImage;
import com.jobtracker.mapper.ChatImageMapper;
import com.jobtracker.mapper.ChatMessageMapper;
import com.jobtracker.mapper.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 聊天图片服务
 * <p>
 * 提供图片的保存、查询和安全校验功能
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatImageService {

    private final ChatImageMapper chatImageMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final FileStorageService fileStorageService;

    /**
     * 保存图片
     * <p>
     * 保存文件到磁盘，并在数据库中创建记录
     * 图片暂时不关联消息（messageId 为 null），待用户发送消息后再关联
     * </p>
     *
     * @param file       上传的文件
     * @param sessionKey 会话标识（用于获取 sessionId）
     * @return 保存的图片实体
     * @throws IOException 文件保存失败
     * @throws IllegalArgumentException 会话不存在
     */
    public ChatImage saveImage(MultipartFile file, String sessionKey) throws IOException {
        // 1. 查询会话获取 sessionId
        var session = chatSessionMapper.selectBySessionKey(sessionKey);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在：sessionKey=" + sessionKey);
        }

        // 2. 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只支持图片文件");
        }

        // 3. 保存文件到磁盘并获取相对路径
        String relativePath = fileStorageService.saveFileAndGetPath(file);

        // 4. 保存图片记录到数据库（messageId 暂时为 null）
        ChatImage chatImage = ChatImage.builder()
                .messageId(null)  // 暂时不关联消息
                .sessionId(session.getId())  // 冗余存储会话ID，用于安全校验
                .filePath(relativePath)
                .mimeType(contentType)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .build();

        chatImageMapper.insert(chatImage);
        log.info("图片已保存：id={}, sessionKey={}, fileName={}",
                chatImage.getId(), sessionKey, file.getOriginalFilename());

        return chatImage;
    }

    /**
     * 更新图片关联的消息ID
     * <p>
     * 当用户发送消息时，将之前上传的图片关联到该消息
     * </p>
     *
     * @param imageIds   图片ID列表
     * @param messageId 消息ID
     */
    public void updateMessageId(List<Long> imageIds, Long messageId) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }

        // 使用 LambdaUpdateWrapper 批量更新
        chatImageMapper.update(null,
                new LambdaUpdateWrapper<ChatImage>()
                        .in(ChatImage::getId, imageIds)
                        .set(ChatImage::getMessageId, messageId)
        );

        log.info("已更新图片关联：messageId={}, imageIds={}, count={}",
                messageId, imageIds, imageIds.size());
    }

    /**
     * 根据 ID 和 sessionKey 获取图片（带安全校验）
     * <p>
     * 校验图片所属会话与请求会话是否一致，防止越权访问
     * </p>
     *
     * @param imageId    图片ID
     * @param sessionKey 会话标识（前端传入）
     * @return 图片实体
     * @throws IllegalArgumentException 图片或会话不存在
     * @throws SecurityException 无权访问该图片
     */
    public ChatImage getImageWithAuth(Long imageId, String sessionKey) {
        // 1. 查询图片
        ChatImage chatImage = chatImageMapper.selectById(imageId);
        if (chatImage == null) {
            throw new IllegalArgumentException("图片不存在：imageId=" + imageId);
        }

        // 2. 查询会话
        var session = chatSessionMapper.selectBySessionKey(sessionKey);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在：sessionKey=" + sessionKey);
        }

        // 3. 安全校验：图片所属会话 == 当前会话
        if (!chatImage.getSessionId().equals(session.getId())) {
            log.warn("图片访问被拒绝：imageId={}, imageSessionId={}, requestSessionId={}",
                    imageId, chatImage.getSessionId(), session.getId());
            throw new SecurityException("无权访问该图片");
        }

        return chatImage;
    }

    /**
     * 获取消息关联的所有图片
     *
     * @param messageId 消息ID
     * @return 图片列表（按创建时间升序）
     */
    public List<ChatImage> getImagesByMessageId(Long messageId) {
        return chatImageMapper.selectList(
                new LambdaQueryWrapper<ChatImage>()
                        .eq(ChatImage::getMessageId, messageId)
                        .orderByAsc(ChatImage::getCreatedAt)
        );
    }

    /**
     * 根据 ID 获取图片（无校验）
     *
     * @param imageId 图片ID
     * @return 图片实体，不存在则返回 null
     */
    public ChatImage getById(Long imageId) {
        return chatImageMapper.selectById(imageId);
    }
}
