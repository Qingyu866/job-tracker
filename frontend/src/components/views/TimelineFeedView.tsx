import { useEffect } from 'react';
import { Inbox } from 'lucide-react';
import { useApplicationStore } from '@/store/applicationStore';
import { TimelineEventCard } from '@/components/common/TimelineEventCard';

export function TimelineFeedView() {
  const { logs, loading, error, fetchLogs } = useApplicationStore();

  // 初始化时加载数据
  useEffect(() => {
    if (logs.length === 0) {
      fetchLogs();
    }
  }, []);

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

  if (logs.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-full text-paper-400">
        <Inbox className="w-16 h-16 mb-2" />
        <div>暂无活动记录</div>
      </div>
    );
  }

  return (
    <div className="p-2 md:p-6">
      <div className="max-w-3xl mx-auto">
        {logs.map((logDto, index) => (
          <TimelineEventCard
            key={logDto.log.id}
            logDto={logDto}
            showLine={index < logs.length - 1}
          />
        ))}
      </div>

      {/* 统计信息 */}
      <div className="mt-6 text-center text-sm text-paper-500">
        共 {logs.length} 条活动记录
      </div>
    </div>
  );
}
