import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { Inbox, XCircle } from 'lucide-react';
import { cn } from '@/lib/utils';
import type { JobApplication } from '@/types';
import { BoardCard } from './BoardCard';
import { STATUS_CONFIG } from '@/utils/constants';

interface BoardColumnProps {
  status: keyof typeof STATUS_CONFIG;
  applications: JobApplication[];
  isAiPanelOpen?: boolean;
  onCardClick?: (id: number) => void;
  canDrop?: boolean;
  isDragOver?: boolean;
}

export function BoardColumn({
  status,
  applications,
  onCardClick,
  canDrop = true,
  isDragOver = false
}: BoardColumnProps) {
  const { setNodeRef } = useDroppable({
    id: status,
    disabled: !canDrop,
  });

  const showInvalidDrop = isDragOver && !canDrop;
  const showValidDrop = isDragOver && canDrop;

  return (
    <div
      className={cn(
        "flex-1 flex flex-col bg-paper-100/50 rounded-lg h-full min-w-[260px] transition-all relative",
        !canDrop && "opacity-50",
        showInvalidDrop && "ring-2 ring-red-400 bg-red-50",
        showValidDrop && "ring-2 ring-blue-400 bg-blue-50"
      )}
    >
      <div className="p-2 md:p-3 border-b border-paper-200 flex-shrink-0">
        <h3 className="font-serif text-paper-700 font-medium text-xs md:text-sm">
          {STATUS_CONFIG[status].label}
        </h3>
        <p className="text-xs text-paper-500 mt-1">
          {applications.length} 个申请
        </p>
      </div>

      <div
        ref={setNodeRef}
        className={cn(
          "flex-1 overflow-y-auto p-2 md:p-3 space-y-2 md:space-y-3 min-h-0",
          !canDrop && "cursor-not-allowed"
        )}
      >
        {applications.length > 0 ? (
          <SortableContext
            items={applications.map(app => app.id)}
            strategy={verticalListSortingStrategy}
          >
            {applications.map((application) => (
              <BoardCard
                key={application.id}
                application={application}
                onClick={onCardClick}
              />
            ))}
          </SortableContext>
        ) : (
          <div className="flex items-center justify-center h-full text-paper-400">
            <div className="text-center">
              <Inbox className="w-8 h-8 mx-auto mb-1" />
              <div className="text-xs">暂无申请</div>
            </div>
          </div>
        )}
      </div>

      {showInvalidDrop && (
        <div className="absolute inset-0 flex items-center justify-center bg-red-100/80 rounded-lg z-10">
          <div className="text-red-600 text-sm font-medium text-center">
            <XCircle className="w-6 h-6 mx-auto mb-1" />
            不允许转换到此状态
          </div>
        </div>
      )}
    </div>
  );
}
