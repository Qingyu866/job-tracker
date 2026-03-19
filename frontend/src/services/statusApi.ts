import { apiClient } from '@/lib/apiClient';

export interface ApplicationStatusInfo {
  code: string;
  description: string;
  stage: string;
  isTerminal: boolean;
  canScheduleInterview: boolean;
  nextStates: string[];
}

export interface InterviewStatusInfo {
  code: string;
  description: string;
  isTerminal: boolean;
  nextStates: string[];
}

export const statusApi = {
  async getApplicationTransitions(): Promise<Record<string, string[]>> {
    const response = await apiClient.get<Record<string, string[]>>('/status/transitions');
    return response.data;
  },

  async getInterviewTransitions(): Promise<Record<string, string[]>> {
    const response = await apiClient.get<Record<string, string[]>>('/status/interview/transitions');
    return response.data;
  },

  async getNextApplicationStatuses(status: string): Promise<string[]> {
    const response = await apiClient.get<string[]>(`/status/applications/${status}/next`);
    return response.data;
  },

  async getNextInterviewStatuses(status: string): Promise<string[]> {
    const response = await apiClient.get<string[]>(`/status/interview/${status}/next`);
    return response.data;
  },

  async validateApplicationTransition(from: string, to: string): Promise<boolean> {
    const response = await apiClient.get<boolean>('/status/applications/validate', {
      params: { from, to }
    });
    return response.data;
  },

  async validateInterviewTransition(from: string, to: string): Promise<boolean> {
    const response = await apiClient.get<boolean>('/status/interview/validate', {
      params: { from, to }
    });
    return response.data;
  },

  async getAllApplicationStatuses(): Promise<ApplicationStatusInfo[]> {
    const response = await apiClient.get<ApplicationStatusInfo[]>('/status/applications');
    return response.data;
  },

  async getAllInterviewStatuses(): Promise<InterviewStatusInfo[]> {
    const response = await apiClient.get<InterviewStatusInfo[]>('/status/interview');
    return response.data;
  },
};
