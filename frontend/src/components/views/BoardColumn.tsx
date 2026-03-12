import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import type { JobApplication } from '@/types';
import { BoardCard } from './BoardCard';
import { STATUS_CONFIG } from '@/utils/constants';

interface BoardColumnProps {
  status: keyof typeof STATUS_CONFIG;
  applications: JobApplication[];
}

export function BoardColumn({ status, applications }: BoardColumnProps) {
  const { setNodeRef } = useDroppable({
    id: status,
  });

  return (
    <div className="flex-shrink-0 w-72 md:w-80 flex flex-col bg-paper-100/50 rounded-lg snap-start">
      {/* 列标题 */}
      <div className="p-3 md:p-4 border-b border-paper-200">
        <h3 className="font-serif text-paper-700 font-medium text-sm">
          {STATUS_CONFIG[status].label}
        </h3>
        <p className="text-xs text-paper-500 mt-1">
          {applications.length} 个申请
        </p>
      </div>

      {/* 卡片列表 */}
      <div
        ref={setNodeRef}
        className="flex-1 overflow-y-auto p-2 md:p-3 space-y-2 md:space-y-3"
      >
        <SortableContext
          items={applications.map(app => app.id)}
          strategy={verticalListSortingStrategy}
        >
          {applications.map((application) => (
            <BoardCard key={application.id} application={application} />
          ))}
        </SortableContext>
      </div>

      {/* 空状态 */}
      {applications.length === 0 && (
        <div className="flex-1 flex items-center justify-center p-6 md:p-8">
          <div className="text-center text-paper-400">
            <div className="text-2xl mb-1">📭</div>
            <div className="text-xs">暂无申请</div>
          </div>
        </div>
      )}
    </div>
  );
}
