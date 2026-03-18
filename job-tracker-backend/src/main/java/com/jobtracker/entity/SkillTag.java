package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 技能标签实体
 * <p>
 * 存储知识点（如：HashMap、Redis 集群、Spring Boot 自动装配）
 * 作为"待考察知识点清单"，支持层级结构
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("skill_tags")
public class SkillTag {

    /**
     * 技能ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long skillId;

    /**
     * 技能名称：Java, HashMap, Spring Boot等
     */
    private String skillName;

    /**
     * 分类：LANGUAGE/FRAMEWORK/DATABASE/TOOL/ALGORITHM
     */
    private String category;

    /**
     * 父技能ID（支持层级结构：Java → Collection → HashMap）
     */
    private Long parentId;

    /**
     * 技能描述
     */
    private String description;

    /**
     * 基础难度：1-5
     */
    private Integer difficultyBase;

    /**
     * 热度分数（根据使用频率更新）
     */
    private Integer hotScore;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 技能分类枚举
     * <p>
     * 将技能按领域分类，便于组织和检索
     * </p>
     * <p>
     * <b>用途</b>:
     * </p>
     * <ul>
     *   <li>构建技能树结构（父子关系）</li>
     *   <li>按分类筛选待考察知识点</li>
     *   <li>统计候选人的技能分布</li>
     * </ul>
     */
    public enum Category {
        /**
         * 编程语言
         * <p>
         * <b>包含内容</b>:
         * </p>
         * <ul>
         *   <li>基础语言：Java, Python, C++, Go, JavaScript</li>
         *   <li>语言特性：泛型, 反射, 并发, 异步</li>
         *   <li>语法糖：Lambda, Stream, Optional</li>
         * </ul>
         * <p>
         * <b>层级结构示例</b>:
         * </p>
         * <pre>
         * LANGUAGE (编程语言)
         *   └── Java
         *       ├── 集合框架
         *       │   ├── HashMap
         *       │   └── ConcurrentHashMap
         *       ├── 并发编程
         *       │   ├── 线程池
         *       │   └── 锁机制
         *       └── JVM
         *           ├── 内存模型
         *           └── GC 垃圾回收
         * </pre>
         */
        LANGUAGE,

        /**
         * 框架/中间件
         * <p>
         * <b>包含内容</b>:
         * </p>
         * <ul>
         *   <li>Web 框架：Spring Boot, Spring Cloud, Django, Express</li>
         *   <li>ORM 框架：MyBatis, Hibernate, JPA</li>
         *   <li>消息队列：Kafka, RabbitMQ, RocketMQ</li>
         *   <li>搜索引擎：Elasticsearch, Solr</li>
         * </ul>
         * <p>
         * <b>层级结构示例</b>:
         * </p>
         * <pre>
         * FRAMEWORK (框架)
         *   └── Spring
         *       ├── Spring Boot
         *       │   ├── 自动装配
         *       │   └── Starter 机制
         *       ├── Spring MVC
         *       │   └── 拦截器
         *       └── Spring Cloud
         *           ├── 服务注册
         *           └── 配置中心
         * </pre>
         */
        FRAMEWORK,

        /**
         * 数据库/存储
         * <p>
         * <b>包含内容</b>:
         * </p>
         * <ul>
         *   <li>关系型数据库：MySQL, PostgreSQL, Oracle</li>
         *   <li>NoSQL 数据库：Redis, MongoDB, Cassandra</li>
         *   <li>搜索引擎：Elasticsearch, Solr</li>
         *   <li>大数据存储：Hadoop, HBase, Hive</li>
         * </ul>
         * <p>
         * <b>层级结构示例</b>:
         * </p>
         * <pre>
         * DATABASE (数据库)
         *   └── MySQL
         *       ├── 索引优化
         *       │   ├── B+树原理
         *       │   └── 联合索引
         *       ├── 事务机制
         *       │   ├── 隔离级别
         *       │   └── MVCC
         *       └── 性能调优
         *           ├── 慢查询
         *           └── 分库分表
         * </pre>
         */
        DATABASE,

        /**
         * 开发工具/平台
         * <p>
         * <b>包含内容</b>:
         * </p>
         * <ul>
         *   <li>版本控制：Git, SVN</li>
         *   <li>构建工具：Maven, Gradle, Webpack</li>
         *   <li>容器技术：Docker, Kubernetes</li>
         *   <li>CI/CD：Jenkins, GitLab CI, GitHub Actions</li>
         *   <li>监控工具：Prometheus, Grafana, ELK</li>
         * </ul>
         * <p>
         * <b>层级结构示例</b>:
         * </p>
         * <pre>
         * TOOL (工具)
         *   └── Docker
         *       ├── 镜像管理
         *       ├── 容器编排
         *       └── 网络配置
         *   └── Git
         *       ├── 分支管理
         *       ├── 合并策略
         *       └── 冲突解决
         * </pre>
         */
        TOOL,

        /**
         * 算法与数据结构
         * <p>
         * <b>包含内容</b>:
         * </p>
         * <ul>
         *   <li>基础数据结构：数组, 链表, 树, 图, 哈希表</li>
         *   <li>算法：排序, 查找, 动态规划, 贪心, 回溯</li>
         *   <li>设计模式：单例, 工厂, 观察者, 策略</li>
         *   <li>系统设计：高并发, 分布式, 缓存, 消息队列</li>
         * </ul>
         * <p>
         * <b>层级结构示例</b>:
         * </p>
         * <pre>
         * ALGORITHM (算法)
         *   ├── 数据结构
         *   │   ├── 数组与链表
         *   │   ├── 树结构
         *   │   │   ├── 二叉树
         *   │   │   ├── 红黑树
         *   │   │   └── B+树
         *   │   └── 哈希表
         *   ├── 算法
         *   │   ├── 排序算法
         *   │   │   ├── 快速排序
         *   │   │   └── 归并排序
         *   │   └── 查找算法
         *   │       ├── 二分查找
         *   │       └── BFS/DFS
         *   └── 设计模式
         *       ├── 创建型模式
         *       ├── 结构型模式
         *       └── 行为型模式
         * </pre>
         */
        ALGORITHM
    }
}
