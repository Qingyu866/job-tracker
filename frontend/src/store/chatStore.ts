import { create } from 'zustand';
import { webSocketManager } from '@/lib/webSocketManager';
import { chatApi } from '@/services/chatApi';
import type { ChatSession, ServerChatMessage } from '@/types/chat';

export interface ChatMessage {
  id?: number;
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
}

interface ChatStore {
  messages: ChatMessage[];
  isConnected: boolean;
  isTyping: boolean;
  panelWidth: number;
  isPanelOpen: boolean;

  // 新增：会话管理
  sessions: ChatSession[];
  currentSessionKey: string;
  isLoadingHistory: boolean;

  // 操作
  sendMessage: (content: string) => Promise<void>;
  clearHistory: () => void;
  togglePanel: () => void;
  setPanelWidth: (width: number) => void;
  connect: () => void;
  disconnect: () => void;

  // 新增：会话操作
  loadSessions: () => Promise<void>;
  loadHistory: (sessionKey: string) => Promise<void>;
  switchSession: (sessionKey: string) => Promise<void>;
  deleteSession: (sessionKey: string) => Promise<void>;
  createNewSession: () => void;
}

// 默认会话标识
const DEFAULT_SESSION_KEY = 'default-session';

export const useChatStore = create<ChatStore>((set, get) => ({
  // 初始状态
  messages: [],
  isConnected: false,
  isTyping: false,
  panelWidth: 400,
  isPanelOpen: true,
  sessions: [],
  currentSessionKey: DEFAULT_SESSION_KEY,
  isLoadingHistory: false,

  // 发送消息
  sendMessage: async (content: string) => {
    try {
      // 添加用户消息到列表
      const userMessage: ChatMessage = {
        role: 'user',
        content,
        timestamp: Date.now(),
      };
      set({ messages: [...get().messages, userMessage], isTyping: true });

      // 通过 WebSocket 发送消息，携带 sessionId
      webSocketManager.send(JSON.stringify({
        type: 'CHAT',
        content,
        sessionId: get().currentSessionKey,
      }));
    } catch (error) {
      const message = error instanceof Error ? error.message : '发送消息失败';
      console.error('[ChatStore] 发送消息失败:', error);

      // 添加错误消息
      const errorMessage: ChatMessage = {
        role: 'assistant',
        content: `发送失败: ${message}`,
        timestamp: Date.now(),
      };
      set({ messages: [...get().messages, errorMessage], isTyping: false });
    }
  },

  // 清空历史
  clearHistory: () => {
    set({ messages: [] });
  },

  // 切换面板
  togglePanel: () => {
    set({ isPanelOpen: !get().isPanelOpen });
  },

  // 设置面板宽度
  setPanelWidth: (width) => {
    set({ panelWidth: width });
  },

  // 连接 WebSocket
  connect: () => {
    // 注册消息处理
    webSocketManager.onMessage((data) => {
      console.log('[ChatStore] 收到消息:', data);

      // 添加 AI 回复到列表
      const aiMessage: ChatMessage = {
        role: 'assistant',
        content: data.content || data.message || '抱歉，我没有理解您的意思。',
        timestamp: data.timestamp || Date.now(),
      };

      set({
        messages: [...get().messages, aiMessage],
        isTyping: false,
      });
    });

    // 注册连接成功处理
    webSocketManager.onOpen(() => {
      console.log('[ChatStore] WebSocket 连接成功');
      set({ isConnected: true });
    });

    // 注册错误处理
    webSocketManager.onError((error) => {
      console.error('[ChatStore] WebSocket 错误:', error);
    });

    // 注册关闭处理
    webSocketManager.onClose(() => {
      console.log('[ChatStore] WebSocket 连接关闭');
      set({ isConnected: false });
    });

    // 开始连接
    webSocketManager.connect();
  },

  // 断开连接
  disconnect: () => {
    webSocketManager.disconnect();
    set({ isConnected: false });
  },

  // 新增：加载会话列表
  loadSessions: async () => {
    try {
      const sessions = await chatApi.getSessions();
      set({ sessions });
    } catch (error) {
      console.error('[ChatStore] 加载会话列表失败:', error);
    }
  },

  // 新增：加载历史消息
  loadHistory: async (sessionKey: string) => {
    set({ isLoadingHistory: true });
    try {
      const serverMessages = await chatApi.getSessionMessages(sessionKey);

      // 转换为前端消息格式
      const messages: ChatMessage[] = serverMessages.map((msg) => ({
        id: msg.id,
        role: msg.role.toLowerCase() as 'user' | 'assistant',
        content: msg.content,
        timestamp: new Date(msg.createdAt).getTime(),
      }));

      set({
        messages,
        currentSessionKey: sessionKey,
        isLoadingHistory: false,
      });
    } catch (error) {
      console.error('[ChatStore] 加载历史消息失败:', error);
      set({ isLoadingHistory: false });
    }
  },

  // 新增：切换会话
  switchSession: async (sessionKey: string) => {
    await get().loadHistory(sessionKey);
  },

  // 新增：删除会话
  deleteSession: async (sessionKey: string) => {
    try {
      await chatApi.deleteSession(sessionKey);
      // 刷新会话列表
      await get().loadSessions();

      // 如果删除的是当前会话，切换到默认会话
      if (get().currentSessionKey === sessionKey) {
        set({ messages: [], currentSessionKey: DEFAULT_SESSION_KEY });
      }
    } catch (error) {
      console.error('[ChatStore] 删除会话失败:', error);
    }
  },

  // 新增：创建新会话
  createNewSession: () => {
    const newSessionKey = `session-${Date.now()}`;
    set({
      messages: [],
      currentSessionKey: newSessionKey,
    });
  },
}));
