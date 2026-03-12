import { useEffect, useState } from 'react';
import { Plus } from 'lucide-react';
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
          {/* 工具栏 */}
          <div className="h-12 md:h-14 border-b flex items-center px-3 md:px-6 space-x-3 md:space-x-4">
            <ViewToggle currentView={currentView} onViewChange={switchView} />
          </div>
          {/* 内容区域 */}
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
        className="fixed bottom-6 right-6 z-50 bg-accent-amber text-white p-3 md:p-4 rounded-full shadow-paper-lg hover:shadow-xl transition-all hover:scale-110"
        aria-label="创建新申请"
      >
        <Plus className="w-5 h-5 md:w-6 md:h-6" />
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
