import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useInterviewStore } from '@/store/interviewStore';
import { useUserStore } from '@/store/userStore';
import { SessionCard } from '@/components/interview/SessionCard';
import { Button, Spinner } from '@/components/common';
import { Header } from '@/components/layout/Header';
import { Plus, Filter, Download, Trash2 } from 'lucide-react';

type FilterType = 'all' | 'active' | 'finished';

export function InterviewListPage() {
  const navigate = useNavigate();
  const { sessionList, fetchSessionList, isListLoading, searchKeyword } = useInterviewStore();
  const { userInfo } = useUserStore();
  
  const [filter, setFilter] = useState<FilterType>('all');
  const [selectedSessions, setSelectedSessions] = useState<Set<string>>(new Set());

  useEffect(() => {
    if (userInfo?.id) {
      fetchSessionList();
    }
  }, [userInfo?.id]);

  const filteredSessions = sessionList.filter((session) => {
    const matchesFilter = 
      filter === 'all' ||
      (filter === 'active' && session.state !== 'FINISHED') ||
      (filter === 'finished' && session.state === 'FINISHED');

    const matchesSearch = 
      !searchKeyword ||
      session.companyName.toLowerCase().includes(searchKeyword.toLowerCase()) ||
      session.jobTitle.toLowerCase().includes(searchKeyword.toLowerCase());

    return matchesFilter && matchesSearch;
  });

  const handleNewInterview = () => {
    navigate('/interview/new');
  };

  const handleContinueInterview = (sessionId: string) => {
    navigate(`/interview/${sessionId}`);
  };

  const handleViewReport = (sessionId: string) => {
    navigate(`/interview/${sessionId}/report`);
  };

  const handleSelectSession = (sessionId: string) => {
    setSelectedSessions(prev => {
      const newSet = new Set(prev);
      if (newSet.has(sessionId)) {
        newSet.delete(sessionId);
      } else {
        newSet.add(sessionId);
      }
      return newSet;
    });
  };

  const handleDeleteSessions = async () => {
    if (selectedSessions.size === 0) return;
    
    if (!confirm(`确定要删除选中的 ${selectedSessions.size} 个面试会话吗？此操作不可恢复。`)) {
      return;
    }

    try {
      for (const sessionId of selectedSessions) {
        await fetch(`/api/mock-interview/sessions/${sessionId}`, {
          method: 'DELETE'
        });
      }
      setSelectedSessions(new Set());
      if (userInfo?.id) {
        fetchSessionList();
      }
    } catch (error) {
      console.error('删除失败:', error);
    }
  };

  const handleExportSessions = async () => {
    if (sessionList.length === 0) return;

    const data = sessionList.map(session => ({
      sessionId: session.sessionId,
      companyName: session.companyName,
      jobTitle: session.jobTitle,
      state: session.state,
      currentRound: session.currentRound,
      totalRounds: session.totalRounds,
      totalScore: session.totalScore,
      credibilityScore: session.credibilityScore,
      createdAt: session.createdAt,
      updatedAt: session.updatedAt,
      finishedAt: session.finishedAt,
    }));

    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `面试会话_${new Date().toISOString().split('T')[0]}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="h-screen flex flex-col">
      <Header />
      
      <div className="h-12 md:h-14 border-b flex items-center px-3 md:px-6 space-x-3 md:space-x-4 bg-paper-50">
        <div className="flex items-center space-x-1 md:space-x-2 bg-paper-100 p-1 rounded-lg border border-paper-200 overflow-x-auto">
          <button
            onClick={handleNewInterview}
            className="px-2 md:px-3 py-1.5 rounded-md text-xs md:text-sm font-medium transition-all flex-shrink-0 flex items-center gap-1 md:gap-2 bg-paper-50 text-paper-700 shadow-paper border border-paper-200 hover:bg-paper-200"
          >
            <Plus className="w-4 h-4" />
            <span className="hidden sm:inline">新建面试</span>
          </button>
          
          <button
            onClick={handleExportSessions}
            disabled={sessionList.length === 0}
            className="px-2 md:px-3 py-1.5 rounded-md text-xs md:text-sm font-medium transition-all flex-shrink-0 flex items-center gap-1 md:gap-2 text-paper-600 hover:bg-paper-200 disabled:opacity-50"
          >
            <Download className="w-4 h-4" />
            <span className="hidden sm:inline">导出</span>
          </button>
          
          <button
            onClick={handleDeleteSessions}
            disabled={selectedSessions.size === 0}
            className="px-2 md:px-3 py-1.5 rounded-md text-xs md:text-sm font-medium transition-all flex-shrink-0 flex items-center gap-1 md:gap-2 text-paper-600 hover:bg-paper-200 disabled:opacity-50"
          >
            <Trash2 className="w-4 h-4" />
            <span className="hidden sm:inline">删除</span>
          </button>
        </div>

        <div className="flex items-center gap-1 bg-paper-100 p-1 rounded-lg border border-paper-200 ml-auto">
          <Filter className="w-4 h-4 text-paper-400 ml-1" />
          {(['all', 'active', 'finished'] as FilterType[]).map((f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-2 md:px-3 py-1 rounded-md text-xs md:text-sm font-medium transition-colors ${
                filter === f
                  ? 'bg-accent-amber text-paper-800'
                  : 'text-paper-600 hover:bg-paper-200'
              }`}
            >
              {f === 'all' ? '全部' : f === 'active' ? '进行中' : '已完成'}
            </button>
          ))}
        </div>
      </div>

      <main className="flex-1 overflow-auto">
        <div className="max-w-6xl mx-auto px-4 py-6">
          {isListLoading ? (
            <div className="flex items-center justify-center py-12">
              <Spinner size="lg" />
            </div>
          ) : filteredSessions.length === 0 ? (
            <div className="text-center py-12">
              <div className="w-16 h-16 rounded-full bg-paper-100 flex items-center justify-center mx-auto mb-4">
                <Filter className="w-8 h-8 text-paper-400" />
              </div>
              <h3 className="text-paper-700 font-medium mb-2">
                {searchKeyword ? '未找到匹配的面试' : '暂无面试记录'}
              </h3>
              <p className="text-paper-500 text-sm mb-4">
                {searchKeyword ? '尝试其他搜索词' : '点击上方按钮开始你的第一次模拟面试'}
              </p>
              {!searchKeyword && (
                <Button onClick={handleNewInterview}>开始面试</Button>
              )}
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {filteredSessions.map((session) => (
                <SessionCard
                  key={session.sessionId}
                  session={session}
                  selected={selectedSessions.has(session.sessionId)}
                  onSelect={() => handleSelectSession(session.sessionId)}
                  onContinue={() => handleContinueInterview(session.sessionId)}
                  onViewReport={() => handleViewReport(session.sessionId)}
                  onRestart={handleNewInterview}
                />
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
