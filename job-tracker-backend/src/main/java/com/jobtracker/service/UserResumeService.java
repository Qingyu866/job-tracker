package com.jobtracker.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jobtracker.dto.*;
import com.jobtracker.entity.ResumeProject;
import com.jobtracker.entity.ResumeSkill;
import com.jobtracker.entity.ResumeWorkExperience;
import com.jobtracker.entity.UserResume;
import com.jobtracker.mapper.ResumeProjectMapper;
import com.jobtracker.mapper.ResumeSkillMapper;
import com.jobtracker.mapper.ResumeWorkExperienceMapper;
import com.jobtracker.mapper.UserResumeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户简历服务
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserResumeService {

    private final UserResumeMapper resumeMapper;
    private final ResumeProjectMapper projectMapper;
    private final ResumeSkillMapper skillMapper;
    private final ResumeWorkExperienceMapper workExperienceMapper;

    // ==================== 简历基础操作 ====================

    /**
     * 创建简历
     */
    @Transactional
    public UserResume create(UserResume resume) {
        resumeMapper.insert(resume);
        log.info("创建简历成功，ID: {}, 用户: {}", resume.getResumeId(), resume.getUserId());
        return resume;
    }

    /**
     * 创建完整简历（包含关联数据）
     *
     * @param userId 当前用户ID
     * @param request 创建请求
     * @return 创建的完整简历
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResume createCompleteResume(Long userId, CreateResumeRequest request) {
        // 1. 创建主表记录
        UserResume resume = buildMainResume(userId, request);
        resumeMapper.insert(resume);

        Long resumeId = resume.getResumeId();

        // 2. 批量创建工作经历
        if (request.getWorkExperiences() != null && !request.getWorkExperiences().isEmpty()) {
            List<ResumeWorkExperience> experiences = request.getWorkExperiences().stream()
                    .map(req -> buildWorkExperience(resumeId, req))
                    .toList();

            // 使用批量插入，避免for循环
            workExperienceMapper.insertBatch(experiences);
            log.info("批量创建 {} 条工作经历", experiences.size());
        }

        // 3. 批量创建项目经历
        if (request.getProjects() != null && !request.getProjects().isEmpty()) {
            List<ResumeProject> projects = request.getProjects().stream()
                    .map(req -> buildProject(resumeId, req))
                    .toList();

            // 使用批量插入
            projectMapper.insertBatch(projects);
            log.info("批量创建 {} 个项目", projects.size());
        }

        // 4. 批量创建技能（直接使用前端传入的skillId）
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            List<ResumeSkill> skills = request.getSkills().stream()
                    .map(req -> buildSkill(resumeId, req))
                    .toList();

            // 使用批量插入
            skillMapper.insertBatch(skills);
            log.info("批量创建 {} 项技能", skills.size());
        }

        // 5. 处理默认简历逻辑
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            setDefaultResume(userId, resumeId);
        }

        log.info("完整简历创建成功：resumeId={}, userId={}", resumeId, userId);

        return resume;
    }

    /**
     * 更新完整简历（包含关联数据）
     * <p>
     * 删除原有关联数据，重新创建新的关联数据
     * </p>
     *
     * @param resumeId 简历ID
     * @param request  完整简历请求
     * @return 更新后的完整简历
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResume updateCompleteResume(Long resumeId, CreateResumeRequest request) {
        // 1. 删除原有的关联数据
        workExperienceMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResumeWorkExperience>()
                        .eq(ResumeWorkExperience::getResumeId, resumeId)
        );
        projectMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResumeProject>()
                        .eq(ResumeProject::getResumeId, resumeId)
        );
        skillMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResumeSkill>()
                        .eq(ResumeSkill::getResumeId, resumeId)
        );
        log.info("删除原有关联数据：resumeId={}", resumeId);

        // 2. 更新主表记录
        UserResume resume = buildMainResumeForUpdate(resumeId, request);
        resumeMapper.updateById(resume);

        // 3. 批量创建工作经历
        if (request.getWorkExperiences() != null && !request.getWorkExperiences().isEmpty()) {
            List<ResumeWorkExperience> experiences = request.getWorkExperiences().stream()
                    .map(req -> buildWorkExperience(resumeId, req))
                    .toList();
            workExperienceMapper.insertBatch(experiences);
            log.info("批量创建 {} 条工作经历", experiences.size());
        }

        // 4. 批量创建项目经历
        if (request.getProjects() != null && !request.getProjects().isEmpty()) {
            List<ResumeProject> projects = request.getProjects().stream()
                    .map(req -> buildProject(resumeId, req))
                    .toList();
            projectMapper.insertBatch(projects);
            log.info("批量创建 {} 个项目", projects.size());
        }

        // 5. 批量创建技能
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            List<ResumeSkill> skills = request.getSkills().stream()
                    .map(req -> buildSkill(resumeId, req))
                    .toList();
            skillMapper.insertBatch(skills);
            log.info("批量创建 {} 项技能", skills.size());
        }

        // 6. 处理默认简历逻辑
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            setDefaultResume(resume.getUserId(), resumeId);
        }

        log.info("完整简历更新成功：resumeId={}", resumeId);

        return resume;
    }

    /**
     * 构建主表记录（用于更新）
     */
    private UserResume buildMainResumeForUpdate(Long resumeId, CreateResumeRequest request) {
        return UserResume.builder()
                .resumeId(resumeId)
                .resumeName(request.getResumeName())
                .isDefault(request.getIsDefault())
                .workYears(request.getWorkYears())
                .currentPosition(request.getCurrentPosition())
                .targetLevel(request.getTargetLevel())
                .summary(request.getSummary())
                .build();
    }

    /**
     * 构建主表记录
     */
    private UserResume buildMainResume(Long userId, CreateResumeRequest request) {
        return UserResume.builder()
                .userId(userId)
                .resumeName(request.getResumeName())
                .isDefault(request.getIsDefault())
                .workYears(request.getWorkYears())
                .currentPosition(request.getCurrentPosition())
                .targetLevel(request.getTargetLevel())
                .summary(request.getSummary())
                .build();
    }

    /**
     * 构建工作经历记录
     */
    private ResumeWorkExperience buildWorkExperience(Long resumeId, WorkExperienceRequest request) {
        return ResumeWorkExperience.builder()
                .resumeId(resumeId)
                .companyName(request.getCompanyName())
                .position(request.getPosition())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isCurrent(request.getIsCurrent())
                .description(request.getDescription())
                .achievements(request.getAchievements())
                .build();
    }

    /**
     * 构建项目经历记录
     */
    private ResumeProject buildProject(Long resumeId, ProjectRequest request) {
        return ResumeProject.builder()
                .resumeId(resumeId)
                .projectName(request.getProjectName())
                .role(request.getRole())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isOngoing(request.getIsOngoing())
                .description(request.getDescription())
                .responsibilities(request.getResponsibilities())
                .achievements(request.getAchievements())
                .techStack(JSONUtil.toJsonStr(request.getTechStack()))
                .projectScale(request.getProjectScale())
                .performanceMetrics(request.getPerformanceMetrics())
                .displayOrder(request.getDisplayOrder())
                .build();
    }

    /**
     * 构建技能记录
     * <p>
     * 直接使用前端传入的skillId，无需查询skill_tags表
     * </p>
     */
    private ResumeSkill buildSkill(Long resumeId, SkillRequest request) {
        return ResumeSkill.builder()
                .resumeId(resumeId)
                .skillId(request.getSkillId())  // 直接使用，无需查询
                .proficiencyLevel(request.getProficiencyLevel())
                .experienceYears(request.getExperienceYears())
                .lastUsedDate(request.getLastUsedDate())
                .isCoreSkill(request.getIsCoreSkill())
                .build();
    }

    /**
     * 更新简历
     */
    @Transactional
    public void update(UserResume resume) {
        resumeMapper.updateById(resume);
        log.info("更新简历成功，ID: {}", resume.getResumeId());
    }

    /**
     * 删除简历
     */
    @Transactional
    public void delete(Long resumeId) {
        resumeMapper.deleteById(resumeId);
        log.info("删除简历成功，ID: {}", resumeId);
    }

    /**
     * 根据ID获取简历
     */
    public UserResume getById(Long resumeId) {
        return resumeMapper.selectById(resumeId);
    }

    /**
     * 获取用户的所有简历
     */
    public List<UserResume> getByUserId(Long userId) {
        return resumeMapper.selectList(
                new LambdaQueryWrapper<UserResume>()
                        .eq(UserResume::getUserId, userId)
                        .orderByDesc(UserResume::getIsDefault)
                        .orderByDesc(UserResume::getCreatedAt)
        );
    }

    /**
     * 获取用户的默认简历
     */
    public UserResume getDefaultResume(Long userId) {
        return resumeMapper.selectOne(
                new LambdaQueryWrapper<UserResume>()
                        .eq(UserResume::getUserId, userId)
                        .eq(UserResume::getIsDefault, true)
        );
    }

    /**
     * 设置默认简历
     */
    @Transactional
    public void setDefaultResume(Long userId, Long resumeId) {
        // 取消其他简历的默认状态
        resumeMapper.selectList(
                new LambdaQueryWrapper<UserResume>()
                        .eq(UserResume::getUserId, userId)
        ).forEach(r -> {
            r.setIsDefault(false);
            resumeMapper.updateById(r);
        });

        // 设置新的默认简历
        UserResume resume = resumeMapper.selectById(resumeId);
        if (resume != null && resume.getUserId().equals(userId)) {
            resume.setIsDefault(true);
            resumeMapper.updateById(resume);
        }
    }

    // ==================== 项目经历操作 ====================

    /**
     * 添加项目经历
     */
    @Transactional
    public ResumeProject addProject(ResumeProject project) {
        projectMapper.insert(project);
        return project;
    }

    /**
     * 更新项目经历
     */
    @Transactional
    public void updateProject(ResumeProject project) {
        projectMapper.updateById(project);
    }

    /**
     * 删除项目经历
     */
    @Transactional
    public void deleteProject(Long projectId) {
        projectMapper.deleteById(projectId);
    }

    /**
     * 获取简历的所有项目
     */
    public List<ResumeProject> getProjects(Long resumeId) {
        return projectMapper.selectList(
                new LambdaQueryWrapper<ResumeProject>()
                        .eq(ResumeProject::getResumeId, resumeId)
                        .orderByAsc(ResumeProject::getDisplayOrder)
        );
    }

    // ==================== 技能操作 ====================

    /**
     * 添加技能
     */
    @Transactional
    public ResumeSkill addSkill(ResumeSkill skill) {
        skillMapper.insert(skill);
        return skill;
    }

    /**
     * 更新技能
     */
    @Transactional
    public void updateSkill(ResumeSkill skill) {
        skillMapper.updateById(skill);
    }

    /**
     * 删除技能
     */
    @Transactional
    public void deleteSkill(Long id) {
        skillMapper.deleteById(id);
    }

    /**
     * 获取简历的所有技能
     */
    public List<ResumeSkill> getSkills(Long resumeId) {
        return skillMapper.selectList(
                new LambdaQueryWrapper<ResumeSkill>()
                        .eq(ResumeSkill::getResumeId, resumeId)
                        .orderByDesc(ResumeSkill::getIsCoreSkill)
                        .orderByDesc(ResumeSkill::getExperienceYears)
        );
    }

    // ==================== 工作经历操作 ====================

    /**
     * 添加工作经历
     */
    @Transactional
    public ResumeWorkExperience addWorkExperience(ResumeWorkExperience experience) {
        workExperienceMapper.insert(experience);
        return experience;
    }

    /**
     * 更新工作经历
     */
    @Transactional
    public void updateWorkExperience(ResumeWorkExperience experience) {
        workExperienceMapper.updateById(experience);
    }

    /**
     * 删除工作经历
     */
    @Transactional
    public void deleteWorkExperience(Long experienceId) {
        workExperienceMapper.deleteById(experienceId);
    }

    /**
     * 获取简历的所有工作经历
     */
    public List<ResumeWorkExperience> getWorkExperiences(Long resumeId) {
        return workExperienceMapper.selectList(
                new LambdaQueryWrapper<ResumeWorkExperience>()
                        .eq(ResumeWorkExperience::getResumeId, resumeId)
                        .orderByDesc(ResumeWorkExperience::getStartDate)
        );
    }
}
