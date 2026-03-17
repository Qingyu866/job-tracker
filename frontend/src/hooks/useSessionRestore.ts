import { useEffect, useCallback } from 'react';
import { useInterviewStore } from '@/store/interviewStore';

export function useSessionRestore() {
  const { restoreActiveSession, activeSessionId, sessions } = useInterviewStore();

  useEffect(() => {
    restoreActiveSession();
  }, []);

  const clearRestoredSession = useCallback(() => {
    try {
      localStorage.removeItem('interview_active_session_id');
    } catch {
      // ignore
    }
  }, []);

  return {
    hasRestoredSession: !!activeSessionId,
    restoredSessionId: activeSessionId,
    restoredSession: activeSessionId ? sessions[activeSessionId]?.session : null,
    clearRestoredSession,
  };
}
