package com.jobtracker.agent.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 技能可信度列表响应
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
public class SkillCredibilityListResponse {

    /**
     * 技能可信度列表
     */
    private List<SkillCredibility> credibilities;
}
