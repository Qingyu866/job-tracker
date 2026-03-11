# 数据库初始化指南

## 概述
本文档说明如何初始化 Job Tracker 项目的 MySQL 数据库。

## 前置条件
1. 安装 MySQL 8.0+
2. 确保 MySQL 服务正在运行
3. 具有 MySQL root 用户权限

## 方式 1: 使用 MySQL 命令行工具（推荐）

### 1.1 创建数据库
```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS job_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 1.2 执行初始化脚本
```bash
cd /Users/qingyu/job-tracker/job-tracker-backend
mysql -u root -p job_tracker < src/main/resources/db/migration/V1__init_schema.sql
```

### 1.3 验证表创建
```bash
mysql -u root -p -e "USE job_tracker; SHOW TABLES;"
```

预期输出：
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

## 方式 2: 使用 MySQL Workbench 或其他 GUI 工具

1. 打开 MySQL Workbench
2. 连接到本地 MySQL 服务器
3. 执行以下 SQL：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS job_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE job_tracker;

-- 然后执行 src/main/resources/db/migration/V1__init_schema.sql 的全部内容
```

## 方式 3: 使用 Docker（如果本地没有安装 MySQL）

### 3.1 启动 MySQL 容器
```bash
docker run --name job-tracker-mysql \
  -e MYSQL_ROOT_PASSWORD=your_password \
  -e MYSQL_DATABASE=job_tracker \
  -p 3306:3306 \
  -d mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

### 3.2 等待容器启动
```bash
docker exec -it job-tracker-mysql mysql -u root -p
# 输入密码: your_password
```

### 3.3 在容器内执行初始化脚本
```bash
docker exec -i job-tracker-mysql mysql -u root -pyour_password job_tracker < src/main/resources/db/migration/V1__init_schema.sql
```

## 数据库表结构说明

### 1. companies (公司信息表)
存储公司基本信息，包括名称、行业、规模、地址等。

**主要字段：**
- id: 公司ID（主键）
- name: 公司名称
- industry: 行业类型
- size: 公司规模
- location: 公司地址
- website: 公司官网
- description: 公司描述

### 2. job_applications (求职申请表)
存储求职申请记录，关联公司信息。

**主要字段：**
- id: 申请ID（主键）
- company_id: 公司ID（外键）
- job_title: 职位名称
- job_description: 职位描述
- job_type: 工作类型（全职/兼职/实习/合同）
- work_location: 工作地点
- salary_min/salary_max: 薪资范围
- status: 申请状态（WISHLIST/APPLIED/INTERVIEW/OFFER/REJECTED/WITHDRAWN）
- application_date: 申请日期
- priority: 优先级（1-10）

### 3. interview_records (面试记录表)
存储面试记录，关联求职申请。

**主要字段：**
- id: 面试记录ID（主键）
- application_id: 申请ID（外键）
- interview_type: 面试类型（PHONE/VIDEO/ONSITE/TECHNICAL/HR）
- interview_date: 面试时间
- interviewer_name: 面试官姓名
- interviewer_title: 面试官职位
- duration_minutes: 面试时长
- status: 面试状态（SCHEDULED/COMPLETED/CANCELLED/NO_SHOW）
- rating: 面试评分（1-5分）
- feedback: 面试反馈
- technical_questions: 技术问题记录

### 4. application_logs (申请日志表)
存储申请过程中的日志记录。

**主要字段：**
- id: 日志ID（主键）
- application_id: 申请ID（外键）
- log_type: 日志类型（STATUS_CHANGE/INTERVIEW_SCHEDULED/FEEDBACK_RECEIVED/NOTE_ADDED/DOCUMENT_UPLOADED）
- log_title: 日志标题
- log_content: 日志内容
- logged_by: 记录者（SYSTEM/USER）
- created_at: 创建时间

## 关系图

```
companies (1) ----< (N) job_applications (1) ----< (N) interview_records
                                    |
                                    |
                                    ----< (N) application_logs
```

## 验证安装

### 检查所有表是否创建成功
```sql
USE job_tracker;
SHOW TABLES;
```

### 检查表结构
```sql
DESC companies;
DESC job_applications;
DESC interview_records;
DESC application_logs;
```

### 检查外键关系
```sql
SELECT
  TABLE_NAME,
  COLUMN_NAME,
  CONSTRAINT_NAME,
  REFERENCED_TABLE_NAME,
  REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'job_tracker'
  AND REFERENCED_TABLE_NAME IS NOT NULL;
```

### 检查示例数据
```sql
-- 查看示例公司
SELECT * FROM companies;

-- 统计表记录数
SELECT
  'companies' AS table_name,
  COUNT(*) AS row_count
FROM companies
UNION ALL
SELECT
  'job_applications' AS table_name,
  COUNT(*) AS row_count
FROM job_applications
UNION ALL
SELECT
  'interview_records' AS table_name,
  COUNT(*) AS row_count
FROM interview_records
UNION ALL
SELECT
  'application_logs' AS table_name,
  COUNT(*) AS row_count
FROM application_logs;
```

## 常见问题

### Q1: MySQL 命令行工具找不到
**A:** 确保已安装 MySQL 并将其添加到系统 PATH 中：
- macOS (Homebrew): `brew install mysql`
- macOS (DMG): 下载官方安装包
- 添加到 PATH: `export PATH=$PATH:/usr/local/mysql/bin`

### Q2: 连接失败
**A:** 检查以下几点：
1. MySQL 服务是否运行
2. 用户名和密码是否正确
3. 端口 3306 是否被占用
4. 防火墙是否允许连接

### Q3: 字符集问题
**A:** 确保数据库和表使用 utf8mb4 字符集：
```sql
ALTER DATABASE job_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 下一步

数据库初始化完成后，继续执行：
- Task 3: 实体类和枚举定义
- Task 4: Mapper 层
- Task 5: Service 层

## 注意事项

1. 生产环境请修改 root 密码
2. 建议创建专用数据库用户
3. 定期备份数据库
4. 使用 Flyway 进行版本管理（后续集成）
