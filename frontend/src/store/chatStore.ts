import { create } from 'zustand';

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
    // WebSocket 实现在 Task 10
    set({ isTyping: true });

    // TODO: Task 10 将使用 WebSocket 发送
    const userMessage: ChatMessage = {
      role: 'user',
      content,
      timestamp: Date.now(),
    };
    set({ messages: [...get().messages, userMessage] });

    // 模拟 AI 回复（临时）
    setTimeout(() => {
      const aiMessage: ChatMessage = {
        role: 'assistant',
        content: `收到：${content}\n\n（WebSocket 连接将在 Task 10 实现）`,
        timestamp: Date.now(),
      };
      set({ messages: [...get().messages, aiMessage], isTyping: false });
    }, 1000);
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
    // Task 10 实现
    set({ isConnected: true });
  },

  // 断开连接
  disconnect: () => {
    set({ isConnected: false });
  },
}));
