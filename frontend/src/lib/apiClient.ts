import axios, { AxiosError, type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import { API_CONFIG } from '@/utils/constants';

const TOKEN_KEY = 'authorization';

export interface Result<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
  success: boolean;
}

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_CONFIG.baseURL,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    this.client.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        const token = localStorage.getItem(TOKEN_KEY);
        if (token && config.headers) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        console.error('[API Client] 请求错误:', error);
        return Promise.reject(error);
      }
    );

    this.client.interceptors.response.use(
      (response: AxiosResponse<Result>) => {
        const { data } = response;

        if (data.success || data.code === 0) {
          return response;
        }

        const error = new Error(data.message || '请求失败');
        console.error('[API Client] 业务错误:', data);
        return Promise.reject(error);
      },
      (error: AxiosError<Result>) => {
        if (error.response) {
          const { status, data } = error.response;

          if (status === 401) {
            localStorage.removeItem(TOKEN_KEY);
            localStorage.removeItem('userinfo');

            const currentPath = window.location.pathname;
            if (currentPath !== '/login' && currentPath !== '/register') {
              sessionStorage.setItem('redirect_after_login', currentPath);
              window.location.href = '/login';
            }

            return Promise.reject(new Error('登录已过期，请重新登录'));
          }

          console.error('[API Client] HTTP 错误:', status, data);
          const message = data?.message || `HTTP ${status} 错误`;
          return Promise.reject(new Error(message));
        } else if (error.request) {
          console.error('[API Client] 网络错误:', error.message);
          return Promise.reject(new Error('网络连接失败，请检查网络设置'));
        } else {
          console.error('[API Client] 请求配置错误:', error.message);
          return Promise.reject(error);
        }
      }
    );
  }

  async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<Result<T>> {
    const response = await this.client.get<Result<T>>(url, config);
    return response.data;
  }

  async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
    const response = await this.client.post<Result<T>>(url, data, config);
    return response.data;
  }

  async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
    const response = await this.client.put<Result<T>>(url, data, config);
    return response.data;
  }

  async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<Result<T>> {
    const response = await this.client.delete<Result<T>>(url, config);
    return response.data;
  }
}

export const apiClient = new ApiClient();
