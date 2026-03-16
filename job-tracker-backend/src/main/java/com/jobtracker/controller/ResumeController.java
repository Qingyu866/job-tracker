package com.jobtracker.controller;

import com.jobtracker.common.ApiResponse;
import com.jobtracker.entity.*;
import com.jobtracker.service.UserResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 简历管理 API 控制器
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@RestController
@RequestMapping("/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final UserResumeService resumeService;

    /**
     * 创建简历
     * POST /api/resumes
     */
    @PostMapping
    public ApiResponse<UserResume> createResume(@RequestBody UserResume resume) {
        UserResume created = resumeService.create(resume);
        return ApiResponse.success("简历创建成功", created);
    }

    /**
     * 更新简历
     * PUT /api/resumes/{resumeId}
     */
    @PutMapping("/{resumeId}")
    public ApiResponse<String> updateResume(
            @PathVariable Long resumeId,
            @RequestBody UserResume resume
    ) {
        resume.setResumeId(resumeId);
        resumeService.update(resume);
        return ApiResponse.success("简历更新成功");
    }

    /**
     * 删除简历
     * DELETE /api/resumes/{resumeId}
     */
    @DeleteMapping("/{resumeId}")
    public ApiResponse<String> deleteResume(@PathVariable Long resumeId) {
        resumeService.delete(resumeId);
        return ApiResponse.success("简历删除成功");
    }

    /**
     * 获取简历详情
     * GET /api/resumes/{resumeId}
     */
    @GetMapping("/{resumeId}")
    public ApiResponse<UserResume> getResume(@PathVariable Long resumeId) {
        UserResume resume = resumeService.getById(resumeId);
        if (resume == null) {
            return ApiResponse.notFound("简历不存在");
        }
        return ApiResponse.success(resume);
    }

    /**
     * 获取用户的所有简历
     * GET /api/resumes/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<UserResume>> getUserResumes(@PathVariable Long userId) {
        List<UserResume> resumes = resumeService.getByUserId(userId);
        return ApiResponse.success(resumes);
    }

    /**
     * 获取用户的默认简历
     * GET /api/resumes/user/{userId}/default
     */
    @GetMapping("/user/{userId}/default")
    public ApiResponse<UserResume> getDefaultResume(@PathVariable Long userId) {
        UserResume resume = resumeService.getDefaultResume(userId);
        if (resume == null) {
            return ApiResponse.notFound("未找到默认简历");
        }
        return ApiResponse.success(resume);
    }

    /**
     * 设置默认简历
     * PUT /api/resumes/{resumeId}/default
     */
    @PutMapping("/{resumeId}/default")
    public ApiResponse<String> setDefaultResume(
            @PathVariable Long resumeId,
            @RequestParam Long userId
    ) {
        resumeService.setDefaultResume(userId, resumeId);
        return ApiResponse.success("默认简历设置成功");
    }

    /**
     * 获取简历的项目经历
     * GET /api/resumes/{resumeId}/projects
     */
    @GetMapping("/{resumeId}/projects")
    public ApiResponse<List<ResumeProject>> getProjects(@PathVariable Long resumeId) {
        List<ResumeProject> projects = resumeService.getProjects(resumeId);
        return ApiResponse.success(projects);
    }

    /**
     * 获取简历的技能
     * GET /api/resumes/{resumeId}/skills
     */
    @GetMapping("/{resumeId}/skills")
    public ApiResponse<List<ResumeSkill>> getSkills(@PathVariable Long resumeId) {
        List<ResumeSkill> skills = resumeService.getSkills(resumeId);
        return ApiResponse.success(skills);
    }
}
