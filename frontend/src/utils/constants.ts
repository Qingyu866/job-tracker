export const STATUS_CONFIG = {
  WISHLIST: {
    label: '意愿清单',
    color: 'gray',
    bgClass: 'bg-gray-100',
    textClass: 'text-gray-700',
    stage: 'initial',
    description: '尚未申请，仅在关注中',
  },
  APPLIED: {
    label: '已申请',
    color: 'blue',
    bgClass: 'bg-blue-100',
    textClass: 'text-blue-700',
    stage: 'applied',
    description: '已提交申请，等待回复',
  },
  SCREENING: {
    label: '筛选中',
    color: 'cyan',
    bgClass: 'bg-cyan-100',
    textClass: 'text-cyan-700',
    stage: 'applied',
    description: 'HR正在筛选简历',
  },
  INTERVIEW: {
    label: '面试中',
    color: 'orange',
    bgClass: 'bg-orange-100',
    textClass: 'text-orange-700',
    stage: 'interview',
    description: '正在面试流程中',
  },
  FINAL_ROUND: {
    label: '终面中',
    color: 'yellow',
    bgClass: 'bg-yellow-100',
    textClass: 'text-yellow-700',
    stage: 'interview',
    description: '已进入最后一轮面试',
  },
  OFFERED: {
    label: '已收到Offer',
    color: 'purple',
    bgClass: 'bg-purple-100',
    textClass: 'text-purple-700',
    stage: 'offer',
    description: '已收到录用通知',
  },
  ACCEPTED: {
    label: '已接受',
    color: 'green',
    bgClass: 'bg-green-100',
    textClass: 'text-green-700',
    stage: 'completed',
    description: '已接受Offer',
  },
  DECLINED: {
    label: '已拒绝Offer',
    color: 'red',
    bgClass: 'bg-red-100',
    textClass: 'text-red-700',
    stage: 'completed',
    description: '主动拒绝了Offer',
  },
  EXPIRED: {
    label: 'Offer过期',
    color: 'gray',
    bgClass: 'bg-gray-100',
    textClass: 'text-gray-700',
    stage: 'completed',
    description: 'Offer超时未处理',
  },
  REJECTED: {
    label: '已被拒绝',
    color: 'red',
    bgClass: 'bg-red-100',
    textClass: 'text-red-700',
    stage: 'completed',
    description: '申请被公司拒绝',
  },
  WITHDRAWN: {
    label: '已撤回',
    color: 'gray',
    bgClass: 'bg-gray-100',
    textClass: 'text-gray-700',
    stage: 'completed',
    description: '主动撤回申请',
  },
} as const;

export const STATUS_STAGES = {
  initial: {
    label: '准备中',
    statuses: ['WISHLIST'] as const,
  },
  applied: {
    label: '申请中',
    statuses: ['APPLIED', 'SCREENING'] as const,
  },
  interview: {
    label: '面试中',
    statuses: ['INTERVIEW', 'FINAL_ROUND'] as const,
  },
  offer: {
    label: 'Offer',
    statuses: ['OFFERED'] as const,
  },
  completed: {
    label: '已结束',
    statuses: ['ACCEPTED', 'DECLINED', 'EXPIRED', 'REJECTED', 'WITHDRAWN'] as const,
  },
} as const;

export const STATUS_TRANSITIONS: Record<string, string[]> = {
  WISHLIST: ['APPLIED', 'WITHDRAWN'],
  APPLIED: ['SCREENING', 'INTERVIEW', 'REJECTED', 'WITHDRAWN'],
  SCREENING: ['INTERVIEW', 'REJECTED', 'WITHDRAWN'],
  INTERVIEW: ['FINAL_ROUND', 'OFFERED', 'REJECTED', 'WITHDRAWN'],
  FINAL_ROUND: ['OFFERED', 'REJECTED', 'WITHDRAWN'],
  OFFERED: ['ACCEPTED', 'DECLINED', 'EXPIRED'],
  ACCEPTED: [],
  DECLINED: [],
  EXPIRED: [],
  REJECTED: [],
  WITHDRAWN: [],
};

export function isValidTransition(from: string, to: string): boolean {
  const allowed = STATUS_TRANSITIONS[from];
  return allowed ? allowed.includes(to) : false;
}

export function getNextStatuses(currentStatus: string): string[] {
  return STATUS_TRANSITIONS[currentStatus] || [];
}

export function isTerminalStatus(status: string): boolean {
  const transitions = STATUS_TRANSITIONS[status];
  return transitions !== undefined && transitions.length === 0;
}

export function canScheduleInterview(status: string): boolean {
  const allowedStatuses = [
    'SCREENING',
    'INTERVIEW',
    'FINAL_ROUND',
    'OFFERED',
  ];
  return allowedStatuses.includes(status);
}

export function getInterviewDisabledReason(status: string): string | null {
  if (canScheduleInterview(status)) return null;

  const reasons: Record<string, string> = {
    WISHLIST: '尚未投递申请，无法安排面试',
    APPLIED: '申请刚提交，请等待 HR 筛选后再安排面试',
    ACCEPTED: '已接受 Offer，无需再安排面试',
    DECLINED: '已拒绝 Offer，无法安排面试',
    EXPIRED: 'Offer 已过期，无法安排面试',
    REJECTED: '申请已被拒绝，无法安排面试',
    WITHDRAWN: '已撤回申请，无法安排面试',
  };

  return reasons[status] || '当前状态无法安排面试';
}

export type ViewType = 'table' | 'board' | 'timeline' | 'calendar';

export const VIEW_CONFIG = {
  table: { label: '表格', icon: 'Table' },
  board: { label: '看板', icon: 'Columns' },
  timeline: { label: '时间线', icon: 'Clock' },
  calendar: { label: '日历', icon: 'Calendar' },
} as const;

export const API_CONFIG = {
  baseURL: 'http://localhost:8080/api',
  wsURL: 'ws://localhost:8080/api/ws/chat',
};
