# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Job Tracker is an AI-powered job application tracking system with a modern Notion-style UI. It helps users manage job applications, interviews, and company information through both traditional UI and natural language AI chat.

**Architecture**: Monorepo with separate backend (Spring Boot) and frontend (React/TypeScript)
**AI Integration**: LangChain4j with local LLM support (LM Studio)
**Real-time**: WebSocket-based chat with persistent history

---

## Common Development Commands

### Backend (Java/Spring Boot)

```bash
# Navigate to backend
cd job-tracker-backend

# Build and run
mvn clean compile          # Compile project
mvn spring-boot:run        # Start development server (port 8080)

# Testing
mvn test                   # Run all tests
mvn test -Dtest=TestName   # Run specific test

# Database migrations
mysql -u root -p job_tracker < src/main/resources/db/migration/V1__init_schema.sql
```

**Backend runs on**: `http://localhost:8080/api`
**Context path**: `/api`

### Frontend (React/TypeScript/Vite)

```bash
# Navigate to frontend
cd frontend

# Development
npm install                # Install dependencies
npm run dev                # Start dev server (port 5173)
npm run build              # Production build
npm run lint               # Run ESLint

# Preview production build
npm run preview
```

**Frontend dev server**: `http://localhost:5173`

### Database Setup

```bash
# Create database
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS job_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Run migrations (in order)
mysql -u root -p job_tracker < src/main/resources/db/migration/V1__init_schema.sql
mysql -u root -p job_tracker < src/main/resources/db/migration/V2__chat_history.sql
mysql -u root -p job_tracker < src/main/resources/db/migration/V5__status_flow_enhancement.sql
```

---

## High-Level Architecture

### Backend Structure

```
src/main/java/com/jobtracker/
├── agent/              # AI Agent system (LangChain4j)
│   ├── tools/         # AI tools (keyword-first design)
│   │   ├── command/   # Create/update/delete operations
│   │   ├── query/     # Read operations
│   │   ├── shared/    # Common utilities (ToolHelper, ToolResult)
│   │   └── statistics/# Statistics tools
│   ├── memory/        # Chat memory providers
│   │   └── SafeTurnBasedChatMemoryProvider.java
│   ├── JobAgent.java  # AI service interface
│   └── JobAgentFactory.java  # Dynamic agent creation (session isolation)
├── config/            # Spring configuration
├── constants/         # Enums (ApplicationStatus, InterviewStatus)
├── controller/        # REST endpoints
├── dto/              # Data Transfer Objects
├── entity/           # JPA/MyBatis entities
├── mapper/           # MyBatis Plus mappers
├── service/          # Business logic
└── websocket/        # WebSocket handlers (chat)
```

### Frontend Structure

```
src/
├── components/       # React components
│   ├── chat/        # AI chat interface
│   ├── common/      # Reusable components (modals, forms)
│   ├── layout/      # Layout components (header, sidebar)
│   └── views/       # View components (table, board, timeline)
├── hooks/           # Custom React hooks
├── lib/             # Utility libraries (apiClient, cn)
├── pages/           # Page components
├── services/        # API service layer
├── store/           # Zustand state management
├── types/           # TypeScript type definitions
└── utils/           # Utility functions
```

---

## Key Architectural Patterns

### 1. AI Agent System (Keyword-First Design)

The AI agent uses **LangChain4j** with a unique **keyword-first** tool design:

**Core Principle**: Users never need to know IDs. All operations use natural language keywords.

```
User: "把字节跳动的申请改为面试中"
   ↓
AI calls: updateApplicationStatus(keyword="字节跳动", newStatus="INTERVIEW")
   ↓
ToolHelper.smartMatchApplication():
  1. Search by company name
  2. Search by job title
  3. Combined search ("字节-前端")
  4. Return unique match OR ask user to choose
```

**Tool Categories**:
- **Query Tools**: Read operations (can use ID)
- **Command Tools**: Write operations (MUST use keyword)
- **Statistics Tools**: Aggregation operations

**Location**: `src/main/java/com/jobtracker/agent/tools/`

### 2. Status Flow with Transition Rules

Application and interview statuses follow strict transition rules:

```java
// Application statuses with stages
WISHLIST → APPLIED → SCREENING → INTERVIEW → FINAL_ROUND → OFFERED
                                                  ↓
                                            ACCEPTED/DECLINED/EXPIRED

// Any status can transition to:
- REJECTED (company rejected)
- WITHDRAWN (user withdrew)
```

**Key Classes**:
- `ApplicationStatus.java`: Enum with stage grouping
- `InterviewStatus.java`: Interview state machine
- `TransitionRules.java`: Legal transition mappings
- `StatusTransitionService.java`: Validation and cascade logic

**Important**: Interview status changes automatically trigger application status updates

### 3. WebSocket Chat with Persistence

Real-time AI chat through WebSocket with full conversation history:

**Flow**:
1. Client connects → `ChatWebSocketHandler.afterConnectionEstablished()`
2. Client sends message → Handler saves to DB + calls AI Agent
3. AI responds → Handler saves response + sends to client
4. Messages persisted in `chat_sessions` and `chat_messages` tables

**Key Files**:
- `ChatWebSocketHandler.java`: Message handling
- `ChatHistoryService.java`: Persistence logic
- `SafeTurnBasedChatMemoryProvider.java`: Memory management
- `JobAgentFactory.java`: Dynamic agent creation for session isolation

**Important**: Each session gets its own isolated ChatMemory through JobAgentFactory

### 4. Chat Memory Isolation Pattern

**Problem**: Multiple users/sessions sharing the same AI agent causes memory contamination.

**Solution**: Use factory pattern to create agents with isolated memories per session.

```java
// JobAgentFactory creates agents with independent memories
@Component
public class JobAgentFactory {
    private final SafeTurnBasedChatMemoryProvider memoryProvider;

    public JobAgent createAgent(String sessionId) {
        // Provider manages internal cache per sessionId
        ChatMemory memory = memoryProvider.get(sessionId);

        return AiServices.builder(JobAgent.class)
            .chatMemory(memory)  // Isolated for this session
            .build();
    }
}

// In handler
JobAgent agent = jobAgentFactory.createAgent(sessionId);
String response = agent.chat(message);
```

**Key Points**:
- `SafeTurnBasedChatMemoryProvider` has internal cache (no duplicate caching needed)
- Each `sessionId` gets its own `ChatMemory` instance
- Messages are cleaned by turn (not by count) to preserve conversation integrity

### 5. Centralized State Management (Frontend)

**Zustand store** at `frontend/src/store/applicationStore.ts`:

```typescript
interface ApplicationStore {
  // State
  applications: JobApplication[];
  companies: Company[];
  interviews: InterviewRecord[];
  currentView: ViewType;
  filters: { status?, priority?, keyword? };

  // Operations
  fetchApplications: (keyword?: string) => Promise<void>;
  updateApplicationStatus: (id, status) => Promise<void>;
  switchView: (view) => void;
  setKeyword: (keyword) => Promise<void>; // Triggers backend search
}
```

**Pattern**: All API calls refresh data automatically (optimistic UI not used)

### 6. Multi-View Architecture

Frontend supports 4 interchangeable views:
- **Table**: Traditional data grid
- **Board**: Kanban-style (drag-drop status changes)
- **Timeline**: Chronological view with filters
- **Calendar**: Calendar view (react-big-calendar)

**Key Component**: `WorkspacePage.tsx` manages view switching

---

## Database Schema

### Core Tables

```sql
-- Companies (master data)
companies (id, name, industry, size, location, website, description, logo_url)

-- Job applications (linked to companies)
job_applications (id, company_id, job_title, status, application_date, priority, notes)

-- Interview records (linked to applications, support multiple rounds)
interview_records (id, application_id, round_number, is_final, interview_type,
                   interview_date, status, rating, feedback)

-- Application logs (audit trail)
application_logs (id, application_id, log_type, log_title, log_content, logged_by)

-- Chat history (AI conversations)
chat_sessions (id, session_key, created_at)
chat_messages (id, session_id, role, content, created_at)

-- Tool execution tracking
tool_call_records (id, session_id, tool_name, parameters, result, execution_time_ms)
```

### Relationships

```
companies (1) ───< (N) job_applications (1) ───< (N) interview_records
                                │
                                └──< (N) application_logs
```

**Migrations**: Located in `src/main/resources/db/migration/`

---

## API Design Patterns

### REST Endpoints

**Base URL**: `http://localhost:8080/api/data`

**Response Format** (unified):
```json
{
  "code": 200,
  "message": "成功",
  "data": { ... },
  "timestamp": 1234567890,
  "success": true
}
```

**Key Endpoints**:
- `GET /applications` - List all (supports keyword search via ?keyword=)
- `GET /applications/search` - Multi-field search
- `GET /applications/{id}/detail` - Full details with related data
- `PUT /applications/{id}/status?status={newStatus}` - Status update
- `GET /applications/{id}/interviews` - Related interviews
- `GET /export/excel` - Export to Excel (EasyExcel)
- `GET /export/json` - Export data as JSON

### WebSocket Endpoint

**URL**: `ws://localhost:8080/api/ws/chat`

**Message Format**:
```json
{
  "type": "CHAT",           // or HEARTBEAT
  "content": "user message",
  "sessionId": "optional-client-provided-id"
}
```

---

## Important Conventions

### Backend Conventions

1. **Entity Naming**: Use `JobApplication`, not `Application` (reserved word)
2. **Status Values**: Use enum codes (e.g., "WISHLIST"), not descriptions
3. **Logical Delete**: All tables have `deleted` field (MyBatis Plus)
4. **Timestamps**: `created_at`, `updated_at` in DB, `createdAt`, `updatedAt` in Java
5. **Service Layer**: Business logic in `*Service` classes, not controllers
6. **Error Handling**: Use `ToolResult.error()` for AI tools, standard exceptions otherwise

### Frontend Conventions

1. **Type Safety**: All API responses have TypeScript interfaces in `types/index.ts`
2. **Component Location**: Feature-based (e.g., `components/common/` for shared)
3. **Styling**: Tailwind CSS with custom colors in `tailwind.config.js`
4. **Date Handling**: Use `dayjs` for formatting
5. **API Calls**: Always use `dataApi` service, never `apiClient` directly in components
6. **State Updates**: Store methods auto-refresh data after mutations

### AI Tool Conventions

1. **Tool Descriptions**: Must follow template (Action + Object + Scenario + Params + Returns)
2. **Parameter Names**: No type suffixes (use `priority`, not `priorityInt`)
3. **Return Format**: Always return `ToolResult`, never plain strings or objects
4. **Keyword Matching**: Use `ToolHelper.smartMatchApplication/Interview/Company()`
5. **Error Codes**: Use constants from `ToolConstants` (e.g., `ERR_PARAM_MISSING`)

---

## AI Integration Details

### LM Studio Setup

**Configuration** (`application.yml`):
```yaml
langchain4j:
  lm-studio:
    base-url: http://127.0.0.1:1234/v1
    api-key: lm-studio
    model-name: google/gemma-3-4b
    temperature: 0.7
```

**System Prompt** (`JobAgent.java`):
- Injects current date/time (crucial for time-relative queries)
- Lists all available tools
- Defines workflow and constraints

### Tool Registration

Tools are auto-registered via `@Tool` annotation in `LangChain4jConfig`:

```java
@Bean
public JobAgent jobAgent(List<Object> toolBeans) {
    return LangChain4j.createChatLanguageModel(modelName)
        .tools(toolBeans)  // Auto-discovers @Component classes with @Tool methods
        .build();
}
```

---

## Testing Strategy

### Backend Testing

**Current State**: Test infrastructure exists, coverage needs improvement

**Key Test Areas**:
1. AI tool matching logic (`ToolHelper`)
2. Status transition validation
3. WebSocket message handling
4. Database integration (MyBatis Plus)

**Test File Location**: `src/test/java/com/jobtracker/`

### Frontend Testing

**Current State**: No automated tests yet

**Recommended Tools**:
- Vitest for unit tests
- React Testing Library for component tests
- Playwright for E2E tests

---

## Development Workflow

### Adding a New AI Tool

1. Create method in appropriate `*CommandTools` or `*QueryTools` class
2. Add `@Tool` annotation with proper description
3. Return `ToolResult` (never raw objects or strings)
4. Use `ToolHelper` for entity matching
5. Test via WebSocket client or frontend chat

### Adding New Application Status

1. Add enum to `ApplicationStatus.java`
2. Update `TransitionRules.APPLICATION_TRANSITIONS`
3. Add migration script (if DB change needed)
4. Update frontend `types/index.ts`
5. Add to `ToolConstants.STATUS_DESC_*`

### Frontend Feature Development

1. Add TypeScript types in `types/index.ts`
2. Create API methods in `services/dataApi.ts`
3. Add store methods if needed
4. Create components in appropriate directory
5. Use `cn()` utility for Tailwind classes

---

## Configuration Files

### Backend Configuration

**`application.yml`**: Main configuration
- Database connection (MySQL)
- Redis (session cache)
- LangChain4j (LM Studio)
- Jackson (JSON serialization)
- MyBatis Plus (ORM)
- Logging levels

**Important Settings**:
- `server.port: 8080`
- `server.servlet.context-path: /api`
- Database timezone: `Asia/Shanghai`
- AI timeout: 300 seconds

### Frontend Configuration

**`vite.config.ts`**: Build configuration
- Tailwind CSS plugin
- React plugin
- Path aliases (@/ → src/)

**`.env`**: Environment variables
```bash
VITE_API_BASE_URL=http://localhost:8080/api/data
VITE_WS_URL=ws://localhost:8080/api/ws/chat
```

---

## Common Issues & Solutions

### Backend Issues

**Problem**: AI not responding
- **Check**: LM Studio running on port 1234
- **Check**: Model loaded in LM Studio
- **Check**: `langchain4j.lm-studio.base-url` in application.yml

**Problem**: Database connection failed
- **Check**: MySQL running on port 3306
- **Check**: Database `job_tracker` exists
- **Check**: Password in `application.yml`

**Problem**: Tool not found by AI
- **Solution**: Verify `@Tool` annotation format
- **Solution**: Check tool description clarity
- **Solution**: Ensure class has `@Component` annotation

### Frontend Issues

**Problem**: API calls failing with CORS
- **Check**: `CorsConfig.java` allows origin
- **Check**: Frontend `.env` has correct API URL

**Problem**: WebSocket not connecting
- **Check**: Backend running on port 8080
- **Check**: `VITE_WS_URL` in `.env`
- **Check**: Browser console for errors

---

## Code Quality Standards

### Java Code Style

- Use Lombok annotations (`@Data`, `@Builder`, `@RequiredArgsConstructor`)
- All service methods must have JavaDoc
- Use `@Transactional` for write operations
- Log AI tool calls at INFO level
- Use `ToolResult` for all AI tool responses

### TypeScript Code Style

- Use functional components with hooks
- Prefer interfaces over types for object shapes
- Use `cn()` for conditional Tailwind classes
- Keep components under 300 lines
- Extract custom hooks for complex logic

---

## Deployment Notes

### Backend Deployment

1. Build: `mvn clean package -DskipTests`
2. Run: `java -jar target/job-tracker-backend-1.0.0.jar`
3. Requires: Java 17+, MySQL 8.0+, Redis (optional)

### Frontend Deployment

1. Build: `npm run build`
2. Output: `dist/` directory
3. Serve with any static file server (nginx, Apache, etc.)

---

## Documentation References

### Documentation Naming Convention

**⚠️ 重要规则**：创建的所有文档文件名必须使用**中文+时间**格式

#### 格式规范

```
中文描述_YYYYMMDD.md
```

#### 示例

- ❌ `TOOL_CALL_TRACKING_FIX.md`
- ✅ `工具调用追踪修复_20260315.md`

- ❌ `AI_TOOLS_REFACTOR_DESIGN.md`
- ✅ `AI工具重构设计_20260314.md`

- ❌ `STATUS_FLOW_DESIGN.md`
- ✅ `状态流程设计_20260314.md`

#### 说明

1. **文件名使用中文**：便于中文用户快速理解文档内容
2. **添加日期后缀**：便于按时间追踪文档历史和版本
3. **日期格式**：`YYYYMMDD`（如 `20260315` 表示 2026年3月15日）
4. **短横线分隔**：中文描述和日期之间用短横线 `_` 分隔

#### 例外情况

- **代码文件**：代码文件名仍使用英文（如 `ApplicationService.java`）
- **配置文件**：配置文件名保持原格式（如 `application.yml`, `pom.xml`）
- **已有文档**：不需要重命名已有文档，但新文档必须遵循此规则

**Backend Design Docs** (`backend/docs/`):

```
docs/
├── 问题修复/          # Problem fixes (4个文档)
│   ├── 聊天记忆隔离问题分析与解决方案_20260315.md
│   ├── 多表查询优化需求文档_20260315.md
│   ├── 工具调用追踪修复_20260315.md
│   └── 工具调用追踪快速参考_20260315.md
├── 功能设计/          # Feature designs (3个文档)
│   ├── AI应用开发_聊天记忆隔离问题_20260315.md
│   ├── AI工具重构设计_20260314.md
│   └── 定时提醒系统设计_20260314.md
├── 架构设计/          # Architecture designs (3个文档)
│   ├── AI聊天持久化设计_20260313.md
│   ├── 状态流程设计_20260314.md
│   └── 前端设计文档_20260312.md
└── API文档/           # API references (5个文档)
    ├── API参考文档_20260312.md
    ├── P2阶段API参考文档_20260313.md
    ├── 前端API参考文档_20260312.md
    ├── 新API参考文档_20260313.md
    └── 待实现接口文档_20260313.md
```

---

## ⚠️ Critical Lessons Learned (重要教训)

### 教训 1：创建文档后必须等待用户审批才能执行实现

**问题**：在创建设计/分析文档后，未经用户同意就开始按照文档执行实现。

**严重性**：🔴 **非常严重**

**正确流程**：
1. 创建分析/设计文档
2. **等待用户审阅和批准**
3. 根据用户反馈修改文档
4. **只有在用户明确批准后才开始实现**

**为什么重要**：
- 用户在审批阶段可能会发现技术方案中的问题
- 用户可能有不同的实现思路
- 提前执行会导致返工和浪费

---

### 教训 2：文档必须按类型分类存放

**问题**：用户要求在 `docs/` 文件夹中创建子文件夹来存放相同类型的文档，但没有执行。

**正确文档结构**：
```
docs/
├── 问题修复/          # Problem fixes
│   └── 聊天记忆隔离问题分析与解决方案_20260315.md
├── 功能设计/          # Feature designs
│   ├── 状态流程设计_20260314.md
│   └── AI工具重构设计_20260314.md
└── 架构设计/          # Architecture designs
    └── AI聊天持久化设计_20260313.md
```

**规则**：
- 创建新文档时，先确定其类型
- 在对应的子文件夹中创建文档
- 如果子文件夹不存在，先创建文件夹

---

### 教训 3：使用项目中已有的自定义实现，不要盲目使用官方简单实现

**问题**：在实现聊天记忆隔离时，使用了官方的 `MessageWindowChatMemory`，而项目中已经存在自定义的 `SafeTurnBasedChatMemory` 和 `SafeTurnBasedChatMemoryProvider`。

**原因**：
- 用户之前就是因为官方 `MessageWindowChatMemory` 有问题才自定义了 `SafeTurnBasedChatMemory`
- 如果等待用户审批，用户就会发现这个问题

**正确做法**：
1. 在设计方案前，先搜索项目中已有的相关实现
2. 优先使用项目中已有的、经过验证的自定义实现
3. 如果需要使用新的组件，在文档中说明理由并获得用户确认

**需要搜索的关键位置**：
- `src/main/java/com/jobtracker/agent/memory/` - 记忆相关实现
- 现有的 `*Provider` 和 `*Manager` 类

---

### 教训 4：避免重复设计，检查现有组件是否已有缓存/管理功能

**问题**：在设计中创建了 `ChatMemoryManager` 来管理 ChatMemory 缓存，但 `SafeTurnBasedChatMemoryProvider` 内部已经有了 `ConcurrentHashMap` 来做同样的事情。

**原因**：
- 没有仔细阅读现有实现的完整代码
- 没有意识到 `SafeTurnBasedChatMemoryProvider.get()` 方法内部已经有缓存逻辑

**正确做法**：
1. 在设计新组件前，**完整阅读**现有相关类的代码
2. 检查现有类是否已经实现了：
   - 缓存管理（ConcurrentHashMap、Map 等）
   - 清理方法（clear、remove 等）
   - 监控方法（size、count 等）
3. 如果现有类已经实现了这些功能，直接复用，不要重复设计

**示例对比**：

❌ **错误设计**（重复缓存）：
```java
// ChatMemoryManager 自己又加了一层缓存
private final Map<String, ChatMemory> memoryMap = new ConcurrentHashMap<>();
private final SafeTurnBasedChatMemoryProvider provider;
```

✅ **正确设计**（直接使用现有缓存）：
```java
// 直接使用 provider，它内部已经有缓存了
private final SafeTurnBasedChatMemoryProvider provider;
public ChatMemory getMemory(String sessionId) {
    return provider.get(sessionId);  // 内部自动缓存
}
```

---

### 综合检查清单

在执行任何实现前，必须确认：

- [ ] 文档已创建并放置在正确的子文件夹中
- [ ] 文档中使用的所有技术组件已验证存在且可用
- [ ] 用户已审阅文档并明确批准执行
- [ ] 没有使用项目中已知有问题的官方简单实现
- [ ] 优先使用项目中已有的自定义实现
- [ ] **已检查现有组件是否有缓存/管理功能，避免重复设计**

---

## 操作规范

### 数据库脚本操作规范

**⚠️ 重要规则**：所有数据库迁移脚本由用户手动执行，AI 禁止自动运行。

**原因**：
- 数据库操作敏感，需要人工确认
- 用户需要掌控执行时机
- 避免意外数据丢失

**AI 职责**：
- 创建迁移脚本文件（`V*__*.sql`）
- 在文档中说明脚本内容和作用
- **不执行**任何数据库命令

**用户操作**：
- 手动运行迁移脚本
- 验证表结构是否正确
- 确认无问题后通知 AI 继续
