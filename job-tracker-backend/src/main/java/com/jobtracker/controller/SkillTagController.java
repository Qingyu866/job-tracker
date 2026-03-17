package com.jobtracker.controller;

import com.jobtracker.common.result.Result;
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
    public Result<List<SkillTag>> getAllSkills() {
        List<SkillTag> skills = skillTagService.getAll();
        return Result.success(skills);
    }

    /**
     * 根据分类获取技能
     * GET /api/skills/category/{category}
     */
    @GetMapping("/category/{category}")
    public Result<List<SkillTag>> getSkillsByCategory(@PathVariable String category) {
        List<SkillTag> skills = skillTagService.getByCategory(category);
        return Result.success(skills);
    }

    /**
     * 搜索技能
     * GET /api/skills/search?keyword=xxx
     */
    @GetMapping("/search")
    public Result<List<SkillTag>> searchSkills(@RequestParam String keyword) {
        List<SkillTag> skills = skillTagService.searchByName(keyword);
        return Result.success(skills);
    }

    /**
     * 获取技能详情
     * GET /api/skills/{skillId}
     */
    @GetMapping("/{skillId}")
    public Result<SkillTag> getSkill(@PathVariable Long skillId) {
        SkillTag skill = skillTagService.getById(skillId);
        if (skill == null) {
            return Result.error("技能不存在");
        }
        return Result.success(skill);
    }

    /**
     * 创建技能标签
     * POST /api/skills
     */
    @PostMapping
    public Result<SkillTag> createSkill(@RequestBody SkillTag skill) {
        SkillTag created = skillTagService.create(skill);
        return Result.success("技能创建成功", created);
    }

    /**
     * 更新技能标签
     * PUT /api/skills/{skillId}
     */
    @PutMapping("/{skillId}")
    public Result<String> updateSkill(
            @PathVariable Long skillId,
            @RequestBody SkillTag skill
    ) {
        skill.setSkillId(skillId);
        skillTagService.update(skill);
        return Result.success("技能更新成功");
    }

    /**
     * 删除技能标签
     * DELETE /api/skills/{skillId}
     */
    @DeleteMapping("/{skillId}")
    public Result<String> deleteSkill(@PathVariable Long skillId) {
        skillTagService.delete(skillId);
        return Result.success("技能删除成功");
    }

    /**
     * 获取技能的子技能
     * GET /api/skills/{skillId}/children
     */
    @GetMapping("/{skillId}/children")
    public Result<List<SkillTag>> getChildren(@PathVariable Long skillId) {
        List<SkillTag> children = skillTagService.getChildren(skillId);
        return Result.success(children);
    }
}
