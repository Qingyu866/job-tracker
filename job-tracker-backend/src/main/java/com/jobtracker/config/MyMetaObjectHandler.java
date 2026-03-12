package com.jobtracker.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 元数据处理器
 * <p>
 * 自动填充公共字段：
 * - created_at: 插入时自动填充
 * - updated_at: 插入和更新时自动填充
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     *
     * @param metaObject 元数据对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");

        // 自动填充创建时间和更新时间
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新时自动填充
     *
     * @param metaObject 元数据对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");

        // 自动填充更新时间
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
