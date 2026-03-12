import axios, { AxiosError, type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios';
import { API_CONFIG } from '@/utils/constants';

/**
 * 统一的 API 响应格式
 */
export interface Result<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
  success: boolean;
}

/**
 * API 客户端类
 * 封装 axios，提供统一的请求/响应处理
 */
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

  /**
   * 设置请求和响应拦截器
   */
  private setupInterceptors() {
    // 请求拦截器
    this.client.interceptors.request.use(
      (config) => {
        // 可以在这里添加 token 等认证信息
        // const token = localStorage.getItem('token');
        // if (token) {
        //   config.headers.Authorization = `Bearer ${token}`;
        // }
        return config;
      },
      (error) => {
        console.error('[API Client] 请求错误:', error);
        return Promise.reject(error);
      }
    );

    // 响应拦截器
    this.client.interceptors.response.use(
      (response: AxiosResponse<Result>) => {
        const { data } = response;

        // 检查业务状态码
        if (data.success || data.code === 0) {
          return response;
        }

        // 业务错误
        const error = new Error(data.message || '请求失败');
        console.error('[API Client] 业务错误:', data);
        return Promise.reject(error);
      },
      (error: AxiosError<Result>) => {
        // 网络错误或服务器错误
        if (error.response) {
          // 服务器返回错误状态码
          const { status, data } = error.response;
          console.error('[API Client] HTTP 错误:', status, data);

          const message = data?.message || `HTTP ${status} 错误`;
          return Promise.reject(new Error(message));
        } else if (error.request) {
          // 请求已发送但没有收到响应
          console.error('[API Client] 网络错误:', error.message);
          return Promise.reject(new Error('网络连接失败，请检查网络设置'));
        } else {
          // 请求配置错误
          console.error('[API Client] 请求配置错误:', error.message);
          return Promise.reject(error);
        }
      }
    );
  }

  /**
   * GET 请求
   */
  async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<Result<T>> {
    const response = await this.client.get<Result<T>>(url, config);
    return response.data;
  }

  /**
   * POST 请求
   */
  async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
    const response = await this.client.post<Result<T>>(url, data, config);
    return response.data;
  }

  /**
   * PUT 请求
   */
  async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
    const response = await this.client.put<Result<T>>(url, data, config);
    return response.data;
  }

  /**
   * DELETE 请求
   */
  async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<Result<T>> {
    const response = await this.client.delete<Result<T>>(url, config);
    return response.data;
  }
}

// 导出单例
export const apiClient = new ApiClient();
