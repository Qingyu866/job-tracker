package com.jobtracker.controller;

import com.jobtracker.common.result.Result;
import com.jobtracker.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计数据控制器
 * <p>
 * 提供统计数据查询接口
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-18
 */
@Slf4j
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final ApplicationService applicationService;

    /**
     * 获取统计数据
     * <p>
     * 返回按状态分组的申请统计数据
     * </p>
     *
     * @return 统计数据列表
     */
    @GetMapping
    public Result<List<Object>> getStatistics() {
        try {
            List<Object> stats = applicationService.countByStatus();
            return Result.success("查询成功", stats);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }
}
