import { clsx } from 'clsx';
import { BookOpen, ExternalLink, ArrowUp, ArrowDown, Minus } from 'lucide-react';
import { Badge } from '@/components/common';
import type { ImprovementSuggestion } from '@/types/interview';

export interface ImprovementSuggestionsProps {
  suggestions: ImprovementSuggestion[];
}

export function ImprovementSuggestions({ suggestions }: ImprovementSuggestionsProps) {
  if (suggestions.length === 0) {
    return (
      <div className="text-center py-8 text-paper-400">
        暂无改进建议
      </div>
    );
  }

  const groupedSuggestions = suggestions.reduce((acc, suggestion) => {
    const category = suggestion.category || '其他';
    if (!acc[category]) {
      acc[category] = [];
    }
    acc[category].push(suggestion);
    return acc;
  }, {} as Record<string, ImprovementSuggestion[]>);

  return (
    <div className="space-y-4">
      <h3 className="text-paper-700 font-medium flex items-center gap-2">
        <BookOpen className="w-5 h-5 text-accent-blue" />
        改进建议
      </h3>

      {Object.entries(groupedSuggestions).map(([category, items]) => (
        <div key={category} className="space-y-3">
          <h4 className="text-paper-600 text-sm font-medium border-b border-paper-200 pb-2">
            {category}
          </h4>
          <div className="space-y-3">
            {items.map((suggestion) => (
              <SuggestionCard key={suggestion.id} suggestion={suggestion} />
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}

interface SuggestionCardProps {
  suggestion: ImprovementSuggestion;
}

function SuggestionCard({ suggestion }: SuggestionCardProps) {
  const priorityConfig = {
    high: { icon: ArrowUp, color: 'text-accent-red', label: '高优先级' },
    medium: { icon: Minus, color: 'text-amber-500', label: '中优先级' },
    low: { icon: ArrowDown, color: 'text-accent-blue', label: '低优先级' },
  };

  const { icon: Icon, color, label } = priorityConfig[suggestion.priority];

  return (
    <div className="border border-paper-200 rounded-lg p-4 hover:border-paper-300 transition-colors">
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1">
          <div className="flex items-center gap-2 mb-2">
            <Icon className={clsx('w-4 h-4', color)} />
            <span className="text-paper-700 font-medium">{suggestion.title}</span>
          </div>
          <p className="text-paper-600 text-sm">{suggestion.description}</p>
        </div>
        <Badge
          variant={
            suggestion.priority === 'high' ? 'error' :
            suggestion.priority === 'medium' ? 'warning' : 'info'
          }
          size="sm"
        >
          {label}
        </Badge>
      </div>

      {suggestion.resources && suggestion.resources.length > 0 && (
        <div className="mt-3 pt-3 border-t border-paper-100">
          <p className="text-paper-500 text-xs mb-2">推荐资源：</p>
          <div className="flex flex-wrap gap-2">
            {suggestion.resources.map((resource, index) => (
              <a
                key={index}
                href={resource}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1 text-accent-blue text-sm hover:underline"
              >
                <ExternalLink className="w-3 h-3" />
                资源 {index + 1}
              </a>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
