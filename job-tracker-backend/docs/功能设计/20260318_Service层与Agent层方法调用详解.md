# Service 层与 Agent 层方法调用详解

## 文档概述

本文档详细描述 Job Tracker 模拟面试系统中 **Service 层** 和 **Agent 层** 的核心方法内部调用流程，包括每一步的执行细节、内存操作、数据库交互和 LangChain4j Agent 调用机制。

**创建日期**: 2026-03-18
**相关层级**:
- Service 层：业务逻辑处理
- Agent 层：LangChain4j AI Agent 调用

---

# 第一部分：Service 层方法详解

## 一、InterviewMessageService - 消息服务

### 1.1 addAnswer() - 保存用户回答

**方法签名**:
```java
@Transactional
public InterviewMessage addAnswer(String sessionId, Integer roundNumber, String content)
```

**调用时序**:
```
Controller.sendMessage()
  │
  └──> InterviewMessageService.addAnswer(sessionId, roundNumber, content)
        │
        ├──> getNextSequence(sessionId, roundNumber)
        │     │
        │     ├──> LambdaQueryWrapper<InterviewMessage>
        │     │     .eq(sessionId)
        │     │     .eq(roundNumber)
        │     │     .orderByDesc(SEQUENCE_IN_ROUND)
        │     │
        │     └──> messageMapper.selectList()
        │           │
        │           └──> Database: SELECT * FROM interview_messages
        │                WHERE session_id = ? AND round_number = ?
        │                ORDER BY sequence_in_round DESC
        │
        ├──> InterviewMessage.builder()
        │     │
        │     ├──> setSessionId(sessionId)
        │     ├──> setRoundNumber(roundNumber)
        │     ├──> setSequenceInRound(sequence)
        │     ├──> setRole("USER")
        │     ├──> setContent(content)
        │     ├──> setCreatedAt(LocalDateTime.now())
        │     │
        │     └──> build()
        │
        └──> messageMapper.insert(message)
              │
              └──> Database: INSERT INTO interview_messages
                   (session_id, round_number, sequence_in_round,
                    role, content, created_at)
                   VALUES (?, ?, ?, 'USER', ?, NOW())
```

**详细执行步骤**:

| 步骤 | 操作 | 数据类型 | 说明 |
|-----|------|---------|------|
| 1 | 计算序号 | Integer | 查询当前轮次的最大序号，+1 |
| 2 | 构建对象 | InterviewMessage | 使用 Builder 模式 |
| 3 | 插入数据库 | int | MyBatis Plus 自动生成 ID |
| 4 | 事务提交 | void | @Transactional 保证原子性 |

**数据库 SQL**:
```sql
-- 查询序号
SELECT * FROM interview_messages
WHERE session_id = 'abc-123' AND round_number = 3
ORDER BY sequence_in_round DESC;

-- 插入消息
INSERT INTO interview_messages (
    id, session_id, round_number, sequence_in_round,
    role, content, created_at
) VALUES (
    DEFAULT, 'abc-123', 3, 1,
    'USER', 'Redis 集群使用一致性哈希...', NOW()
);
```

**返回值**:
```java
InterviewMessage {
    id: 123L,
    sessionId: "abc-123",
    roundNumber: 3,
    sequenceInRound: 1,
    role: "USER",
    content: "Redis 集群使用一致性哈希...",
    createdAt: 2026-03-18T10:30:45
}
```

---

### 1.2 addQuestion() - 保存面试官问题

**方法签名**:
```java
@Transactional
public InterviewMessage addQuestion(
    String sessionId,
    Integer roundNumber,
    String content,
    Long skillId,
    String skillName
)
```

**调用时序**:
```
Controller.sendMessage()
  │
  └──> InterviewMessageService.addQuestion(sessionId, roundNumber+1, question, skillId, skillName)
        │
        ├──> getNextSequence(sessionId, roundNumber+1)
        │     └──> [同 addAnswer]
        │
        ├──> InterviewMessage.builder()
        │     │
        │     ├──> setSessionId(sessionId)
        │     ├──> setRoundNumber(roundNumber + 1)
        │     ├──> setSequenceInRound(sequence)
        │     ├──> setRole("ASSISTANT")
        │     ├──> setContent(content)
        │     ├──> setSkillId(skillId)          // 可选
        │     ├──> setSkillName(skillName)      // 可选
        │     ├──> setCreatedAt(LocalDateTime.now())
        │     │
        │     └──> build()
        │
        └──> messageMapper.insert(message)
              └──> Database: INSERT INTO interview_messages
```

**与 addAnswer 的区别**:
- `role` 为 `"ASSISTANT"` 而非 `"USER"`
- `roundNumber` 为下一轮（当前轮次 + 1）
- 包含 `skillId` 和 `skillName`（可为 null）

---

### 1.3 getLastQuestion() - 获取最后一条问题

**方法签名**:
```java
public InterviewMessage getLastQuestion(String sessionId)
```

**调用时序**:
```
Controller.sendMessage()
  │
  └──> InterviewMessageService.getLastQuestion(sessionId)
        │
        ├──> LambdaQueryWrapper<InterviewMessage>
        │     │
        │     ├──> .eq(InterviewMessage::getSessionId, sessionId)
        │     ├──> .eq(InterviewMessage::getRole, "ASSISTANT")
        │     ├──> .orderByDesc(InterviewMessage::getCreatedAt)
        │     └──> .last("LIMIT 1")
        │
        └──> messageMapper.selectList(wrapper)
              │
              └──> Database: SELECT * FROM interview_messages
                   WHERE session_id = ? AND role = 'ASSISTANT'
                   ORDER BY created_at DESC
                   LIMIT 1
```

**数据库 SQL**:
```sql
SELECT * FROM interview_messages
WHERE session_id = 'abc-123'
  AND role = 'ASSISTANT'
ORDER BY created_at DESC
LIMIT 1;
```

**返回值**:
- 找到：返回 `InterviewMessage` 对象
- 未找到：返回 `null`

---

### 1.4 getSessionMessages() - 获取会话所有消息

**方法签名**:
```java
public List<InterviewMessage> getSessionMessages(String sessionId)
```

**调用时序**:
```
Controller.buildNextStepContext()
  │
  └──> InterviewMessageService.getSessionMessages(sessionId)
        │
        ├──> LambdaQueryWrapper<InterviewMessage>
        │     │
        │     ├──> .eq(InterviewMessage::getSessionId, sessionId)
        │     ├──> .orderByAsc(InterviewMessage::getRoundNumber)
        │     └──> .orderByAsc(InterviewMessage::getSequenceInRound)
        │
        └──> messageMapper.selectList(wrapper)
              └──> Database: SELECT * FROM interview_messages
                   WHERE session_id = ?
                   ORDER BY round_number ASC, sequence_in_round ASC
```

**数据库 SQL**:
```sql
SELECT * FROM interview_messages
WHERE session_id = 'abc-123'
ORDER BY round_number ASC, sequence_in_round ASC;
```

**返回示例**:
```java
[
  InterviewMessage { roundNumber: 1, sequenceInRound: 1, role: "ASSISTANT", content: "自我介绍" },
  InterviewMessage { roundNumber: 1, sequenceInRound: 2, role: "USER", content: "我是..." },
  InterviewMessage { roundNumber: 2, sequenceInRound: 1, role: "ASSISTANT", content: "讲讲 Redis" },
  InterviewMessage { roundNumber: 2, sequenceInRound: 2, role: "USER", content: "Redis 是..." },
  ...
]
```

---

## 二、EvaluationService - 评估服务

### 2.1 createEvaluation() - 创建评分记录

**方法签名**:
```java
@Transactional
public MockInterviewEvaluation createEvaluation(MockInterviewEvaluation evaluation)
```

**调用时序**:
```
Controller.parseAndCreateEvaluation()
  │
  └──> EvaluationService.createEvaluation(evaluation)
        │
        ├──> setEvaluatedAt(LocalDateTime.now())
        │
        └──> evaluationMapper.insert(evaluation)
              │
              └──> Database: INSERT INTO mock_interview_evaluations
                   (session_id, round_number, skill_id, skill_name,
                    question_text, user_answer,
                    technical_score, logic_score, depth_score, total_score,
                    feedback, suggestion, evaluated_at)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
```

**详细执行步骤**:

| 步骤 | 操作 | 数据类型 | 说明 |
|-----|------|---------|------|
| 1 | 设置评估时间 | LocalDateTime | `evaluatedAt = NOW()` |
| 2 | 插入数据库 | int | MyBatis Plus 自动生成 ID |
| 3 | 记录日志 | void | 输出会话、轮次、技能、分数 |
| 4 | 事务提交 | void | @Transactional 保证原子性 |

**数据库 SQL**:
```sql
INSERT INTO mock_interview_evaluations (
    id, session_id, round_number, skill_id, skill_name,
    question_text, user_answer,
    technical_score, logic_score, depth_score, total_score,
    feedback, suggestion, evaluated_at
) VALUES (
    DEFAULT, 'abc-123', 3, 0, '通用',
    'Redis 集群如何实现数据分片？', 'Redis 集群使用一致性哈希...',
    3.5, 3.0, 2.5, 9.0,
    '回答基本正确，但深度不够...', '建议深入学习...',
    NOW()
);
```

**日志输出**:
```
INFO  c.j.service.EvaluationService - 创建评分记录，会话: abc-123, 轮次: 3, 技能: 通用, 分数: 9.0
```

---

### 2.2 getSessionEvaluations() - 获取会话所有评分

**方法签名**:
```java
public List<MockInterviewEvaluation> getSessionEvaluations(String sessionId)
```

**调用时序**:
```
Controller.finishInterview()
  │
  └──> EvaluationService.getSessionEvaluations(sessionId)
        │
        ├──> LambdaQueryWrapper<MockInterviewEvaluation>
        │     │
        │     ├──> .eq(MockInterviewEvaluation::getSessionId, sessionId)
        │     └──> .orderByAsc(MockInterviewEvaluation::getRoundNumber)
        │
        └──> evaluationMapper.selectList(wrapper)
              └──> Database: SELECT * FROM mock_interview_evaluations
                   WHERE session_id = ?
                   ORDER BY round_number ASC
```

**数据库 SQL**:
```sql
SELECT * FROM mock_interview_evaluations
WHERE session_id = 'abc-123'
ORDER BY round_number ASC;
```

---

### 2.3 calculateTotalScore() - 计算总分

**方法签名**:
```java
public BigDecimal calculateTotalScore(String sessionId)
```

**调用时序**:
```
Controller.finishInterview()
  │
  └──> EvaluationService.calculateTotalScore(sessionId)
        │
        ├──> getSessionEvaluations(sessionId)
        │     │
        │     └──> [查询所有评分记录]
        │
        ├──> evaluations.isEmpty()
        │     │
        │     ├──> true: return BigDecimal.ZERO
        │     └──> false: 继续计算
        │
        ├──> evaluations.stream()
        │     │
        │     ├──> .mapToDouble(e -> e.getTotalScore().doubleValue())
        │     ├──> .sum()
        │     │
        │     └──> [计算总分]
        │
        ├──> sum / evaluations.size()
        │     │
        │     └──> [计算平均分]
        │
        └──> BigDecimal.valueOf(avg).setScale(1, ROUND_HALF_UP)
```

**计算示例**:
```java
// 假设有 3 轮评分
evaluations = [
  { totalScore: 8.5 },
  { totalScore: 7.0 },
  { totalScore: 9.0 }
]

sum = 8.5 + 7.0 + 9.0 = 24.5
avg = 24.5 / 3 = 8.1666...
result = 8.2 (四舍五入，保留1位小数)
```

**返回值**: `BigDecimal.valueOf(8.2)`

---

## 三、InterviewAgentFactory - Agent 工厂

### 3.1 createAgents() - 创建三组 Agent

**方法签名**:
```java
public InterviewAgents createAgents(MockInterviewSession session)
```

**调用时序**:
```
Controller.startInterview()
  │
  └──> InterviewAgentFactory.createAgents(session)
        │
        ├──> 检查缓存: agentsCache.containsKey(sessionId)
        │     │
        │     ├──> true: 返回缓存
        │     └──> false: 继续创建
        │
        ├──> memoryProvider.getChatMemory(sessionId, MAIN_INTERVIEWER)
        │     │
        │     ├──> memoryCache.computeIfAbsent(sessionId, loadOrCreateSessionMemories)
        │     │     │
        │     │     ├──> 查询数据库: interview_memories
        │     │     │
        │     │     ├──> dbMemory == null?
        │     │     │     │
        │     │     │     ├──> true: 插入新记录
        │     │     │     └──> false: 从数据库加载
        │     │     │
        │     │     ├──> 创建 SessionMemories 容器
        │     │     │
        │     │     ├──> restoreChatMemory(mainInterviewerMemory)
        │     │     ├──> restoreChatMemory(viceInterviewerMemory)
        │     │     └──> restoreChatMemory(evaluatorMemory)
        │     │
        │     └──> sessionMemories.getMemory(MAIN_INTERVIEWER)
        │           │
        │           └──> memory == null?
        │                 │
        │                 ├──> true: createChatMemory() -> MessageWindowChatMemory
        │                 └──> false: 返回现有 memory
        │
        ├──> [重复上述步骤，获取 VICE_INTERVIEWER 和 EVALUATOR 的 ChatMemory]
        │
        ├──> buildContext(session)
        │     │
        │     ├──> 公司: session.getJobTitle()
        │     ├──> 岗位: session.getJobTitle()
        │     ├──> 级别: session.getSeniorityLevel()
        │     ├──> 查询简历: resumeService.getById(session.getResumeId())
        │     ├──> 工作年限: resume.getWorkYears()
        │     └──> 当前职位: resume.getCurrentPosition()
        │
        ├──> 创建主面试官
        │     │
        │     └──> AiServices.builder(MainInterviewerAgent.class)
        │           ├──> .chatModel(chatModel)
        │           ├──> .chatMemory(mainMemory)
        │           ├──> .systemMessage(buildMainInterviewerSystemMessage(context))
        │           └──> .build()
        │
        ├──> 创建副面试官
        │     │
        │     └──> AiServices.builder(ViceInterviewerAgent.class)
        │           ├──> .chatModel(chatModel)
        │           ├──> .chatMemory(viceMemory)
        │           ├──> .systemMessage(buildViceInterviewerSystemMessage(context))
        │           └──> .build()
        │
        ├──> 创建评审专家
        │     │
        │     └──> AiServices.builder(ExpertEvaluatorAgent.class)
        │           ├──> .chatModel(chatModel)
        │           ├──> .chatMemory(evalMemory)
        │           ├──> .systemMessage(buildExpertEvaluatorSystemMessage(context))
        │           └──> .build()
        │
        ├──> 组装 InterviewAgents
        │     │
        │     └──> new InterviewAgents(sessionId, mainInterviewer, viceInterviewer, evaluator)
        │
        ├──> 缓存: agentsCache.put(sessionId, agents)
        │
        └──> return agents
```

**内存结构**:
```
InterviewAgentFactory
  │
  └──> agentsCache: ConcurrentHashMap<String, InterviewAgents>
        │
        └──> "abc-123" -> InterviewAgents
              ├──> mainInterviewer: MainInterviewerAgent (ChatMemory: "abc-123:main")
              ├──> viceInterviewer: ViceInterviewerAgent (ChatMemory: "abc-123:vice")
              └──> evaluator: ExpertEvaluatorAgent (ChatMemory: "abc-123:evaluator")
```

**关键点**:
- 每个 Agent 有独立的 `ChatMemory` 实例
- `ChatMemory` 的 ID 格式：`sessionId:agentType`
- 三个 Agent 互不干扰，记忆完全隔离

---

### 3.2 persistMemories() - 持久化 Agent 记忆

**方法签名**:
```java
public void persistMemories(String sessionId)
```

**调用时序**:
```
Controller.sendMessage() / Controller.finishInterview()
  │
  └──> InterviewAgentFactory.persistMemories(sessionId)
        │
        └──> memoryProvider.persistAll(sessionId)
              │
              ├──> persistMemory(sessionId, MAIN_INTERVIEWER)
              │     │
              │     ├──> memoryCache.get(sessionId)
              │     │     │
              │     │     └──> SessionMemories
              │     │
              │     ├──> sessionMemories.getMemory(MAIN_INTERVIEWER)
              │     │     │
              │     │     └──> ChatMemory
              │     │
              │     ├──> convertToJson(memory)
              │     │     │
              │     │     ├──> memory.messages() -> List<ChatMessage>
              │     │     │
              │     │     ├──> 遍历消息
              │     │     │     │
              │     │     │     ├──> instanceof UserMessage
              │     │     │     │     └──> MessageDto("user", content)
              │     │     │     │
              │     │     │     └──> instanceof AiMessage
              │     │     │           └──> MessageDto("ai", content)
              │     │     │
              │     │     └──> objectMapper.writeValueAsString(messages)
              │     │
              │     ├──> 查询数据库: interview_memories WHERE session_id = ?
              │     │
              │     ├──> dbMemory != null?
              │     │     │
              │     │     ├──> true: 更新字段
              │     │     │     │
              │     │     │     └──> switch(agentType)
              │     │     │           ├──> MAIN_INTERVIEWER -> setMainInterviewerMemory(json)
              │     │     │           ├──> VICE_INTERVIEWER -> setViceInterviewerMemory(json)
              │     │     │           └──> EVALUATOR -> setEvaluatorMemory(json)
              │     │     │
              │     │     └──> memoryMapper.updateById(dbMemory)
              │     │
              │     └──> false: 记录警告日志
              │
              ├──> [重复上述步骤，持久化 VICE_INTERVIEWER]
              │
              └──> [重复上述步骤，持久化 EVALUATOR]
```

**JSON 序列化示例**:
```java
// ChatMemory.messages() 返回
[
  UserMessage { text: "Redis 集群使用一致性哈希..." },
  AiMessage { text: "回答基本正确，但深度不够..." },
  UserMessage { text: "那具体是如何分片的呢？" },
  AiMessage { text: "Redis Cluster 将数据分成 16384 个 Slot..." }
]

// 转换为 JSON
[
  {"role": "user", "content": "Redis 集群使用一致性哈希..."},
  {"role": "ai", "content": "回答基本正确，但深度不够..."},
  {"role": "user", "content": "那具体是如何分片的呢？"},
  {"role": "ai", "content": "Redis Cluster 将数据分成 16384 个 Slot..."}
]
```

**数据库操作** (每个 Agent 一次):
```sql
-- 更新主面试官记忆
UPDATE interview_memories
SET main_interviewer_memory = '[{"role":"user",...}]',
    updated_at = NOW()
WHERE session_id = 'abc-123';

-- 更新副面试官记忆
UPDATE interview_memories
SET vice_interviewer_memory = '[{"role":"user",...}]',
    updated_at = NOW()
WHERE session_id = 'abc-123';

-- 更新评审专家记忆
UPDATE interview_memories
SET evaluator_memory = '[{"role":"user",...}]',
    updated_at = NOW()
WHERE session_id = 'abc-123';
```

**总共**: 3 次 UPDATE 操作

---

# 第二部分：Agent 层方法详解

## 四、LangChain4j Agent 调用机制

### 4.1 Agent 调用基础流程

所有 Agent 方法都遵循相同的调用模式：

```
Java Agent 接口方法
  │
  ├──> ChatMemoryProvider.getChatMemory()
  │     │
  │     └──> 返回对应的 ChatMemory 实例
  │
  ├──> 构建消息列表
  │     │
  │     ├──> SystemMessage (来自 @SystemMessage 注解或构建时设置)
  │     ├──> ChatMemory 中的历史消息
  │     └── 当前的 UserMessage
  │
  ├──> ChatModel.generate()
  │     │
  │     ├──> HTTP POST to LLM API
  │     │     │
  │     │     └──> http://127.0.0.1:1234/v1/chat/completions
  │     │
  │     └──> 等待 LLM 响应 (1-3秒)
  │
  ├──> 解析响应
  │     │
  │     └──> 提取 content 字段
  │
  ├──> 更新 ChatMemory
  │     │
  │     ├──> memory.add(UserMessage)
  │     └──> memory.add(AiMessage)
  │
  └──> 返回结果
```

---

## 五、ExpertEvaluatorAgent.evaluate() - 评估用户回答

### 5.1 调用时序

```
Controller.sendMessage()
  │
  └──> agents.evaluator().evaluate(evaluationContext)
        │
        ├──> [LangChain4j 内部流程]
        │     │
        │     ├──> ChatMemory (EVALUATOR)
        │     │     │
        │     │     ├──> messages() -> List<ChatMessage>
        │     │     │
        │     │     └──> [
        │     │           UserMessage "介绍一下你自己",
        │     │           AiMessage "我是后端工程师...",
        │     │           UserMessage "Redis 集群如何实现数据分片？",
        │     │           AiMessage "Redis 集群使用一致性哈希...",
        │     │           ...
        │     │         ]
        │     │
        │     ├──> 构建完整消息列表
        │     │     │
        │     │     ├──> SystemMessage (来自 buildExpertEvaluatorSystemMessage)
        │     │     │     │
        │     │     │     └──> """
        │     │     │         # 角色设定
        │     │     │         你是资深技术评审专家，负责评估候选人...
        │     │     │
        │     │     │         # 评分维度（总分 10 分）
        │     │     │         ...
        │     │     │         """
        │     │     │
        │     │     ├──> ChatMemory 中的历史消息
        │     │     │
        │     │     └──> UserMessage (evaluationContext)
        │     │           │
        │     │           └──> """
        │     │               # 评估任务
        │     │               请评估候选人的第 3 轮回答
        │     │
        │     │               # 问题
        │     │               Redis 集群如何实现数据分片？
        │     │
        │     │               # 用户回答
        │     │               Redis 集群使用一致性哈希...
        │     │
        │     │               # 简历快照
        │     │               {"work_years": 3, "position": "后端工程师"}
        │     │
        │     │               # JD 要求
        │     │               {"skills": ["Redis", "MySQL", "Kafka"]}
        │     │               """
        │     │
        │     ├──> OpenAiChatModel.generate(messages)
        │     │     │
        │     │     ├──> 构建请求体
        │     │     │     │
        │     │     │     └──> {
        │     │     │           "model": "google/gemma-3-4b",
        │     │     │           "messages": [
        │     │     │             {"role": "system", "content": "你是资深技术评审专家..."},
        │     │     │             {"role": "user", "content": "介绍一下你自己"},
        │     │     │             {"role": "assistant", "content": "我是后端工程师..."},
        │     │     │             ...
        │     │     │             {"role": "user", "content": "# 评估任务\n请评估..."}
        │     │     │           ],
        │     │     │           "temperature": 0.7
        │     │     │         }
        │     │     │
        │     │     ├──> HTTP POST http://127.0.0.1:1234/v1/chat/completions
        │     │     │
        │     │     ├──> 等待 LM Studio 响应 (1-3秒)
        │     │     │
        │     │     └──> 解析响应
        │     │           │
        │     │           └──> {
        │     │                 "choices": [
        │     │                   {
        │     │                     "message": {
        │     │                       "role": "assistant",
        │     │                       "content": "```json\n{\n  \"scores\": {\n    \"technical\": 3.5,\n..."
        │     │                     }
        │     │                   }
        │     │                 ]
        │     │               }
        │     │
        │     ├──> 提取 content: String
        │     │     │
        │     │     └──> "```json\n{\n  \"scores\": {\n    \"technical\": 3.5,\n    \"logic\": 3.0,\n    \"depth\": 2.5\n  },\n  \"total_score\": 9.0,\n  \"credibility_assessment\": {\n    \"match_level\": \"部分夸大\",\n    \"gap_description\": \"回答基本正确，但深度不够\",\n    \"exaggeration_score\": 0.4\n  },\n  \"feedback\": \"候选人知道 Redis 集群的基本概念...\",\n  \"suggestion\": \"建议深入学习 Slot 迁移机制\"\n}\n```"
        │     │
        │     ├──> 更新 ChatMemory
        │     │     │
        │     │     ├──> memory.add(UserMessage(evaluationContext))
        │     │     └──> memory.add(AiMessage(response))
        │     │
        │     └──> 返回 response
        │
        └──> String evaluationResult = "```json\n{...}\n```"
```

### 5.2 HTTP 请求详情

**请求 URL**:
```
POST http://127.0.0.1:1234/v1/chat/completions
```

**请求头**:
```http
Content-Type: application/json
Authorization: Bearer lm-studio
```

**请求体**:
```json
{
  "model": "google/gemma-3-4b",
  "messages": [
    {
      "role": "system",
      "content": "# 角色设定\n你是资深技术评审专家，负责评估候选人并分析简历真实性...\n\n# 评分维度（总分 10 分）\n## 1. 技术准确性（4 分）\n- 概念是否正确\n- 有无明显错误\n- **与简历声称的匹配度**\n..."
    },
    {
      "role": "user",
      "content": "介绍一下你自己"
    },
    {
      "role": "assistant",
      "content": "我是后端工程师，有3年经验..."
    },
    {
      "role": "user",
      "content": "Redis 集群如何实现数据分片？"
    },
    {
      "role": "assistant",
      "content": "Redis 集群使用一致性哈希..."
    },
    {
      "role": "user",
      "content": "# 评估任务\n请评估候选人的第 3 轮回答\n\n# 问题\nRedis 集群如何实现数据分片？\n\n# 用户回答\nRedis 集群使用一致性哈希...\n\n# 简历快照\n{\"work_years\": 3, \"position\": \"后端工程师\"}\n\n# JD 要求\n{\"skills\": [\"Redis\", \"MySQL\", \"Kafka\"]}\n\n# 评分要求\n请按以下 JSON 格式返回评估结果：\n{\n  \"scores\": {...},\n  \"total_score\": 10.0,\n  \"credibility_assessment\": {...},\n  \"feedback\": \"详细反馈...\",\n  \"suggestion\": \"改进建议...\"\n}"
    }
  ],
  "temperature": 0.7,
  "stream": false
}
```

**响应体**:
```json
{
  "id": "chatcmpl-123",
  "object": "chat.completion",
  "created": 1677652288,
  "model": "google/gemma-3-4b",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "```json\n{\n  \"scores\": {\n    \"technical\": 3.5,\n    \"logic\": 3.0,\n    \"depth\": 2.5\n  },\n  \"total_score\": 9.0,\n  \"credibility_assessment\": {\n    \"match_level\": \"部分夸大\",\n    \"gap_description\": \"回答基本正确，但深度不够\",\n    \"exaggeration_score\": 0.4\n  },\n  \"feedback\": \"候选人知道 Redis 集群的基本概念，能够说出一致性哈希，但对具体实现细节描述不够深入。简历声称精通 Redis，实际回答显示对集群模式的了解停留在表面。\",\n  \"suggestion\": \"建议深入学习 Redis Cluster 的 Slot 分配机制、数据迁移流程以及故障恢复原理\"\n}\n```"
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 520,
    "completion_tokens": 180,
    "total_tokens": 700
  }
}
```

---

## 六、ViceInterviewerAgent.decideNextStep() - 决定下一步

### 6.1 调用时序

```
Controller.sendMessage()
  │
  └──> agents.viceInterviewer().decideNextStep(nextStepContext)
        │
        ├──> [LangChain4j 内部流程]
        │     │
        │     ├──> ChatMemory (VICE_INTERVIEWER)
        │     │     │
        │     │     └──> [
        │     │           UserMessage "用户最新回答: ...",
        │     │           AiMessage "NEXT_QUESTION: Redis 集群",
        │     │           UserMessage "用户最新回答: ...",
        │     │           AiMessage "NEXT_QUESTION: Redis 持久化",
        │     │           ...
        │     │         ]
        │     │
        │     ├──> SystemMessage (来自 buildViceInterviewerSystemMessage)
        │     │     │
        │     │     └──> """
        │     │         # 角色设定
        │     │         你是面试流程控制器，负责维护面试进度和智能选题。
        │     │
        │     │         # 选题策略（优先级）
        │     │         ## 1. 项目深挖（最高优先级）
        │     │         ## 2. 技能验证（次优先级）
        │     │         ### ⚠️ 技能验证停止条件（重要！）
        │     │         ...
        │     │         """
        │     │
        │     ├──> UserMessage (nextStepContext)
        │     │     │
        │     │     └──> """
        │     │         用户最新回答: Redis 集群使用一致性哈希...
        │     │         已考察知识点: ["HashMap", "Redis"]
        │     │         待考察知识点: ["MySQL", "Kafka", "分布式事务"]
        │     │
        │     │         # 最近 5 轮对话历史
        │     │         好的，我们来聊聊Redis集群...
        │     │         Redis 集群如何实现数据分片？
        │     │         Redis 集群使用一致性哈希...
        │     │         那持久化方式有哪些？
        │     │
        │     │         # 统计信息
        │     │         - 当前轮次: 3
        │     │         - 总消息数: 15
        │     │         """
        │     │
        │     ├──> OpenAiChatModel.generate(messages)
        │     │
        │     └──> 提取 content
        │
        └──> String nextStep = "{
  \"action\": \"NEXT_QUESTION\",
  \"next_topic\": \"MySQL 索引优化\",
  \"topic_source\": \"JD_REQUIREMENT\",
  \"reason\": \"Redis 相关问题已经问了3轮，用户回答深度不够，切换到 JD 要求的 MySQL\",
  \"question_type\": \"SKILL_VERIFICATION\",
  \"difficulty\": 3
}"
```

### 6.2 停止条件判断逻辑

副面试官在决定下一步时会检查以下停止条件：

```java
// 在 buildNextStepContext() 中包含最近对话历史
String recentTopics = """
最近 5 轮对话历史:
1. "好的，我们来聊聊Redis集群..."
2. "Redis 集群如何实现数据分片？"
3. "Redis 集群使用一致性哈希..."
4. "那持久化方式有哪些？"
5. "我不太清楚..."
""";

// LLM 根据这些信息判断：
if (用户连续2次说"不知道" ||
    用户要求"换题" ||
    同一技能已验证超过3轮 ||
    用户回答与技能无关) {
    切换到其他话题;
}
```

---

## 七、MainInterviewerAgent.askQuestion() - 生成问题

### 7.1 调用时序

```
Controller.sendMessage()
  │
  └──> agents.mainInterviewer().askQuestion(questionContext)
        │
        ├──> [LangChain4j 内部流程]
        │     │
        │     ├──> ChatMemory (MAIN_INTERVIEWER)
        │     │     │
        │     │     └──> [
        │     │           UserMessage "下一步决策: ...",
        │     │           AiMessage "好的，我们来聊聊Redis集群...",
        │     │           UserMessage "下一步决策: ...",
        │     │           AiMessage "那Redis持久化方式有哪些？",
        │     │           ...
        │     │         ]
        │     │
        │     ├──> SystemMessage (来自 buildMainInterviewerSystemMessage)
        │     │     │
        │     │     └──> """
        │     │         # 角色设定
        │     │         你是 JobTracker 模拟面试系统的主面试官...
        │     │
        │     │         # 你的职责
        │     │         ## 1. 提问策略（优先级从高到低）
        │     │         1. **项目深挖**（最高优先级）
        │     │         2. **技能验证**
        │     │         3. **JD 要求覆盖**
        │     │         4. **经验匹配度评估**
        │     │
        │     │         # 严格约束
        │     │         1. **严禁评分**：不要提及分数、等级
        │     │         2. **严禁重复**：不要问已考察过的知识点
        │     │         3. **单一问题**：每次只问一个问题
        │     │         4. **长度控制**：回复控制在 200 字以内
        │     │         5. **必须结合简历**：尽量基于简历内容提问
        │     │         """
        │     │
        │     ├──> UserMessage (questionContext)
        │     │     │
        │     │     └──> """
        │     │         下一步决策: {
        │     │           "action": "NEXT_QUESTION",
        │     │           "next_topic": "MySQL 索引优化",
        │     │           "topic_source": "JD_REQUIREMENT",
        │     │           "reason": "Redis 相关问题已经问了3轮...",
        │     │           "question_type": "SKILL_VERIFICATION",
        │     │           "difficulty": 3
        │     │         }
        │     │         岗位: 后端工程师
        │     │         级别: 中级
        │     │         简历快照: {"work_years": 3, "position": "后端工程师"}
        │     │         JD 快照: {"skills": ["Redis", "MySQL", "Kafka"]}
        │     │         """
        │     │
        │     ├──> OpenAiChatModel.generate(messages)
        │     │
        │     └──> 提取 content
        │
        └──> String question = "好的，我们换个话题。你在简历中提到熟悉 MySQL，那我想问一下，MySQL 的索引为什么使用 B+ 树而不是 B 树或者哈希表？"
```

---

## 八、面试结束时的 Agent 调用

### 8.1 ExpertEvaluatorAgent.generateCredibilityAnalysis()

```
Controller.finishInterview()
  │
  └──> agents.evaluator().generateCredibilityAnalysis(reportContext)
        │
        ├──> ChatMemory (EVALUATOR) - 包含所有评估历史
        │
        ├──> SystemMessage (评审专家角色)
        │
        ├──> UserMessage (完整报告上下文)
        │     │
        │     └──> """
        │         # 面试基本信息
        │         - 公司: 字节跳动
        │         - 岗位: 后端工程师
        │         - 面试轮次: 5
        │
        │         # 评分记录汇总
        │         ## 轮次 1 - 通用
        │         - 技术分: 3.5
        │         - 逻辑分: 3.0
        │         - 深度分: 2.5
        │         - 总分: 9.0
        │
        │         ## 轮次 2 - Redis
        │         ...
        │         """
        │
        ├──> OpenAiChatModel.generate()
        │
        └──> 返回 JSON 数组
              │
              └──> """
                  [
                    {
                      "skillName": "Redis",
                      "claimedLevel": "精通",
                      "actualLevel": "了解",
                      "exaggerationLevel": "high",
                      "comment": "简历声称精通 Redis，但对集群模式和持久化的理解不够深入"
                    },
                    {
                      "skillName": "MySQL",
                      "claimedLevel": "熟悉",
                      "actualLevel": "熟悉",
                      "exaggerationLevel": "none",
                      "comment": "回答与简历声称一致"
                    }
                  ]
                  """
```

### 8.2 ViceInterviewerAgent.generateSuggestions()

```
Controller.finishInterview()
  │
  └──> agents.viceInterviewer().generateSuggestions(reportContext)
        │
        ├──> ChatMemory (VICE_INTERVIEWER) - 包含所有决策历史
        │
        ├──> SystemMessage (副面试官角色)
        │
        ├──> UserMessage (完整报告上下文)
        │
        ├──> OpenAiChatModel.generate()
        │
        └──> 返回 JSON 数组
              │
              └──> """
                  [
                    {
                      "category": "技术深度",
                      "title": "深入学习 Redis 集群原理",
                      "description": "在 Redis 集群相关问题上表现较弱，建议学习 Slot 分配机制、数据迁移流程等...",
                      "resources": [
                        "Redis 集群原理与实战",
                        "分布式缓存架构设计"
                      ],
                      "priority": "high"
                    },
                    {
                      "category": "项目经验",
                      "title": "使用 STAR 法则描述项目",
                      "description": "项目经验描述较为笼统，建议使用 STAR 法则（情境-任务-行动-结果）来组织回答...",
                      "priority": "medium"
                    }
                  ]
                  """
```

### 8.3 MainInterviewerAgent.generateSummary()

```
Controller.finishInterview()
  │
  └──> agents.mainInterviewer().generateSummary(reportContext)
        │
        ├──> ChatMemory (MAIN_INTERVIEWER) - 包含所有提问历史
        │
        ├──> SystemMessage (主面试官角色)
        │
        ├──> UserMessage (完整报告上下文)
        │
        ├──> OpenAiChatModel.generate()
        │
        └──> 返回文本总结
              │
              └──> """
                  # 面试总结

                  ## 整体表现
                  候选人在本次面试中表现中等偏上，能够正确回答大部分基础问题，但在深度和细节方面有所欠缺。

                  ## 优势亮点
                  1. 基础知识扎实，对常见技术栈有较好理解
                  2. 逻辑清晰，表达有条理
                  3. 学习意愿强，能够承认不足

                  ## 待改进方面
                  1. Redis 集群模式理解不够深入
                  2. 项目经验描述较为笼统
                  3. 对底层原理的掌握有待加强

                  ## 推荐方向
                  建议深入学习分布式系统原理，加强项目经验的积累和总结。
                  """
```

---

## 九、性能分析

### 9.1 Agent 调用性能指标

| 指标 | evaluate() | decideNextStep() | askQuestion() | generateCredibilityAnalysis() | generateSuggestions() | generateSummary() |
|-----|-----------|------------------|---------------|-------------------------------|----------------------|-------------------|
| 输入 Token | ~800 | ~600 | ~500 | ~2000 | ~2000 | ~2000 |
| 输出 Token | ~200 | ~150 | ~100 | ~300 | ~250 | ~400 |
| 响应时间 | 2-3秒 | 1-2秒 | 1-2秒 | 5-8秒 | 4-6秒 | 6-10秒 |
| 内存占用 | ~500KB | ~300KB | ~200KB | ~2MB | ~1.5MB | ~2.5MB |

### 9.2 内存使用分析

**单个会话的内存占用**:
```
InterviewAgentFactory.agentsCache
  │
  └──> "abc-123" -> InterviewAgents (~5MB)
        ├──> mainInterviewer (~1.5MB)
        │     └──> ChatMemory (~1.5MB)
        │           └──> 50条消息 × 30KB/条
        │
        ├──> viceInterviewer (~1MB)
        │     └──> ChatMemory (~1MB)
        │           └──> 50条消息 × 20KB/条
        │
        └──> evaluator (~2.5MB)
              └──> ChatMemory (~2.5MB)
                    └──> 50条消息 × 50KB/条 (包含评估结果)
```

**总内存占用**: 约 5MB/会话

---

## 十、关键设计模式

### 10.1 Factory 模式

```java
// InterviewAgentFactory
public InterviewAgents createAgents(MockInterviewSession session) {
    // 动态创建三个 Agent
    MainInterviewerAgent main = AiServices.builder(MainInterviewerAgent.class)
        .chatMemory(getChatMemory(sessionId, MAIN_INTERVIEWER))
        .build();

    ViceInterviewerAgent vice = AiServices.builder(ViceInterviewerAgent.class)
        .chatMemory(getChatMemory(sessionId, VICE_INTERVIEWER))
        .build();

    ExpertEvaluatorAgent eval = AiServices.builder(ExpertEvaluatorAgent.class)
        .chatMemory(getChatMemory(sessionId, EVALUATOR))
        .build();

    return new InterviewAgents(sessionId, main, vice, eval);
}
```

### 10.2 Provider 模式

```java
// InterviewMemoryProvider
public ChatMemory getChatMemory(String sessionId, AgentType agentType) {
    // 1. 获取或创建会话记忆容器
    SessionMemories sessionMemories = memoryCache.computeIfAbsent(
        sessionId,
        id -> loadOrCreateSessionMemories(id)
    );

    // 2. 获取或创建 Agent 记忆
    ChatMemory memory = sessionMemories.getMemory(agentType);
    if (memory == null) {
        memory = createChatMemory(sessionId, agentType);
        sessionMemories.setMemory(agentType, memory);
    }

    return memory;
}
```

### 10.3 Builder 模式

```java
// InterviewMessage
InterviewMessage message = InterviewMessage.builder()
    .sessionId(sessionId)
    .roundNumber(roundNumber)
    .sequenceInRound(sequence)
    .role("USER")
    .content(content)
    .createdAt(LocalDateTime.now())
    .build();

// MockInterviewEvaluation
MockInterviewEvaluation evaluation = MockInterviewEvaluation.builder()
    .sessionId(sessionId)
    .roundNumber(roundNumber)
    .skillId(0L)
    .skillName("通用")
    .technicalScore(BigDecimal.valueOf(3.5))
    .logicScore(BigDecimal.valueOf(3.0))
    .depthScore(BigDecimal.valueOf(2.5))
    .totalScore(BigDecimal.valueOf(9.0))
    .feedback("...")
    .suggestion("...")
    .build();
```

---

## 十一、异常处理

### 11.1 Service 层异常处理

```java
// InterviewMessageService
public InterviewMessage getLastQuestion(String sessionId) {
    List<InterviewMessage> messages = messageMapper.selectList(...);
    return messages.isEmpty() ? null : messages.get(0);
    // 不抛出异常，返回 null
}

// EvaluationService
public BigDecimal calculateTotalScore(String sessionId) {
    List<MockInterviewEvaluation> evaluations = getSessionEvaluations(sessionId);
    if (evaluations.isEmpty()) {
        return BigDecimal.ZERO;
        // 不抛出异常，返回默认值
    }
    // ...
}
```

### 11.2 Agent 层异常处理

```java
// Controller
try {
    String evaluationResult = agents.evaluator().evaluate(evaluationContext);
    MockInterviewEvaluation evaluation = parseAndCreateEvaluation(...);
} catch (Exception e) {
    log.warn("评估回答失败，跳过本轮评分", e);
    // 不中断流程，继续后续步骤
}

// parseAndCreateEvaluation
try {
    Map<String, Object> result = mapper.readValue(cleanedJson, Map.class);
    // ...
} catch (Exception e) {
    log.warn("解析失败，使用默认值");
    return createDefaultEvaluation(...);
    // 降级处理，使用默认值
}
```

---

## 十二、总结

本文档详细描述了：

### Service 层
- **InterviewMessageService**: 消息的增删查改
- **EvaluationService**: 评分记录的创建和计算
- **InterviewAgentFactory**: Agent 的创建和记忆管理

### Agent 层
- **ExpertEvaluatorAgent**: 评估用户回答、生成可信度分析
- **ViceInterviewerAgent**: 决定下一步、生成改进建议
- **MainInterviewerAgent**: 生成问题、生成面试总结

### 关键流程
1. **消息流程**: 保存用户消息 → 获取上一轮问题 → 构建上下文 → Agent 评估 → 保存评分
2. **决策流程**: 获取历史消息 → 构建下一步上下文 → Agent 决策 → Agent 提问 → 保存问题
3. **报告流程**: 获取所有评分 → 构建报告上下文 → Agent 生成报告 → 持久化记忆 → 更新会话

### 性能优化点
1. **内存缓存**: `ConcurrentHashMap` 缓存 Agent 和 ChatMemory
2. **每轮持久化**: 防止长时间面试导致数据丢失
3. **宽松解析**: 处理 AI 返回的不规范 JSON
4. **降级处理**: Agent 不可用时使用简化版报告
