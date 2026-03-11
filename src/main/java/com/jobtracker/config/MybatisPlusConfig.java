package com.jobtracker.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 配置类
 * <p>
 * 配置 MyBatis Plus 的核心功能：
 * - 分页插件
 * - 乐观锁插件
 * - 防止全表更新删除插件
 * - Mapper 扫描配置
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@MapperScan("com.jobtracker.mapper")
public class MybatisPlusConfig {

    /**
     * 配置 MyBatis Plus 拦截器
     * <p>
     * 添加多个插件以增强功能：
     * 1. 分页插件：支持 MySQL 分页查询
     * 2. 乐观锁插件：支持乐观锁机制
     * 3. 防止全表更新删除插件：避免误操作导致数据丢失
     * </p>
     *
     * @return MybatisPlusInterceptor 拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加分页插件（指定数据库类型为 MySQL）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        // 添加乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 添加防止全表更新删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }
}
