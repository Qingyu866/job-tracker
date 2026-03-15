# 聊天历史 API 接口文档

**文档版本**: 1.0
**创建日期**: 2026-03-13
**维护者**: Backend Team

---

## 一、接口概览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/chat/sessions` | 获取所有会话列表 |
| GET | `/api/chat/sessions/{sessionKey}/messages` | 获取会话消息历史 |
| DELETE | `/api/chat/sessions/{sessionKey}` | 删除会话 |
| GET | `/api/chat/messages/{messageId}/tool-calls` | 获取工具调用记录 |

---

## 二、通用说明

### 2.1 基础URL

```
http://localhost:8080/api/chat
```

### 2.2 响应格式

所有接口统一返回格式：

```json
{
  "code": 200,
  "message": "操作描述",
  "data": { ... }
}
```

### 2.3 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 三、接口详情

### 3.1 获取所有会话列表

获取当前用户的所有聊天会话。

**请求**

```http
GET /api/chat/sessions
```

**响应**

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
    },
    {
      "id": 2,
      "sessionKey": "session-1710315600000",
      "title": null,
      "messageCount": 3,
      "createdAt": "2026-03-13T14:00:00",
      "updatedAt": "2026-03-13T14:15:00",
      "deleted": 0
    }
  ]
}
```

**字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 会话ID（数据库主键） |
| sessionKey | string | 会话唯一标识（用于API调用） |
| title | string \| null | 会话标题（可AI生成，当前为null） |
| messageCount | number | 消息总数 |
| createdAt | string | 创建时间（ISO 8601格式） |
| updatedAt | string | 最后更新时间（ISO 8601格式） |
| deleted | number | 逻辑删除标记（0:正常, 1:已删除） |

**调用示例**

```typescript
// TypeScript
const response = await fetch('http://localhost:8080/api/chat/sessions');
const result = await response.json();
const sessions = result.data;
```

```bash
# cURL
curl http://localhost:8080/api/chat/sessions
```

---

### 3.2 获取会话消息历史

获取指定会话的所有消息，按时间升序排列。

**请求**

```http
GET /api/chat/sessions/{sessionKey}/messages
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sessionKey | string | 是 | 会话唯一标识 |

**响应**

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
      "content": "好的，您目前有 5 条求职申请记录，分别是：\n\n1. **Java高级工程师** - 字节跳动 - 面试中\n2. **前端开发工程师** - 阿里巴巴 - 已投递\n...",
      "createdAt": "2026-03-13T10:00:05"
    },
    {
      "id": 3,
      "sessionId": 1,
      "role": "USER",
      "content": "帮我给字节跳动添加一个面试记录",
      "createdAt": "2026-03-13T10:01:00"
    }
  ]
}
```

**字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 消息ID（数据库主键） |
| sessionId | number | 所属会话ID |
| role | string | 角色：`USER`（用户）或 `ASSISTANT`（AI） |
| content | string | 消息内容（AI消息支持Markdown格式） |
| createdAt | string | 创建时间（ISO 8601格式） |

**调用示例**

```typescript
// TypeScript
async function loadChatHistory(sessionKey: string) {
  const response = await fetch(
    `http://localhost:8080/api/chat/sessions/${sessionKey}/messages`
  );
  const result = await response.json();

  // 转换为前端消息格式
  return result.data.map(msg => ({
    id: msg.id,
    role: msg.role.toLowerCase(), // USER -> user, ASSISTANT -> assistant
    content: msg.content,
    timestamp: new Date(msg.createdAt).getTime()
  }));
}
```

```bash
# cURL
curl http://localhost:8080/api/chat/sessions/default-session/messages
```

---

### 3.3 删除会话

删除指定会话及其所有消息（逻辑删除）。

**请求**

```http
DELETE /api/chat/sessions/{sessionKey}
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sessionKey | string | 是 | 会话唯一标识 |

**响应（成功）**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": true
}
```

**响应（失败 - 会话不存在）**

```json
{
  "code": 404,
  "message": "会话不存在",
  "data": null
}
```

**调用示例**

```typescript
// TypeScript
async function deleteChatSession(sessionKey: string) {
  const response = await fetch(
    `http://localhost:8080/api/chat/sessions/${sessionKey}`,
    { method: 'DELETE' }
  );
  const result = await response.json();
  return result.code === 200;
}
```

```bash
# cURL
curl -X DELETE http://localhost:8080/api/chat/sessions/session-1710315600000
```

---

### 3.4 获取工具调用记录

获取指定AI消息的工具调用记录（用于调试和分析AI行为）。

**请求**

```http
GET /api/chat/messages/{messageId}/tool-calls
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| messageId | number | 是 | 消息ID（AI消息） |

**响应**

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
      "toolOutput": "[{\"id\":1,\"jobTitle\":\"Java高级工程师\",\"companyId\":1,\"status\":\"INTERVIEW\"}...]",
      "status": "SUCCESS",
      "errorMessage": null,
      "executionTimeMs": 45,
      "createdAt": "2026-03-13T10:00:05"
    },
    {
      "id": 2,
      "messageId": 2,
      "toolName": "getApplicationById",
      "toolInput": "{\"applicationId\":1}",
      "toolOutput": "{\"id\":1,\"jobTitle\":\"Java高级工程师\"...}",
      "status": "SUCCESS",
      "errorMessage": null,
      "executionTimeMs": 12,
      "createdAt": "2026-03-13T10:00:05"
    }
  ]
}
```

**字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 记录ID |
| messageId | number | 关联的AI消息ID |
| toolName | string | 工具名称（对应后端工具方法名） |
| toolInput | string | 工具入参（JSON字符串） |
| toolOutput | string | 工具输出（JSON字符串） |
| status | string | 状态：`SUCCESS` 或 `FAILURE` |
| errorMessage | string \| null | 错误信息（仅FAILURE时有值） |
| executionTimeMs | number | 执行耗时（毫秒） |
| createdAt | string | 创建时间 |

**可用工具列表**

| 工具名称 | 说明 |
|----------|------|
| getAllApplications | 获取所有求职申请 |
| getApplicationById | 根据ID获取申请 |
| createApplication | 创建求职申请 |
| updateApplicationStatus | 更新申请状态 |
| getAllCompanies | 获取所有公司 |
| getCompanyById | 根据ID获取公司 |
| createCompany | 创建公司 |
| getAllInterviews | 获取所有面试记录 |
| getInterviewById | 根据ID获取面试记录 |
| createInterview | 创建面试记录 |

**调用示例**

```typescript
// TypeScript
async function getToolCalls(messageId: number) {
  const response = await fetch(
    `http://localhost:8080/api/chat/messages/${messageId}/tool-calls`
  );
  const result = await response.json();

  return result.data.map(record => ({
    ...record,
    toolInput: JSON.parse(record.toolInput),
    toolOutput: JSON.parse(record.toolOutput)
  }));
}
```

```bash
# cURL
curl http://localhost:8080/api/chat/messages/2/tool-calls
```

---

## 四、WebSocket 消息格式

### 4.1 发送消息格式

客户端发送消息时，需要携带 `sessionId` 字段：

```json
{
  "type": "CHAT",
  "content": "帮我查看所有求职申请",
  "sessionId": "default-session"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 是 | 消息类型：`CHAT` 或 `HEARTBEAT` |
| content | string | 是 | 消息内容 |
| sessionId | string | 否 | 会话标识（不传则使用WebSocket sessionId） |

### 4.2 接收消息格式

服务端响应格式：

```json
{
  "type": "CHAT",
  "content": "好的，您目前有 5 条求职申请记录...",
  "timestamp": 1710315605000
}
```

---

## 五、前端集成建议

### 5.1 封装 API 服务

```typescript
// src/services/chatApi.ts
import apiClient from './api';

const CHAT_BASE = 'http://localhost:8080/api/chat';

export const chatApi = {
  getSessions: () =>
    apiClient.get(`${CHAT_BASE}/sessions`).then(r => r.data.data),

  getMessages: (sessionKey: string) =>
    apiClient.get(`${CHAT_BASE}/sessions/${sessionKey}/messages`).then(r => r.data.data),

  deleteSession: (sessionKey: string) =>
    apiClient.delete(`${CHAT_BASE}/sessions/${sessionKey}`).then(r => r.data.data),

  getToolCalls: (messageId: number) =>
    apiClient.get(`${CHAT_BASE}/messages/${messageId}/tool-calls`).then(r => r.data.data),
};
```

### 5.2 消息格式转换

```typescript
// 服务端格式转前端格式
function toFrontendMessage(serverMsg: ServerChatMessage): ChatMessage {
  return {
    id: serverMsg.id,
    role: serverMsg.role.toLowerCase() as 'user' | 'assistant',
    content: serverMsg.content,
    timestamp: new Date(serverMsg.createdAt).getTime()
  };
}
```

### 5.3 页面加载时恢复历史

```typescript
// 在组件挂载时加载历史
useEffect(() => {
  const loadHistory = async () => {
    const sessionKey = 'default-session'; // 或从本地存储读取
    const messages = await chatApi.getMessages(sessionKey);
    setMessages(messages.map(toFrontendMessage));
  };
  loadHistory();
}, []);
```

---

**文档版本**: 1.0
**最后更新**: 2026-03-13
