package com.jobtracker.dto;

import com.jobtracker.entity.ApplicationLog;
import com.jobtracker.entity.Company;
import com.jobtracker.entity.JobApplication;
import lombok.Data;

import java.io.Serializable;

/**
 * 申请日志DTO
 * <p>
 * 包含日志、申请和公司的完整信息
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class ApplicationLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志信息
     */
    private ApplicationLog log;

    /**
     * 申请信息
     */
    private JobApplication application;

    /**
     * 公司信息
     */
    private Company company;
}
