package com.jobtracker.controller;

import com.jobtracker.common.result.Result;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索功能控制器
 * <p>
 * 提供多字段搜索功能接口
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-18
 */
@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final ApplicationService applicationService;

    /**
     * 搜索求职申请（多字段）
     * <p>
     * 支持按公司名称、职位名称、备注等字段进行模糊搜索
     * </p>
     *
     * @param keyword 搜索关键词
     * @return 申请列表
     */
    @GetMapping("/applications")
    public Result<List<JobApplication>> searchApplications(@RequestParam String keyword) {
        try {
            List<JobApplication> applications = applicationService.searchApplications(keyword);
            return Result.success("查询成功", applications);
        } catch (Exception e) {
            log.error("搜索申请失败：keyword={}", keyword, e);
            return Result.error("搜索失败：" + e.getMessage());
        }
    }
}
