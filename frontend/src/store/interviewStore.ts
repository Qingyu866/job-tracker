import { create } from 'zustand';
import { interviewApi } from '@/services/interviewApi';
import type {
  MockInterviewSession,
  InterviewMessage,
  MockInterviewEvaluation,
  InterviewReport,
} from '@/types/interview';

interface SessionState {
  session: MockInterviewSession | null;
  messages: InterviewMessage[];
  evaluations: MockInterviewEvaluation[];
  report: InterviewReport | null;
  loading: boolean;
  lastUpdated: number;
}

interface InterviewState {
  activeSessionId: string | null;
  sessions: Record<string, SessionState>;
  sessionList: MockInterviewSession[];
  isListLoading: boolean;
  searchKeyword: string;
}

interface InterviewStore extends InterviewState {
  fetchSessionList: (userId: number) => Promise<void>;
  fetchSession: (sessionId: string) => Promise<void>;
  startInterview: (applicationId: number, resumeId: number) => Promise<string>;
  sendMessage: (content: string) => Promise<void>;
  finishInterview: () => Promise<void>;
  fetchReport: (sessionId: string) => Promise<void>;
  switchSession: (sessionId: string) => Promise<void>;
  clearSession: (sessionId: string) => void;
  getActiveSession: () => SessionState | null;
  persistActiveSession: () => void;
  restoreActiveSession: () => Promise<void>;
  setSearchKeyword: (keyword: string) => void;
}

const STORAGE_KEY = 'interview_active_session_id';

function getPersistedSessionId(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
}

function persistSessionId(sessionId: string): void {
  try {
    localStorage.setItem(STORAGE_KEY, sessionId);
  } catch {
    // ignore
  }
}

function clearPersistedSessionId(): void {
  try {
    localStorage.removeItem(STORAGE_KEY);
  } catch {
    // ignore
  }
}

export const useInterviewStore = create<InterviewStore>((set, get) => ({
  activeSessionId: null,
  sessions: {},
  sessionList: [],
  isListLoading: false,
  searchKeyword: '',

  fetchSessionList: async (userId: number) => {
    set({ isListLoading: true });
    try {
      const sessions = await interviewApi.getUserSessions(userId);
      set({ sessionList: sessions, isListLoading: false });
    } catch (error) {
      console.error('获取面试会话列表失败:', error);
      set({ isListLoading: false });
    }
  },

  fetchSession: async (sessionId: string) => {
    const currentSession = get().sessions[sessionId];
    if (currentSession?.loading) return;

    set({
      sessions: {
        ...get().sessions,
        [sessionId]: {
          session: null,
          messages: [],
          evaluations: [],
          report: null,
          loading: true,
          lastUpdated: Date.now(),
        },
      },
    });

    try {
      const [session, messages] = await Promise.all([
        interviewApi.getSession(sessionId),
        interviewApi.getMessages(sessionId),
      ]);

      set({
        activeSessionId: sessionId,
        sessions: {
          ...get().sessions,
          [sessionId]: {
            session,
            messages,
            evaluations: [],
            report: null,
            loading: false,
            lastUpdated: Date.now(),
          },
        },
      });

      persistSessionId(sessionId);
    } catch (error) {
      console.error('获取面试会话失败:', error);
      set({
        sessions: {
          ...get().sessions,
          [sessionId]: {
            ...get().sessions[sessionId],
            loading: false,
          },
        },
      });
    }
  },

  startInterview: async (applicationId: number, resumeId: number) => {
    try {
      const session = await interviewApi.startInterview(applicationId, resumeId);
      const sessionId = session.sessionId;

      set({
        activeSessionId: sessionId,
        sessions: {
          ...get().sessions,
          [sessionId]: {
            session,
            messages: [],
            evaluations: [],
            report: null,
            loading: false,
            lastUpdated: Date.now(),
          },
        },
      });

      persistSessionId(sessionId);
      return sessionId;
    } catch (error) {
      console.error('开始面试失败:', error);
      throw error;
    }
  },

  sendMessage: async (content: string) => {
    const sessionId = get().activeSessionId;
    const sessionState = sessionId ? get().sessions[sessionId] : null;

    if (!sessionId || !sessionState?.session) {
      console.error('没有活跃的面试会话');
      return;
    }

    const userMessage: InterviewMessage = {
      id: `temp-${Date.now()}`,
      sessionId,
      role: 'user',
      content,
      roundNumber: sessionState.session.currentRound,
      createdAt: new Date().toISOString(),
    };

    set({
      sessions: {
        ...get().sessions,
        [sessionId]: {
          ...sessionState,
          messages: [...sessionState.messages, userMessage],
        },
      },
    });

    try {
      const response = await interviewApi.sendMessage(
        sessionId,
        content,
        sessionState.session.currentRound
      );

      set({
        sessions: {
          ...get().sessions,
          [sessionId]: {
            ...get().sessions[sessionId],
            messages: [...get().sessions[sessionId].messages, response],
            lastUpdated: Date.now(),
          },
        },
      });

      const updatedSession = await interviewApi.getSession(sessionId);
      set({
        sessions: {
          ...get().sessions,
          [sessionId]: {
            ...get().sessions[sessionId],
            session: updatedSession,
          },
        },
      });
    } catch (error) {
      console.error('发送消息失败:', error);
    }
  },

  finishInterview: async () => {
    const sessionId = get().activeSessionId;
    if (!sessionId) return;

    try {
      const session = await interviewApi.finishInterview(sessionId);
      set({
        sessions: {
          ...get().sessions,
          [sessionId]: {
            ...get().sessions[sessionId],
            session,
            lastUpdated: Date.now(),
          },
        },
      });
    } catch (error) {
      console.error('结束面试失败:', error);
      throw error;
    }
  },

  fetchReport: async (sessionId: string) => {
    try {
      const [report, evaluations] = await Promise.all([
        interviewApi.getReport(sessionId),
        interviewApi.getEvaluations(sessionId),
      ]);

      set({
        sessions: {
          ...get().sessions,
          [sessionId]: {
            ...get().sessions[sessionId],
            report,
            evaluations,
            lastUpdated: Date.now(),
          },
        },
      });
    } catch (error) {
      console.error('获取面试报告失败:', error);
    }
  },

  switchSession: async (sessionId: string) => {
    await get().fetchSession(sessionId);
  },

  clearSession: (sessionId: string) => {
    const sessions = { ...get().sessions };
    delete sessions[sessionId];

    set({
      sessions,
      activeSessionId: get().activeSessionId === sessionId ? null : get().activeSessionId,
    });

    if (get().activeSessionId === sessionId) {
      clearPersistedSessionId();
    }
  },

  getActiveSession: () => {
    const sessionId = get().activeSessionId;
    return sessionId ? get().sessions[sessionId] : null;
  },

  persistActiveSession: () => {
    const sessionId = get().activeSessionId;
    if (sessionId) {
      persistSessionId(sessionId);
    }
  },

  restoreActiveSession: async () => {
    const sessionId = getPersistedSessionId();
    if (sessionId) {
      try {
        const session = await interviewApi.getSession(sessionId);
        if (session.state !== 'FINISHED') {
          await get().fetchSession(sessionId);
        } else {
          clearPersistedSessionId();
        }
      } catch {
        clearPersistedSessionId();
      }
    }
  },

  setSearchKeyword: (keyword: string) => {
    set({ searchKeyword: keyword });
  },
}));
