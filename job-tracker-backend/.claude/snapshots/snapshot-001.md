# 快照 #001 - 2026-03-15

## 🎯 当前目标
实现多模态聊天功能（图片 + 文字输入），让 AI 能够分析用户上传的图片。

## 📦 项目状态
- **工作目录**：`/Users/qingyu/job-tracker/job-tracker-backend`
- **当前分支**：`main`
- **技术栈**：Spring Boot + LangChain4j + MyBatis Plus + MySQL
- **数据库**：需要执行 V7__multimodal_support.sql 迁移脚本

## ✅ 已完成 - 后端开发

### 阶段 1：数据库和实体
- [x] 创建 `V7__multimodal_support.sql` 数据库迁移脚本
  - 新增 `chat_images` 表（独立存储图片元数据）
  - 添加索引优化查询
- [x] 创建 `ChatImage` 实体类
- [x] 创建 `ChatImageMapper` 接口
- [x] 创建 `ImageAttachment` DTO（返回给前端）

### 阶段 2：服务和控制器
- [x] 创建 `FileStorageService`（文件上传、读取、删除）
- [x] 创建 `ChatImageService`（图片保存、安全校验）
- [x] 增强 `ChatController`
  - `GET /chat/images/{imageId}?sessionId={sessionKey}` - 图片访问（带安全校验）
  - `POST /chat/upload/image?messageId={id}` - 图片上传
- [x] 扩展 `ChatHistoryService`（添加图片相关方法）
- [x] 扩展 `WebSocketMessage`（添加 images 字段）
- [x] 修改 `ChatWebSocketHandler`（支持多模态消息处理）
- [x] 扩展 `JobAgent` 接口（添加 chatWithImages 方法框架）

### 核心设计
- **安全访问**：通过 `imageId` + `sessionId` 双重校验，防止越权访问
- **文件存储**：本地磁盘存储，数据库只存路径信息
- **独立图片表**：`chat_images` 表包含 id、message_id、session_id（冗余用于安全校验）

## ⏳ 进行中
无

## 🔜 待办

### 前端开发（阶段 3）
- [ ] 扩展 `chatApi.ts`（添加 uploadImage 方法）
- [ ] 创建 `ImageUploader.tsx` 组件（图片选择、上传、预览）
- [ ] 修改 `ChatInput.tsx`（添加图片上传按钮）
- [ ] 修改 `ChatMessage.tsx`（支持图片渲染）
- [ ] 创建 `ChatMessageImage.tsx`（图片展示组件，含错误处理）
- [ ] 更新 `chatStore.ts`（sendMessage 支持图片）

### 配置和测试
- [ ] 执行数据库迁移 `V7__multimodal_support.sql`
- [ ] 更新 `application.yml` 配置文件上传目录
- [ ] 功能测试（上传、访问、安全校验）

### AI 多模态集成
- [ ] 实现 `JobAgent.chatWithImages` 的多模态支持
  - 需要集成 LangChain4j 的多模态 API
  - 读取本地文件并转换为 Base64
  - 构建包含图片的 UserMessage

## 🧠 用户偏好
- 使用中文进行沟通
- 文档命名遵循「中文描述_YYYYMMDD.md」格式
- 文档按类型分类存放（功能设计/问题修复/知识点/架构设计/API文档）
- 优先使用项目中已有的自定义实现
- 审批后再执行：创建设计文档后需等待用户确认才能开始实现

## 📎 相关文件

### 后端新创建
```
src/main/java/com/jobtracker/
├── entity/ChatImage.java              # 图片实体
├── mapper/ChatImageMapper.java        # 图片 Mapper
├── dto/ImageAttachment.java           # 图片附件 DTO
├── service/
│   ├── FileStorageService.java       # 文件存储服务
│   └── ChatImageService.java         # 图片业务服务
```

### 后端已修改
```
src/main/java/com/jobtracker/
├── controller/ChatController.java    # 添加图片接口
├── service/ChatHistoryService.java   # 添加图片方法
├── dto/WebSocketMessage.java         # 添加 images 字段
├── agent/JobAgent.java               # 添加 chatWithImages
└── websocket/ChatWebSocketHandler.java  # 支持图片消息
```

### 文档
```
docs/功能设计/
├── 多模态聊天图片文字输入功能设计_20260315.md  # 主设计文档
└── 聊天历史图片展示方案设计_20260315.md        # 图片访问设计
```

### 数据库
```
src/main/resources/db/migration/
└── V7__multimodal_support.sql  # 待执行
```

---
_初始快照_
