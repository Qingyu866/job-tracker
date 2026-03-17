import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
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
  const location = useLocation();
  const { currentView, switchView, fetchApplications, fetchCompanies, isDetailOpen } = useApplicationStore();
  const shouldHideFloatingButton = 
    location.pathname.startsWith('/applications/') ||
    location.pathname.startsWith('/interviews') ||
    location.pathname.startsWith('/interview/') ||
    location.pathname.startsWith('/companies') ||
    isDetailOpen;
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isAiPanelOpen, setIsAiPanelOpen] = useState(true);

  useEffect(() => {
    fetchApplications();
    fetchCompanies();
  }, [fetchApplications, fetchCompanies]);

  const renderView = () => {
    switch (currentView) {
      case 'table':
        return <TableView />;
      case 'board':
        return <BoardView isAiPanelOpen={isAiPanelOpen} />;
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
      <SplitView onPanelChange={setIsAiPanelOpen}>
        <div className="flex flex-col h-full">
          {/* 工具栏 */}
          <div className="h-12 md:h-14 border-b flex items-center px-3 md:px-6 space-x-3 md:space-x-4 bg-paper-50">
            <ViewToggle currentView={currentView} onViewChange={switchView} />
          </div>
          {/* 内容区域 */}
          <div className="flex-1 overflow-auto">
            {renderView()}
          </div>
        </div>
      </SplitView>
      {/* 浮动操作按钮 - 只在首页显示 */}
      {!shouldHideFloatingButton && (
        <button
          onClick={() => setIsModalOpen(true)}
          className={`fixed bottom-6 z-50 bg-paper-700 text-paper-50 p-3 md:p-4 rounded-full shadow-lg hover:bg-paper-800 hover:shadow-xl transition-all hover:scale-110 border-2 border-paper-600 ${
            isAiPanelOpen ? 'right-24 md:right-96' : 'right-6'
          }`}
          aria-label="创建新申请"
          style={{ transition: 'right 0.3s ease' }}
        >
          <Plus className="w-5 h-5 md:w-6 md:h-6" />
        </button>
      )}

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
