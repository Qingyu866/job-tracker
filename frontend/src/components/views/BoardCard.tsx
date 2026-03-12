import { useSortable } from '@dnd-kit/sortable';
import { MapPin } from 'lucide-react';
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
        paper-card p-2 md:p-3 rounded-lg cursor-grab active:cursor-grabbing
        hover:shadow-paper-md transition-all
        ${isDragging ? 'opacity-50 rotate-2' : ''}
      `}
      {...attributes}
      {...listeners}
    >
      {/* 公司名称首字母 */}
      <div className="flex items-start justify-between mb-1.5 md:mb-2">
        <div className="w-7 h-7 md:w-8 md:h-8 rounded-full bg-paper-200 flex items-center justify-center text-paper-700 font-semibold text-xs flex-shrink-0">
          {initials}
        </div>
        {application.priority !== undefined && application.priority > 0 && (
          <div className="flex">
            {[1, 2, 3].map((level) => (
              <span
                key={level}
                className={`mx-0.5 text-xs ${
                  (application.priority || 0) >= level ? 'text-amber-600' : 'text-paper-300'
                }`}
              >
                ★
              </span>
            ))}
          </div>
        )}
      </div>

      {/* 职位信息 */}
      <h4 className="font-serif text-paper-700 font-medium mb-1 text-xs md:text-sm leading-tight line-clamp-2">
        {application.jobTitle}
      </h4>
      <p className="text-paper-500 text-xs mb-2 truncate">{application.company?.name}</p>

      {/* 标签 */}
      <div className="flex flex-wrap gap-1 mb-1.5">
        {application.jobType && (
          <span className="px-1.5 py-0.5 text-xs bg-paper-100 text-paper-600 rounded truncate max-w-full">
            {application.jobType}
          </span>
        )}
        {application.workLocation && (
          <span className="px-1.5 py-0.5 text-xs bg-paper-100 text-paper-600 rounded truncate max-w-full flex items-center gap-1">
            <MapPin className="w-3 h-3 flex-shrink-0" />
            {application.workLocation}
          </span>
        )}
      </div>

      {/* 申请日期 */}
      {application.applicationDate && (
        <div className="text-xs text-paper-400 mt-1.5">
          {application.applicationDate}
        </div>
      )}
    </div>
  );
}
