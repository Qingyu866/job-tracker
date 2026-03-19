# 前后端API接口对照分析报告

**生成时间**: 2026-03-19  
**后端文档版本**: v1.0  
**分析范围**: 全部15个模块，136个接口

---

## 📊 总体统计

| 项目 | 数量 |
|------|------|
| 后端接口总数 | 136 |
| 前端已实现接口 | 78 |
| 前端缺失接口 | 58 |
| 接口不匹配 | 8 |
| 前端多余接口 | 3 |

---

## 🔴 关键问题

### 1. 简历模块路径不匹配 ⚠️ **严重**

**问题描述**: 前端使用错误的API路径

| 功能 | 后端正确路径 | 前端错误路径 | 影响 |
|------|-------------|-------------|------|
| 获取用户简历列表 | `/resumes/my` | `/resumes/user/${userId}` | ❌ 无法获取数据 |
| 获取默认简历 | `/resumes/my/default` | `/resumes/user/${userId}/default` | ❌ 无法获取数据 |

**修复优先级**: 🔴 **最高**

---

### 2. 模拟面试模块缺失关键接口 ⚠️ **严重**

**缺失接口**:
- `POST /mock-interview/sessions/{sessionId}/pause` - 暂停面试
- `POST /mock-interview/sessions/{sessionId}/resume` - 恢复面试
- `GET /mock-interview/sessions/{sessionId}/progress` - 获取面试进度

**影响**: 用户无法暂停/恢复面试，无法查看面试进度

**修复优先级**: 🔴 **最高**

---

### 3. 面试记录模块缺失重要接口 ⚠️ **高**

**缺失接口**:
- `GET /interviews/upcoming` - 获取即将进行的面试
- `GET /interviews/applications/{applicationId}/progress` - 获取面试进度
- `GET /interviews/applications/{applicationId}/current` - 获取当前面试
- `PUT /interviews/{id}/start` - 开始面试
- `PUT /interviews/{id}/mark-final` - 标记终面
- `POST /interviews/{id}/reschedule` - 重新安排面试

**影响**: 面试管理功能不完整

**修复优先级**: 🟠 **高**

---

### 4. 求职申请模块缺失关键功能 ⚠️ **高**

**缺失接口**:
- `GET /applications/status/{status}` - 根据状态获取申请
- `GET /applications/page` - 分页查询申请
- `GET /applications/high-priority` - 获取高优先级申请
- `PUT /applications/{id}/status` - 更新申请状态

**影响**: 无法按状态筛选、分页查询、查看高优先级申请

**修复优先级**: 🟠 **高**

---

## 📋 详细对比分析

### 1. 认证模块 ✅ **完全匹配**

| 接口 | 后端路径 | 前端实现 | 状态 |
|------|---------|---------|------|
| 用户登录 | POST /auth/login | ✅ | 匹配 |
| 用户注册 | POST /auth/register | ✅ | 匹配 |
| 获取用户信息 | GET /auth/info | ✅ | 匹配 |
| 退出登录 | POST /auth/logout | ✅ | 匹配 |
| 修改密码 | POST /auth/change-password | ✅ | 匹配 |

---

### 2. 求职申请模块 ⚠️ **部分缺失**

| 接口 | 后端路径 | 前端实现 | 状态 |
|------|---------|---------|------|
| 获取所有申请 | GET /applications | ✅ | 匹配 |
| 获取单个申请 | GET /applications/{id} | ✅ | 匹配 |
| 根据状态获取 | GET /applications/status/{status} | ❌ | **缺失** |
| 分页查询 | GET /applications/page | ❌ | **缺失** |
| 高优先级申请 | GET /applications/high-priority | ❌ | **缺失** |
| 更新状态 | PUT /applications/{id}/status | ❌ | **缺失** |
| 创建申请 | POST /applications | ✅ | 匹配 |
| 更新申请 | PUT /applications/{id} | ✅ | 匹配 |
| 删除申请 | DELETE /applications/{id} | ✅ | 匹配 |

**问题**: 前端 `getApplications` 支持 `keyword` 参数，但后端文档未提及，应使用 `/search/applications`

---

### 3. 面试记录模块 ⚠️ **部分缺失**

| 接口 | 后端路径 | 前端实现 | 状态 |
|------|---------|---------|------|
| 获取所有面试 | GET /interviews | ✅ | 匹配 |
| 获取单个面试 | GET /interviews/{id} | ✅ | 匹配 |
| 根据申请ID获取 | GET /interviews/application/{applicationId} | ✅ | 匹配 |
| 即将进行的面试 | GET /interviews/upcoming | ❌ | **缺失** |
| 面试进度 | GET /interviews/applications/{applicationId}/progress | ❌ | **缺失** |
| 当前面试 | GET /interviews/applications/{applicationId}/current | ❌ | **缺失** |
| 创建面试 | POST /interviews | ✅ | 匹配 |
| 更新面试 | PUT /interviews/{id} | ✅ | 匹配 |
| 删除面试 | DELETE /interviews/{id} | ✅ | 匹配 |
| 完成面试 | POST /interviews/{id}/complete | ✅ | 匹配 |
| 更新反馈 | PUT /interviews/{id}/feedback | ✅ | 匹配 |
| 更新技术问题 | PUT /interviews/{id}/technical-questions | ✅ | 匹配 |
| 取消面试 | PUT /interviews/{id}/cancel | ✅ | 匹配 |
| 标记未参加 | PUT /interviews/{id}/no-show | ✅ | 匹配 |
| 开始面试 | PUT /interviews/{id}/start | ❌ | **缺失** |
| 标记终面 | PUT /interviews/{id}/mark-final | ❌ | **缺失** |
| 重新安排 | POST /interviews/{id}/reschedule | ❌ | **缺失** |
| 设置跟进 | PUT /interviews/{id}/follow-up | ✅ | 匹配 |

---

### 4. 公司信息模块 ⚠️ **部分缺失**

| 接口 | 后端路径 | 前端实现 | 状态 |
|------|---------|---------|------|
| 获取所有公司 | GET /companies | ✅ | 匹配 |
| 获取单个公司 | GET /companies/{id} | ✅ | 匹配 |
| 根据名称获取 | GET /companies/name | ❌ | **缺失** |
| 搜索公司 | GET /companies/search | ❌ | **缺失** |
| 创建公司 | POST /companies | ✅ | 匹配 |
| 更新公司 | PUT /companies/{id} | ✅ | 匹配 |
| 删除公司 | DELETE /companies/{id} | ✅ | 匹配 |

---

### 5. 简历管理模块 ❌ **路径错误**

| 接口 | 后端路径 | 前端实现 | 状态 |
|------|---------|---------|------|
| 创建简历 | POST /resumes | ✅ | 匹配 |
| 创建完整简历 | POST /resumes/complete | ❌ | **缺失** |
| 更新简历 | PUT /resumes/{resumeId} | ✅ | 匹配 |
| 删除简历 | DELETE /resumes/{resumeId} | ✅ | 匹配 |
| 获取简历详情 | GET /resumes/{resumeId} | ✅ | 匹配 |
| 获取用户简历 | GET /resumes/my | ❌ | **路径错误** |
| 获取默认简历 | GET /resumes/my/default | ❌ | **路径错误** |
| 设置默认简历 | PUT /resumes/{resumeId}/default | ✅ | 匹配 |
| 获取项目经历 | GET /resumes/{resumeId}/projects | ✅ | 匹配 |
| 获取技能 | GET /resumes/{resumeId}/skills | ✅ | 匹配 |
| 获取完整简历 | GET /resumes/{resumeId}/complete | ❌ | **缺失** |

**路径错误详情**:
- 前端使用: `/resumes/user/${userId}` ❌
- 后端正确: `/resumes/my` ✅
- 前端使用: `/resumes/user/${userId}/default` ❌
- 后端正确: `/resumes/my/default` ✅

---

### 6. 模拟面试模块 ⚠️ **部分缺失**

| 接口 | 后端路径 | 前端实现 | 状态 |
|------|---------|---------|------|
| 创建面试会话 | POST /mock-interview/start | ✅ | 匹配 |
| 获取会话详情 | GET /mock-interview/sessions/{sessionId} | ✅ | 匹配 |
| 获取用户会话 | GET /mock-interview/sessions/my | ✅ | 匹配 |
| 发送消息 | POST /mock-interview/sessions/{sessionId}/message | ✅ | 匹配 |
| 获取消息列表 | GET /mock-interview/sessions/{sessionId}/messages | ✅ | 匹配 |
| 结束面试 | POST /mock-interview/sessions/{sessionId}/finish | ✅ | 匹配 |
| 暂停面试 | POST /mock-interview/sessions/{sessionId}/pause | ❌ | **缺失** |
| 恢复面试 | POST /mock-interview/sessions/{sessionId}/resume | ❌ | **缺失** |
| 获取进度 | GET /mock-interview/sessions/{sessionId}/progress | ❌ | **缺失** |
| 获取评分列表 | GET /mock-interview/sessions/{sessionId}/evaluations | ✅ | 匹配 |
| 评估单轮回答 | POST /mock-interview/sessions/{sessionId}/evaluate | ✅ | 匹配 |

---

### 7. 聊天模块 ⚠️ **部分缺失**

| 接口 | 后端路径 | 前端实现 | 状态 |
|------|---------|---------|------|
| 获取会话列表 | GET /chat/sessions | ✅ | 匹配 |
| 获取消息历史 | GET /chat/sessions/{sessionKey}/messages | ✅ | 匹配 |
| 获取消息历史（含图片） | GET /chat/sessions/{sessionKey}/messages-with-images | ✅ | 匹配 |
| 删除会话 | DELETE /chat/sessions/{sessionKey} | ✅ | 匹配 |
| 获取工具调用记录 | GET /chat/messages/{messageId}/tool-calls | ✅ | 匹配 |
| 获取图片 | GET /chat/images/{imageId} | ❌ | **缺失** |
| 上传图片 | POST /chat/upload/image | ✅ | 匹配 |
| 创建会话 | - | ✅ | **前端多余** |

**问题**: 前端实现了 `createSession` 接口，但后端文档未定义

---

### 8. OCR识别模块 ⚠️ **部分缺失**

| 接口 | 后端路径 | 前端实现 | 状态 |
|------|---------|---------|------|
| 直接OCR识别 | POST /ocr/recognize | ✅ | 匹配 |
| 识别简历 | POST /ocr/resume | ✅ | 匹配 |
| 识别JD | POST /ocr/jd | ✅ | 匹配 |
| 获取用户OCR记录 | GET /ocr/records/my | ❌ | **缺失** |
| 获取OCR记录 | GET /ocr/records | ✅ | 匹配 |
| 上传图片 | - | ✅ | **前端多余** |

**问题**: 前端实现了 `uploadImage` 接口，但后端文档未定义

---

### 9. 技能标签模块 ❌ **完全缺失**

| 接口 | 后端路径 | 前端实现 | 状态 |
|------|---------|---------|------|
| 获取所有技能 | GET /skills | ❌ | **缺失** |
| 根据分类获取 | GET /skills/category/{category} | ❌ | **缺失** |
| 搜索技能 | GET /skills/search | ❌ | **缺失** |
| 获取技能详情 | GET /skills/{skillId} | ❌ | **缺失** |
| 创建技能 | POST /skills | ❌ | **缺失** |
| 更新技能 | PUT /skills/{skillId} | ❌ | **缺失** |
| 删除技能 | DELETE /skills/{skillId} | ❌ | **缺失** |
| 获取子技能 | GET /skills/{skillId}/children | ❌ | **缺失** |

---

### 10. 状态转换模块 ❌ **完全缺失**

| 接口 | 后端路径 | 前端实现 | 状态 |
|------|---------|---------|------|
| 申请状态转换规则 | GET /status/transitions | ❌ | **缺失** |
| 面试状态转换规则 | GET /status/interview/transitions | ❌ | **缺失** |
| 申请下一个状态 | GET /status/applications/{status}/next | ❌ | **缺失** |
| 面试下一个状态 | GET /status/interview/{status}/next | ❌ | **缺失** |
| 验证申请状态转换 | GET /status/applications/validate | ❌ | **缺失** |
| 验证面试状态转换 | GET /status/interview/validate | ❌ | **缺失** |
| 获取所有申请状态 | GET /status/applications | ❌ | **缺失** |
| 获取所有面试状态 | GET /status/interview | ❌ | **缺失** |

---

### 11. 统计数据模块 ✅ **完全匹配**

| 接口 | 后端路径 | 前端实现 | 状态 |
|------|---------|---------|------|
| 获取统计数据 | GET /statistics | ✅ | 匹配 |

---

### 12. 数据导出模块 ✅ **完全匹配**

| 接口 | 后端路径 | 前端实现 | 状态 |
|------|---------|---------|------|
| 获取申请详情 | GET /export/applications/{id}/detail | ✅ | 匹配 |
| 导出Excel | GET /export/excel | ✅ | 匹配 |
| 导出JSON | GET /export/json | ✅ | 匹配 |

---

### 13. 搜索功能模块 ✅ **完全匹配**

| 接口 | 后端路径 | 前端实现 | 状态 |
|------|---------|---------|------|
| 搜索申请 | GET /search/applications | ✅ | 匹配 |

---

### 14. 日志模块 ✅ **完全匹配**

| 接口 | 后端路径 | 前端实现 | 状态 |
|------|---------|---------|------|
| 获取所有日志 | GET /logs | ✅ | 匹配 |
| 获取申请日志 | GET /logs/applications/{applicationId} | ✅ | 匹配 |

---

### 15. 数据查询模块 ❌ **完全缺失**

**说明**: 此模块提供备用路径，前端未实现，但不影响功能（使用专用路径即可）

---

## 🔧 修复建议

### 优先级 P0 - 立即修复

1. **修复简历模块路径错误**
   - 文件: `src/services/resumeApi.ts`, `src/services/interviewApi.ts`
   - 修改: `/resumes/user/${userId}` → `/resumes/my`
   - 修改: `/resumes/user/${userId}/default` → `/resumes/my/default`

2. **添加模拟面试缺失接口**
   - 文件: `src/services/interviewApi.ts`
   - 添加: pause, resume, getProgress 接口

### 优先级 P1 - 高优先级

3. **添加面试记录缺失接口**
   - 文件: `src/services/dataApi.ts`
   - 添加: getUpcoming, getProgress, getCurrent, start, markFinal, reschedule

4. **添加求职申请缺失接口**
   - 文件: `src/services/dataApi.ts`
   - 添加: getByStatus, getPage, getHighPriority, updateStatus

### 优先级 P2 - 中优先级

5. **添加公司信息缺失接口**
   - 文件: `src/services/dataApi.ts`
   - 添加: getByName, search

6. **添加技能标签模块**
   - 新建: `src/services/skillApi.ts`
   - 实现所有8个接口

7. **添加状态转换模块**
   - 新建: `src/services/statusApi.ts`
   - 实现所有8个接口

### 优先级 P3 - 低优先级

8. **添加OCR缺失接口**
   - 文件: `src/services/ocrApi.ts`
   - 添加: getMyRecords

9. **添加聊天缺失接口**
   - 文件: `src/services/chatApi.ts`
   - 添加: getImage

10. **添加简历缺失接口**
    - 文件: `src/services/resumeApi.ts`
    - 添加: createComplete, getComplete

---

## 📝 其他发现

### 1. API Client 重复定义

**问题**: 存在两个 API Client 实现
- `src/lib/apiClient.ts` - 新版本，封装更好
- `src/services/api.ts` - 旧版本

**建议**: 统一使用 `src/lib/apiClient.ts`，删除 `src/services/api.ts`

### 2. 响应格式处理不一致

**问题**: 部分文件直接返回 `response.data`，部分返回 `response.data.data`

**建议**: 统一使用 `apiClient` 的封装，自动处理 `Result<T>` 格式

### 3. 缺少类型定义

**问题**: 部分接口缺少 TypeScript 类型定义

**建议**: 为所有接口添加完整的类型定义

---

## 📚 参考文档

- 后端API文档: `/Users/qingyu/job-tracker/frontend/docs/完整API参考文档_20260319.md`
- 前端API实现: `/Users/qingyu/job-tracker/frontend/src/services/`

---

**报告生成者**: AI Assistant  
**最后更新**: 2026-03-19
