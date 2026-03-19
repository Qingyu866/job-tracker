package com.jobtracker.service;

import com.jobtracker.entity.MockInterviewEvaluation;
import com.jobtracker.mapper.MockInterviewEvaluationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试评估服务
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final MockInterviewEvaluationMapper evaluationMapper;

    /**
     * 创建评分记录
     */
    @Transactional
    public MockInterviewEvaluation createEvaluation(MockInterviewEvaluation evaluation) {
        evaluation.setEvaluatedAt(LocalDateTime.now());
        evaluationMapper.insert(evaluation);
        log.info("创建评分记录，会话: {}, 轮次: {}, 技能: {}, 分数: {}",
                evaluation.getSessionId(),
                evaluation.getRoundNumber(),
                evaluation.getSkillName(),
                evaluation.getTotalScore());
        return evaluation;
    }

    /**
     * 获取会话的所有评分
     */
    public List<MockInterviewEvaluation> getSessionEvaluations(String sessionId) {
        return evaluationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MockInterviewEvaluation>()
                        .eq(MockInterviewEvaluation::getSessionId, sessionId)
                        .orderByAsc(MockInterviewEvaluation::getRoundNumber)
        );
    }

    /**
     * 获取轮次评分
     */
    public MockInterviewEvaluation getRoundEvaluation(String sessionId, Integer roundNumber) {
        List<MockInterviewEvaluation> evaluations = evaluationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MockInterviewEvaluation>()
                        .eq(MockInterviewEvaluation::getSessionId, sessionId)
                        .eq(MockInterviewEvaluation::getRoundNumber, roundNumber)
        );

        return evaluations.isEmpty() ? null : evaluations.get(0);
    }

    /**
     * 计算会话总分
     */
    public BigDecimal calculateTotalScore(String sessionId) {
        List<MockInterviewEvaluation> evaluations = getSessionEvaluations(sessionId);
        if (evaluations.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double sum = evaluations.stream()
                .mapToDouble(e -> e.getTotalScore() != null ? e.getTotalScore().doubleValue() : 0)
                .sum();

        return BigDecimal.valueOf(sum / evaluations.size()).setScale(1, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 生成面试总结
     */
    public String generateSummary(String sessionId) {
        List<MockInterviewEvaluation> evaluations = getSessionEvaluations(sessionId);
        if (evaluations.isEmpty()) {
            return "暂无评分记录";
        }

        // 简单统计
        double avgScore = evaluations.stream()
                .mapToDouble(e -> e.getTotalScore() != null ? e.getTotalScore().doubleValue() : 0)
                .average()
                .orElse(0);

        long goodCount = evaluations.stream()
                .filter(e -> e.getTotalScore() != null && e.getTotalScore().compareTo(BigDecimal.valueOf(7)) >= 0)
                .count();

        return String.format("共进行 %d 轮面试，平均得分 %.1f 分，其中 %d 轮表现优秀（≥7分）。",
                evaluations.size(), avgScore, goodCount);
    }

    /**
     * 更新评估记录的计划状态
     * <p>
     * 根据会话ID和轮次更新对应的评估记录的计划状态
     * </p>
     *
     * @param sessionId 会话ID
     * @param roundNumber 轮次
     * @param newStatus 新状态
     */
    @Transactional
    public void updateEvaluationPlanStatus(String sessionId, Integer roundNumber, String newStatus) {
        evaluationMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<MockInterviewEvaluation>()
                        .eq(MockInterviewEvaluation::getSessionId, sessionId)
                        .eq(MockInterviewEvaluation::getRoundNumber, roundNumber)
                        .set(MockInterviewEvaluation::getPlanStatus, newStatus)
        );

        log.debug("更新评估记录计划状态: sessionId={}, roundNumber={}, newStatus={}",
                sessionId, roundNumber, newStatus);
    }
}
