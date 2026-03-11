package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公司实体类
 * <p>
 * 对应数据库表：companies
 * 存储公司基本信息，包括名称、行业、规模、地址等
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("companies")
public class Company implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公司ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公司名称（必填）
     */
    private String name;

    /**
     * 行业类型
     */
    private String industry;

    /**
     * 公司规模（如：100-499人）
     */
    private String size;

    /**
     * 公司地址
     */
    private String location;

    /**
     * 公司官网
     */
    private String website;

    /**
     * 公司描述
     */
    private String description;

    /**
     * 公司Logo URL
     */
    private String logoUrl;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记（0:未删除, 1:已删除）
     */
    @TableLogic
    private Integer deleted;
}
