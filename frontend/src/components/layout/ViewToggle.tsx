import { VIEW_CONFIG, type ViewType } from '@/utils/constants';

interface ViewToggleProps {
  currentView: ViewType;
  onViewChange: (view: ViewType) => void;
}

export function ViewToggle({ currentView, onViewChange }: ViewToggleProps) {
  const views = Object.entries(VIEW_CONFIG) as [ViewType, typeof VIEW_CONFIG[keyof typeof VIEW_CONFIG]][];

  return (
    <div className="flex items-center space-x-2 bg-paper-100 p-1 rounded-lg border border-paper-200">
      {views.map(([key, config]) => (
        <button
          key={key}
          onClick={() => onViewChange(key)}
          className={`
            px-3 py-1.5 rounded-md text-sm font-medium transition-all
            ${currentView === key
              ? 'bg-paper-50 text-paper-700 shadow-paper border border-paper-200'
              : 'text-paper-600 hover:bg-paper-200'
            }
          `}
        >
          {config.label}
        </button>
      ))}
    </div>
  );
}
