package com.jobtracker.service;

import com.jobtracker.dto.ImageAttachment;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件存储服务
 * <p>
 * 负责将上传的文件保存到本地磁盘
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-15
 */
@Slf4j
@Service
public class FileStorageService {

    /**
     * 文件上传基础目录（配置值）
     */
    @Value("${file.upload-dir:./uploads/chat}")
    private String configuredUploadDir;

    /**
     * 实际使用的绝对路径
     */
    private String baseUploadDir;

    /**
     * API 基础 URL
     */
    @Value("${app.api-base-url:/api}")
    private String apiBaseUrl;

    /**
     * 初始化：将相对路径转换为绝对路径
     */
    @PostConstruct
    public void init() {
        File dir = new File(configuredUploadDir);

        if (dir.isAbsolute()) {
            // 已经是绝对路径，直接使用
            baseUploadDir = configuredUploadDir;
        } else {
            // 相对路径：转换为相对于项目根目录的绝对路径
            String userDir = System.getProperty("user.dir");
            baseUploadDir = new File(userDir, configuredUploadDir).getAbsolutePath();
        }

        log.info("文件上传目录：{}", baseUploadDir);

        // 确保基础目录存在
        File baseDir = new File(baseUploadDir);
        if (!baseDir.exists()) {
            boolean created = baseDir.mkdirs();
            if (created) {
                log.info("创建基础上传目录：{}", baseUploadDir);
            }
        }
    }

    /**
     * 保存上传的文件并返回相对路径
     * <p>
     * 内部方法，用于需要 filePath 的场景
     * </p>
     *
     * @param file 上传的文件
     * @return 相对路径
     * @throws IOException 文件保存失败
     */
    public String saveFileAndGetPath(MultipartFile file) throws IOException {
        // 1. 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只支持图片文件");
        }

        // 2. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        // 3. 按日期组织目录：chat/2026-03-15/
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String relativeDir = "chat" + File.separator + dateDir + File.separator;
        String fullDir = baseUploadDir + File.separator + relativeDir;

        // 4. 确保目录存在
        File directory = new File(fullDir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("创建目录：{}", fullDir);
            }
        }

        // 5. 保存文件
        File targetFile = new File(fullDir, uniqueFileName);
        file.transferTo(targetFile);

        log.info("文件已保存：path={}, size={}", targetFile.getAbsolutePath(), file.getSize());

        // 6. 返回相对路径（使用正斜杠）
        return relativeDir.replace(File.separatorChar, '/') + uniqueFileName;
    }

    /**
     * 保存上传的文件
     *
     * @param file 上传的文件
     * @return 文件访问信息（用于前端）
     * @throws IOException 文件保存失败
     */
    public ImageAttachment saveFile(MultipartFile file) throws IOException {
        // 1. 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只支持图片文件");
        }

        // 2. 保存文件并获取路径
        String relativePath = saveFileAndGetPath(file);

        // 3. 构建返回信息
        return ImageAttachment.builder()
                .publicUrl(apiBaseUrl + "/chat/images/" + relativePath)
                .mimeType(contentType)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .build();
    }

    /**
     * 读取文件并转换为字节数组
     *
     * @param filePath 相对路径
     * @return 文件内容
     * @throws IOException 文件读取失败
     */
    public byte[] readFileAsBytes(String filePath) throws IOException {
        String fullPath = getFullPath(filePath);
        Path path = Paths.get(fullPath);

        if (!Files.exists(path)) {
            throw new IOException("文件不存在：" + fullPath);
        }

        return Files.readAllBytes(path);
    }

    /**
     * 检查文件是否存在
     *
     * @param filePath 相对路径
     * @return 文件是否存在
     */
    public boolean fileExists(String filePath) {
        try {
            String fullPath = getFullPath(filePath);
            return Files.exists(Paths.get(fullPath));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 删除文件
     *
     * @param filePath 相对路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String filePath) {
        try {
            String fullPath = getFullPath(filePath);
            Path path = Paths.get(fullPath);

            if (Files.exists(path)) {
                Files.delete(path);
                log.info("文件已删除：path={}", fullPath);
                return true;
            }

            return false;
        } catch (IOException e) {
            log.error("删除文件失败：path={}", filePath, e);
            return false;
        }
    }

    /**
     * 获取文件的完整路径
     *
     * @param filePath 相对路径
     * @return 完整路径
     */
    public String getFullPath(String filePath) {
        // 标准化路径分隔符
        String normalizedPath = filePath.replace('\\', '/');
        return baseUploadDir + File.separator + normalizedPath.replace('/', File.separatorChar);
    }
}
