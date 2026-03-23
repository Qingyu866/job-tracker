export interface EducationItem {
  id?: number;
  school: string;
  major?: string;
  degree?: string;
  startDate?: string;
  endDate?: string;
}

export interface EducationItem {
  id?: number;
  school: string;
  major?: string;
  degree?: string;
  startDate?: string;
  endDate?: string;
}

export interface WorkExperienceItem {
  id?: number;
  companyName: string;
  position: string;
  startDate: string;
  endDate?: string;
  isCurrent?: boolean;
  description?: string;
  achievements?: string;
}

export interface ProjectItem {
  projectId?: number;
  resumeId?: number;
  projectName: string;
  role?: string;
  startDate: string;
  endDate?: string;
  isOngoing?: boolean;
  description?: string;
  responsibilities?: string;
  achievements?: string;
  techStack?: string;
  createdAt?: string;
}

export interface SkillItem {
  id?: number;
  resumeId?: number;
  skillId?: number;
  proficiencyLevel: string;
  experienceYears?: number;
  isCoreSkill?: boolean;
  createdAt?: string;
}

export interface UserResume {
  resumeId: number;
  userId: number;
  resumeName: string;
  workYears?: number;
  currentPosition?: string;
  targetLevel?: string;
  summary?: string;
  isDefault: boolean;
  education?: EducationItem[];
  workExperiences?: WorkExperienceItem[];
  projects?: ProjectItem[];
  skills?: SkillItem[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateResumeRequest {
  resumeName: string;
  isDefault?: boolean;
  workYears?: number;
  currentPosition?: string;
  targetLevel?: string;
  summary?: string;
  education?: EducationItem[];
  workExperiences?: WorkExperienceItem[];
  projects?: ProjectItem[];
  skills?: SkillItem[];
}

export interface CreateCompleteResumeRequest {
  resume: CreateResumeRequest;
  skills: SkillItem[];
  projects: ProjectItem[];
  workExperiences: WorkExperienceItem[];
}

export interface UpdateResumeRequest extends Partial<CreateResumeRequest> {}
