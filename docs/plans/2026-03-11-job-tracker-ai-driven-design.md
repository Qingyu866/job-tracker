# 求职追踪应用 - AI 对话驱动架构设计文档

**文档版本**: 1.0
**创建日期**: 2026-03-11
**设计者**: Claude (AI Assistant)
**架构方案**: 方案 A - 对话驱动架构

---

## 📋 目录

1. [概述](#1-概述)
2. [整体架构](#2-整体架构)
3. [后端架构设计](#3-后端架构设计)
4. [LangChain4j 集成](#4-langchain4j-集成)
5. [WebSocket 通信](#5-websocket-通信)
6. [错误处理与降级](#6-错误处理与降级)
7. [对话上下文压缩](#7-对话上下文压缩)
8. [API 接口定义](#8-api-接口定义)
9. [数据库设计](#9-数据库设计)
10. [实施计划](#10-实施计划)

---

## 1. 概述

### 1.1 项目定位

求职追踪应用 - 一个以 AI 对话为主导交互方式的应用，帮助用户管理和追踪求职进度。

### 1.2 核心特性

- 🤖 **AI 对话驱动**: 90% 操作通过自然语言完成
- 📝 **Notion 风格界面**: 左侧多视图展示，右侧 AI 对话面板
- 🔄 **实时响应**: WebSocket 流式对话和视图更新
- 🧠 **上下文记忆**: AI 记住完整对话历史，支持引用
- 📦 **上下文压缩**: 自动压缩长对话，保留关键信息

### 1.3 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| **前端** | React + Vite | 18+ |
| **后端** | Spring Boot | 3.2+ |
| **AI 框架** | LangChain4j | 1.12.1 |
| **数据库** | MySQL | 8.0+ |
| **AI 模型** | Gemma3-4b | via LM Studio |
| **通信** | WebSocket | Spring WebSocket |

---

## 2. 整体架构

### 2.1 系统架构图

```
┌─────────────────────────────────────────────────────────┐
│                   前端层 (React)                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  多视图展示  │  │  AI 对话面板 │  │  快捷操作栏  │  │
│  │  表格/时间线 │  │  WebSocket   │  │  高频操作    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           ↕ WebSocket
┌─────────────────────────────────────────────────────────┐
│              后端层 (Spring Boot + LangChain4j)          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Chat Handler │  │ AI Agent     │  │ Service 层   │  │
│  │              │  │              │  │              │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│       ↓                   ↓                   ↓          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ChatMemory    │  │ ToolRegistry │  │ MyBatis Plus │  │
│  │+ Compressor  │  │ (@Tool)      │  │              │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           ↕
┌─────────────────────────────────────────────────────────┐
│              AI 层 (LM Studio + Gemma3-4b)               │
│           http://localhost:1234/v1                      │
└─────────────────────────────────────────────────────────┘
                           ↕
┌─────────────────────────────────────────────────────────┐
│                 数据层 (MySQL 8.0+)                      │
│  companies ─ job_applications ─ interview_records        │
└─────────────────────────────────────────────────────────┘
```

### 2.2 交互流程

```
用户输入 "帮我记录字节跳动的投递"
    ↓
前端 WebSocket 发送消息
    ↓
后端 ChatHandler 接收
    ↓
AI Agent 分析意图 → LangChain4j
    ↓
Function Calling 匹配工具 → ApplicationTools.createApplication()
    ↓
执行业务逻辑 → Service 层 → 数据库
    ↓
返回结果 → AI 生成自然语言回复
    ↓
WebSocket 流式推送到前端
    ↓
前端更新对话面板 + 发送 VIEW_UPDATE 事件刷新数据视图
```

---

## 3. 后端架构设计

### 3.1 项目结构

```
job-tracker-backend/
├── pom.xml                                           # Maven 配置
├── src/main/java/com/jobtracker/
│   ├── JobTrackerApplication.java                    # 启动类
│   │
│   ├── config/                                       # 配置类
│   │   ├── LangChain4jConfig.java                    # LangChain4j 配置
│   │   ├── WebSocketConfig.java                      # WebSocket 配置
│   │   ├── ChatMemoryConfig.java                     # 对话历史配置
│   │   ├── MybatisPlusConfig.java                    # MyBatis Plus 配置
│   │   └── SwaggerConfig.java                        # Swagger 配置
│   │
│   ├── agent/                                        # AI Agent 层（核心）
│   │   ├── JobAgent.java                             # 求职助手 Agent
│   │   ├── tools/                                    # AI 工具包
│   │   │   ├── ApplicationTools.java                 # 投递相关工具
│   │   │   ├── InterviewTools.java                   # 面试相关工具
│   │   │   ├── CompanyTools.java                     # 公司相关工具
│   │   │   └── DashboardTools.java                   # 统计相关工具
│   │   └── prompt/                                   # Prompt 模板
│   │       └── SystemPromptTemplate.java
│   │
│   ├── websocket/                                    # WebSocket 处理
│   │   ├── ChatWebSocketHandler.java                 # 对话处理器
│   │   └── WebSocketSessionManager.java              # 会话管理
│   │
│   ├── controller/                                   # 控制器层（HTTP API）
│   │   └── DataController.java                       # 数据查询接口
│   │
│   ├── service/                                      # 业务服务层
│   │   ├── ApplicationService.java
│   │   ├── InterviewService.java
│   │   ├── CompanyService.java
│   │   └── DashboardService.java
│   │
│   ├── mapper/                                       # 数据访问层
│   │   ├── ApplicationMapper.java
│   │   ├── InterviewMapper.java
│   │   ├── CompanyMapper.java
│   │   └── ApplicationLogMapper.java
│   │
│   ├── entity/                                       # 实体类
│   │   ├── JobApplication.java
│   │   ├── InterviewRecord.java
│   │   ├── Company.java
│   │   └── ApplicationLog.java
│   │
│   ├── dto/                                          # 数据传输对象
│   │   ├── request/
│   │   └── response/
│   │
│   └── common/                                       # 通用组件
│       ├── result/                                   # 统一响应
│       │   └── Result.java
│       ├── exception/                                # 异常处理
│       │   └── GlobalExceptionHandler.java
│       └── constants/                                # 常量定义
│           └── ApplicationStatus.java
│
└── src/main/resources/
    ├── application.yml                               # 应用配置
    ├── prompts/                                      # Prompt 模板文件
    │   └── system-prompt.txt
    └── db/migration/                                 # 数据库脚本
        └── V1__init_schema.sql
```

### 3.2 Maven 依赖

```xml
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
        <version>1.12.1</version>
    </dependency>

    <!-- MyBatis Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        <version>3.5.5</version>
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
</dependencies>
```

### 3.3 核心配置类

#### LangChain4jConfig.java

```java
@Configuration
public class LangChain4jConfig {

    @Bean
    public StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
            .baseUrl("${ai.api.base-url:http://localhost:1234/v1}")
            .modelName("${ai.model.name:gemma-3-4b-it}")
            .temperature(0.7)
            .timeout(Duration.ofSeconds(60))
            .build();
    }

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
            .maxMessages(20)
            .id("user-session")
            .build();
    }

    @Bean
    public Assistant jobAssistant(
        StreamingChatModel model,
        ChatMemory memory,
        List<Object> tools
    ) {
        return AiServices.builder(Assistant.class)
            .streamingChatModel(model)
            .chatMemory(memory)
            .tools(tools)
            .build();
    }
}
```

---

## 4. LangChain4j 集成

### 4.1 AI 工具方法定义

#### ApplicationTools.java

```java
@Component
public class ApplicationTools {

    @Autowired
    private ApplicationService applicationService;

    @Tool("创建新的投递记录")
    String createApplication(
        @P("公司名称") String companyName,
        @P("职位名称") String position,
        @P("职位描述（可选）") String description,
        @P("最低薪资（可选）") BigDecimal salaryMin,
        @P("最高薪资（可选）") BigDecimal salaryMax,
        @P("工作地点（可选）") String location
    );

    @Tool("更新投递状态")
    String updateApplicationStatus(
        @P("投递记录ID或公司名称") String identifier,
        @P("新状态：APPLIED/RESUME_SCREENING/INTERVIEWING/OFFER/REJECTED") String status
    );

    @Tool("查询投递记录")
    String queryApplications(
        @P("筛选条件：公司名、职位、状态等") String filters
    );

    @Tool("删除投递记录")
    String deleteApplication(
        @P("投递记录ID或公司名称") String identifier
    );

    @Tool("获取投递统计数据")
    String getStatistics();
}
```

#### InterviewTools.java

```java
@Component
public class InterviewTools {

    @Tool("创建面试记录")
    String createInterview(
        @P("投递记录ID或公司名称") String applicationIdentifier,
        @P("面试日期时间，格式：yyyy-MM-dd HH:mm") String interviewDate,
        @P("面试类型：VIDEO/ONSITE/PHONE") String type,
        @P("面试官姓名（可选）") String interviewer
    );

    @Tool("更新面试记录")
    String updateInterview(
        @P("面试记录ID") Long interviewId,
        @P("面试问题") String questions,
        @P("回答记录") String answers,
        @P("面试反馈") String feedback
    );

    @Tool("查询面试记录")
    String queryInterviews(
        @P("投递记录ID或公司名称") String applicationIdentifier
    );
}
```

### 4.2 System Prompt

```java
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
        - INTERVIEWING: 遨试中
        - OFFER: 已获得offer
        - REJECTED: 已拒绝

        重要提醒：
        - 创建投递时，如果公司不存在，先询问是否创建新公司
        - 更新状态时，如果记录有多条匹配，列出选项让用户选择
        - 始终使用工具方法操作数据，不要编造信息
        """;
}
```

---

## 5. WebSocket 通信

### 5.1 配置

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler(), "/api/v1/chat/stream")
                .setAllowedOrigins("http://localhost:5173");
    }

    @Bean
    public WebSocketHandler chatWebSocketHandler() {
        return new ChatWebSocketHandler();
    }
}
```

### 5.2 消息协议

**请求格式：**
```json
{
  "type": "CHAT",
  "message": "帮我记录字节跳动的投递",
  "sessionId": "user-123"
}
```

**响应格式：**

1. 内容块（流式）
```json
{
  "type": "CONTENT_CHUNK",
  "content": "已为你创建投递记录",
  "sessionId": "user-123",
  "timestamp": 1709876543000
}
```

2. 视图更新事件
```json
{
  "type": "VIEW_UPDATE",
  "entityType": "APPLICATION",
  "action": "CREATE",
  "data": { "id": 1, "company": "字节跳动" },
  "timestamp": 1709876543000
}
```

3. 完成
```json
{
  "type": "DONE",
  "sessionId": "user-123",
  "timestamp": 1709876543000
}
```

---

## 6. 错误处理与降级

### 6.1 降级触发条件

| 场景 | 触发条件 | 降级方案 |
|------|----------|----------|
| LM Studio 连接失败 | 连续 3 次超时 | 提示用户检查 LM Studio |
| Function Calling 失败 | AI 无法正确提取参数 | 询问用户提供明确信息 |
| 对话历史管理复杂 | 实现超出预估时间 2x | 生成 TODO，使用简化版本 |

### 6.2 保留的传统 API

即使 AI 失效，以下 HTTP API 依然可用：

| API | 方法 | 功能 |
|-----|------|------|
| `/api/v1/applications` | GET/POST | 查询/创建投递 |
| `/api/v1/applications/{id}` | PUT/DELETE | 更新/删除投递 |
| `/api/v1/companies` | GET/POST | 查询/创建公司 |
| `/api/v1/dashboard/stats` | GET | 获取统计数据 |

---

## 7. 对话上下文压缩

### 7.1 压缩策略

```
消息数量达到 15 条 → 触发压缩 → 提取关键信息 → 生成摘要 → 替换历史消息
```

### 7.2 压缩实现

```java
@Component
public class ChatMemoryCompressor {

    private static final int MAX_MESSAGES = 20;
    private static final int COMPRESS_THRESHOLD = 15;

    public boolean shouldCompress(ChatMemory memory) {
        return memory.messages().size() >= COMPRESS_THRESHOLD;
    }

    public String compress(ChatMemory memory, String sessionId) {
        // 1. 提取实体信息
        String entities = extractEntitiesFromMemory(memory);

        // 2. 提取最近意图
        String recentIntent = extractRecentIntent(memory);

        // 3. 调用 LLM 生成摘要
        String compressionPrompt = String.format("""
            请将以下对话历史压缩为简洁的摘要，保留：
            1. 用户创建的所有实体信息（公司、职位、面试等）
            2. 重要的决策和偏好
            3. 当前的对话上下文

            已提取的实体信息：
            %s

            对话历史：
            %s
            """, entities, formatMessages(memory.messages()));

        return chatModel.generate(compressionPrompt);
    }

    public void compressMemory(ChatMemory memory, String sessionId) {
        String summary = compress(memory, sessionId);
        memory.clear();
        memory.add(SystemMessage.from("【对话历史摘要】\n" + summary));
    }
}
```

---

## 8. API 接口定义

### 8.1 WebSocket 接口

| 接口 | 路径 | 说明 |
|------|------|------|
| 对话流 | `/api/v1/chat/stream` | WebSocket 实时对话 |

### 8.2 HTTP REST API（备用）

#### 投递管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/applications` | 获取投递列表 |
| GET | `/api/v1/applications/{id}` | 获取投递详情 |
| POST | `/api/v1/applications` | 创建投递 |
| PUT | `/api/v1/applications/{id}` | 更新投递 |
| DELETE | `/api/v1/applications/{id}` | 删除投递 |

#### 统计分析

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/dashboard/stats` | 获取统计数据 |

---

## 9. 数据库设计

### 9.1 表结构

详见原始设计文档 `DESIGN_job-tracker.md`，包含 4 张表：
- `companies` - 公司信息
- `job_applications` - 投递记录
- `interview_records` - 面试记录
- `application_logs` - 状态日志

---

## 10. 实施计划

### 10.1 开发阶段

1. **阶段 1：环境准备**（3小时）
   - 数据库初始化
   - Spring Boot 项目搭建

2. **阶段 2：LangChain4j 集成**（5小时）
   - 配置 LangChain4j
   - 实现工具方法
   - AI Agent 开发

3. **阶段 3：WebSocket 通信**（4小时）
   - WebSocket 配置
   - 消息协议实现

4. **阶段 4：业务逻辑**（8小时）
   - Service 层开发
   - Mapper 层开发

5. **阶段 5：上下文压缩**（3小时）
   - 压缩算法实现
   - 质量验证

6. **阶段 6：测试与文档**（4小时）
   - 接口测试
   - Swagger 文档生成

### 10.2 接口文档

完成开发后，Swagger UI 地址：
```
http://localhost:8080/swagger-ui.html
```

完整的接口文档将保存到：
```
docs/API_REFERENCE.md
```

---

**文档状态**: ✅ 设计完成，等待实现
**下一步**: 调用 writing-plans skill 创建详细实现计划
