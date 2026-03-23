package com.jobtracker.agent.interview.dto;

import com.jobtracker.entity.SkillTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 技能标签列表响应
 * <p>
 * 包装类：用于 LangChain4j 正确解析集合类型
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillTagListResponse {

    /**
     * 技能标签列表
     */
    private List<SkillTag> skillTags;
}
