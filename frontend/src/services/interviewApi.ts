import { apiClient } from '@/lib/apiClient';
import type {
  MockInterviewSession,
  InterviewMessage,
  MockInterviewEvaluation,
  InterviewReport,
  UserResume,
  ResumeProject,
  ResumeSkill,
} from '@/types/interview';

export const interviewApi = {
  async startInterview(applicationId: number, userId?: number): Promise<MockInterviewSession> {
    const response = await apiClient.post<MockInterviewSession>('/mock-interview/start', {
      applicationId,
      userId,
    });
    return response.data;
  },

  async getSession(sessionId: string): Promise<MockInterviewSession> {
    const response = await apiClient.get<MockInterviewSession>(`/mock-interview/sessions/${sessionId}`);
    return response.data;
  },

  async sendMessage(sessionId: string, content: string, roundNumber: number): Promise<InterviewMessage> {
    const response = await apiClient.post<InterviewMessage>(
      `/mock-interview/sessions/${sessionId}/message`,
      { content, roundNumber }
    );
    return response.data;
  },

  async getMessages(sessionId: string): Promise<InterviewMessage[]> {
    const response = await apiClient.get<InterviewMessage[]>(
      `/mock-interview/sessions/${sessionId}/messages`
    );
    return response.data;
  },

  async finishInterview(sessionId: string): Promise<MockInterviewSession> {
    const response = await apiClient.post<MockInterviewSession>(
      `/mock-interview/sessions/${sessionId}/finish`
    );
    return response.data;
  },

  async getEvaluations(sessionId: string): Promise<MockInterviewEvaluation[]> {
    const response = await apiClient.get<MockInterviewEvaluation[]>(
      `/mock-interview/sessions/${sessionId}/evaluations`
    );
    return response.data;
  },

  async getReport(sessionId: string): Promise<InterviewReport> {
    const response = await apiClient.get<InterviewReport>(
      `/mock-interview/sessions/${sessionId}/report`
    );
    return response.data;
  },

  async getUserSessions(userId: number): Promise<MockInterviewSession[]> {
    const response = await apiClient.get<MockInterviewSession[]>(
      `/mock-interview/sessions/user/${userId}`
    );
    return response.data;
  },
};

export const resumeApi = {
  async createResume(resume: Partial<UserResume>): Promise<UserResume> {
    const response = await apiClient.post<UserResume>('/resumes', resume);
    return response.data;
  },

  async updateResume(resumeId: number, resume: Partial<UserResume>): Promise<void> {
    await apiClient.put(`/resumes/${resumeId}`, resume);
  },

  async getUserResumes(userId: number): Promise<UserResume[]> {
    const response = await apiClient.get<UserResume[]>(`/resumes/user/${userId}`);
    return response.data;
  },

  async getDefaultResume(userId: number): Promise<UserResume> {
    const response = await apiClient.get<UserResume>(`/resumes/user/${userId}/default`);
    return response.data;
  },

  async setDefaultResume(resumeId: number, userId: number): Promise<void> {
    await apiClient.put(`/resumes/${resumeId}/default`, null, {
      params: { userId }
    });
  },

  async deleteResume(resumeId: number): Promise<void> {
    await apiClient.delete(`/resumes/${resumeId}`);
  },

  async getProjects(resumeId: number): Promise<ResumeProject[]> {
    const response = await apiClient.get<ResumeProject[]>(`/resumes/${resumeId}/projects`);
    return response.data;
  },

  async getSkills(resumeId: number): Promise<ResumeSkill[]> {
    const response = await apiClient.get<ResumeSkill[]>(`/resumes/${resumeId}/skills`);
    return response.data;
  },
};
