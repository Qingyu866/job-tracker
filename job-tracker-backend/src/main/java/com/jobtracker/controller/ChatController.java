package com.jobtracker.controller;

import com.jobtracker.common.result.Result;
import com.jobtracker.dto.ChatMessageWithImages;
import com.jobtracker.entity.ChatImage;
import com.jobtracker.entity.ChatMessage;
import com.jobtracker.entity.ChatSession;
import com.jobtracker.entity.ToolCallRecord;
import com.jobtracker.service.ChatHistoryService;
import com.jobtracker.service.ChatImageService;
import com.jobtracker.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 聊天控制器
 * <p>
 * 提供聊天会话、消息历史和图片管理接口
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatHistoryService chatHistoryService;
    private final ChatImageService chatImageService;
    private final FileStorageService fileStorageService;

    /**
     * 获取所有会话列表
     *
     * @return 会话列表
     */
    @GetMapping("/sessions")
    public Result<List<ChatSession>> getAllSessions() {
        List<ChatSession> sessions = chatHistoryService.getAllSessions();
        return Result.success("查询成功", sessions);
    }

    /**
     * 获取会话消息历史
     *
     * @param sessionKey 会话标识
     * @return 消息列表
     */
    @GetMapping("/sessions/{sessionKey}/messages")
    public Result<List<ChatMessage>> getSessionMessages(@PathVariable String sessionKey) {
        List<ChatMessage> messages = chatHistoryService.getSessionMessages(sessionKey);
        return Result.success("查询成功", messages);
    }

    /**
     * 获取会话消息历史（含图片信息）
     * <p>
     * 返回的消息列表包含每条消息关联的图片附件
     * </p>
     *
     * @param sessionKey 会话标识
     * @return 消息列表（含图片）
     */
    @GetMapping("/sessions/{sessionKey}/messages-with-images")
    public Result<List<ChatMessageWithImages>> getSessionMessagesWithImages(@PathVariable String sessionKey) {
        List<ChatMessageWithImages> messages = chatHistoryService.getSessionMessagesWithImages(sessionKey);
        return Result.success("查询成功", messages);
    }

    /**
     * 删除会话
     *
     * @param sessionKey 会话标识
     * @return 删除结果
     */
    @DeleteMapping("/sessions/{sessionKey}")
    public Result<Boolean> deleteSession(@PathVariable String sessionKey) {
        chatHistoryService.deleteSession(sessionKey);
        return Result.success("删除成功", true);
    }

    /**
     * 获取消息的工具调用记录
     *
     * @param messageId 消息ID
     * @return 工具调用记录列表
     */
    @GetMapping("/messages/{messageId}/tool-calls")
    public Result<List<ToolCallRecord>> getToolCallRecords(@PathVariable Long messageId) {
        List<ToolCallRecord> records = chatHistoryService.getToolCallRecords(messageId);
        return Result.success("查询成功", records);
    }

    // ==================== 图片相关接口 ====================

    /**
     * 获取图片（带安全校验）
     * <p>
     * 访问格式：GET /chat/images/{imageId}?sessionId={sessionKey}
     * </p>
     *
     * @param imageId    图片ID
     * @param sessionId 会话标识（用于安全校验）
     * @return 图片文件
     */
    @GetMapping("/images/{imageId}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable Long imageId,
            @RequestParam String sessionId) {

        try {
            // 1. 带安全校验获取图片信息
            ChatImage chatImage = chatImageService.getImageWithAuth(imageId, sessionId);

            // 2. 读取文件
            byte[] imageBytes = fileStorageService.readFileAsBytes(chatImage.getFilePath());

            // 3. 返回图片（带缓存）
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(chatImage.getMimeType()))
                    .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                    .body(imageBytes);

        } catch (IllegalArgumentException e) {
            log.warn("图片不存在：imageId={}, sessionId={}", imageId, sessionId);
            return ResponseEntity.notFound().build();

        } catch (SecurityException e) {
            log.warn("图片访问被拒绝：imageId={}, sessionId={}", imageId, sessionId);
            return ResponseEntity.status(403).build();

        } catch (IOException e) {
            log.error("读取图片失败：imageId={}, sessionId={}", imageId, sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 上传图片
     * <p>
     * 用户可以在发送消息前先上传图片，返回 imageId 供后续使用
     * </p>
     *
     * @param file       上传的文件
     * @param sessionKey 会话标识（用于安全校验）
     * @return 图片信息（包含 imageId 和 publicUrl）
     */
    @PostMapping("/upload/image")
    public Result<ChatImage> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sessionKey") String sessionKey) {
        try {
            ChatImage chatImage = chatImageService.saveImage(file, sessionKey);
            return Result.success("上传成功", chatImage);
        } catch (IOException e) {
            log.error("文件上传失败：sessionKey={}", sessionKey, e);
            return Result.error("文件上传失败：" + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("上传失败：sessionKey={}", sessionKey, e);
            return Result.error(e.getMessage());
        }
    }
}
