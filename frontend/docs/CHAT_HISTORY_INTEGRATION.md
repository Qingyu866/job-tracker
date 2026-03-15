# AI 聊天历史持久化 - 前端集成文档

**文档版本**: 1.0
**创建日期**: 2026-03-13
**维护者**: Frontend Team

---

## 一、功能概述

### 1.1 背景

当前问题：
- 用户每次刷新页面，AI对话历史丢失
- 无法查看历史对话记录
- 无法追溯AI的工具调用情况

解决方案：
- 后端已实现聊天历史持久化（3个数据表）
- 提供 REST API 查询历史记录
- 前端需集成API实现历史加载和会话管理

### 1.2 目标功能

- 页面加载时恢复历史对话
- 支持多会话管理
- 支持历史对话查询和切换
- 支持删除会话

---

## 二、API 接口文档

### 2.1 基础信息

| 项目 | 内容 |
|------|------|
| Base URL | `http://localhost:8080/api/chat` |
| Content-Type | `application/json` |

### 2.2 获取所有会话列表

**请求**：

```http
GET /api/chat/sessions
```

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "sessionKey": "default-session",
      "title": "求职咨询对话",
      "messageCount": 10,
      "createdAt": "2026-03-13T10:00:00",
      "updatedAt": "2026-03-13T12:30:00",
      "deleted": 0
    }
  ]
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 会话ID |
| sessionKey | string | 会话唯一标识 |
| title | string | 会话标题 |
| messageCount | number | 消息数量 |
| createdAt | string | 创建时间 |
| updatedAt | string | 更新时间 |

---

### 2.3 获取会话消息历史

**请求**：

```http
GET /api/chat/sessions/{sessionKey}/messages
```

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sessionKey | string | 是 | 会话唯一标识 |

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "sessionId": 1,
      "role": "USER",
      "content": "帮我查看一下所有的求职申请",
      "createdAt": "2026-03-13T10:00:00"
    },
    {
      "id": 2,
      "sessionId": 1,
      "role": "ASSISTANT",
      "content": "好的，您目前有 5 条求职申请记录...",
      "createdAt": "2026-03-13T10:00:05"
    }
  ]
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 消息ID |
| sessionId | number | 会话ID |
| role | string | 角色：USER/ASSISTANT/SYSTEM |
| content | string | 消息内容 |
| createdAt | string | 创建时间 |

---

### 2.4 删除会话

**请求**：

```http
DELETE /api/chat/sessions/{sessionKey}
```

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sessionKey | string | 是 | 会话唯一标识 |

**响应示例（成功）**：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": true
}
```

---

### 2.5 获取工具调用记录

**请求**：

```http
GET /api/chat/messages/{messageId}/tool-calls
```

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| messageId | number | 是 | 消息ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "messageId": 2,
      "toolName": "getAllApplications",
      "toolInput": "{}",
      "toolOutput": "[{\"id\":1,\"jobTitle\":\"Java工程师\"...}]",
      "status": "SUCCESS",
      "errorMessage": null,
      "executionTimeMs": 45,
      "createdAt": "2026-03-13T10:00:05"
    }
  ]
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 记录ID |
| messageId | number | 关联的消息ID |
| toolName | string | 工具名称 |
| toolInput | string | 工具入参（JSON字符串） |
| toolOutput | string | 工具输出（JSON字符串） |
| status | string | 状态：SUCCESS/FAILURE |
| errorMessage | string | 错误信息 |
| executionTimeMs | number | 执行耗时（毫秒） |

---

## 三、类型定义

### 3.1 新增类型文件

在 `src/types/chat.ts` 中添加：

```typescript
/**
 * 聊天会话
 */
export interface ChatSession {
  id: number;
  sessionKey: string;
  title: string | null;
  messageCount: number;
  createdAt: string;
  updatedAt: string;
  deleted: number;
}

/**
 * 服务端聊天消息
 */
export interface ServerChatMessage {
  id: number;
  sessionId: number;
  role: 'USER' | 'ASSISTANT' | 'SYSTEM';
  content: string;
  createdAt: string;
}

/**
 * 工具调用记录
 */
export interface ToolCallRecord {
  id: number;
  messageId: number;
  toolName: string;
  toolInput: string;
  toolOutput: string;
  status: 'SUCCESS' | 'FAILURE';
  errorMessage: string | null;
  executionTimeMs: number | null;
  createdAt: string;
}

/**
 * API 响应包装
 */
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}
```

---

## 四、服务层实现

### 4.1 创建聊天历史 API 服务

创建文件 `src/services/chatApi.ts`：

```typescript
import apiClient from './api';
import type { ApiResponse, ChatSession, ServerChatMessage, ToolCallRecord } from '@/types/chat';

const CHAT_BASE_URL = 'http://localhost:8080/api/chat';

/**
 * 聊天历史 API 服务
 */
export const chatApi = {
  /**
   * 获取所有会话列表
   */
  async getSessions(): Promise<ChatSession[]> {
    const response = await apiClient.get<ApiResponse<ChatSession[]>>(
      `${CHAT_BASE_URL}/sessions`
    );
    return response.data.data;
  },

  /**
   * 获取会话消息历史
   * @param sessionKey 会话标识
   */
  async getSessionMessages(sessionKey: string): Promise<ServerChatMessage[]> {
    const response = await apiClient.get<ApiResponse<ServerChatMessage[]>>(
      `${CHAT_BASE_URL}/sessions/${sessionKey}/messages`
    );
    return response.data.data;
  },

  /**
   * 删除会话
   * @param sessionKey 会话标识
   */
  async deleteSession(sessionKey: string): Promise<boolean> {
    const response = await apiClient.delete<ApiResponse<boolean>>(
      `${CHAT_BASE_URL}/sessions/${sessionKey}`
    );
    return response.data.data;
  },

  /**
   * 获取消息的工具调用记录
   * @param messageId 消息ID
   */
  async getToolCallRecords(messageId: number): Promise<ToolCallRecord[]> {
    const response = await apiClient.get<ApiResponse<ToolCallRecord[]>>(
      `${CHAT_BASE_URL}/messages/${messageId}/tool-calls`
    );
    return response.data.data;
  },
};
```

---

## 五、Store 改造

### 5.1 改造 chatStore.ts

需要修改 `src/store/chatStore.ts`，添加以下功能：

```typescript
import { create } from 'zustand';
import { webSocketManager } from '@/lib/webSocketManager';
import { chatApi } from '@/services/chatApi';
import type { ChatSession, ServerChatMessage } from '@/types/chat';

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
  id?: number; // 新增：服务端消息ID
}

interface ChatStore {
  // 现有状态
  messages: ChatMessage[];
  isConnected: boolean;
  isTyping: boolean;
  panelWidth: number;
  isPanelOpen: boolean;

  // 新增状态
  sessions: ChatSession[];
  currentSessionKey: string;
  isLoadingHistory: boolean;

  // 现有操作
  sendMessage: (content: string) => Promise<void>;
  clearHistory: () => void;
  togglePanel: () => void;
  setPanelWidth: (width: number) => void;
  connect: () => void;
  disconnect: () => void;

  // 新增操作
  loadSessions: () => Promise<void>;
  loadHistory: (sessionKey: string) => Promise<void>;
  switchSession: (sessionKey: string) => Promise<void>;
  deleteSession: (sessionKey: string) => Promise<void>;
  createNewSession: () => void;
}

// 默认会话标识
const DEFAULT_SESSION_KEY = 'default-session';

export const useChatStore = create<ChatStore>((set, get) => ({
  // 初始状态
  messages: [],
  isConnected: false,
  isTyping: false,
  panelWidth: 400,
  isPanelOpen: true,
  sessions: [],
  currentSessionKey: DEFAULT_SESSION_KEY,
  isLoadingHistory: false,

  // ... 现有方法保持不变 ...

  // 新增：加载会话列表
  loadSessions: async () => {
    try {
      const sessions = await chatApi.getSessions();
      set({ sessions });
    } catch (error) {
      console.error('[ChatStore] 加载会话列表失败:', error);
    }
  },

  // 新增：加载历史消息
  loadHistory: async (sessionKey: string) => {
    set({ isLoadingHistory: true });
    try {
      const serverMessages = await chatApi.getSessionMessages(sessionKey);

      // 转换为前端消息格式
      const messages: ChatMessage[] = serverMessages.map((msg) => ({
        id: msg.id,
        role: msg.role.toLowerCase() as 'user' | 'assistant',
        content: msg.content,
        timestamp: new Date(msg.createdAt).getTime(),
      }));

      set({
        messages,
        currentSessionKey: sessionKey,
        isLoadingHistory: false
      });
    } catch (error) {
      console.error('[ChatStore] 加载历史消息失败:', error);
      set({ isLoadingHistory: false });
    }
  },

  // 新增：切换会话
  switchSession: async (sessionKey: string) => {
    await get().loadHistory(sessionKey);
  },

  // 新增：删除会话
  deleteSession: async (sessionKey: string) => {
    try {
      await chatApi.deleteSession(sessionKey);
      // 刷新会话列表
      await get().loadSessions();

      // 如果删除的是当前会话，切换到默认会话
      if (get().currentSessionKey === sessionKey) {
        set({ messages: [], currentSessionKey: DEFAULT_SESSION_KEY });
      }
    } catch (error) {
      console.error('[ChatStore] 删除会话失败:', error);
    }
  },

  // 新增：创建新会话
  createNewSession: () => {
    const newSessionKey = `session-${Date.now()}`;
    set({
      messages: [],
      currentSessionKey: newSessionKey
    });
  },
}));
```

---

## 六、WebSocket 改造

### 6.1 修改消息发送

在发送 WebSocket 消息时，需要携带 `sessionId` 字段：

```typescript
// 在 webSocketManager 或 chatStore 中修改发送逻辑
sendMessage: async (content: string) => {
  try {
    const userMessage: ChatMessage = {
      role: 'user',
      content,
      timestamp: Date.now(),
    };
    set({ messages: [...get().messages, userMessage], isTyping: true });

    // 发送消息时携带 sessionId
    webSocketManager.send({
      type: 'CHAT',
      content,
      sessionId: get().currentSessionKey,
    });
  } catch (error) {
    // ... 错误处理
  }
},
```

---

## 七、UI 组件实现

### 7.1 会话列表组件

创建文件 `src/components/chat/SessionList.tsx`：

```tsx
import { Plus, Trash2, MessageSquare } from 'lucide-react';
import { useChatStore } from '@/store/chatStore';
import type { ChatSession } from '@/types/chat';

export function SessionList() {
  const { sessions, currentSessionKey, switchSession, deleteSession, createNewSession } = useChatStore();

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="flex flex-col h-full">
      {/* 新建会话按钮 */}
      <div className="p-2 border-b border-paper-200">
        <button
          onClick={createNewSession}
          className="w-full flex items-center justify-center gap-2 px-3 py-2 bg-accent-amber text-paper-900 rounded-lg hover:bg-accent-amber/90 transition-colors"
        >
          <Plus className="w-4 h-4" />
          <span className="text-sm font-medium">新对话</span>
        </button>
      </div>

      {/* 会话列表 */}
      <div className="flex-1 overflow-y-auto">
        {sessions.length === 0 ? (
          <div className="p-4 text-center text-paper-400 text-sm">
            暂无历史对话
          </div>
        ) : (
          sessions.map((session) => (
            <SessionItem
              key={session.sessionKey}
              session={session}
              isActive={session.sessionKey === currentSessionKey}
              onSelect={() => switchSession(session.sessionKey)}
              onDelete={() => deleteSession(session.sessionKey)}
              formatDate={formatDate}
            />
          ))
        )}
      </div>
    </div>
  );
}

interface SessionItemProps {
  session: ChatSession;
  isActive: boolean;
  onSelect: () => void;
  onDelete: () => void;
  formatDate: (date: string) => string;
}

function SessionItem({ session, isActive, onSelect, onDelete, formatDate }: SessionItemProps) {
  return (
    <div
      onClick={onSelect}
      className={`group flex items-center gap-2 px-3 py-2 cursor-pointer transition-colors ${
        isActive
          ? 'bg-paper-200 border-l-2 border-accent-amber'
          : 'hover:bg-paper-100'
      }`}
    >
      <MessageSquare className="w-4 h-4 text-paper-500 flex-shrink-0" />
      <div className="flex-1 min-w-0">
        <div className="text-sm text-paper-700 truncate">
          {session.title || `对话 ${session.sessionKey.slice(0, 8)}`}
        </div>
        <div className="text-xs text-paper-400">
          {session.messageCount} 条消息 · {formatDate(session.updatedAt)}
        </div>
      </div>
      <button
        onClick={(e) => {
          e.stopPropagation();
          onDelete();
        }}
        className="opacity-0 group-hover:opacity-100 p-1 hover:bg-paper-200 rounded transition-opacity"
      >
        <Trash2 className="w-4 h-4 text-paper-500" />
      </button>
    </div>
  );
}
```

### 7.2 修改 ChatPanel 组件

在 `ChatPanel.tsx` 中添加会话列表切换功能：

```tsx
import { useEffect, useRef, useState } from 'react';
import { X, ChevronRight, Bot, History, ChevronLeft } from 'lucide-react';
import { useChatStore } from '@/store/chatStore';
import { ChatMessage } from './ChatMessage';
import { ChatInput } from './ChatInput';
import { SessionList } from './SessionList';
import './ChatPanel.css';

interface ChatPanelProps {
  onClose?: () => void;
}

export function ChatPanel({ onClose }: ChatPanelProps) {
  const {
    messages,
    sendMessage,
    isTyping,
    isConnected,
    isLoadingHistory,
    loadSessions,
    currentSessionKey,
  } = useChatStore();

  const [showSessions, setShowSessions] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // 初始化时加载会话列表和历史
  useEffect(() => {
    loadSessions();
    // 可选：自动加载上次会话
  }, [loadSessions]);

  const handleSend = async (content: string) => {
    await sendMessage(content);
  };

  return (
    <div className="flex h-full bg-paper-50">
      {/* 会话列表面板（可折叠） */}
      {showSessions && (
        <div className="w-48 border-r border-paper-200 flex-shrink-0">
          <SessionList />
        </div>
      )}

      {/* 主聊天区域 */}
      <div className="flex flex-col flex-1 min-w-0">
        {/* 头部 */}
        <div className="p-4 border-b border-paper-200">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <button
                onClick={() => setShowSessions(!showSessions)}
                className="p-1.5 hover:bg-paper-200 rounded-lg text-paper-600 transition-colors"
                title={showSessions ? '隐藏会话列表' : '显示会话列表'}
              >
                {showSessions ? (
                  <ChevronLeft className="w-5 h-5" />
                ) : (
                  <History className="w-5 h-5" />
                )}
              </button>
              <Bot className="w-5 h-5 text-paper-600" />
              <h3 className="font-serif text-paper-700">AI 助手</h3>
            </div>
            {/* ... 其他头部内容 ... */}
          </div>
        </div>

        {/* 消息列表 */}
        <div className="flex-1 overflow-y-auto p-4">
          {isLoadingHistory ? (
            <div className="flex items-center justify-center h-full text-paper-400">
              <div className="text-sm">加载历史消息中...</div>
            </div>
          ) : (
            <>
              {/* ... 现有消息渲染逻辑 ... */}
            </>
          )}
        </div>

        {/* 输入框 */}
        <div className="p-4 border-t border-paper-200">
          <ChatInput onSend={handleSend} disabled={!isConnected} />
        </div>
      </div>
    </div>
  );
}
```

---

## 八、实施步骤

### Phase 1: 基础设施（预计 1 小时）

- [ ] 创建 `src/types/chat.ts` 类型文件
- [ ] 创建 `src/services/chatApi.ts` API 服务
- [ ] 验证 API 调用正常

### Phase 2: Store 改造（预计 1.5 小时）

- [ ] 修改 `chatStore.ts` 添加新状态和方法
- [ ] 修改 WebSocket 消息发送逻辑
- [ ] 添加历史消息加载功能

### Phase 3: UI 组件（预计 2 小时）

- [ ] 创建 `SessionList.tsx` 会话列表组件
- [ ] 修改 `ChatPanel.tsx` 集成会话列表
- [ ] 添加加载状态和空状态处理
- [ ] 样式调整

### Phase 4: 测试与优化（预计 1 小时）

- [ ] 功能测试
- [ ] 边界条件处理
- [ ] 性能优化（消息列表虚拟滚动，可选）

---

## 九、注意事项

### 9.1 消息格式转换

服务端消息 `role` 为大写（USER/ASSISTANT），前端为小写（user/assistant），需要转换。

### 9.2 会话标识

- 前端需要生成唯一的 `sessionKey`
- 建议使用 `session-${timestamp}` 格式
- 默认会话使用 `default-session`

### 9.3 WebSocket 消息格式

发送消息时需要携带 `sessionId` 字段：

```json
{
  "type": "CHAT",
  "content": "用户消息内容",
  "sessionId": "session-1710123456789"
}
```

### 9.4 错误处理

- API 调用失败时显示友好提示
- 网络断开时保留本地消息
- 重新连接后可选择同步

---

## 十、文件清单

| 文件 | 类型 | 说明 |
|------|------|------|
| `src/types/chat.ts` | 新增 | 聊天相关类型定义 |
| `src/services/chatApi.ts` | 新增 | 聊天历史 API 服务 |
| `src/store/chatStore.ts` | 修改 | 添加会话管理功能 |
| `src/components/chat/SessionList.tsx` | 新增 | 会话列表组件 |
| `src/components/chat/ChatPanel.tsx` | 修改 | 集成会话列表 |

---

**文档版本**: 1.0
**最后更新**: 2026-03-13
