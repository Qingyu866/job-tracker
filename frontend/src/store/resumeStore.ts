import { create } from 'zustand';
import { resumeApi } from '@/services/resumeApi';
import type { UserResume, CreateResumeRequest, UpdateResumeRequest } from '@/types/resume';

interface ResumeState {
  resumes: UserResume[];
  currentResume: UserResume | null;
  listLoading: boolean;
  detailLoading: boolean;
  saving: boolean;
  deleting: boolean;
  error: string | null;
  formData: Partial<CreateResumeRequest>;
  formDirty: boolean;
  currentStep: number;

  fetchResumes: (userId: number) => Promise<void>;
  fetchResumeDetail: (resumeId: number) => Promise<void>;
  createResume: (data: CreateResumeRequest) => Promise<UserResume>;
  updateResume: (resumeId: number, data: UpdateResumeRequest) => Promise<void>;
  deleteResume: (resumeId: number) => Promise<void>;
  setDefaultResume: (resumeId: number) => Promise<void>;
  updateFormData: (data: Partial<CreateResumeRequest>) => void;
  setCurrentStep: (step: number) => void;
  resetForm: () => void;
  clearError: () => void;
}

const initialFormData: Partial<CreateResumeRequest> = {
  resumeName: '',
  workYears: undefined,
  currentPosition: '',
  summary: '',
  education: [],
  experience: [],
  projects: [],
  skills: [],
};

export const useResumeStore = create<ResumeState>((set) => ({
  resumes: [],
  currentResume: null,
  listLoading: false,
  detailLoading: false,
  saving: false,
  deleting: false,
  error: null,
  formData: { ...initialFormData },
  formDirty: false,
  currentStep: 0,

  fetchResumes: async (userId: number) => {
    set({ listLoading: true, error: null });
    try {
      const resumes = await resumeApi.getList(userId);
      set({ resumes, listLoading: false });
    } catch (error) {
      set({ 
        error: error instanceof Error ? error.message : '获取简历列表失败', 
        listLoading: false 
      });
    }
  },

  fetchResumeDetail: async (resumeId: number) => {
    set({ detailLoading: true, error: null });
    try {
      const resume = await resumeApi.getDetail(resumeId);
      set({ 
        currentResume: resume, 
        formData: resume,
        detailLoading: false 
      });
    } catch (error) {
      set({ 
        error: error instanceof Error ? error.message : '获取简历详情失败', 
        detailLoading: false 
      });
    }
  },

  createResume: async (data: CreateResumeRequest) => {
    set({ saving: true, error: null });
    try {
      const resume = await resumeApi.create(data);
      set((state) => ({
        resumes: [...state.resumes, resume],
        saving: false,
        formData: { ...initialFormData },
        formDirty: false,
        currentStep: 0,
      }));
      return resume;
    } catch (error) {
      set({ 
        error: error instanceof Error ? error.message : '创建简历失败', 
        saving: false 
      });
      throw error;
    }
  },

  updateResume: async (resumeId: number, data: UpdateResumeRequest) => {
    set({ saving: true, error: null });
    try {
      await resumeApi.update(resumeId, data);
      const updatedResume = await resumeApi.getDetail(resumeId);
      set((state) => ({
        resumes: state.resumes.map((r) => 
          r.resumeId === resumeId ? updatedResume : r
        ),
        currentResume: updatedResume,
        saving: false,
        formDirty: false,
      }));
    } catch (error) {
      set({ 
        error: error instanceof Error ? error.message : '更新简历失败', 
        saving: false 
      });
      throw error;
    }
  },

  deleteResume: async (resumeId: number) => {
    set({ deleting: true, error: null });
    try {
      await resumeApi.delete(resumeId);
      set((state) => ({
        resumes: state.resumes.filter((r) => r.resumeId !== resumeId),
        currentResume: state.currentResume?.resumeId === resumeId 
          ? null 
          : state.currentResume,
        deleting: false,
      }));
    } catch (error) {
      set({ 
        error: error instanceof Error ? error.message : '删除简历失败', 
        deleting: false 
      });
      throw error;
    }
  },

  setDefaultResume: async (resumeId: number) => {
    set({ error: null });
    try {
      await resumeApi.setDefault(resumeId);
      set((state) => ({
        resumes: state.resumes.map((r) => ({
          ...r,
          isDefault: r.resumeId === resumeId,
        })),
      }));
    } catch (error) {
      set({ 
        error: error instanceof Error ? error.message : '设置默认简历失败'
      });
      throw error;
    }
  },

  updateFormData: (data: Partial<CreateResumeRequest>) => {
    set((state) => ({
      formData: { ...state.formData, ...data },
      formDirty: true,
    }));
  },

  setCurrentStep: (step: number) => {
    set({ currentStep: step });
  },

  resetForm: () => {
    set({
      formData: { ...initialFormData },
      formDirty: false,
      currentStep: 0,
      currentResume: null,
    });
  },

  clearError: () => {
    set({ error: null });
  },
}));
