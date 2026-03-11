# Job Tracker Backend - 测试文档

## 目录

- [前置条件](#前置条件)
- [环境配置](#环境配置)
- [启动应用](#启动应用)
- [HTTP API 测试](#http-api-测试)
- [WebSocket 测试](#websocket-测试)
- [AI 对话测试](#ai-对话测试)

---

## 前置条件

### 1. MySQL 数据库

确保 MySQL 数据库已安装并运行：

```bash
# 检查 MySQL 状态
mysql --version

# 登录 MySQL
mysql -u root -p
```

### 2. LM Studio

确保 LM Studio 已安装并运行 OpenAI 兼容的 API 服务：

1. 打开 LM Studio
2. 选择一个模型（如 `llama-3.2-3b-instruct`）
3. 启动本地服务器（默认端口 `1234`）
4. 确认 API 端点：`http://localhost:1234/v1`

### 3. Java 环境

确保 Java 17+ 已安装：

```bash
java -version
```

---

## 环境配置

### 1. 配置文件

编辑 `src/main/resources/application.yml`：

```yaml
server:
  port: 8080

spring:
  application:
    name: job-tracker-backend

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/job_tracker?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改为你的 MySQL 密码

  data:
    redis:
      host: localhost
      port: 6379
      database: 0

# LangChain4j 配置
langchain4j:
  open-ai:
    base-url: http://localhost:1234/v1  # LM Studio API 地址
    api-key: lm-studio                  # 固定值
    model-name: llama-3.2-3b-instruct   # 修改为你的模型名称
    temperature: 0.7
    max-tokens: 2000
    timeout: 60s
```

### 2. 初始化数据库

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS job_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入表结构
mysql -u root -p job_tracker < src/main/resources/schema.sql

# 导入测试数据（可选）
mysql -u root -p job_tracker < src/main/resources/data.sql
```

---

## 启动应用

### 使用 Maven 启动

```bash
# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

### 使用 IDEA 启动

1. 打开 `JobTrackerApplication.java`
2. 右键点击 `Run 'JobTrackerApplication'`

### 验证启动

```bash
# 检查健康状态
curl http://localhost:8080/actuator/health

# 查看日志
tail -f logs/job-tracker-backend.log
```

---

## HTTP API 测试

### 测试工具

- **推荐**：Postman、Insomnia
- **命令行**：curl、HTTPie
- **在线**：Swagger UI（如已集成）

### 基础 URL

```
http://localhost:8080/api/data
```

### 1. 求职申请接口

#### 1.1 获取所有申请

```bash
curl -X GET http://localhost:8080/api/data/applications
```

**预期响应**：

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

#### 1.2 获取单个申请

```bash
curl -X GET http://localhost:8080/api/data/applications/1
```

#### 1.3 按状态查询申请

```bash
curl -X GET http://localhost:8080/api/data/applications/status/面试中
```

#### 1.4 分页查询申请

```bash
curl -X GET "http://localhost:8080/api/data/applications/page?pageNum=1&pageSize=10"
```

#### 1.5 更新申请状态

```bash
curl -X PUT "http://localhost:8080/api/data/applications/1/status?status=已录用"
```

#### 1.6 创建申请

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

### 2. 面试记录接口

#### 2.1 获取所有面试记录

```bash
curl -X GET http://localhost:8080/api/data/interviews
```

#### 2.2 按申请ID查询面试记录

```bash
curl -X GET http://localhost:8080/api/data/interviews/application/1
```

#### 2.3 获取即将进行的面试

```bash
curl -X GET http://localhost:8080/api/data/interviews/upcoming
```

#### 2.4 创建面试记录

```bash
curl -X POST http://localhost:8080/api/data/interviews \
  -H "Content-Type: application/json" \
  -d '{
    "applicationId": 1,
    "interviewDate": "2025-03-15T14:00:00",
    "interviewType": "技术面试",
    "interviewer": "张三",
    "status": "待面试"
  }'
```

### 3. 公司接口

#### 3.1 获取所有公司

```bash
curl -X GET http://localhost:8080/api/data/companies
```

#### 3.2 获取单个公司

```bash
curl -X GET http://localhost:8080/api/data/companies/1
```

#### 3.3 按名称查询公司

```bash
curl -X GET "http://localhost:8080/api/data/companies/name?name=字节跳动"
```

#### 3.4 创建公司

```bash
curl -X POST http://localhost:8080/api/data/companies \
  -H "Content-Type: application/json" \
  -d '{
    "name": "阿里巴巴",
    "industry": "互联网",
    "location": "杭州",
    "website": "https://www.alibaba.com"
  }'
```

### 4. 统计接口

#### 4.1 获取统计数据

```bash
curl -X GET http://localhost:8080/api/data/statistics
```

#### 4.2 获取高优先级申请

```bash
curl -X GET http://localhost:8080/api/data/applications/high-priority
```

---

## WebSocket 测试

### 测试工具

- **在线工具**：[WebSocket Test Client](https://www.websocket.org/echo.html)
- **浏览器控制台**：直接编写 JavaScript 代码
- **命令行**：wscat

### 1. 使用 wscat 测试

```bash
# 安装 wscat
npm install -g wscat

# 连接 WebSocket
wscat -c ws://localhost:8080/ws/chat
```

**测试对话**：

```
# 发送消息（客户端）
> {"type":"chat","content":"我有哪些投递中的申请？"}

# 接收响应（服务端）
< {"type":"chat","content":"您当前有以下投递中的申请：\n1. 前端工程师 - 腾讯（2025-03-12）"}

# 继续对话
> {"type":"chat","content":"帮我统计一下各状态的申请数量"}
< {"type":"chat","content":"统计结果：\n- 投递中：2\n- 面试中：3\n- 已录用：1"}
```

### 2. 使用浏览器控制台测试

打开浏览器控制台（F12），执行以下代码：

```javascript
// 建立 WebSocket 连接
const ws = new WebSocket('ws://localhost:8080/ws/chat');

// 连接成功
ws.onopen = function() {
    console.log('WebSocket 已连接');
};

// 接收消息
ws.onmessage = function(event) {
    const message = JSON.parse(event.data);
    console.log('收到消息：', message);
};

// 连接关闭
ws.onclose = function() {
    console.log('WebSocket 已关闭');
};

// 发送消息
ws.send(JSON.stringify({
    type: 'chat',
    content: '我有哪些投递中的申请？'
}));
```

### 3. 消息格式

#### 请求格式（客户端 -> 服务端）

```json
{
  "type": "chat",
  "content": "用户输入的消息内容"
}
```

#### 响应格式（服务端 -> 客户端）

**正常消息**：

```json
{
  "type": "chat",
  "content": "AI 的回复内容"
}
```

**状态消息**：

```json
{
  "type": "status",
  "content": "连接成功，您现在可以开始对话了"
}
```

**错误消息**：

```json
{
  "type": "error",
  "content": "错误信息描述"
}
```

---

## AI 对话测试

### 1. 工具方法测试

#### 1.1 查询申请列表

**输入**：

```
我有哪些申请？
```

**预期输出**：

```
您当前有以下求职申请：

1. Java 后端工程师 - 字节跳动（面试中）
2. 前端工程师 - 腾讯（投递中）
3. 全栈开发工程师 - 阿里巴巴（已录用）
```

#### 1.2 查询特定状态的申请

**输入**：

```
帮我查一下投递中的申请
```

**预期输出**：

```
您当前投递中的申请：

1. 前端工程师 - 腾讯
   申请日期：2025-03-12
   优先级：高

2. 产品经理 - 美团
   申请日期：2025-03-10
   优先级：普通
```

#### 1.3 查询面试安排

**输入**：

```
我有哪些即将到来的面试？
```

**预期输出**：

```
您即将参加的面试：

1. 技术面试 - 字节跳动
   时间：2025-03-15 14:00
   面试官：张三
   备注：准备好算法题

2. HR 面试 - 腾讯
   时间：2025-03-16 10:00
   面试官：李四
```

#### 1.4 创建新申请

**输入**：

```
我要投递字节跳动的前端工程师岗位
```

**预期输出**：

```
已成功创建求职申请：

公司：字节跳动
职位：前端工程师
状态：投递中
申请日期：2025-03-12

ID：10
```

#### 1.5 更新申请状态

**输入**：

```
字节跳动的面试通过了，更新状态为面试中
```

**预期输出**：

```
已成功更新申请状态：

公司：字节跳动
职位：前端工程师
新状态：面试中
```

#### 1.6 创建面试记录

**输入**：

```
字节跳动安排了技术面试，时间是3月15日下午2点，面试官是王五
```

**预期输出**：

```
已成功创建面试记录：

申请：字节跳动 - 前端工程师
面试类型：技术面试
时间：2025-03-15 14:00
面试官：王五
状态：待面试

ID：5
```

#### 1.7 公司信息查询

**输入**：

```
帮我查一下字节跳动这家公司的信息
```

**预期输出**：

```
公司信息：

名称：字节跳动
行业：互联网
地点：北京
官网：https://www.bytedance.com
```

#### 1.8 创建公司信息

**输入**：

```
帮我添加一家新公司，名字是拼多多，行业是电商，地点在上海
```

**预期输出**：

```
已成功创建公司信息：

名称：拼多多
行业：电商
地点：上海
官网：暂无

ID：20
```

### 2. 复杂对话测试

#### 2.1 统计分析

**输入**：

```
帮我统计一下各状态的申请数量
```

**预期输出**：

```
申请状态统计：

• 投递中：2
• 面试中：3
• 已录用：1
• 已拒绝：0
• 已撤回：0

总计：6 个申请
```

#### 2.2 高优先级申请

**输入**：

```
我有哪些高优先级的申请需要关注？
```

**预期输出**：

```
您的高优先级申请：

1. Java 后端工程师 - 字节跳动（面试中）
   建议关注：准备技术面试

2. 前端工程师 - 腾讯（投递中）
   建议关注：跟进投递进度
```

#### 2.3 多轮对话

**第一轮**：

```
我有哪些投递中的申请？
```

**第二轮**：

```
帮我查一下其中腾讯那家的面试安排
```

**第三轮**：

```
把那个面试的时间改到后天下午3点
```

### 3. 边界测试

#### 3.1 空消息

**输入**：

```

（空消息）
```

**预期输出**：

```json
{
  "type": "error",
  "content": "消息内容不能为空"
}
```

#### 3.2 未知意图

**输入**：

```
今天天气怎么样？
```

**预期输出**：

```
抱歉，我只能帮您管理求职相关信息。您可以询问：
- 求职申请列表
- 面试安排
- 公司信息
- 创建/更新申请
```

#### 3.3 数据不存在

**输入**：

```
帮我查一下 ID 999 的申请
```

**预期输出**：

```
未找到 ID 为 999 的申请记录。
```

---

## 故障排查

### 1. 应用启动失败

**问题**：`Port 8080 is already in use`

**解决**：

```bash
# 查找占用端口的进程
lsof -i :8080

# 结束进程
kill -9 <PID>
```

### 2. 数据库连接失败

**问题**：`Communications link failure`

**解决**：

1. 检查 MySQL 是否运行
2. 检查 `application.yml` 中的数据库配置
3. 确认数据库 `job_tracker` 已创建

### 3. AI 服务无响应

**问题**：AI 对话无响应或超时

**解决**：

1. 确认 LM Studio 已启动
2. 确认模型已加载
3. 检查 API 地址是否正确（默认 `http://localhost:1234/v1`）
4. 查看日志文件 `logs/job-tracker-backend-ai.log`

### 4. WebSocket 连接失败

**问题**：`WebSocket connection failed`

**解决**：

1. 确认应用已启动
2. 检查 WebSocket URL 是否正确（`ws://localhost:8080/ws/chat`）
3. 查看浏览器控制台错误信息

---

## 附录

### HTTP 状态码

- `200 OK`：请求成功
- `400 Bad Request`：请求参数错误
- `404 Not Found`：资源不存在
- `500 Internal Server Error`：服务器内部错误

### 日志文件

- `logs/job-tracker-backend.log`：所有日志
- `logs/job-tracker-backend-error.log`：错误日志
- `logs/job-tracker-backend-ai.log`：AI 相关日志

### 测试数据

完整的测试数据请参考 `src/main/resources/data.sql`。
