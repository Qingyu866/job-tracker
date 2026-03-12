import { FileText, CheckCircle, XCircle, ArrowLeft, Calendar, MessageSquare, Star, FileText as FileIcon } from 'lucide-react';
import type { ApplicationLogDTO } from '@/types';
import { formatDistanceToNow } from 'date-fns';
import { zhCN } from 'date-fns/locale';

interface TimelineEventCardProps {
  logDto: ApplicationLogDTO;
  showLine?: boolean;
}

// 日志类型图标配置
const LOG_TYPE_CONFIG = {
  APPLICATION_CREATED: {
    icon: FileIcon,
    color: 'text-paper-500',
    bgColor: 'bg-paper-100',
    borderColor: 'border-paper-300',
  },
  APPLICATION_SUBMITTED: {
    icon: CheckCircle,
    color: 'text-accent-blue',
    bgColor: 'bg-accent-blue/10',
    borderColor: 'border-accent-blue/30',
  },
  STATUS_CHANGE: {
    icon: ArrowLeft,
    color: 'text-accent-purple',
    bgColor: 'bg-accent-purple/10',
    borderColor: 'border-accent-purple/30',
  },
  INTERVIEW_SCHEDULED: {
    icon: Calendar,
    color: 'text-accent-amber',
    bgColor: 'bg-accent-amber/10',
    borderColor: 'border-accent-amber/30',
  },
  INTERVIEW_COMPLETED: {
    icon: Star,
    color: 'text-accent-green',
    bgColor: 'bg-accent-green/10',
    borderColor: 'border-accent-green/30',
  },
  INTERVIEW_CANCELLED: {
    icon: XCircle,
    color: 'text-accent-red',
    bgColor: 'bg-accent-red/10',
    borderColor: 'border-accent-red/30',
  },
  INTERVIEW_NO_SHOW: {
    icon: XCircle,
    color: 'text-paper-400',
    bgColor: 'bg-paper-100',
    borderColor: 'border-paper-300',
  },
  FEEDBACK_RECEIVED: {
    icon: MessageSquare,
    color: 'text-accent-blue',
    bgColor: 'bg-accent-blue/10',
    borderColor: 'border-accent-blue/30',
  },
  NOTE_ADDED: {
    icon: FileText,
    color: 'text-paper-500',
    bgColor: 'bg-paper-100',
    borderColor: 'border-paper-300',
  },
  DOCUMENT_UPLOADED: {
    icon: FileIcon,
    color: 'text-paper-500',
    bgColor: 'bg-paper-100',
    borderColor: 'border-paper-300',
  },
};

export function TimelineEventCard({ logDto, showLine = true }: TimelineEventCardProps) {
  const { log, application, company } = logDto;
  const config = LOG_TYPE_CONFIG[log.logType] || LOG_TYPE_CONFIG.APPLICATION_CREATED;
  const Icon = config.icon;

  // 格式化相对时间
  const relativeTime = formatDistanceToNow(new Date(log.createdAt), {
    addSuffix: true,
    locale: zhCN
  });

  return (
    <div className="relative pl-6 md:pl-8 pb-6 md:pb-8 last:pb-0">
      {/* 时间线 */}
      {showLine && (
        <div className="absolute left-0 top-8 bottom-0 w-0.5 bg-paper-200" />
      )}

      {/* 时间点 */}
      <div className={`absolute left-0 top-1 w-4 h-4 rounded-full ${config.bgColor} border-2 ${config.borderColor} flex items-center justify-center z-10`}>
        <Icon className={`w-3 h-3 ${config.color}`} />
      </div>

      {/* 时间标签 */}
      <div className="text-xs text-paper-500 mb-2">
        {relativeTime}
      </div>

      {/* 卡片内容 */}
      <div className="paper-card p-3 md:p-4 rounded-lg border-2 border-paper-200 hover:border-paper-300 transition-colors">
        {/* 公司和职位 */}
        <div className="mb-2">
          <h4 className="font-serif text-paper-800 font-medium text-sm mb-1">
            {company?.name || '未知公司'}
          </h4>
          <p className="text-paper-600 text-xs">{application?.jobTitle || '未知职位'}</p>
        </div>

        {/* 事件标题和内容 */}
        <div className={`inline-block px-2 py-1 rounded ${config.bgColor} ${config.borderColor} border mb-2`}>
          <span className={`text-xs font-medium ${config.color}`}>{log.logTitle}</span>
        </div>

        {log.logContent && (
          <p className="text-sm text-paper-700 line-clamp-2">{log.logContent}</p>
        )}

        {/* 记录者标签 */}
        <div className="mt-2 flex items-center gap-1 text-xs text-paper-400">
          <span className={`px-1.5 py-0.5 rounded ${
            log.loggedBy === 'SYSTEM'
              ? 'bg-paper-100 text-paper-500'
              : 'bg-accent-blue/10 text-accent-blue'
          }`}>
            {log.loggedBy === 'SYSTEM' ? '系统记录' : '用户操作'}
          </span>
        </div>
      </div>
    </div>
  );
}
