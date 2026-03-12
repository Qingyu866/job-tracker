import { useState } from 'react';
import { MessageCircle } from 'lucide-react';
import { ChatPanel } from '@/components/chat/ChatPanel';

interface SplitViewProps {
  children: React.ReactNode;
}

export function SplitView({ children }: SplitViewProps) {
  const [isPanelOpen, setIsPanelOpen] = useState(true);

  return (
    <div className="flex h-screen overflow-hidden relative">
      {/* 左侧内容区 */}
      <div className="flex-1 h-full overflow-auto bg-paper-50">
        {children}
      </div>

      {/* 分隔条 - 仅桌面端显示 */}
      {isPanelOpen && (
        <div className="hidden md:block w-1 bg-paper-200 hover:bg-accent-amber cursor-col-resize transition-colors" />
      )}

      {/* 右侧 AI 面板 - 桌面端 */}
      {isPanelOpen && (
        <div className="hidden md:block h-full border-l border-paper-200 bg-paper-50 w-96">
          <ChatPanel onClose={() => setIsPanelOpen(false)} />
        </div>
      )}

      {/* 移动端 AI 面板 - 浮层 */}
      {isPanelOpen && (
        <div className="md:hidden fixed inset-0 z-50 bg-paper-50">
          <ChatPanel onClose={() => setIsPanelOpen(false)} />
        </div>
      )}

      {/* 移动端 AI 按钮 - 仅在面板关闭时显示 */}
      {!isPanelOpen && (
        <button
          onClick={() => setIsPanelOpen(true)}
          className="fixed bottom-6 right-6 z-40 p-4 bg-accent-amber text-white rounded-full shadow-lg hover:bg-accent-amber/90 transition-colors md:hidden"
          aria-label="打开 AI 助手"
        >
          <MessageCircle className="w-6 h-6" />
        </button>
      )}
    </div>
  );
}
