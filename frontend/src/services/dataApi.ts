import apiClient from './api';
import type { JobApplication, Company, InterviewRecord, ApplicationLogDTO } from '@/types';

/**
 * 数据 API 服务
 * 封装所有后端 REST API 调用
 */
export const dataApi = {
  // ========== 申请相关 ==========

  /**
   * 获取所有申请
   */
  getApplications: async (keyword?: string): Promise<JobApplication[]> => {
    const params = keyword ? { keyword } : {};
    const response = await apiClient.get<any>('/applications', { params });
    return response.data.data;
  },

  /**
   * 搜索申请（多字段搜索）
   */
  searchApplications: async (keyword: string): Promise<JobApplication[]> => {
    const response = await apiClient.get<any>('/applications/search', {
      params: { keyword }
    });
    return response.data.data;
  },

  /**
   * 获取单个申请
   */
  getApplication: async (id: number): Promise<JobApplication> => {
    const response = await apiClient.get<any>(`/applications/${id}`);
    return response.data.data;
  },

  /**
   * 创建申请
   */
  createApplication: async (data: Partial<JobApplication>): Promise<number> => {
    const response = await apiClient.post<any>('/applications', data);
    return response.data.data;
  },

  /**
   * 更新申请
   */
  updateApplication: async (id: number, data: Partial<JobApplication>): Promise<void> => {
    await apiClient.put(`/applications/${id}`, data);
  },

  /**
   * 删除申请
   */
  deleteApplication: async (id: number): Promise<void> => {
    await apiClient.delete(`/applications/${id}`);
  },

  // ========== 公司相关 ==========

  /**
   * 获取所有公司
   */
  getCompanies: async (): Promise<Company[]> => {
    const response = await apiClient.get<any>('/companies');
    return response.data.data;
  },

  /**
   * 获取单个公司
   */
  getCompany: async (id: number): Promise<Company> => {
    const response = await apiClient.get<any>(`/companies/${id}`);
    return response.data.data;
  },

  /**
   * 创建公司
   */
  createCompany: async (data: Partial<Company>): Promise<number> => {
    const response = await apiClient.post<any>('/companies', data);
    return response.data.data;
  },

  /**
   * 更新公司信息
   */
  updateCompany: async (id: number, data: Partial<Company>): Promise<void> => {
    await apiClient.put(`/companies/${id}`, data);
  },

  /**
   * 删除公司（保护性删除）
   */
  deleteCompany: async (id: number): Promise<void> => {
    await apiClient.delete(`/companies/${id}`);
  },

  // ========== 面试相关 ==========

  /**
   * 获取所有面试记录
   */
  getInterviews: async (): Promise<InterviewRecord[]> => {
    const response = await apiClient.get<any>('/interviews');
    return response.data.data;
  },

  /**
   * 获取单个面试记录
   */
  getInterview: async (id: number): Promise<InterviewRecord> => {
    const response = await apiClient.get<any>(`/interviews/${id}`);
    return response.data.data;
  },

  /**
   * 更新面试记录
   */
  updateInterview: async (id: number, data: Partial<InterviewRecord>): Promise<void> => {
    await apiClient.put(`/interviews/${id}`, data);
  },

  /**
   * 删除面试记录
   */
  deleteInterview: async (id: number): Promise<void> => {
    await apiClient.delete(`/interviews/${id}`);
  },

  /**
   * 获取申请的面试记录
   */
  getInterviewsByApplication: async (applicationId: number): Promise<InterviewRecord[]> => {
    const response = await apiClient.get<any>(`/applications/${applicationId}/interviews`);
    return response.data.data;
  },

  /**
   * 创建面试记录
   */
  createInterview: async (data: Partial<InterviewRecord>): Promise<number> => {
    const response = await apiClient.post<any>('/interviews', data);
    return response.data.data;
  },

  /**
   * 标记面试为已完成
   */
  completeInterview: async (
    id: number,
    rating?: number,
    feedback?: string
  ): Promise<boolean> => {
    const response = await apiClient.post<any>(`/interviews/${id}/complete`, {
      rating,
      feedback,
    });
    return response.data.data;
  },

  /**
   * 更新面试反馈
   */
  updateFeedback: async (id: number, feedback: string): Promise<boolean> => {
    const response = await apiClient.put<any>(`/interviews/${id}/feedback`, {
      feedback,
    });
    return response.data.data;
  },

  /**
   * 更新技术问题记录
   */
  updateTechnicalQuestions: async (
    id: number,
    technicalQuestions: string
  ): Promise<boolean> => {
    const response = await apiClient.put<any>(
      `/interviews/${id}/technical-questions`,
      { technicalQuestions }
    );
    return response.data.data;
  },

  /**
   * 取消面试
   */
  cancelInterview: async (id: number): Promise<boolean> => {
    const response = await apiClient.put<any>(`/interviews/${id}/cancel`);
    return response.data.data;
  },

  /**
   * 标记面试为未参加
   */
  markAsNoShow: async (id: number): Promise<boolean> => {
    const response = await apiClient.put<any>(`/interviews/${id}/no-show`);
    return response.data.data;
  },

  /**
   * 设置跟进标记
   */
  setFollowUpRequired: async (
    id: number,
    followUpRequired: boolean
  ): Promise<boolean> => {
    const response = await apiClient.put<any>(`/interviews/${id}/follow-up`, {
      followUpRequired,
    });
    return response.data.data;
  },

  // ========== 日志相关 ==========

  /**
   * 获取所有日志（带申请和公司信息）
   */
  getLogs: async (): Promise<ApplicationLogDTO[]> => {
    const response = await apiClient.get<any>('/logs');
    return response.data.data;
  },

  /**
   * 根据申请ID获取日志（带申请和公司信息）
   */
  getLogsByApplicationId: async (applicationId: number): Promise<ApplicationLogDTO[]> => {
    const response = await apiClient.get<any>(`/applications/${applicationId}/logs`);
    return response.data.data;
  },

  // ========== 统计相关 ==========

  /**
   * 获取统计数据
   */
  getStatistics: async (): Promise<any[]> => {
    const response = await apiClient.get<any>('/statistics');
    return response.data.data;
  },

  // ========== P2阶段：聚合与导出 ==========

  /**
   * 获取申请详情聚合（包含公司、面试、日志、统计）
   */
  getApplicationDetail: async (id: number): Promise<any> => {
    const response = await apiClient.get<any>(`/applications/${id}/detail`);
    return response.data.data;
  },

  /**
   * 导出 Excel 文件
   */
  exportExcel: (): void => {
    window.open(`${apiClient.defaults.baseURL}/export/excel`);
  },

  /**
   * 下载 Excel 文件（自定义处理）
   */
  downloadExcelBlob: async (): Promise<void> => {
    const response = await apiClient.get('/export/excel', {
      responseType: 'blob',
    });

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `求职记录_${new Date().toISOString().split('T')[0]}.xlsx`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  /**
   * 获取 JSON 导出数据
   */
  getJsonExport: async (): Promise<any[]> => {
    const response = await apiClient.get<any>('/export/json');
    return response.data.data;
  },

  /**
   * 下载 JSON 文件
   */
  downloadJsonFile: async (): Promise<void> => {
    const data = await dataApi.getJsonExport();

    const blob = new Blob([JSON.stringify(data, null, 2)], {
      type: 'application/json',
    });

    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `求职记录_${new Date().toISOString().split('T')[0]}.json`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
  },
};

export default dataApi;
