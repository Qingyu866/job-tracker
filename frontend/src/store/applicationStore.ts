import { create } from 'zustand';
import type { JobApplication, Company, InterviewRecord, ApplicationLogDTO, ViewType } from '@/types';
import { apiClient } from '@/lib/apiClient';

interface ApplicationStore {
  // 状态
  applications: JobApplication[];
  companies: Company[];
  interviews: InterviewRecord[];
  logs: ApplicationLogDTO[];
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
  fetchInterviews: () => Promise<void>;
  fetchLogs: () => Promise<void>;
  createApplication: (data: Partial<JobApplication>) => Promise<number>;
  updateApplication: (id: number, data: Partial<JobApplication>) => Promise<void>;
  updateApplicationStatus: (id: number, status: string) => Promise<void>;
  deleteApplication: (id: number) => Promise<void>;
  switchView: (view: ViewType) => void;
  setFilters: (filters: Partial<ApplicationStore['filters']>) => void;
}

export const useApplicationStore = create<ApplicationStore>((set, get) => ({
  // 初始状态
  applications: [],
  companies: [],
  interviews: [],
  logs: [],
  currentView: 'table',
  filters: {},
  loading: false,
  error: null,

  // 获取申请列表
  fetchApplications: async () => {
    set({ loading: true, error: null });
    try {
      // 同时获取申请和公司
      const [appsResponse, companiesResponse] = await Promise.all([
        apiClient.get<JobApplication[]>('/applications'),
        apiClient.get<Company[]>('/companies')
      ]);

      const applications = appsResponse.data || [];
      const companies = companiesResponse.data || [];

      // 为每个申请关联公司信息
      const applicationsWithCompany = applications.map(app => ({
        ...app,
        company: companies.find(c => c.id === app.companyId) || null
      }));

      set({ applications: applicationsWithCompany, companies, loading: false });
    } catch (error) {
      const message = error instanceof Error ? error.message : '获取申请列表失败';
      set({ error: message, loading: false, applications: [] });
    }
  },

  // 获取面试列表
  fetchInterviews: async () => {
    set({ loading: true, error: null });
    try {
      const response = await apiClient.get<InterviewRecord[]>('/interviews');
      set({ interviews: response.data || [], loading: false });
    } catch (error) {
      const message = error instanceof Error ? error.message : '获取面试列表失败';
      set({ error: message, loading: false, interviews: [] });
    }
  },

  // 获取日志列表
  fetchLogs: async () => {
    set({ loading: true, error: null });
    try {
      const response = await apiClient.get<ApplicationLogDTO[]>('/logs');
      set({ logs: response.data || [], loading: false });
    } catch (error) {
      const message = error instanceof Error ? error.message : '获取日志列表失败';
      set({ error: message, loading: false, logs: [] });
    }
  },

  // 创建申请
  createApplication: async (data: Partial<JobApplication>) => {
    try {
      const response = await apiClient.post<number>('/applications', data);
      // 刷新列表
      await get().fetchApplications();
      return response.data;
    } catch (error) {
      const message = error instanceof Error ? error.message : '创建申请失败';
      set({ error: message });
      throw error;
    }
  },

  // 更新申请
  updateApplication: async (id: number, data: Partial<JobApplication>) => {
    try {
      await apiClient.put(`/applications/${id}`, data);
      // 刷新列表
      await get().fetchApplications();
    } catch (error) {
      const message = error instanceof Error ? error.message : '更新失败';
      set({ error: message });
      throw error;
    }
  },

  // 更新申请状态
  updateApplicationStatus: async (id: number, status: string) => {
    try {
      await apiClient.put(`/applications/${id}/status?status=${encodeURIComponent(status)}`, {});
      // 刷新列表
      await get().fetchApplications();
    } catch (error) {
      const message = error instanceof Error ? error.message : '状态更新失败';
      set({ error: message });
      throw error;
    }
  },

  // 删除申请
  deleteApplication: async (id: number) => {
    try {
      await apiClient.delete(`/applications/${id}`);
      // 刷新列表
      await get().fetchApplications();
    } catch (error) {
      const message = error instanceof Error ? error.message : '删除失败';
      set({ error: message });
      throw error;
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
