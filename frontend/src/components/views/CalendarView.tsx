import { useCallback, useEffect, useMemo, useState } from 'react';
import { Calendar, dateFnsLocalizer, Views, type View } from 'react-big-calendar';
import { format, parse, startOfWeek, getDay } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import { useApplicationStore } from '@/store/applicationStore';
import type { InterviewRecord } from '@/types';
import { InterviewDetailModal } from '@/components/common/InterviewDetailModal';
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
        event.resource.status === 'SCHEDULED'
          ? 'bg-accent-blue/20 text-accent-blue border-accent-blue/30'
          : event.resource.status === 'COMPLETED'
          ? 'bg-accent-green/20 text-accent-green border-accent-green/30'
          : event.resource.status === 'CANCELLED'
          ? 'bg-accent-red/20 text-accent-red border-accent-red/30'
          : 'bg-paper-100 text-paper-600 border-paper-200'
      }`}
    >
      <div className="truncate font-medium">{event.title}</div>
      {event.interviewType && (
        <div className="text-xs opacity-80 truncate">{event.interviewType}</div>
      )}
    </div>
  );
}

interface CalendarEventData {
  id: number;
  title: string;
  start: Date;
  end: Date;
  resource: InterviewRecord;
  interviewType?: string;
}

export function CalendarView() {
  const { interviews, loading, error, fetchInterviews } = useApplicationStore();
  const [selectedInterview, setSelectedInterview] = useState<InterviewRecord | null>(null);
  const [currentDate, setCurrentDate] = useState(new Date());
  const [currentView, setCurrentView] = useState<View>(Views.MONTH);

  // 初始化时加载面试数据（只执行一次）
  useEffect(() => {
    console.log('[CalendarView] 加载面试数据');
    fetchInterviews();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 将面试记录转换为日历事件 - 必须在条件渲染之前
  const events = useMemo(() => {
    console.log('[CalendarView] 面试数据:', interviews);

    return interviews
      .filter(interview => {
        const hasDate = !!interview.interviewDate;
        console.log(`[CalendarView] 面试 ${interview.id} 是否有日期:`, hasDate, interview.interviewDate);
        return hasDate;
      })
      .map((interview) => {
        const interviewDate = new Date(interview.interviewDate!);
        console.log(`[CalendarView] 面试 ${interview.id} 解析后的日期:`, interviewDate);

        // 计算结束时间
        const endDate = new Date(interviewDate);
        if (interview.durationMinutes) {
          endDate.setMinutes(endDate.getMinutes() + interview.durationMinutes);
        } else {
          endDate.setHours(endDate.getHours() + 1);
        }

        const eventData = {
          id: interview.id,
          title: `面试 - ${interview.interviewType || '未指定类型'}`,
          start: interviewDate,
          end: endDate,
          resource: interview,
          interviewType: interview.interviewType,
        } as CalendarEventData;

        console.log(`[CalendarView] 面试 ${interview.id} 事件数据:`, eventData);
        return eventData;
      });
  }, [interviews]);

  // 点击事件处理 - 必须在条件渲染之前
  const handleSelectEvent = useCallback((event: CalendarEventData) => {
    console.log('[CalendarView] 点击事件:', event);
    setSelectedInterview(event.resource);
  }, []);

  // 导航事件处理
  const handleNavigate = useCallback((newDate: Date, view: View, action: 'NEXT' | 'PREV' | 'TODAY' | 'DATE') => {
    console.log('[CalendarView] 导航事件:', action, newDate, view);
    setCurrentDate(newDate);
  }, []);

  // 视图切换处理
  const handleViewChange = useCallback((view: View) => {
    console.log('[CalendarView] 视图切换:', view);
    setCurrentView(view);
  }, []);

  console.log('[CalendarView] 渲染日历，事件数量:', events.length, '当前日期:', currentDate);

  // 加载状态
  if (loading) {
    console.log('[CalendarView] 加载中状态');
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-paper-500 flex items-center space-x-2">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-paper-400"></div>
          <span>加载中...</span>
        </div>
      </div>
    );
  }

  // 错误状态
  if (error) {
    console.log('[CalendarView] 错误状态:', error);
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-accent-red bg-accent-red/10 px-4 py-2 rounded-lg border border-accent-red/20">
          {error}
        </div>
      </div>
    );
  }

  // 空状态
  if (interviews.length === 0) {
    console.log('[CalendarView] 空状态，无面试记录');
    return (
      <div className="flex flex-col items-center justify-center h-full text-paper-500">
        <div className="text-4xl mb-2">📅</div>
        <div className="text-sm mb-1">暂无面试安排</div>
        <div className="text-xs text-paper-400">创建求职申请后可添加面试记录</div>
      </div>
    );
  }

  return (
    <div className="p-2 md:p-6 h-full flex flex-col">
      <div className="flex-1 bg-paper-50 rounded-lg border border-paper-200 overflow-hidden">
        <Calendar
          localizer={localizer}
          events={events}
          defaultDate={currentDate}
          startAccessor="start"
          endAccessor="end"
          onSelectEvent={handleSelectEvent}
          onNavigate={handleNavigate}
          onView={handleViewChange}
          view={currentView}
          components={{
            event: CalendarEvent as any,
          toolbar: (props: any) => {
              // 过滤掉有问题的导航按钮
              const { views } = props;
              return (
                <div className="rbc-toolbar">
                  <span className="rbc-btn-group">
                    <button type="button" onClick={() => handleNavigate(new Date(), currentView, 'TODAY')}>
                      今天
                    </button>
                    <button type="button" onClick={() => props.onNavigate('PREV')}>
                      上一页
                    </button>
                    <button type="button" onClick={() => props.onNavigate('NEXT')}>
                      下一页
                    </button>
                  </span>
                  <span className="rbc-toolbar-label">{format(currentDate, 'yyyy年MM月', { locale: zhCN })}</span>
                  <span className="rbc-btn-group">{views.map((view: View) => (
                    <button
                      key={view}
                      type="button"
                      className={currentView === view ? 'rbc-active' : ''}
                      onClick={() => handleViewChange(view)}
                    >
                      {view === 'month' ? '月' : view === 'week' ? '周' : view === 'day' ? '日' : view === 'agenda' ? '列表' : view}
                    </button>
                  ))}</span>
                </div>
              );
            },
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
            noEventsInRange: '此范围内无面试',
            showMore: (count: number) => `+${count} 更多`,
          }}
          className="rbc-calendar-paper"
          style={{ height: '100%' }}
          views={['month', 'week', 'day', 'agenda']}
          defaultView="month"
        />
      </div>

      {/* 面试详情模态框 */}
      <InterviewDetailModal
        interview={selectedInterview}
        onClose={() => setSelectedInterview(null)}
      />
    </div>
  );
}

