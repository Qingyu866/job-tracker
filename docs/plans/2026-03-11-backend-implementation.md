# 求职追踪应用后端实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans or superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** 构建一个基于 LangChain4j 的 Spring Boot 后端，支持 AI 对话驱动的方式来管理求职投递记录。

**Architecture:**
- Spring Boot 3.2+ 提供传统 REST API 和 WebSocket 实时通信
- LangChain4j 1.12.1 集成 Gemma3-4b 模型，实现 Function Calling
- MyBatis Plus 访问 MySQL 数据库
- AI Agent 通过 @Tool 注解的方法操作数据库
- WebSocket 流式返回 AI 回复，并推送视图更新事件

**Tech Stack:**
- Spring Boot 3.2+, JDK 17+
- LangChain4j 1.12.1 (OpenAI 兼容接口)
- MyBatis Plus 3.5.5+
- MySQL 8.0+
- WebSocket (Spring WebSocket)
- Lombok, Swagger

---

## 前置准备

### 开始前的检查清单

- [ ] MySQL 8.0+ 已安装并运行
- [ ] LM Studio 已安装，Gemma3-4b 模型已下载
- [ ] LM Studio API Server 已启动（默认端口 1234）
- [ ] JDK 17+ 已安装
- [ ] Maven 3.6+ 已安装

---

## Task 1: 项目初始化

**Files:**
- Create: `job-tracker-backend/pom.xml`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/JobTrackerApplication.java`
- Create: `job-tracker-backend/src/main/resources/application.yml`

**Step 1: 创建项目目录结构**

```bash
mkdir -p job-tracker-backend/src/main/java/com/jobtracker
mkdir -p job-tracker-backend/src/main/resources
mkdir -p job-tracker-backend/src/test/java/com/jobtracker
```

**Step 2: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.3</version>
        <relativePath/>
    </parent>

    <groupId>com.jobtracker</groupId>
    <artifactId>job-tracker-backend</artifactId>
    <version>1.0.0</version>
    <name>Job Tracker Backend</name>
    <description>AI-powered job application tracker</description>

    <properties>
        <java.version>17</java.version>
        <langchain4j.version>1.12.1</langchain4j.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
    </properties>

    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- LangChain4j -->
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-open-ai</artifactId>
            <version>${langchain4j.version}</version>
        </dependency>

        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Swagger -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**Step 3: 创建主启动类**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/JobTrackerApplication.java`

```java
package com.jobtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JobTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobTrackerApplication.class, args);
    }
}
```

**Step 4: 创建 application.yml 配置文件**

创建文件: `job-tracker-backend/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: job-tracker-backend

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/job_tracker?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD:your_password}

server:
  port: 8080

# AI 服务配置
ai:
  api:
    base-url: ${AI_BASE_URL:http://localhost:1234/v1}
    model-name: ${AI_MODEL:gemma-3-4b-it}
    timeout: 60s

# MyBatis Plus 配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# Swagger 配置
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true

logging:
  level:
    com.jobtracker: DEBUG
    dev.langchain4j: DEBUG
```

**Step 5: 创建 .gitignore**

```bash
cd job-tracker-backend
cat > .gitignore << 'EOF'
# Maven
target/
!.mvn/wrapper/maven-wrapper.jar

# IDE
.idea/
*.iml
.vscode/

# Logs
*.log

# Environment
.env
application-local.yml
EOF
```

**Step 6: 验证项目启动**

```bash
cd job-tracker-backend
mvn clean install
mvn spring-boot:run
```

预期输出: Spring Boot 启动成功，监听 8080 端口

**Step 7: 访问 Swagger 验证**

浏览器访问: `http://localhost:8080/swagger-ui.html`
预期: 看到 Swagger UI 页面

**Step 8: 提交**

```bash
git add .
git commit -m "feat: initialize Spring Boot project with dependencies"
```

---

## Task 2: 数据库初始化

**Files:**
- Create: `job-tracker-backend/src/main/resources/db/migration/V1__init_schema.sql`

**Step 1: 创建数据库**

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS job_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

**Step 2: 创建数据库初始化脚本**

创建文件: `job-tracker-backend/src/main/resources/db/migration/V1__init_schema.sql`

```sql
-- 公司表
CREATE TABLE IF NOT EXISTS companies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '公司ID',
    name VARCHAR(200) NOT NULL COMMENT '公司名称',
    website VARCHAR(500) COMMENT '招聘官网URL',
    industry VARCHAR(100) COMMENT '所属行业',
    description TEXT COMMENT '公司描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司信息表';

-- 投递记录表
CREATE TABLE IF NOT EXISTS job_applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '投递ID',
    company_id BIGINT NOT NULL COMMENT '公司ID',
    position VARCHAR(200) NOT NULL COMMENT '职位名称',
    job_description TEXT COMMENT '职位描述',
    status VARCHAR(50) DEFAULT 'APPLIED' COMMENT '状态：APPLIED/RESUME_SCREENING/INTERVIEWING/OFFER/REJECTED',
    salary_min DECIMAL(10,2) COMMENT '最低薪资',
    salary_max DECIMAL(10,2) COMMENT '最高薪资',
    work_location VARCHAR(200) COMMENT '工作地点',
    application_url VARCHAR(1000) COMMENT '申请链接',
    applied_at DATETIME COMMENT '投递时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_applied_at (applied_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='求职申请表';

-- 面试记录表
CREATE TABLE IF NOT EXISTS interview_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '面试记录ID',
    application_id BIGINT NOT NULL COMMENT '申请ID',
    interview_date DATETIME NOT NULL COMMENT '面试日期',
    interview_type VARCHAR(50) COMMENT '面试类型：VIDEO/ONSITE/PHONE',
    interviewer VARCHAR(200) COMMENT '面试官',
    questions TEXT COMMENT '面试问题（JSON）',
    answers TEXT COMMENT '回答记录',
    feedback TEXT COMMENT '面试反馈',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (application_id) REFERENCES job_applications(id) ON DELETE CASCADE,
    INDEX idx_interview_date (interview_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试记录表';

-- 状态变更日志表
CREATE TABLE IF NOT EXISTS application_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    application_id BIGINT NOT NULL COMMENT '申请ID',
    action VARCHAR(100) NOT NULL COMMENT '操作类型',
    description TEXT COMMENT '描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (application_id) REFERENCES job_applications(id) ON DELETE CASCADE,
    INDEX idx_application_id (application_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申请日志表';
```

**Step 3: 执行数据库脚本**

```bash
mysql -u root -p job_tracker < job-tracker-backend/src/main/resources/db/migration/V1__init_schema.sql
```

**Step 4: 验证表创建**

```bash
mysql -u root -p -e "USE job_tracker; SHOW TABLES;"
```

预期输出:
```
+-------------------------+
| Tables_in_job_tracker   |
+-------------------------+
| application_logs        |
| companies               |
| interview_records       |
| job_applications        |
+-------------------------+
```

**Step 5: 提交**

```bash
git add .
git commit -m "feat: add database schema and migration scripts"
```

---

## Task 3: 实体类和枚举定义

**Files:**
- Create: `job-tracker-backend/src/main/java/com/jobtracker/entity/Company.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/entity/JobApplication.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/entity/InterviewRecord.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/entity/ApplicationLog.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/constants/ApplicationStatus.java`

**Step 1: 创建状态枚举**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/constants/ApplicationStatus.java`

```java
package com.jobtracker.constants;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 投递状态枚举
 */
public enum ApplicationStatus {
    APPLIED("已投递"),
    RESUME_SCREENING("简历筛选中"),
    INTERVIEWING("面试中"),
    OFFER("已获得offer"),
    REJECTED("已拒绝");

    @EnumValue
    @JsonValue
    private final String description;

    ApplicationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
```

**Step 2: 创建公司实体**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/entity/Company.java`

```java
package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公司实体
 */
@Data
@TableName("companies")
public class Company {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String website;

    private String industry;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

**Step 3: 创建投递实体**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/entity/JobApplication.java`

```java
package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.jobtracker.constants.ApplicationStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 求职申请实体
 */
@Data
@TableName("job_applications")
public class JobApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long companyId;

    private String position;

    private String jobDescription;

    private ApplicationStatus status;

    private BigDecimal salaryMin;

    private BigDecimal salaryMax;

    private String workLocation;

    private String applicationUrl;

    private LocalDateTime appliedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 非数据库字段
    @TableField(exist = false)
    private Company company;
}
```

**Step 4: 创建面试记录实体**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/entity/InterviewRecord.java`

```java
package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 面试记录实体
 */
@Data
@TableName("interview_records")
public class InterviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicationId;

    private LocalDateTime interviewDate;

    private String interviewType;

    private String interviewer;

    private String questions;

    private String answers;

    private String feedback;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

**Step 5: 创建日志实体**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/entity/ApplicationLog.java`

```java
package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 申请日志实体
 */
@Data
@TableName("application_logs")
public class ApplicationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicationId;

    private String action;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

**Step 6: 创建 MyBatis Plus 配置**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/config/MybatisPlusConfig.java`

```java
package com.jobtracker.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 配置
 */
@Configuration
@MapperScan("com.jobtracker.mapper")
public class MybatisPlusConfig {

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

**Step 7: 提交**

```bash
git add .
git commit -m "feat: add entity classes and enums"
```

---

## Task 4: Mapper 层

**Files:**
- Create: `job-tracker-backend/src/main/java/com/jobtracker/mapper/CompanyMapper.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/mapper/ApplicationMapper.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/mapper/InterviewMapper.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/mapper/ApplicationLogMapper.java`

**Step 1: 创建 CompanyMapper**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/mapper/CompanyMapper.java`

```java
package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.Company;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公司 Mapper
 */
@Mapper
public interface CompanyMapper extends BaseMapper<Company> {
}
```

**Step 2: 创建 ApplicationMapper**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/mapper/ApplicationMapper.java`

```java
package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jobtracker.entity.JobApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 投递记录 Mapper
 */
@Mapper
public interface ApplicationMapper extends BaseMapper<JobApplication> {

    /**
     * 分页查询投递记录（包含公司信息）
     */
    @Select("""
        SELECT ja.*,
               c.id as company_id,
               c.name as company_name,
               c.website as company_website
        FROM job_applications ja
        LEFT JOIN companies c ON ja.company_id = c.id
        ORDER BY ja.created_at DESC
        """)
    IPage<JobApplication> selectPageWithCompany(Page<JobApplication> page);

    /**
     * 模糊搜索投递记录
     */
    @Select("""
        SELECT ja.*, c.name as company_name
        FROM job_applications ja
        LEFT JOIN companies c ON ja.company_id = c.id
        WHERE c.name LIKE CONCAT('%', #{keyword}, '%')
           OR ja.position LIKE CONCAT('%', #{keyword}, '%')
        ORDER BY ja.created_at DESC
        """)
    IPage<JobApplication> fuzzySearch(Page<JobApplication> page, @Param("keyword") String keyword);
}
```

**Step 3: 创建 InterviewMapper**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/mapper/InterviewMapper.java`

```java
package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.InterviewRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试记录 Mapper
 */
@Mapper
public interface InterviewMapper extends BaseMapper<InterviewRecord> {
}
```

**Step 4: 创建 ApplicationLogMapper**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/mapper/ApplicationLogMapper.java`

```java
package com.jobtracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobtracker.entity.ApplicationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 申请日志 Mapper
 */
@Mapper
public interface ApplicationLogMapper extends BaseMapper<ApplicationLog> {
}
```

**Step 5: 提交**

```bash
git add .
git commit -m "feat: add Mapper layer with custom queries"
```

---

## Task 5: Service 层 - 基础 CRUD

**Files:**
- Create: `job-tracker-backend/src/main/java/com/jobtracker/service/CompanyService.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/service/ApplicationService.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/service/InterviewService.java`

**Step 1: 创建 CompanyService**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/service/CompanyService.java`

```java
package com.jobtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jobtracker.entity.Company;
import com.jobtracker.mapper.CompanyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公司服务
 */
@Slf4j
@Service
public class CompanyService extends ServiceImpl<CompanyMapper, Company> {

    /**
     * 根据名称查找公司
     */
    public Company findByName(String name) {
        QueryWrapper<Company> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        return this.getOne(wrapper);
    }

    /**
     * 模糊搜索公司
     */
    public List<Company> searchByName(String keyword) {
        QueryWrapper<Company> wrapper = new QueryWrapper<>();
        wrapper.like("name", keyword);
        return this.list(wrapper);
    }
}
```

**Step 2: 创建 ApplicationService**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/service/ApplicationService.java`

```java
package com.jobtracker.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jobtracker.constants.ApplicationStatus;
import com.jobtracker.entity.ApplicationLog;
import com.jobtracker.entity.Company;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.mapper.ApplicationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 投递记录服务
 */
@Slf4j
@Service
public class ApplicationService extends ServiceImpl<ApplicationMapper, JobApplication> {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private ApplicationLogService logService;

    /**
     * 创建投递记录
     */
    @Transactional
    public JobApplication createApplication(String companyName, String position,
                                           String description, String location) {
        // 查找或创建公司
        Company company = companyService.findByName(companyName);
        if (company == null) {
            company = new Company();
            company.setName(companyName);
            companyService.save(company);
            log.info("创建新公司: {}", companyName);
        }

        // 创建投递记录
        JobApplication application = new JobApplication();
        application.setCompanyId(company.getId());
        application.setPosition(position);
        application.setJobDescription(description);
        application.setWorkLocation(location);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());

        this.save(application);

        // 记录日志
        logService.createLog(application.getId(), "CREATE", "创建投递记录");

        log.info("创建投递记录: {} - {}", companyName, position);
        return application;
    }

    /**
     * 更新投递状态
     */
    @Transactional
    public void updateStatus(Long applicationId, ApplicationStatus newStatus) {
        JobApplication application = this.getById(applicationId);
        if (application == null) {
            throw new RuntimeException("投递记录不存在: " + applicationId);
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(newStatus);
        this.updateById(application);

        // 记录日志
        logService.createLog(applicationId, "STATUS_CHANGE",
                String.format("状态从 %s 变更为 %s", oldStatus.getDescription(), newStatus.getDescription()));

        log.info("更新投递状态: {} -> {}", oldStatus, newStatus);
    }

    /**
     * 分页查询投递记录（包含公司信息）
     */
    public IPage<JobApplication> getApplicationsWithCompany(int page, int size) {
        Page<JobApplication> pageParam = new Page<>(page, size);
        return this.baseMapper.selectPageWithCompany(pageParam);
    }

    /**
     * 模糊搜索投递记录
     */
    public List<JobApplication> fuzzySearch(String keyword) {
        QueryWrapper<JobApplication> wrapper = new QueryWrapper<>();
        // 模糊搜索公司名和职位
        return this.baseMapper.fuzzySearch(new Page<>(1, 10), keyword).getRecords();
    }

    /**
     * 获取统计数据
     */
    public StatisticsDTO getStatistics() {
        StatisticsDTO stats = new StatisticsDTO();

        // 总投递数
        stats.totalApplications = this.count();

        // 各状态数量
        for (ApplicationStatus status : ApplicationStatus.values()) {
            QueryWrapper<JobApplication> wrapper = new QueryWrapper<>();
            wrapper.eq("status", status);
            long count = this.count(wrapper);
            stats.statusCounts.put(status, count);
        }

        return stats;
    }

    /**
     * 统计数据 DTO
     */
    @lombok.Data
    public static class StatisticsDTO {
        private Long totalApplications = 0L;
        private java.util.Map<ApplicationStatus, Long> statusCounts = new java.util.HashMap<>();
    }
}
```

**Step 3: 创建 ApplicationLogService**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/service/ApplicationLogService.java`

```java
package com.jobtracker.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jobtracker.entity.ApplicationLog;
import com.jobtracker.mapper.ApplicationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 申请日志服务
 */
@Slf4j
@Service
public class ApplicationLogService extends ServiceImpl<ApplicationLogMapper, ApplicationLog> {

    /**
     * 创建日志
     */
    public void createLog(Long applicationId, String action, String description) {
        ApplicationLog log = new ApplicationLog();
        log.setApplicationId(applicationId);
        log.setAction(action);
        log.setDescription(description);
        this.save(log);
    }
}
```

**Step 4: 创建 InterviewService**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/service/InterviewService.java`

```java
package com.jobtracker.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jobtracker.entity.InterviewRecord;
import com.jobtracker.mapper.InterviewMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试记录服务
 */
@Slf4j
@Service
public class InterviewService extends ServiceImpl<InterviewMapper, InterviewRecord> {

    @Autowired
    private ApplicationService applicationService;

    /**
     * 创建面试记录
     */
    public InterviewRecord createInterview(Long applicationId, LocalDateTime interviewDate,
                                          String type, String interviewer) {
        InterviewRecord record = new InterviewRecord();
        record.setApplicationId(applicationId);
        record.setInterviewDate(interviewDate);
        record.setInterviewType(type);
        record.setInterviewer(interviewer);

        this.save(record);

        log.info("创建面试记录: 投递ID={}, 时间={}", applicationId, interviewDate);
        return record;
    }

    /**
     * 获取投递的所有面试记录
     */
    public List<InterviewRecord> getByApplicationId(Long applicationId) {
        return lambdaQuery()
                .eq(InterviewRecord::getApplicationId, applicationId)
                .orderByAsc(InterviewRecord::getInterviewDate)
                .list();
    }
}
```

**Step 5: 提交**

```bash
git add .
git commit -m "feat: add service layer with CRUD operations"
```

---

## Task 6: LangChain4j 集成 - 配置和基础架构

**Files:**
- Create: `job-tracker-backend/src/main/java/com/jobtracker/config/LangChain4jConfig.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/agent/JobAgent.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/agent/prompt/SystemPromptTemplate.java`

**Step 1: 创建 LangChain4j 配置**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/config/LangChain4jConfig.java`

```java
package com.jobtracker.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * LangChain4j 配置
 */
@Configuration
public class LangChain4jConfig {

    @Value("${ai.api.base-url}")
    private String baseUrl;

    @Value("${ai.api.model-name}")
    private String modelName;

    @Bean
    public StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }

    @Bean
    public JobAgent jobAgent(StreamingChatModel model,
                            ChatMemory memory,
                            List<Object> tools) {
        return AiServices.builder(JobAgent.class)
                .streamingChatModel(model)
                .chatMemory(memory)
                .tools(tools)
                .build();
    }
}
```

**Step 2: 创建 Agent 接口**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/agent/JobAgent.java`

```java
package com.jobtracker.agent;

import dev.langchain4j.service.spring.AiService;

/**
 * 求职助手 AI Agent
 */
@AiService
public interface JobAgent {

    /**
     * 聊天接口
     */
    String chat(String userMessage);
}
```

**Step 3: 创建 System Prompt**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/agent/prompt/SystemPromptTemplate.java`

```java
package com.jobtracker.agent.prompt;

/**
 * System Prompt 模板
 */
public class SystemPromptTemplate {

    public static final String SYSTEM_PROMPT = """
            你是一个求职追踪助手，帮助用户管理和追踪求职进度。

            你的能力：
            1. 记录和管理投递信息（公司、职位、状态、薪资等）
            2. 追踪面试安排和记录
            3. 提供统计数据和分析
            4. 帮助用户查询历史记录

            交互原则：
            - 使用友好、专业的语气
            - 当信息不明确时，主动询问用户确认
            - 操作成功后，用自然语言总结结果
            - 如果用户引用之前的记录，在对话历史中查找上下文

            状态说明：
            - APPLIED: 已投递
            - RESUME_SCREENING: 简历筛选中
            - INTERVIEWING: 面试中
            - OFFER: 已获得offer
            - REJECTED: 已拒绝

            面试类型：
            - VIDEO: 视频面试
            - ONSITE: 现场面试
            - PHONE: 电话面试

            重要提醒：
            - 创建投递时，如果公司不存在，先询问是否创建新公司
            - 更新状态时，如果记录有多条匹配，列出选项让用户选择
            - 始终使用工具方法操作数据，不要编造信息
            """;
}
```

**Step 4: 验证依赖可用**

```bash
cd job-tracker-backend
mvn clean compile
```

预期: 编译成功，无错误

**Step 5: 提交**

```bash
git add .
git commit -m "feat: add LangChain4j configuration and base Agent"
```

---

## Task 7: LangChain4j 工具方法 - ApplicationTools

**Files:**
- Create: `job-tracker-backend/src/main/java/com/jobtracker/agent/tools/ApplicationTools.java`

**Step 1: 创建 ApplicationTools**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/agent/tools/ApplicationTools.java`

```java
package com.jobtracker.agent.tools;

import com.jobtracker.service.ApplicationService;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 投递相关工具
 */
@Slf4j
@Component
public class ApplicationTools {

    @Autowired
    private ApplicationService applicationService;

    @Tool("创建新的投递记录")
    public String createApplication(
            @P("公司名称") String companyName,
            @P("职位名称") String position,
            @P("职位描述（可选）") String description,
            @P("最低薪资（可选）") BigDecimal salaryMin,
            @P("最高薪资（可选）") BigDecimal salaryMax,
            @P("工作地点（可选）") String location
    ) {
        try {
            var application = applicationService.createApplication(
                    companyName, position, description, location
            );

            return String.format("已成功创建投递记录：%s - %s。状态：已投递。",
                    companyName, position);
        } catch (Exception e) {
            log.error("创建投递失败", e);
            return "创建投递失败：" + e.getMessage();
        }
    }

    @Tool("更新投递状态")
    public String updateApplicationStatus(
            @P("投递记录ID或公司名称") String identifier,
            @P("新状态：APPLIED/RESUME_SCREENING/INTERVIEWING/OFFER/REJECTED") String status
    ) {
        try {
            // 尝试精确匹配
            var applications = applicationService.fuzzySearch(identifier);

            if (applications.isEmpty()) {
                return "没有找到匹配的投递记录。请提供公司名称或职位名称。";
            }

            if (applications.size() > 1) {
                // 返回候选列表
                StringBuilder sb = new StringBuilder("找到多条匹配记录，请选择：\n");
                for (int i = 0; i < applications.size(); i++) {
                    var app = applications.get(i);
                    sb.append(String.format("%d. %s - %s (ID: %d)\n",
                            i + 1, app.getPosition(), app.getCompany().getName(), app.getId()));
                }
                return sb.toString();
            }

            // 唯一匹配，执行更新
            var app = applications.get(0);
            applicationService.updateStatus(app.getId(),
                    com.jobtracker.constants.ApplicationStatus.valueOf(status));

            return String.format("已将 %s 的投递状态更新为：%s",
                    app.getCompany().getName(), status);

        } catch (Exception e) {
            log.error("更新状态失败", e);
            return "更新失败：" + e.getMessage() + "\n请确认状态值是否正确。";
        }
    }

    @Tool("查询投递记录")
    public String queryApplications(@P("筛选条件：公司名、职位、状态等") String filters) {
        try {
            var applications = applicationService.fuzzySearch(filters);

            if (applications.isEmpty()) {
                return "没有找到匹配的投递记录。";
            }

            StringBuilder sb = new StringBuilder("找到 ").append(applications.size())
                    .append(" 条投递记录：\n\n");

            for (var app : applications) {
                sb.append(String.format("- %s | %s | 状态：%s\n",
                        app.getCompany().getName(),
                        app.getPosition(),
                        app.getStatus().getDescription()));
            }

            return sb.toString();

        } catch (Exception e) {
            log.error("查询投递失败", e);
            return "查询失败：" + e.getMessage();
        }
    }

    @Tool("获取投递统计数据")
    public String getStatistics() {
        try {
            var stats = applicationService.getStatistics();

            StringBuilder sb = new StringBuilder("📊 投递统计数据\n\n");
            sb.append("总投递数：").append(stats.getTotalApplications()).append("\n\n");
            sb.append("各状态分布：\n");

            stats.getStatusCounts().forEach((status, count) -> {
                sb.append(String.format("- %s：%d\n", status.getDescription(), count));
            });

            return sb.toString();

        } catch (Exception e) {
            log.error("获取统计失败", e);
            return "获取统计数据失败：" + e.getMessage();
        }
    }
}
```

**Step 2: 提交**

```bash
git add .
git commit -m "feat: add ApplicationTools with CRUD operations"
```

---

## Task 8: LangChain4j 工具方法 - InterviewTools 和 CompanyTools

**Files:**
- Create: `job-tracker-backend/src/main/java/com/jobtracker/agent/tools/InterviewTools.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/agent/tools/CompanyTools.java`

**Step 1: 创建 InterviewTools**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/agent/tools/InterviewTools.java`

```java
package com.jobtracker.agent.tools;

import com.jobtracker.service.InterviewService;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 面试相关工具
 */
@Slf4j
@Component
public class InterviewTools {

    @Autowired
    private InterviewService interviewService;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Tool("创建面试记录")
    public String createInterview(
            @P("投递记录ID或公司名称") String applicationIdentifier,
            @P("面试日期时间，格式：yyyy-MM-dd HH:mm") String interviewDateStr,
            @P("面试类型：VIDEO/ONSITE/PHONE") String type,
            @P("面试官姓名（可选）") String interviewer
    ) {
        try {
            // 解析日期
            LocalDateTime interviewDate = LocalDateTime.parse(interviewDateStr, DATE_FORMATTER);

            // TODO: 根据 applicationIdentifier 查找 applicationId
            // 这里简化处理，假设直接传入 applicationId
            Long applicationId = Long.parseLong(applicationIdentifier);

            var record = interviewService.createInterview(
                    applicationId, interviewDate, type, interviewer
            );

            return String.format("已创建面试记录：时间 %s，类型 %s",
                    interviewDate.format(DATE_FORMATTER), type);

        } catch (Exception e) {
            log.error("创建面试记录失败", e);
            return "创建面试记录失败：" + e.getMessage();
        }
    }

    @Tool("查询面试记录")
    public String queryInterviews(
            @P("投递记录ID") Long applicationId
    ) {
        try {
            var interviews = interviewService.getByApplicationId(applicationId);

            if (interviews.isEmpty()) {
                return "该投递暂无面试记录。";
            }

            StringBuilder sb = new StringBuilder("面试记录（共 ").append(interviews.size())
                    .append(" 条）：\n\n");

            for (var interview : interviews) {
                sb.append(String.format("- 时间：%s\n", interview.getInterviewDate()));
                sb.append(String.format("  类型：%s\n", interview.getInterviewType()));
                if (interview.getInterviewer() != null) {
                    sb.append(String.format("  面试官：%s\n", interview.getInterviewer()));
                }
                sb.append("\n");
            }

            return sb.toString();

        } catch (Exception e) {
            log.error("查询面试记录失败", e);
            return "查询失败：" + e.getMessage();
        }
    }
}
```

**Step 2: 创建 CompanyTools**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/agent/tools/CompanyTools.java`

```java
package com.jobtracker.agent.tools;

import com.jobtracker.entity.Company;
import com.jobtracker.service.CompanyService;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 公司相关工具
 */
@Slf4j
@Component
public class CompanyTools {

    @Autowired
    private CompanyService companyService;

    @Tool("创建公司信息")
    public String createCompany(
            @P("公司名称") String name,
            @P("官网URL（可选）") String website,
            @P("所属行业（可选）") String industry,
            @P("公司描述（可选）") String description
    ) {
        try {
            // 检查是否已存在
            Company existing = companyService.findByName(name);
            if (existing != null) {
                return String.format("公司 %s 已存在，无需重复创建。", name);
            }

            Company company = new Company();
            company.setName(name);
            company.setWebsite(website);
            company.setIndustry(industry);
            company.setDescription(description);

            companyService.save(company);

            return String.format("已成功创建公司：%s", name);

        } catch (Exception e) {
            log.error("创建公司失败", e);
            return "创建公司失败：" + e.getMessage();
        }
    }

    @Tool("查询公司信息")
    public String queryCompany(@P("公司名称或关键词") String keyword) {
        try {
            var companies = companyService.searchByName(keyword);

            if (companies.isEmpty()) {
                return "没有找到匹配的公司。";
            }

            StringBuilder sb = new StringBuilder("找到 ").append(companies.size())
                    .append(" 家公司：\n\n");

            for (var company : companies) {
                sb.append(String.format("- %s\n", company.getName()));
                if (company.getIndustry() != null) {
                    sb.append(String.format("  行业：%s\n", company.getIndustry()));
                }
                if (company.getWebsite() != null) {
                    sb.append(String.format("  官网：%s\n", company.getWebsite()));
                }
                sb.append("\n");
            }

            return sb.toString();

        } catch (Exception e) {
            log.error("查询公司失败", e);
            return "查询失败：" + e.getMessage();
        }
    }
}
```

**Step 3: 提交**

```bash
git add .
git commit -m "feat: add InterviewTools and CompanyTools"
```

---

## Task 9: WebSocket 实时通信

**Files:**
- Create: `job-tracker-backend/src/main/java/com/jobtracker/websocket/ChatWebSocketHandler.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/websocket/WebSocketSessionManager.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/config/WebSocketConfig.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/dto/WebSocketMessage.java`

**Step 1: 创建 WebSocket 消息 DTO**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/dto/WebSocketMessage.java`

```java
package com.jobtracker.dto;

import lombok.Data;

/**
 * WebSocket 消息
 */
@Data
public class WebSocketMessage {

    private String type;  // CHAT, CONTENT_CHUNK, VIEW_UPDATE, DONE
    private String content;
    private String sessionId;
    private Long timestamp;
    private Object data;
    private String entityType;
    private String action;
}
```

**Step 2: 创建 WebSocket 会话管理器**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/websocket/WebSocketSessionManager.java`

```java
package com.jobtracker.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话管理器
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void addSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
        log.info("WebSocket 会话已添加: {}", sessionId);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        log.info("WebSocket 会话已移除: {}", sessionId);
    }

    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
}
```

**Step 3: 创建 ChatWebSocketHandler**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/websocket/ChatWebSocketHandler.java`

```java
package com.jobtracker.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.agent.JobAgent;
import com.jobtracker.dto.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;

/**
 * 聊天 WebSocket 处理器
 */
@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private JobAgent jobAgent;

    @Autowired
    private WebSocketSessionManager sessionManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessionManager.addSession(sessionId, session);
        log.info("WebSocket 连接已建立: {}", sessionId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();

        try {
            // 解析请求消息
            WebSocketMessage request = objectMapper.readValue(
                    message.getPayload(), WebSocketMessage.class
            );

            if ("CHAT".equals(request.getType())) {
                handleChat(session, request);
            }

        } catch (Exception e) {
            log.error("处理消息失败", e);
            sendError(session, e.getMessage());
        }
    }

    /**
     * 处理聊天消息
     */
    private void handleChat(WebSocketSession session, WebSocketMessage request) {
        try {
            String userMessage = request.getMessage();
            log.info("收到用户消息: {}", userMessage);

            // 调用 AI Agent
            String response = jobAgent.chat(userMessage);

            // 发送完整回复（非流式简化版）
            WebSocketMessage responseMsg = new WebSocketMessage();
            responseMsg.setType("CONTENT_CHUNK");
            responseMsg.setContent(response);
            responseMsg.setSessionId(session.getId());
            responseMsg.setTimestamp(Instant.now().toEpochMilli());

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(responseMsg)));

            // 发送完成消息
            WebSocketMessage doneMsg = new WebSocketMessage();
            doneMsg.setType("DONE");
            doneMsg.setSessionId(session.getId());
            doneMsg.setTimestamp(Instant.now().toEpochMilli());

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(doneMsg)));

        } catch (Exception e) {
            log.error("处理聊天消息失败", e);
            sendError(session, e.getMessage());
        }
    }

    /**
     * 发送错误消息
     */
    private void sendError(WebSocketSession session, String error) {
        try {
            WebSocketMessage errorMsg = new WebSocketMessage();
            errorMsg.setType("ERROR");
            errorMsg.setContent(error);
            errorMsg.setTimestamp(Instant.now().toEpochMilli());

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMsg)));
        } catch (Exception e) {
            log.error("发送错误消息失败", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessionManager.removeSession(sessionId);
        log.info("WebSocket 连接已关闭: {}", sessionId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 传输错误", exception);
        sessionManager.removeSession(session.getId());
    }
}
```

**Step 4: 创建 WebSocket 配置**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/config/WebSocketConfig.java`

```java
package com.jobtracker.config;

import com.jobtracker.websocket.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/api/v1/chat/stream")
                .setAllowedOrigins("http://localhost:5173", "http://localhost:3000");
    }
}
```

**Step 5: 提交**

```bash
git add .
git commit -m "feat: add WebSocket support for real-time chat"
```

---

## Task 10: HTTP REST API（备用接口）

**Files:**
- Create: `job-tracker-backend/src/main/java/com/jobtracker/controller/DataController.java`
- Create: `job-tracker-backend/src/main/java/com/jobtracker/common/result/Result.java`

**Step 1: 创建统一响应类**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/common/result/Result.java`

```java
package com.jobtracker.common.result;

import lombok.Data;

/**
 * 统一响应结果
 */
@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
}
```

**Step 2: 创建数据查询控制器**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/controller/DataController.java`

```java
package com.jobtracker.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jobtracker.common.result.Result;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.service.ApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 数据查询控制器（备用 HTTP API）
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class DataController {

    @Autowired
    private ApplicationService applicationService;

    /**
     * 获取投递列表（分页）
     */
    @GetMapping("/applications")
    public Result<IPage<JobApplication>> getApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            IPage<JobApplication> applications = applicationService.getApplicationsWithCompany(page, size);
            return Result.success(applications);
        } catch (Exception e) {
            log.error("获取投递列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/dashboard/stats")
    public Result<ApplicationService.StatisticsDTO> getStatistics() {
        try {
            ApplicationService.StatisticsDTO stats = applicationService.getStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error(e.getMessage());
        }
    }
}
```

**Step 3: 提交**

```bash
git add .
git commit -m "feat: add HTTP REST API as backup interface"
```

---

## Task 11: 异常处理和日志配置

**Files:**
- Create: `job-tracker-backend/src/main/java/com/jobtracker/common/exception/GlobalExceptionHandler.java`
- Create: `job-tracker-backend/src/main/resources/logback-spring.xml`

**Step 1: 创建全局异常处理器**

创建文件: `job-tracker-backend/src/main/java/com/jobtracker/common/exception/GlobalExceptionHandler.java`

```java
package com.jobtracker.common.exception;

import com.jobtracker.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统异常：" + e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        return Result.error("运行时异常：" + e.getMessage());
    }
}
```

**Step 2: 创建 Logback 配置**

创建文件: `job-tracker-backend/src/main/resources/logback-spring.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProperty scope="context" name="APP_NAME" source="spring.application.name"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/${APP_NAME}.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/${APP_NAME}.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="com.jobtracker" level="DEBUG"/>
    <logger name="dev.langchain4j" level="DEBUG"/>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

**Step 3: 提交**

```bash
git add .
git commit -m "feat: add global exception handler and logging configuration"
```

---

## Task 12: 测试和验证

**Step 1: 启动应用**

```bash
cd job-tracker-backend
mvn clean install
mvn spring-boot:run
```

预期输出: Spring Boot 启动成功，监听 8080 端口

**Step 2: 验证 Swagger**

浏览器访问: `http://localhost:8080/swagger-ui.html`
预期: 看到 API 文档

**Step 3: 测试 HTTP API**

```bash
# 测试获取统计数据
curl http://localhost:8080/api/v1/dashboard/stats

# 测试获取投递列表
curl http://localhost:8080/api/v1/applications
```

**Step 4: 测试 WebSocket（需要前端或 WebSocket 客户端）**

使用 wscat 或类似工具:
```bash
wscat -c "ws://localhost:8080/api/v1/chat/stream"
# 发送: {"type":"CHAT","message":"帮我查询统计数据","sessionId":"test"}
```

**Step 5: 创建测试文档**

创建文件: `job-tracker-backend/TESTING.md`

```markdown
# 后端测试指南

## 前置条件

1. MySQL 已运行，数据库 `job_tracker` 已创建
2. LM Studio 已启动，Gemma3-4b 模型已加载

## 测试步骤

### 1. HTTP API 测试

```bash
# 获取统计数据
curl http://localhost:8080/api/v1/dashboard/stats

# 获取投递列表
curl http://localhost:8080/api/v1/applications?page=1&size=10
```

### 2. WebSocket 测试

使用 WebSocket 客户端连接: `ws://localhost:8080/api/v1/chat/stream`

发送消息格式:
```json
{
  "type": "CHAT",
  "message": "帮我查询统计数据",
  "sessionId": "test-session-123"
}
```

### 3. AI 对话测试示例

- "帮我查询统计数据"
- "创建一个字节跳动的后端工程师投递"
- "把腾讯的投递状态改成面试中"
```

**Step 6: 提交**

```bash
git add .
git commit -m "test: add testing documentation"
```

---

## Task 13: 生成接口文档

**Step 1: 导出 Swagger API 文档**

访问: `http://localhost:8080/swagger-ui.html`

点击 "Download" 按钮，下载 OpenAPI JSON 格式的文档。

**Step 2: 创建接口参考文档**

创建文件: `docs/API_REFERENCE.md`

```markdown
# Job Tracker 后端 API 参考文档

## WebSocket 接口

### 聊天流

**端点:** `ws://localhost:8080/api/v1/chat/stream`

**请求格式:**
```json
{
  "type": "CHAT",
  "message": "用户消息",
  "sessionId": "会话ID"
}
```

**响应格式:**
```json
{
  "type": "CONTENT_CHUNK",
  "content": "AI回复内容",
  "sessionId": "会话ID",
  "timestamp": 1709876543000
}
```

## HTTP REST API

### 获取投递列表

**端点:** `GET /api/v1/applications`

**参数:**
- page: 页码（默认 1）
- size: 每页数量（默认 10）

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

### 获取统计数据

**端点:** `GET /api/v1/dashboard/stats`

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalApplications": 50,
    "statusCounts": {
      "APPLIED": 20,
      "INTERVIEWING": 10
    }
  }
}
```

## AI 工具方法

ApplicationTools:
- createApplication: 创建投递记录
- updateApplicationStatus: 更新投递状态
- queryApplications: 查询投递记录
- getStatistics: 获取统计数据

InterviewTools:
- createInterview: 创建面试记录
- queryInterviews: 查询面试记录

CompanyTools:
- createCompany: 创建公司
- queryCompany: 查询公司
```

**Step 3: 提交**

```bash
git add .
git commit -m "docs: add API reference documentation"
```

---

## 完成检查清单

在宣布完成前，请验证：

- [ ] 项目可以正常启动（mvn spring-boot:run）
- [ ] WebSocket 连接成功
- [ ] HTTP API 可以正常调用
- [ ] Swagger 文档可以访问
- [ ] AI Agent 可以响应简单消息
- [ ] 所有测试通过
- [ ] 接口文档已生成

---

## 后续 TODO（如有需要）

如果在实现过程中遇到 LangChain4j 集成的复杂问题，创建以下 TODO 文档：

**文件:** `docs/TODO_LANGCHAIN4J.md`

```markdown
# LangChain4j 集成 TODO

## 未完成的功能

### 对话上下文压缩
- **方法名**: `ChatMemoryCompressor.compress()`
- **实现思路**:
  1. 检查消息数量是否达到阈值
  2. 提取实体信息（调用 Service 获取用户创建的所有实体）
  3. 调用 LLM 生成摘要
  4. 清空历史并插入摘要作为系统消息
- **参数**: `ChatMemory memory, String sessionId`
- **返回值**: `String summary`

### 流式响应支持
- **实现思路**: 使用 `StreamingChatModel` 的 `stream()` 方法
- **参考**: LangChain4j 官方文档 - Streaming Responses

请根据以上思路实现，或联系 AI 助手协助。
```

---

## 总结

本实现计划涵盖了：

1. ✅ 项目初始化和依赖配置
2. ✅ 数据库设计和初始化
3. ✅ 实体类、Mapper、Service 层
4. ✅ LangChain4j 集成和 AI 工具方法
5. ✅ WebSocket 实时通信
6. ✅ HTTP REST API（备用）
7. ✅ 异常处理和日志
8. ✅ 测试验证
9. ✅ 接口文档生成

**预计总耗时**: 约 27-35 小时

**关键风险点**:
- LangChain4j 与 LM Studio 的兼容性
- Function Calling 的准确性
- 对话上下文管理的复杂度

**降级方案**: 如果 LangChain4j 集成遇到问题，可以：
1. 使用传统 HTTP API
2. 生成 TODO 文档供后续实现
3. 简化对话上下文管理
