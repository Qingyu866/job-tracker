import { useApplicationStore } from '@/store/applicationStore';
import { Inbox, Library } from 'lucide-react';
import { TimelineItem } from './TimelineItem';

export function TimelineView() {
  const { applications, loading, error } = useApplicationStore();

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

  if (applications.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-full text-paper-400">
        <Inbox className="w-16 h-16 mb-2" />
        <div>暂无数据</div>
      </div>
    );
  }

  // 按申请日期降序排序
  const sortedApplications = [...applications].sort((a, b) => {
    const dateA = a.applicationDate ? new Date(a.applicationDate).getTime() : 0;
    const dateB = b.applicationDate ? new Date(b.applicationDate).getTime() : 0;
    return dateB - dateA;
  });

  return (
    <div className="p-2 md:p-6">
      <div className="max-w-3xl mx-auto">
        {sortedApplications.map((application, index) => (
          <TimelineItem
            key={application.id}
            application={application}
            showLine={index < sortedApplications.length - 1}
          />
        ))}
      </div>

      {/* 统计信息 */}
      <div className="mt-6 text-center text-sm text-paper-500 flex items-center justify-center gap-1">
        <Library className="w-4 h-4" />
        共 {applications.length} 条记录
      </div>
    </div>
  );
}
