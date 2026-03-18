import { useRef, useEffect, useState, type KeyboardEvent } from 'react';
import { clsx } from 'clsx';
import { Send } from 'lucide-react';
import { Button, Spinner } from '@/components/common';
import type { InterviewMessage, MockInterviewSession } from '@/types/interview';
import { normalizeState } from '@/types/interview';

export interface InterviewChatProps {
  session: MockInterviewSession | null;
  messages: InterviewMessage[];
  onSendMessage: (content: string) => Promise<void>;
  disabled?: boolean;
  loading?: boolean;
}

export function InterviewChat({
  session,
  messages,
  onSendMessage,
  disabled = false,
  loading = false,
}: InterviewChatProps) {
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSubmit = async () => {
    const content = input.trim();
    if (!content || disabled || sending) return;

    setSending(true);
    setInput('');
    
    try {
      await onSendMessage(content);
    } finally {
      setSending(false);
    }
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  };

  const handleInput = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setInput(e.target.value);
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = `${textareaRef.current.scrollHeight}px`;
    }
  };

  const isFinished = normalizeState(session?.state || '') === 'FINISHED';
  const isDisabled = disabled || sending || isFinished;
  const isWelcome = normalizeState(session?.state || '') === 'IDLE' && messages.length === 0;

  return (
    <div className="flex flex-col h-full bg-white rounded-xl overflow-hidden shadow-paper">
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {loading ? (
          <div className="flex items-center justify-center h-full">
            <Spinner size="lg" />
          </div>
        ) : messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-paper-400">
            {isWelcome ? (
              <>
                <p>欢迎来到 {session?.companyName} 的面试</p>
                <p className="text-sm mt-2">面试官将根据你的简历内容进行提问</p>
              </>
            ) : (
              <>
                <p>面试即将开始...</p>
                <p className="text-sm mt-2">请准备好回答面试官的问题</p>
              </>
            )}
          </div>
        ) : (
          messages.map((message) => (
            <ChatMessageItem key={message.id} message={message} />
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      <div className="border-t border-paper-200 p-4 bg-paper-50">
        <div className="flex gap-3">
          <textarea
            ref={textareaRef}
            value={input}
            onChange={handleInput}
            onKeyDown={handleKeyDown}
            placeholder={isFinished ? '面试已结束' : '输入你的回答...（Shift+Enter 换行）'}
            disabled={isDisabled}
            rows={1}
            className={clsx(
              'flex-1 px-4 py-3 rounded-lg border border-paper-200',
              'bg-white text-paper-700 placeholder-paper-400',
              'focus:outline-none focus:ring-2 focus:ring-accent-amber focus:border-transparent',
              'resize-none overflow-hidden min-h-[48px] max-h-32',
              'disabled:opacity-50 disabled:cursor-not-allowed'
            )}
          />
          <Button
            onClick={handleSubmit}
            disabled={!input.trim() || isDisabled}
            loading={sending}
            className="self-end"
          >
            <Send className="w-5 h-5" />
          </Button>
        </div>
      </div>
    </div>
  );
}

function ChatMessageItem({ message }: { message: InterviewMessage }) {
  const isUser = message.role === 'CANDIDATE';
  const isSystem = message.role === 'system';

  if (isSystem) {
    return (
      <div className="flex justify-center">
        <div className="px-4 py-2 bg-paper-100 rounded-full text-paper-500 text-sm">
          {message.content}
        </div>
      </div>
    );
  }

  return (
    <div className={clsx('flex gap-3', isUser && 'flex-row-reverse')}>
      <div
        className={clsx(
          'rounded-full flex items-center justify-center text-white font-medium w-8 h-8 text-sm flex-shrink-0'
        )}
        style={{ backgroundColor: isUser ? '#d4a574' : '#e53d3d' }}
      >
        {isUser ? '你' : '面'}
      </div>
      <div
        className={clsx(
          'max-w-[75%] rounded-xl px-4 py-3',
          isUser
            ? 'text-paper-800'
            : 'bg-paper-100 text-paper-700'
        )}
        style={isUser ? { backgroundColor: '#d4a574' } : undefined}
      >
        <p className="whitespace-pre-wrap break-words">{message.content}</p>
        {message.skillTag && (
          <div className="mt-2 pt-2 border-t border-paper-200/50">
            <span className="text-xs text-paper-500">考察技能: {message.skillTag}</span>
          </div>
        )}
      </div>
    </div>
  );
}
