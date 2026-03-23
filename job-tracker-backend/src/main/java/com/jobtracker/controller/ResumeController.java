package com.jobtracker.controller;

import com.jobtracker.context.UserContext;
import com.jobtracker.common.result.Result;
import com.jobtracker.dto.CreateResumeRequest;
import com.jobtracker.dto.ResumeResponse;
import com.jobtracker.entity.UserResume;
import com.jobtracker.service.UserResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 简历管理 API 控制器
 * <p>
 * 所有简历操作都使用"完整简历"接口，包含关联数据（技能、项目、工作经历）
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-19
 */
@Slf4j
@RestController
@RequestMapping("/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final UserResumeService resumeService;

    /**
     * 创建完整简历（包含关联数据）
     * POST /api/resumes/complete
     *
     * @param request 完整简历请求（包含基本信息、技能、项目、工作经历）
     * @return 完整简历响应
     */
    @PostMapping("/complete")
    public Result<ResumeResponse> createCompleteResume(@RequestBody @Valid CreateResumeRequest request) {
        Long userId = UserContext.getCurrentUserId();

        UserResume resume = resumeService.createCompleteResume(userId, request);

        // 构建完整响应
        ResumeResponse response = buildResumeResponse(resume);

        log.info("创建完整简历成功: resumeId={}, userId={}", resume.getResumeId(), userId);

        return Result.success("简历创建成功", response);
    }

    /**
     * 更新完整简历（包含关联数据）
     * PUT /api/resumes/{resumeId}/complete
     *
     * @param resumeId 简历ID
     * @param request   完整简历请求（包含基本信息、技能、项目、工作经历）
     * @return 完整简历响应
     */
    @PutMapping("/{resumeId}/complete")
    public Result<ResumeResponse> updateCompleteResume(
            @PathVariable Long resumeId,
            @RequestBody @Valid CreateResumeRequest request
    ) {
        Long userId = UserContext.getCurrentUserId();

        // 验证简历所有权
        UserResume existing = resumeService.getById(resumeId);
        if (existing == null) {
            return Result.error("简历不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            return Result.error("无权操作此简历");
        }

        // 更新完整简历
        UserResume updated = resumeService.updateCompleteResume(resumeId, request);

        // 构建完整响应
        ResumeResponse response = buildResumeResponse(updated);

        log.info("更新完整简历成功: resumeId={}, userId={}", updated.getResumeId(), userId);

        return Result.success("简历更新成功", response);
    }

    /**
     * 获取当前用户的所有简历
     * GET /api/resumes/my
     *
     * @return 简历列表（不包含关联数据）
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
     *
     * @return 默认简历（完整，包含关联数据）
     */
    @GetMapping("/my/default")
    public Result<ResumeResponse> getMyDefaultResume() {
        Long userId = UserContext.getCurrentUserId();
        UserResume resume = resumeService.getDefaultResume(userId);
        if (resume == null) {
            return Result.error("未找到默认简历");
        }

        ResumeResponse response = buildResumeResponse(resume);
        return Result.success(response);
    }

    /**
     * 设置默认简历
     * PUT /api/resumes/{resumeId}/default
     *
     * @param resumeId 简历ID
     * @return 操作结果
     */
    @PutMapping("/{resumeId}/default")
    public Result<String> setDefaultResume(@PathVariable Long resumeId) {
        Long userId = UserContext.getCurrentUserId();

        // 验证简历所有权
        UserResume existing = resumeService.getById(resumeId);
        if (existing == null) {
            return Result.error("简历不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            return Result.error("无权操作此简历");
        }

        resumeService.setDefaultResume(userId, resumeId);
        return Result.success("默认简历设置成功");
    }

    /**
     * 获取完整简历详情（包含关联数据）
     * GET /api/resumes/{resumeId}/complete
     *
     * @param resumeId 简历ID
     * @return 完整简历响应
     */
    @GetMapping("/{resumeId}/complete")
    public Result<ResumeResponse> getCompleteResume(@PathVariable Long resumeId) {
        Long userId = UserContext.getCurrentUserId();

        UserResume resume = resumeService.getById(resumeId);
        if (resume == null) {
            return Result.error("简历不存在");
        }

        // 验证所有权
        if (!resume.getUserId().equals(userId)) {
            return Result.error("无权查看此简历");
        }

        ResumeResponse response = buildResumeResponse(resume);
        return Result.success(response);
    }

    /**
     * 构建完整简历响应
     */
    private ResumeResponse buildResumeResponse(UserResume resume) {
        List<com.jobtracker.entity.ResumeWorkExperience> workExperiences =
                resumeService.getWorkExperiences(resume.getResumeId());
        List<com.jobtracker.entity.ResumeProject> projects =
                resumeService.getProjects(resume.getResumeId());
        List<com.jobtracker.entity.ResumeSkill> skills =
                resumeService.getSkills(resume.getResumeId());

        return ResumeResponse.builder()
                .resumeId(resume.getResumeId())
                .userId(resume.getUserId())
                .resumeName(resume.getResumeName())
                .isDefault(resume.getIsDefault())
                .workYears(resume.getWorkYears())
                .currentPosition(resume.getCurrentPosition())
                .targetLevel(resume.getTargetLevel())
                .summary(resume.getSummary())
                .workExperiences(workExperiences)
                .projects(projects)
                .skills(skills)
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }
}
