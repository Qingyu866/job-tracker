# Job Tracker Backend - API 参考文档

## 目录

- [概述](#概述)
- [WebSocket 接口](#websocket-接口)
- [HTTP REST API](#http-rest-api)
- [AI 工具方法](#ai-工具方法)
- [数据模型](#数据模型)
- [错误处理](#错误处理)

---

## 概述

Job Tracker Backend 提供两类 API 接口：

1. **WebSocket API**：实时双向通信，用于 AI 对话交互
2. **HTTP REST API**：标准的 CRUD 操作，用于数据查询和管理

### 基础 URL

```
HTTP API: http://localhost:8080/api/data
WebSocket: ws://localhost:8080/ws/chat
```

### 通用响应格式

**成功响应**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { }
}
```

**错误响应**：

```json
{
  "code": 400,
  "message": "错误描述",
  "data": null
}
```

---

## WebSocket 接口

### 连接端点

```
ws://localhost:8080/ws/chat
```

### 消息格式

#### 客户端发送

**类型：`chat`**

```json
{
  "type": "chat",
  "content": "用户输入的消息内容"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 是 | 消息类型，固定为 "chat" |
| content | string | 是 | 消息内容 |

#### 服务端响应

**类型：`chat`**（正常回复）

```json
{
  "type": "chat",
  "content": "AI 的回复内容"
}
```

**类型：`status`**（状态通知）

```json
{
  "type": "status",
  "content": "连接成功，您现在可以开始对话了"
}
```

**类型：`error`**（错误消息）

```json
{
  "type": "error",
  "content": "错误信息描述"
}
```

### 消息类型枚举

| 值 | 说明 | 方向 |
|---|---|---|
| `chat` | 聊天消息 | 双向 |
| `status` | 状态通知 | 服务端 -> 客户端 |
| `error` | 错误消息 | 服务端 -> 客户端 |

### 连接生命周期

1. **建立连接**：客户端连接到 `ws://localhost:8080/ws/chat`
2. **欢迎消息**：服务端发送 `status` 类型的欢迎消息
3. **消息交互**：客户端发送 `chat` 消息，服务端返回 AI 回复
4. **关闭连接**：客户端或服务端主动关闭连接

### 使用示例

#### JavaScript (浏览器)

```javascript
// 建立连接
const ws = new WebSocket('ws://localhost:8080/ws/chat');

// 监听连接成功
ws.onopen = () => {
    console.log('WebSocket 已连接');
};

// 监听消息
ws.onmessage = (event) => {
    const message = JSON.parse(event.data);

    switch (message.type) {
        case 'chat':
            console.log('AI 回复：', message.content);
            break;
        case 'status':
            console.log('状态：', message.content);
            break;
        case 'error':
            console.error('错误：', message.content);
            break;
    }
};

// 监听错误
ws.onerror = (error) => {
    console.error('WebSocket 错误：', error);
};

// 监听关闭
ws.onclose = () => {
    console.log('WebSocket 已关闭');
};

// 发送消息
ws.send(JSON.stringify({
    type: 'chat',
    content: '我有哪些投递中的申请？'
}));
```

#### Python

```python
import asyncio
import websockets
import json

async def chat_with_ai():
    uri = "ws://localhost:8080/ws/chat"

    async with websockets.connect(uri) as websocket:
        # 接收欢迎消息
        welcome = await websocket.recv()
        print(f"服务端：{welcome}")

        # 发送消息
        message = {
            "type": "chat",
            "content": "我有哪些投递中的申请？"
        }
        await websocket.send(json.dumps(message))

        # 接收响应
        response = await websocket.recv()
        print(f"AI 回复：{response}")

# 运行
asyncio.run(chat_with_ai())
```

---

## HTTP REST API

### 1. 求职申请接口

#### 1.1 获取所有申请

```
GET /api/data/applications
```

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "position": "Java 后端工程师",
      "companyName": "字节跳动",
      "status": "面试中",
      "applicationDate": "2025-01-10",
      "priority": true,
      "createdAt": "2025-01-10T10:00:00",
      "updatedAt": "2025-01-10T10:00:00"
    }
  ]
}
```

#### 1.2 获取单个申请

```
GET /api/data/applications/{id}
```

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 申请 ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "position": "Java 后端工程师",
    "companyName": "字节跳动",
    "status": "面试中",
    "applicationDate": "2025-01-10",
    "priority": true,
    "jobDescription": "负责后端系统开发...",
    "salaryRange": "20k-35k",
    "workLocation": "北京",
    "notes": "已通过初筛",
    "createdAt": "2025-01-10T10:00:00",
    "updatedAt": "2025-01-10T10:00:00"
  }
}
```

#### 1.3 按状态查询申请

```
GET /api/data/applications/status/{status}
```

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| status | String | 申请状态（投递中/面试中/已录用/已拒绝/已撤回） |

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "position": "Java 后端工程师",
      "companyName": "字节跳动",
      "status": "面试中",
      "applicationDate": "2025-01-10",
      "priority": true
    }
  ]
}
```

#### 1.4 分页查询申请

```
GET /api/data/applications/page?pageNum=1&pageSize=10&status=面试中
```

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |
| status | String | 否 | - | 申请状态（可选） |

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 1,
        "position": "Java 后端工程师",
        "companyName": "字节跳动",
        "status": "面试中",
        "applicationDate": "2025-01-10",
        "priority": true
      }
    ],
    "total": 25,
    "size": 10,
    "current": 1,
    "pages": 3
  }
}
```

#### 1.5 更新申请状态

```
PUT /api/data/applications/{id}/status?status=已录用
```

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 申请 ID |

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 是 | 新状态 |

**响应示例**：

```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": null
}
```

#### 1.6 创建申请

```
POST /api/data/applications
```

**请求体**：

```json
{
  "position": "前端工程师",
  "companyName": "腾讯",
  "status": "投递中",
  "applicationDate": "2025-03-12",
  "priority": true,
  "jobDescription": "负责前端页面开发...",
  "salaryRange": "15k-25k",
  "workLocation": "深圳",
  "notes": "内推"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "创建成功",
  "data": 10
}
```

### 2. 面试记录接口

#### 2.1 获取所有面试记录

```
GET /api/data/interviews
```

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "applicationId": 1,
      "interviewDate": "2025-03-15T14:00:00",
      "interviewType": "技术面试",
      "interviewer": "张三",
      "status": "待面试",
      "notes": "准备算法题",
      "createdAt": "2025-03-10T10:00:00"
    }
  ]
}
```

#### 2.2 按申请ID查询面试记录

```
GET /api/data/interviews/application/{applicationId}
```

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| applicationId | Long | 申请 ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "applicationId": 1,
      "interviewDate": "2025-03-15T14:00:00",
      "interviewType": "技术面试",
      "interviewer": "张三",
      "status": "待面试",
      "notes": "准备算法题"
    }
  ]
}
```

#### 2.3 获取即将进行的面试

```
GET /api/data/interviews/upcoming
```

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "applicationId": 1,
      "interviewDate": "2025-03-15T14:00:00",
      "interviewType": "技术面试",
      "interviewer": "张三",
      "status": "待面试"
    }
  ]
}
```

#### 2.4 创建面试记录

```
POST /api/data/interviews
```

**请求体**：

```json
{
  "applicationId": 1,
  "interviewDate": "2025-03-15T14:00:00",
  "interviewType": "技术面试",
  "interviewer": "张三",
  "status": "待面试",
  "notes": "准备算法题和数据结构"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "创建成功",
  "data": 5
}
```

### 3. 公司接口

#### 3.1 获取所有公司

```
GET /api/data/companies
```

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "name": "字节跳动",
      "industry": "互联网",
      "location": "北京",
      "website": "https://www.bytedance.com",
      "notes": "短视频、人工智能",
      "createdAt": "2025-01-01T00:00:00"
    }
  ]
}
```

#### 3.2 获取单个公司

```
GET /api/data/companies/{id}
```

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 公司 ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "name": "字节跳动",
    "industry": "互联网",
    "location": "北京",
    "website": "https://www.bytedance.com",
    "notes": "短视频、人工智能",
    "createdAt": "2025-01-01T00:00:00"
  }
}
```

#### 3.3 按名称查询公司

```
GET /api/data/companies/name?name=字节跳动
```

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 公司名称 |

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "name": "字节跳动",
    "industry": "互联网",
    "location": "北京",
    "website": "https://www.bytedance.com"
  }
}
```

#### 3.4 创建公司

```
POST /api/data/companies
```

**请求体**：

```json
{
  "name": "阿里巴巴",
  "industry": "互联网",
  "location": "杭州",
  "website": "https://www.alibaba.com",
  "notes": "电商、云计算"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "创建成功",
  "data": 20
}
```

### 4. 统计接口

#### 4.1 获取统计数据

```
GET /api/data/statistics
```

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {"status": "投递中", "count": 2},
    {"status": "面试中", "count": 3},
    {"status": "已录用", "count": 1},
    {"status": "已拒绝", "count": 0}
  ]
}
```

#### 4.2 获取高优先级申请

```
GET /api/data/applications/high-priority
```

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "position": "Java 后端工程师",
      "companyName": "字节跳动",
      "status": "面试中",
      "priority": true
    }
  ]
}
```

---

## AI 工具方法

AI Agent 通过 LangChain4j 的工具调用能力，可以执行以下操作：

### 1. ApplicationTools

#### 1.1 listApplications

**描述**：获取所有求职申请列表

**参数**：无

**返回**：`List<JobApplication>`

**示例对话**：

```
用户：我有哪些申请？
AI 调用：listApplications()
AI 回复：您当前有以下求职申请：...
```

#### 1.2 getApplicationById

**描述**：根据 ID 获取单个申请

**参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 申请 ID |

**返回**：`JobApplication`

**示例对话**：

```
用户：帮我查一下 ID 为 1 的申请
AI 调用：getApplicationById(1)
AI 回复：找到申请：Java 后端工程师 - 字节跳动...
```

#### 1.3 listApplicationsByStatus

**描述**：根据状态查询申请

**参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 是 | 申请状态 |

**返回**：`List<JobApplication>`

**示例对话**：

```
用户：我有哪些投递中的申请？
AI 调用：listApplicationsByStatus("投递中")
AI 回复：您当前投递中的申请有：...
```

#### 1.4 createApplication

**描述**：创建新的求职申请

**参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| position | String | 是 | 职位名称 |
| companyName | String | 是 | 公司名称 |
| status | String | 否 | 申请状态（默认：投递中） |
| applicationDate | String | 否 | 申请日期（默认：今天） |
| priority | Boolean | 否 | 是否高优先级（默认：false） |

**返回**：`JobApplication`

**示例对话**：

```
用户：我要投递字节跳动的前端工程师岗位
AI 调用：createApplication("前端工程师", "字节跳动", "投递中", "2025-03-12", false)
AI 回复：已成功创建求职申请...
```

#### 1.5 updateApplicationStatus

**描述**：更新申请状态

**参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 申请 ID |
| status | String | 是 | 新状态 |

**返回**：`Boolean`

**示例对话**：

```
用户：字节跳位的面试通过了，更新状态为面试中
AI 调用：updateApplicationStatus(1, "面试中")
AI 回复：已成功更新申请状态...
```

#### 1.6 countByStatus

**描述**：统计各状态的申请数量

**参数**：无

**返回**：`List<Map<String, Object>>`

**示例对话**：

```
用户：帮我统计一下各状态的申请数量
AI 调用：countByStatus()
AI 回复：申请状态统计：投递中 2，面试中 3，已录用 1...
```

#### 1.7 listHighPriorityApplications

**描述**：获取高优先级申请

**参数**：无

**返回**：`List<JobApplication>`

**示例对话**：

```
用户：我有哪些高优先级的申请需要关注？
AI 调用：listHighPriorityApplications()
AI 回复：您的高优先级申请：...
```

### 2. InterviewTools

#### 2.1 listInterviews

**描述**：获取所有面试记录

**参数**：无

**返回**：`List<InterviewRecord>`

#### 2.2 getInterviewById

**描述**：根据 ID 获取面试记录

**参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 面试记录 ID |

**返回**：`InterviewRecord`

#### 2.3 listInterviewsByApplicationId

**描述**：根据申请 ID 查询面试记录

**参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| applicationId | Long | 是 | 申请 ID |

**返回**：`List<InterviewRecord>`

**示例对话**：

```
用户：帮我查一下字节跳动的面试安排
AI 调用：listInterviewsByApplicationId(1)
AI 回复：字节跳位的面试安排：...
```

#### 2.4 listUpcomingInterviews

**描述**：获取即将进行的面试

**参数**：无

**返回**：`List<InterviewRecord>`

**示例对话**：

```
用户：我有哪些即将到来的面试？
AI 调用：listUpcomingInterviews()
AI 回复：您即将参加的面试：...
```

#### 2.5 createInterview

**描述**：创建面试记录

**参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| applicationId | Long | 是 | 申请 ID |
| interviewDate | String | 是 | 面试日期时间 |
| interviewType | String | 否 | 面试类型（默认：综合面试） |
| interviewer | String | 否 | 面试官 |
| status | String | 否 | 状态（默认：待面试） |
| notes | String | 否 | 备注 |

**返回**：`InterviewRecord`

**示例对话**：

```
用户：字节跳动安排了技术面试，时间是3月15日下午2点
AI 调用：createInterview(1, "2025-03-15 14:00", "技术面试", null, "待面试", null)
AI 回复：已成功创建面试记录...
```

### 3. CompanyTools

#### 3.1 listCompanies

**描述**：获取所有公司

**参数**：无

**返回**：`List<Company>`

#### 3.2 getCompanyById

**描述**：根据 ID 获取公司

**参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 公司 ID |

**返回**：`Company`

#### 3.3 getCompanyByName

**描述**：根据名称获取公司

**参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 公司名称 |

**返回**：`Company`

**示例对话**：

```
用户：帮我查一下字节跳动这家公司的信息
AI 调用：getCompanyByName("字节跳动")
AI 回复：公司信息：字节跳动，互联网行业...
```

#### 3.4 createOrUpdateCompany

**描述**：创建或更新公司信息

**参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 公司名称 |
| industry | String | 否 | 行业 |
| location | String | 否 | 地点 |
| website | String | 否 | 官网 |
| notes | String | 否 | 备注 |

**返回**：`Company`

**示例对话**：

```
用户：帮我添加一家新公司，名字是拼多多
AI 调用：createOrUpdateCompany("拼多多", "电商", "上海", null, null)
AI 回复：已成功创建公司信息...
```

---

## 数据模型

### JobApplication（求职申请）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 自动 | 主键 ID |
| position | String | 是 | 职位名称 |
| companyName | String | 是 | 公司名称 |
| status | String | 是 | 申请状态 |
| applicationDate | LocalDate | 是 | 申请日期 |
| priority | Boolean | 是 | 是否高优先级 |
| jobDescription | String | 否 | 职位描述 |
| salaryRange | String | 否 | 薪资范围 |
| workLocation | String | 否 | 工作地点 |
| notes | String | 否 | 备注 |
| createdAt | LocalDateTime | 自动 | 创建时间 |
| updatedAt | LocalDateTime | 自动 | 更新时间 |

**申请状态枚举**：

- `投递中`（APPLYING）
- `面试中`（INTERVIEWING）
- `已录用`（OFFERED）
- `已拒绝`（REJECTED）
- `已撤回`（WITHDRAWN）

### InterviewRecord（面试记录）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 自动 | 主键 ID |
| applicationId | Long | 是 | 申请 ID |
| interviewDate | LocalDateTime | 是 | 面试日期时间 |
| interviewType | String | 是 | 面试类型 |
| interviewer | String | 否 | 面试官 |
| status | String | 是 | 面试状态 |
| notes | String | 否 | 备注 |
| createdAt | LocalDateTime | 自动 | 创建时间 |

**面试类型**：技术面试、HR 面试、综合面试、笔试等

**面试状态**：待面试、已完成、已取消

### Company（公司）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 自动 | 主键 ID |
| name | String | 是 | 公司名称 |
| industry | String | 否 | 行业 |
| location | String | 否 | 地点 |
| website | String | 否 | 官网 |
| notes | String | 否 | 备注 |
| createdAt | LocalDateTime | 自动 | 创建时间 |

### ApplicationLog（申请日志）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 自动 | 主键 ID |
| applicationId | Long | 是 | 申请 ID |
| action | String | 是 | 操作类型 |
| actionDate | LocalDateTime | 是 | 操作时间 |
| notes | String | 否 | 备注 |
| createdAt | LocalDateTime | 自动 | 创建时间 |

---

## 错误处理

### HTTP 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用（如 AI 服务异常） |

### 错误响应格式

```json
{
  "code": 400,
  "message": "参数校验失败：position 不能为空",
  "data": null
}
```

### 常见错误

| 错误信息 | 说明 | 解决方案 |
|---------|------|---------|
| 参数校验失败 | 请求参数不符合要求 | 检查参数格式和必填项 |
| 资源不存在 | 查询的资源不存在 | 确认资源 ID 是否正确 |
| 数据库操作失败 | 数据库异常 | 检查数据库连接和状态 |
| AI 服务暂时不可用 | LM Studio 服务异常 | 检查 LM Studio 是否正常运行 |

---

## 附录

### 请求示例（curl）

#### 获取所有申请

```bash
curl -X GET http://localhost:8080/api/data/applications
```

#### 创建申请

```bash
curl -X POST http://localhost:8080/api/data/applications \
  -H "Content-Type: application/json" \
  -d '{
    "position": "前端工程师",
    "companyName": "腾讯",
    "status": "投递中",
    "applicationDate": "2025-03-12",
    "priority": true
  }'
```

#### WebSocket 连接（wscat）

```bash
wscat -c ws://localhost:8080/ws/chat
```

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2025-03-12 | 初始版本 |
