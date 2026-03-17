import { Building2, Briefcase, Calendar, Clock, Trophy, Play, FileText, RotateCcw, Check } from 'lucide-react';
import { Button, Badge, Progress } from '@/components/common';
import type { MockInterviewSession, InterviewState } from '@/types/interview';

export interface SessionCardProps {
  session: MockInterviewSession;
  selected?: boolean;
  onSelect?: () => void;
  onContinue: () => void;
  onViewReport: () => void;
  onRestart: () => void;
}

export function SessionCard({
  session,
  selected = false,
  onSelect,
  onContinue,
  onViewReport,
  onRestart,
}: SessionCardProps) {
  const isFinished = session.state === 'FINISHED';
  const progressPercentage = session.totalRounds > 0
    ? Math.round((session.currentRound / session.totalRounds) * 100)
    : 0;

  const getStateBadge = () => {
    if (isFinished) {
      return <Badge variant="success">已完成</Badge>;
    }
    return <Badge variant="info">进行中</Badge>;
  };

  const getStateLabel = (state: InterviewState): string => {
    const labels: Record<InterviewState, string> = {
      IDLE: '准备中',
      WELCOME: '欢迎阶段',
      TECHNICAL_QA: '技术问答',
      SCENARIO: '场景题',
      EVALUATION: '评估中',
      FINISHED: '已结束',
      CREATED: '准备中',
      INTRODUCTION: '自我介绍',
      PROJECT_QA: '项目问答',
      BEHAVIORAL_QA: '行为面试',
      CLOSING: '结束阶段',
    };
    return labels[state] || '未知';
  };

  return (
    <div 
      className={`bg-white rounded-xl border shadow-paper hover:shadow-paper-md transition-shadow overflow-hidden relative ${
        selected ? 'border-accent-amber ring-2 ring-accent-amber/20' : 'border-paper-200'
      }`}
      onClick={onSelect}
    >
      {selected && (
        <div className="absolute top-3 right-3 z-10">
          <div className="w-6 h-6 bg-accent-amber rounded-full flex items-center justify-center">
            <Check className="w-4 h-4 text-white" />
          </div>
        </div>
      )}
      
      <div className="p-4">
        <div className="flex items-start justify-between mb-3">
          <div className="flex items-center gap-2">
            <Building2 className="w-5 h-5 text-paper-400" />
            <h3 className="text-paper-700 font-medium">{session.companyName}</h3>
          </div>
          {getStateBadge()}
        </div>

        <div className="flex items-center gap-2 text-paper-500 text-sm mb-3">
          <Briefcase className="w-4 h-4" />
          <span>{session.jobTitle}</span>
        </div>

        {isFinished ? (
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-paper-500 text-sm">最终评分</span>
              <div className="flex items-center gap-1 text-accent-amber">
                <Trophy className="w-4 h-4" />
                <span className="font-bold">{session.totalScore?.toFixed(1) || '-'}</span>
              </div>
            </div>

            {session.credibilityScore !== undefined && (
              <div className="flex items-center justify-between text-sm">
                <span className="text-paper-500">简历可信度</span>
                <span className={`${
                  session.credibilityScore >= 0.8 ? 'text-accent-green' :
                  session.credibilityScore >= 0.6 ? 'text-amber-500' : 'text-accent-red'
                }`}>
                  {(session.credibilityScore * 100).toFixed(0)}%
                </span>
              </div>
            )}

            <div className="flex items-center gap-2 text-paper-400 text-xs">
              <Calendar className="w-3 h-3" />
              <span>完成于 {new Date(session.finishedAt || session.updatedAt).toLocaleDateString()}</span>
            </div>
          </div>
        ) : (
          <div className="space-y-3">
            <div className="flex items-center justify-between text-sm">
              <span className="text-paper-500">进度</span>
              <span className="text-paper-600">
                第 {session.currentRound} / {session.totalRounds} 轮
              </span>
            </div>
            <Progress value={progressPercentage} size="sm" />
            <p className="text-paper-500 text-xs">
              当前阶段: {getStateLabel(session.state)}
            </p>

            <div className="flex items-center gap-2 text-paper-400 text-xs">
              <Clock className="w-3 h-3" />
              <span>预计剩余 {Math.max(0, (session.totalRounds - session.currentRound) * 3)} 分钟</span>
            </div>
          </div>
        )}
      </div>

      <div className="border-t border-paper-200 p-3 bg-paper-50 flex justify-end gap-2">
        {isFinished ? (
          <>
            <Button
              variant="outline"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                onViewReport();
              }}
              leftIcon={<FileText className="w-4 h-4" />}
            >
              查看报告
            </Button>
            <Button
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                onRestart();
              }}
              leftIcon={<RotateCcw className="w-4 h-4" />}
            >
              再次面试
            </Button>
          </>
        ) : (
          <>
            <Button
              variant="outline"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                onViewReport();
              }}
              leftIcon={<FileText className="w-4 h-4" />}
            >
              查看详情
            </Button>
            <Button
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                onContinue();
              }}
              leftIcon={<Play className="w-4 h-4" />}
            >
              继续面试
            </Button>
          </>
        )}
      </div>
    </div>
  );
}
