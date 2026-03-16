import { useCallback, useEffect, useMemo, useState } from 'react';
import { Calendar, dateFnsLocalizer, Views, type View } from 'react-big-calendar';
import { format, parse, startOfWeek, getDay } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import { Calendar as CalendarIcon } from 'lucide-react';
import { useApplicationStore } from '@/store/applicationStore';
import type { InterviewRecord } from '@/types';
import { InterviewDetailModal } from '@/components/common/InterviewDetailModal';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import './CalendarView.css';

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
      className="px-3 py-2 rounded text-xs font-medium border-2 border-paper-400"
      style={{ backgroundColor: '#E6E6E6', color: '#000000' }}
    >
      <div className="truncate font-medium text-sm">{event.title}</div>
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
          allDay: false,  // 明确标记为非全天事件
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

  // 计算当前视图范围内的面试事件数量
  const currentViewEvents = useMemo(() => {
    if (currentView === Views.MONTH) return events;

    const viewStart = new Date(currentDate);
    const viewEnd = new Date(currentDate);

    if (currentView === Views.WEEK) {
      // 获取本周的起始和结束日期
      const dayOfWeek = viewStart.getDay();
      viewStart.setDate(viewStart.getDate() - dayOfWeek);
      viewStart.setHours(0, 0, 0, 0);
      viewEnd.setDate(viewStart.getDate() + 7);
      viewEnd.setHours(23, 59, 59, 999);
    } else if (currentView === Views.DAY || currentView === Views.AGENDA) {
      // 当天的起始和结束
      viewStart.setHours(0, 0, 0, 0);
      viewEnd.setHours(23, 59, 59, 999);
    }

    return events.filter(event => {
      const eventDate = new Date(event.start);
      return eventDate >= viewStart && eventDate <= viewEnd;
    });
  }, [events, currentDate, currentView]);

  // 计算每天有多少个面试
  const interviewsByDate = useMemo(() => {
    const countMap = new Map<string, number>();

    events.forEach(event => {
      const dateKey = format(new Date(event.start), 'yyyy-MM-dd');
      countMap.set(dateKey, (countMap.get(dateKey) || 0) + 1);
    });

    return countMap;
  }, [events]);

  // 根据面试数量动态计算日历高度
  const calendarHeight = useMemo(() => {
    if (currentView === Views.MONTH) return '100%';

    // 周/日视图：24小时 × 每小时100px = 2400px
    // 再加上头部高度（约150px）和一些额外空间
    return '2600px'; // 固定高度，24小时 × 100px + 头部
  }, [currentView]);

  console.log('[CalendarView] 渲染日历，事件数量:', events.length, '当前视图事件:', currentViewEvents.length, '计算高度:', calendarHeight);

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
        <CalendarIcon className="w-16 h-16 mb-2" />
        <div className="text-sm mb-1">暂无面试安排</div>
        <div className="text-xs text-paper-400">创建求职申请后可添加面试记录</div>
      </div>
    );
  }

  return (
    <div className="p-2 md:p-6 h-full flex flex-col overflow-auto">
      <div className={`${currentView === Views.MONTH ? 'flex-1' : ''} bg-paper-50 rounded-lg border border-paper-200`}>
        <Calendar
          localizer={localizer}
          events={events}
          date={currentDate}
          startAccessor="start"
          endAccessor="end"
          onSelectEvent={handleSelectEvent}
          onNavigate={handleNavigate}
          onView={handleViewChange}
          onDrillDown={(date: Date) => {
            // 点击日期时跳转到日视图
            setCurrentDate(date);
            setCurrentView(Views.DAY);
          }}
          view={currentView}
          components={{
            event: CalendarEvent as any,
            timeGutterHeader: () => (
              <div className="rbc-time-gutter-header flex items-center justify-center py-2 text-paper-600">
                <span className="text-xs font-medium">时间</span>
              </div>
            ),
            week: {
              header: ({ date, localizer }: { date: Date; localizer: any }) => {
                // 自定义周视图的列头，显示星期和日期
                const dayOfWeek = localizer.format(date, 'cccc', 'zh-CN');
                const dayOfMonth = localizer.format(date, 'M月d日', 'zh-CN');
                const isToday = localizer.format(date, 'yyyy-MM-dd', 'zh-CN') === localizer.format(new Date(), 'yyyy-MM-dd', 'zh-CN');

                return (
                  <div className={`flex flex-col items-center justify-center py-2 ${isToday ? 'bg-accent-blue/10' : ''}`}>
                    <div className={`text-sm font-medium ${isToday ? 'text-accent-blue' : 'text-paper-700'}`}>
                      {dayOfWeek}
                    </div>
                    <div className={`text-lg font-bold ${isToday ? 'text-accent-blue' : 'text-paper-900'}`}>
                      {dayOfMonth}
                    </div>
                  </div>
                );
              },
            },
            day: {
              header: ({ date, localizer }: { date: Date; localizer: any }) => {
                // 自定义日视图的列头，显示星期和日期
                const dayOfWeek = localizer.format(date, 'cccc', 'zh-CN');
                const dayOfMonth = localizer.format(date, 'M月d日', 'zh-CN');
                const isToday = localizer.format(date, 'yyyy-MM-dd', 'zh-CN') === localizer.format(new Date(), 'yyyy-MM-dd', 'zh-CN');

                return (
                  <div className={`flex flex-col items-center justify-center py-2 ${isToday ? 'bg-accent-blue/10' : ''}`}>
                    <div className={`text-sm font-medium ${isToday ? 'text-accent-blue' : 'text-paper-700'}`}>
                      {dayOfWeek}
                    </div>
                    <div className={`text-lg font-bold ${isToday ? 'text-accent-blue' : 'text-paper-900'}`}>
                      {dayOfMonth}
                    </div>
                  </div>
                );
              },
            },
            month: {
              dateHeader: ({ date, label }: { date: Date; label: string }) => {
                const dateKey = format(date, 'yyyy-MM-dd');
                const count = interviewsByDate.get(dateKey) || 0;
                const isToday = format(date, 'yyyy-MM-dd') === format(new Date(), 'yyyy-MM-dd');

                const handleClick = () => {
                  setCurrentDate(date);
                  setCurrentView(Views.DAY);
                };

                return (
                  <button
                    type="button"
                    onClick={handleClick}
                    className="w-full h-full relative cursor-pointer hover:bg-paper-100/50 rounded transition-colors"
                    style={{ background: 'transparent', border: 'none', padding: 0 }}
                  >
                    {/* 左上角的日期数字 */}
                    <div className="absolute top-1 left-2 pointer-events-none">
                      <span className={`text-sm ${isToday ? 'text-accent-blue font-bold' : 'text-paper-700'}`}>
                        {label}
                      </span>
                    </div>

                    {/* 居中显示的面试数量 */}
                    {count > 0 && (
                      <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                        <div className="rounded-lg px-3 py-1.5 shadow-lg border-2 border-paper-400" style={{ backgroundColor: '#E6E6E6', color: '#4a3828' }}>
                          <span className="text-lg font-bold">{count}</span>
                          <span className="text-xs ml-1">个面试</span>
                        </div>
                      </div>
                    )}
                  </button>
                );
              },
            },
            toolbar: (props: any) => {
              const { views, label } = props;
              return (
                <div className="rbc-toolbar">
                  <span className="rbc-btn-group">
                    <button type="button" onClick={() => props.onNavigate('TODAY')}>
                      今天
                    </button>
                    <button type="button" onClick={() => props.onNavigate('PREV')}>
                      上一页
                    </button>
                    <button type="button" onClick={() => props.onNavigate('NEXT')}>
                      下一页
                    </button>
                  </span>
                  <span className="rbc-toolbar-label">{label}</span>
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
          formats={{
            dayFormat: 'M月d日 EEEE',
            timeGutterFormat: (date: Date) => {
              const hours = date.getHours().toString().padStart(2, '0');
              const minutes = date.getMinutes().toString().padStart(2, '0');
              return `${hours}时${minutes}分`;
            },
            eventTimeRangeFormat: ({ start, end }: { start: Date; end: Date }) => {
              const startHours = start.getHours().toString().padStart(2, '0');
              const startMinutes = start.getMinutes().toString().padStart(2, '0');
              const endHours = end.getHours().toString().padStart(2, '0');
              const endMinutes = end.getMinutes().toString().padStart(2, '0');
              return `${startHours}时${startMinutes}分 - ${endHours}时${endMinutes}分`;
            },
          }}
          step={60}
          timeslots={1}
          allDayMaxRows={0}
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
          style={{ height: calendarHeight }}
          views={['month', 'week', 'day', 'agenda']}
          defaultView="month"
        />
      </div>

      {/* 面试详情模态框 */}
      <InterviewDetailModal
        interview={selectedInterview}
        onClose={() => setSelectedInterview(null)}
        onUpdate={() => {
          console.log('[CalendarView] 刷新面试数据');
          fetchInterviews();
        }}
      />
    </div>
  );
}

