import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import type { JobApplication } from '@/types';
import { BoardCard } from './BoardCard';
import { STATUS_CONFIG } from '@/utils/constants';

interface BoardColumnProps {
  status: keyof typeof STATUS_CONFIG;
  applications: JobApplication[];
  isAiPanelOpen?: boolean;
}

export function BoardColumn({ status, applications, isAiPanelOpen = true }: BoardColumnProps) {
  const { setNodeRef } = useDroppable({
    id: status,
  });

  // 根据AI面板状态调整列宽
  const columnWidth = isAiPanelOpen
    ? 'w-48 sm:w-56 md:w-60'  // AI面板打开时：较窄
    : 'w-56 sm:w-64 md:w-72';  // AI面板收起时：较宽

  return (
    <div className={`flex-shrink-0 ${columnWidth} flex flex-col bg-paper-100/50 rounded-lg h-full`}>
      {/* 列标题 */}
      <div className="p-2 md:p-3 border-b border-paper-200 flex-shrink-0">
        <h3 className="font-serif text-paper-700 font-medium text-xs md:text-sm">
          {STATUS_CONFIG[status].label}
        </h3>
        <p className="text-xs text-paper-500 mt-1">
          {applications.length} 个申请
        </p>
      </div>

      {/* 卡片列表 */}
      <div
        ref={setNodeRef}
        className="flex-1 overflow-y-auto p-2 md:p-3 space-y-2 md:space-y-3 min-h-0"
      >
        {applications.length > 0 ? (
          <SortableContext
            items={applications.map(app => app.id)}
            strategy={verticalListSortingStrategy}
          >
            {applications.map((application) => (
              <BoardCard key={application.id} application={application} />
            ))}
          </SortableContext>
        ) : (
          <div className="flex items-center justify-center h-full text-paper-400">
            <div className="text-center">
              <div className="text-2xl mb-1">📭</div>
              <div className="text-xs">暂无申请</div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
