# 前端开发文档索引

**最后更新**: 2026-03-13

---

## 文档列表

| 文档 | 说明 |
|------|------|
| [DATA_API_REFERENCE.md](./DATA_API_REFERENCE.md) | 数据管理API接口文档（P1+P2阶段） |
| [DATA_TYPES.md](./DATA_TYPES.md) | TypeScript类型定义参考 |
| [CHAT_API_REFERENCE.md](./CHAT_API_REFERENCE.md) | 聊天历史API接口文档 |
| [CHAT_HISTORY_INTEGRATION.md](./CHAT_HISTORY_INTEGRATION.md) | 聊天历史前端集成指南 |

---

## 快速导航

### 数据管理功能

**需要调用的接口**：
- 更新/删除申请、面试、公司 → 查看 `DATA_API_REFERENCE.md`
- 获取申请详情聚合 → 查看 `DATA_API_REFERENCE.md` 第三节
- 导出Excel/JSON → 查看 `DATA_API_REFERENCE.md` 第四节

**类型定义**：
- 所有实体类型和枚举 → 查看 `DATA_TYPES.md`

### 聊天历史功能

**需要调用的接口**：
- 获取会话列表/消息历史 → 查看 `CHAT_API_REFERENCE.md`
- 删除会话 → 查看 `CHAT_API_REFERENCE.md`

**集成指南**：
- Store改造、组件实现 → 查看 `CHAT_HISTORY_INTEGRATION.md`

---

## 后端API基础信息

| 项目 | 值 |
|------|------|
| 数据API | `http://localhost:8080/api/data` |
| 聊天API | `http://localhost:8080/api/chat` |
| WebSocket | `ws://localhost:8080/ws/chat` |

---

## 相关后端文档

后端文档位于 `../job-tracker-backend/docs/`：

| 文档 | 说明 |
|------|------|
| `NEW_API_REFERENCE.md` | P1阶段接口文档 |
| `P2_API_REFERENCE.md` | P2阶段接口文档 |
| `AI_CHAT_PERSISTENCE_DESIGN.md` | AI聊天持久化设计 |
| `PENDING_INTERFACES.md` | 接口开发进度 |
