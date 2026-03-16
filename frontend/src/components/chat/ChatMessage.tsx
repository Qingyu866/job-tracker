import ReactMarkdown from 'react-markdown';
import type { Components } from 'react-markdown';
import remarkGfm from 'remark-gfm';
import type { ChatMessage as ChatMessageType } from '@/types/chat';
import { ChatMessageImageGrid } from './ChatMessageImage';
import './ChatMessage.css';

interface ChatMessageProps {
  message: ChatMessageType;
}

const markdownComponents: Components = {
  h1: ({ children }) => <h1 className="text-lg font-bold text-paper-800 mt-2 mb-1">{children}</h1>,
  h2: ({ children }) => <h2 className="text-base font-bold text-paper-800 mt-2 mb-1">{children}</h2>,
  h3: ({ children }) => <h3 className="text-sm font-bold text-paper-800 mt-2 mb-1">{children}</h3>,
  p: ({ children }) => <p className="mb-2 last:mb-0">{children}</p>,
  ul: ({ children }) => <ul className="list-disc list-inside mb-2 space-y-1">{children}</ul>,
  ol: ({ children }) => <ol className="list-decimal list-inside mb-2 space-y-1">{children}</ol>,
  li: ({ children }) => <li className="text-paper-700">{children}</li>,
  code: ({ className, children, ...props }) => {
    const isInline = !className;
    return isInline ? (
      <code className="bg-paper-200 text-paper-800 px-1 py-0.5 rounded text-xs font-mono" {...props}>{children}</code>
    ) : (
      <code className="block bg-paper-200 text-paper-800 p-2 rounded text-xs font-mono my-2 overflow-x-auto" {...props}>{children}</code>
    );
  },
  pre: ({ children }) => <pre className="bg-paper-200 p-3 rounded-lg my-2 overflow-x-auto">{children}</pre>,
  strong: ({ children }) => <strong className="font-bold text-paper-800">{children}</strong>,
  a: ({ href, children }) => <a href={href} className="text-accent-blue hover:underline">{children}</a>,
  blockquote: ({ children }) => <blockquote className="border-l-4 border-paper-400 pl-3 italic text-paper-600 my-2">{children}</blockquote>,
};

export function ChatMessage({ message }: ChatMessageProps) {
  const isUser = message.role === 'user';
  const hasImages = message.images && message.images.length > 0;
  const hasContent = message.content && message.content.trim().length > 0;

  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'} mb-4`}>
      <div
        className={`max-w-[85%] md:max-w-[80%] rounded-2xl px-3 md:px-4 py-2 ${
          isUser
            ? 'bg-accent-amber text-paper-900 border-2 border-amber-500'
            : 'bg-paper-100 text-paper-700 border border-paper-200'
        }`}
      >
        {hasImages && isUser && (
          <ChatMessageImageGrid images={message.images!} />
        )}
        {hasContent ? (
          isUser ? (
            <p className="text-sm whitespace-pre-wrap break-words">{message.content}</p>
          ) : (
            <div className="text-sm prose prose-sm max-w-none prose-paper">
              <ReactMarkdown
                remarkPlugins={[remarkGfm]}
                components={markdownComponents}
              >
                {message.content}
              </ReactMarkdown>
            </div>
          )
        ) : hasImages ? null : (
          <p className="text-sm text-paper-400 italic">空消息</p>
        )}
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
