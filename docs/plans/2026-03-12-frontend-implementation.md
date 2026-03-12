# Frontend Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans or superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** 构建一个 Notion 风格的求职追踪应用前端，支持 AI 对话、多视图系统、实时数据同步

**Architecture:**
- React 18 + Vite + TypeScript
- block-suite 实现 Notion 风格编辑器
- 分屏布局：左侧数据展示，右侧 AI 对话（可收起）
- WebSocket 实时通信
- Zustand 状态管理

**Tech Stack:**
- React 18.2+, Vite 5.0+, TypeScript 5.0+
- block-suite 0.15+, shadcn/ui, Tailwind CSS 3.4+
- Zustand 4.4+, React Router 6.20+, Axios 1.6+
- SockJS 1.6+, @dnd-kit/core 0.3+, react-big-calendar 1.11+
- dayjs 1.11+, lucide-react

---

## 📋 目录

1. [项目初始化](#task-1-项目初始化)
2. [基础架构搭建](#task-2-基础架构搭建)
3. [布局组件](#task-3-布局组件)
4. [状态管理](#task-4-状态管理)
5. [API 服务层](#task-5-api-服务层)
6. [表格视图](#task-6-表格视图)
7. [看板视图](#task-7-看板视图)
8. [时间线视图](#task-8-时间线视图)
9. [日历视图](#task-9-日历视图)
10. [AI 对话系统](#task-10-ai-对话系统)
11. [快捷操作](#task-11-快捷操作)
12. [响应式适配](#task-12-响应式适配)
13. [测试与部署](#task-13-测试与部署)

---

## Task 1: 项目初始化

**Files:**
- Create: `frontend/vite.config.ts`
- Create: `frontend/tsconfig.json`
- Create: `frontend/package.json`
- Create: `frontend/index.html`
- Create: `frontend/README.md`

**Step 1: 创建 Vite + React 项目**

```bash
# 在项目根目录执行
cd /Users/qingyu/job-tracker
npm create vite@latest frontend -- --template react-ts
cd frontend
```

预期输出: 项目创建成功

**Step 2: 安装核心依赖**

```bash
# UI 组件库
npm install block-suite @block-suite/react @block-suite/core
npm install -D tailwindcss postcss autoprefixer

# shadcn/ui 初始化（需要手动确认配置）
npx shadcn-ui@latest init
# 按提示选择：Default style, CSS variables, Slate color scheme

# 状态管理
npm install zustand

# 路由
npm install react-router-dom

# HTTP 客户端
npm install axios

# WebSocket
npm install sockjs-client @stomp/stompjs

# 拖拽
npm install @dnd-kit/core

# 日历
npm install react-big-calendar date-fns

# 工具库
npm install dayjs clsx tailwind-merge

# 图标
npm install lucide-react

# 开发依赖
npm install -D @types/node
```

**Step 3: 配置 Tailwind CSS**

创建文件: `frontend/tailwind.config.js`

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  darkMode: ['class'],
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}',
  ],
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
        // Notion 风格配色
        notion: {
          text: '#37352f',
          background: '#ffffff',
          border: '#e3e2e0',
          hover: '#f7f6f3',
          selected: '#2e68c0',
        }
      }
    }
  }
}
```

**Step 4: 配置 block-suite**

创建文件: `frontend/src/lib/block-suite.ts`

```typescript
import { createReactEditor } from '@blocksuite/react';
import '@blocksuite/core/styles/style.css';
import '@blocksuite/core/themes/theme.css';

export const editor = createReactEditor();
```

**注意**: block-suite 的 API 可能会变化，如遇到导入问题，参考官方文档：https://blockSuite.cn

**Step 5: 配置 TypeScript**

创建文件: `frontend/tsconfig.json`

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "jsx": "react-jsx",
    "jsxImportSource": "react",
    "moduleResolution": "bundler",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "allowSyntheticDefaultImports": true
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

**Step 6: 更新 index.html**

修改: `frontend/index.html`

```html
<!doctype html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/favicon.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Job Tracker</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
```

**Step 7: 配置 Vite**

创建文件: `frontend/vite.config.ts`

```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/api/ws': {
        target: 'ws://localhost:8080',
        ws: true,
      },
    },
  },
});
```

**Step 8: 提交初始化**

```bash
git add .
git commit -m "feat: initialize frontend project with Vite + React + TypeScript"
```

---

## Task 2: 基础架构搭建

**Files:**
- Create: `frontend/src/main.tsx`
- Create: `frontend/src/App.tsx`
- Create: `frontend/src/types/index.ts`
- Create: `frontend/src/utils/constants.ts`

**Step 1: 创建主入口**

创建: `frontend/src/main.tsx`

```tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
```

**Step 2: 创建根组件**

创建: `frontend/src/App.tsx`

```tsx
import { BrowserRouter } from 'react-router-dom';
import { WorkspacePage } from './pages/WorkspacePage';

function App() {
  return (
    <BrowserRouter>
      <WorkspacePage />
    </BrowserRouter>
  );
}

export default App;
```

**Step 2.5: 创建工作区页面**

创建: `frontend/src/pages/WorkspacePage.tsx`

```tsx
import { Header } from '@/components/layout/Header';
import { ViewToggle } from '@/components/layout/ViewToggle';
import { SplitView } from '@/components/layout/SplitView';
import { useApplicationStore } from '@/store/applicationStore';
import type { ViewType } from '@/utils/constants';

export function WorkspacePage() {
  const { currentView, switchView } = useApplicationStore();

  return (
    <div className="h-screen flex flex-col">
      <Header />
      <div className="flex-1 flex overflow-hidden">
        <div className="flex-1 flex flex-col">
          <div className="h-14 border-b flex items-center px-6 space-x-4">
            <ViewToggle currentView={currentView} onViewChange={switchView} />
          </div>
          <div className="flex-1 overflow-auto">
            {/* 视图内容将在后续任务中实现 */}
            <div className="p-4">
              当前视图: {currentView}
            </div>
          </div>
        </div>
        <SplitView>
          {/* AI 对话面板将在 Task 10 实现 */}
        </SplitView>
      </div>
    </div>
  );
}
```

**Step 3: 创建类型定义**

创建: `frontend/src/types/index.ts`

```typescript
// 通用类型
export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

// 申请类型
export interface JobApplication {
  id: number;
  companyId: number;
  jobTitle: string;
  jobDescription?: string;
  jobType?: string;
  workLocation?: string;
  salaryMin?: number;
  salaryMax?: number;
  salaryCurrency?: string;
  jobUrl?: string;
  status: 'WISHLIST' | 'APPLIED' | 'INTERVIEW' | 'OFFER' | 'REJECTED' | 'WITHDRAWN';
  applicationDate?: string;
  priority?: number;
  notes?: string;
  createdAt: string;
  updatedAt?: string;
  company?: Company;
}

// 公司类型
export interface Company {
  id: number;
  name: string;
  industry?: string;
  size?: string;
  location?: string;
  website?: string;
  description?: string;
  logoUrl?: string;
  createdAt: string;
  updatedAt?: string;
}

// 面试记录
export interface InterviewRecord {
  id: number;
  applicationId: number;
  interviewType?: string;
  interviewDate: string;
  interviewerName?: string;
  interviewerTitle?: string;
  durationMinutes?: number;
  status?: 'SCHEDULED' | 'COMPLETED' | 'CANCELLED';
  rating?: number;
  feedback?: string;
  technicalQuestions?: string;
  notes?: string;
  followUpRequired?: boolean;
  createdAt: string;
  updatedAt?: string;
}

// WebSocket 消息类型
export interface WebSocketMessage {
  type: 'CHAT' | 'STATUS' | 'ERROR';
  content: string;
  timestamp?: number;
}
```

**Step 4: 创建常量文件**

创建: `frontend/src/utils/constants.ts`

```typescript
// 状态映射
export const STATUS_CONFIG = {
  WISHLIST: { label: '愿望清单', color: 'gray' },
  APPLIED: { label: '已投递', color: 'blue' },
  INTERVIEW: { label: '面试中', color: 'purple' },
  OFFER: { label: '已offer', color: 'green' },
  REJECTED: { label: '已拒绝', color: 'red' },
  WITHDRAWN: { label: '已撤回', color: 'orange' },
} as const;

// 视图类型
export type ViewType = 'table' | 'board' | 'timeline' | 'calendar';

export const VIEW_CONFIG = {
  table: { label: '表格', icon: 'Table' },
  board: { label: '看板', icon: 'Columns' },
  timeline: { label: '时间线', icon: 'Clock' },
  calendar: { label: '日历', icon: 'Calendar' },
} as const;

// API 配置
export const API_CONFIG = {
  baseURL: 'http://localhost:8080/api/data',
  wsURL: 'ws://localhost:8080/api/ws/chat',
};
```

**Step 5: 提交基础架构**

```bash
git add .
git commit -m "feat: add basic architecture - types, constants, App routing"
```

---

## Task 3: 布局组件

**Files:**
- Create: `frontend/src/components/layout/SplitView.tsx`
- Create: `frontend/src/components/layout/Header.tsx`
- Create: `frontend/src/components/layout/ViewToggle.tsx`

**Step 1: 实现 SplitView 组件**

创建: `frontend/src/components/layout/SplitView.tsx`

```tsx
import { useState } from 'react';

export function SplitView({ children }: { children: React.ReactNode }) {
  const [panelWidth, setPanelWidth] = useState(400);
  const [isPanelOpen, setIsPanelOpen] = useState(true);
  const [isResizing, setIsResizing] = useState(false);

  const handleMouseDown = () => {
    setIsResizing(true);
  };

  const togglePanel = () => {
    setIsPanelOpen(!isPanelOpen);
  };

  return (
    <div className="flex h-screen overflow-hidden">
      {/* 左侧内容区 */}
      <div
        className="flex-1 h-full overflow-auto"
        style={{ width: isPanelOpen ? `calc(100% - ${panelWidth}px)` : '100%' }}
      >
        {children}
      </div>

      {/* 分隔条 */}
      {isPanelOpen && (
        <div
          className="w-1 bg-gray-200 hover:bg-blue-400 cursor-col-resize"
          onMouseDown={handleMouseDown}
        />
      )}

      {/* 右侧 AI 面板 */}
      {isPanelOpen && (
        <div
          className="h-full border-l border-gray-200"
          style={{ width: `${panelWidth}px` }}
        >
          {/* ChatPanel 内容将在 Task 10 实现 */}
          <div className="p-4">AI 对话面板（待实现）</div>
        </div>
      )}
    </div>
  );
}
```

**Step 2: 实现 Header 组件**

创建: `frontend/src/components/layout/Header.tsx`

```tsx
import { Bell, Settings, Search } from 'lucide-react';

export function Header() {
  return (
    <header className="h-16 border-b border-gray-200 flex items-center justify-between px-6">
      {/* Logo */}
      <div className="flex items-center space-x-2">
        <h1 className="text-xl font-semibold text-gray-900">Job Tracker</h1>
      </div>

      {/* 搜索 */}
      <div className="flex-1 max-w-md mx-8">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <input
            type="text"
            placeholder="搜索公司、职位..."
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      {/* 右侧操作 */}
      <div className="flex items-center space-x-4">
        <button className="p-2 hover:bg-gray-100 rounded-lg">
          <Bell className="w-5 h-5 text-gray-600" />
        </button>
        <button className="p-2 hover:bg-gray-100 rounded-lg">
          <Settings className="w-5 h-5 text-gray-600" />
        </button>
      </div>
    </header>
  );
}
```

**Step 3: 实现视图切换组件**

创建: `frontend/src/components/layout/ViewToggle.tsx`

```tsx
import { VIEW_CONFIG, type ViewType } from '@/utils/constants';

interface ViewToggleProps {
  currentView: ViewType;
  onViewChange: (view: ViewType) => void;
}

export function ViewToggle({ currentView, onViewChange }: ViewToggleProps) {
  const views = Object.entries(VIEW_CONFIG) as [ViewType, typeof VIEW_CONFIG[keyof ViewType]];

  return (
    <div className="flex items-center space-x-2 bg-gray-100 p-1 rounded-lg">
      {views.map(([key, config]) => (
        <button
          key={key}
          onClick={() => onViewChange(key)}
          className={`
            px-3 py-1.5 rounded-md text-sm font-medium transition-colors
            ${currentView === key
              ? 'bg-white text-gray-900 shadow-sm'
              : 'text-gray-600 hover:bg-gray-200'
            }
          `}
        >
          {config.label}
        </button>
      ))}
    </div>
  );
}
```

**Step 4: 提交布局组件**

```bash
git add .
git commit -m "feat: add layout components - SplitView, Header, ViewToggle"
```

---

## Task 4: 状态管理

**Files:**
- Create: `frontend/src/store/applicationStore.ts`
- Create: `frontend/src/store/chatStore.ts`
- Create: `frontend/src/store/uiStore.ts`

**Step 1: 实现 ApplicationStore**

创建: `frontend/src/store/applicationStore.ts`

```typescript
import { create } from 'zustand';
import axios from 'axios';
import apiClient from '@/services/api';
import type { JobApplication, Company, InterviewRecord } from '@/types';

interface ApplicationStore {
  // 状态
  applications: JobApplication[];
  companies: Company[];
  interviews: InterviewRecord[];
  currentView: 'table' | 'board' | 'timeline' | 'calendar';
  filters: {
    status?: string;
    priority?: number;
    keyword?: string;
  };
  loading: boolean;
  error: string | null;

  // 操作
  fetchApplications: () => Promise<void>;
  createApplication: (data: Partial<JobApplication>) => Promise<number>;
  updateApplication: (id: number, data: Partial<JobApplication>) => Promise<void>;
  deleteApplication: (id: number) => Promise<void>;
  switchView: (view: ApplicationStore['currentView']) => void;
  setFilters: (filters: Partial<ApplicationStore['filters']>) => void;
}

export const useApplicationStore = create<ApplicationStore>((set, get) => ({
  // 初始状态
  applications: [],
  companies: [],
  interviews: [],
  currentView: 'table',
  filters: {},
  loading: false,
  error: null,

  // 获取申请列表
  fetchApplications: async () => {
    set({ loading: true, error: null });
    try {
      const response = await apiClient.get<{ code: number; data: JobApplication[] }>('/applications');
      if (response.code === 200) {
        set({ applications: response.data });
      }
    } catch (error) {
      set({ error: '获取申请列表失败' });
    } finally {
      set({ loading: false });
    }
  },

  // 创建申请
  createApplication: async (data: Partial<JobApplication>) => {
    try {
      const response = await apiClient.post<{ code: number; data: number }>('/applications', data);
      if (response.code === 200) {
        await get().fetchApplications();
        return response.data;
      }
      throw new Error('创建失败');
    } catch (error) {
      set({ error: '创建申请失败' });
      throw error;
    }
  },

  // 更新申请
  updateApplication: async (id: number, data: Partial<JobApplication>) => {
    try {
      await apiClient.put(`/applications/${id}`, data);
      await get().fetchApplications();
    } catch (error) {
      set({ error: '更新失败' });
    }
  },

  // 删除申请
  deleteApplication: async (id: number) => {
    try {
      await apiClient.delete(`/applications/${id}`);
      await get().fetchApplications();
    } catch (error) {
      set({ error: '删除失败' });
    }
  },

  // 切换视图
  switchView: (view) => {
    set({ currentView: view });
  },

  // 设置筛选
  setFilters: (filters) => {
    set({ filters: { ...get().filters, ...filters } });
  },
}));
```

**Step 2: 实现 ChatStore**

创建: `frontend/src/store/chatStore.ts`

```typescript
import { create } from 'zustand';
import type { WebSocketMessage } from '@/types';

interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
}

interface ChatStore {
  messages: ChatMessage[];
  isConnected: boolean;
  isTyping: boolean;
  panelWidth: number;
  isPanelOpen: boolean;

  // 操作
  sendMessage: (content: string) => Promise<void>;
  clearHistory: () => void;
  togglePanel: () => void;
  setPanelWidth: (width: number) => void;
  connect: () => void;
  disconnect: () => void;
}

export const useChatStore = create<ChatStore>((set, get) => ({
  // 初始状态
  messages: [],
  isConnected: false,
  isTyping: false,
  panelWidth: 400,
  isPanelOpen: true,

  // 发送消息
  sendMessage: async (content: string) => {
    // WebSocket 实现在 Task 10
    set({ isTyping: true });

    // TODO: WebSocket 发送
    const userMessage: ChatMessage = {
      role: 'user',
      content,
      timestamp: Date.now(),
    };
    set({ messages: [...get().messages, userMessage] });

    // 模拟 AI 回复（临时）
    setTimeout(() => {
      const aiMessage: ChatMessage = {
        role: 'assistant',
        content: '收到：' + content,
        timestamp: Date.now(),
      };
      set({ messages: [...get().messages, aiMessage], isTyping: false });
    }, 1000);
  },

  // 清空历史
  clearHistory: () => {
    set({ messages: [] });
  },

  // 切换面板
  togglePanel: () => {
    set({ isPanelOpen: !get().isPanelOpen });
  },

  // 设置面板宽度
  setPanelWidth: (width) => {
    set({ panelWidth: width });
  },

  // 连接 WebSocket
  connect: () => {
    // Task 10 实现
    set({ isConnected: true });
  },

  // 断开连接
  disconnect: () => {
    set({ isConnected: false });
  },
}));
```

**Step 3: 实现 UIStore**

创建: `frontend/src/store/uiStore.ts`

```typescript
import { create } from 'zustand';

interface UIStore {
  sidebarCollapsed: boolean;
  theme: 'light' | 'dark';
  loading: boolean;

  toggleSidebar: () => void;
  setTheme: (theme: UIStore['theme']) => void;
  setLoading: (loading: boolean) => void;
}

export const useUIStore = create<UIStore>((set) => ({
  // 初始状态
  sidebarCollapsed: false,
  theme: 'light',
  loading: false,

  // 切换侧边栏
  toggleSidebar: () => {
    set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed }));
  },

  // 设置主题
  setTheme: (theme) => {
    set({ theme });
  },

  // 设置加载状态
  setLoading: (loading) => {
    set({ loading });
  },
}));
```

**Step 4: 提交状态管理**

```bash
git add .
git commit -m "feat: add Zustand stores for application, chat, and UI state"
```

---

## Task 5: API 服务层

**Files:**
- Create: `frontend/src/services/api.ts`
- Create: `frontend/src/services/dataApi.ts`
- Create: `frontend/src/services/websocket.ts`

**Step 1: 配置 Axios 客户端**

创建: `frontend/src/services/api.ts`

```typescript
import axios from 'axios';
import type { Result } from '@/types';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api/data',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
apiClient.interceptors.request.use((config) => {
  // 可以添加 token
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

**Step 2: 实现 HTTP API 服务**

创建: `frontend/src/services/dataApi.ts`

```typescript
import apiClient from './api';
import type { JobApplication, Company, InterviewRecord } from '@/types';

export const dataApi = {
  // 申请相关
  getApplications: () => Promise<JobApplication[]>,
  getApplication: (id: number) => Promise<JobApplication>,
  createApplication: (data: Partial<JobApplication>) => Promise<number>,
  updateApplication: (id: number, data: Partial<JobApplication>) => Promise<void>,
  deleteApplication: (id: number) => Promise<void>,

  // 公司相关
  getCompanies: () => Promise<Company[]>,
  getCompany: (id: number) => Promise<Company>,
  createCompany: (data: Partial<Company>) => Promise<number>,

  // 面试相关
  getInterviews: () => Promise<InterviewRecord[]>,
  getInterviewsByApplication: (applicationId: number) => Promise<InterviewRecord[]>,
  createInterview: (data: Partial<InterviewRecord>) => Promise<number>,

  // 统计
  getStatistics: () => Promise<any[]>,
};

// 实现示例
dataApi.getApplications = async () => {
  const response = await apiClient.get<Result<JobApplication[]>>('/applications');
  return response.data.data;
};
```

**Step 3: 提交 API 服务**

```bash
git add .
git commit -m "feat: add API service layer with HTTP client"
```

---

## Task 6: 表格视图

**Files:**
- Create: `frontend/src/components/views/TableView.tsx`

**Step 1: 实现基础表格**

创建: `frontend/src/components/views/TableView.tsx`

```tsx
import { useApplicationStore } from '@/store/applicationStore';
import { STATUS_CONFIG } from '@/utils/constants';

export function TableView() {
  const { applications, loading } = useApplicationStore();

  if (loading) {
    return <div>加载中...</div>;
  }

  return (
    <div className="p-4">
      <table className="w-full border-collapse">
        <thead>
          <tr className="bg-gray-50">
            <th className="px-4 py-2 text-left">公司</th>
            <th className="px-4 py-2 text-left">职位</th>
            <th className="px-4 py-2 text-left">状态</th>
            <th className="px-4 py-2 text-left">日期</th>
            <th className="px-4 py-2 text-left">优先级</th>
          </tr>
        </thead>
        <tbody>
          {applications.map((app) => (
            <tr key={app.id} className="border-b hover:bg-gray-50">
              <td className="px-4 py-3">{app.company?.name}</td>
              <td className="px-4 py-3">{app.jobTitle}</td>
              <td className="px-4 py-3">
                <span className={`px-2 py-1 rounded text-xs text-white ${
                  STATUS_CONFIG[app.status].color
                }`}>
                  {STATUS_CONFIG[app.status].label}
                </span>
              </td>
              <td className="px-4 py-3">{app.applicationDate || '-'}</td>
              <td className="px-4 py-3">{app.priority || '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

**Step 2: 提交表格视图**

```bash
git add .
git commit -m "feat: add table view with status display"
```

---

## Task 7-13: 其他任务（简化）

由于篇幅限制，其他任务概要：

### Task 7-9: 其他视图
- **BoardView**: 使用 @dnd-kit 实现拖拽看板
- **TimelineView**: 垂直时间线展示
- **CalendarView**: react-big-calendar 实现日历

### Task 10: AI 对话系统
- **ChatPanel**: 消息列表、输入框、WebSocket 集成
- **流式显示**: 打字机效果

### Task 11-13: 增强
- 快捷操作按钮
- 响应式适配
- 测试和部署

---

## 接口需求文档

文件位置：`frontend/BACKEND_TODO.md`

```markdown
# 后端接口需求 TODO

## 当前状态
- ✅ 16 个 HTTP REST API 已提供
- ✅ WebSocket 聊天接口已提供
- ✅ 9 个 AI 工具方法已实现

## 待确认需求
（前端开发过程中，如发现接口缺失，将在此记录）

---

## 接口需求模板

### 接口 #1: 批量更新状态

**优先级**: P0（阻塞功能）
**用途**: 看板视图拖拽后批量更新

**详情**: 见设计文档第 10.2 节
```

---

**下一步**:
1. ✅ 设计文档已完成
2. ⏭ 等待执行实现计划
