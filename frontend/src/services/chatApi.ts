import apiClient from './api';
import type { ChatSession, ServerChatMessage, ToolCallRecord, ApiResponse } from '@/types/chat';

const CHAT_BASE_URL = 'http://localhost:8080/api/chat';

/**
 * 聊天历史 API 服务
 */
export const chatApi = {
  /**
   * 获取所有会话列表
   */
  async getSessions(): Promise<ChatSession[]> {
    const response = await apiClient.get<ApiResponse<ChatSession[]>>(
      `${CHAT_BASE_URL}/sessions`
    );
    return response.data.data;
  },

  /**
   * 获取会话消息历史
   */
  async getSessionMessages(sessionKey: string): Promise<ServerChatMessage[]> {
    const response = await apiClient.get<ApiResponse<ServerChatMessage[]>>(
      `${CHAT_BASE_URL}/sessions/${sessionKey}/messages`
    );
    return response.data.data;
  },

  /**
   * 删除会话
   */
  async deleteSession(sessionKey: string): Promise<boolean> {
    const response = await apiClient.delete<ApiResponse<boolean>>(
      `${CHAT_BASE_URL}/sessions/${sessionKey}`
    );
    return response.data.data;
  },

  /**
   * 获取消息的工具调用记录
   */
  async getToolCallRecords(messageId: number): Promise<ToolCallRecord[]> {
    const response = await apiClient.get<ApiResponse<ToolCallRecord[]>>(
      `${CHAT_BASE_URL}/messages/${messageId}/tool-calls`
    );
    return response.data.data;
  },
};

export default chatApi;
