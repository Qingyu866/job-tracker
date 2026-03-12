// 状态映射
export const STATUS_CONFIG = {
  WISHLIST: { label: '愿望清单', color: 'gray' },
  APPLIED: { label: '已投递', color: 'blue' },
  INTERVIEW: { label: '面试中', color: 'purple' },
  OFFER: { label: '已offer', color: 'green' },
  REJECTED: { label: '已拒绝', color: 'red' },
  WITHDRAWN: { label: '已撤回', color: 'orange' },
} as const;

// 视图类型
export type ViewType = 'table' | 'board' | 'timeline' | 'calendar';

export const VIEW_CONFIG = {
  table: { label: '表格', icon: 'Table' },
  board: { label: '看板', icon: 'Columns' },
  timeline: { label: '时间线', icon: 'Clock' },
  calendar: { label: '日历', icon: 'Calendar' },
} as const;

// API 配置
export const API_CONFIG = {
  baseURL: 'http://localhost:8080/api/data',
  wsURL: 'ws://localhost:8080/api/ws/chat',
};
