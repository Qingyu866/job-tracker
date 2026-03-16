import { create } from 'zustand';
import { webSocketManager } from '@/lib/webSocketManager';
import { chatApi } from '@/services/chatApi';
import type { ChatSession, ImageAttachment, WebSocketMessage } from '@/types/chat';

export interface ChatMessage {
  id?: number;
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
  images?: ImageAttachment[];
  pending?: boolean;
}

interface ChatStore {
  messages: ChatMessage[];
  isConnected: boolean;
  isTyping: boolean;
  panelWidth: number;
  isPanelOpen: boolean;
  sessions: ChatSession[];
  currentSessionKey: string;
  isLoadingHistory: boolean;
  isInitialized: boolean;
  pendingRefresh: boolean;

  sendMessage: (content: string, images?: ImageAttachment[]) => Promise<void>;
  clearHistory: () => void;
  togglePanel: () => void;
  setPanelWidth: (width: number) => void;
  connect: () => void;
  disconnect: () => void;
  loadSessions: () => Promise<ChatSession[]>;
  loadHistory: (sessionKey: string) => Promise<void>;
  switchSession: (sessionKey: string) => Promise<void>;
  deleteSession: (sessionKey: string) => Promise<void>;
  createNewSession: () => Promise<void>;
  initializeSession: () => Promise<void>;
}

const STORAGE_KEY = 'chat_current_session_key';

function generateUUID(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

function getPersistedSessionKey(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY);
  } catch (error) {
    console.warn('[ChatStore] 无法读取本地存储的会话ID:', error);
    return null;
  }
}

function persistSessionKey(sessionKey: string): void {
  try {
    localStorage.setItem(STORAGE_KEY, sessionKey);
  } catch (error) {
    console.warn('[ChatStore] 无法保存会话ID到本地存储:', error);
  }
}

function clearPersistedSessionKey(): void {
  try {
    localStorage.removeItem(STORAGE_KEY);
  } catch (error) {
    console.warn('[ChatStore] 无法清除本地存储的会话ID:', error);
  }
}

export const useChatStore = create<ChatStore>((set, get) => ({
  messages: [],
  isConnected: false,
  isTyping: false,
  panelWidth: 400,
  isPanelOpen: true,
  sessions: [],
  currentSessionKey: '',
  isLoadingHistory: false,
  isInitialized: false,
  pendingRefresh: false,

  sendMessage: async (content: string, images: ImageAttachment[] = []) => {
    try {
      const hasImages = images.length > 0;
      console.log('[ChatStore] 发送消息，是否有图片:', hasImages, '图片数量:', images.length);
      
      const userMessage: ChatMessage = {
        role: 'user',
        content,
        timestamp: Date.now(),
        images: hasImages ? images : undefined,
      };
      
      set({ 
        messages: [...get().messages, userMessage], 
        isTyping: true, 
        pendingRefresh: hasImages 
      });

      console.log('[ChatStore] 设置 pendingRefresh 为:', hasImages);

      const imageIds = hasImages ? images.map(img => img.id) : undefined;

      const wsMessage: WebSocketMessage = {
        type: 'CHAT',
        content,
        sessionId: get().currentSessionKey,
        imageIds,
      };

      webSocketManager.send(wsMessage);
    } catch (error) {
      const message = error instanceof Error ? error.message : '发送消息失败';
      console.error('[ChatStore] 发送消息失败:', error);

      const errorMessage: ChatMessage = {
        role: 'assistant',
        content: `发送失败: ${message}`,
        timestamp: Date.now(),
      };
      set({ messages: [...get().messages, errorMessage], isTyping: false, pendingRefresh: false });
    }
  },

  clearHistory: () => {
    set({ messages: [] });
  },

  togglePanel: () => {
    set({ isPanelOpen: !get().isPanelOpen });
  },

  setPanelWidth: (width) => {
    set({ panelWidth: width });
  },

  connect: () => {
    webSocketManager.onMessage(async (data) => {
      console.log('[ChatStore] 收到消息:', data);
      console.log('[ChatStore] 当前 pendingRefresh 状态:', get().pendingRefresh);

      const aiMessage: ChatMessage = {
        role: 'assistant',
        content: data.content || data.message || '抱歉，我没有理解您的意思。',
        timestamp: data.timestamp || Date.now(),
      };

      set({
        messages: [...get().messages, aiMessage],
        isTyping: false,
      });

      console.log('[ChatStore] 添加 AI 消息后，pendingRefresh 状态:', get().pendingRefresh);

      if (get().pendingRefresh) {
        console.log('[ChatStore] 检测到待刷新标记，准备重新加载历史消息');
        set({ pendingRefresh: false });
        
        setTimeout(async () => {
          console.log('[ChatStore] 开始重新加载历史消息，sessionKey:', get().currentSessionKey);
          await get().loadHistory(get().currentSessionKey);
          console.log('[ChatStore] 历史消息重新加载完成');
        }, 100);
      }
    });

    webSocketManager.onOpen(() => {
      console.log('[ChatStore] WebSocket 连接成功');
      set({ isConnected: true });
    });

    webSocketManager.onError((error) => {
      console.error('[ChatStore] WebSocket 错误:', error);
    });

    webSocketManager.onClose(() => {
      console.log('[ChatStore] WebSocket 连接关闭');
      set({ isConnected: false });
    });

    webSocketManager.connect();
  },

  disconnect: () => {
    webSocketManager.disconnect();
    set({ isConnected: false });
  },

  loadSessions: async () => {
    try {
      const sessions = await chatApi.getSessions();
      set({ sessions });
      return sessions;
    } catch (error) {
      console.error('[ChatStore] 加载会话列表失败:', error);
      return [];
    }
  },

  loadHistory: async (sessionKey: string) => {
    console.log('[ChatStore] 开始加载历史消息，sessionKey:', sessionKey);
    set({ isLoadingHistory: true });
    try {
      const serverMessages = await chatApi.getSessionMessagesWithImages(sessionKey);
      console.log('[ChatStore] 从服务器获取到消息数量:', serverMessages.length);

      const messages: ChatMessage[] = serverMessages.map((msg) => {
        let content = msg.content;
        
        if (content.startsWith('{') && content.includes('"content"')) {
          try {
            const parsed = JSON.parse(content);
            if (parsed.content && typeof parsed.content === 'string') {
              content = parsed.content;
            }
          } catch {
            // 解析失败，保持原样
          }
        }
        
        return {
          id: msg.id,
          role: msg.role.toLowerCase() as 'user' | 'assistant',
          content,
          timestamp: new Date(msg.createdAt).getTime(),
          images: msg.images && msg.images.length > 0 ? msg.images : undefined,
        };
      });

      console.log('[ChatStore] 转换后的消息数量:', messages.length);

      set({
        messages,
        currentSessionKey: sessionKey,
        isLoadingHistory: false,
      });

      persistSessionKey(sessionKey);
      console.log('[ChatStore] 历史消息加载完成');
    } catch (error) {
      console.error('[ChatStore] 加载历史消息失败:', error);
      set({ isLoadingHistory: false });
    }
  },

  switchSession: async (sessionKey: string) => {
    await get().loadHistory(sessionKey);
  },

  deleteSession: async (sessionKey: string) => {
    try {
      await chatApi.deleteSession(sessionKey);
      await get().loadSessions();

      if (get().currentSessionKey === sessionKey) {
        clearPersistedSessionKey();
        set({ messages: [], currentSessionKey: '' });
      }
    } catch (error) {
      console.error('[ChatStore] 删除会话失败:', error);
    }
  },

  createNewSession: async () => {
    try {
      const newSession = await chatApi.createSession();
      set({
        messages: [],
        currentSessionKey: newSession.sessionKey,
      });
      persistSessionKey(newSession.sessionKey);
      await get().loadSessions();
    } catch (error) {
      console.error('[ChatStore] 创建新会话失败:', error);
      const fallbackKey = generateUUID();
      set({
        messages: [],
        currentSessionKey: fallbackKey,
      });
      persistSessionKey(fallbackKey);
    }
  },

  initializeSession: async () => {
    if (get().isInitialized) {
      return;
    }

    try {
      const sessions = await get().loadSessions();

      const persistedKey = getPersistedSessionKey();

      if (persistedKey) {
        const sessionExists = sessions.some(s => s.sessionKey === persistedKey);
        
        if (sessionExists) {
          console.log('[ChatStore] 恢复持久化的会话:', persistedKey);
          await get().loadHistory(persistedKey);
          set({ isInitialized: true });
          return;
        }
      }

      if (sessions.length > 0) {
        const sortedSessions = [...sessions].sort((a, b) => 
          new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
        );
        
        const mostRecentSession = sortedSessions[0];
        console.log('[ChatStore] 使用最近的会话:', mostRecentSession.sessionKey);
        await get().loadHistory(mostRecentSession.sessionKey);
      } else {
        console.log('[ChatStore] 没有历史会话，创建新会话');
        try {
          const newSession = await chatApi.createSession();
          console.log('[ChatStore] 新会话已创建:', newSession.sessionKey);
          set({ 
            currentSessionKey: newSession.sessionKey,
            isInitialized: true 
          });
          persistSessionKey(newSession.sessionKey);
          await get().loadSessions();
        } catch (createError) {
          console.error('[ChatStore] 创建会话失败:', createError);
          const fallbackKey = generateUUID();
          set({ 
            currentSessionKey: fallbackKey,
            isInitialized: true 
          });
          persistSessionKey(fallbackKey);
        }
      }
    } catch (error) {
      console.error('[ChatStore] 初始化会话失败:', error);
      try {
        console.log('[ChatStore] 尝试创建新会话作为降级方案');
        const newSession = await chatApi.createSession();
        set({ 
          currentSessionKey: newSession.sessionKey,
          isInitialized: true 
        });
        persistSessionKey(newSession.sessionKey);
      } catch (fallbackError) {
        console.error('[ChatStore] 降级创建会话也失败:', fallbackError);
        const emergencyKey = generateUUID();
        set({ 
          currentSessionKey: emergencyKey,
          isInitialized: true 
        });
        persistSessionKey(emergencyKey);
      }
    }
  },
}));
