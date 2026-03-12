import { useEffect } from 'react';
import { Header } from '@/components/layout/Header';
import { ViewToggle } from '@/components/layout/ViewToggle';
import { SplitView } from '@/components/layout/SplitView';
import { TableView } from '@/components/views/TableView';
import { useApplicationStore } from '@/store/applicationStore';

export function WorkspacePage() {
  const { currentView, switchView, fetchApplications } = useApplicationStore();

  // 初始化时加载数据
  useEffect(() => {
    fetchApplications();
  }, [fetchApplications]);

  const renderView = () => {
    switch (currentView) {
      case 'table':
        return <TableView />;
      case 'board':
        return (
          <div className="p-4 text-paper-500 flex flex-col items-center justify-center h-full">
            <div className="text-4xl mb-2">📋</div>
            <div>看板视图（Task 7 实现）</div>
          </div>
        );
      case 'timeline':
        return (
          <div className="p-4 text-paper-500 flex flex-col items-center justify-center h-full">
            <div className="text-4xl mb-2">📅</div>
            <div>时间线视图（Task 8 实现）</div>
          </div>
        );
      case 'calendar':
        return (
          <div className="p-4 text-paper-500 flex flex-col items-center justify-center h-full">
            <div className="text-4xl mb-2">🗓️</div>
            <div>日历视图（Task 9 实现）</div>
          </div>
        );
      default:
        return <TableView />;
    }
  };

  return (
    <div className="h-screen flex flex-col">
      <Header />
      <div className="flex-1 flex overflow-hidden">
        <div className="flex-1 flex flex-col">
          <div className="h-14 border-b flex items-center px-6 space-x-4">
            <ViewToggle currentView={currentView} onViewChange={switchView} />
          </div>
          <div className="flex-1 overflow-auto">
            {renderView()}
          </div>
        </div>
        <SplitView>
          <div className="p-4">
            {/* AI 对话面板将在 Task 10 实现 */}
          </div>
        </SplitView>
      </div>
    </div>
  );
}
