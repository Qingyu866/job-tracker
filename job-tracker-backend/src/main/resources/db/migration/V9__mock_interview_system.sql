-- V9__mock_interview_system.sql
-- 模拟面试系统数据库设计
-- 创建日期: 2026-03-16
-- 说明：整合模拟面试功能到现有 Job Tracker 系统

-- ================================
-- 1. 技能标签表（知识点库）
-- ================================
CREATE TABLE IF NOT EXISTS skill_tags (
    skill_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '技能ID',
    skill_name VARCHAR(50) NOT NULL UNIQUE COMMENT '技能名称：Java, HashMap, Spring Boot等',
    category VARCHAR(30) NOT NULL COMMENT '分类：LANGUAGE/FRAMEWORK/DATABASE/TOOL/ALGORITHM',
    parent_id BIGINT COMMENT '父技能ID（支持层级结构：Java → Collection → HashMap）',
    description VARCHAR(200) COMMENT '技能描述',
    difficulty_base INT DEFAULT 1 COMMENT '基础难度：1-5',
    hot_score INT DEFAULT 0 COMMENT '热度分数（根据使用频率更新）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_parent (parent_id),
    INDEX idx_name (skill_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能标签表';

-- 初始化技能标签数据
INSERT INTO skill_tags (skill_name, category, description, difficulty_base) VALUES
-- 编程语言
('Java', 'LANGUAGE', 'Java 编程语言', 2),
('Python', 'LANGUAGE', 'Python 编程语言', 2),
('JavaScript', 'LANGUAGE', 'JavaScript 编程语言', 2),
('TypeScript', 'LANGUAGE', 'TypeScript 编程语言', 2),
('Go', 'LANGUAGE', 'Go 编程语言', 2),

-- 框架
('Spring Boot', 'FRAMEWORK', 'Spring Boot 框架', 3),
('Spring Cloud', 'FRAMEWORK', 'Spring Cloud 微服务框架', 4),
('Vue.js', 'FRAMEWORK', 'Vue.js 前端框架', 2),
('React', 'FRAMEWORK', 'React 前端框架', 3),

-- 数据库
('MySQL', 'DATABASE', 'MySQL 数据库', 2),
('PostgreSQL', 'DATABASE', 'PostgreSQL 数据库', 2),
('Redis', 'DATABASE', 'Redis 缓存数据库', 3),
('MongoDB', 'DATABASE', 'MongoDB 文档数据库', 2),

-- 工具
('Git', 'TOOL', 'Git 版本控制', 1),
('Docker', 'TOOL', 'Docker 容器技术', 3),
('Kubernetes', 'TOOL', 'Kubernetes 容器编排', 4),

-- 算法与数据结构
('Array', 'ALGORITHM', '数组数据结构', 1),
('LinkedList', 'ALGORITHM', '链表数据结构', 2),
('HashMap', 'ALGORITHM', 'HashMap 哈希表', 3),
('BinaryTree', 'ALGORITHM', '二叉树', 3),
('Sorting', 'ALGORITHM', '排序算法', 3),
('DynamicProgramming', 'ALGORITHM', '动态规划', 4);

-- ================================
-- 2. 用户简历表（极简版）
-- ================================
CREATE TABLE IF NOT EXISTS user_resumes (
    resume_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '简历ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    resume_name VARCHAR(100) COMMENT '简历名称（如"Java后端-3年经验"）',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否为默认简历',

    -- 工作信息（影响面试问题难度）
    work_years INT COMMENT '工作年限（年）- 影响：3年问JVM，1年问基础',
    current_position VARCHAR(100) COMMENT '当前职位 - 如：Java后端工程师',
    target_level VARCHAR(20) DEFAULT 'MIDDLE' COMMENT '目标岗位级别：JUNIOR/MIDDLE/SENIOR',

    -- 自我介绍（AI 用于了解技术背景）
    summary TEXT COMMENT '自我介绍（突出技术栈、项目经验，如"3年Java后端，熟悉Spring Cloud..."）',

    -- 原始文件（用于 AI 解析）
    original_file_url VARCHAR(255) COMMENT '原始简历文件URL',
    original_file_type VARCHAR(20) COMMENT '文件类型：PDF/DOCX/IMG',

    -- 解析状态
    parse_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '解析状态：PENDING/PARSING/SUCCESS/FAILED',
    parsed_at DATETIME COMMENT '解析完成时间',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_user_id (user_id),
    INDEX idx_is_default (user_id, is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户简历表（极简版，仅保留面试必需）';

-- ================================
-- 3. 简历项目经历表
-- ================================
CREATE TABLE IF NOT EXISTS resume_projects (
    project_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    role VARCHAR(50) COMMENT '担任角色',
    start_date DATE COMMENT '开始时间',
    end_date DATE COMMENT '结束时间（NULL表示至今）',
    is_ongoing BOOLEAN DEFAULT FALSE COMMENT '是否进行中',

    -- 项目描述
    description TEXT COMMENT '项目描述',
    responsibilities TEXT COMMENT '职责描述',
    achievements TEXT COMMENT '项目成就',

    -- 技术栈（重要！用于生成针对性问题）
    tech_stack JSON COMMENT '技术栈：["Java", "Spring Boot", "MySQL"]',

    -- 项目指标（重要！用于深入挖掘）
    project_scale VARCHAR(50) COMMENT '项目规模：团队人数',
    performance_metrics JSON COMMENT '性能指标：{"qps": "10000", "response_time": "50ms"}',

    display_order INT DEFAULT 0 COMMENT '显示顺序',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (resume_id) REFERENCES user_resumes(resume_id) ON DELETE CASCADE,
    INDEX idx_resume_id (resume_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历项目经历表';

-- ================================
-- 4. 简历技能表
-- ================================
CREATE TABLE IF NOT EXISTS resume_skills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    skill_id BIGINT NOT NULL COMMENT '技能ID（关联 skill_tags）',
    proficiency_level VARCHAR(20) COMMENT '熟练度：BEGINNER/INTERMEDIATE/ADVANCED/EXPERT',
    experience_years DECIMAL(3,1) COMMENT '使用年限（年）',
    last_used_date DATE COMMENT '最后使用时间',
    is_core_skill BOOLEAN DEFAULT FALSE COMMENT '是否为核心技能',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (resume_id) REFERENCES user_resumes(resume_id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skill_tags(skill_id),
    UNIQUE KEY uk_resume_skill (resume_id, skill_id),
    INDEX idx_resume_id (resume_id),
    INDEX idx_proficiency (proficiency_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历技能表';

-- ================================
-- 5. 简历工作经历表
-- ================================
CREATE TABLE IF NOT EXISTS resume_work_experiences (
    experience_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工作经历ID',
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    company_name VARCHAR(100) NOT NULL COMMENT '公司名称',
    position VARCHAR(100) COMMENT '职位',
    start_date DATE NOT NULL COMMENT '开始时间',
    end_date DATE COMMENT '结束时间',
    is_current BOOLEAN DEFAULT FALSE COMMENT '是否为当前公司',

    -- 工作描述
    description TEXT COMMENT '工作描述',
    achievements TEXT COMMENT '工作成就',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (resume_id) REFERENCES user_resumes(resume_id) ON DELETE CASCADE,
    INDEX idx_resume_id (resume_id),
    INDEX idx_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历工作经历表';

-- ================================
-- 6. 申请-技能关联表
-- ================================
CREATE TABLE IF NOT EXISTS application_skill_mapping (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT NOT NULL COMMENT '申请ID',
    skill_id BIGINT NOT NULL COMMENT '技能ID',
    priority TINYINT DEFAULT 1 COMMENT '优先级：1-核心, 2-重要, 3-了解',
    source VARCHAR(20) DEFAULT 'MANUAL' COMMENT '来源：MANUAL/JD_AI/PARSED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_app_skill (application_id, skill_id),
    FOREIGN KEY (application_id) REFERENCES job_applications(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skill_tags(skill_id),
    INDEX idx_application (application_id),
    INDEX idx_skill (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申请-技能关联表';

-- ================================
-- 7. 修改 job_applications 表（添加模拟面试相关字段）
-- ================================
-- 检查并添加字段（如果不存在）
ALTER TABLE job_applications
ADD COLUMN IF NOT EXISTS resume_id BIGINT COMMENT '关联的简历ID',
ADD COLUMN IF NOT EXISTS resume_snapshot JSON COMMENT '简历快照（面试时的简历状态）',
ADD COLUMN IF NOT EXISTS skills_required JSON COMMENT '岗位技能要求（从JD解析或手动添加）',
ADD COLUMN IF NOT EXISTS seniority_level VARCHAR(20) DEFAULT 'MIDDLE' COMMENT '岗位级别：JUNIOR/MIDDLE/SENIOR/LEAD',
ADD COLUMN IF NOT EXISTS interview_prepared BOOLEAN DEFAULT FALSE COMMENT '是否已准备模拟面试',
ADD COLUMN IF NOT EXISTS mock_interview_count INT DEFAULT 0 COMMENT '模拟面试次数',
ADD COLUMN IF NOT EXISTS best_mock_score DECIMAL(4,1) COMMENT '最佳模拟面试成绩';

-- 添加索引
ALTER TABLE job_applications
ADD INDEX IF NOT EXISTS idx_resume_id (resume_id);

-- ================================
-- 8. 模拟面试会话表
-- ================================
CREATE TABLE IF NOT EXISTS mock_interview_sessions (
    session_id VARCHAR(64) PRIMARY KEY COMMENT '会话ID',
    application_id BIGINT NOT NULL COMMENT '关联的求职申请ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    resume_id BIGINT COMMENT '关联的简历ID',

    -- 岗位信息（冗余，便于查询）
    company_id BIGINT COMMENT '公司ID',
    job_title VARCHAR(100) COMMENT '岗位名称',
    seniority_level VARCHAR(20) COMMENT '岗位级别',

    -- 快照（重要！避免后续更新影响历史记录）
    resume_snapshot JSON COMMENT '简历快照',
    jd_snapshot JSON COMMENT 'JD 技能快照',
    skills_snapshot JSON COMMENT '技能标签快照（存档用）',

    -- 面试状态
    state VARCHAR(20) DEFAULT 'INIT' COMMENT '当前状态：INIT/WELCOME/TECHNICAL_QA/HR_QA/GENERATING_REPORT/FINISHED',
    current_round INT DEFAULT 0 COMMENT '当前轮次',
    total_rounds INT DEFAULT 5 COMMENT '总轮次（可调整）',

    -- 面试进度
    skills_covered JSON COMMENT '已考察的知识点',
    skills_pending JSON COMMENT '待考察的知识点',
    projects_discussed JSON COMMENT '已讨论的项目',

    -- 评分结果
    total_score DECIMAL(4,1) COMMENT '最终总分',
    feedback_summary TEXT COMMENT '面试总结',
    improvement_suggestions TEXT COMMENT '改进建议',

    -- 简历真实性分析
    resume_credibility_score DECIMAL(3,2) COMMENT '简历可信度评分（0-1）',
    resume_gap_analysis JSON COMMENT '简历差距分析',

    -- 时间
    started_at DATETIME COMMENT '开始时间',
    finished_at DATETIME COMMENT '结束时间',
    duration_seconds INT COMMENT '持续时间（秒）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (application_id) REFERENCES job_applications(id) ON DELETE CASCADE,
    INDEX idx_application (application_id),
    INDEX idx_user (user_id),
    INDEX idx_resume (resume_id),
    INDEX idx_state (state),
    INDEX idx_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟面试会话表';

-- ================================
-- 9. 面试对话记录表
-- ================================
CREATE TABLE IF NOT EXISTS mock_interview_messages (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    round_number INT NOT NULL COMMENT '轮次号',
    sequence_in_round INT NOT NULL COMMENT '轮内序号（1=问题，2=回答，3=追问...）',
    role VARCHAR(20) NOT NULL COMMENT '角色：ASSISTANT/USER',
    content TEXT NOT NULL COMMENT '消息内容',
    skill_id BIGINT COMMENT '关联的知识点ID',
    skill_name VARCHAR(50) COMMENT '知识点名称（冗余，便于查询）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES mock_interview_sessions(session_id) ON DELETE CASCADE,
    INDEX idx_session_round (session_id, round_number, sequence_in_round),
    INDEX idx_skill (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟面试对话记录表';

-- ================================
-- 10. 面试评分记录表
-- ================================
CREATE TABLE IF NOT EXISTS mock_interview_evaluations (
    evaluation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    round_number INT NOT NULL COMMENT '轮次号',
    skill_id BIGINT NOT NULL COMMENT '知识点ID',
    skill_name VARCHAR(50) COMMENT '知识点名称（冗余，便于查询）',
    question_text TEXT COMMENT '问题内容（AI生成的问题）',
    user_answer TEXT COMMENT '用户回答',

    -- 评分明细
    technical_score DECIMAL(3,1) COMMENT '技术准确性得分（0-4）',
    logic_score DECIMAL(3,1) COMMENT '逻辑清晰度得分（0-3）',
    depth_score DECIMAL(3,1) COMMENT '深度与广度得分（0-3）',
    total_score DECIMAL(3,1) COMMENT '总分（0-10）',

    feedback TEXT COMMENT '详细反馈',
    suggestion TEXT COMMENT '改进建议',
    keywords_matched JSON COMMENT '匹配的关键词',
    keywords_missing JSON COMMENT '缺失的关键词',

    evaluated_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',

    FOREIGN KEY (session_id) REFERENCES mock_interview_sessions(session_id) ON DELETE CASCADE,
    INDEX idx_session (session_id),
    INDEX idx_round (session_id, round_number),
    INDEX idx_skill (skill_id),
    INDEX idx_score (total_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟面试评分记录表';
