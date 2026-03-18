import axios from 'axios';
import type { UserResume, CreateResumeRequest, UpdateResumeRequest } from '@/types/resume';

const TOKEN_KEY = 'satoken';

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

  async update(resumeId: number, data: UpdateResumeRequest): Promise<void> {
    await resumeClient.put(`/resumes/${resumeId}`, data);
  },

  async getList(userId: number): Promise<UserResume[]> {
    const response = await resumeClient.get<ApiResponse<UserResume[]>>(`/resumes/user/${userId}`);
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

  async getDefault(userId: number): Promise<UserResume> {
    const response = await resumeClient.get<ApiResponse<UserResume>>(`/resumes/user/${userId}/default`);
    return response.data.data;
  },
};
