import { apiClient } from '@/lib/apiClient';

export interface Skill {
  skillId: number;
  skillName: string;
  category: string;
  difficultyBase: number;
  parentId: number | null;
  description: string;
}

export interface CreateSkillRequest {
  skillName: string;
  category: string;
  difficultyBase: number;
  parentId?: number | null;
  description: string;
}

export const skillApi = {
  async getAllSkills(): Promise<Skill[]> {
    const response = await apiClient.get<Skill[]>('/skills');
    return response.data;
  },

  async getSkillsByCategory(category: string): Promise<Skill[]> {
    const response = await apiClient.get<Skill[]>(`/skills/category/${category}`);
    return response.data;
  },

  async searchSkills(keyword: string): Promise<Skill[]> {
    const response = await apiClient.get<Skill[]>('/skills/search', {
      params: { keyword }
    });
    return response.data;
  },

  async getSkillById(skillId: number): Promise<Skill> {
    const response = await apiClient.get<Skill>(`/skills/${skillId}`);
    return response.data;
  },

  async createSkill(data: CreateSkillRequest): Promise<Skill> {
    const response = await apiClient.post<Skill>('/skills', data);
    return response.data;
  },

  async updateSkill(skillId: number, data: CreateSkillRequest): Promise<string> {
    const response = await apiClient.put<string>(`/skills/${skillId}`, data);
    return response.data;
  },

  async deleteSkill(skillId: number): Promise<string> {
    const response = await apiClient.delete<string>(`/skills/${skillId}`);
    return response.data;
  },

  async getChildSkills(skillId: number): Promise<Skill[]> {
    const response = await apiClient.get<Skill[]>(`/skills/${skillId}/children`);
    return response.data;
  },
};
