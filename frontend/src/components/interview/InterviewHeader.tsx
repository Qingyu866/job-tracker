import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Clock, Trophy } from 'lucide-react';
import { Button, Badge } from '@/components/common';
import type { MockInterviewSession } from '@/types/interview';
import { normalizeState } from '@/types/interview';

export interface InterviewHeaderProps {
  session: MockInterviewSession;
  onExit?: () => void;
}

export function InterviewHeader({ session, onExit }: InterviewHeaderProps) {
  const navigate = useNavigate();
  const isFinished = session.state === 'FINISHED';

  const handleExit = () => {
    if (onExit) {
      onExit();
    } else {
      navigate('/interviews');
    }
  };

  const getStateLabel = () => {
    const normalizedState = normalizeState(session.state);
    switch (normalizedState) {
      case 'IDLE':
        return '准备中';
      case 'WELCOME':
        return '欢迎';
      case 'TECHNICAL_QA':
        return '技术问答';
      case 'SCENARIO':
        return '情景问答';
      case 'EVALUATION':
        return '综合评估';
      case 'FINISHED':
        return '已结束';
      default:
        return '进行中';
    }
  };

  const getStateVariant = () => {
    if (isFinished) return 'secondary';
    return 'info';
  };

  return (
    <header className="h-16 bg-white border-b border-paper-200 flex items-center justify-between px-4 lg:px-6">
      <div className="flex items-center gap-4">
        <Button
          variant="ghost"
          size="sm"
          onClick={handleExit}
          leftIcon={<ArrowLeft className="w-4 h-4" />}
        >
          退出面试
        </Button>

        <div className="hidden sm:block h-6 w-px bg-paper-200" />

        <div className="hidden sm:flex items-center gap-3">
          <h1 className="text-paper-700 font-medium truncate max-w-xs lg:max-w-md">
            {session.companyName} - {session.jobTitle}
          </h1>
          <Badge variant={getStateVariant()} size="sm">
            {getStateLabel()}
          </Badge>
        </div>
      </div>

      <div className="flex items-center gap-4">
        {!isFinished && (
          <div className="hidden md:flex items-center gap-2 text-paper-500 text-sm">
            <Clock className="w-4 h-4" />
            <span>
              第 {session.currentRound} / {session.totalRounds} 轮
            </span>
          </div>
        )}

        {isFinished && session.totalScore !== undefined && (
          <div className="flex items-center gap-2 text-accent-amber">
            <Trophy className="w-5 h-5" />
            <span className="font-medium">{session.totalScore.toFixed(1)}</span>
          </div>
        )}

        <div className="sm:hidden flex items-center gap-2 text-paper-600 text-sm">
          <span>{session.currentRound}/{session.totalRounds}</span>
        </div>
      </div>
    </header>
  );
}
