import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useInterviewStore } from '@/store/interviewStore';
import { ScoreCard, ScoreDetails, CredibilityAnalysis, ImprovementSuggestions } from '@/components/interview/report';
import { Button, Spinner } from '@/components/common';
import { Header } from '@/components/layout/Header';
import { ArrowLeft, Download, RefreshCw, Calendar, Clock, Building2 } from 'lucide-react';

export function InterviewReportPage() {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();
  
  const { sessions, fetchSession, fetchReport } = useInterviewStore();
  const sessionState = sessionId ? sessions[sessionId] : null;
  const session = sessionState?.session;
  const report = sessionState?.report;
  const evaluations = sessionState?.evaluations || [];
  const loading = sessionState?.loading || false;

  useEffect(() => {
    if (sessionId) {
      fetchSession(sessionId);
      fetchReport(sessionId);
    }
  }, [sessionId]);

  const handleDownload = () => {
    if (!report) return;
    
    const content = generateReportText(report);
    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `面试报告_${session?.companyName}_${new Date().toISOString().split('T')[0]}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const handleRestart = () => {
    navigate('/interview/new');
  };

  if (loading && !session) {
    return (
      <div className="h-screen flex flex-col">
        <Header />
        <div className="flex-1 flex items-center justify-center">
          <Spinner size="xl" />
        </div>
      </div>
    );
  }

  if (!session) {
    return (
      <div className="h-screen flex flex-col">
        <Header />
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center">
            <p className="text-paper-500 mb-4">面试会话不存在</p>
            <Button onClick={() => navigate('/interviews')}>返回列表</Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-paper-50 flex flex-col">
      <Header />
      
      <div className="bg-white border-b border-paper-200">
        <div className="max-w-4xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate('/interviews')}
              leftIcon={<ArrowLeft className="w-4 h-4" />}
            >
              返回
            </Button>
            <div>
              <h1 className="text-paper-700 font-medium">面试报告</h1>
              <p className="text-paper-500 text-sm">
                {session.companyName} - {session.jobTitle}
              </p>
            </div>
          </div>
          
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={handleDownload}
              leftIcon={<Download className="w-4 h-4" />}
            >
              下载报告
            </Button>
            <Button
              size="sm"
              onClick={handleRestart}
              leftIcon={<RefreshCw className="w-4 h-4" />}
            >
              再次面试
            </Button>
          </div>
        </div>
      </div>

      <main className="flex-1 max-w-4xl mx-auto w-full px-4 py-6 space-y-6">
        <div className="flex items-center gap-4 text-paper-500 text-sm">
          <div className="flex items-center gap-1">
            <Calendar className="w-4 h-4" />
            <span>{new Date(session.createdAt).toLocaleDateString()}</span>
          </div>
          <div className="flex items-center gap-1">
            <Clock className="w-4 h-4" />
            <span>{session.totalRounds} 轮面试</span>
          </div>
          <div className="flex items-center gap-1">
            <Building2 className="w-4 h-4" />
            <span>{session.companyName}</span>
          </div>
        </div>

        {report ? (
          <>
            <ScoreCard
              totalScore={report.totalScore}
              credibilityScore={report.credibilityScore}
            />

            {report.summary && (
              <div className="bg-white rounded-xl p-6 shadow-paper">
                <h3 className="text-paper-700 font-medium mb-3">面试总结</h3>
                <p className="text-paper-600 leading-relaxed">{report.summary}</p>
              </div>
            )}

            <div className="bg-white rounded-xl p-6 shadow-paper">
              <ScoreDetails evaluations={evaluations} />
            </div>

            {report.credibilityAnalysis && report.credibilityAnalysis.length > 0 && (
              <div className="bg-white rounded-xl p-6 shadow-paper">
                <CredibilityAnalysis analysis={report.credibilityAnalysis} />
              </div>
            )}

            {report.suggestions && report.suggestions.length > 0 && (
              <div className="bg-white rounded-xl p-6 shadow-paper">
                <ImprovementSuggestions suggestions={report.suggestions} />
              </div>
            )}
          </>
        ) : (
          <div className="bg-white rounded-xl p-8 shadow-paper text-center">
            <Spinner size="lg" className="mx-auto mb-4" />
            <p className="text-paper-500">正在生成报告...</p>
          </div>
        )}
      </main>
    </div>
  );
}

function generateReportText(report: any): string {
  return `
面试报告
========

综合评分: ${report.totalScore.toFixed(1)} / 10
简历可信度: ${(report.credibilityScore * 100).toFixed(0)}%

面试总结:
${report.summary || '无'}

评分明细:
${report.evaluations?.map((e: any) => 
  `- ${e.skillName}: 技术 ${e.technicalScore}/4, 逻辑 ${e.logicScore}/3, 深度 ${e.depthScore}/3`
).join('\n') || '无'}

改进建议:
${report.suggestions?.map((s: any) => `- [${s.priority}] ${s.title}: ${s.description}`).join('\n') || '无'}

生成时间: ${new Date().toLocaleString()}
  `.trim();
}
