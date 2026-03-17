import { clsx } from 'clsx';
import { Trophy, Award, Target } from 'lucide-react';
import { CircularProgress } from '@/components/common';

export interface ScoreCardProps {
  totalScore: number;
  credibilityScore?: number;
  maxScore?: number;
}

export function ScoreCard({ totalScore, credibilityScore, maxScore = 10 }: ScoreCardProps) {
  const scorePercentage = (totalScore / maxScore) * 100;
  const credibilityPercentage = credibilityScore !== undefined ? credibilityScore * 100 : null;

  const getScoreColor = (score: number) => {
    if (score >= 8) return 'success';
    if (score >= 6) return 'warning';
    return 'error';
  };

  const getScoreLabel = (score: number) => {
    if (score >= 9) return '优秀';
    if (score >= 8) return '良好';
    if (score >= 6) return '中等';
    if (score >= 4) return '需改进';
    return '较差';
  };

  return (
    <div className="bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl p-6 text-white">
      <div className="flex flex-col md:flex-row items-center gap-6">
        <div className="flex flex-col items-center">
          <div className="relative">
            <CircularProgress
              value={scorePercentage}
              size={120}
              strokeWidth={8}
              variant={getScoreColor(totalScore)}
            />
            <div className="absolute inset-0 flex flex-col items-center justify-center">
              <span className="text-4xl font-bold">{totalScore.toFixed(1)}</span>
              <span className="text-sm opacity-80">/ {maxScore}</span>
            </div>
          </div>
          <div className="mt-3 flex items-center gap-2">
            <Trophy className="w-5 h-5" />
            <span className="font-medium">{getScoreLabel(totalScore)}</span>
          </div>
        </div>

        <div className="flex-1 space-y-4">
          <div className="flex items-center gap-3">
            <Award className="w-6 h-6 opacity-80" />
            <span className="text-lg font-medium">综合评分</span>
          </div>

          {credibilityPercentage !== null && (
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="opacity-80">简历可信度</span>
                <span className="font-medium">{credibilityPercentage.toFixed(0)}%</span>
              </div>
              <div className="h-2 bg-white/20 rounded-full overflow-hidden">
                <div
                  className={clsx(
                    'h-full rounded-full transition-all duration-500',
                    credibilityPercentage >= 80 ? 'bg-accent-green' :
                    credibilityPercentage >= 60 ? 'bg-amber-400' : 'bg-accent-red'
                  )}
                  style={{ width: `${credibilityPercentage}%` }}
                />
              </div>
              {credibilityPercentage < 70 && (
                <p className="text-sm opacity-80 flex items-center gap-1">
                  <Target className="w-4 h-4" />
                  部分技能可能存在夸大
                </p>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
