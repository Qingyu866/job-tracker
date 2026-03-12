import { create } from 'zustand';
import type { JobApplication, Company, InterviewRecord, ViewType } from '@/types';

interface ApplicationStore {
  // 状态
  applications: JobApplication[];
  companies: Company[];
  interviews: InterviewRecord[];
  currentView: ViewType;
  filters: {
    status?: string;
    priority?: number;
    keyword?: string;
  };
  loading: boolean;
  error: string | null;

  // 操作
  fetchApplications: () => Promise<void>;
  createApplication: (data: Partial<JobApplication>) => Promise<number>;
  updateApplication: (id: number, data: Partial<JobApplication>) => Promise<void>;
  deleteApplication: (id: number) => Promise<void>;
  switchView: (view: ViewType) => void;
  setFilters: (filters: Partial<ApplicationStore['filters']>) => void;
}

// 模拟数据（将在 Task 5 中连接真实 API）
const mockApplications: JobApplication[] = [
  {
    id: 1,
    companyId: 1,
    jobTitle: '高级前端工程师',
    status: 'APPLIED',
    applicationDate: '2026-03-10',
    priority: 1,
    createdAt: '2026-03-10T10:00:00',
    company: {
      id: 1,
      name: '字节跳动',
      industry: '互联网',
      location: '北京',
      createdAt: '2026-03-10T10:00:00',
    },
  },
  {
    id: 2,
    companyId: 2,
    jobTitle: '全栈工程师',
    status: 'INTERVIEW',
    applicationDate: '2026-03-08',
    priority: 2,
    createdAt: '2026-03-08T10:00:00',
    company: {
      id: 2,
      name: '阿里巴巴',
      industry: '互联网',
      location: '杭州',
      createdAt: '2026-03-08T10:00:00',
    },
  },
];

export const useApplicationStore = create<ApplicationStore>((set, get) => ({
  // 初始状态
  applications: mockApplications,
  companies: [],
  interviews: [],
  currentView: 'table',
  filters: {},
  loading: false,
  error: null,

  // 获取申请列表
  fetchApplications: async () => {
    set({ loading: true, error: null });
    try {
      // TODO: Task 5 将连接真实 API
      // const response = await apiClient.get<Result<JobApplication[]>>('/applications');

      // 模拟 API 调用
      await new Promise(resolve => setTimeout(resolve, 500));
      set({ applications: mockApplications, loading: false });
    } catch (error) {
      set({ error: '获取申请列表失败', loading: false });
    }
  },

  // 创建申请
  createApplication: async (data: Partial<JobApplication>) => {
    try {
      // TODO: Task 5 将连接真实 API
      const newId = Math.max(...mockApplications.map(app => app.id)) + 1;
      const newApplication: JobApplication = {
        id: newId,
        companyId: data.companyId || 0,
        jobTitle: data.jobTitle || '',
        status: data.status || 'WISHLIST',
        createdAt: new Date().toISOString(),
        ...data,
      };
      mockApplications.push(newApplication);
      await get().fetchApplications();
      return newId;
    } catch (error) {
      set({ error: '创建申请失败' });
      throw error;
    }
  },

  // 更新申请
  updateApplication: async (id: number, data: Partial<JobApplication>) => {
    try {
      // TODO: Task 5 将连接真实 API
      const index = mockApplications.findIndex(app => app.id === id);
      if (index !== -1) {
        mockApplications[index] = { ...mockApplications[index], ...data };
        await get().fetchApplications();
      }
    } catch (error) {
      set({ error: '更新失败' });
    }
  },

  // 删除申请
  deleteApplication: async (id: number) => {
    try {
      // TODO: Task 5 将连接真实 API
      const index = mockApplications.findIndex(app => app.id === id);
      if (index !== -1) {
        mockApplications.splice(index, 1);
        await get().fetchApplications();
      }
    } catch (error) {
      set({ error: '删除失败' });
    }
  },

  // 切换视图
  switchView: (view) => {
    set({ currentView: view });
  },

  // 设置筛选
  setFilters: (filters) => {
    set({ filters: { ...get().filters, ...filters } });
  },
}));
