import type { JobApplication } from '@/types';
import { STATUS_CONFIG } from '@/utils/constants';

interface TimelineItemProps {
  application: JobApplication;
  showLine?: boolean;
}

// 状态图标映射
const STATUS_ICONS: Record<string, string> = {
  WISHLIST: '📝',
  APPLIED: '✅',
  INTERVIEW: '💼',
  OFFER: '🎉',
  REJECTED: '❌',
  WITHDRAWN: '🔙',
};

export function TimelineItem({ application, showLine = true }: TimelineItemProps) {
  const statusIcon = STATUS_ICONS[application.status] || '📄';

  return (
    <div className="relative pl-8 pb-8 last:pb-0">
      {/* 时间线 */}
      {showLine && (
        <div className="absolute left-0 top-8 bottom-0 w-0.5 bg-paper-200" />
      )}

      {/* 时间点 */}
      <div className="absolute left-0 top-1 w-4 h-4 rounded-full bg-paper-100 border-2 border-paper-300 flex items-center justify-center z-10">
        <div className="text-xs">{statusIcon}</div>
      </div>

      {/* 日期 */}
      <div className="text-xs text-paper-500 mb-2">
        {application.applicationDate || '未知日期'}
      </div>

      {/* 卡片内容 */}
      <div className="paper-card p-4 rounded-lg">
        <h4 className="font-serif text-paper-700 font-medium text-sm mb-2">
          {application.company?.name || '未知公司'}
        </h4>
        <p className="text-paper-600 text-sm mb-3">{application.jobTitle}</p>

        {/* 详情 */}
        <div className="space-y-2">
          {application.jobType && (
            <div className="flex items-center text-xs text-paper-500">
              <span className="w-16">类型:</span>
              <span className="px-2 py-0.5 bg-paper-100 rounded">{application.jobType}</span>
            </div>
          )}
          {application.workLocation && (
            <div className="flex items-center text-xs text-paper-500">
              <span className="w-16">地点:</span>
              <span>📍 {application.workLocation}</span>
            </div>
          )}
          {application.salaryMin && (
            <div className="flex items-center text-xs text-paper-500">
              <span className="w-16">薪资:</span>
              <span>
                {application.salaryMin}
                {application.salaryMax && ` - ${application.salaryMax}`}
                {application.salaryCurrency || 'CNY'}
              </span>
            </div>
          )}

          {/* 状态标签 */}
          <div className="mt-3">
            <span
              className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                application.status === 'APPLIED'
                  ? 'bg-accent-blue/20 text-accent-blue border border-accent-blue/30'
                  : application.status === 'INTERVIEW'
                  ? 'bg-accent-purple/20 text-accent-purple border border-accent-purple/30'
                  : application.status === 'OFFER'
                  ? 'bg-accent-green/20 text-accent-green border border-accent-green/30'
                  : application.status === 'REJECTED'
                  ? 'bg-accent-red/20 text-accent-red border border-accent-red/30'
                  : 'bg-paper-100 text-paper-600 border border-paper-200'
              }`}
            >
              {STATUS_CONFIG[application.status].label}
            </span>
          </div>
        </div>

        {/* 备注 */}
        {application.notes && (
          <div className="mt-3 pt-3 border-t border-paper-200">
            <p className="text-xs text-paper-500 line-clamp-2">{application.notes}</p>
          </div>
        )}
      </div>
    </div>
  );
}
