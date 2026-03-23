import { useState } from 'react';
import { ChevronDown, ChevronUp, Award, Lightbulb, MessageCircle, User } from 'lucide-react';
import { Badge, Progress } from '@/components/common';
import type { MockInterviewEvaluation } from '@/types/interview';

export interface ScoreDetailsProps {
  evaluations: MockInterviewEvaluation[];
}

export function ScoreDetails({ evaluations }: ScoreDetailsProps) {
  const [expandedId, setExpandedId] = useState<number | null>(null);

  if (evaluations.length === 0) {
    return (
      <div className="text-center py-8 text-paper-400">
        暂无评分数据
      </div>
    );
  }

  const toggleExpand = (id: number) => {
    setExpandedId(expandedId === id ? null : id);
  };

  return (
    <div className="space-y-4">
      <h3 className="text-paper-700 font-medium flex items-center gap-2">
        <Award className="w-5 h-5 text-accent-amber" />
        各轮评分明细
      </h3>

      <div className="space-y-3">
        {evaluations.map((evaluation, index) => (
          <EvaluationCard 
            key={evaluation.id} 
            evaluation={evaluation} 
            index={index + 1}
            isExpanded={expandedId === evaluation.id}
            onToggle={() => toggleExpand(evaluation.id)}
          />
        ))}
      </div>
    </div>
  );
}

interface EvaluationCardProps {
  evaluation: MockInterviewEvaluation;
  index: number;
  isExpanded: boolean;
  onToggle: () => void;
}

function EvaluationCard({ evaluation, index, isExpanded, onToggle }: EvaluationCardProps) {
  const technicalScore = evaluation.technicalScore ?? 0;
  const logicScore = evaluation.logicScore ?? 0;
  const depthScore = evaluation.depthScore ?? 0;
  const totalScore = technicalScore + logicScore + depthScore;
  const maxScore = 4 + 3 + 3;

  return (
    <div className="border border-paper-200 rounded-lg overflow-hidden">
      <button
        type="button"
        onClick={onToggle}
        className="w-full p-4 flex items-center justify-between bg-paper-50 hover:bg-paper-100 transition-colors"
      >
        <div className="flex items-center gap-3">
          <Badge variant="secondary">第{index}轮</Badge>
          <span className="text-paper-700 font-medium">{evaluation.skillName}</span>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-paper-600 font-medium">
            {totalScore.toFixed(1)} / {maxScore}
          </span>
          {isExpanded ? (
            <ChevronUp className="w-5 h-5 text-paper-400" />
          ) : (
            <ChevronDown className="w-5 h-5 text-paper-400" />
          )}
        </div>
      </button>

      {isExpanded && (
        <div className="p-4 space-y-4 border-t border-paper-200">
          {(evaluation.questionText || evaluation.userAnswer) && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pb-4 border-b border-paper-200">
              <div className="bg-gradient-to-br from-accent-red/5 to-accent-red/10 rounded-lg p-4 border border-accent-red/20">
                <div className="flex items-center gap-2 mb-3">
                  <div className="w-7 h-7 rounded-full bg-accent-red flex items-center justify-center">
                    <MessageCircle className="w-4 h-4 text-white" />
                  </div>
                  <span className="text-paper-700 font-medium text-sm">面试官问题</span>
                </div>
                <p className="text-paper-700 text-sm leading-relaxed whitespace-pre-wrap">
                  {evaluation.questionText || '暂无问题记录'}
                </p>
              </div>
              <div className="bg-gradient-to-br from-accent-amber/5 to-accent-amber/10 rounded-lg p-4 border border-accent-amber/20">
                <div className="flex items-center gap-2 mb-3">
                  <div className="w-7 h-7 rounded-full bg-accent-amber flex items-center justify-center">
                    <User className="w-4 h-4 text-white" />
                  </div>
                  <span className="text-paper-700 font-medium text-sm">你的回答</span>
                </div>
                <p className="text-paper-700 text-sm leading-relaxed whitespace-pre-wrap">
                  {evaluation.userAnswer || '暂无回答记录'}
                </p>
              </div>
            </div>
          )}

          <div className="grid grid-cols-3 gap-4">
            <ScoreItem label="技术能力" score={technicalScore} max={4} />
            <ScoreItem label="逻辑思维" score={logicScore} max={3} />
            <ScoreItem label="深度理解" score={depthScore} max={3} />
          </div>

          <div className="space-y-2">
            <p className="text-paper-600 text-sm">{evaluation.comment}</p>
            {evaluation.improvement && (
              <div className="flex items-start gap-2 p-3 bg-accent-amber/10 rounded-lg">
                <Lightbulb className="w-4 h-4 text-accent-amber flex-shrink-0 mt-0.5" />
                <p className="text-paper-600 text-sm">{evaluation.improvement}</p>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

interface ScoreItemProps {
  label: string;
  score: number;
  max: number;
}

function ScoreItem({ label, score, max }: ScoreItemProps) {
  const percentage = (score / max) * 100;

  return (
    <div className="text-center">
      <p className="text-paper-500 text-xs mb-1">{label}</p>
      <p className="text-paper-700 font-medium">{score.toFixed(1)} / {max}</p>
      <Progress
        value={percentage}
        size="md"
        variant={percentage >= 70 ? 'success' : percentage >= 50 ? 'warning' : 'error'}
        className="mt-2"
      />
    </div>
  );
}
