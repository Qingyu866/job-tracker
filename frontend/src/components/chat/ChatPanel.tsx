import { useEffect, useRef, useState } from 'react';
import { X, ChevronRight, Bot, History, ChevronLeft } from 'lucide-react';
import { useChatStore } from '@/store/chatStore';
import { ChatMessage } from './ChatMessage';
import { ChatInput } from './ChatInput';
import { SessionList } from './SessionList';
import './ChatPanel.css';

interface ChatPanelProps {
  onClose?: () => void;
}

export function ChatPanel({ onClose }: ChatPanelProps) {
  const {
    messages,
    sendMessage,
    isTyping,
    isConnected,
    isLoadingHistory,
    loadSessions,
    initializeSession,
    isInitialized,
  } = useChatStore();

  const [showSessions, setShowSessions] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    const init = async () => {
      await loadSessions();
      await initializeSession();
    };
    init();
  }, [loadSessions, initializeSession]);

  const handleSend = async (content: string) => {
    await sendMessage(content);
  };

  const isLoading = isLoadingHistory || !isInitialized;

  return (
    <div className="flex h-full bg-paper-50">
      {showSessions && (
        <div className="w-48 border-r border-paper-200 flex-shrink-0 bg-paper-100">
          <SessionList />
        </div>
      )}

      <div className="flex flex-col flex-1 min-w-0">
        <div className="p-4 border-b border-paper-200">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <button
                onClick={() => setShowSessions(!showSessions)}
                className="p-1.5 hover:bg-paper-200 rounded-lg text-paper-600 transition-colors"
                title={showSessions ? '隐藏会话列表' : '显示会话列表'}
              >
                {showSessions ? (
                  <ChevronLeft className="w-5 h-5" />
                ) : (
                  <History className="w-5 h-5" />
                )}
              </button>
              <Bot className="w-5 h-5 text-paper-600" />
              <h3 className="font-serif text-paper-700">AI 助手</h3>
            </div>
            <div className="flex items-center space-x-2">
              <div
                className={`w-2 h-2 rounded-full ${
                  isConnected ? 'bg-accent-green' : 'bg-accent-red'
                }`}
              />
              <span className="text-xs text-paper-500 hidden sm:inline">
                {isConnected ? '已连接' : '未连接'}
              </span>
              {onClose && (
                <button
                  onClick={onClose}
                  className="hidden md:block p-1.5 hover:bg-paper-200 rounded-lg text-paper-600 transition-colors"
                  aria-label="收起面板"
                  title="收起面板"
                >
                  <ChevronRight className="w-5 h-5" />
                </button>
              )}
              {onClose && (
                <button
                  onClick={onClose}
                  className="md:hidden p-1 hover:bg-paper-200 rounded-lg text-paper-600 transition-colors"
                  aria-label="关闭"
                >
                  <X className="w-5 h-5" />
                </button>
              )}
            </div>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto p-4">
          {isLoading ? (
            <div className="flex items-center justify-center h-full text-paper-400">
              <div className="text-sm">加载中...</div>
            </div>
          ) : messages.length === 0 ? (
            <div className="flex items-center justify-center h-full text-paper-400">
              <div className="text-center">
                <Bot className="w-16 h-16 mx-auto mb-2" />
                <div className="text-sm">你好！我是 AI 助手</div>
                <div className="text-xs mt-1">有什么可以帮你的吗？</div>
              </div>
            </div>
          ) : (
            <>
              {messages.map((msg, index) => (
                <ChatMessage key={msg.timestamp || index} message={msg} />
              ))}
              {isTyping && (
                <div className="flex justify-start mb-4">
                  <div className="bg-paper-100 border border-paper-200 rounded-2xl px-4 py-3">
                    <div className="text-paper-600 font-mono text-sm">
                      <span className="typing-indicator">AI 正在输入</span>
                    </div>
                  </div>
                </div>
              )}
              <div ref={messagesEndRef} />
            </>
          )}
        </div>

        <div className="p-4 border-t border-paper-200">
          <ChatInput onSend={handleSend} disabled={!isConnected} />
        </div>
      </div>
    </div>
  );
}
