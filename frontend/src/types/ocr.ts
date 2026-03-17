export type OcrStatus = 'idle' | 'pending' | 'success' | 'failed';

export type OcrImageType = 'resume' | 'jd' | 'general';

export interface OcrResult {
  status: OcrStatus;
  text?: string;
  confidence?: number;
  error?: string;
  resumeData?: ResumeInfo;
  jdData?: JdInfo;
}

export interface ResumeInfo {
  name?: string;
  workYears?: number;
  currentPosition?: string;
  skills?: string[];
  projects?: ProjectInfo[];
  education?: EducationInfo[];
  experience?: WorkExperience[];
}

export interface ProjectInfo {
  name: string;
  role?: string;
  startDate?: string;
  endDate?: string;
  description?: string;
  technologies?: string[];
}

export interface EducationInfo {
  school: string;
  degree?: string;
  major?: string;
  startDate?: string;
  endDate?: string;
}

export interface WorkExperience {
  company: string;
  position?: string;
  startDate?: string;
  endDate?: string;
  description?: string;
}

export interface JdInfo {
  companyName?: string;
  jobTitle?: string;
  skills?: string[];
  requirements?: string[];
  responsibilities?: string[];
  salary?: string;
  location?: string;
}

export interface OcrRecord {
  id: number;
  imageData: string;
  type: OcrImageType;
  result: OcrResult;
  createdAt: string;
}

export interface OcrUploadResponse {
  imageId: number;
  publicUrl: string;
}
