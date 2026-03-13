/**
 * 聊天相关类型定义
 */

/**
 * 聊天会话
 */
export interface ChatSession {
  id: number;
  sessionKey: string;
  title: string | null;
  messageCount: number;
  createdAt: string;
  updatedAt: string;
  deleted: number;
}

/**
 * 服务端聊天消息
 */
export interface ServerChatMessage {
  id: number;
  sessionId: number;
  role: 'USER' | 'ASSISTANT' | 'SYSTEM';
  content: string;
  createdAt: string;
}

/**
 * 工具调用记录
 */
export interface ToolCallRecord {
  id: number;
  messageId: number;
  toolName: string;
  toolInput: string;
  toolOutput: string;
  status: 'SUCCESS' | 'FAILURE';
  errorMessage: string | null;
  executionTimeMs: number | null;
  createdAt: string;
}

/**
 * 前端聊天消息
 */
export interface ChatMessage {
  id?: number;
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
}
