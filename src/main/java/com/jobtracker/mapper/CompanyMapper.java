package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.Company;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公司数据访问层接口
 * <p>
 * 基于 MyBatis Plus 的 BaseMapper，提供对公司表（companies）的 CRUD 操作
 * 自动继承以下方法：
 * - insert: 插入单条记录
 * - deleteById: 根据 ID 删除
 * - updateById: 根据 ID 更新
 * - selectById: 根据 ID 查询
 * - selectList: 查询列表
 * - selectPage: 分页查询
 * 等等...
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper
public interface CompanyMapper extends BaseMapper<Company> {

    // BaseMapper 已提供所有基础 CRUD 方法
    // 如需自定义 SQL，可在此添加方法并使用 @Select、@Insert 等注解
    // 或创建对应的 XML 文件
}
