import { useSortable } from '@dnd-kit/sortable';
import type { JobApplication } from '@/types';
import { CSS } from '@dnd-kit/utilities';

interface BoardCardProps {
  application: JobApplication;
  onEdit?: (id: number) => void;
  onDelete?: (id: number) => void;
}

export function BoardCard({ application }: BoardCardProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: application.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  // 获取公司首字母
  const initials = application.company?.name
    ?.split(' ')
    .map(word => word[0])
    .join('')
    .toUpperCase()
    .slice(0, 2) || '??';

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`
        paper-card p-3 md:p-4 rounded-lg cursor-grab active:cursor-grabbing
        hover:shadow-paper-md transition-all
        ${isDragging ? 'opacity-50 rotate-2' : ''}
      `}
      {...attributes}
      {...listeners}
    >
      {/* 公司名称首字母 */}
      <div className="flex items-start justify-between mb-2 md:mb-3">
        <div className="w-8 h-8 md:w-10 md:h-10 rounded-full bg-paper-200 flex items-center justify-center text-paper-700 font-semibold text-xs md:text-sm flex-shrink-0">
          {initials}
        </div>
        {application.priority !== undefined && application.priority > 0 && (
          <div className="flex">
            {[1, 2, 3].map((level) => (
              <span
                key={level}
                className={`mx-0.5 text-xs ${
                  (application.priority || 0) >= level ? 'text-accent-amber' : 'text-paper-200'
                }`}
              >
                ★
              </span>
            ))}
          </div>
        )}
      </div>

      {/* 职位信息 */}
      <h4 className="font-serif text-paper-700 font-medium mb-1 text-sm leading-tight">
        {application.jobTitle}
      </h4>
      <p className="text-paper-500 text-xs mb-2">{application.company?.name}</p>

      {/* 标签 */}
      <div className="flex flex-wrap gap-1 mb-2">
        {application.jobType && (
          <span className="px-2 py-0.5 text-xs bg-paper-100 text-paper-600 rounded">
            {application.jobType}
          </span>
        )}
        {application.workLocation && (
          <span className="px-2 py-0.5 text-xs bg-paper-100 text-paper-600 rounded truncate">
            📍 {application.workLocation}
          </span>
        )}
      </div>

      {/* 申请日期 */}
      {application.applicationDate && (
        <div className="text-xs text-paper-400 mt-2">
          {application.applicationDate}
        </div>
      )}
    </div>
  );
}
