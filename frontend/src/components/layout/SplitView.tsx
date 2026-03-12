import { useState } from 'react';
import { ChatPanel } from '@/components/chat/ChatPanel';

interface SplitViewProps {
  children: React.ReactNode;
}

export function SplitView({ children }: SplitViewProps) {
  const [panelWidth] = useState(400);
  const [isPanelOpen] = useState(true);

  return (
    <div className="flex h-screen overflow-hidden">
      {/* 左侧内容区 */}
      <div
        className="flex-1 h-full overflow-auto bg-paper-50"
        style={{ width: isPanelOpen ? `calc(100% - ${panelWidth}px)` : '100%' }}
      >
        {children}
      </div>

      {/* 分隔条 */}
      {isPanelOpen && (
        <div className="w-1 bg-paper-200 hover:bg-accent-amber cursor-col-resize transition-colors" />
      )}

      {/* 右侧 AI 面板 */}
      {isPanelOpen && (
        <div
          className="h-full border-l border-paper-200 bg-paper-50"
          style={{ width: `${panelWidth}px` }}
        >
          <ChatPanel />
        </div>
      )}
    </div>
  );
}
