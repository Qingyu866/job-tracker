// 通用类型
export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

// 申请类型
export interface JobApplication {
  id: number;
  companyId: number;
  jobTitle: string;
  jobDescription?: string;
  jobType?: string;
  workLocation?: string;
  salaryMin?: number;
  salaryMax?: number;
  salaryCurrency?: string;
  jobUrl?: string;
  status: 'WISHLIST' | 'APPLIED' | 'INTERVIEW' | 'OFFER' | 'REJECTED' | 'WITHDRAWN';
  applicationDate?: string;
  priority?: number;
  notes?: string;
  createdAt: string;
  updatedAt?: string;
  company?: Company;
}

// 公司类型
export interface Company {
  id: number;
  name: string;
  industry?: string;
  size?: string;
  location?: string;
  website?: string;
  description?: string;
  logoUrl?: string;
  createdAt: string;
  updatedAt?: string;
}

// 面试记录
export interface InterviewRecord {
  id: number;
  applicationId: number;
  interviewType?: string;
  interviewDate: string;
  interviewerName?: string;
  interviewerTitle?: string;
  durationMinutes?: number;
  status?: 'SCHEDULED' | 'INTERVIEW' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';
  rating?: number;
  feedback?: string;
  technicalQuestions?: string;
  notes?: string;
  followUpRequired?: boolean;
  createdAt: string;
  updatedAt?: string;
}

// 申请日志
export interface ApplicationLog {
  id: number;
  applicationId: number;
  logType: 'APPLICATION_CREATED' | 'APPLICATION_SUBMITTED' | 'STATUS_CHANGE' | 'INTERVIEW_SCHEDULED' | 'INTERVIEW_COMPLETED' | 'INTERVIEW_CANCELLED' | 'INTERVIEW_NO_SHOW' | 'FEEDBACK_RECEIVED' | 'NOTE_ADDED' | 'DOCUMENT_UPLOADED';
  logTitle: string;
  logContent: string;
  loggedBy: 'SYSTEM' | 'USER';
  createdAt: string;
}

// 申请日志DTO（包含申请和公司信息）
export interface ApplicationLogDTO {
  log: ApplicationLog;
  application: JobApplication;
  company?: Company;
}

// WebSocket 消息类型
export interface WebSocketMessage {
  type: 'CHAT' | 'STATUS' | 'ERROR';
  content: string;
  timestamp?: number;
}

// 视图类型
export type ViewType = 'table' | 'board' | 'timeline' | 'calendar';

// P2阶段：申请详情聚合
export interface ApplicationDetail {
  application: JobApplication;
  company: Company | null;
  interviews: InterviewRecord[];
  logs: ApplicationLog[];
  statistics: InterviewStatistics;
}

// 面试统计
export interface InterviewStatistics {
  total: number;
  completed: number;
  scheduled: number;
  averageRating: number;
}
