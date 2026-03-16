import apiClient from './api';
import type { ChatSession, ServerChatMessage, ChatMessageWithImages, ToolCallRecord, ImageAttachment, ApiResponse } from '@/types/chat';

const CHAT_BASE_URL = 'http://localhost:8080/api/chat';

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

  async getSessionMessagesWithImages(sessionKey: string): Promise<ChatMessageWithImages[]> {
    const response = await apiClient.get<ApiResponse<ChatMessageWithImages[]>>(
      `${CHAT_BASE_URL}/sessions/${sessionKey}/messages-with-images`
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

  async uploadImage(file: File, sessionKey: string): Promise<ImageAttachment> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('sessionKey', sessionKey);

    const response = await apiClient.post<ApiResponse<ImageAttachment>>(
      `${CHAT_BASE_URL}/upload/image`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data.data;
  },

  getImageUrl(imageId: number, sessionKey: string): string {
    return `${CHAT_BASE_URL}/images/${imageId}?sessionId=${sessionKey}`;
  },
};

export default chatApi;
