import { useEffect, useMemo } from 'react';
import { Inbox, ChevronDown, ChevronRight } from 'lucide-react';
import { useApplicationStore } from '@/store/applicationStore';
import { TimelineEventCard } from '@/components/common/TimelineEventCard';
import { useState } from 'react';
import type { ApplicationLogDTO } from '@/types';
import { STATUS_CONFIG } from '@/utils/constants';

export function TimelineByApplicationView() {
  const { logs, loading, error, fetchLogs } = useApplicationStore();
  const [expandedApps, setExpandedApps] = useState<Set<number>>(new Set());

  // 初始化时加载数据
  useEffect(() => {
    if (logs.length === 0) {
      fetchLogs();
    }
  }, []);

  // 按申请分组日志
  const applicationsWithLogs = useMemo(() => {
    const grouped = new Map<number, ApplicationLogDTO[]>();

    logs.forEach(logDto => {
      const appId = logDto.log.applicationId;
      if (!grouped.has(appId)) {
        grouped.set(appId, []);
      }
      grouped.get(appId)!.push(logDto);
    });

    // 转换为数组并排序
    return Array.from(grouped.entries())
      .map(([applicationId, logs]) => ({
        applicationId,
        application: logs[0].application,
        company: logs[0].company,
        logs: logs.sort((a, b) =>
          new Date(b.log.createdAt).getTime() - new Date(a.log.createdAt).getTime()
        ),
      }))
      .sort((a, b) => {
        // 按最新日志时间排序
        const aLatest = a.logs[0]?.log.createdAt || '';
        const bLatest = b.logs[0]?.log.createdAt || '';
        return new Date(bLatest).getTime() - new Date(aLatest).getTime();
      });
  }, [logs]);

  const toggleExpand = (appId: number) => {
    setExpandedApps(prev => {
      const next = new Set(prev);
      if (next.has(appId)) {
        next.delete(appId);
      } else {
        next.add(appId);
      }
      return next;
    });
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-paper-500 flex items-center space-x-2">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-paper-400"></div>
          <span>加载中...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-accent-red bg-accent-red/10 px-4 py-2 rounded-lg border border-accent-red/20">
          {error}
        </div>
      </div>
    );
  }

  if (applicationsWithLogs.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-full text-paper-400">
        <Inbox className="w-16 h-16 mb-2" />
        <div>暂无活动记录</div>
      </div>
    );
  }

  return (
    <div className="p-2 md:p-6">
      <div className="max-w-4xl mx-auto space-y-6">
        {applicationsWithLogs.map(({ applicationId, application, company, logs }) => {
          const isExpanded = expandedApps.has(applicationId);

          return (
            <div
              key={applicationId}
              className="paper-card rounded-lg border-2 border-paper-300 overflow-hidden"
            >
              {/* 申请头部 - 可点击展开/收起 */}
              <button
                onClick={() => toggleExpand(applicationId)}
                className="w-full p-4 flex items-center justify-between hover:bg-paper-50/50 transition-colors text-left"
              >
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  {/* 展开/收起图标 */}
                  <div className="flex-shrink-0">
                    {isExpanded ? (
                      <ChevronDown className="w-5 h-5 text-paper-400" />
                    ) : (
                      <ChevronRight className="w-5 h-5 text-paper-400" />
                    )}
                  </div>

                  {/* 公司和职位 */}
                  <div className="flex-1 min-w-0">
                    <div className="font-serif text-paper-800 font-medium text-sm truncate">
                      {company?.name || '未知公司'}
                    </div>
                    <div className="text-paper-600 text-xs truncate">
                      {application?.jobTitle || '未知职位'}
                    </div>
                  </div>

                  {/* 状态标签 */}
                  {application && (
                    <div className="flex-shrink-0">
                      <span className={`px-2 py-1 text-xs rounded-full border ${
                        application.status === 'APPLIED'
                          ? 'bg-accent-blue/20 text-accent-blue border-accent-blue/30'
                          : application.status === 'SCREENING'
                          ? 'bg-cyan-100 text-cyan-700 border-cyan-200'
                          : application.status === 'INTERVIEW'
                          ? 'bg-accent-purple/20 text-accent-purple border-accent-purple/30'
                          : application.status === 'FINAL_ROUND'
                          ? 'bg-yellow-100 text-yellow-700 border-yellow-200'
                          : application.status === 'OFFERED'
                          ? 'bg-accent-green/20 text-accent-green border-accent-green/30'
                          : application.status === 'ACCEPTED'
                          ? 'bg-green-100 text-green-700 border-green-200'
                          : application.status === 'DECLINED'
                          ? 'bg-red-100 text-red-700 border-red-200'
                          : application.status === 'REJECTED'
                          ? 'bg-accent-red/20 text-accent-red border-accent-red/30'
                          : 'bg-paper-100 text-paper-600 border-paper-200'
                      }`}>
                        {STATUS_CONFIG[application.status as keyof typeof STATUS_CONFIG]?.label || application.status}
                      </span>
                    </div>
                  )}
                </div>

                {/* 日志数量 */}
                <div className="flex-shrink-0 ml-4 text-xs text-paper-500">
                  {logs.length} 条记录
                </div>
              </button>

              {/* 展开时显示日志列表 */}
              {isExpanded && (
                <div className="px-4 pb-4 border-t border-paper-200">
                  {logs.map((logDto, index) => (
                    <div key={logDto.log.id} className="py-4 last:pb-0">
                      <TimelineEventCard
                        logDto={logDto}
                        showLine={index < logs.length - 1}
                      />
                    </div>
                  ))}
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* 统计信息 */}
      <div className="mt-6 text-center text-sm text-paper-500">
        共 {applicationsWithLogs.length} 个申请有活动记录
      </div>
    </div>
  );
}
