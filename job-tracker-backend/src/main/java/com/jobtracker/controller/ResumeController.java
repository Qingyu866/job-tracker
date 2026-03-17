package com.jobtracker.controller;

import com.jobtracker.context.UserContext;
import com.jobtracker.common.result.Result;
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
    public Result<UserResume> createResume(@RequestBody UserResume resume) {
        // 从 Token 获取当前用户 ID
        Long userId = UserContext.getCurrentUserId();
        resume.setUserId(userId);

        UserResume created = resumeService.create(resume);

        log.info("创建简历成功: resumeId={}, userId={}", created.getResumeId(), userId);

        return Result.success("简历创建成功", created);
    }

    /**
     * 更新简历
     * PUT /api/resumes/{resumeId}
     */
    @PutMapping("/{resumeId}")
    public Result<String> updateResume(
            @PathVariable Long resumeId,
            @RequestBody UserResume resume
    ) {
        resume.setResumeId(resumeId);
        resumeService.update(resume);
        return Result.success("简历更新成功");
    }

    /**
     * 删除简历
     * DELETE /api/resumes/{resumeId}
     */
    @DeleteMapping("/{resumeId}")
    public Result<String> deleteResume(@PathVariable Long resumeId) {
        resumeService.delete(resumeId);
        return Result.success("简历删除成功");
    }

    /**
     * 获取简历详情
     * GET /api/resumes/{resumeId}
     */
    @GetMapping("/{resumeId}")
    public Result<UserResume> getResume(@PathVariable Long resumeId) {
        UserResume resume = resumeService.getById(resumeId);
        if (resume == null) {
            return Result.error("简历不存在");
        }
        return Result.success(resume);
    }

    /**
     * 获取当前用户的所有简历
     * GET /api/resumes/my
     */
    @GetMapping("/my")
    public Result<List<UserResume>> getMyResumes() {
        Long userId = UserContext.getCurrentUserId();
        List<UserResume> resumes = resumeService.getByUserId(userId);
        return Result.success(resumes);
    }

    /**
     * 获取当前用户的默认简历
     * GET /api/resumes/my/default
     */
    @GetMapping("/my/default")
    public Result<UserResume> getMyDefaultResume() {
        Long userId = UserContext.getCurrentUserId();
        UserResume resume = resumeService.getDefaultResume(userId);
        if (resume == null) {
            return Result.error("未找到默认简历");
        }
        return Result.success(resume);
    }

    /**
     * 设置默认简历
     * PUT /api/resumes/{resumeId}/default
     */
    @PutMapping("/{resumeId}/default")
    public Result<String> setDefaultResume(@PathVariable Long resumeId) {
        Long userId = UserContext.getCurrentUserId();
        resumeService.setDefaultResume(userId, resumeId);
        return Result.success("默认简历设置成功");
    }

    /**
     * 获取用户的所有简历（已废弃，请使用 /my）
     * GET /api/resumes/user/{userId}
     * @deprecated 请使用 /my
     */
    @Deprecated(since = "2026-03-17", forRemoval = true)
    @GetMapping("/user/{userId}")
    public Result<List<UserResume>> getUserResumes(@PathVariable Long userId) {
        List<UserResume> resumes = resumeService.getByUserId(userId);
        return Result.success(resumes);
    }

    /**
     * 获取用户的默认简历（已废弃，请使用 /my/default）
     * GET /api/resumes/user/{userId}/default
     * @deprecated 请使用 /my/default
     */
    @Deprecated(since = "2026-03-17", forRemoval = true)
    @GetMapping("/user/{userId}/default")
    public Result<UserResume> getDefaultResume(@PathVariable Long userId) {
        UserResume resume = resumeService.getDefaultResume(userId);
        if (resume == null) {
            return Result.error("未找到默认简历");
        }
        return Result.success(resume);
    }

    /**
     * 获取简历的项目经历
     * GET /api/resumes/{resumeId}/projects
     */
    @GetMapping("/{resumeId}/projects")
    public Result<List<ResumeProject>> getProjects(@PathVariable Long resumeId) {
        List<ResumeProject> projects = resumeService.getProjects(resumeId);
        return Result.success(projects);
    }

    /**
     * 获取简历的技能
     * GET /api/resumes/{resumeId}/skills
     */
    @GetMapping("/{resumeId}/skills")
    public Result<List<ResumeSkill>> getSkills(@PathVariable Long resumeId) {
        List<ResumeSkill> skills = resumeService.getSkills(resumeId);
        return Result.success(skills);
    }
}
