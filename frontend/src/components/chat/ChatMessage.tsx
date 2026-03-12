import type { ChatMessage as ChatMessageType } from '@/store/chatStore';

interface ChatMessageProps {
  message: ChatMessageType;
}

export function ChatMessage({ message }: ChatMessageProps) {
  const isUser = message.role === 'user';

  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'} mb-4`}>
      <div
        className={`max-w-[85%] md:max-w-[80%] rounded-2xl px-3 md:px-4 py-2 ${
          isUser
            ? 'bg-accent-amber text-paper-900'
            : 'bg-paper-100 text-paper-700 border border-paper-200'
        }`}
      >
        <p className="text-sm whitespace-pre-wrap break-words">{message.content}</p>
        <div className={`text-xs mt-1 opacity-70 ${isUser ? 'text-right' : ''}`}>
          {new Date(message.timestamp).toLocaleTimeString('zh-CN', {
            hour: '2-digit',
            minute: '2-digit',
          })}
        </div>
      </div>
    </div>
  );
}
