import { useCallback } from 'react';
import { Calendar, dateFnsLocalizer } from 'react-big-calendar';
import { format, parse, startOfWeek, getDay } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import { useApplicationStore } from '@/store/applicationStore';
import { STATUS_CONFIG } from '@/utils/constants';
import type { JobApplication } from '@/types';
import 'react-big-calendar/lib/css/react-big-calendar.css';

// 配置本地化
const localizer = dateFnsLocalizer({
  format,
  parse,
  startOfWeek,
  getDay,
  locales: { 'zh-CN': zhCN },
});

// 自定义事件组件
function CalendarEvent({ event }: { event: CalendarEventData }) {
  return (
    <div
      className={`px-2 py-1 rounded text-xs font-medium border ${
        event.resource.status === 'APPLIED'
          ? 'bg-accent-blue/20 text-accent-blue border-accent-blue/30'
          : event.resource.status === 'INTERVIEW'
          ? 'bg-accent-purple/20 text-accent-purple border-accent-purple/30'
          : event.resource.status === 'OFFER'
          ? 'bg-accent-green/20 text-accent-green border-accent-green/30'
          : event.resource.status === 'REJECTED'
          ? 'bg-accent-red/20 text-accent-red border-accent-red/30'
          : 'bg-paper-100 text-paper-600 border-paper-200'
      }`}
    >
      <div className="truncate">{event.title}</div>
    </div>
  );
}

interface CalendarEventData {
  id: number;
  title: string;
  start: Date;
  end: Date;
  resource: JobApplication;
}

export function CalendarView() {
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

  // 将申请转换为日历事件
  const events = applications
    .filter(app => app.applicationDate)
    .map((app) => {
      const applicationDate = new Date(app.applicationDate!);
      const endDate = new Date(applicationDate);
      endDate.setDate(endDate.getDate() + 1);

      return {
        id: app.id,
        title: `${app.company?.name || ''} - ${app.jobTitle}`,
        start: applicationDate,
        end: endDate,
        resource: app,
      } as CalendarEventData;
    });

  const handleSelectEvent = useCallback((event: CalendarEventData) => {
    alert(`查看申请：${event.resource.company?.name}\n职位：${event.resource.jobTitle}\n状态：${STATUS_CONFIG[event.resource.status as keyof typeof STATUS_CONFIG].label}`);
  }, []);

  return (
    <div className="p-6 h-full flex flex-col">
      <div className="flex-1 bg-paper-50 rounded-lg border border-paper-200 overflow-hidden">
        <Calendar
          localizer={localizer}
          events={events}
          startAccessor="start"
          endAccessor="end"
          onSelectEvent={handleSelectEvent}
          components={{
            event: CalendarEvent as any,
          }}
          messages={{
            next: '下一页',
            previous: '上一页',
            today: '今天',
            month: '月',
            week: '周',
            day: '日',
            agenda: '列表',
            date: '日期',
            time: '时间',
            event: '事件',
            noEventsInRange: '此范围内无事件',
            showMore: (count: number) => `+${count} 更多`,
          }}
          className="rbc-calendar-paper"
          style={{ height: '100%' }}
        />
      </div>
    </div>
  );
}
