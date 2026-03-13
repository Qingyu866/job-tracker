import { Plus, Trash2, MessageSquare } from 'lucide-react';
import { useChatStore } from '@/store/chatStore';
import type { ChatSession } from '@/types/chat';

export function SessionList() {
  const {
    sessions,
    currentSessionKey,
    switchSession,
    deleteSession,
    createNewSession
  } = useChatStore();

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="flex flex-col h-full bg-paper-50">
      {/* 新建会话按钮 */}
      <div className="p-2 border-b border-paper-200">
        <button
          onClick={createNewSession}
          className="w-full flex items-center justify-center gap-2 px-3 py-2 bg-accent-amber text-paper-900 rounded-lg hover:bg-accent-amber/90 transition-colors"
          style={{
            fontFamily: 'Georgia, "Times New Roman", Times, serif',
          }}
        >
          <Plus className="w-4 h-4" />
          <span className="text-sm font-medium">新对话</span>
        </button>
      </div>

      {/* 会话列表 */}
      <div className="flex-1 overflow-y-auto">
        {sessions.length === 0 ? (
          <div
            className="p-4 text-center text-paper-400 text-sm"
            style={{
              fontFamily: 'Georgia, "Times New Roman", Times, serif',
            }}
          >
            暂无历史对话
          </div>
        ) : (
          sessions.map((session) => (
            <SessionItem
              key={session.sessionKey}
              session={session}
              isActive={session.sessionKey === currentSessionKey}
              onSelect={() => switchSession(session.sessionKey)}
              onDelete={() => deleteSession(session.sessionKey)}
              formatDate={formatDate}
            />
          ))
        )}
      </div>
    </div>
  );
}

interface SessionItemProps {
  session: ChatSession;
  isActive: boolean;
  onSelect: () => void;
  onDelete: () => void;
  formatDate: (date: string) => string;
}

function SessionItem({ session, isActive, onSelect, onDelete, formatDate }: SessionItemProps) {
  return (
    <div
      onClick={onSelect}
      className={`group flex items-center gap-2 px-3 py-2 cursor-pointer transition-colors ${
        isActive
          ? 'bg-paper-200 border-l-2 border-accent-amber'
          : 'hover:bg-paper-100'
      }`}
      style={{
        fontFamily: 'Georgia, "Times New Roman", Times, serif',
      }}
    >
      <MessageSquare className="w-4 h-4 text-paper-500 flex-shrink-0" />
      <div className="flex-1 min-w-0">
        <div className="text-sm text-paper-700 truncate">
          {session.title || `对话 ${session.sessionKey.slice(0, 8)}`}
        </div>
        <div className="text-xs text-paper-400">
          {session.messageCount} 条消息 · {formatDate(session.updatedAt)}
        </div>
      </div>
      <button
        onClick={(e) => {
          e.stopPropagation();
          if (window.confirm('确定要删除这个对话吗？')) {
            onDelete();
          }
        }}
        className="opacity-0 group-hover:opacity-100 p-1 hover:bg-paper-200 rounded transition-opacity"
      >
        <Trash2 className="w-4 h-4 text-paper-500" />
      </button>
    </div>
  );
}
