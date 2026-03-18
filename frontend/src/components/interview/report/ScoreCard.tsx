import { clsx } from 'clsx';
import { Trophy, Award, Target } from 'lucide-react';
import { CircularProgress, Progress } from '@/components/common';

export interface ScoreCardProps {
  totalScore: number;
  credibilityScore?: number;
  maxScore?: number;
}

export function ScoreCard({ totalScore, credibilityScore, maxScore = 10 }: ScoreCardProps) {
  const scorePercentage = (totalScore / maxScore) * 100;
  const credibilityPercentage = credibilityScore !== undefined ? credibilityScore * 100 : null;

  const getScoreVariant = (score: number): 'default' | 'success' | 'warning' | 'error' => {
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

  const getScoreLabelColor = (score: number) => {
    if (score >= 8) return 'text-accent-green';
    if (score >= 6) return 'text-amber-500';
    return 'text-accent-red';
  };

  return (
    <div className="bg-white rounded-xl border border-paper-200 shadow-paper p-6">
      <div className="flex flex-col md:flex-row items-center gap-6">
        <div className="flex flex-col items-center">
          <div className="relative">
            <CircularProgress
              value={scorePercentage}
              size={120}
              strokeWidth={8}
              variant={getScoreVariant(totalScore)}
            />
            <div className="absolute inset-0 flex flex-col items-center justify-center">
              <span className="text-3xl font-bold text-paper-700">{totalScore.toFixed(1)}</span>
              <span className="text-sm text-paper-400">/ {maxScore}</span>
            </div>
          </div>
          <div className="mt-3 flex items-center gap-2">
            <Trophy className={clsx('w-5 h-5', getScoreLabelColor(totalScore))} />
            <span className={clsx('font-medium', getScoreLabelColor(totalScore))}>
              {getScoreLabel(totalScore)}
            </span>
          </div>
        </div>

        <div className="flex-1 space-y-4 w-full">
          <div className="flex items-center gap-3">
            <Award className="w-6 h-6 text-accent-amber" />
            <span className="text-lg font-medium text-paper-700">综合评分</span>
          </div>

          {credibilityPercentage !== null && (
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-paper-500 text-sm">简历可信度</span>
                <span className={clsx(
                  'font-medium',
                  credibilityPercentage >= 80 ? 'text-accent-green' :
                  credibilityPercentage >= 60 ? 'text-amber-500' : 'text-accent-red'
                )}>
                  {credibilityPercentage.toFixed(0)}%
                </span>
              </div>
              <Progress
                value={credibilityPercentage}
                size="md"
                variant={
                  credibilityPercentage >= 80 ? 'success' :
                  credibilityPercentage >= 60 ? 'warning' : 'error'
                }
              />
              {credibilityPercentage < 70 && (
                <p className="text-paper-400 text-sm flex items-center gap-1">
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
