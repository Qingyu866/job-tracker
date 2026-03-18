package com.jobtracker.controller;

import com.alibaba.excel.EasyExcel;
import com.jobtracker.common.result.Result;
import com.jobtracker.dto.ApplicationDetailDTO;
import com.jobtracker.dto.ApplicationExcelDTO;
import com.jobtracker.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * 数据导出控制器
 * <p>
 * 提供数据导出功能接口
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-18
 */
@Slf4j
@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    /**
     * 获取申请详情（聚合信息）
     * <p>
     * 返回申请的完整信息，包括：
     * - 申请基本信息
     * - 关联的公司信息
     * - 所有面试记录
     * - 最近操作日志
     * - 统计信息
     * </p>
     *
     * @param id 申请ID
     * @return 申请详情聚合DTO
     */
    @GetMapping("/applications/{id}/detail")
    public Result<ApplicationDetailDTO> getApplicationDetail(@PathVariable Long id) {
        try {
            ApplicationDetailDTO detail = exportService.getApplicationDetail(id);
            if (detail == null) {
                return Result.error("申请不存在");
            }
            return Result.success("查询成功", detail);
        } catch (Exception e) {
            log.error("获取申请详情失败：id={}", id, e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 导出数据为Excel格式
     * <p>
     * 使用EasyExcel导出所有求职申请数据
     * </p>
     *
     * @param response HTTP响应
     */
    @GetMapping("/excel")
    public void exportExcel(HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("求职记录_" + LocalDate.now(), StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            // 获取数据并导出
            List<ApplicationExcelDTO> data = exportService.getExcelExportData();

            EasyExcel.write(response.getOutputStream(), ApplicationExcelDTO.class)
                    .sheet("求职记录")
                    .doWrite(data);

            log.info("Excel导出成功：共 {} 条记录", data.size());
        } catch (Exception e) {
            log.error("Excel导出失败", e);
        }
    }

    /**
     * 导出数据为JSON格式
     *
     * @return 所有申请的详情列表
     */
    @GetMapping("/json")
    public Result<List<ApplicationDetailDTO>> exportJson() {
        try {
            List<ApplicationDetailDTO> data = exportService.getJsonExportData();
            log.info("JSON导出成功：共 {} 条记录", data.size());
            return Result.success("导出成功", data);
        } catch (Exception e) {
            log.error("JSON导出失败", e);
            return Result.error("导出失败：" + e.getMessage());
        }
    }
}
