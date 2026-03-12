import { VIEW_CONFIG, type ViewType } from '@/utils/constants';
import { Table, Columns, Clock, Calendar } from 'lucide-react';

interface ViewToggleProps {
  currentView: ViewType;
  onViewChange: (view: ViewType) => void;
}

// 图标组件映射
const ICON_MAP = {
  table: Table,
  board: Columns,
  timeline: Clock,
  calendar: Calendar,
};

export function ViewToggle({ currentView, onViewChange }: ViewToggleProps) {
  const views = Object.entries(VIEW_CONFIG) as [ViewType, typeof VIEW_CONFIG[keyof typeof VIEW_CONFIG]][];

  return (
    <div className="flex items-center space-x-1 md:space-x-2 bg-paper-100 p-1 rounded-lg border border-paper-200 overflow-x-auto">
      {views.map(([key, config]) => {
        const IconComponent = ICON_MAP[key as keyof typeof ICON_MAP];
        return (
          <button
            key={key}
            onClick={() => onViewChange(key)}
            className={`
              px-2 md:px-3 py-1.5 rounded-md text-xs md:text-sm font-medium transition-all flex-shrink-0 flex items-center gap-1 md:gap-2
              ${currentView === key
                ? 'bg-paper-50 text-paper-700 shadow-paper border border-paper-200'
                : 'text-paper-600 hover:bg-paper-200'
              }
            `}
            title={config.label}
          >
            <IconComponent className="w-4 h-4" />
            <span className="hidden sm:inline">{config.label}</span>
          </button>
        );
      })}
    </div>
  );
}
