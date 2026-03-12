import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api/data',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
apiClient.interceptors.request.use((config) => {
  // 可以添加 token
  // if (config.headers) {
  //   config.headers.Authorization = `Bearer ${getToken()}`;
  // }
  return config;
});

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    // 统一错误处理
    if (error.response) {
      // 服务器返回错误状态码
      const message = error.response.data?.message || '请求失败';
      return Promise.reject(new Error(message));
    } else if (error.request) {
      // 请求发送但没有收到响应
      return Promise.reject(new Error('网络错误，请检查连接'));
    } else {
      // 其他错误
      return Promise.reject(error);
    }
  }
);

export default apiClient;
