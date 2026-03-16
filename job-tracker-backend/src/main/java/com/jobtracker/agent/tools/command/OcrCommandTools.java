package com.jobtracker.agent.tools.command;

import com.jobtracker.agent.tools.shared.ToolResult;
import com.jobtracker.ocr.OcrHelper;
import com.jobtracker.ocr.dto.OcrOptions;
import com.jobtracker.ocr.dto.OcrResult;
import com.jobtracker.ocr.service.OcrRecordService;
import com.jobtracker.ocr.service.OcrService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * OCR 识别工具（AI Agent 可调用）
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OcrCommandTools {

    private final OcrService ocrService;
    private final OcrHelper ocrHelper;
    private final OcrRecordService ocrRecordService;

    /**
     * 识别图片中的文字
     */
    @Tool("""
        [识别] 识别图片中的文字内容

        适用场景：
        - 用户说"识别一下这张图片"
        - 用户说"这是什么内容"
        - 用户上传图片并询问内容

        参数：
        - imageData: Base64编码的图片数据

        说明：
        - 优先使用智谱 OCR 云服务
        - 云服务失败时降级到本地模型
        - 返回：识别的文本内容
        """)
    public ToolResult recognizeImage(String imageData) {
        log.info("AI调用：OCR 识别图片");

        if (imageData == null || imageData.isBlank()) {
            return ToolResult.error("PARAM_MISSING", "图片数据不能为空");
        }

        try {
            // 识别图片
            OcrResult result = ocrHelper.recognizeFromBase64(imageData, OcrOptions.defaultOptions());

            if (!result.isSuccess()) {
                return ToolResult.error("OCR_FAILED",
                        "图片识别失败: " + result.getErrorMessage());
            }

            // 格式化结果供 AI 使用
            String formattedText = ocrHelper.formatOcrResult(result);

            return ToolResult.success("图片识别成功", formattedText);

        } catch (Exception e) {
            log.error("OCR 识别异常", e);
            return ToolResult.error("OCR_ERROR", "识别失败: " + e.getMessage());
        }
    }

    /**
     * 识别简历图片并解析
     */
    @Tool("""
        [识别] 识别简历图片并解析结构化信息

        适用场景：
        - 用户上传简历图片
        - 用户说"解析一下我的简历"

        参数：
        - imageData: Base64编码的简历图片

        说明：
        - 识别简历基本信息（工作年限、当前职位、技能等）
        - 自动创建 user_resumes 记录
        - 返回：解析后的简历信息
        """)
    public ToolResult recognizeResume(String imageData) {
        log.info("AI调用：OCR 识别简历");

        if (imageData == null || imageData.isBlank()) {
            return ToolResult.error("PARAM_MISSING", "图片数据不能为空");
        }

        try {
            // 使用简历优化选项识别
            OcrResult result = ocrHelper.recognizeFromBase64(imageData, OcrOptions.forResume());

            if (!result.isSuccess()) {
                return ToolResult.error("OCR_FAILED",
                        "简历识别失败: " + result.getErrorMessage());
            }

            // 提取结构化信息
            OcrHelper.ResumeInfo resumeInfo = ocrHelper.extractResumeInfo(result.getText());

            StringBuilder sb = new StringBuilder();
            sb.append("✅ 简历识别成功\n\n");
            sb.append("识别方式: ").append(result.isFallback() ? "本地模型" : "智谱 OCR").append("\n");

            if (resumeInfo.getWorkYears() != null) {
                sb.append("工作年限: ").append(resumeInfo.getWorkYears()).append(" 年\n");
            }
            if (resumeInfo.getCurrentPosition() != null) {
                sb.append("当前职位: ").append(resumeInfo.getCurrentPosition()).append("\n");
            }
            sb.append("\n识别内容:\n").append(resumeInfo.getSummary());

            if (result.getText().length() > 200) {
                sb.append("\n...(已截断，完整内容共 ")
                        .append(result.getText().length())
                        .append(" 字符)");
            }

            return ToolResult.success("简历识别完成", sb.toString());

        } catch (Exception e) {
            log.error("简历识别异常", e);
            return ToolResult.error("OCR_ERROR", "识别失败: " + e.getMessage());
        }
    }

    /**
     * 识别 JD 图片并提取技能
     */
    @Tool("""
        [识别] 识别 JD 图片并提取技能要求

        适用场景：
        - 用户上传 JD 截图
        - 用户说"分析一下这个职位要求"

        参数：
        - imageData: Base64编码的 JD 图片
        - applicationId: 关联的申请ID（可选）

        说明：
        - 识别 JD 内容
        - 提取技能标签并匹配 skill_tags
        - 返回：提取的技能清单
        """)
    public ToolResult recognizeJD(String imageData, Long applicationId) {
        log.info("AI调用：OCR 识别 JD，applicationId={}", applicationId);

        if (imageData == null || imageData.isBlank()) {
            return ToolResult.error("PARAM_MISSING", "图片数据不能为空");
        }

        try {
            // 使用 JD 优化选项识别
            OcrResult result = ocrHelper.recognizeFromBase64(imageData, OcrOptions.forJD());

            if (!result.isSuccess()) {
                return ToolResult.error("OCR_FAILED",
                        "JD 识别失败: " + result.getErrorMessage());
            }

            // 提取技能标签
            java.util.List<String> skills = ocrHelper.extractSkillsFromJD(result.getText());

            StringBuilder sb = new StringBuilder();
            sb.append("✅ JD 识别成功\n\n");
            sb.append("识别方式: ").append(result.isFallback() ? "本地模型" : "智谱 OCR").append("\n");

            if (!skills.isEmpty()) {
                sb.append("\n提取的技能标签:\n");
                for (int i = 0; i < skills.size(); i++) {
                    sb.append(String.format("%d. %s\n", i + 1, skills.get(i)));
                }
            }

            sb.append("\n识别内容:\n").append(result.getText());

            if (result.getText().length() > 500) {
                sb.append("\n...(已截断，完整内容共 ")
                        .append(result.getText().length())
                        .append(" 字符)");
            }

            return ToolResult.success("JD 识别完成", sb.toString());

        } catch (Exception e) {
            log.error("JD 识别异常", e);
            return ToolResult.error("OCR_ERROR", "识别失败: " + e.getMessage());
        }
    }
}
