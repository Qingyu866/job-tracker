import { clsx } from 'clsx';
import { AlertTriangle, CheckCircle, AlertCircle, MinusCircle } from 'lucide-react';
import { Badge } from '@/components/common';
import type { CredibilityAnalysisItem } from '@/types/interview';

export interface CredibilityAnalysisProps {
  analysis: CredibilityAnalysisItem[];
}

export function CredibilityAnalysis({ analysis }: CredibilityAnalysisProps) {
  if (analysis.length === 0) {
    return (
      <div className="text-center py-8 text-paper-400">
        暂无真实性分析数据
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <h3 className="text-paper-700 font-medium flex items-center gap-2">
        <AlertTriangle className="w-5 h-5 text-amber-500" />
        简历真实性分析
      </h3>

      <div className="bg-paper-50 rounded-lg p-4 mb-4">
        <p className="text-paper-600 text-sm">
          基于面试回答与简历描述的对比，分析技能掌握程度的真实性
        </p>
      </div>

      <div className="space-y-3">
        {analysis.map((item, index) => (
          <CredibilityItem key={index} item={item} />
        ))}
      </div>

      <div className="mt-4 p-4 bg-accent-amber/10 rounded-lg">
        <p className="text-paper-600 text-sm">
          <strong>建议：</strong>根据分析结果，适当调整简历中技能描述的准确程度，
          避免在面试中出现较大偏差。
        </p>
      </div>
    </div>
  );
}

interface CredibilityItemProps {
  item: CredibilityAnalysisItem;
}

function CredibilityItem({ item }: CredibilityItemProps) {
  const config = {
    high: {
      icon: AlertCircle,
      color: 'text-accent-red',
      bgColor: 'bg-accent-red/10',
      label: '夸大程度高',
    },
    medium: {
      icon: AlertTriangle,
      color: 'text-amber-500',
      bgColor: 'bg-amber-100',
      label: '夸大程度中',
    },
    low: {
      icon: MinusCircle,
      color: 'text-accent-blue',
      bgColor: 'bg-accent-blue/10',
      label: '夸大程度低',
    },
    none: {
      icon: CheckCircle,
      color: 'text-accent-green',
      bgColor: 'bg-accent-green/10',
      label: '真实可信',
    },
  };

  const { icon: Icon, color, bgColor, label } = config[item.exaggerationLevel];

  return (
    <div className={clsx('rounded-lg p-4 border border-paper-200', bgColor)}>
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-2">
          <Icon className={clsx('w-5 h-5', color)} />
          <span className="text-paper-700 font-medium">{item.skillName}</span>
        </div>
        <Badge
          variant={
            item.exaggerationLevel === 'none' ? 'success' :
            item.exaggerationLevel === 'high' ? 'error' :
            item.exaggerationLevel === 'medium' ? 'warning' : 'info'
          }
          size="sm"
        >
          {label}
        </Badge>
      </div>

      <div className="mt-3 grid grid-cols-2 gap-4 text-sm">
        <div>
          <span className="text-paper-400">简历声称：</span>
          <span className="text-paper-600 ml-1">{item.claimedLevel}</span>
        </div>
        <div>
          <span className="text-paper-400">实际水平：</span>
          <span className="text-paper-600 ml-1">{item.actualLevel}</span>
        </div>
      </div>

      {item.comment && (
        <p className="mt-2 text-paper-500 text-sm">{item.comment}</p>
      )}
    </div>
  );
}
