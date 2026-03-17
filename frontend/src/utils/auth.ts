import { authApiClient } from '@/lib/authApiClient';

const TOKEN_KEY = 'satoken';
const USER_INFO_KEY = 'userinfo';

export interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  avatar: string | null;
  email: string | null;
  phone: string | null;
}

export interface LoginResponse {
  token: string;
  tokenName: string;
  userInfo: UserInfo;
}

export interface RegisterRequest {
  username: string;
  password: string;
  nickname?: string;
  email?: string;
  phone?: string;
}

export const authUtil = {
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  },

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  },

  removeToken(): void {
    localStorage.removeItem(TOKEN_KEY);
  },

  isLoggedIn(): boolean {
    return !!this.getToken();
  },

  saveUserInfo(userInfo: UserInfo): void {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo));
  },

  getUserInfo(): UserInfo | null {
    const info = localStorage.getItem(USER_INFO_KEY);
    return info ? JSON.parse(info) : null;
  },

  clearUserInfo(): void {
    localStorage.removeItem(USER_INFO_KEY);
  },

  clearAll(): void {
    this.removeToken();
    this.clearUserInfo();
  },

  async login(username: string, password: string): Promise<LoginResponse> {
    const response = await authApiClient.post<LoginResponse>('/auth/login', {
      username,
      password,
    });

    if (response.success && response.data) {
      this.setToken(response.data.token);
      this.saveUserInfo(response.data.userInfo);
    }

    return response.data;
  },

  async register(data: RegisterRequest): Promise<void> {
    await authApiClient.post('/auth/register', data);
  },

  async logout(): Promise<void> {
    try {
      await authApiClient.post('/auth/logout');
    } finally {
      this.clearAll();
    }
  },

  async changePassword(oldPassword: string, newPassword: string): Promise<void> {
    await authApiClient.post('/auth/change-password', {
      oldPassword,
      newPassword,
    });
  },

  async fetchUserInfo(): Promise<UserInfo> {
    const response = await authApiClient.get<UserInfo>('/auth/info');

    if (response.success && response.data) {
      this.saveUserInfo(response.data);
    }

    return response.data;
  },
};
