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
 * 用户简历实体（极简版）
 * <p>
 * 模拟面试只关注技术能力，不需要隐私信息
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_resumes")
public class UserResume {

    /**
     * 简历ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long resumeId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 简历名称（如"Java后端-3年经验"）
     */
    private String resumeName;

    /**
     * 是否为默认简历
     */
    private Boolean isDefault;

    // 工作信息（影响面试问题难度）

    /**
     * 工作年限（年）- 影响：3年问JVM，1年问基础
     */
    private Integer workYears;

    /**
     * 当前职位 - 如：Java后端工程师
     */
    private String currentPosition;

    /**
     * 目标岗位级别：JUNIOR/MIDDLE/SENIOR
     */
    private String targetLevel;

    // 自我介绍（AI 用于了解技术背景）

    /**
     * 自我介绍（突出技术栈、项目经验）
     */
    private String summary;

    // 原始文件（用于 AI 解析）

    /**
     * 原始简历文件URL
     */
    private String originalFileUrl;

    /**
     * 文件类型：PDF/DOCX/IMG
     */
    private String originalFileType;

    // 解析状态

    /**
     * 解析状态：PENDING/PARSING/SUCCESS/FAILED
     */
    private String parseStatus;

    /**
     * 解析完成时间
     */
    private LocalDateTime parsedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 目标岗位级别枚举
     * <p>
     * 定义求职者目标申请的岗位级别，影响面试问题的难度和深度
     * </p>
     */
    public enum TargetLevel {
        /**
         * 初级（1-2年经验）
         * <p>
         * <b>面试特点</b>:
         * </p>
         * <ul>
         *   <li>侧重基础知识和概念理解</li>
         *   <li>问题难度：1-2分（5分制）</li>
         *   <li>考察内容：基本语法、常用API、简单原理</li>
         * </ul>
         * <p>
         * <b>示例问题</b>:
         * </p>
         * <ul>
         *   <li>"HashMap 的基本用法是什么？"</li>
         *   <li>"ArrayList 和 LinkedList 的区别是什么？"</li>
         *   <li>"Spring 的 @Autowired 注解有什么作用？"</li>
         * </ul>
         */
        JUNIOR,

        /**
         * 中级（3-5年经验）
         * <p>
         * <b>面试特点</b>:
         * </p>
         * <ul>
         *   <li>侧重原理理解和实际应用</li>
         *   <li>问题难度：2-3分（5分制）</li>
         *   <li>考察内容：底层原理、性能优化、源码分析</li>
         * </ul>
         * <p>
         * <b>示例问题</b>:
         * </p>
         * <ul>
         *   <li>"HashMap 的扩容机制是怎样的？"</li>
         *   <li>"如何解决 Redis 缓存穿透问题？"</li>
         *   <li>"Spring Boot 的自动装配原理是什么？"</li>
         * </ul>
         */
        MIDDLE,

        /**
         * 高级（5-8年经验）
         * <p>
         * <b>面试特点</b>:
         * </p>
         * <ul>
         *   <li>侧重架构设计和系统优化</li>
         *   <li>问题难度：3-4分（5分制）</li>
         *   <li>考察内容：系统设计、性能调优、问题排查</li>
         * </ul>
         * <p>
         * <b>示例问题</b>:
         * </p>
         * <ul>
         *   <li>"如何设计一个高并发的秒杀系统？"</li>
         *   <li>"MySQL 慢查询如何排查和优化？"</li>
         *   <li>"分布式事务有哪些解决方案？"</li>
         * </ul>
         */
        SENIOR,

        /**
         * 专家/架构师（8年以上经验）
         * <p>
         * <b>面试特点</b>:
         * </p>
         * <ul>
         *   <li>侧重技术选型和团队管理</li>
         *   <li>问题难度：4-5分（5分制）</li>
         *   <li>考察内容：技术决策、团队建设、业务规划</li>
         * </ul>
         * <p>
         * <b>示例问题</b>:
         * </p>
         * <ul>
         *   <li>"如何从0到1搭建微服务架构？"</li>
         *   <li>"技术选型时如何权衡不同方案？"</li>
         *   <li>"如何培养团队的技术能力？"</li>
         * </ul>
         */
        LEAD
    }

    /**
     * 解析状态枚举
     * <p>
     * 定义简历文件从上传到解析完成的整个生命周期
     * </p>
     */
    public enum ParseStatus {
        /**
         * 待解析
         * <p>
         * <b>状态说明</b>: 简历文件已上传，但尚未开始解析
         * </p>
         * <p>
         * <b>触发条件</b>: 用户上传简历文件后
         * </p>
         * <p>
         * <b>下一步</b>: 等待解析任务调度
         * </p>
         */
        PENDING,

        /**
         * 解析中
         * <p>
         * <b>状态说明</b>: 正在使用 OCR/AI 技术解析简历内容
         * </p>
         * <p>
         * <b>触发条件</b>: 解析任务开始执行
         * </p>
         * <p>
         * <b>耗时</b>: 通常 5-30 秒，取决于文件大小和复杂度
         * </p>
         * <p>
         * <b>下一步</b>: 解析完成后进入 SUCCESS 或 FAILED
         * </p>
         */
        PARSING,

        /**
         * 解析成功
         * <p>
         * <b>状态说明</b>: 简历解析成功，已提取出关键信息
         * </p>
         * <p>
         * <b>提取的信息</b>:
         * </p>
         * <ul>
         *   <li>工作年限</li>
         *   <li>当前职位</li>
         *   <li>技能列表（含熟练度）</li>
         *   <li>项目经验</li>
         *   <li>教育背景</li>
         * </ul>
         * <p>
         * <b>下一步</b>: 用户可以编辑解析结果，然后开始模拟面试
         * </p>
         */
        SUCCESS,

        /**
         * 解析失败
         * <p>
         * <b>状态说明</b>: 简历解析失败，无法提取有效信息
         * </p>
         * <p>
         * <b>常见原因</b>:
         * </p>
         * <ul>
         *   <li>文件格式不支持</li>
         *   <li>文件损坏</li>
         *   <li>图片模糊（OCR 失败）</li>
         *   <li>内容语言不支持</li>
         * </ul>
         * <p>
         * <b>下一步</b>: 用户需要重新上传或手动输入简历信息
         * </p>
         */
        FAILED
    }
}
