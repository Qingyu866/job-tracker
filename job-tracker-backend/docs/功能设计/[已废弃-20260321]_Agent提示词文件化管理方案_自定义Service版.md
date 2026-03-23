# Agent 提示词文件化管理方案（自定义 PromptService 版）

> **⚠️ 已废弃**
>
> **废弃原因**: LangChain4j 原生支持 `@SystemMessage(fromResource = "...")`，无需自定义实现
>
> **替代方案**: 请参阅 `20260321_Agent提示词文件化实施计划.md`
>
> **废弃日期**: 2026-03-21

---

**原始文档**：

**日期**: 2026-03-21
**目标**: 将 Agent 系统提示词和用户提示词从代码中分离，保存到独立文件

---

## 一、目录结构设计

```
src/main/resources/
└── prompts/
    ├── system/                      # 系统提示词
    │   ├── interview/
    │   │   ├── main-interviewer.txt           # 主面试官系统提示词
    │   │   ├── vice-interviewer.txt           # 副面试官系统提示词
    │   │   ├── expert-evaluator.txt           # 评审专家系统提示词
    │   │   └── skill-generator.txt            # 技能生成 Agent 系统提示词
    │   └── job-agent/
    │       └── job-agent.txt                  # Job Agent 系统提示词
    │
    └── user/                        # 用户提示词模板
        ├── interview/
        │   ├── generate-plan.txt              # 生成问题计划
        │   ├── ask-question.txt               # 提问
        │   ├── evaluate-round.txt             # 评估单轮回答
        │   ├── evaluate-manual.txt            # 手动评估
        │   ├── generate-report.txt            # 生成报告
        │   ├── decide-next-step.txt           # 决定下一步（备用）
        │   └── generate-summary.txt           # 生成总结
        └── job-agent/
            └── chat.txt                        # 聊天（如果需要模板）
```

---

## 二、文件命名规范

### 2.1 系统提示词
- 格式：`{agent-name}.txt`
- 示例：`main-interviewer.txt`, `expert-evaluator.txt`

### 2.2 用户提示词
- 格式：`{action-name}.txt`
- 示例：`generate-plan.txt`, `ask-question.txt`

---

## 三、提示词文件格式

### 3.1 系统提示词格式

使用 **占位符** 来支持动态变量替换：

```text
# main-interviewer.txt

# 角色设定
你是 JobTracker 模拟面试系统的主面试官，正在对候选人进行专业面试。

# ⚠️ 重要：明确区分
- **面试公司**：这是候选人要应聘的公司，不是候选人工作过的公司
- **候选人工作经历**：这是候选人之前在别的公司的工作经历
- **千万不要混淆**：不要把面试公司当成候选人的工作经历！

# ⏰ 当前时间
- 当前日期：{{current_date}}
- 当前时间：{{current_time}}

# 面试上下文
{{context}}

# ⭐ 简历信息（固定上下文）
{{resume_snapshot}}

# ⭐ JD 要求（固定上下文）
{{jd_snapshot}}

# 你的职责
...
```

### 3.2 用户提示词格式

同样使用占位符：

```text
# ask-question.txt

# 任务
请根据以下计划生成一个面试问题。

# 问题计划
- 技能: {{skill_name}}
- 选题来源: {{topic_source}}
- 问题类型: {{question_type}}
- 难度: {{difficulty}}
- 上下文信息: {{context_info}}
- 选择原因: {{reason}}

# 面试信息
- 岗位: {{job_title}}
- 级别: {{seniority_level}}
- 当前轮次: {{current_round}}

# 要求
1. 问题必须符合计划中的技能和难度要求
2. 问题类型要匹配（开放性问题/技术问题/情景问题）
3. 根据上下文信息调整问题细节
4. 简历信息已在系统提示词中提供，请结合简历中的项目经验生成问题
```

---

## 四、占位符设计规范

### 4.1 占位符语法

- 使用 `{{variable_name}}` 格式
- 变量名使用小写字母和下划线
- 示例：`{{current_date}}`, `{{skill_name}}`

### 4.2 常用占位符分类

#### 时间相关
- `{{current_date}}` - 当前日期
- `{{current_time}}` - 当前时间
- `{{day_of_week}}` - 星期几

#### 会话相关
- `{{session_id}}` - 会话ID
- `{{current_round}}` - 当前轮次
- `{{job_title}}` - 岗位名称
- `{{seniority_level}}` - 岗位级别

#### 内容相关
- `{{context}}` - 基础上下文
- `{{resume_snapshot}}` - 简历快照
- `{{jd_snapshot}}` - JD 快照
- `{{question}}` - 问题
- `{{user_answer}}` - 用户回答

#### 计划相关
- `{{skill_name}}` - 技能名称
- `{{topic_source}}` - 选题来源
- `{{question_type}}` - 问题类型
- `{{difficulty}}` - 难度等级
- `{{context_info}}` - 上下文信息
- `{{reason}}` - 选择原因

---

## 五、实现方案

### 5.1 提示词加载服务

创建 `PromptTemplateService` 来加载和渲染提示词：

```java
@Service
public class PromptTemplateService {

    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    /**
     * 加载提示词模板
     *
     * @param path 模板路径（相对于 resources/prompts/）
     * @return 模板内容
     */
    public String loadTemplate(String path) {
        // 先从缓存获取
        if (templateCache.containsKey(path)) {
            return templateCache.get(path);
        }

        // 从文件加载
        Resource resource = new ClassPathResource("prompts/" + path);
        try {
            String content = StreamUtils.copyToString(
                resource.getInputStream(),
                StandardCharsets.UTF_8
            );
            templateCache.put(path, content);
            return content;
        } catch (IOException e) {
            log.error("加载提示词模板失败: {}", path, e);
            throw new RuntimeException("加载提示词模板失败: " + path, e);
        }
    }

    /**
     * 渲染提示词（替换占位符）
     *
     * @param template 模板内容
     * @param variables 变量映射
     * @return 渲染后的内容
     */
    public String render(String template, Map<String, Object> variables) {
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }

    /**
     * 加载并渲染提示词
     *
     * @param path 模板路径
     * @param variables 变量映射
     * @return 渲染后的内容
     */
    public String loadAndRender(String path, Map<String, Object> variables) {
        String template = loadTemplate(path);
        return render(template, variables);
    }
}
```

### 5.2 修改 InterviewAgentFactory

```java
@Component
@RequiredArgsConstructor
public class InterviewAgentFactory {

    private final OpenAiChatModel chatModel;
    private final PromptTemplateService promptService;
    // ... 其他依赖

    /**
     * 构建主面试官 System Message（使用文件模板）
     */
    private String buildMainInterviewerSystemMessage(
            String context,
            String resumeSnapshot,
            String jdSnapshot
    ) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("current_date", LocalDate.now().toString());
        variables.put("current_time", LocalTime.now().toString());
        variables.put("context", context);
        variables.put("resume_snapshot", resumeSnapshot != null ? resumeSnapshot : "无");
        variables.put("jd_snapshot", jdSnapshot != null ? jdSnapshot : "无");

        return promptService.loadAndRender(
            "system/interview/main-interviewer.txt",
            variables
        );
    }

    /**
     * 构建副面试官 System Message（使用文件模板）
     */
    private String buildViceInterviewerSystemMessage(
            String context,
            String resumeSnapshot,
            String jdSnapshot
    ) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("context", context);
        variables.put("resume_snapshot", resumeSnapshot != null ? resumeSnapshot : "无");
        variables.put("jd_snapshot", jdSnapshot != null ? jdSnapshot : "无");

        return promptService.loadAndRender(
            "system/interview/vice-interviewer.txt",
            variables
        );
    }

    /**
     * 构建评审专家 System Message（使用文件模板）
     */
    private String buildExpertEvaluatorSystemMessage(
            String context,
            String resumeSnapshot,
            String jdSnapshot
    ) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("context", context);
        variables.put("resume_snapshot", resumeSnapshot != null ? resumeSnapshot : "无");
        variables.put("jd_snapshot", jdSnapshot != null ? jdSnapshot : "无");

        return promptService.loadAndRender(
            "system/interview/expert-evaluator.txt",
            variables
        );
    }
}
```

### 5.3 修改 Controller

```java
@RestController
@RequiredArgsConstructor
public class MockInterviewController {

    private final PromptTemplateService promptService;
    // ... 其他依赖

    /**
     * 从计划构建提问上下文（使用文件模板）
     */
    private String buildQuestionContextFromPlan(
            MockInterviewSession session,
            MockInterviewEvaluation plan
    ) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("skill_name", plan.getSkillName() != null ? plan.getSkillName() : "通用");
        variables.put("topic_source", plan.getTopicSource() != null ? plan.getTopicSource() : "UNKNOWN");
        variables.put("question_type", plan.getQuestionType() != null ? plan.getQuestionType() : "OPEN_ENDED");
        variables.put("difficulty", plan.getPlannedDifficulty() != null ? plan.getPlannedDifficulty() : 3);
        variables.put("context_info", plan.getContextInfo() != null ? plan.getContextInfo() : "无");
        variables.put("reason", plan.getReason() != null ? plan.getReason() : "无");
        variables.put("job_title", session.getJobTitle());
        variables.put("seniority_level", session.getSeniorityLevel());
        variables.put("current_round", session.getCurrentRound());

        return promptService.loadAndRender(
            "user/interview/ask-question.txt",
            variables
        );
    }

    /**
     * 构建单轮评估上下文（使用文件模板）
     */
    private String buildEvaluationContextForRound(
            MockInterviewSession session,
            String question,
            String userAnswer,
            Integer roundNumber
    ) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("round_number", roundNumber);
        variables.put("question", question);
        variables.put("user_answer", userAnswer);

        return promptService.loadAndRender(
            "user/interview/evaluate-round.txt",
            variables
        );
    }
}
```

---

## 六、优势

### 6.1 维护性
- ✅ 提示词独立管理，不需要重新编译代码
- ✅ 便于版本控制和变更追踪
- ✅ 支持热更新（重启应用即可）

### 6.2 可测试性
- ✅ 便于 A/B 测试不同版本的提示词
- ✅ 可以快速迭代优化提示词
- ✅ 支持多语言提示词（只需创建不同语言的文件）

### 6.3 可扩展性
- ✅ 新增 Agent 只需添加新的提示词文件
- ✅ 支持提示词的模块化管理
- ✅ 便于团队协作（非开发人员也可以修改提示词）

---

## 七、实施计划

### 阶段 1：创建提示词文件（优先级：高）
1. 创建目录结构
2. 从代码中提取现有的提示词
3. 创建所有 .txt 文件

### 阶段 2：实现加载服务（优先级：高）
1. 创建 `PromptTemplateService`
2. 实现加载和渲染方法
3. 添加缓存机制

### 阶段 3：修改现有代码（优先级：高）
1. 修改 `InterviewAgentFactory`
2. 修改 `MockInterviewController`
3. 修改 `MockInterviewService`

### 阶段 4：测试验证（优先级：中）
1. 单元测试
2. 集成测试
3. 性能测试

---

## 八、注意事项

### 8.1 文件编码
- 所有文件使用 **UTF-8** 编码
- 注意换行符（使用 \n 而非 \r\n）

### 8.2 变量替换
- 占位符必须完整匹配
- 空值处理（null 值替换为 "无" 或默认值）

### 8.3 性能优化
- 使用缓存避免重复读取文件
- 考虑使用 `ConcurrentHashMap` 实现线程安全

### 8.4 错误处理
- 文件不存在时的降级方案
- 占位符缺失时的处理
- 日志记录

---

## 九、示例文件

### 9.1 system/interview/main-interviewer.txt

```text
# 角色设定
你是 JobTracker 模拟面试系统的主面试官，正在对候选人进行专业面试。

# ⚠️ 重要：明确区分
- **面试公司**：这是候选人要应聘的公司，不是候选人工作过的公司
- **候选人工作经历**：这是候选人之前在别的公司的工作经历
- **千万不要混淆**：不要把面试公司当成候选人的工作经历！

# ⏰ 当前时间
- 当前日期：{{current_date}}
- 当前时间：{{current_time}}

# 面试上下文
{{context}}

# ⭐ 简历信息（固定上下文）
{{resume_snapshot}}

# ⭐ JD 要求（固定上下文）
{{jd_snapshot}}

# 你的职责

## 1. 提问策略（优先级从高到低）
1. **项目深挖**（最高优先级）
   - "看到你简历上写了项目，能详细讲讲吗？"
   - "在这个项目中，你负责的模块遇到了什么技术挑战？"
   - "项目的性能指标是多少？是如何优化的？"

2. **技能验证**
   - "你简历上写着精通技能，那深入问题？"
   - 针对"精通"、"熟悉"等词汇，提出有深度的问题
   - 验证简历中的技能声称是否真实

3. **JD 要求覆盖**
   - 基于 JD 中的技能要求，补充提问简历未提及的部分

4. **经验匹配度评估**
   - 基于工作年限经验，提出相应难度的问题
   - 避免"过度提问"或"提问过浅"

## 2. 提问示例
❌ 错误： "请讲讲 HashMap 的原理。" （太泛泛，没有结合简历）
✅ 正确： "看到你简历上写了电商秒杀项目，能详细讲讲在高并发场景下，Redis 和 Kafka 是如何配合使用的吗？"

## 3. 特殊情况处理
- 如果候选人**没有工作经历**（work_years: 0, work_experiences: []）：
    - 不要问"你在某某公司的工作经历"
    - 应该问"你为什么要应聘这个岗位"
    - 或者从基础知识开始问起

# 严格约束
1. **严禁评分**：不要提及分数、等级
2. **严禁重复**：不要问已考察过的知识点
3. **单一问题**：每次只问一个问题
4. **长度控制**：回复控制在 200 字以内
5. **必须结合简历**：尽量基于简历内容提问

# 对话风格
- 专业但不失温和
- 带有"审视"的态度（面试官天然属性）
- 遇到回答不清时，适度追问
- 给候选人充分的表达空间
```

### 9.2 user/interview/ask-question.txt

```text
# 任务
请根据以下计划生成一个面试问题。

# 问题计划
- 技能: {{skill_name}}
- 选题来源: {{topic_source}}
- 问题类型: {{question_type}}
- 难度: {{difficulty}}
- 上下文信息: {{context_info}}
- 选择原因: {{reason}}

# 面试信息
- 岗位: {{job_title}}
- 级别: {{seniority_level}}
- 当前轮次: {{current_round}}

# 要求
1. 问题必须符合计划中的技能和难度要求
2. 问题类型要匹配（开放性问题/技术问题/情景问题）
3. 根据上下文信息调整问题细节
4. 简历信息已在系统提示词中提供，请结合简历中的项目经验生成问题
```

### 9.3 user/interview/evaluate-round.txt

```text
# 评估任务
请评估候选人的第 {{round_number}} 轮回答

# 问题
{{question}}

# 用户回答
{{user_answer}}

# 注意事项
- 简历信息和 JD 要求已在系统提示词中提供
- 请基于系统提示词中的简历信息，评估回答的真实性
- 关注回答与简历声称的匹配度
```

---

## 十、总结

通过将提示词文件化：

1. **便于维护**：提示词独立管理，不需要重新编译
2. **快速迭代**：可以直接修改文件测试效果
3. **版本控制**：提示词变更可追踪
4. **团队协作**：非开发人员也可以参与提示词优化
5. **A/B 测试**：便于创建不同版本的提示词进行对比

**关键设计**：
- 使用 `{{variable}}` 占位符支持动态替换
- 分类存储（system/user）
- 缓存机制提升性能
- 统一的加载服务
