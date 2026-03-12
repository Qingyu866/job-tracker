import { useState } from 'react';
import { MessageCircle, ChevronLeft } from 'lucide-react';
import { ChatPanel } from '@/components/chat/ChatPanel';

interface SplitViewProps {
  children: React.ReactNode;
  onPanelChange?: (isOpen: boolean) => void;
}

export function SplitView({ children, onPanelChange }: SplitViewProps) {
  const [isPanelOpen, setIsPanelOpen] = useState(true);

  // 当面板状态改变时通知父组件
  const handlePanelToggle = (isOpen: boolean) => {
    setIsPanelOpen(isOpen);
    onPanelChange?.(isOpen);
  };

  return (
    <div className="flex flex-1 overflow-hidden relative">
      {/* 左侧内容区 */}
      <div className="flex-1 h-full overflow-auto bg-paper-50 relative">
        {children}

        {/* 桌面端重新打开按钮 - 仅在面板关闭时显示 */}
        {!isPanelOpen && (
          <button
            onClick={() => handlePanelToggle(true)}
            className="hidden md:flex fixed top-1/2 right-4 -translate-y-1/2 z-30 p-3 bg-accent-amber text-paper-900 rounded-l-lg shadow-lg hover:bg-accent-amber/90 transition-colors items-center gap-2"
            aria-label="打开 AI 助手"
          >
            <ChevronLeft className="w-5 h-5" />
            <MessageCircle className="w-5 h-5" />
          </button>
        )}
      </div>

      {/* 分隔条 - 仅桌面端显示 */}
      {isPanelOpen && (
        <div className="hidden md:block w-1 bg-paper-200 hover:bg-accent-amber cursor-col-resize transition-colors" />
      )}

      {/* 右侧 AI 面板 - 桌面端 */}
      {isPanelOpen && (
        <div className="hidden md:block h-full border-l border-paper-200 bg-paper-50 w-96">
          <ChatPanel onClose={() => handlePanelToggle(false)} />
        </div>
      )}

      {/* 移动端 AI 面板 - 浮层 */}
      {isPanelOpen && (
        <div className="md:hidden fixed inset-0 z-50 bg-paper-50">
          <ChatPanel onClose={() => handlePanelToggle(false)} />
        </div>
      )}

      {/* 移动端 AI 按钮 - 仅在面板关闭时显示 */}
      {!isPanelOpen && (
        <button
          onClick={() => handlePanelToggle(true)}
          className="md:hidden fixed bottom-6 right-6 z-40 p-4 bg-accent-amber text-paper-900 rounded-full shadow-lg hover:bg-accent-amber/90 transition-colors"
          aria-label="打开 AI 助手"
        >
          <MessageCircle className="w-6 h-6" />
        </button>
      )}
    </div>
  );
}
