# 求职追踪应用 - 前端设计文档

**文档版本**: 1.0
**创建日期**: 2026-03-12
**设计者**: Claude (AI Assistant)
**架构方案**: 方案 A - 全功能实现

---

## 📋 目录

1. [项目概述](#1-项目概述)
2. [整体架构](#2-整体架构)
3. [技术栈](#3-技术栈)
4. [项目结构](#4-项目结构)
5. [核心功能设计](#5-核心功能设计)
6. [状态管理](#6-状态管理)
7. [API 服务层](#7-api-服务层)
8. [UI 组件库](#8-ui-组件库)
9. [开发计划](#9-开发计划)

---

## 1. 项目概述

### 1.1 项目定位

求职追踪应用的前端界面 - 一个以 AI 对话为主导，同时提供传统 CRUD 操作的 Web 应用。

### 1.2 核心特性

- 🎨 **Notion 风格**：使用 block-suite 实现正宗 Notion 体验
- 🤖 **AI 对话驱动**：90% 操作通过自然语言完成
- 📊 **多视图系统**：表格、看板、时间线、日历 4 种视图
- 🔄 **分屏布局**：左侧数据展示，右侧 AI 面板（可收起）
- ⚡ **实时同步**：WebSocket 实时通信，数据自动刷新

### 1.3 用户画像

- 🎯 **主要用户**：求职者（正在找工作的技术人员）
- 💻 **技术背景**：熟悉 Notion、GitHub 等工具
- 🎯 **使用场景**：
  - 记录投递公司和岗位
  - 追踪面试进度
  - 管理公司信息
  - 查看统计数据

---

## 2. 整体架构

### 2.1 页面布局

```
┌─────────────────────────────────────────────────────────┐
│  Header（固定，60px）                                          │
│  Logo | 导航 | 搜索 | 用户 | 设置                            │
├──────────────────────┬────────────────────────────────────┤
│  左侧：数据展示区      │  右侧：AI 对话区（可收起）            │
│  (flex-grow)          │  (width: 400px, 可变)              │
│                      │                                     │
│  ┌────────────────┐  │  ┌─────────────────────────────┐   │
│  │ 视图切换 Tabs  │  │  │ 聊天消息列表               │   │
│  │ [表格|看板|...] │  │  │                           │   │
│  ├────────────────┤  │  │ 用户: 帮我查询统计数据      │   │
│  │                │  │  │ AI: 📊 统计报告如下...      │   │
│  │  内容区域       │  │  │                           │   │
│  │  表格/卡片      │  │  │                           │   │
│  │                │  │  └─────────────────────────────┘   │
│  └────────────────┘  │                                     │
│                      │  [输入框: 输入你的问题...]    │   │
│                      │  [收起按钮 ◀]                    │
│                      │                                     │
└──────────────────────┴────────────────────────────────────┘
```

### 2.2 组件层次

```
App
├── Router
│   ├── / (WorkspacePage)
│   └── /dashboard (DashboardPage)
├── SplitView (布局容器)
│   ├── ViewSelector (视图切换)
│   ├── ContentArea (内容区)
│   │   └── ViewRenderer (视图渲染器)
│   │       ├── TableView
│   │       ├── BoardView
│   │       ├── TimelineView
│   │       └── CalendarView
│   └── ChatPanel (AI 面板)
│       ├── MessageList
│       ├── TypingIndicator
│       └── InputBox
└── ThemeProvider (主题)
```

---

## 3. 技术栈

### 3.1 核心依赖

| 包名 | 版本 | 用途 |
|------|------|------|
| react | 18.2+ | UI 框架 |
| react-dom | 18.2+ | DOM 渲染 |
| vite | 5.0+ | 构建工具 |
| typescript | 5.0+ | 类型系统 |
| @block-suite/core | 0.15+ | Notion 风格块编辑器 |
| @block-suite/react | 0.15+ | React 集成 |
| zustand | 4.4+ | 状态管理 |
| react-router-dom | 6.20+ | 路由 |
| axios | 1.6+ | HTTP 客户端 |
| sockjs-client | 1.6+ | WebSocket（降级支持） |
| @dnd-kit/core | 0.3.0+ | 拖拽（看板） |
| react-big-calendar | 1.11+ | 日历视图 |
| dayjs | 1.11+ | 日期处理 |
| tailwindcss | 3.4+ | 样式框架 |
| lucide-react | latest | 图标库 |

### 3.2 UI 组件库

**shadcn/ui** + **Tailwind CSS**
- 组件位置：`components/ui/`
- 主要组件：Button, Card, Input, Select, Dialog, Dropdown
- 主题：支持亮色/暗色模式切换

### 3.3 开发工具

- ESLint + Prettier（代码规范）
- Vitest（测试框架，可选）
- VS Code（推荐 IDE）

---

## 4. 项目结构

```
frontend/
├── src/
│   ├── main.tsx                # 应用入口
│   ├── App.tsx                 # 根组件
│   │
│   ├── pages/                  # 页面组件
│   │   ├── WorkspacePage.tsx   # 主工作区
│   │   └── DashboardPage.tsx    # 仪表盘
│   │
│   ├── components/             # 可复用组件
│   │   ├── layout/             # 布局组件
│   │   │   ├── Header.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   ├── SplitView.tsx   # 分屏容器 ⭐
│   │   │   └── ViewToggle.tsx  # 视图切换
│   │   │
│   │   ├── views/             # 视图组件 ⭐
│   │   │   ├── TableView.tsx    # 表格视图
│   │   │   ├── BoardView.tsx    # 看板视图
│   │   │   ├── TimelineView.tsx # 时间线视图
│   │   │   └── CalendarView.tsx # 日历视图
│   │   │
│   │   ├── ai/               # AI 组件 ⭐
│   │   │   ├── ChatPanel.tsx    # AI 面板
│   │   │   ├── MessageList.tsx  # 消息列表
│   │   │   ├── MessageBubble.tsx # 消息气泡
│   │   │   ├── InputBox.tsx     # 输入框
│   │   │   ├── ToggleButton.tsx # 收起按钮
│   │   │   └── StatusIndicator.tsx
│   │   │
│   │   └── ui/               # 基础 UI 组件
│   │       ├── ApplicationCard.tsx
│   │       ├── CompanyCard.tsx
│   │       ├── InterviewCard.tsx
│   │       ├── StatusBadge.tsx
│   │       └── PriorityBadge.tsx
│   │
│   ├── store/                 # Zustand 状态
│   │   ├── applicationStore.ts # 数据状态 ⭐
│   │   ├── chatStore.ts        # 对话状态 ⭐
│   │   └── uiStore.ts          # UI 状态
│   │
│   ├── services/              # API 服务 ⭐
│   │   ├── api.ts             # Axios 配置
│   │   ├── dataApi.ts         # HTTP API 封装
│   │   └── websocket.ts      # WebSocket 客户端
│   │
│   ├── lib/                   # 第三方库配置
│   │   ├── block-suite/       # block-suite 配置
│   │   └── shadcn/           # shadcn/ui 组件
│   │
│   ├── hooks/                 # 自定义 Hooks
│   │   ├── useApi.ts
│   │   ├── useWebSocket.ts
│   │   └── useLocalStorage.ts
│   │
│   ├── types/                 # TypeScript 类型
│   │   ├── application.ts
│   │   ├── company.ts
│   │   ├── interview.ts
│   │   ├── websocket.ts
│   │   └── store.ts
│   │
│   └── utils/                 # 工具函数
│       ├── formatters.ts
│       ├── validators.ts
│       └── constants.ts
│
├── public/                     # 静态资源
│   └── favicon.ico
│
├── index.html
├── vite.config.ts
├── tsconfig.json
├── tailwind.config.js
├── package.json
└── README.md
```

---

## 5. 核心功能设计

### 5.1 分屏布局（SplitView）

**功能**：
- 左侧：数据展示区（自适应宽度）
- 右侧：AI 对话面板（默认 400px，可收起）
- 拖拽分隔条调整宽度
- 移动端自动堆叠

**状态**：
```typescript
interface SplitViewState {
  panelWidth: number;        // 右侧面板宽度
  isPanelOpen: boolean;      // 面板是否展开
  isResizing: boolean;       // 是否正在拖拽
}
```

**交互**：
- 点击收起按钮：面板宽度变为 0
- 拖拽分隔条：调整左右比例
- 移动端：面板覆盖在数据上方

---

### 5.2 表格视图（TableView）

**功能**：
- 类似 Notion Database 的表格
- 列：公司、职位、状态、日期、优先级
- 行内编辑：双击单元格编辑
- 列排序：点击表头排序
- 列筛选：下拉筛选条件
- 快捷操作：`/` 命令打开操作菜单

**block-suite 集成**：
- 表格作为 Block 组件渲染
- 支持斜杠命令（如 `/st` 过滤状态）
- 支持拖拽排序

---

### 5.3 看板视图（BoardView）

**功能**：
- 按状态分列：WISHLIST / APPLIED / INTERVIEW / OFFER / REJECTED
- 每列显示卡片
- 拖拽卡片改变状态
- 卡片信息：公司名、职位、日期、优先级

**实现**：
- @dnd-kit/core 实现拖拽
- shadcn/ui Card 组件
- 状态映射到不同颜色

---

### 5.4 时间线视图（TimelineView）

**功能**：
- 垂直时间轴
- 节点类型：
  - 🔵 申请
  - 🟢 面试
  - 🟡 状态变更
  - 🔴 已拒绝
  - 🟢 已 offer
- 点击节点展开详情

**样式**：
- shadcn/ui Timeline 或自定义 CSS
- Notion 风格的简洁设计

---

### 5.5 日历视图（CalendarView）

**功能**：
- 月度日历
- 面试日期标记
- 点击日期查看详情
- 支持月份切换

**实现**：
- react-big-calendar
- 自定义事件渲染

---

### 5.6 AI 对话系统

#### 5.6.1 ChatPanel 组件

**结构**：
```tsx
<div className="chat-panel">
  <Header>
    <Title>AI 助手</Title>
    <ConnectionStatus />
    <Actions>
      <ClearButton />
      <MinimizeButton />
    </Actions>
  </Header>

  <MessageList>
    {messages.map(msg => (
      <MessageBubble
        role={msg.role}
        content={msg.content}
        timestamp={msg.timestamp}
      />
    ))}
  </MessageList>

  <InputArea>
    <Textarea
      placeholder="输入你的问题，或使用 / 查看命令..."
      onKeyDown={(e) => handleSend(e)}
    />
    <SendButton />
  </InputArea>
</div>
```

#### 5.6.2 WebSocket 集成

**消息格式**：
```typescript
// 发送
{
  type: 'CHAT',
  content: userInput,
  sessionId: getSessionId()
}

// 接收
{
  type: 'CHAT',
  content: aiResponse,
  timestamp: 1678567890000
}
```

**流式显示**：
- 使用 `stream: true` 开启流式响应
- 打字机效果逐字显示

---

## 6. 状态管理

### 6.1 ApplicationStore（数据状态）

```typescript
interface ApplicationStore {
  // 状态
  applications: JobApplication[];
  companies: Company[];
  interviews: InterviewRecord[];
  currentView: ViewType;
  filters: {
    status?: string;
    priority?: number;
    dateRange?: [Date, Date];
    keyword?: string;
  };
  loading: boolean;
  error: string | null;

  // 操作
  fetchApplications(): Promise<void>;
  createApplication(data: CreateApplicationDTO): Promise<number>;
  updateApplication(id: number, data: Partial<JobApplication>): Promise<void>;
  deleteApplication(id: number): Promise<void>;

  switchView(view: ViewType): void;
  setFilters(filters: Partial<typeof this.filters>): void;
  clearFilters(): void;
}
```

### 6.2 ChatStore（对话状态）

```typescript
interface ChatStore {
  // 状态
  messages: ChatMessage[];
  isConnected: boolean;
  isTyping: boolean;
  panelWidth: number;
  isPanelOpen: boolean;

  // 操作
  sendMessage(content: string): Promise<void>;
  clearHistory(): void;
  togglePanel(): void;
  setPanelWidth(width: number): void;
  connect(): void;
  disconnect(): void;
}
```

---

## 7. API 服务层

### 7.1 HTTP API 客户端

```typescript
// src/services/api.ts
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api/data',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
apiClient.interceptors.request.use((config) => {
  // 添加 token（如果需要）
  return config;
});

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);

export default apiClient;
```

### 7.2 WebSocket 客户端

```typescript
// src/services/websocket.ts
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class ChatWebSocketClient {
  private socket: WebSocket | SockJS | null = null;
  private stompClient: Client | null = null;
  private subscriptions: Map<string, (msg: any) => void> = new Map();

  connect(url: string) {
    this.socket = new SockJS(url);
    this.stompClient = Stomp.over(this.socket);

    this.stompClient.connect({}, () => {
      console.log('WebSocket 连接成功');
      this.onConnected();
    });
  }

  sendMessage(message: string) {
    this.stompClient.send('/api/ws/chat', {}, JSON.stringify({
      type: 'CHAT',
      content: message,
      sessionId: this.getSessionId()
    }));
  }

  onMessage(callback: (msg: any) => void) {
    this.stompClient.subscribe('/topic/messages', (message) => {
      callback(JSON.parse(message.body));
    });
  }

  disconnect() {
    this.stompClient?.disconnect();
  }
}

export default new ChatWebSocketClient();
```

---

## 8. UI 组件库

### 8.1 使用 shadcn/ui

初始化：
```bash
npx shadcn-ui@latest init
```

添加组件：
```bash
npx shadcn-ui@latest add button
npx shadcn-ui@latest add card
npx shadcn-ui@latest add input
npx shadcn-ui@latest add select
npx shadcn-ui@latest add dialog
npx shadcn-ui@latest add dropdown
npx shadcn-ui@latest add badge
...
```

### 8.2 自定义主题

**Tailwind 配置**：
```javascript
// tailwind.config.js
module.exports = {
  darkMode: ['class'],
  theme: {
    extend: {
      colors: {
        border: 'hsl(var(--border))',
        input: 'hsl(var(--input))',
        ring: 'hsl(var(--ring))',
        background: 'hsl(var(--background))',
        foreground: 'hsl(var(--foreground))',
        primary: {
          DEFAULT: '#3b82f6',
          foreground: '#ffffff',
        },
        // ... Notion 风格配色
      }
    }
  }
}
```

---

## 9. 开发计划

### Week 1：基础架构（3 月 11-15 日）

**目标**：完成项目搭建和核心框架

- [ ] 项目初始化
  - [ ] Vite + React + TypeScript 项目
  - [ ] 安装所有依赖
  - [ ] 配置 Tailwind CSS
  - [ ] 配置 shadcn/ui
  - [ ] 配置 block-suite
- [ ] 创建项目结构
  - [ ] 创建所有目录
  - [ ] 创建组件模板
- [ ] 实现状态管理
  - [ ] applicationStore
  - [ ] chatStore
  - [ ] uiStore
- [ ] 实现 API 服务层
  - [ ] HTTP 客户端
  - [ ] WebSocket 客户端
  - [ ] 类型定义
- [ ] 实现布局组件
  - [ ] SplitView（分屏容器）
  - [ ] Header（顶部导航）
  - [ ] ViewToggle（视图切换）

**验收标准**：
- ✅ 项目可正常启动
- ✅ 状态管理工作正常
- ✅ 可连接后端 API
- ✅ WebSocket 可连接

---

### Week 2：视图系统（3 月 16-22 日）

**目标**：完成 4 个视图

- [ ] TableView（表格视图）
  - [ ] 基础表格渲染
  - [ ] 数据加载
  - [ ] 状态筛选
  - [ ] 列排序
- [ ] BoardView（看板视图）
  - [ ] 分列布局
  - [ ] 拖拽功能
  - [ ] 状态更新
- [ ] TimelineView（时间线视图）
  - [ ] 垂直时间轴
  - [ ] 节点渲染
  - [ ] 详情展开
- [ ] CalendarView（日历视图）
  - [ ] 月度日历
  - [ ] 面试标记
  - [ ] 月份切换

**验收标准**：
- ✅ 4 个视图均可正常展示
- ✅ 数据在视图间切换时保持一致
- ✅ 看板拖拽功能正常

---

### Week 3：AI 集成与优化（3 月 23-29 日）

**目标**：完成 AI 对话和优化

- [ ] ChatPanel 组件
  - [ ] 消息列表
  - [ ] 消息气泡
  - [ ] 输入框
  - [ ] 发送按钮
- [ ] WebSocket 完整集成
  - [ ] 连接管理
  - [ ] 消息收发
  - [ ] 流式显示
  - [ ] 错误处理
- [ ] 快捷操作系统
  - [ ] "新建申请" 按钮
  - [ ] "更新状态" 按钮
- [ ] 增强功能
  - [ ] 表格行内编辑
  - [ ] 搜索功能
  - [ ] 响应式适配
  - [ ] 性能优化

**验收标准**：
- ✅ AI 对话功能完整可用
- ✅ 快捷操作与 AI 对话同步
- ✅ 移动端适配良好

---

## 10. 后端接口需求说明

### 10.1 接口缺失处理流程

**当前已提供的接口**：
- ✅ 16 个 HTTP REST API
- ✅ WebSocket 聊天接口
- ✅ 9 个 AI 工具方法

**发现接口缺失时的处理流程**：

1. **前端记录需求** → 创建 `BACKEND_TODO.md`
2. **提交后端实现** → 后端添加接口
3. **验证可用** → 前端集成

### 10.2 TODO 文档模板

文件位置：`frontend/BACKEND_TODO.md`

```markdown
# 后端接口需求 TODO

## 待实现接口列表

### 接口 #1：批量更新状态

**优先级**: P0（阻塞功能）
**用途**: 用户在看板视图拖拽卡片后，批量更新多个申请的状态

**接口详情**:
- **URL**: `PUT /api/data/applications/batch/status`
- **方法**: HTTP PUT
- **请求参数**:
  ```typescript
  {
    ids: number[];        // 申请 ID 数组
    newStatus: string;    // 目标状态
  }
  ```
- **参数类型**:
  - `ids`: `number[]` - 必填
  - `newStatus`: `string` - 必填，枚举值（WISHLIST/APPLIED/INTERVIEW/OFFER/REJECTED/WITHDRAWN）
- **返回值**:
  ```typescript
  {
    code: 200,
    message: string,
    data: {
      success: number;    // 成功更新的数量
      failed: number;      // 失败数量
      errors?: {          // 失败详情
        id: number;
        error: string;
      }[]
    }
  }
  ```

**使用场景**:
- 看板视图拖拽 3 个申请到"面试中"列
- 前端调用：`PUT /api/data/applications/batch/status?ids=1,3,5&newStatus=INTERVIEW`

**备选方案（降级接口）**:
如果批量接口无法实现，前端将逐个调用现有接口：
```javascript
// 降级方案：循环调用单条更新接口
for (const id of ids) {
  await fetch(`/api/data/applications/${id}/status?status=${newStatus}`, {
    method: 'PUT'
  });
}
```

**备注**:
- 需要事务支持，确保全部成功或全部失败
- 需要在 ApplicationLog 中记录批量操作日志

---

### 接口 #2：搜索申请

**优先级**: P1（影响体验）
**用途**: 全局搜索栏，支持公司名、职位名、备注关键词搜索

**接口详情**:
- **URL**: `GET /api/data/applications/search`
- **方法**: HTTP GET
- **请求参数**:
  - `keyword`: `string` - 搜索关键词
  - `page`: `number` - 页码（可选，默认 1）
  - `size`: `number` - 每页数量（可选，默认 10）
- **返回值**:
  ```typescript
  {
    code: 200,
    message: "搜索成功",
    data: {
      records: JobApplication[];
      total: number;
    }
  }
  ```

**使用场景**:
- 顶部搜索框输入 "字节"，搜索所有包含"字节"的申请
- 输入 "后端工程师"，搜索职位名称包含该关键词的申请

**备选方案**:
使用现有接口 `GET /api/data/applications` + 前端过滤：
```javascript
const allApps = await fetch('/api/data/applications').then(r => r.json());
const filtered = allApps.data.filter(app =>
  app.jobTitle.includes(keyword) ||
  app.company?.name.includes(keyword)
);
```

---

### 接口 #3：删除申请

**优先级**: P2（功能完善）
**用途**: 用户删除不需要的申请记录

**接口详情**:
- **URL**: `DELETE /api/data/applications/{id}`
- **方法**: HTTP DELETE
- **请求参数**:
  - `id`: `number` - 申请 ID（路径参数）
- **返回值**:
  ```typescript
  {
    code: 200,
    message: "删除成功",
    data: null
  }
  ```

**使用场景**:
- 用户在申请卡片上点击"删除"按钮
- 前端调用：`DELETE /api/data/applications/5`

**备选方案**:
软删除：将状态更新为 WITHDRAWN
```javascript
await fetch(`/api/data/applications/${id}/status?status=WITHDRAWN`, {
  method: 'PUT'
});
```

---

## 接口需求提交流程

### 前端开发人员操作

1. **发现接口缺失**
   - 在开发过程中发现需要某个接口
   - 检查后端 API 文档确认是否存在

2. **记录到 TODO 文档**
   - 打开 `frontend/BACKEND_TODO.md`
   - 按照模板添加接口需求

3. **提交给后端**
   - 通过 Git 提交 TODO 文档
   - 或直接联系后端开发人员

4. **等待实现**
   - 后端实现接口
   - 更新 API 文档

5. **集成测试**
   - 前端集成新接口
   - 验证功能正常

### 后端开发人员操作

1. **查看 TODO 文档**
   - 阅读 `frontend/BACKEND_TODO.md`
   - 理解接口需求

2. **评估可行性**
   - 确认是否能实现
   - 评估工作量

3. **实现接口**
   - 按照接口详情实现
   - 更新 Swagger 文档

4. **沟通备选方案**
   - 如果无法实现，提供备选接口
   - 在 TODO 文档中标注

5. **通知前端**
   - 接口已就绪
   - 更新 TODO 文档状态

---

## 11. 部署

### 11.1 构建命令

```bash
# 开发环境
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview
```

### 11.2 部署方案

**方案 A：静态部署**
- 部署到 Vercel / Netlify / GitHub Pages
- 使用 `/api/data` 代理到后端

**方案 B：容器化**
- Docker 容器
- Nginx 反向代理

---

**文档状态**: ✅ 设计完成，等待实现
**下一步**: 调用 writing-plans skill 创建详细实现计划
