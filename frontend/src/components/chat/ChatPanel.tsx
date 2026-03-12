import { useEffect, useRef } from 'react';
import { useChatStore } from '@/store/chatStore';
import { ChatMessage } from './ChatMessage';
import { ChatInput } from './ChatInput';

export function ChatPanel() {
  const { messages, sendMessage, isTyping, isConnected } = useChatStore();
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = async (content: string) => {
    await sendMessage(content);
  };

  return (
    <div className="flex flex-col h-full bg-paper-50">
      {/* 头部 */}
      <div className="p-4 border-b border-paper-200">
        <div className="flex items-center justify-between">
          <h3 className="font-serif text-paper-700">🤖 AI 助手</h3>
          <div className="flex items-center space-x-2">
            <div
              className={`w-2 h-2 rounded-full ${
                isConnected ? 'bg-accent-green' : 'bg-accent-red'
              }`}
            />
            <span className="text-xs text-paper-500">
              {isConnected ? '已连接' : '未连接'}
            </span>
          </div>
        </div>
      </div>

      {/* 消息列表 */}
      <div className="flex-1 overflow-y-auto p-4">
        {messages.length === 0 ? (
          <div className="flex items-center justify-center h-full text-paper-400">
            <div className="text-center">
              <div className="text-4xl mb-2">🤖</div>
              <div className="text-sm">你好！我是 AI 助手</div>
              <div className="text-xs mt-1">有什么可以帮你的吗？</div>
            </div>
          </div>
        ) : (
          <>
            {messages.map((msg) => (
              <ChatMessage key={msg.timestamp} message={msg} />
            ))}
            {isTyping && (
              <div className="flex justify-start mb-4">
                <div className="bg-paper-100 border border-paper-200 rounded-2xl px-4 py-2">
                  <div className="flex space-x-1">
                    <div className="w-2 h-2 bg-paper-400 rounded-full animate-bounce" />
                    <div className="w-2 h-2 bg-paper-400 rounded-full animate-bounce delay-100" />
                    <div className="w-2 h-2 bg-paper-400 rounded-full animate-bounce delay-200" />
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </>
        )}
      </div>

      {/* 输入框 */}
      <div className="p-4 border-t border-paper-200">
        <ChatInput onSend={handleSend} disabled={!isConnected} />
      </div>
    </div>
  );
}
