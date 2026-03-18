import { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { useInterviewStore } from '@/store/interviewStore';
import { InterviewHeader, InterviewChat, InterviewSidebar, InterviewStartDialog } from '@/components/interview';
import { Spinner, Button } from '@/components/common';
import { Header } from '@/components/layout/Header';
import { FileText, AlertCircle } from 'lucide-react';

import { normalizeState } from '@/types/interview';

interface InterviewPageState {
  applicationId?: number;
  companyName?: string;
  jobTitle?: string;
}

export function InterviewPage() {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const locationState = location.state as InterviewPageState | undefined;
  
  const {
    activeSessionId,
    sessions,
    fetchSession,
    sendMessage,
    finishInterview,
    startInterview,
    persistActiveSession,
  } = useInterviewStore();

  const [showStartDialog, setShowStartDialog] = useState(false);
  const [starting, setStarting] = useState(false);
  const [finishing, setFinishing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const sessionState = activeSessionId ? sessions[activeSessionId] : null;
  const session = sessionState?.session;
  const messages = sessionState?.messages || [];
  const loading = sessionState?.loading || false;

  const applicationId = locationState?.applicationId;
  const companyName = locationState?.companyName;
  const jobTitle = locationState?.jobTitle;

  console.log('InterviewPage render:', { 
    sessionId, 
    applicationId, 
    companyName, 
    jobTitle,
    showStartDialog,
    activeSessionId,
    sessions,
    loading,
    session
  });

  if (!applicationId) {
    console.warn('InterviewPage: No applicationId, location.state:', location.state);
    return (
      <div className="h-screen flex flex-col">
        <Header />
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center">
            <AlertCircle className="w-12 h-12 text-accent-red mx-auto mb-4" />
            <h2 className="text-paper-700 text-lg font-medium mb-2">缺少职位信息</h2>
            <p className="text-paper-500 mb-4">请从职位详情页开始面试</p>
            <Button onClick={() => navigate('/')}>返回工作台</Button>
          </div>
        </div>
      </div>
    );
  }

  useEffect(() => {
    if (sessionId && sessionId !== 'new') {
      fetchSession(sessionId).catch((err) => {
        setError('加载面试会话失败');
        console.error(err);
      });
    } else if (sessionId === 'new') {
      setShowStartDialog(true);
    }
  }, [sessionId]);

  useEffect(() => {
    persistActiveSession();
  }, [activeSessionId]);

  useEffect(() => {
    if (normalizeState(session?.state || '') === 'FINISHED') {
      navigate(`/interview/${session?.sessionId}/report`);
    }
  }, [session?.state, session?.sessionId, navigate]);

  const handleStartInterview = async () => {
    setStarting(true);
    setError(null);
    
    try {
      const newSessionId = await startInterview(applicationId);
      setShowStartDialog(false);
      navigate(`/interview/${newSessionId}`, { replace: true });
    } catch (err) {
      setError('开始面试失败，请重试');
      console.error(err);
    } finally {
      setStarting(false);
    }
  };

  const handleFinishInterview = async () => {
    if (!activeSessionId) return;
    
    if (!confirm('确定要结束面试吗？结束后将生成面试报告。')) {
      return;
    }

    setFinishing(true);
    try {
      await finishInterview();
    } catch (err) {
      setError('结束面试失败');
      console.error(err);
    } finally {
      setFinishing(false);
    }
  };

  const handleExit = () => {
    if (session && session.state !== 'FINISHED') {
      if (!confirm('面试尚未结束，确定要退出吗？进度将被保存。')) {
        return;
      }
    }
    navigate('/interviews');
  };

  if (error) {
    return (
      <div className="h-screen flex flex-col">
        <Header />
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center">
            <AlertCircle className="w-12 h-12 text-accent-red mx-auto mb-4" />
            <h2 className="text-paper-700 text-lg font-medium mb-2">出错了</h2>
            <p className="text-paper-500 mb-4">{error}</p>
            <Button onClick={() => navigate('/interviews')}>返回列表</Button>
          </div>
        </div>
      </div>
    );
  }

  if (loading && !session) {
    return (
      <div className="h-screen flex flex-col">
        <Header />
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center">
            <Spinner size="xl" />
            <p className="text-paper-500 mt-4">加载面试会话...</p>
          </div>
        </div>
      </div>
    );
  }

  if (sessionId === 'new' || sessionId === undefined || showStartDialog) {
    return (
      <div className="h-screen flex flex-col">
        <Header />
        <div className="flex-1 flex items-center justify-center">
          <InterviewStartDialog
            applicationId={applicationId!}
            companyName={companyName!}
            jobTitle={jobTitle!}
            onStart={handleStartInterview}
            onCancel={() => navigate('/interviews')}
            starting={starting}
          />
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
            <FileText className="w-12 h-12 text-paper-300 mx-auto mb-4" />
            <h2 className="text-paper-700 text-lg font-medium mb-2">面试会话不存在</h2>
            <p className="text-paper-500 mb-4">该面试会话可能已被删除</p>
            <Button onClick={() => navigate('/interviews')}>返回列表</Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="h-screen flex flex-col">
      <Header />
      <InterviewHeader session={session!} onExit={handleExit} />

      <div className="flex-1 p-4 lg:p-6">
        <div className="h-full max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-[1fr_320px] gap-4 lg:gap-6">
          <InterviewChat
            session={session || null}
            messages={messages}
            onSendMessage={sendMessage}
            loading={loading}
          />

          <div className="hidden lg:block">
            <InterviewSidebar
              session={session!}
              onFinish={handleFinishInterview}
              finishing={finishing}
            />
          </div>

          <div className="lg:hidden fixed bottom-4 right-4 flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={handleFinishInterview}
              loading={finishing}
            >
              结束面试
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
