import { useState } from 'react';
import { List, LayoutList } from 'lucide-react';
import { TimelineFeedView } from './TimelineFeedView';
import { TimelineByApplicationView } from './TimelineByApplicationView';

type TimelineViewMode = 'feed' | 'by-application';

export function TimelineView() {
  const [viewMode, setViewMode] = useState<TimelineViewMode>('feed');

  return (
    <div className="h-full flex flex-col">
      {/* 视图切换按钮 */}
      <div className="flex-shrink-0 p-4 border-b border-paper-200 bg-paper-50">
        <div className="flex items-center justify-between max-w-4xl mx-auto">
          <h2 className="font-serif text-paper-700 font-semibold">时间线</h2>

          {/* 切换按钮组 */}
          <div className="flex items-center gap-2 bg-paper-100 p-1 rounded-lg border border-paper-300">
            <button
              onClick={() => setViewMode('feed')}
              className={`flex items-center gap-2 px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
                viewMode === 'feed'
                  ? 'bg-white text-paper-800 shadow-sm'
                  : 'text-paper-600 hover:text-paper-800'
              }`}
              title="动态流视图"
            >
              <List className="w-4 h-4" />
              <span className="hidden sm:inline">动态流</span>
            </button>

            <button
              onClick={() => setViewMode('by-application')}
              className={`flex items-center gap-2 px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
                viewMode === 'by-application'
                  ? 'bg-white text-paper-800 shadow-sm'
                  : 'text-paper-600 hover:text-paper-800'
              }`}
              title="按申请分组"
            >
              <LayoutList className="w-4 h-4" />
              <span className="hidden sm:inline">按申请</span>
            </button>
          </div>
        </div>
      </div>

      {/* 视图内容 */}
      <div className="flex-1 overflow-y-auto">
        {viewMode === 'feed' ? <TimelineFeedView /> : <TimelineByApplicationView />}
      </div>
    </div>
  );
}
