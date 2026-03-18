package com.jobtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jobtracker.entity.*;
import com.jobtracker.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 模拟面试服务
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockInterviewService {

    private final MockInterviewSessionMapper sessionMapper;
    private final InterviewMessageMapper messageMapper;
    private final MockInterviewEvaluationMapper evaluationMapper;
    private final UserResumeMapper resumeMapper;
    private final ResumeSkillMapper resumeSkillMapper;
    private final ResumeProjectMapper resumeProjectMapper;
    private final ResumeWorkExperienceMapper resumeWorkExperienceMapper;
    private final SkillTagMapper skillTagMapper;
    private final ApplicationMapper applicationMapper;
    private final CompanyMapper companyMapper;

    // 新增：智能解析服务
    private final ResumeParsingService resumeParsingService;
    private final JDParsingService jdParsingService;

    // ==================== 面试会话操作 ====================

    /**
     * 创建面试会话
     * <p>
     * 创建会话时生成简历快照和 JD 快照，确保后续 Agent 有完整的上下文信息
     * </p>
     *
     * @param applicationId 求职申请ID
     * @param resumeId 简历ID（如果为 null，使用申请关联的简历）
     * @param userId 用户ID
     * @return 创建的面试会话
     */
    @Transactional
    public MockInterviewSession createSession(Long applicationId, Long resumeId, Long userId) {
        // 1. 获取申请信息
        JobApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("申请不存在: " + applicationId);
        }

        // 2. 确定使用的简历ID
        resumeId = resumeId == null ? application.getResumeId() : resumeId;
        if (resumeId == null) {
            throw new IllegalArgumentException("未指定简历，且申请未关联简历: " + applicationId);
        }

        // 3. 生成简历快照（⚠️ 重要：包含技能、项目等完整信息）
        String resumeSnapshot = generateResumeSnapshot(resumeId);
        if (resumeSnapshot == null || isEmptySnapshot(resumeSnapshot)) {
            log.warn("简历快照为空，尝试使用 AI 解析，简历ID: {}", resumeId);
            resumeSnapshot = generateResumeSnapshotWithAI(resumeId);
        }

        // 4. 生成 JD 快照（⚠️ 重要：包含技能要求等完整信息）
        String jdSnapshot = generateJdSnapshot(applicationId);
        if (jdSnapshot == null || isEmptySnapshot(jdSnapshot)) {
            log.warn("JD 快照为空，尝试使用 AI 解析，申请ID: {}", applicationId);
            jdSnapshot = generateJdSnapshotWithAI(applicationId);
        }

        // 5. 生成会话ID
        String sessionId = UUID.randomUUID().toString();

        // 6. 创建会话（包含快照）
        MockInterviewSession session = MockInterviewSession.builder()
                .sessionId(sessionId)
                .applicationId(applicationId)
                .userId(userId)
                .resumeId(resumeId)
                .companyId(application.getCompanyId())
                .jobTitle(application.getJobTitle())
                .seniorityLevel(application.getSeniorityLevel())
                .resumeSnapshot(resumeSnapshot)  // ⚠️ 设置简历快照
                .jdSnapshot(jdSnapshot)           // ⚠️ 设置 JD 快照
                .state(MockInterviewSession.InterviewState.INIT.name())
                .currentRound(0)
                .totalRounds(25)
                .createdAt(LocalDateTime.now())
                .build();

        // 7. 插入数据库
        sessionMapper.insert(session);

        log.info("创建面试会话成功，ID: {}, 申请: {}, 简历: {}, 快照已生成",
                sessionId, applicationId, resumeId);

        return session;
    }

    /**
     * 获取会话
     */
    public MockInterviewSession getSession(String sessionId) {
        return sessionMapper.selectById(sessionId);
    }

    /**
     * 更新会话状态
     */
    @Transactional
    public void updateSessionState(String sessionId, MockInterviewSession.InterviewState newState) {
        MockInterviewSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setState(newState.name());
            sessionMapper.updateById(session);
        }
    }

    /**
     * 更新会话
     */
    @Transactional
    public void updateSession(MockInterviewSession session) {
        sessionMapper.updateById(session);
    }

    /**
     * 开始面试
     */
    @Transactional
    public void startInterview(String sessionId) {
        MockInterviewSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setState(MockInterviewSession.InterviewState.WELCOME.name());
            session.setStartedAt(LocalDateTime.now());
            sessionMapper.updateById(session);
            log.info("面试开始，会话ID: {}", sessionId);
        }
    }

    /**
     * 结束面试
     */
    @Transactional
    public void finishInterview(String sessionId) {
        MockInterviewSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setState(MockInterviewSession.InterviewState.FINISHED.name());
            session.setFinishedAt(LocalDateTime.now());
            if (session.getStartedAt() != null) {
                session.setDurationSeconds((int) java.time.Duration.between(
                        session.getStartedAt(),
                        session.getFinishedAt()
                ).getSeconds());
            }
            sessionMapper.updateById(session);
            log.info("面试结束，会话ID: {}, 耗时: {}秒", sessionId, session.getDurationSeconds());
        }
    }

    /**
     * 进入下一轮
     */
    @Transactional
    public void nextRound(String sessionId) {
        MockInterviewSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setCurrentRound(session.getCurrentRound() + 1);
            session.setState(MockInterviewSession.InterviewState.TECHNICAL_QA.name());
            sessionMapper.updateById(session);
            log.info("进入下一轮，会话ID: {}, 轮次: {}", sessionId, session.getCurrentRound());
        }
    }

    /**
     * 获取用户的所有面试会话
     */
    public List<MockInterviewSession> getUserSessions(Long userId) {
        return sessionMapper.selectList(
                new LambdaQueryWrapper<MockInterviewSession>()
                        .eq(MockInterviewSession::getUserId, userId)
                        .orderByDesc(MockInterviewSession::getCreatedAt)
        );
    }

    // ==================== 消息记录操作 ====================

    /**
     * 添加消息
     */
    @Transactional
    public InterviewMessage addMessage(InterviewMessage message) {
        messageMapper.insert(message);
        return message;
    }

    /**
     * 获取会话的所有消息
     */
    public List<InterviewMessage> getSessionMessages(String sessionId) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
                        .orderByAsc(InterviewMessage::getRoundNumber)
                        .orderByAsc(InterviewMessage::getSequenceInRound)
        );
    }

    /**
     * 获取当前轮次的消息
     */
    public List<InterviewMessage> getRoundMessages(String sessionId, Integer roundNumber) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
                        .eq(InterviewMessage::getRoundNumber, roundNumber)
                        .orderByAsc(InterviewMessage::getSequenceInRound)
        );
    }

    // ==================== 评分操作 ====================

    /**
     * 保存评分
     */
    @Transactional
    public MockInterviewEvaluation saveEvaluation(MockInterviewEvaluation evaluation) {
        evaluationMapper.insert(evaluation);

        // 更新会话总分（简单平均）
        MockInterviewSession session = sessionMapper.selectById(evaluation.getSessionId());
        if (session != null) {
            List<MockInterviewEvaluation> evaluations = getSessionEvaluations(evaluation.getSessionId());
            double avgScore = evaluations.stream()
                    .mapToDouble(e -> e.getTotalScore() != null ? e.getTotalScore().doubleValue() : 0)
                    .average()
                    .orElse(0);
            session.setTotalScore(java.math.BigDecimal.valueOf(avgScore));
            sessionMapper.updateById(session);
        }

        return evaluation;
    }

    /**
     * 获取会话的所有评分
     */
    public List<MockInterviewEvaluation> getSessionEvaluations(String sessionId) {
        return evaluationMapper.selectList(
                new LambdaQueryWrapper<MockInterviewEvaluation>()
                        .eq(MockInterviewEvaluation::getSessionId, sessionId)
                        .orderByAsc(MockInterviewEvaluation::getRoundNumber)
        );
    }

    // ==================== 简历快照操作 ====================

    /**
     * 生成简历快照
     * <p>
     * 从简历和相关表中提取完整信息，生成 JSON 格式的快照
     * </p>
     * <p>
     * 包含信息：
     * <ul>
     *   <li>基本信息：工作年限、当前职位、目标级别</li>
     *   <li>自我介绍</li>
     *   <li>技能列表：技能名称、熟练度、使用年限、是否核心</li>
     *   <li>工作经历：公司、职位、时间、描述</li>
     *   <li>项目经验：项目名称、角色、描述、技术栈</li>
     * </ul>
     * </p>
     *
     * @param resumeId 简历ID
     * @return JSON 格式的简历快照，如果简历不存在返回 null
     */
    public String generateResumeSnapshot(Long resumeId) {
        if (resumeId == null) {
            return null;
        }

        UserResume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            log.warn("简历不存在，无法生成快照: {}", resumeId);
            return null;
        }

        try {
            // 1. 查询简历技能
            List<ResumeSkill> skills = resumeSkillMapper.selectList(
                    new LambdaQueryWrapper<ResumeSkill>()
                            .eq(ResumeSkill::getResumeId, resumeId)
            );

            // 2. 查询项目经验
            List<ResumeProject> projects = resumeProjectMapper.selectList(
                    new LambdaQueryWrapper<ResumeProject>()
                            .eq(ResumeProject::getResumeId, resumeId)
                            .orderByAsc(ResumeProject::getDisplayOrder)
            );

            // 3. 查询工作经历
            List<ResumeWorkExperience> workExperiences = resumeWorkExperienceMapper.selectList(
                    new LambdaQueryWrapper<ResumeWorkExperience>()
                            .eq(ResumeWorkExperience::getResumeId, resumeId)
                            .orderByDesc(ResumeWorkExperience::getStartDate)
            );

            // 4. 批量查询技能名称（优化性能）
            List<Long> skillIds = skills.stream()
                    .map(ResumeSkill::getSkillId)
                    .distinct()
                    .toList();
            List<SkillTag> skillTags = skillIds.isEmpty() ? List.of() :
                    skillTagMapper.selectBatchIds(skillIds);

            // 构建 skillId -> skillName 映射
            Map<Long, String> skillNameMap = skillTags.stream()
                    .collect(Collectors.toMap(
                            SkillTag::getSkillId,
                            SkillTag::getSkillName,
                            (existing, replacement) -> existing
                    ));

            // 5. 使用 ObjectMapper 构建 JSON（更可靠）
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.node.ObjectNode rootNode = mapper.createObjectNode();

            // 基本信息
            rootNode.put("work_years", resume.getWorkYears() != null ? resume.getWorkYears() : 0);
            rootNode.put("current_position", resume.getCurrentPosition() != null ? resume.getCurrentPosition() : "");
            rootNode.put("target_level", resume.getTargetLevel() != null ? resume.getTargetLevel() : "");
            if (resume.getSummary() != null) {
                rootNode.put("summary", resume.getSummary());
            }

            // 技能列表
            com.fasterxml.jackson.databind.node.ArrayNode skillsArray = mapper.createArrayNode();
            for (ResumeSkill skill : skills) {
                com.fasterxml.jackson.databind.node.ObjectNode skillNode = mapper.createObjectNode();
                skillNode.put("skill_id", skill.getSkillId());

                // 添加技能名称（从 skill_tags 表查询）
                String skillName = skillNameMap.get(skill.getSkillId());
                skillNode.put("skill_name", skillName != null ? skillName : "未知技能");

                skillNode.put("proficiency", skill.getProficiencyLevel() != null ? skill.getProficiencyLevel() : "UNKNOWN");

                if (skill.getExperienceYears() != null) {
                    skillNode.put("experience_years", skill.getExperienceYears());
                }

                if (skill.getLastUsedDate() != null) {
                    skillNode.put("last_used_date", skill.getLastUsedDate().toString());
                }

                skillNode.put("is_core", skill.getIsCoreSkill() != null && skill.getIsCoreSkill());

                skillsArray.add(skillNode);
            }
            rootNode.set("skills", skillsArray);

            // 工作经历
            com.fasterxml.jackson.databind.node.ArrayNode workArray = mapper.createArrayNode();
            for (ResumeWorkExperience work : workExperiences) {
                com.fasterxml.jackson.databind.node.ObjectNode workNode = mapper.createObjectNode();
                workNode.put("company_name", work.getCompanyName() != null ? work.getCompanyName() : "");
                workNode.put("position", work.getPosition() != null ? work.getPosition() : "");

                if (work.getStartDate() != null) {
                    workNode.put("start_date", work.getStartDate().toString());
                }

                if (work.getEndDate() != null) {
                    workNode.put("end_date", work.getEndDate().toString());
                }

                workNode.put("is_current", work.getIsCurrent() != null && work.getIsCurrent());

                if (work.getDescription() != null) {
                    workNode.put("description", work.getDescription());
                }

                if (work.getAchievements() != null) {
                    workNode.put("achievements", work.getAchievements());
                }

                workArray.add(workNode);
            }
            rootNode.set("work_experiences", workArray);

            // 项目经验
            com.fasterxml.jackson.databind.node.ArrayNode projectsArray = mapper.createArrayNode();
            for (ResumeProject project : projects) {
                com.fasterxml.jackson.databind.node.ObjectNode projectNode = mapper.createObjectNode();
                projectNode.put("project_name", project.getProjectName() != null ? project.getProjectName() : "");
                projectNode.put("role", project.getRole() != null ? project.getRole() : "");

                if (project.getDescription() != null) {
                    projectNode.put("description", project.getDescription());
                }

                if (project.getResponsibilities() != null) {
                    projectNode.put("responsibilities", project.getResponsibilities());
                }

                if (project.getAchievements() != null) {
                    projectNode.put("achievements", project.getAchievements());
                }

                projectNode.put("tech_stack", project.getTechStack() != null ? project.getTechStack() : "");

                if (project.getProjectScale() != null) {
                    projectNode.put("project_scale", project.getProjectScale());
                }

                if (project.getPerformanceMetrics() != null) {
                    projectNode.put("performance_metrics", project.getPerformanceMetrics());
                }

                if (project.getIsOngoing() != null) {
                    projectNode.put("is_ongoing", project.getIsOngoing());
                }

                projectsArray.add(projectNode);
            }
            rootNode.set("projects", projectsArray);

            String result = mapper.writeValueAsString(rootNode);
            log.info("生成简历快照成功，简历ID: {}, 技能数: {}, 工作经历数: {}, 项目数: {}",
                    resumeId, skills.size(), workExperiences.size(), projects.size());

            return result;
        } catch (Exception e) {
            log.error("生成简历快照失败，简历ID: {}", resumeId, e);
            return null;
        }
    }

    /**
     * 生成 JD 快照
     * <p>
     * 从求职申请中提取完整信息，生成 JSON 格式的 JD 快照
     * </p>
     * <p>
     * 包含信息：
     * <ul>
     *   <li>岗位信息：职位名称、级别、类型</li>
     *   <li>岗位描述</li>
     *   <li>薪资范围：最低薪资、最高薪资、货币</li>
     *   <li>工作地点</li>
     *   <li>公司信息：公司名称、行业、规模</li>
     *   <li>技能要求</li>
     *   <li>职位链接</li>
     * </ul>
     * </p>
     *
     * @param applicationId 求职申请ID
     * @return JSON 格式的 JD 快照
     */
    public String generateJdSnapshot(Long applicationId) {
        if (applicationId == null) {
            return null;
        }

        JobApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            log.warn("求职申请不存在，无法生成 JD 快照: {}", applicationId);
            return null;
        }

        try {
            // 使用 ObjectMapper 构建 JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.node.ObjectNode rootNode = mapper.createObjectNode();

            // 岗位信息
            rootNode.put("job_title", application.getJobTitle() != null ? application.getJobTitle() : "");
            rootNode.put("seniority_level", application.getSeniorityLevel() != null ? application.getSeniorityLevel() : "");
            rootNode.put("job_type", application.getJobType() != null ? application.getJobType() : "");

            // 岗位描述
            if (application.getJobDescription() != null) {
                rootNode.put("job_description", application.getJobDescription());
            }

            // 薪资范围
            if (application.getSalaryMin() != null || application.getSalaryMax() != null) {
                com.fasterxml.jackson.databind.node.ObjectNode salaryNode = mapper.createObjectNode();
                if (application.getSalaryMin() != null) {
                    salaryNode.put("min", application.getSalaryMin());
                }
                if (application.getSalaryMax() != null) {
                    salaryNode.put("max", application.getSalaryMax());
                }
                salaryNode.put("currency", application.getSalaryCurrency() != null ? application.getSalaryCurrency() : "CNY");
                rootNode.set("salary_range", salaryNode);
            }

            // 工作地点
            if (application.getWorkLocation() != null) {
                rootNode.put("work_location", application.getWorkLocation());
            }

            // 公司信息
            if (application.getCompanyId() != null) {
                Company company = companyMapper.selectById(application.getCompanyId());
                if (company != null) {
                    com.fasterxml.jackson.databind.node.ObjectNode companyNode = mapper.createObjectNode();
                    companyNode.put("company_id", company.getId());
                    companyNode.put("name", company.getName() != null ? company.getName() : "");

                    if (company.getIndustry() != null) {
                        companyNode.put("industry", company.getIndustry());
                    }

                    if (company.getSize() != null) {
                        companyNode.put("size", company.getSize());
                    }

                    if (company.getLocation() != null) {
                        companyNode.put("location", company.getLocation());
                    }

                    if (company.getDescription() != null) {
                        companyNode.put("description", company.getDescription());
                    }

                    rootNode.set("company", companyNode);
                }
            }

            // 技能要求
            if (application.getSkillsRequired() != null) {
                String skillsRequired = application.getSkillsRequired().trim();

                // 判断是否已经是 JSON 格式
                if (skillsRequired.startsWith("[") || skillsRequired.startsWith("{")) {
                    // 直接解析并验证
                    try {
                        com.fasterxml.jackson.databind.JsonNode skillsNode = mapper.readTree(skillsRequired);
                        rootNode.set("skills_required", skillsNode);
                    } catch (Exception e) {
                        // 解析失败，作为普通字符串处理
                        rootNode.set("skills_required", parseSkillsString(mapper, skillsRequired));
                    }
                } else {
                    // 逗号分隔的字符串，转换为 JSON 数组
                    rootNode.set("skills_required", parseSkillsString(mapper, skillsRequired));
                }
            } else {
                rootNode.set("skills_required", mapper.createArrayNode());
            }

            // 职位链接
            if (application.getJobUrl() != null) {
                rootNode.put("job_url", application.getJobUrl());
            }

            String result = mapper.writeValueAsString(rootNode);
            log.info("生成 JD 快照成功，申请ID: {}, 职位: {}", applicationId, application.getJobTitle());

            return result;
        } catch (Exception e) {
            log.error("生成 JD 快照失败，申请ID: {}", applicationId, e);
            return null;
        }
    }

    /**
     * 解析技能字符串（逗号分隔）为 JSON 数组
     */
    private com.fasterxml.jackson.databind.node.ArrayNode parseSkillsString(
            com.fasterxml.jackson.databind.ObjectMapper mapper, String skillsString) {
        com.fasterxml.jackson.databind.node.ArrayNode skillsArray = mapper.createArrayNode();

        if (skillsString == null || skillsString.trim().isEmpty()) {
            return skillsArray;
        }

        String[] skills = skillsString.split(",");
        for (String skill : skills) {
            String trimmedSkill = skill.trim();
            if (!trimmedSkill.isEmpty()) {
                skillsArray.add(trimmedSkill);
            }
        }

        return skillsArray;
    }

    // ==================== AI 智能解析方法 ====================

    /**
     * 检查快照是否为空
     */
    private boolean isEmptySnapshot(String snapshot) {
        if (snapshot == null || snapshot.trim().isEmpty() || "{}".equals(snapshot.trim())) {
            return true;
        }

        try {
            com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(snapshot);

            // 检查是否有实际内容
            if (node.has("skills") && node.get("skills").isArray()) {
                if (node.get("skills").size() > 0) {
                    return false;  // 有技能，不为空
                }
            }

            if (node.has("skills_required") && node.get("skills_required").isArray()) {
                if (node.get("skills_required").size() > 0) {
                    return false;  // 有技能要求，不为空
                }
            }

            // 如果关键字段都是空的，认为是空快照
            return true;

        } catch (Exception e) {
            log.warn("检查快照是否为空时出错，认为不为空: {}", snapshot, e);
            return false;
        }
    }

    /**
     * 使用 AI 解析简历并生成快照
     */
    private String generateResumeSnapshotWithAI(Long resumeId) {
        UserResume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            log.warn("简历不存在，无法使用 AI 解析: {}", resumeId);
            return "{}";
        }

        try {
            // 构建简历文本（用于 AI 解析）
            String resumeText = buildResumeTextForParsing(resume);

            // 调用 AI 解析服务
            ResumeParsingService.ResumeParseResult parseResult = resumeParsingService.parseResume(resumeText);

            // 转换为 JSON 快照
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(parseResult);

        } catch (Exception e) {
            log.error("AI 解析简历失败，简历ID: {}", resumeId, e);
            return "{}";
        }
    }

    /**
     * 使用 AI 解析 JD 并生成快照
     */
    private String generateJdSnapshotWithAI(Long applicationId) {
        JobApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            log.warn("申请不存在，无法使用 AI 解析 JD: {}", applicationId);
            return "{}";
        }

        try {
            // 构建 JD 文本（用于 AI 解析）
            String jdText = buildJdTextForParsing(application);

            // 调用 AI 解析服务
            JDParsingService.JDParseResult parseResult = jdParsingService.parseJD(jdText);

            // 合并公司信息
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.node.ObjectNode rootNode = mapper.valueToTree(parseResult);

            // 添加岗位基本信息
            rootNode.put("job_title", application.getJobTitle() != null ? application.getJobTitle() : "");
            rootNode.put("seniority_level", application.getSeniorityLevel() != null ? application.getSeniorityLevel() : "");

            // 添加公司信息
            if (application.getCompanyId() != null) {
                Company company = companyMapper.selectById(application.getCompanyId());
                if (company != null) {
                    com.fasterxml.jackson.databind.node.ObjectNode companyNode = mapper.createObjectNode();
                    companyNode.put("company_id", company.getId());
                    companyNode.put("name", company.getName() != null ? company.getName() : "");
                    companyNode.put("industry", company.getIndustry() != null ? company.getIndustry() : "");
                    companyNode.put("size", company.getSize() != null ? company.getSize() : "");
                    rootNode.set("company", companyNode);
                }
            }

            return mapper.writeValueAsString(rootNode);

        } catch (Exception e) {
            log.error("AI 解析 JD 失败，申请ID: {}", applicationId, e);
            return "{}";
        }
    }

    /**
     * 构建简历文本（用于 AI 解析）
     */
    private String buildResumeTextForParsing(UserResume resume) {
        StringBuilder text = new StringBuilder();

        // 基本信息
        text.append("工作年限：").append(resume.getWorkYears() != null ? resume.getWorkYears() : 0).append(" 年\n");
        text.append("当前职位：").append(resume.getCurrentPosition() != null ? resume.getCurrentPosition() : "无").append("\n");
        text.append("目标级别：").append(resume.getTargetLevel() != null ? resume.getTargetLevel() : "").append("\n");

        if (resume.getSummary() != null && !resume.getSummary().isEmpty()) {
            text.append("\n自我介绍：\n").append(resume.getSummary()).append("\n");
        }

        // 查询技能
        List<ResumeSkill> skills = resumeSkillMapper.selectList(
                new LambdaQueryWrapper<ResumeSkill>().eq(ResumeSkill::getResumeId, resume.getId())
        );

        if (!skills.isEmpty()) {
            text.append("\n技能列表：\n");
            for (ResumeSkill skill : skills) {
                text.append("- ").append(skill.getSkillName())
                        .append("（").append(skill.getProficiencyLevel()).append("）\n");
            }
        }

        // 查询项目
        List<ResumeProject> projects = resumeProjectMapper.selectList(
                new LambdaQueryWrapper<ResumeProject>()
                        .eq(ResumeProject::getResumeId, resume.getId())
                        .orderByAsc(ResumeProject::getDisplayOrder)
        );

        if (!projects.isEmpty()) {
            text.append("\n项目经验：\n");
            for (ResumeProject project : projects) {
                text.append("项目名称：").append(project.getProjectName()).append("\n");
                text.append("角色：").append(project.getRole()).append("\n");
                if (project.getDescription() != null) {
                    text.append("描述：").append(project.getDescription()).append("\n");
                }
                if (project.getTechStack() != null) {
                    text.append("技术栈：").append(project.getTechStack()).append("\n");
                }
                text.append("\n");
            }
        }

        // 查询工作经历
        List<ResumeWorkExperience> workExps = resumeWorkExperienceMapper.selectList(
                new LambdaQueryWrapper<ResumeWorkExperience>()
                        .eq(ResumeWorkExperience::getResumeId, resume.getId())
                        .orderByDesc(ResumeWorkExperience::getStartDate)
        );

        if (!workExps.isEmpty()) {
            text.append("\n工作经历：\n");
            for (ResumeWorkExperience work : workExps) {
                text.append("公司：").append(work.getCompanyName()).append("\n");
                text.append("职位：").append(work.getPosition()).append("\n");
                if (work.getStartDate() != null) {
                    text.append("开始时间：").append(work.getStartDate()).append("\n");
                }
                if (work.getDescription() != null) {
                    text.append("描述：").append(work.getDescription()).append("\n");
                }
                text.append("\n");
            }
        }

        return text.toString();
    }

    /**
     * 构建 JD 文本（用于 AI 解析）
     */
    private String buildJdTextForParsing(JobApplication application) {
        StringBuilder text = new StringBuilder();

        text.append("职位名称：").append(application.getJobTitle()).append("\n");
        text.append("级别：").append(application.getSenioriorityLevel()).append("\n");

        if (application.getJobDescription() != null && !application.getJobDescription().isEmpty()) {
            text.append("\n职位描述：\n").append(application.getJobDescription()).append("\n");
        }

        if (application.getSkillsRequired() != null && !application.getSkillsRequired().isEmpty()) {
            text.append("\n技能要求：\n").append(application.getSkillsRequired()).append("\n");
        }

        if (application.getWorkLocation() != null) {
            text.append("\n工作地点：").append(application.getWorkLocation()).append("\n");
        }

        return text.toString();
    }
}
