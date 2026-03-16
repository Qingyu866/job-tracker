package com.jobtracker.controller;

import com.jobtracker.common.ApiResponse;
import com.jobtracker.entity.SkillTag;
import com.jobtracker.service.SkillTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 技能标签 API 控制器
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillTagController {

    private final SkillTagService skillTagService;

    /**
     * 获取所有技能标签
     * GET /api/skills
     */
    @GetMapping
    public ApiResponse<List<SkillTag>> getAllSkills() {
        List<SkillTag> skills = skillTagService.getAll();
        return ApiResponse.success(skills);
    }

    /**
     * 根据分类获取技能
     * GET /api/skills/category/{category}
     */
    @GetMapping("/category/{category}")
    public ApiResponse<List<SkillTag>> getSkillsByCategory(@PathVariable String category) {
        List<SkillTag> skills = skillTagService.getByCategory(category);
        return ApiResponse.success(skills);
    }

    /**
     * 搜索技能
     * GET /api/skills/search?keyword=xxx
     */
    @GetMapping("/search")
    public ApiResponse<List<SkillTag>> searchSkills(@RequestParam String keyword) {
        List<SkillTag> skills = skillTagService.searchByName(keyword);
        return ApiResponse.success(skills);
    }

    /**
     * 获取技能详情
     * GET /api/skills/{skillId}
     */
    @GetMapping("/{skillId}")
    public ApiResponse<SkillTag> getSkill(@PathVariable Long skillId) {
        SkillTag skill = skillTagService.getById(skillId);
        if (skill == null) {
            return ApiResponse.notFound("技能不存在");
        }
        return ApiResponse.success(skill);
    }

    /**
     * 创建技能标签
     * POST /api/skills
     */
    @PostMapping
    public ApiResponse<SkillTag> createSkill(@RequestBody SkillTag skill) {
        SkillTag created = skillTagService.create(skill);
        return ApiResponse.success("技能创建成功", created);
    }

    /**
     * 更新技能标签
     * PUT /api/skills/{skillId}
     */
    @PutMapping("/{skillId}")
    public ApiResponse<String> updateSkill(
            @PathVariable Long skillId,
            @RequestBody SkillTag skill
    ) {
        skill.setSkillId(skillId);
        skillTagService.update(skill);
        return ApiResponse.success("技能更新成功");
    }

    /**
     * 删除技能标签
     * DELETE /api/skills/{skillId}
     */
    @DeleteMapping("/{skillId}")
    public ApiResponse<String> deleteSkill(@PathVariable Long skillId) {
        skillTagService.delete(skillId);
        return ApiResponse.success("技能删除成功");
    }

    /**
     * 获取技能的子技能
     * GET /api/skills/{skillId}/children
     */
    @GetMapping("/{skillId}/children")
    public ApiResponse<List<SkillTag>> getChildren(@PathVariable Long skillId) {
        List<SkillTag> children = skillTagService.getChildren(skillId);
        return ApiResponse.success(children);
    }
}
