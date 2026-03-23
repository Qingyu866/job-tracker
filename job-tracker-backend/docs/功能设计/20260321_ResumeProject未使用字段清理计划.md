# ResumeProject 未使用字段清理计划

**创建时间**: 2026-03-21
**状态**: 待审批

---

## 一、背景

在 `ResumeProject` 实体中存在三个字段在实际运行中未被使用，需要清理以简化数据模型：

1. **achievements** (项目成就)
2. **projectScale** (项目规模：团队人数)
3. **performanceMetrics** (性能指标)

---

## 二、影响范围分析

### 2.1 后端文件清单

| 文件路径 | 修改类型 | 说明 |
|---------|---------|------|
| `src/main/java/com/jobtracker/entity/ResumeProject.java` | 删除字段 | 删除三个字段的定义 |
| `src/main/java/com/jobtracker/dto/ProjectRequest.java` | 删除字段 | 删除三个字段的定义 |
| `src/main/java/com/jobtracker/service/UserResumeService.java` | 修改方法 | `buildProject()` 方法中删除字段赋值 |
| `src/main/resources/mapper/ResumeProjectMapper.xml` | 修改SQL | `insertBatch` 中删除三个字段 |
| `src/main/resources/db/migration/V12__remove_unused_project_fields.sql` | 新增 | 数据库迁移脚本（删除列） |

### 2.2 前端文件清单（后端完成后处理）

| 文件路径 | 修改类型 | 说明 |
|---------|---------|------|
| `frontend/src/types/resume.ts` | 删除字段 | 删除类型定义 |
| `frontend/src/pages/ResumeEditPage.tsx` | 删除字段 | 删除表单字段初始化 |
| `frontend/src/pages/ResumeCreatePage.tsx` | 删除字段 | 删除表单字段初始化 |
| `frontend/src/components/common/ResumeCreateModal.tsx` | 删除字段 | 删除表单字段初始化 |

---

## 三、后端修改详情

### 3.1 ResumeProject.java

**位置**: `src/main/java/com/jobtracker/entity/ResumeProject.java`

**删除内容**:
```java
// 删除第 76-79 行
/**
 * 项目成就
 */
private String achievements;

// 删除第 88-93 行
/**
 * 项目规模：团队人数
 */
private String projectScale;

// 删除第 91-98 行
/**
 * 性能指标：{"qps": "10000", "response_time": "50ms"}
 */
private String performanceMetrics;
```

---

### 3.2 ProjectRequest.java

**位置**: `src/main/java/com/jobtracker/dto/ProjectRequest.java`

**删除内容**:
```java
// 删除第 57-60 行
/**
 * 项目成就
 */
private String achievements;

// 删除第 67-70 行
/**
 * 项目规模
 */
private String projectScale;

// 删除第 72-75 行
/**
 * 性能指标
 */
private String performanceMetrics;
```

---

### 3.3 UserResumeService.java

**位置**: `src/main/java/com/jobtracker/service/UserResumeService.java`

**修改内容**: 在 `buildProject()` 方法中删除字段赋值

**原代码** (第 225-241 行):
```java
private ResumeProject buildProject(Long resumeId, ProjectRequest request) {
    return ResumeProject.builder()
            .resumeId(resumeId)
            .projectName(request.getProjectName())
            .role(request.getRole())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .isOngoing(request.getIsOngoing())
            .description(request.getDescription())
            .responsibilities(request.getResponsibilities())
            .achievements(request.getAchievements())           // ← 删除
            .techStack(JSONUtil.toJsonStr(request.getTechStack()))
            .projectScale(request.getProjectScale())           // ← 删除
            .performanceMetrics(request.getPerformanceMetrics()) // ← 删除
            .displayOrder(request.getDisplayOrder())
            .build();
}
```

**修改后**:
```java
private ResumeProject buildProject(Long resumeId, ProjectRequest request) {
    return ResumeProject.builder()
            .resumeId(resumeId)
            .projectName(request.getProjectName())
            .role(request.getRole())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .isOngoing(request.getIsOngoing())
            .description(request.getDescription())
            .responsibilities(request.getResponsibilities())
            .techStack(JSONUtil.toJsonStr(request.getTechStack()))
            .displayOrder(request.getDisplayOrder())
            .build();
}
```

---

### 3.4 ResumeProjectMapper.xml

**位置**: `src/main/resources/mapper/ResumeProjectMapper.xml`

**原代码**:
```xml
<insert id="insertBatch">
    INSERT INTO resume_projects
    (resume_id, project_name, role, start_date, end_date, is_ongoing, description,
     responsibilities, achievements, tech_stack, project_scale, performance_metrics,
     display_order, created_at)
    VALUES
    <foreach collection="list" item="item" separator=",">
        (#{item.resumeId}, #{item.projectName}, #{item.role}, #{item.startDate}, #{item.endDate},
         #{item.isOngoing}, #{item.description}, #{item.responsibilities}, #{item.achievements},
         #{item.techStack}, #{item.projectScale}, #{item.performanceMetrics},
         #{item.displayOrder}, NOW())
    </foreach>
</insert>
```

**修改后**:
```xml
<insert id="insertBatch">
    INSERT INTO resume_projects
    (resume_id, project_name, role, start_date, end_date, is_ongoing, description,
     responsibilities, tech_stack, display_order, created_at)
    VALUES
    <foreach collection="list" item="item" separator=",">
        (#{item.resumeId}, #{item.projectName}, #{item.role}, #{item.startDate}, #{item.endDate},
         #{item.isOngoing}, #{item.description}, #{item.responsibilities},
         #{item.techStack}, #{item.displayOrder}, NOW())
    </foreach>
</insert>
```

---

### 3.5 数据库迁移脚本

**新建文件**: `src/main/resources/db/migration/V12__remove_unused_project_fields.sql`

```sql
-- ====================================
-- ResumeProject 表清理未使用字段
-- ====================================
-- 删除三个未使用的字段：
-- 1. achievements (项目成就)
-- 2. project_scale (项目规模)
-- 3. performance_metrics (性能指标)
-- ====================================

ALTER TABLE resume_projects
    DROP COLUMN IF EXISTS achievements;

ALTER TABLE resume_projects
    DROP COLUMN IF EXISTS project_scale;

ALTER TABLE resume_projects
    DROP COLUMN IF EXISTS performance_metrics;
```

---

## 四、执行顺序

### 4.1 后端修改顺序

1. **停止应用服务**
   ```bash
   # 停止 Spring Boot 应用
   ```

2. **执行数据库迁移**
   ```bash
   mysql -u root -p job_tracker < src/main/resources/db/migration/V12__remove_unused_project_fields.sql
   ```

3. **修改代码文件**（按以下顺序）
   - `ResumeProject.java` - 删除实体字段
   - `ProjectRequest.java` - 删除DTO字段
   - `UserResumeService.java` - 修改buildProject方法
   - `ResumeProjectMapper.xml` - 修改SQL映射

4. **重新编译**
   ```bash
   mvn clean compile
   ```

5. **启动应用服务**
   ```bash
   mvn spring-boot:run
   ```

6. **验证功能**
   - 测试创建简历功能
   - 测试更新简历功能
   - 检查日志是否有错误

### 4.2 前端修改顺序（后端完成后）

1. 修改类型定义 `frontend/src/types/resume.ts`
2. 修改页面组件：
   - `ResumeEditPage.tsx`
   - `ResumeCreatePage.tsx`
   - `ResumeCreateModal.tsx`
3. 前端编译测试 `npm run build`

---

## 五、风险评估

### 5.1 数据风险

- **风险等级**: 🟡 中等
- **说明**: 删除字段会导致现有数据丢失
- **缓解措施**:
  - 建议在执行迁移前备份数据库
  - 如果未来需要恢复数据，可以从备份中恢复

### 5.2 兼容性风险

- **风险等级**: 🟢 低
- **说明**: 前端和后端同步修改，不会出现版本不兼容

### 5.3 功能影响

- **影响范围**: 简历创建和编辑功能
- **影响程度**: 无功能损失（字段未使用）

---

## 六、验证清单

### 后端验证

- [ ] 数据库字段已删除
- [ ] 代码编译无错误
- [ ] 应用启动无异常
- [ ] 创建简历功能正常
- [ ] 更新简历功能正常
- [ ] 日志无相关错误

### 前端验证

- [ ] TypeScript 编译无错误
- [ ] 创建简历页面正常
- [ ] 编辑简历页面正常
- [ ] 表单提交正常

---

## 七、回滚方案

如果需要回滚：

### 7.1 数据库回滚

```sql
-- 恢复字段
ALTER TABLE resume_projects ADD COLUMN achievements TEXT COMMENT '项目成就';
ALTER TABLE resume_projects ADD COLUMN project_scale VARCHAR(50) COMMENT '项目规模：团队人数';
ALTER TABLE resume_projects ADD COLUMN performance_metrics JSON COMMENT '性能指标';
```

### 7.2 代码回滚

使用 Git 恢复修改的文件：
```bash
git checkout HEAD~1 -- <file_path>
```

---

## 八、待审批事项

- [ ] 用户确认三个字段确实未使用
- [ ] 用户批准执行后端修改
- [ ] 用户确认数据库可以执行迁移
- [ ] 后端修改完成后，用户批准前端修改

---

## 附录：相关文件路径

### 后端
- `/Users/qingyu/job-tracker/job-tracker-backend/src/main/java/com/jobtracker/entity/ResumeProject.java`
- `/Users/qingyu/job-tracker/job-tracker-backend/src/main/java/com/jobtracker/dto/ProjectRequest.java`
- `/Users/qingyu/job-tracker/job-tracker-backend/src/main/java/com/jobtracker/service/UserResumeService.java`
- `/Users/qingyu/job-tracker/job-tracker-backend/src/main/resources/mapper/ResumeProjectMapper.xml`
- `/Users/qingyu/job-tracker/job-tracker-backend/src/main/resources/db/migration/`

### 前端
- `/Users/qingyu/job-tracker/frontend/src/types/resume.ts`
- `/Users/qingyu/job-tracker/frontend/src/pages/ResumeEditPage.tsx`
- `/Users/qingyu/job-tracker/frontend/src/pages/ResumeCreatePage.tsx`
- `/Users/qingyu/job-tracker/frontend/src/components/common/ResumeCreateModal.tsx`
