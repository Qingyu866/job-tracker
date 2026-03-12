import { create } from 'zustand';
import { webSocketManager } from '@/lib/webSocketManager';

export interface ChatMessage {
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

  // 操作
  sendMessage: (content: string) => Promise<void>;
  clearHistory: () => void;
  togglePanel: () => void;
  setPanelWidth: (width: number) => void;
  connect: () => void;
  disconnect: () => void;
}

export const useChatStore = create<ChatStore>((set, get) => ({
  // 初始状态
  messages: [],
  isConnected: false,
  isTyping: false,
  panelWidth: 400,
  isPanelOpen: true,

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

      // 通过 WebSocket 发送消息
      webSocketManager.send(content);
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
}));
