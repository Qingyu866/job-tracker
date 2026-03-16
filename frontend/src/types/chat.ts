/**
 * 聊天相关类型定义
 */

/**
 * 图片附件
 */
export interface ImageAttachment {
  id: number;
  publicUrl: string;
  mimeType: string;
  fileName?: string;
  fileSize?: number;
  width?: number;
  height?: number;
}

/**
 * 待上传的图片（本地预览）
 */
export interface PendingImage {
  id: string;
  file: File;
  preview: string;
  uploading: boolean;
  error?: string;
  uploadedAttachment?: ImageAttachment;
}

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
  images?: ImageAttachment[];
}

/**
 * 服务端聊天消息（含图片）
 */
export interface ChatMessageWithImages {
  id: number;
  sessionId: number;
  role: 'USER' | 'ASSISTANT' | 'SYSTEM';
  content: string;
  createdAt: string;
  images: ImageAttachment[];
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
  images?: ImageAttachment[];
  pending?: boolean;
}

/**
 * WebSocket 消息格式
 */
export interface WebSocketMessage {
  type: 'CHAT' | 'HEARTBEAT' | 'ERROR';
  content: string;
  sessionId?: string;
  imageIds?: number[];
  timestamp?: number;
}

/**
 * API 响应包装
 */
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
  success: boolean;
}
