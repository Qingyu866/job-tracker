import { useState } from 'react';

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
          {/* ChatPanel 内容将在 Task 10 实现 */}
          <div className="p-4 h-full flex flex-col">
            <div className="flex-1 flex items-center justify-center text-paper-400">
              <div className="text-center">
                <div className="text-4xl mb-2">🤖</div>
                <div className="text-sm">AI 对话面板</div>
                <div className="text-xs mt-1">（Task 10 实现）</div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
