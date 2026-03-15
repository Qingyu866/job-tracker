import apiClient from './api';
import type { ChatSession, ServerChatMessage, ToolCallRecord } from '@/types/chat';

const CHAT_BASE_URL = 'http://localhost:8080/api/chat';

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
  success: boolean;
}

export const chatApi = {
  async createSession(title?: string): Promise<ChatSession> {
    const response = await apiClient.post<ApiResponse<ChatSession>>(
      `${CHAT_BASE_URL}/sessions`,
      { title: title || '新会话' }
    );
    return response.data.data;
  },

  async getSessions(): Promise<ChatSession[]> {
    const response = await apiClient.get<ApiResponse<ChatSession[]>>(
      `${CHAT_BASE_URL}/sessions`
    );
    return response.data.data;
  },

  async getSessionMessages(sessionKey: string): Promise<ServerChatMessage[]> {
    const response = await apiClient.get<ApiResponse<ServerChatMessage[]>>(
      `${CHAT_BASE_URL}/sessions/${sessionKey}/messages`
    );
    return response.data.data;
  },

  async deleteSession(sessionKey: string): Promise<boolean> {
    const response = await apiClient.delete<ApiResponse<boolean>>(
      `${CHAT_BASE_URL}/sessions/${sessionKey}`
    );
    return response.data.data;
  },

  async getToolCallRecords(messageId: number): Promise<ToolCallRecord[]> {
    const response = await apiClient.get<ApiResponse<ToolCallRecord[]>>(
      `${CHAT_BASE_URL}/messages/${messageId}/tool-calls`
    );
    return response.data.data;
  },
};

export default chatApi;
