import { useEffect, useState } from 'react';
import { Header } from '@/components/layout/Header';
import { ViewToggle } from '@/components/layout/ViewToggle';
import { SplitView } from '@/components/layout/SplitView';
import { TableView } from '@/components/views/TableView';
import { BoardView } from '@/components/views/BoardView';
import { TimelineView } from '@/components/views/TimelineView';
import { CalendarView } from '@/components/views/CalendarView';
import { Modal } from '@/components/common/Modal';
import { CreateApplicationForm } from '@/components/common/CreateApplicationForm';
import { useApplicationStore } from '@/store/applicationStore';

export function WorkspacePage() {
  const { currentView, switchView, fetchApplications } = useApplicationStore();
  const [isModalOpen, setIsModalOpen] = useState(false);

  // 初始化时加载数据
  useEffect(() => {
    fetchApplications();
  }, [fetchApplications]);

  const renderView = () => {
    switch (currentView) {
      case 'table':
        return <TableView />;
      case 'board':
        return <BoardView />;
      case 'timeline':
        return <TimelineView />;
      case 'calendar':
        return <CalendarView />;
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

      {/* 浮动操作按钮 */}
      <button
        onClick={() => setIsModalOpen(true)}
        className="fixed bottom-6 right-6 z-50 bg-accent-amber text-white p-4 rounded-full shadow-paper-lg hover:shadow-xl transition-all hover:scale-110"
      >
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
        </svg>
      </button>

      {/* 创建申请模态框 */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="创建新申请"
      >
        <CreateApplicationForm onClose={() => setIsModalOpen(false)} />
      </Modal>
    </div>
  );
}
