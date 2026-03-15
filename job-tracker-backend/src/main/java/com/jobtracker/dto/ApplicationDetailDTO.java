package com.jobtracker.dto;

import com.jobtracker.entity.ApplicationLog;
import com.jobtracker.entity.Company;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.entity.JobApplication;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 申请详情聚合DTO
 */
@Data
@Builder
public class ApplicationDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申请基本信息
     */
    private JobApplication application;

    /**
     * 关联的公司信息
     */
    private Company company;

    /**
     * 该申请的所有面试记录
     */
    private List<InterviewRecord> interviews;

    /**
     * 最近的操作日志
     */
    private List<ApplicationLog> logs;

    /**
     * 统计信息
     */
    private InterviewStatistics statistics;

    /**
     * 面试统计信息
     */
    @Data
    @Builder
    public static class InterviewStatistics implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 面试总数 */
        private Integer total;
        /** 已完成 */
        private Integer completed;
        /** 待进行 */
        private Integer scheduled;
        /** 平均评分 */
        private Double averageRating;
    }
}
