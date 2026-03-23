import { apiClient } from '@/lib/apiClient';
import type {
  MockInterviewSession,
  InterviewMessage,
  MockInterviewEvaluation,
  InterviewReport,
  ResumeProject,
  ResumeSkill,
} from '@/types/interview';
import type { UserResume } from '@/types/resume';

interface BackendInterviewMessage {
  messageId: number | string;
  sessionId: string;
  roundNumber: number;
  sequenceInRound: number;
  role: 'USER' | 'ASSISTANT';
  content: string;
  skillId?: number;
  skillName?: string;
  createdAt: string;
}

interface BackendMockInterviewSession {
  sessionId: string;
  applicationId: number;
  resumeId: number;
  userId: number;
  companyId?: number;
  jobTitle: string;
  seniorityLevel?: string;
  state: string;
  currentRound: number;
  totalRounds: number;
  skillsCovered?: string;
  skillsPending?: string;
  totalScore?: number;
  resumeCredibilityScore?: number;
  createdAt: string;
  updatedAt?: string;
  finishedAt?: string;
}

interface Company {
  id: number;
  name: string;
}

function transformMessage(msg: BackendInterviewMessage): InterviewMessage {
  return {
    id: String(msg.messageId),
    sessionId: msg.sessionId,
    role: msg.role === 'USER' ? 'CANDIDATE' : 'INTERVIEWER',
    content: msg.content,
    roundNumber: msg.roundNumber,
    skillTag: msg.skillName,
    createdAt: msg.createdAt,
  };
}

async function getCompanyName(companyId?: number): Promise<string> {
  if (!companyId) return '';
  try {
    const response = await apiClient.get<Company>(`/companies/${companyId}`);
    return response.data?.name || '';
  } catch {
    return '';
  }
}

function transformSession(session: BackendMockInterviewSession, companyName?: string): MockInterviewSession {
  return {
    sessionId: session.sessionId,
    applicationId: session.applicationId,
    resumeId: session.resumeId,
    userId: session.userId,
    companyName: companyName || '',
    jobTitle: session.jobTitle,
    state: session.state as any,
    currentRound: session.currentRound,
    totalRounds: session.totalRounds,
    skillsCovered: [],
    skillsPending: [],
    totalScore: session.totalScore,
    credibilityScore: session.resumeCredibilityScore,
    createdAt: session.createdAt,
    updatedAt: session.updatedAt || session.createdAt,
    finishedAt: session.finishedAt,
  };
}

interface BackendMockInterviewEvaluation {
  evaluationId: number;
  sessionId: string;
  roundNumber: number;
  skillName: string;
  questionText?: string;
  userAnswer?: string;
  technicalScore: number;
  logicScore: number;
  depthScore: number;
  totalScore: number;
  feedback: string;
  suggestion?: string;
  evaluatedAt: string;
}

function transformEvaluation(evaluation: BackendMockInterviewEvaluation): MockInterviewEvaluation {
  return {
    id: evaluation.evaluationId,
    sessionId: evaluation.sessionId,
    roundNumber: evaluation.roundNumber,
    skillName: evaluation.skillName,
    questionText: evaluation.questionText,
    userAnswer: evaluation.userAnswer,
    technicalScore: evaluation.technicalScore,
    logicScore: evaluation.logicScore,
    depthScore: evaluation.depthScore,
    comment: evaluation.feedback,
    improvement: evaluation.suggestion,
    createdAt: evaluation.evaluatedAt,
  };
}

export const interviewApi = {
  async startInterview(applicationId: number): Promise<MockInterviewSession> {
    const response = await apiClient.post<BackendMockInterviewSession>('/mock-interview/start', {
      applicationId,
    });
    const companyName = await getCompanyName(response.data.companyId);
    return transformSession(response.data, companyName);
  },

  async getSession(sessionId: string): Promise<MockInterviewSession> {
    const response = await apiClient.get<BackendMockInterviewSession>(`/mock-interview/sessions/${sessionId}`);
    const companyName = await getCompanyName(response.data.companyId);
    return transformSession(response.data, companyName);
  },

  async sendMessage(sessionId: string, content: string, roundNumber: number): Promise<InterviewMessage> {
    const response = await apiClient.post<BackendInterviewMessage>(
      `/mock-interview/sessions/${sessionId}/message`,
      { content, roundNumber }
    );
    return transformMessage(response.data);
  },

  async getMessages(sessionId: string): Promise<InterviewMessage[]> {
    const response = await apiClient.get<BackendInterviewMessage[]>(
      `/mock-interview/sessions/${sessionId}/messages`
    );
    const messages = response.data.map(transformMessage);
    return messages.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
  },

  async finishInterview(sessionId: string): Promise<MockInterviewSession> {
    const response = await apiClient.post<BackendMockInterviewSession>(
      `/mock-interview/sessions/${sessionId}/finish`
    );
    const companyName = await getCompanyName(response.data.companyId);
    return transformSession(response.data, companyName);
  },

  async pauseInterview(sessionId: string): Promise<void> {
    await apiClient.post(`/mock-interview/sessions/${sessionId}/pause`);
  },

  async resumeInterview(sessionId: string): Promise<void> {
    await apiClient.post(`/mock-interview/sessions/${sessionId}/resume`);
  },

  async getProgress(sessionId: string): Promise<{
    sessionId: string;
    state: string;
    currentRound: number;
    totalPlans: number;
    completedPlans: number;
    pendingPlans: number;
    progressPercentage: number;
    pausedAt: string | null;
    resumedAt: string | null;
  }> {
    const response = await apiClient.get(`/mock-interview/sessions/${sessionId}/progress`);
    return response.data;
  },

  async getEvaluations(sessionId: string): Promise<MockInterviewEvaluation[]> {
    const response = await apiClient.get<BackendMockInterviewEvaluation[]>(
      `/mock-interview/sessions/${sessionId}/evaluations`
    );
    return response.data.map(transformEvaluation);
  },

  async getReport(sessionId: string, session: MockInterviewSession, evaluations: MockInterviewEvaluation[]): Promise<InterviewReport> {
    return {
      sessionId: sessionId,
      totalScore: session.totalScore || 0,
      credibilityScore: session.credibilityScore || 0,
      evaluations: evaluations,
      credibilityAnalysis: [],
      suggestions: [],
      summary: '',
      createdAt: session.createdAt,
    };
  },

  async evaluateRound(
    sessionId: string,
    roundNumber: number,
    question: string,
    answer: string
  ): Promise<MockInterviewEvaluation> {
    const response = await apiClient.post<MockInterviewEvaluation>(
      `/mock-interview/sessions/${sessionId}/evaluate`,
      { roundNumber, question, answer }
    );
    return response.data;
  },

  async getUserSessions(): Promise<MockInterviewSession[]> {
    const response = await apiClient.get<BackendMockInterviewSession[]>(
      `/mock-interview/sessions/my`
    );
    const sessions = await Promise.all(
      response.data.map(async (s) => {
        const companyName = await getCompanyName(s.companyId);
        return transformSession(s, companyName);
      })
    );
    return sessions;
  },
};

export const resumeApi = {
  async createResume(resume: Partial<UserResume>): Promise<UserResume> {
    const response = await apiClient.post<UserResume>('/resumes', resume);
    return response.data;
  },

  async updateResume(resumeId: number, resume: Partial<UserResume>): Promise<void> {
    await apiClient.put(`/resumes/${resumeId}`, resume);
  },

  async getMyResumes(): Promise<UserResume[]> {
    const response = await apiClient.get<UserResume[]>('/resumes/my');
    return response.data;
  },

  async getMyDefaultResume(): Promise<UserResume> {
    const response = await apiClient.get<UserResume>('/resumes/my/default');
    return response.data;
  },

  async setDefaultResume(resumeId: number, userId: number): Promise<void> {
    await apiClient.put(`/resumes/${resumeId}/default`, null, {
      params: { userId }
    });
  },

  async deleteResume(resumeId: number): Promise<void> {
    await apiClient.delete(`/resumes/${resumeId}`);
  },

  async getProjects(resumeId: number): Promise<ResumeProject[]> {
    const response = await apiClient.get<ResumeProject[]>(`/resumes/${resumeId}/projects`);
    return response.data;
  },

  async getSkills(resumeId: number): Promise<ResumeSkill[]> {
    const response = await apiClient.get<ResumeSkill[]>(`/resumes/${resumeId}/skills`);
    return response.data;
  },
};
