package com.jobtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
