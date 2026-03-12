# 求职追踪应用 - 前端对接接口文档

**后端地址**: `http://localhost:8080`
**API 版本**: v1.0
**文档更新**: 2026-03-11

---

## 📋 目录

1. [HTTP REST API](#1-http-rest-api)
2. [WebSocket 接口](#2-websocket-接口)
3. [AI 工具方法](#3-ai-工具方法)
4. [数据模型](#4-数据模型)
5. [错误处理](#5-错误处理)
6. [使用示例](#6-使用示例)

---

## 1. HTTP REST API

### 基础信息

- **Base URL**: `http://localhost:8080/api/data`
- **Content-Type**: `application/json`
- **响应格式**: 统一的 Result 对象

### 统一响应格式

```typescript
interface Result<T> {
  code: number;        // 200 表示成功，其他表示失败
  message: string;     // 响应消息
  data: T;            // 响应数据
}
```

---

### 1.1 求职申请相关接口

#### 1.1.1 获取所有求职申请

```
GET /api/data/applications
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "companyId": 1,
      "jobTitle": "后端工程师",
      "status": "APPLIED",
      "jobType": "FULL_TIME",
      "workLocation": "北京",
      "salaryMin": 20000,
      "salaryMax": 35000,
      "applicationDate": "2026-03-10",
      "priority": 8,
      "createdAt": "2026-03-10T10:00:00"
    }
  ]
}
```

#### 1.1.2 获取申请详情

```
GET /api/data/applications/{id}
```

**路径参数**:
- `id`: 申请 ID

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "companyId": 1,
    "jobTitle": "后端工程师",
    "jobDescription": "负责后端系统开发",
    "status": "APPLIED",
    "jobType": "FULL_TIME",
    "workLocation": "北京",
    "salaryMin": 20000,
    "salaryMax": 35000,
    "jobUrl": "https://example.com/job/123",
    "applicationDate": "2026-03-10",
    "priority": 8,
    "notes": "重点跟进",
    "createdAt": "2026-03-10T10:00:00"
  }
}
```

#### 1.1.3 分页查询申请

```
GET /api/data/applications/page?pageNum=1&pageSize=10&status=APPLIED
```

**查询参数**:
- `pageNum`: 当前页（默认 1）
- `pageSize`: 每页大小（默认 10）
- `status`: 申请状态（可选）

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [...],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  }
}
```

#### 1.1.4 按状态查询申请

```
GET /api/data/applications/status/{status}
```

**路径参数**:
- `status`: 状态值（WISHLIST/APPLIED/INTERVIEW/OFFER/REJECTED/WITHDRAWN）

#### 1.1.5 更新申请状态

```
PUT /api/data/applications/{id}/status?status=INTERVIEW
```

**路径参数**:
- `id`: 申请 ID

**查询参数**:
- `status`: 新状态

**响应示例**:
```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": null
}
```

#### 1.1.6 创建求职申请

```
POST /api/data/applications
Content-Type: application/json
```

**请求体**:
```json
{
  "companyId": 1,
  "jobTitle": "前端工程师",
  "jobDescription": "负责前端开发",
  "jobType": "FULL_TIME",
  "workLocation": "上海",
  "salaryMin": 18000,
  "salaryMax": 30000,
  "jobUrl": "https://example.com/job/456",
  "status": "WISHLIST",
  "priority": 7,
  "notes": "备选方案"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": 2
}
```

#### 1.1.7 获取高优先级申请

```
GET /api/data/applications/high-priority
```

---

### 1.2 面试记录相关接口

#### 1.2.1 获取所有面试记录

```
GET /api/data/interviews
```

#### 1.2.2 获取申请的面试记录

```
GET /api/data/interviews/application/{applicationId}
```

**路径参数**:
- `applicationId`: 申请 ID

#### 1.2.3 获取即将进行的面试

```
GET /api/data/interviews/upcoming
```

#### 1.2.4 创建面试记录

```
POST /api/data/interviews
Content-Type: application/json
```

**请求体**:
```json
{
  "applicationId": 1,
  "interviewType": "TECHNICAL",
  "interviewDate": "2026-03-15 14:00",
  "interviewerName": "张三",
  "interviewerTitle": "技术经理",
  "durationMinutes": 60,
  "status": "SCHEDULED"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": 1
}
```

---

### 1.3 公司相关接口

#### 1.3.1 获取所有公司

```
GET /api/data/companies
```

#### 1.3.2 获取公司详情

```
GET /api/data/companies/{id}
```

#### 1.3.3 根据名称获取公司

```
GET /api/data/companies/name?name=字节跳动
```

**查询参数**:
- `name`: 公司名称

#### 1.3.4 创建公司

```
POST /api/data/companies
Content-Type: application/json
```

**请求体**:
```json
{
  "name": "字节跳动",
  "industry": "互联网",
  "size": "10000+",
  "location": "北京",
  "website": "https://jobs.bytedance.com",
  "description": "字节跳动是一家全球领先的科技公司"
}
```

---

### 1.4 统计相关接口

#### 1.4.1 获取统计数据

```
GET /api/data/statistics
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {"status": "WISHLIST", "count": 5},
    {"status": "APPLIED", "count": 12},
    {"status": "INTERVIEW", "count": 3},
    {"status": "OFFER", "count": 1},
    {"status": "REJECTED", "count": 2}
  ]
}
```

---

## 2. WebSocket 接口

### 2.1 连接信息

- **WebSocket URL**: `ws://localhost:8080/api/ws/chat`
- **支持协议**: WebSocket + SockJS 降级

### 2.2 消息格式

#### 2.2.1 客户端发送消息

```json
{
  "type": "CHAT",
  "content": "帮我查询统计数据",
  "sessionId": "user-session-123"
}
```

**字段说明**:
- `type`: 消息类型（CHAT/STREAM）
- `content`: 消息内容
- `sessionId`: 会话 ID（可选，用于会话管理）

#### 2.2.2 服务器响应消息

**聊天响应**:
```json
{
  "type": "CHAT",
  "content": "📊 **求职统计报告**\n\n**总体概况**\n- 总申请数：23\n...",
  "timestamp": 1678567890000
}
```

**状态消息**:
```json
{
  "type": "STATUS",
  "content": "连接成功，您现在可以开始对话了",
  "timestamp": 1678567890000
}
```

**错误消息**:
```json
{
  "type": "ERROR",
  "content": "处理失败：服务不可用",
  "timestamp": 1678567890000
}
```

### 2.3 连接生命周期

```
1. 建立连接 → 服务器发送 STATUS 欢迎消息
2. 客户端发送 CHAT 消息 → 服务器处理并返回 CHAT 响应
3. 连接保持 → 可持续发送消息
4. 关闭连接 → 服务器清理会话
```

---

## 3. AI 工具方法

AI 可以通过自然语言理解调用以下工具方法：

### 3.1 求职申请工具（ApplicationTools）

#### createApplication
**功能**: 创建新的求职申请
**示例对话**:
- "帮我记录一个字节跳动的后端工程师岗位"
- "添加腾讯的 Java 开发职位"

**参数**:
- companyName（必填）: 公司名称
- jobTitle（必填）: 职位名称
- jobDescription（可选）: 职位描述
- jobType（可选）: 工作类型（FULL_TIME/PART_TIME/CONTRACT/INTERNSHIP）
- workLocation（可选）: 工作地点
- salaryMinStr（可选）: 薪资下限
- salaryMaxStr（可选）: 薪资上限
- jobUrl（可选）: 职位链接
- status（可选）: 申请状态（默认 WISHLIST）
- notes（可选）: 备注
- priorityInt（可选）: 优先级 1-10（默认 5）

**返回**: 成功消息和申请 ID

#### updateApplicationStatus
**功能**: 更新申请状态
**示例对话**:
- "把字节跳动的申请状态改成面试中"
- "ID 为 5 的申请更新为已投递"

**参数**:
- applicationId（必填）: 申请 ID
- newStatus（必填）: 新状态（WISHLIST/APPLIED/INTERVIEW/OFFER/REJECTED/WITHDRAWN）

**返回**: 成功确认消息

#### queryApplications
**功能**: 查询求职申请列表
**示例对话**:
- "显示所有面试中的申请"
- "查询最近 7 天的申请"
- "搜索包含 Java 的职位"

**参数**:
- status（可选）: 按状态筛选
- priorityInt（可选）: 按优先级筛选
- days（可选）: 查询最近 N 天
- keyword（可选）: 按职位名称模糊搜索

**返回**: 格式化的申请列表

#### getStatistics
**功能**: 获取求职统计数据和趋势分析
**示例对话**:
- "帮我看看统计数据"
- "分析一下我的求职情况"

**返回**: 统计报告和趋势分析

---

### 3.2 面试记录工具（InterviewTools）

#### createInterview
**功能**: 创建面试记录
**示例对话**:
- "为字节跳动的申请添加一个面试，下周五下午 2 点"
- "记录一个腾讯的技术面试，明天上午 10 点"

**参数**:
- applicationId（必填）: 申请 ID
- interviewType（可选）: 面试类型（PHONE/VIDEO/ONSITE/TECHNICAL/HR）
- interviewDateStr（必填）: 面试时间（格式：yyyy-MM-dd HH:mm）
- interviewerName（可选）: 面试官姓名
- interviewerTitle（可选）: 面试官职位
- durationMinutesInt（可选）: 面试时长（分钟）

**返回**: 成功确认消息和面试详情

#### updateInterview
**功能**: 更新面试记录
**示例对话**:
- "更新 ID 为 3 的面试状态为已完成"

**参数**:
- interviewId（必填）: 面试 ID
- newStatus（可选）: 新状态
- rating（可选）: 评分 1-5
- feedback（可选）: 面试反馈

**返回**: 成功确认消息

#### queryInterviews
**功能**: 查询面试记录
**示例对话**:
- "显示字节跳动的所有面试"
- "查看即将到来的面试"

**参数**:
- applicationId（可选）: 申请 ID

**返回**: 格式化的面试记录列表

---

### 3.3 公司工具（CompanyTools）

#### createCompany
**功能**: 创建公司信息
**示例对话**:
- "创建阿里巴巴的公司信息"
- "添加美团公司，互联网行业"

**参数**:
- name（必填）: 公司名称
- industry（可选）: 所属行业
- size（可选）: 公司规模
- location（可选）: 所在地
- website（可选）: 官网 URL
- description（可选）: 公司描述

**返回**: 成功确认消息

#### queryCompanies
**功能**: 查询公司信息
**示例对话**:
- "显示所有互联网公司"
- "搜索字节跳动"

**参数**:
- industry（可选）: 按行业筛选
- keyword（可选）: 按名称关键词搜索

**返回**: 格式化的公司列表

---

## 4. 数据模型

### 4.1 求职申请（JobApplication）

```typescript
interface JobApplication {
  id: number;
  companyId: number;
  jobTitle: string;
  jobDescription?: string;
  jobType?: string;              // FULL_TIME/PART_TIME/CONTRACT/INTERNSHIP
  workLocation?: string;
  salaryMin?: number;
  salaryMax?: number;
  salaryCurrency?: string;
  jobUrl?: string;
  status: string;                // WISHLIST/APPLIED/INTERVIEW/OFFER/REJECTED/WITHDRAWN
  applicationDate?: string;      // YYYY-MM-DD
  priority?: number;             // 1-10
  notes?: string;
  createdAt: string;
  updatedAt?: string;

  // 非数据库字段
  company?: Company;
}
```

### 4.2 公司（Company）

```typescript
interface Company {
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
```

### 4.3 面试记录（InterviewRecord）

```typescript
interface InterviewRecord {
  id: number;
  applicationId: number;
  interviewType?: string;        // PHONE/VIDEO/ONSITE/TECHNICAL/HR
  interviewDate: string;         // YYYY-MM-DD HH:mm
  interviewerName?: string;
  interviewerTitle?: string;
  durationMinutes?: number;
  status?: string;               // SCHEDULED/COMPLETED/CANCELLED
  rating?: number;               // 1-5
  feedback?: string;
  technicalQuestions?: string;
  notes?: string;
  followUpRequired?: boolean;
  createdAt: string;
  updatedAt?: string;
}
```

### 4.4 申请日志（ApplicationLog）

```typescript
interface ApplicationLog {
  id: number;
  applicationId: number;
  logType: string;               // STATUS_CHANGE/INTERVIEW_SCHEDULED/NOTE_ADDED
  logTitle?: string;
  logContent?: string;
  loggedBy?: string;             // USER/AI
  createdAt: string;
}
```

---

## 5. 错误处理

### 5.1 错误响应格式

```typescript
interface ErrorResponse {
  code: number;                  // 非 200 的状态码
  message: string;               // 错误描述
  data: null;
}
```

### 5.2 常见错误码

| 错误码 | 说明 | 示例 |
|-------|------|------|
| 400 | 请求参数错误 | 必填参数为空 |
| 404 | 资源不存在 | 申请 ID 不存在 |
| 500 | 服务器内部错误 | 数据库连接失败 |

### 5.3 错误处理建议

```typescript
async function fetchData<T>(url: string, options?: RequestInit): Promise<T> {
  try {
    const response = await fetch(`http://localhost:8080${url}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
    });

    const result: Result<T> = await response.json();

    if (result.code === 200) {
      return result.data;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    // 统一错误处理
    console.error('API 调用失败:', error);
    throw error;
  }
}
```

---

## 6. 使用示例

### 6.1 HTTP API 调用示例

#### 获取所有申请

```typescript
// 获取所有申请
const applications = await fetchData<JobApplication[]>('/api/data/applications');

console.log(applications);
```

#### 分页查询

```typescript
// 分页查询申请状态为 APPLIED 的记录
const pageData = await-fetchData<Page<JobApplication>>(
  '/api/data/applications/page?pageNum=1&pageSize=10&status=APPLIED'
);

console.log(`共 ${pageData.total} 条记录`);
pageData.records.forEach(app => {
  console.log(`${app.jobTitle} - ${app.status}`);
});
```

#### 创建申请

```typescript
// 创建新的求职申请
const newApplication = {
  companyId: 1,
  jobTitle: '前端工程师',
  jobType: 'FULL_TIME',
  workLocation: '上海',
  status: 'WISHLIST',
  priority: 7
};

const applicationId = await fetchData<number>('/api/data/applications', {
  method: 'POST',
  body: JSON.stringify(newApplication)
});

console.log(`创建成功，ID: ${applicationId}`);
```

#### 更新状态

```typescript
// 更新申请状态
await fetch(`/api/data/applications/${applicationId}/status?status=APPLIED`, {
  method: 'PUT'
});
```

---

### 6.2 WebSocket 连接示例

```typescript
class ChatClient {
  private ws: WebSocket | null = null;
  private sessionId: string;

  constructor() {
    this.sessionId = `session-${Date.now()}`;
  }

  connect() {
    this.ws = new WebSocket('ws://localhost:8080/api/ws/chat');

    this.ws.onopen = () => {
      console.log('WebSocket 连接已建立');
    };

    this.ws.onmessage = (event) => {
      const message: WebSocketMessage = JSON.parse(event.data);

      switch (message.type) {
        case 'STATUS':
          console.log('状态:', message.content);
          break;
        case 'CHAT':
          console.log('AI 回复:', message.content);
          break;
        case 'ERROR':
          console.error('错误:', message.content);
          break;
      }
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket 错误:', error);
    };

    this.ws.onclose = () => {
      console.log('WebSocket 连接已关闭');
    };
  }

  sendMessage(content: string) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const message: WebSocketMessage = {
        type: 'CHAT',
        content,
        sessionId: this.sessionId
      };
      this.ws.send(JSON.stringify(message));
    } else {
      console.error('WebSocket 未连接');
    }
  }

  disconnect() {
    this.ws?.close();
  }
}

// 使用示例
const client = new ChatClient();
client.connect();

// 发送消息
client.sendMessage('帮我查询统计数据');

// 断开连接
// client.disconnect();
```

---

### 6.3 完整的 React Hook 示例

```typescript
import { useState, useEffect } from 'react';

interface UseApiResult<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
  refetch: () => void;
}

function useApi<T>(url: string): UseApiResult<T> {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(`http://localhost:8080${url}`);
      const result: Result<T> = await response.json();

      if (result.code === 200) {
        setData(result.data);
      } else {
        setError(result.message);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '未知错误');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [url]);

  return { data, loading, error, refetch: fetchData };
}

// 使用示例
function ApplicationList() {
  const { data: applications, loading, error } = useApi<JobApplication[]>('/api/data/applications');

  if (loading) return <div>加载中...</div>;
  if (error) return <div>错误: {error}</div>;

  return (
    <ul>
      {applications?.map(app => (
        <li key={app.id}>
          {app.jobTitle} - {app.status}
        </li>
      ))}
    </ul>
  );
}
```

---

## 7. 前端开发建议

### 7.1 技术栈推荐

- **框架**: React 18 + Vite
- **状态管理**: Zustand
- **HTTP 客户端**: Axios 或 Fetch API
- **WebSocket**: 原生 WebSocket 或 SockJS
- **UI 组件库**: shadcn/ui + Tailwind CSS
- **日期处理**: dayjs

### 7.2 目录结构建议

```
src/
├── api/                    # API 服务层
│   ├── client.ts          # HTTP 客户端配置
│   ├── dataApi.ts         # HTTP API 封装
│   └── websocket.ts       # WebSocket 客户端
├── types/                 # TypeScript 类型定义
│   ├── application.ts
│   ├── company.ts
│   └── interview.ts
├── store/                 # 状态管理
│   ├── applicationStore.ts
│   └── chatStore.ts
├── components/            # 组件
│   ├── ui/                # shadcn/ui 组件
│   ├── ApplicationCard.tsx
│   ├── ChatPanel.tsx
│   └── ...
├── pages/                 # 页面
│   ├── Dashboard.tsx
│   ├── Applications.tsx
│   └── ...
└── hooks/                 # 自定义 Hooks
    ├── useApi.ts
    ├── useWebSocket.ts
    └── ...
```

### 7.3 开发优先级

1. **P0 - 核心功能**（第一周）
   - ✅ HTTP API 集成和类型定义
   - ✅ 申请列表和详情页
   - ✅ 创建/编辑申请表单
   - ✅ 状态更新功能

2. **P1 - AI 对话功能**（第二周）
   - ✅ WebSocket 集成
   - ✅ 聊天面板 UI
   - ✅ 消息流式显示
   - ✅ 对话历史管理

3. **P2 - 增强功能**（第三周）
   - ✅ 面试记录管理
   - ✅ 统计数据可视化
   - ✅ 公司管理
   - ✅ 多视图切换（列表/看板/时间线）

---

**文档版本**: 1.0
**最后更新**: 2026-03-11
**维护者**: Backend Team
