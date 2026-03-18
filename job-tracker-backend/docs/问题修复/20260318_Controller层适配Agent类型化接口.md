# Controller 层适配 Agent 类型化接口

## 文档概述

**修复日期**: 2026-03-18
**修复文件**: `MockInterviewController.java`
**问题**: Agent 接口已改为返回类型化对象，但 Controller 层仍期望返回 String，导致编译错误

---

## 问题分析

### 编译错误

```
java: 不兼容的类型: com.jobtracker.agent.interview.dto.EvaluationResult无法转换为java.lang.String
```

### 根本原因

Agent 接口已重构为返回类型化对象：
- `ViceInterviewerAgent.decideNextStep()` 现在返回 `NextStepDecision`
- `ExpertEvaluatorAgent.evaluate()` 现在返回 `EvaluationResult`
- `ExpertEvaluatorAgent.generateCredibilityAnalysis()` 现在返回 `List<SkillCredibility>`
- `ExpertEvaluatorAgent.calculateCredibilityScore()` 现在返回 `CredibilityScoreResult`
- `ViceInterviewerAgent.generateSuggestions()` 现在返回 `List<ImprovementSuggestion>`

但 Controller 层的调用代码仍然期望返回 `String`，导致类型不匹配。

---

## 修复内容

### 1. sendMessage() 方法 - 评估用户回答

**修改前**（期望 JSON 字符串）：
```java
// 调用评审专家评估
String evaluationResult = agents.evaluator().evaluate(evaluationContext);

// 解析评估结果并创建评分记录
MockInterviewEvaluation evaluation = parseAndCreateEvaluation(
        sessionId,
        session.getCurrentRound(),
        lastQuestion.getContent(),
        request.getContent(),
        evaluationResult  // 传入 JSON 字符串
);
```

**修改后**（接收 EvaluationResult 对象）：
```java
// 调用评审专家评估（现在返回 EvaluationResult 对象）
com.jobtracker.agent.interview.dto.EvaluationResult evaluationResult =
        agents.evaluator().evaluate(evaluationContext);

// 从 EvaluationResult 对象创建评分记录
MockInterviewEvaluation evaluation = createEvaluationFromResult(
        sessionId,
        session.getCurrentRound(),
        lastQuestion.getContent(),
        request.getContent(),
        evaluationResult  // 传入对象
);
```

---

### 2. sendMessage() 方法 - 副面试官决策

**修改前**（期望 JSON 字符串）：
```java
// 副面试官决定下一步
String nextStepContext = buildNextStepContext(session, request.getContent());
String nextStep = agents.viceInterviewer().decideNextStep(nextStepContext);

// 主面试官生成问题
String questionContext = buildQuestionContext(session, nextStep);
```

**修改后**（接收 NextStepDecision 对象）：
```java
// 副面试官决定下一步（现在返回 NextStepDecision 对象）
String nextStepContext = buildNextStepContext(session, request.getContent());
com.jobtracker.agent.interview.dto.NextStepDecision nextStepDecision =
        agents.viceInterviewer().decideNextStep(nextStepContext);

// 主面试官生成问题
String questionContext = buildQuestionContext(session, nextStepDecision);
```

---

### 3. finishInterview() 方法 - 生成可信度分析

**修改前**（期望 JSON 字符串）：
```java
// 调用评审专家生成可信度分析
String credibilityAnalysisJson = agents.evaluator().generateCredibilityAnalysis(reportContext);
// 验证是否为有效 JSON，如果不是则包装为 JSON
if (credibilityAnalysisJson != null && !credibilityAnalysisJson.trim().startsWith("[")) {
    credibilityAnalysisJson = "[{\"skillName\": \"未知\", ...}]";
}
session.setResumeGapAnalysis(credibilityAnalysisJson);
```

**修改后**（接收 List<SkillCredibility>）：
```java
// 调用评审专家生成可信度分析（现在返回 List<SkillCredibility>）
java.util.List<com.jobtracker.agent.interview.dto.SkillCredibility> credibilityList =
        agents.evaluator().generateCredibilityAnalysis(reportContext);

// 序列化为 JSON 字符串存储
com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
String credibilityAnalysisJson = mapper.writeValueAsString(credibilityList);
session.setResumeGapAnalysis(credibilityAnalysisJson);
```

---

### 4. finishInterview() 方法 - 计算总体可信度评分

**修改前**（期望 JSON 字符串）：
```java
// 计算总体可信度评分
String credibilityScoreJson = agents.evaluator().calculateCredibilityScore(reportContext);
Double credibilityScore = 0.5;
try {
    // 去除可能的 Markdown 标记
    String cleanedJson = credibilityScoreJson
        .replaceAll("```json\\s*", "")
        .replaceAll("```\\s*$", "")
        .trim();
    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
    java.util.Map<String, Object> scoreResult = mapper.readValue(cleanedJson, java.util.Map.class);
    Object scoreValue = scoreResult.get("credibility_score");
    if (scoreValue instanceof Number) {
        credibilityScore = ((Number) scoreValue).doubleValue();
    }
} catch (Exception e) {
    log.warn("解析可信度评分失败，使用默认值 0.5", e);
}
session.setResumeCredibilityScore(java.math.BigDecimal.valueOf(credibilityScore));
```

**修改后**（接收 CredibilityScoreResult 对象）：
```java
// 计算总体可信度评分（现在返回 CredibilityScoreResult 对象）
com.jobtracker.agent.interview.dto.CredibilityScoreResult credibilityScoreResult =
        agents.evaluator().calculateCredibilityScore(reportContext);
session.setResumeCredibilityScore(java.math.BigDecimal.valueOf(
        credibilityScoreResult.getCredibilityScore()));
```

---

### 5. finishInterview() 方法 - 生成改进建议

**修改前**（期望 JSON 字符串）：
```java
// 调用副面试官生成改进建议
String suggestionsJson = agents.viceInterviewer().generateSuggestions(reportContext);
// 验证是否为有效 JSON，如果不是则包装为 JSON
if (suggestionsJson != null && !suggestionsJson.trim().startsWith("[")) {
    suggestionsJson = "[{\"title\": \"无改进建议\", ...}]";
}
session.setImprovementSuggestions(suggestionsJson);
```

**修改后**（接收 List<ImprovementSuggestion>）：
```java
// 调用副面试官生成改进建议（现在返回 List<ImprovementSuggestion>）
java.util.List<com.jobtracker.agent.interview.dto.ImprovementSuggestion> suggestions =
        agents.viceInterviewer().generateSuggestions(reportContext);

// 序列化为 JSON 字符串存储
String suggestionsJson = mapper.writeValueAsString(suggestions);
session.setImprovementSuggestions(suggestionsJson);
```

---

### 6. evaluateAnswer() 方法 - 手动评估单轮回答

**修改前**（期望 JSON 字符串）：
```java
// 评估回答
String context = buildEvaluationContext(session, request);
String evaluationResult = agents.evaluator().evaluate(context);

// TODO: 解析 JSON 并保存评分
// 当前简化处理
MockInterviewEvaluation evaluation = new MockInterviewEvaluation();
evaluation.setSessionId(sessionId);
evaluation.setRoundNumber(request.getRoundNumber());
evaluation.setQuestionText(request.getQuestion());
evaluation.setUserAnswer(request.getAnswer());
evaluation.setTotalScore(java.math.BigDecimal.valueOf(7.0));
evaluation.setFeedback(evaluationResult);
```

**修改后**（接收 EvaluationResult 对象）：
```java
// 评估回答（现在返回 EvaluationResult 对象）
String context = buildEvaluationContext(session, request);
com.jobtracker.agent.interview.dto.EvaluationResult evaluationResult =
        agents.evaluator().evaluate(context);

// 从 EvaluationResult 对象创建评分记录
MockInterviewEvaluation evaluation = createEvaluationFromResult(
        sessionId,
        request.getRoundNumber(),
        request.getQuestion(),
        request.getAnswer(),
        evaluationResult  // 传入对象
);
```

---

### 7. buildQuestionContext() 方法 - 参数类型变更

**修改前**（接收 String）：
```java
private String buildQuestionContext(MockInterviewSession session, String nextStep) {
    return String.format("""
            下一步决策: %s
            岗位: %s
            级别: %s
            简历快照: %s
            JD 快照: %s
            """,
            nextStep,  // String 类型的决策结果
            session.getJobTitle(),
            session.getSeniorityLevel(),
            session.getResumeSnapshot(),
            session.getJdSnapshot()
    );
}
```

**修改后**（接收 NextStepDecision）：
```java
private String buildQuestionContext(
        MockInterviewSession session,
        com.jobtracker.agent.interview.dto.NextStepDecision nextStepDecision
) {
    return String.format("""
            下一步决策: %s
            选题来源: %s
            选择原因: %s
            难度等级: %d

            岗位: %s
            级别: %s
            简历快照: %s
            JD 快照: %s
            """,
            nextStepDecision.getNextTopic(),      // 从对象中获取
            nextStepDecision.getTopicSource(),   // 从对象中获取
            nextStepDecision.getReason(),        // 从对象中获取
            nextStepDecision.getDifficulty(),    // 从对象中获取
            session.getJobTitle(),
            session.getSeniorityLevel(),
            session.getResumeSnapshot(),
            session.getJdSnapshot()
    );
}
```

---

### 8. 移除 JSON 格式说明

**修改前**（包含 JSON 格式说明）：
```java
private String buildEvaluationContextForRound(...) {
    return String.format("""
            # 评估任务
            请评估候选人的第 %d 轮回答

            # 问题
            %s

            # 用户回答
            %s

            # 简历快照
            %s

            # JD 要求
            %s

            # 评分要求
            请按以下 JSON 格式返回评估结果：
            {
              "scores": {
                "technical": 4.0,
                "logic": 3.0,
                "depth": 3.0
              },
              "total_score": 10.0,
              ...
            }
            """,
            roundNumber, question, userAnswer,
            session.getResumeSnapshot(),
            session.getJdSnapshot()
    );
}
```

**修改后**（移除 JSON 格式说明）：
```java
private String buildEvaluationContextForRound(...) {
    return String.format("""
            # 评估任务
            请评估候选人的第 %d 轮回答

            # 问题
            %s

            # 用户回答
            %s

            # 简历快照
            %s

            # JD 要求
            %s
            """,
            roundNumber, question, userAnswer,
            session.getResumeSnapshot(),
            session.getJdSnapshot()
    );
}
```

---

### 9. 替换方法：parseAndCreateEvaluation() → createEvaluationFromResult()

**删除**：`parseAndCreateEvaluation()` 方法（负责解析 JSON 字符串）

**新增**：`createEvaluationFromResult()` 方法（直接从对象创建）

```java
/**
 * 从 EvaluationResult 对象创建评分记录
 * <p>
 * LangChain4j 会自动将 Agent 返回的 JSON 反序列化为 EvaluationResult 对象
 * </p>
 */
private MockInterviewEvaluation createEvaluationFromResult(
        String sessionId,
        Integer roundNumber,
        String question,
        String userAnswer,
        com.jobtracker.agent.interview.dto.EvaluationResult evaluationResult
) {
    try {
        com.jobtracker.agent.interview.dto.ScoreDetail scores = evaluationResult.getScores();

        // 创建评分记录
        MockInterviewEvaluation evaluation = MockInterviewEvaluation.builder()
                .sessionId(sessionId)
                .roundNumber(roundNumber)
                .skillId(0L)
                .skillName("通用")
                .questionText(question)
                .userAnswer(userAnswer)
                .technicalScore(java.math.BigDecimal.valueOf(
                        scores != null ? scores.getTechnical() : 2.5))
                .logicScore(java.math.BigDecimal.valueOf(
                        scores != null ? scores.getLogic() : 2.5))
                .depthScore(java.math.BigDecimal.valueOf(
                        scores != null ? scores.getDepth() : 2.5))
                .totalScore(java.math.BigDecimal.valueOf(
                        evaluationResult.getTotalScore() != null ?
                                evaluationResult.getTotalScore() : 7.5))
                .feedback(evaluationResult.getFeedback())
                .suggestion(evaluationResult.getSuggestion())
                .build();

        return evaluationService.createEvaluation(evaluation);

    } catch (Exception e) {
        log.warn("从 EvaluationResult 创建评分记录失败，使用默认值", e);

        // 创建失败，使用默认值
        MockInterviewEvaluation evaluation = MockInterviewEvaluation.builder()
                .sessionId(sessionId)
                .roundNumber(roundNumber)
                .skillId(0L)
                .skillName("通用")
                .questionText(question)
                .userAnswer(userAnswer)
                .technicalScore(java.math.BigDecimal.valueOf(2.5))
                .logicScore(java.math.BigDecimal.valueOf(2.5))
                .depthScore(java.math.BigDecimal.valueOf(2.5))
                .totalScore(java.math.BigDecimal.valueOf(7.5))
                .feedback("评估结果处理失败")
                .build();

        return evaluationService.createEvaluation(evaluation);
    }
}
```

---

## 修复效果对比

### 代码简洁性

**修改前**（需要手动解析 JSON）：
```java
// 1. 获取 JSON 字符串
String json = agents.evaluator().evaluate(context);

// 2. 清理 Markdown 标记
String cleanedJson = json.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();

// 3. 解析 JSON
ObjectMapper mapper = new ObjectMapper();
Map<String, Object> result = mapper.readValue(cleanedJson, Map.class);

// 4. 提取字段
Map<String, Object> scores = (Map<String, Object>) result.get("scores");
Double technical = ((Number) scores.get("technical")).doubleValue();

// 5. 使用数据
evaluation.setTechnicalScore(BigDecimal.valueOf(technical));
```

**修改后**（直接使用对象）：
```java
// 1. 获取对象
EvaluationResult result = agents.evaluator().evaluate(context);

// 2. 直接使用数据
evaluation.setTechnicalScore(BigDecimal.valueOf(result.getScores().getTechnical()));
```

---

### 类型安全

**修改前**（运行时错误）：
```java
// ❌ 编译期无法检查字段名
Object scoreValue = scoreResult.get("credibility_score");  // 拼写错误？运行时才知道
if (scoreValue instanceof Number) {
    credibilityScore = ((Number) scoreValue).doubleValue();
}
```

**修改后**（编译期检查）：
```java
// ✅ 编译期检查，IDE 自动补全
credibilityScore = credibilityScoreResult.getCredibilityScore();
```

---

### 错误处理

**修改前**（多种可能的异常）：
```java
try {
    // 可能抛出：JsonParseException, JsonMappingException, IOException
    // 可能抛出：ClassCastException, NullPointerException
    Map<String, Object> result = mapper.readValue(cleanedJson, Map.class);
    // ...
} catch (IOException e) {
    // JSON 解析失败
} catch (ClassCastException e) {
    // 类型转换失败
} catch (NullPointerException e) {
    // 字段不存在
}
```

**修改后**（统一异常处理）：
```java
try {
    EvaluationResult result = agents.evaluator().evaluate(context);
    // LangChain4j 已处理反序列化异常
} catch (Exception e) {
    // 统一异常处理
    log.warn("评估失败", e);
}
```

---

## 关键改进点

### 1. 移除 JSON 解析逻辑

**删除的代码**：
- ❌ 去除 Markdown 标记的代码
- ❌ ObjectMapper 配置代码
- ❌ JSON 解析 try-catch 块
- ❌ 类型转换和空值检查代码

**新增的代码**：
- ✅ 直接使用对象字段
- ✅ 简单的空值检查（`scores != null ? scores.getTechnical() : 2.5`）

---

### 2. 使用 ObjectMapper 序列化对象

**场景**：需要将对象存储为 JSON 字符串（如 `resume_gap_analysis` 字段）

```java
// 创建 ObjectMapper（复用实例）
com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

// 序列化对象为 JSON
List<SkillCredibility> credibilityList = agents.evaluator().generateCredibilityAnalysis(context);
String credibilityAnalysisJson = mapper.writeValueAsString(credibilityList);
session.setResumeGapAnalysis(credibilityAnalysisJson);
```

---

### 3. 利用对象的嵌套结构

**EvaluationResult 包含嵌套对象**：
```java
EvaluationResult
├── ScoreDetail scores
│   ├── technical
│   ├── logic
│   └── depth
├── CredibilityAssessment credibilityAssessment
│   ├── matchLevel (枚举)
│   ├── gapDescription
│   └── exaggerationScore
├── totalScore
├── feedback
└── suggestion
```

**使用方式**：
```java
ScoreDetail scores = evaluationResult.getScores();
Double technical = scores != null ? scores.getTechnical() : 2.5;

CredibilityAssessment credibility = evaluationResult.getCredibilityAssessment();
if (credibility != null &&
    credibility.getMatchLevel() == CredibilityAssessment.MatchLevel.PARTIALLY_EXAGGERATED) {
    log.warn("简历存在夸大：{}", credibility.getGapDescription());
}
```

---

## 注意事项

### ⚠️ ObjectMapper 实例管理

**建议**：将 ObjectMapper 声明为 Spring Bean，避免重复创建

```java
@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

// 在 Controller 中注入
private final ObjectMapper mapper;

// 使用
String json = mapper.writeValueAsString(object);
```

---

### ⚠️ 空值处理

虽然 LangChain4j 会自动反序列化，但仍需处理可能的空值：

```java
// ✅ 安全的方式
ScoreDetail scores = evaluationResult.getScores();
Double technical = scores != null ? scores.getTechnical() : 2.5;

// ❌ 不安全的方式（可能 NullPointerException）
Double technical = evaluationResult.getScores().getTechnical();
```

---

### ⚠️ 枚举类型处理

LangChain4j 会自动处理枚举的序列化/反序列化：

```java
// Agent 返回的 JSON
{
  "matchLevel": "PARTIALLY_EXAGGERATED"
}

// 自动反序列化为枚举
CredibilityAssessment.MatchLevel matchLevel = credibility.getMatchLevel();

// 直接使用枚举比较
if (matchLevel == CredibilityAssessment.MatchLevel.PARTIALLY_EXAGGERATED) {
    // ...
}
```

---

## 总结

### 修复成果

| 方面 | 修复前 | 修复后 |
|-----|-------|-------|
| **代码行数** | ~100 行（包含 JSON 解析） | ~30 行（直接使用对象） |
| **类型安全** | ❌ 运行时检查 | ✅ 编译期检查 |
| **错误处理** | ❌ 多种异常类型 | ✅ 统一异常处理 |
| **维护性** | ❌ JSON 结构变化需修改解析代码 | ✅ 只需修改 DTO 类 |
| **可读性** | ❌ 大量解析逻辑 | ✅ 清晰的对象访问 |

### 核心价值

1. **代码更简洁**
   - 移除了所有 JSON 解析代码
   - 直接使用对象字段，代码更易读

2. **类型更安全**
   - 编译期检查，避免运行时错误
   - IDE 自动补全，提高开发效率

3. **维护更容易**
   - DTO 类集中管理数据结构
   - 结构变化只需修改 DTO 类

4. **充分利用框架能力**
   - LangChain4j 自动处理序列化/反序列化
   - 遵循框架最佳实践

### 经验教训

1. **接口重构要同步更新调用方**
   - Agent 接口返回类型变更后，Controller 层必须同步更新
   - 不能只修改接口定义，要修改所有调用点

2. **删除不再需要的代码**
   - JSON 解析代码不再需要，应该删除
   - 保持代码简洁，避免遗留无用代码

3. **使用框架能力而非手动处理**
   - LangChain4j 已提供自动序列化，不应手动解析 JSON
   - 充分利用框架特性，减少代码量

---

**修复完成日期**: 2026-03-18
**文档版本**: 1.0.0
**作者**: Job Tracker Team
