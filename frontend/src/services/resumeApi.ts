import axios from 'axios';
import type { UserResume, CreateResumeRequest, UpdateResumeRequest } from '@/types/resume';
import type { ResumeSkill } from '@/types/interview';

const TOKEN_KEY = 'authorization';

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  success: boolean;
}

const resumeClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

resumeClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

resumeClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export const resumeApi = {
  async create(data: CreateResumeRequest): Promise<UserResume> {
    const response = await resumeClient.post<ApiResponse<UserResume>>('/resumes', data);
    return response.data.data;
  },

  async createComplete(data: {
    resume: CreateResumeRequest;
    skills: Array<{
      skillId: number;
      proficiencyLevel: string;
    }>;
    projects: Array<{
      projectName: string;
      role: string;
      description: string;
      techStack: string;
      startDate: string;
      endDate: string;
    }>;
    workExperiences: Array<{
      company: string;
      position: string;
      startDate: string;
      endDate: string;
      description: string;
    }>;
  }): Promise<{
    resume: UserResume;
    skills: any[];
    projects: any[];
    workExperiences: any[];
  }> {
    const response = await resumeClient.post<ApiResponse<any>>('/resumes/complete', data);
    return response.data.data;
  },

  async update(resumeId: number, data: UpdateResumeRequest): Promise<void> {
    await resumeClient.put(`/resumes/${resumeId}`, data);
  },

  async getMyResumes(): Promise<UserResume[]> {
    const response = await resumeClient.get<ApiResponse<UserResume[]>>('/resumes/my');
    return response.data.data;
  },

  async getDetail(resumeId: number): Promise<UserResume> {
    const response = await resumeClient.get<ApiResponse<UserResume>>(`/resumes/${resumeId}`);
    return response.data.data;
  },

  async delete(resumeId: number): Promise<void> {
    await resumeClient.delete(`/resumes/${resumeId}`);
  },

  async setDefault(resumeId: number): Promise<void> {
    await resumeClient.put(`/resumes/${resumeId}/default`);
  },

  async getMyDefaultResume(): Promise<UserResume> {
    const response = await resumeClient.get<ApiResponse<UserResume>>('/resumes/my/default');
    return response.data.data;
  },

  async getSkills(resumeId: number): Promise<ResumeSkill[]> {
    const response = await resumeClient.get<ApiResponse<ResumeSkill[]>>(`/resumes/${resumeId}/skills`);
    return response.data.data;
  },

  async getCompleteResume(resumeId: number): Promise<{
    resume: UserResume;
    skills: any[];
    projects: any[];
    workExperiences: any[];
  }> {
    const response = await resumeClient.get<ApiResponse<any>>(`/resumes/${resumeId}/complete`);
    return response.data.data;
  },
};
