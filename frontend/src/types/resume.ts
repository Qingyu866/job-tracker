export interface EducationItem {
  id?: number;
  school: string;
  major?: string;
  degree?: string;
  startDate?: string;
  endDate?: string;
}

export interface ExperienceItem {
  id?: number;
  company: string;
  position: string;
  startDate?: string;
  endDate?: string;
  description?: string;
}

export interface ProjectItem {
  id?: number;
  name: string;
  role?: string;
  startDate?: string;
  endDate?: string;
  description?: string;
  technologies?: string[];
}

export interface SkillItem {
  id?: number;
  name: string;
  level: '初级' | '中级' | '高级' | '专家';
  yearsOfExperience?: number;
}

export interface UserResume {
  resumeId: number;
  userId: number;
  resumeName: string;
  workYears?: number;
  currentPosition?: string;
  summary?: string;
  education?: EducationItem[];
  experience?: ExperienceItem[];
  projects?: ProjectItem[];
  skills?: SkillItem[];
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateResumeRequest {
  resumeName: string;
  workYears?: number;
  currentPosition?: string;
  summary?: string;
  education?: EducationItem[];
  experience?: ExperienceItem[];
  projects?: ProjectItem[];
  skills?: SkillItem[];
}

export interface UpdateResumeRequest extends Partial<CreateResumeRequest> {}
