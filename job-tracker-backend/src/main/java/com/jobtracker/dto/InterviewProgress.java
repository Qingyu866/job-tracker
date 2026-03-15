package com.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 面试进度数据传输对象
 * <p>
 * 用于表示申请的面试进度信息，支持多轮面试的进度追踪
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewProgress {

    /**
     * 总轮次数
     */
    private int totalRounds;

    /**
     * 已完成轮次数
     */
    private int completedRounds;

    /**
     * 通过轮次数
     */
    private int passedRounds;

    /**
     * 当前进行中的轮次（0表示没有进行中的面试）
     */
    private int currentRound;

    /**
     * 进度文本描述
     */
    private String progressText;

    /**
     * 是否所有面试都已通过
     */
    private boolean allPassed;

    /**
     * 是否有任何面试失败
     */
    private boolean hasFailed;
}
