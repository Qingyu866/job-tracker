import axios, { AxiosError, type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';

const TOKEN_KEY = 'satoken';

const apiClient: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api/data',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error('[API] 请求错误:', error);
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError) => {
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

      console.error('[API] HTTP 错误:', status, data);
      const message = (data as any)?.message || `HTTP ${status} 错误`;
      return Promise.reject(new Error(message));
    } else if (error.request) {
      console.error('[API] 网络错误:', error.message);
      return Promise.reject(new Error('网络连接失败，请检查网络设置'));
    } else {
      console.error('[API] 请求配置错误:', error.message);
      return Promise.reject(error);
    }
  }
);

export default apiClient;
