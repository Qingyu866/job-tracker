import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import type { ChatMessage as ChatMessageType } from '@/store/chatStore';
import './ChatMessage.css';

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
        {isUser ? (
          // 用户消息：纯文本显示
          <p className="text-sm whitespace-pre-wrap break-words">{message.content}</p>
        ) : (
          // AI 消息：支持 Markdown 渲染
          <div className="text-sm prose prose-sm max-w-none prose-paper">
            <ReactMarkdown
              remarkPlugins={[remarkGfm]}
              components={{
                // 自定义样式以符合牛皮纸主题
                h1: ({ node, ...props }) => <h1 className="text-lg font-bold text-paper-800 mt-2 mb-1" {...props} />,
                h2: ({ node, ...props }) => <h2 className="text-base font-bold text-paper-800 mt-2 mb-1" {...props} />,
                h3: ({ node, ...props }) => <h3 className="text-sm font-bold text-paper-800 mt-2 mb-1" {...props} />,
                p: ({ node, ...props }) => <p className="mb-2 last:mb-0" {...props} />,
                ul: ({ node, ...props }) => <ul className="list-disc list-inside mb-2 space-y-1" {...props} />,
                ol: ({ node, ...props }) => <ol className="list-decimal list-inside mb-2 space-y-1" {...props} />,
                li: ({ node, ...props }) => <li className="text-paper-700" {...props} />,
                code: ({ node, inline, ...props }) =>
                  inline
                    ? <code className="bg-paper-200 text-paper-800 px-1 py-0.5 rounded text-xs font-mono" {...props} />
                    : <code className="block bg-paper-200 text-paper-800 p-2 rounded text-xs font-mono my-2 overflow-x-auto" {...props} />,
                pre: ({ node, ...props }) => <pre className="bg-paper-200 p-3 rounded-lg my-2 overflow-x-auto" {...props} />,
                strong: ({ node, ...props }) => <strong className="font-bold text-paper-800" {...props} />,
                a: ({ node, ...props }) => <a className="text-accent-blue hover:underline" {...props} />,
                blockquote: ({ node, ...props }) => <blockquote className="border-l-4 border-paper-400 pl-3 italic text-paper-600 my-2" {...props} />,
              }}
            >
              {message.content}
            </ReactMarkdown>
          </div>
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
