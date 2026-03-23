# LangChain4j 集合返回值异常问题与解决

## 问题概述

在使用 LangChain4j 的 AI 服务接口时，如果方法返回类型是集合（如 `List<T>`），可能会抛出 `IllegalStateException` 异常。

## 错误信息

```
java.lang.IllegalStateException: null
    at dev.langchain4j.service.output.PojoCollectionOutputParser.formatInstructions(PojoCollectionOutputParser.java:57)
    at dev.langchain4j.service.output.ServiceOutputParser.outputFormatInstructions(ServiceOutputParser.java:112)
    at dev.langchain4j.service.DefaultAiServices$1.appendOutputFormatInstructions(DefaultAiServices.java:452)
    at dev.langchain4j.service.DefaultAiServices$1.invoke(DefaultAiServices.java:232)
    at dev.langchain4j.service.DefaultAiServices$1.invoke(DefaultAiServices.java:154)
    at jdk.proxy2/jdk.proxy2.$Proxy165.generateQuestionPlan(Unknown Source)
```

## 问题原因

### 根本原因

`PojoCollectionOutputParser` 是 LangChain4j 用来解析集合类型返回值（如 `List<T>`）的组件。在初始化时，它需要通过反射获取集合的元素类型 `T`。

但由于以下原因可能导致无法获取泛型类型参数：

1. **Java 类型擦除**：Java 的泛型在运行时会被擦除，只保留原始类型
2. **动态代理限制**：JDK 动态代理在处理泛型时可能有额外的限制
3. **编译配置问题**：某些编译配置可能导致泛型签名信息丢失

### 触发场景

当 AI 接口方法的返回类型直接使用集合类型时：

```java
// ❌ 错误写法
public interface ViceInterviewerAgent {
    List<QuestionPlanDTO> generateQuestionPlan(String context);
}
```

## 解决方案

### 标准做法：使用包装类

**这是 LangChain4j 社区中解决此类问题的标准做法。**

将集合类型包装到一个响应对象中：

```java
// ✅ 正确写法
public class QuestionPlanListResponse {
    private List<QuestionPlanDTO> plans;
    // getter/setter
}

public interface ViceInterviewerAgent {
    QuestionPlanListResponse generateQuestionPlan(String context);
}
```

### 为什么包装类有效？

1. **保留泛型信息**：包装类中的 `List<QuestionPlanDTO>` 字段的泛型信息可以通过反射获取
2. **避免类型擦除**：LangChain4j 可以通过字段签名获取准确的类型参数
3. **更好的扩展性**：未来可以在响应对象中添加元数据（如总数、分页信息等）

## 实施步骤

### 1. 创建包装类

```java
package com.jobtracker.agent.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 考察计划列表响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionPlanListResponse {
    /**
     * 考察计划列表
     */
    private List<QuestionPlanDTO> plans;
}
```

### 2. 修改 AI 接口

```java
// 修改前
List<QuestionPlanDTO> generateQuestionPlan(String context);

// 修改后
QuestionPlanListResponse generateQuestionPlan(String context);
```

### 3. 修改调用代码

```java
// 修改前
List<QuestionPlanDTO> plans = agents.viceInterviewer().generateQuestionPlan(context);

// 修改后
QuestionPlanListResponse response = agents.viceInterviewer().generateQuestionPlan(context);
List<QuestionPlanDTO> plans = response.getPlans();
```

## 相关文档

- **修复计划**：[20260321_LangChain4j集合返回值修复计划](../问题修复/20260321_LangChain4j集合返回值修复计划.md)
- **LangChain4j 官方文档**：https://docs.langchain4j.dev/

## 影响范围

本项目中有以下接口需要修复：

| 接口 | 方法 | 当前返回类型 | 修复后返回类型 |
|------|------|------------|--------------|
| `ViceInterviewerAgent` | `generateQuestionPlan` | `List<QuestionPlanDTO>` | `QuestionPlanListResponse` |
| `ViceInterviewerAgent` | `generateSuggestions` | `List<ImprovementSuggestion>` | `ImprovementSuggestionListResponse` |
| `ExpertEvaluatorAgent` | `generateCredibilityAnalysis` | `List<SkillCredibility>` | `SkillCredibilityListResponse` |
| `SkillGeneratorAgent` | `generateSkillTags` | `List<SkillTag>` | `SkillTagListResponse` |

详见：[修复计划文档](../问题修复/20260321_LangChain4j集合返回值修复计划.md)

## 最佳实践

### ✅ 推荐做法

1. **所有 AI 接口返回值都使用包装类**，即使返回单个对象
2. **统一的响应格式**：便于扩展和维护
3. **清晰的命名**：如 `*Response`、`*Result`

### ❌ 避免做法

1. **直接返回集合类型**：`List<T>`、`Set<T>`、`Map<K,V>`
2. **直接返回数组**：`T[]`
3. **返回原始类型**：`String`、`int` 等（除非确实是纯文本）

## 版本信息

- **LangChain4j 版本**：1.12.1-beta21
- **Spring Boot 版本**：3.4.3
- **Java 版本**：17

## 参考资料

1. LangChain4j GitHub Issues: PojoCollectionOutputParser issues
2. LangChain4j 官方文档: AI Services with structured outputs
3. Java 泛型类型擦除机制

---

**创建时间**：2026-03-21
**文档版本**：1.0.0
**作者**：Job Tracker Team
