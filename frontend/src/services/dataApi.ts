import apiClient from './api';
import type { JobApplication, Company, InterviewRecord } from '@/types';

/**
 * 数据 API 服务
 * 封装所有后端 REST API 调用
 */
export const dataApi = {
  // ========== 申请相关 ==========

  /**
   * 获取所有申请
   */
  getApplications: async (): Promise<JobApplication[]> => {
    const response = await apiClient.get<any>('/applications');
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

  // ========== 面试相关 ==========

  /**
   * 获取所有面试记录
   */
  getInterviews: async (): Promise<InterviewRecord[]> => {
    const response = await apiClient.get<any>('/interviews');
    return response.data.data;
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

  // ========== 统计相关 ==========

  /**
   * 获取统计数据
   */
  getStatistics: async (): Promise<any[]> => {
    const response = await apiClient.get<any>('/statistics');
    return response.data.data;
  },
};

export default dataApi;
