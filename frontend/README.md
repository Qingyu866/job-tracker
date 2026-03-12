# Job Tracker Frontend

求职追踪应用的前端界面 - Notion 风格，支持 AI 对话和多视图系统。

## 🎨 特性

- ✅ **Notion 风格 UI**：牛皮纸阅读风格，舒适宜人
- ✅ **多视图系统**：表格、看板、时间线、日历 4 种视图
- ✅ **拖拽功能**：看板视图支持拖拽更新状态
- ✅ **AI 对话面板**：集成 WebSocket 实时通信
- ✅ **快捷操作**：浮动按钮快速创建申请

## 🛠️ 技术栈

- **框架**: React 18 + Vite 5.0 + TypeScript 5.0
- **UI**: Tailwind CSS 4.x + 自定义牛皮纸风格
- **状态管理**: Zustand 4.4+
- **路由**: React Router 6.20+
- **HTTP**: Axios 1.6+
- **拖拽**: @dnd-kit/core 0.3+
- **日历**: react-big-calendar 1.11+

## 📦 安装

```bash
npm install
```

## 🚀 快速开始

```bash
# 开发模式
npm run dev

# 构建
npm run build

# 预览
npm run preview
```

## 🔧 环境变量

创建 `.env.local`：

```bash
VITE_API_BASE_URL=http://localhost:8080/api/data
VITE_WS_URL=ws://localhost:8080/api/ws/chat
```

## 📁 项目结构

```
frontend/
├── src/
│   ├── components/    # 组件
│   │   ├── chat/       # AI 对话
│   │   ├── common/     # 通用组件
│   │   ├── layout/     # 布局
│   │   └── views/      # 视图
│   ├── pages/         # 页面
│   ├── store/        # 状态管理
│   ├── services/     # API 服务
│   └── types/        # 类型定义
└── package.json
```

## 🎯 开发进度

- [x] Task 1-11: 核心功能完成
- [ ] Task 12: 响应式适配（可选）
- [x] Task 13: 测试与部署

## 🔗 相关链接

- 后端: [job-tracker-backend](../job-tracker-backend)
- 设计: [docs/plans/](../docs/plans)
