export type InterviewState = 
  | 'CREATED'
  | 'INTRODUCTION'
  | 'TECHNICAL_QA'
  | 'PROJECT_QA'
  | 'BEHAVIORAL_QA'
  | 'CLOSING'
  | 'FINISHED';

export type InterviewRole = 'user' | 'assistant' | 'system';

export interface MockInterviewSession {
  sessionId: string;
  applicationId: number;
  resumeId: number;
  userId: number;
  companyName: string;
  jobTitle: string;
  state: InterviewState;
  currentRound: number;
  totalRounds: number;
  skillsCovered: SkillCovered[];
  skillsPending: SkillPending[];
  totalScore?: number;
  credibilityScore?: number;
  createdAt: string;
  updatedAt: string;
  finishedAt?: string;
}

export interface InterviewMessage {
  id: string;
  sessionId: string;
  role: InterviewRole;
  content: string;
  roundNumber?: number;
  skillTag?: string;
  createdAt: string;
}

export interface SkillCovered {
  id: string;
  name: string;
  score?: number;
  roundNumber: number;
}

export interface SkillPending {
  id: string;
  name: string;
  priority: number;
}

export interface MockInterviewEvaluation {
  id: number;
  sessionId: string;
  roundNumber: number;
  skillName: string;
  technicalScore: number;
  logicScore: number;
  depthScore: number;
  comment: string;
  improvement?: string;
  createdAt: string;
}

export interface CredibilityAnalysisItem {
  skillName: string;
  claimedLevel: string;
  actualLevel: string;
  exaggerationLevel: 'high' | 'medium' | 'low' | 'none';
  comment?: string;
}

export interface ImprovementSuggestion {
  id: string;
  category: string;
  title: string;
  description: string;
  resources?: string[];
  priority: 'high' | 'medium' | 'low';
}

export interface InterviewReport {
  sessionId: string;
  totalScore: number;
  credibilityScore: number;
  evaluations: MockInterviewEvaluation[];
  credibilityAnalysis: CredibilityAnalysisItem[];
  suggestions: ImprovementSuggestion[];
  summary: string;
  createdAt: string;
}

export interface UserResume {
  id: number;
  userId: number;
  resumeName: string;
  workYears?: number;
  currentPosition?: string;
  skills?: string[];
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ResumeProject {
  id: number;
  resumeId: number;
  name: string;
  role?: string;
  startDate?: string;
  endDate?: string;
  description?: string;
  technologies?: string[];
}

export interface ResumeSkill {
  id: number;
  resumeId: number;
  name: string;
  level: string;
  yearsOfExperience?: number;
}
