import { apiClient, type Result } from '@/lib/apiClient';
import type { UserInfo, LoginResponse, RegisterRequest } from '@/utils/auth';

export interface LoginRequest {
  username: string;
  password: string;
}

export const authApi = {
  async login(data: LoginRequest): Promise<Result<LoginResponse>> {
    return apiClient.post<LoginResponse>('/auth/login', data);
  },

  async register(data: RegisterRequest): Promise<Result<void>> {
    return apiClient.post<void>('/auth/register', data);
  },

  async getCurrentUserInfo(): Promise<Result<UserInfo>> {
    return apiClient.get<UserInfo>('/auth/info');
  },

  async logout(): Promise<Result<void>> {
    return apiClient.post<void>('/auth/logout');
  },

  async changePassword(data: {
    oldPassword: string;
    newPassword: string;
  }): Promise<Result<void>> {
    return apiClient.post<void>('/auth/change-password', data);
  },
};
