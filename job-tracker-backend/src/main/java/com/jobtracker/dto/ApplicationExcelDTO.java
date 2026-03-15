package com.jobtracker.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 求职申请导出Excel实体
 *
 * @author Job Tracker Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationExcelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty("ID")
    @ColumnWidth(10)
    private Long id;

    @ExcelProperty("职位名称")
    @ColumnWidth(25)
    private String jobTitle;

    @ExcelProperty("公司名称")
    @ColumnWidth(20)
    private String companyName;

    @ExcelProperty("状态")
    @ColumnWidth(12)
    private String status;

    @ExcelProperty("工作类型")
    @ColumnWidth(12)
    private String jobType;

    @ExcelProperty("工作地点")
    @ColumnWidth(15)
    private String workLocation;

    @ExcelProperty("薪资范围")
    @ColumnWidth(15)
    private String salaryRange;

    @ExcelProperty("申请日期")
    @ColumnWidth(12)
    private String applicationDate;

    @ExcelProperty("优先级")
    @ColumnWidth(8)
    private Integer priority;

    @ExcelProperty("面试次数")
    @ColumnWidth(10)
    private Integer interviewCount;

    @ExcelProperty("备注")
    @ColumnWidth(30)
    private String notes;

    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    private String createdAt;
}
