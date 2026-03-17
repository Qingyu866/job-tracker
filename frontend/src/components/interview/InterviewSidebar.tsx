import { Target, Clock, CheckCircle, Circle } from 'lucide-react';
import { Progress, Badge, Button } from '@/components/common';
import type { MockInterviewSession, SkillCovered, SkillPending } from '@/types/interview';

export interface InterviewSidebarProps {
  session: MockInterviewSession;
  onFinish?: () => void;
  finishing?: boolean;
}

export function InterviewSidebar({
  session,
  onFinish,
  finishing = false,
}: InterviewSidebarProps) {
  const isFinished = session.state === 'FINISHED';
  const progressPercentage = session.totalRounds > 0
    ? Math.round((session.currentRound / session.totalRounds) * 100)
    : 0;

  return (
    <div className="h-full bg-white rounded-xl shadow-paper overflow-hidden flex flex-col">
      <div className="p-4 border-b border-paper-200 bg-paper-50">
        <h2 className="text-paper-700 font-medium flex items-center gap-2">
          <Target className="w-5 h-5 text-accent-amber" />
          面试进度
        </h2>
      </div>

      <div className="flex-1 overflow-y-auto p-4 space-y-6">
        <RoundProgress
          current={session.currentRound}
          total={session.totalRounds}
          percentage={progressPercentage}
        />

        <SkillSection
          title="已考察技能"
          skills={session.skillsCovered}
          type="covered"
        />

        <SkillSection
          title="待考察技能"
          skills={session.skillsPending}
          type="pending"
        />

        {isFinished && session.totalScore !== undefined && (
          <div className="p-4 bg-accent-amber/10 rounded-lg">
            <div className="flex items-center justify-between">
              <span className="text-paper-600">最终得分</span>
              <span className="text-2xl font-bold text-accent-amber">
                {session.totalScore.toFixed(1)}
              </span>
            </div>
            {session.credibilityScore !== undefined && (
              <div className="mt-2 flex items-center justify-between text-sm">
                <span className="text-paper-500">简历可信度</span>
                <span className="text-paper-600">{(session.credibilityScore * 100).toFixed(0)}%</span>
              </div>
            )}
          </div>
        )}
      </div>

      {!isFinished && onFinish && (
        <div className="p-4 border-t border-paper-200 bg-paper-50">
          <Button
            variant="outline"
            className="w-full"
            onClick={onFinish}
            loading={finishing}
          >
            结束面试
          </Button>
          <p className="text-center text-paper-400 text-xs mt-2">
            结束后将生成面试报告
          </p>
        </div>
      )}
    </div>
  );
}

interface RoundProgressProps {
  current: number;
  total: number;
  percentage: number;
}

function RoundProgress({ current, total, percentage }: RoundProgressProps) {
  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between text-sm">
        <span className="text-paper-600">当前轮次</span>
        <span className="text-paper-700 font-medium">
          {current} / {total}
        </span>
      </div>
      <Progress value={percentage} variant="default" size="md" />
      <div className="flex items-center justify-center gap-2 text-paper-500 text-xs">
        <Clock className="w-3 h-3" />
        <span>预计剩余 {Math.max(0, (total - current) * 3)} 分钟</span>
      </div>
    </div>
  );
}

interface SkillSectionProps {
  title: string;
  skills: SkillCovered[] | SkillPending[];
  type: 'covered' | 'pending';
}

function SkillSection({ title, skills, type }: SkillSectionProps) {
  if (skills.length === 0) return null;

  return (
    <div className="space-y-3">
      <h3 className="text-paper-600 text-sm font-medium flex items-center gap-2">
        {type === 'covered' ? (
          <CheckCircle className="w-4 h-4 text-accent-green" />
        ) : (
          <Circle className="w-4 h-4 text-paper-400" />
        )}
        {title}
        <span className="text-paper-400">({skills.length})</span>
      </h3>
      <div className="flex flex-wrap gap-2">
        {skills.map((skill) => (
          <SkillBadge key={skill.id} skill={skill} type={type} />
        ))}
      </div>
    </div>
  );
}

interface SkillBadgeProps {
  skill: SkillCovered | SkillPending;
  type: 'covered' | 'pending';
}

function SkillBadge({ skill, type }: SkillBadgeProps) {
  if (type === 'covered') {
    const coveredSkill = skill as SkillCovered;
    return (
      <Badge variant="success" size="sm">
        {skill.name}
        {coveredSkill.score !== undefined && (
          <span className="ml-1 opacity-70">
            {coveredSkill.score.toFixed(1)}
          </span>
        )}
      </Badge>
    );
  }

  return (
    <Badge variant="outline" size="sm">
      {skill.name}
    </Badge>
  );
}
