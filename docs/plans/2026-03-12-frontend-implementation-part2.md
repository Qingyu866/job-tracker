# 前端实现计划 - 第二阶段

**创建时间**: 2026-03-12
**当前状态**: Task 1-6 已完成
**剩余任务**: Task 7-13

---

## 📊 当前进度总结

### ✅ 已完成（Task 1-6）
- [x] 项目初始化（Vite + React + TypeScript + Tailwind 4.x）
- [x] 基础架构（类型定义、常量、路由）
- [x] 布局组件（SplitView、Header、ViewToggle）
- [x] 状态管理（ApplicationStore、ChatStore、UIStore）
- [x] API 服务层（Axios、HTTP API、WebSocket）
- [x] 表格视图（TableView + 牛皮纸风格）

### 📋 待实现（Task 7-13）
- [ ] Task 7: 看板视图
- [ ] Task 8: 时间线视图
- [ ] Task 9: 日历视图
- [ ] Task 10: AI 对话系统
- [ ] Task 11: 快捷操作
- [ ] Task 12: 响应式适配
- [ ] Task 13: 测试与部署

---

## Task 7: 看板视图

### 📁 文件清单
- **创建**: `frontend/src/components/views/BoardView.tsx`
- **创建**: `frontend/src/components/views/BoardColumn.tsx`
- **创建**: `frontend/src/components/views/BoardCard.tsx`
- **修改**: `frontend/src/pages/WorkspacePage.tsx`（集成看板视图）

### 🔧 实现步骤

#### Step 1: 安装拖拽依赖
```bash
npm install @dnd-kit/core @dnd-kit/sortable @dnd-kit/utilities
```

#### Step 2: 创建 BoardCard 组件
**文件**: `frontend/src/components/views/BoardCard.tsx`

**功能**:
- 显示单个申请卡片
- 显示公司 Logo（首字母）、职位、状态
- 优先级指示器
- 悬浮效果

**牛皮纸风格**:
- 卡片背景：`#faf8f3`（paper-50）
- 边框：`1px solid #ebe4d6`（paper-200）
- 阴影：`shadow-paper`
- 悬浮：`bg-paper-100`

**props**:
```typescript
interface BoardCardProps {
  application: JobApplication;
  onEdit?: (id: number) => void;
  onDelete?: (id: number) => void;
}
```

#### Step 3: 创建 BoardColumn 组件
**文件**: `frontend/src/components/views/BoardColumn.tsx`

**功能**:
- 使用 `@dnd-kit/core` 和 `@dnd-kit/sortable`
- 显示状态列（愿望清单、已投递、面试中、已offer、已拒绝、已撤回）
- 拖放区域（`Droppable`）
- 排序功能（`SortableContext`）

**关键代码结构**:
```tsx
import { DndContext, DragEndEvent } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';

interface BoardColumnProps {
  status: JobApplicationStatus;
  applications: JobApplication[];
  onMove: (id: number, newStatus: JobApplicationStatus) => void;
}
```

#### Step 4: 创建 BoardView 组件
**文件**: `frontend/src/components/views/BoardView.tsx`

**功能**:
- 水平滚动容器（`overflow-x-auto`）
- 6 列布局（使用 Flexbox）
- 集成 `DndContext` 处理拖拽
- 调用 `updateApplication` 更新状态

**布局结构**:
```tsx
<div className="flex gap-4 p-6 overflow-x-auto h-full">
  {Object.keys(STATUS_CONFIG).map(status => (
    <BoardColumn
      key={status}
      status={status}
      applications={filteredApplications[status]}
      onMove={handleMove}
    />
  ))}
</div>
```

**拖拽处理**:
```typescript
const handleDragEnd = async (event: DragEndEvent) => {
  const { active, over } = event;
  if (over && active.id !== over.id) {
    await updateApplication(active.id as number, {
      status: over.id as JobApplicationStatus
    });
  }
};
```

#### Step 5: 集成到 WorkspacePage
修改 `renderView()` 函数，添加 case 'board': return <BoardView />

### 🎨 牛皮纸风格设计
- 列标题：大字体 serif，paper-700
- 列背景：paper-100/50
- 卡片：圆角、阴影、悬浮效果
- 拖拽时：透明度降低 + 阴影增强

### ✅ 验证方式
1. 拖拽卡片到其他列
2. 状态正确更新
3. 动画流畅

---

## Task 8: 时间线视图

### 📁 文件清单
- **创建**: `frontend/src/components/views/TimelineView.tsx`
- **创建**: `frontend/src/components/views/TimelineItem.tsx`
- **修改**: `frontend/src/pages/WorkspacePage.tsx`

### 🔧 实现步骤

#### Step 1: 创建 TimelineItem 组件
**文件**: `frontend/src/components/views/TimelineItem.tsx`

**功能**:
- 显示时间节点（左侧时间 + 右侧内容）
- 连接线（垂直线）
- 状态图标（emoji 或图标）
- 详细信息展开/折叠

**样式要点**:
- 垂直线：`border-l-2 border-paper-200`
- 时间点：圆点 + 状态颜色
- 卡片：左侧对齐时间线

**props**:
```typescript
interface TimelineItemProps {
  application: JobApplication;
  showLine?: boolean;
}
```

#### Step 2: 创建 TimelineView 组件
**文件**: `frontend/src/components/views/TimelineView.tsx`

**功能**:
- 按申请日期降序排列
- 分组显示（按月份）
- 滚动容器

**数据结构**:
```typescript
const groupedByMonth = applications.reduce((groups, app) => {
  const month = new Date(app.applicationDate).toLocaleString('zh-CN', { year: 'numeric', month: 'long' });
  // ...
}, {});
```

**布局**:
```tsx
<div className="max-w-3xl mx-auto p-6">
  {Object.entries(groupedByMonth).map(([month, apps]) => (
    <div key={month} className="mb-8">
      <h3 className="text-xl font-serif text-paper-700 mb-4">{month}</h3>
      {apps.map(app => (
        <TimelineItem key={app.id} application={app} />
      ))}
    </div>
  ))}
</div>
```

### 🎨 牛皮纸风格设计
- 时间线颜色：paper-200
- 节点图标：状态对应的 emoji（📝、✅、💼、🎉、❌、🔙）
- 卡片：纸张纹理
- 字体：时间用 serif，内容用 sans-serif

### ✅ 验证方式
1. 时间线正确显示
2. 分组逻辑正确
3. 响应式布局

---

## Task 9: 日历视图

### 📁 文件清单
- **创建**: `frontend/src/components/views/CalendarView.tsx`
- **创建**: `frontend/src/components/views/CalendarEvent.tsx`
- **修改**: `frontend/src/pages/WorkspacePage.tsx`

### 🔧 实现步骤

#### Step 1: 安装日历依赖
```bash
npm install react-big-calendar date-fns
```

#### Step 2: 创建 CalendarEvent 组件
**文件**: `frontend/src/components/views/CalendarEvent.tsx`

**功能**:
- 显示日历中的事件
- 颜色区分状态
- Tooltip 显示详情

#### Step 3: 创建 CalendarView 组件
**文件**: `frontend/src/components/views/CalendarView.tsx`

**功能**:
- 使用 `react-big-calendar`
- 月视图/周视图/日视图切换
- 点击事件查看详情
- 自定义样式

**关键配置**:
```tsx
import { Calendar, dateFnsLocalizer } from 'react-big-calendar';
import { format } from 'date-fns';
import { zhCN } from 'date-fns/locale';

const localizer = dateFnsLocalizer({
  format,
  parse,
  startOfWeek,
  getDay,
  locales: { 'zh-CN': zhCN },
});

<Calendar
  localizer={localizer}
  events={events}
  startAccessor="applicationDate"
  endAccessor="applicationDate"
  style={{ height: '100%' }}
  views={['month', 'week', 'day']}
  defaultView="month"
  components={{
    event: CalendarEvent,
  }}
/>
```

**事件数据转换**:
```typescript
const events = applications.map(app => ({
  id: app.id,
  title: `${app.company?.name} - ${app.jobTitle}`,
  start: new Date(app.applicationDate),
  end: new Date(app.applicationDate),
  resource: app,
}));
```

### 🎨 牛皮纸风格设计
- 覆盖默认样式（使用 `@global` CSS）
- 背景色：paper-50
- 事件颜色：状态对应的强调色
- 边框：paper-200

**样式覆盖示例**:
```css
@layer components {
  .rbc-calendar {
    background-color: #faf8f3;
  }
  .rbc-header {
    color: #4a3828;
    font-family: Georgia, serif;
  }
  .rbc-today {
    background-color: #f5f0e6;
  }
  /* ... */
}
```

### ✅ 验证方式
1. 日历正确显示
2. 事件正确渲染
3. 点击事件查看详情

---

## Task 10: AI 对话系统

### 📁 文件清单
- **创建**: `frontend/src/components/chat/ChatPanel.tsx`
- **创建**: `frontend/src/components/chat/ChatMessage.tsx`
- **创建**: `frontend/src/components/chat/ChatInput.tsx`
- **修改**: `frontend/src/store/chatStore.ts`（集成 WebSocket）
- **修改**: `frontend/src/components/layout/SplitView.tsx`（替换占位符）
- **修改**: `frontend/src/pages/WorkspacePage.tsx`

### 🔧 实现步骤

#### Step 1: 创建 ChatMessage 组件
**文件**: `frontend/src/components/chat/ChatMessage.tsx`

**功能**:
- 消息气泡（用户/AI 不同样式）
- 打字机效果（流式显示）
- Markdown 渲染（可选）
- 时间戳

**样式**:
- 用户消息：右侧，accent-amber 背景
- AI 消息：左侧，paper-100 背景
- 字体：阅读字体

**打字机效果**:
```tsx
const [displayedText, setDisplayedText] = useState('');

useEffect(() => {
  if (isTyping) {
    let index = 0;
    const timer = setInterval(() => {
      if (index < content.length) {
        setDisplayedText(content.slice(0, index + 1));
        index++;
      } else {
        clearInterval(timer);
      }
    }, 30);
    return () => clearInterval(timer);
  }
}, [content, isTyping]);
```

#### Step 2: 创建 ChatInput 组件
**文件**: `frontend/src/components/chat/ChatInput.tsx`

**功能**:
- 多行文本输入
- 发送按钮
- 清空历史按钮
- 快捷指令按钮（如"帮我写简历"、"分析面试题"等）

#### Step 3: 创建 ChatPanel 组件
**文件**: `frontend/src/components/chat/ChatPanel.tsx`

**功能**:
- 消息列表（滚动容器）
- 自动滚动到底部
- 集成 ChatStore
- 连接状态指示

**布局**:
```tsx
<div className="flex flex-col h-full bg-paper-50">
  <div className="p-4 border-b border-paper-200">
    <h3 className="font-serif text-paper-700">🤖 AI 助手</h3>
  </div>
  <div className="flex-1 overflow-y-auto p-4">
    {messages.map(msg => (
      <ChatMessage key={msg.timestamp} message={msg} />
    ))}
  </div>
  <div className="p-4 border-t border-paper-200">
    <ChatInput onSend={sendMessage} />
  </div>
</div>
```

#### Step 4: 集成 WebSocket 到 ChatStore
**文件**: `frontend/src/store/chatStore.ts`

**修改要点**:
- 导入 `websocketService`
- 在 `connect()` 中建立连接
- 在 `sendMessage()` 中使用 WebSocket
- 订阅消息队列

**示例代码**:
```typescript
import websocketService from '@/services/websocket';

// 在 connect() 中
await websocketService.connect();
websocketService.subscribe('/user/queue/messages', (message) => {
  const aiMessage: ChatMessage = {
    role: 'assistant',
    content: message.content,
    timestamp: Date.now(),
  };
  set({ messages: [...get().messages, aiMessage], isTyping: false });
});

// 在 sendMessage() 中
websocketService.publish('/app/chat', { content });
```

#### Step 5: 更新 SplitView 组件
替换占位符为实际的 `<ChatPanel />`

### 🎨 牛皮纸风格设计
- 整体背景：paper-50
- 消息气泡：圆角、阴影
- 输入框：paper-100 背景
- 打字机动画：流畅

### ✅ 验证方式
1. 发送消息
2. 接收回复
3. 打字机效果
4. 连接状态显示

---

## Task 11: 快捷操作

### 📁 文件清单
- **创建**: `frontend/src/components/common/QuickActions.tsx`
- **创建**: `frontend/src/components/common/Modal.tsx`
- **创建**: `frontend/src/components/common/CreateApplicationForm.tsx`
- **修改**: `frontend/src/pages/WorkspacePage.tsx`

### 🔧 实现步骤

#### Step 1: 创建 Modal 组件
**文件**: `frontend/src/components/common/Modal.tsx`

**功能**:
- 通用模态框
- 遮罩层（半透明）
- 关闭按钮
- 点击外部关闭

#### Step 2: 创建 CreateApplicationForm 组件
**文件**: `frontend/src/components/common/CreateApplicationForm.tsx`

**功能**:
- 表单输入（公司、职位、薪资等）
- 表单验证
- 提交到 store
- 成功后关闭模态框

**表单字段**:
- 公司名称（必填）
- 职位（必填）
- 状态（下拉选择）
- 申请日期（日期选择器）
- 优先级（星级或数字）
- 备注（文本域）

#### Step 3: 创建 QuickActions 组件
**文件**: `frontend/src/components/common/QuickActions.tsx`

**功能**:
- 浮动操作按钮（FAB）
- 或固定在 Header 右侧
- 下拉菜单或按钮组
- 快捷操作：
  - 新增申请
  - 导入数据
  - 批量编辑
  - 导出数据

**布局**:
```tsx
<div className="fixed bottom-6 right-6 z-50">
  <button className="bg-accent-amber text-white p-4 rounded-full shadow-paper-lg hover:shadow-xl transition-all">
    +
  </button>
  {/* 展开的菜单项 */}
</div>
```

#### Step 4: 集成到各个视图
- TableView：添加列操作按钮（编辑/删除）
- BoardView：卡片点击打开详情
- TimelineView：点击展开详情
- CalendarView：点击事件查看详情

### 🎨 牛皮纸风格设计
- 按钮：accent-amber 主色调
- 模态框：纸张纹理背景
- 表单：paper-100 输入框
- 按钮：圆角、阴影

### ✅ 验证方式
1. 打开/关闭模态框
2. 表单验证
3. 提交成功
4. 数据更新

---

## Task 12: 响应式适配

### 📁 文件清单
- **修改**: `frontend/src/components/views/TableView.tsx`
- **修改**: `frontend/src/components/views/BoardView.tsx`
- **修改**: `frontend/src/components/views/TimelineView.tsx`
- **修改**: `frontend/src/components/views/CalendarView.tsx`
- **修改**: `frontend/src/components/layout/SplitView.tsx`
- **修改**: `frontend/src/components/layout/Header.tsx`

### 🔧 实现步骤

#### Step 1: 移动端断点
**Tailwind 断点**:
- sm: 640px
- md: 768px
- lg: 1024px
- xl: 1280px

#### Step 2: 适配 Header
- 移动端：隐藏搜索框，显示汉堡菜单
- 平板：保留搜索框，调整间距
- 桌面：完整显示

**示例**:
```tsx
<header className="h-16 ... px-4 md:px-6">
  {/* Logo */}
  <h1 className="text-base md:text-xl">Job Tracker</h1>

  {/* 搜索 - 移动端隐藏 */}
  <div className="hidden md:block max-w-md mx-4 md:mx-8">...</div>

  {/* 移动端菜单按钮 */}
  <button className="md:hidden p-2">
    <Menu />
  </button>
</header>
```

#### Step 3: 适配 SplitView
- 移动端：默认收起 AI 面板，全屏显示主视图
- 平板：可收起/展开
- 桌面：固定显示

**响应式处理**:
```tsx
const [isPanelOpen, setIsPanelOpen] = useState(window.innerWidth > 768);

useEffect(() => {
  const handleResize = () => {
    setIsPanelOpen(window.innerWidth > 768);
  };
  window.addEventListener('resize', handleResize);
  return () => window.removeEventListener('resize', handleResize);
}, []);
```

#### Step 4: 适配 TableView
- 移动端：卡片式布局代替表格
- 平板：横向滚动
- 桌面：完整表格

**卡片布局（移动端）**:
```tsx
<div className="grid grid-cols-1 md:table">
  {applications.map(app => (
    <div key={app.id} className="md:table-row p-4 md:p-0">
      {/* 移动端卡片 */}
      <div className="md:hidden">
        <h3 className="font-medium">{app.company?.name}</h3>
        <p>{app.jobTitle}</p>
        {/* ... */}
      </div>
      {/* 桌面端表格行 */}
    </div>
  ))}
</div>
```

#### Step 5: 适配 BoardView
- 移动端：单列显示，横向滚动
- 平板：2-3 列
- 桌面：全部列

#### Step 6: 适配 TimelineView 和 CalendarView
- 字体大小调整
- 间距调整
- 触摸手势支持

### ✅ 验证方式
1. Chrome DevTools 设备模拟
2. 实际设备测试
3. 不同屏幕尺寸

---

## Task 13: 测试与部署

### 📁 文件清单
- **创建**: `frontend/.env.example`
- **创建**: `frontend/README.md`
- **创建**: `frontend/package.json`（scripts）
- **修改**: `frontend/vite.config.ts`（build 配置）

### 🔧 实步骤骤

#### Step 1: 环境变量管理
**创建**: `frontend/.env.example`
```bash
VITE_API_BASE_URL=http://localhost:8080/api/data
VITE_WS_URL=ws://localhost:8080/api/ws/chat
```

**创建**: `frontend/.env.local`
```bash
VITE_API_BASE_URL=http://localhost:8080/api/data
VITE_WS_URL=ws://localhost:8080/api/ws/chat
```

**更新**: `frontend/src/utils/constants.ts`
```typescript
export const API_CONFIG = {
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/data',
  wsURL: import.meta.env.VITE_WS_URL || 'ws://localhost:8080/api/ws/chat',
};
```

#### Step 2: 添加 npm scripts
**修改**: `frontend/package.json`
```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "preview": "vite preview",
    "lint": "eslint .",
    "type-check": "tsc --noEmit"
  }
}
```

#### Step 3: 创建 README.md
**文件**: `frontend/README.md`

**内容**:
- 项目简介
- 技术栈
- 环境要求
- 安装步骤
- 运行命令
- 环境变量说明
- 构建部署

#### Step 4: 优化构建配置
**修改**: `frontend/vite.config.ts`

**优化项**:
- 代码分割
- 压缩
- CDN 配置（可选）
- 环境变量注入

#### Step 5: 功能测试清单
- [ ] 所有视图正常显示
- [ ] CRUD 操作正常
- [ ] WebSocket 连接正常
- [ ] 拖拽功能正常
- [ ] 表单验证正常
- [ ] 响应式布局正常
- [ ] 无 console 错误
- [ ] 性能优化（懒加载、缓存）

#### Step 6: 部署准备
1. **构建生产版本**:
   ```bash
   npm run build
   ```

2. **测试构建版本**:
   ```bash
   npm run preview
   ```

3. **部署选项**:
   - **静态托管**：Vercel、Netlify、GitHub Pages
   - **传统服务器**：Nginx、Apache
   - **Docker**：创建 Dockerfile

4. **Nginx 配置示例**:
   ```nginx
   server {
     listen 80;
     root /var/www/job-tracker/frontend/dist;
     index index.html;

     location / {
       try_files $uri $uri/ /index.html;
     }

     location /api {
       proxy_pass http://localhost:8080;
     }

     location /api/ws {
       proxy_pass http://localhost:8080;
       proxy_http_version 1.1;
       proxy_set_header Upgrade $http_upgrade;
       proxy_set_header Connection "upgrade";
     }
   }
   ```

#### Step 7: Git 提交
```bash
git add .
git commit -m "feat: complete frontend implementation with all views"
```

### ✅ 验证方式
1. 本地构建成功
2. 生产环境测试
3. 部署成功
4. 功能完整

---

## 📊 实现优先级建议

### 高优先级（核心功能）
1. **Task 7: 看板视图** - 用户最常用的视图
2. **Task 10: AI 对话系统** - 核心差异化功能
3. **Task 11: 快捷操作** - 提升用户体验

### 中优先级（增强功能）
4. **Task 8: 时间线视图** - 辅助视图
5. **Task 9: 日历视图** - 时间管理

### 低优先级（优化）
6. **Task 12: 响应式适配** - 可以逐步完善
7. **Task 13: 测试与部署** - 最后阶段

---

## 🎯 预估工作量

| 任务 | 预估时间 | 难度 |
|-----|---------|------|
| Task 7: 看板视图 | 2-3h | ⭐⭐⭐ |
| Task 8: 时间线视图 | 1-2h | ⭐⭐ |
| Task 9: 日历视图 | 1-2h | ⭐⭐ |
| Task 10: AI 对话系统 | 2-3h | ⭐⭐⭐⭐ |
| Task 11: 快捷操作 | 1-2h | ⭐⭐ |
| Task 12: 响应式适配 | 2-3h | ⭐⭐⭐ |
| Task 13: 测试与部署 | 1-2h | ⭐⭐ |
| **总计** | **10-17h** | - |

---

## 📝 注意事项

### 技术要点
1. **拖拽功能**（@dnd-kit）：需要注意拖拽时的样式变化和数据更新
2. **WebSocket**：连接管理和重连机制很重要
3. **响应式**：使用 Tailwind 的响应式工具类
4. **性能优化**：大量数据时考虑虚拟滚动

### 代码规范
- 所有函数添加注释
- 使用 TypeScript 类型
- 遵循牛皮纸风格设计
- 保持代码简洁

### 测试建议
- 每完成一个任务就测试
- 优先测试核心功能
- 注意控制台错误

---

## 🚀 开始执行

明天执行时，建议按照以下顺序：
1. Task 7（看板视图）→ 核心功能
2. Task 10（AI 对话）→ 核心功能
3. Task 11（快捷操作）→ 提升体验
4. Task 8-9（其他视图）→ 完善功能
5. Task 12-13（优化部署）→ 最后完善

每个任务完成后记得：
- 运行 `npm run build` 验证
- 启动 `npm run dev` 测试
- Git commit 保存进度

**预计总时间**: 10-17 小时（可以分 2-3 天完成）

---

**准备好了吗？明天 5 点 token 重置后开始！** 🎯
