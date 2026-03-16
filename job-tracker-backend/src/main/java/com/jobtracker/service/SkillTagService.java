package com.jobtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jobtracker.entity.SkillTag;
import com.jobtracker.mapper.SkillTagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 技能标签服务
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillTagService {

    private final SkillTagMapper skillTagMapper;

    /**
     * 获取所有技能标签
     */
    public List<SkillTag> getAll() {
        return skillTagMapper.selectList(null);
    }

    /**
     * 根据分类获取技能标签
     */
    public List<SkillTag> getByCategory(String category) {
        return skillTagMapper.selectList(
                new LambdaQueryWrapper<SkillTag>()
                        .eq(SkillTag::getCategory, category)
                        .orderByAsc(SkillTag::getDifficultyBase)
        );
    }

    /**
     * 根据名称模糊搜索技能
     */
    public List<SkillTag> searchByName(String keyword) {
        return skillTagMapper.selectList(
                new LambdaQueryWrapper<SkillTag>()
                        .like(SkillTag::getSkillName, keyword)
                        .orderByAsc(SkillTag::getDifficultyBase)
        );
    }

    /**
     * 根据名称精确获取技能
     */
    public SkillTag getByName(String name) {
        return skillTagMapper.selectOne(
                new LambdaQueryWrapper<SkillTag>()
                        .eq(SkillTag::getSkillName, name)
        );
    }

    /**
     * 根据ID获取技能
     */
    public SkillTag getById(Long id) {
        return skillTagMapper.selectById(id);
    }

    /**
     * 创建新技能标签
     */
    public SkillTag create(SkillTag skillTag) {
        skillTagMapper.insert(skillTag);
        return skillTag;
    }

    /**
     * 更新技能标签
     */
    public void update(SkillTag skillTag) {
        skillTagMapper.updateById(skillTag);
    }

    /**
     * 删除技能标签
     */
    public void delete(Long id) {
        skillTagMapper.deleteById(id);
    }

    /**
     * 获取技能的子技能
     */
    public List<SkillTag> getChildren(Long parentId) {
        return skillTagMapper.selectList(
                new LambdaQueryWrapper<SkillTag>()
                        .eq(SkillTag::getParentId, parentId)
                        .orderByAsc(SkillTag::getSkillName)
        );
    }

    /**
     * 增加技能热度
     */
    public void incrementHotScore(Long skillId) {
        SkillTag skill = skillTagMapper.selectById(skillId);
        if (skill != null) {
            skill.setHotScore(skill.getHotScore() + 1);
            skillTagMapper.updateById(skill);
        }
    }
}
