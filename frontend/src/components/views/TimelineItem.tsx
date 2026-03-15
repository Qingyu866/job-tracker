import { FileText, CheckCircle, Briefcase, Trophy, XCircle, ArrowLeft, MapPin } from 'lucide-react';
import type { JobApplication } from '@/types';
import { STATUS_CONFIG } from '@/utils/constants';

interface TimelineItemProps {
  application: JobApplication;
  showLine?: boolean;
}

// 状态图标组件映射
const STATUS_ICONS: Record<string, React.ComponentType<{ className?: string }>> = {
  WISHLIST: FileText,
  APPLIED: CheckCircle,
  SCREENING: Briefcase,
  INTERVIEW: Briefcase,
  FINAL_ROUND: Briefcase,
  OFFERED: Trophy,
  ACCEPTED: Trophy,
  DECLINED: XCircle,
  EXPIRED: XCircle,
  REJECTED: XCircle,
  WITHDRAWN: ArrowLeft,
};

const STATUS_ICON_COLORS: Record<string, string> = {
  WISHLIST: 'text-paper-500',
  APPLIED: 'text-accent-blue',
  SCREENING: 'text-cyan-600',
  INTERVIEW: 'text-accent-purple',
  FINAL_ROUND: 'text-yellow-600',
  OFFERED: 'text-accent-green',
  ACCEPTED: 'text-green-600',
  DECLINED: 'text-red-600',
  EXPIRED: 'text-gray-500',
  REJECTED: 'text-accent-red',
  WITHDRAWN: 'text-paper-400',
};

export function TimelineItem({ application, showLine = true }: TimelineItemProps) {
  const StatusIcon = STATUS_ICONS[application.status] || FileText;
  const iconColor = STATUS_ICON_COLORS[application.status] || 'text-paper-500';

  return (
    <div className="relative pl-6 md:pl-8 pb-6 md:pb-8 last:pb-0">
      {/* 时间线 */}
      {showLine && (
        <div className="absolute left-0 top-8 bottom-0 w-0.5 bg-paper-200" />
      )}

      {/* 时间点 */}
      <div className="absolute left-0 top-1 w-4 h-4 rounded-full bg-paper-100 border-2 border-paper-300 flex items-center justify-center z-10">
        <StatusIcon className={`w-3 h-3 ${iconColor}`} />
      </div>

      {/* 日期 */}
      <div className="text-xs text-paper-500 mb-2">
        {application.applicationDate || '未知日期'}
      </div>

      {/* 卡片内容 */}
      <div className="paper-card p-3 md:p-4 rounded-lg">
        <h4 className="font-serif text-paper-700 font-medium text-sm mb-2">
          {application.company?.name || '未知公司'}
        </h4>
        <p className="text-paper-600 text-sm mb-3">{application.jobTitle}</p>

        {/* 详情 */}
        <div className="space-y-2">
          {application.jobType && (
            <div className="flex items-center text-xs text-paper-500">
              <span className="w-16 flex-shrink-0">类型:</span>
              <span className="px-2 py-0.5 bg-paper-100 rounded">{application.jobType}</span>
            </div>
          )}
          {application.workLocation && (
            <div className="flex items-center text-xs text-paper-500">
              <span className="w-16 flex-shrink-0">地点:</span>
              <span className="truncate flex items-center gap-1">
                <MapPin className="w-3 h-3 flex-shrink-0" />
                {application.workLocation}
              </span>
            </div>
          )}
          {application.salaryMin && (
            <div className="flex items-center text-xs text-paper-500">
              <span className="w-16 flex-shrink-0">薪资:</span>
              <span className="truncate">
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
                  : application.status === 'SCREENING'
                  ? 'bg-cyan-100 text-cyan-700 border border-cyan-200'
                  : application.status === 'INTERVIEW'
                  ? 'bg-accent-purple/20 text-accent-purple border border-accent-purple/30'
                  : application.status === 'FINAL_ROUND'
                  ? 'bg-yellow-100 text-yellow-700 border border-yellow-200'
                  : application.status === 'OFFERED'
                  ? 'bg-accent-green/20 text-accent-green border border-accent-green/30'
                  : application.status === 'ACCEPTED'
                  ? 'bg-green-100 text-green-700 border border-green-200'
                  : application.status === 'DECLINED'
                  ? 'bg-red-100 text-red-700 border border-red-200'
                  : application.status === 'REJECTED'
                  ? 'bg-accent-red/20 text-accent-red border border-accent-red/30'
                  : 'bg-paper-100 text-paper-600 border border-paper-200'
              }`}
            >
              {STATUS_CONFIG[application.status as keyof typeof STATUS_CONFIG]?.label || application.status}
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
