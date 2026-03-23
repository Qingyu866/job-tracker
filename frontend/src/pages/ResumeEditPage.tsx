import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useResumeStore } from '@/store/resumeStore';
import { useUserStore } from '@/store/userStore';
import { Button, Spinner } from '@/components/common';
import { SkillSearch } from '@/components/skill/SkillSearch';
import { Header } from '@/components/layout/Header';
import { ArrowLeft, Save } from 'lucide-react';
import { toast } from '@/store/toastStore';
import type { UpdateResumeRequest, SkillItem, WorkExperienceItem, ProjectItem } from '@/types/resume';

export function ResumeEditPage() {
  const { resumeId } = useParams<{ resumeId: string }>();
  const navigate = useNavigate();
  const { currentResume, detailLoading, saving, fetchResumeDetail, updateResume, error } = useResumeStore();
  const { isLoggedIn } = useUserStore();

  const [formData, setFormData] = useState<UpdateResumeRequest>({});
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [newTechInputs, setNewTechInputs] = useState<Record<number, string>>({});

  const parseTechStack = (techStack: string | undefined): string[] => {
    if (!techStack) return [];
    try {
      const parsed = JSON.parse(techStack);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return techStack.split(',').map(t => t.trim()).filter(t => t);
    }
  };

  const stringifyTechStack = (techs: string[]): string => {
    return JSON.stringify(techs);
  };

  const handleAddTech = (projectIndex: number) => {
    const newTech = newTechInputs[projectIndex]?.trim();
    if (!newTech) return;

    const project = formData.projects?.[projectIndex];
    if (!project) return;

    const currentTechs = parseTechStack(project.techStack);
    if (currentTechs.includes(newTech)) {
      toast.error('该技术已存在');
      return;
    }

    const updatedTechs = [...currentTechs, newTech];
    handleProjectChange(projectIndex, 'techStack', stringifyTechStack(updatedTechs));
    setNewTechInputs(prev => ({ ...prev, [projectIndex]: '' }));
  };

  const handleRemoveTech = (projectIndex: number, techIndex: number) => {
    const project = formData.projects?.[projectIndex];
    if (!project) return;

    const currentTechs = parseTechStack(project.techStack);
    const updatedTechs = currentTechs.filter((_, i) => i !== techIndex);
    handleProjectChange(projectIndex, 'techStack', stringifyTechStack(updatedTechs));
  };

  useEffect(() => {
    if (!isLoggedIn) {
      // 保存当前路径，登录后重定向回来
      sessionStorage.setItem('redirect_after_login', window.location.href);
      navigate('/login');
      return;
    }

    if (resumeId) {
      fetchResumeDetail(parseInt(resumeId));
    }
  }, [resumeId, isLoggedIn, navigate]);

  useEffect(() => {
    if (currentResume) {
      setFormData({
        resumeName: currentResume.resumeName,
        workYears: currentResume.workYears,
        currentPosition: currentResume.currentPosition,
        summary: currentResume.summary,
        skills: currentResume.skills,
        workExperiences: currentResume.workExperiences,
        projects: currentResume.projects,
      });
    }
  }, [currentResume]);

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (formData.resumeName !== undefined) {
      if (!formData.resumeName.trim()) {
        newErrors.resumeName = '简历名称不能为空';
      } else if (formData.resumeName.length < 2) {
        newErrors.resumeName = '简历名称至少2个字符';
      } else if (formData.resumeName.length > 50) {
        newErrors.resumeName = '简历名称最多50个字符';
      }
    }

    if (formData.workYears !== undefined) {
      if (formData.workYears < 0) {
        newErrors.workYears = '工作年限不能小于0';
      } else if (formData.workYears > 50) {
        newErrors.workYears = '工作年限不能超过50';
      }
    }

    if (formData.currentPosition && formData.currentPosition.length > 100) {
      newErrors.currentPosition = '职位名称最多100个字符';
    }

    if (formData.summary && formData.summary.length > 500) {
      newErrors.summary = '个人简介最多500个字符';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate() || !resumeId) {
      return;
    }

    try {
      await updateResume(parseInt(resumeId), formData);
      toast.success('简历更新成功');
      navigate('/resumes');
    } catch (error) {
      toast.error('更新失败，请重试');
    }
  };

  const handleAddSkill = () => {
    setFormData((prev) => ({
      ...prev,
      skills: [...(prev.skills || []), { skillId: undefined, skillName: '', proficiencyLevel: 'INTERMEDIATE' }],
    }));
  };

  const handleSkillSelect = (index: number, skillId: number, skillName: string) => {
    setFormData((prev) => ({
      ...prev,
      skills: prev.skills?.map((skill, i) =>
        i === index ? { ...skill, skillId, skillName } : skill
      ),
    }));
  };

  const handleRemoveSkill = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      skills: prev.skills?.filter((_, i) => i !== index),
    }));
  };

  const handleSkillChange = (index: number, field: keyof SkillItem, value: string | number) => {
    setFormData((prev) => ({
      ...prev,
      skills: prev.skills?.map((skill, i) =>
        i === index ? { ...skill, [field]: value } : skill
      ),
    }));
  };

  const handleAddWorkExperience = () => {
    setFormData((prev) => ({
      ...prev,
      workExperiences: [...(prev.workExperiences || []), {
        companyName: '',
        position: '',
        startDate: new Date().toISOString().split('T')[0],
        endDate: undefined,
        isCurrent: false,
        description: '',
        achievements: ''
      }]
    }));
  };

  const handleRemoveWorkExperience = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      workExperiences: prev.workExperiences?.filter((_, i) => i !== index),
    }));
  };

  const handleWorkExperienceChange = (index: number, field: keyof WorkExperienceItem, value: string | boolean | number | undefined) => {
    setFormData((prev) => ({
      ...prev,
      workExperiences: prev.workExperiences?.map((exp, i) =>
        i === index ? { ...exp, [field]: value } : exp
      ),
    }));
  };

  const handleAddProject = () => {
    setFormData((prev) => ({
      ...prev,
      projects: [...(prev.projects || []), {
        projectName: '',
        role: '',
        startDate: new Date().toISOString().split('T')[0],
        endDate: undefined,
        isOngoing: false,
        description: '',
        responsibilities: '',
        achievements: '',
        techStack: '',
        projectScale: '',
        performanceMetrics: '',
        displayOrder: prev.projects?.length || 0
      }]
    }));
  };

  const handleRemoveProject = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      projects: prev.projects?.filter((_, i) => i !== index),
    }));
  };

  const handleProjectChange = (index: number, field: keyof ProjectItem, value: string | boolean | number | undefined) => {
    setFormData((prev) => ({
      ...prev,
      projects: prev.projects?.map((project, i) =>
        i === index ? { ...project, [field]: value } : project
      ),
    }));
  };

  if (detailLoading) {
    return (
      <div className="h-screen flex flex-col">
        <Header />
        <div className="flex-1 flex items-center justify-center">
          <Spinner size="lg" />
        </div>
      </div>
    );
  }

  if (!currentResume && !detailLoading) {
    return (
      <div className="h-screen flex flex-col">
        <Header />
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center">
            {!isLoggedIn ? (
              <>
                <p className="text-paper-500 mb-4">请先登录</p>
                <Button onClick={() => navigate('/login')}>去登录</Button>
              </>
            ) : (
              <>
                <p className="text-paper-500 mb-4">{error || '简历不存在'}</p>
                <Button onClick={() => navigate('/resumes')}>返回列表</Button>
              </>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="h-screen flex flex-col">
      <Header />

      <div className="h-12 md:h-14 border-b flex items-center px-3 md:px-6 bg-paper-50">
        <button
          onClick={() => navigate('/resumes')}
          className="flex items-center gap-2 text-paper-600 hover:text-paper-800 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span className="text-sm font-medium">返回</span>
        </button>
        <h1 className="ml-4 text-paper-800 font-medium">编辑简历</h1>
      </div>

      <main className="flex-1 overflow-auto bg-paper-50">
        <div className="max-w-3xl mx-auto px-4 py-6">
          <form onSubmit={handleSubmit}>
            <div className="bg-white rounded-xl border-2 border-paper-300 p-6 space-y-6">
              <div>
                <h2 className="text-paper-800 font-medium mb-4">基本信息</h2>

                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-paper-700 mb-1">
                      简历名称 <span className="text-accent-red">*</span>
                    </label>
                    <input
                      type="text"
                      value={formData.resumeName || ''}
                      onChange={(e) =>
                        setFormData({ ...formData, resumeName: e.target.value })
                      }
                      className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50 ${
                        errors.resumeName ? 'border-accent-red' : 'border-paper-300'
                      }`}
                      placeholder="例如：前端开发工程师简历"
                    />
                    {errors.resumeName && (
                      <p className="mt-1 text-sm text-accent-red">{errors.resumeName}</p>
                    )}
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-paper-700 mb-1">
                        工作年限
                      </label>
                      <input
                        type="number"
                        min="0"
                        max="50"
                        value={formData.workYears ?? ''}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            workYears: e.target.value ? parseInt(e.target.value) : undefined,
                          })
                        }
                        className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50 ${
                          errors.workYears ? 'border-accent-red' : 'border-paper-300'
                        }`}
                        placeholder="例如：3"
                      />
                      {errors.workYears && (
                        <p className="mt-1 text-sm text-accent-red">{errors.workYears}</p>
                      )}
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-paper-700 mb-1">
                        当前职位
                      </label>
                      <input
                        type="text"
                        value={formData.currentPosition || ''}
                        onChange={(e) =>
                          setFormData({ ...formData, currentPosition: e.target.value })
                        }
                        className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50 ${
                          errors.currentPosition ? 'border-accent-red' : 'border-paper-300'
                        }`}
                        placeholder="例如：高级前端工程师"
                      />
                      {errors.currentPosition && (
                        <p className="mt-1 text-sm text-accent-red">{errors.currentPosition}</p>
                      )}
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-paper-700 mb-1">
                      个人简介
                    </label>
                    <textarea
                      value={formData.summary || ''}
                      onChange={(e) =>
                        setFormData({ ...formData, summary: e.target.value })
                      }
                      rows={4}
                      className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50 resize-none ${
                        errors.summary ? 'border-accent-red' : 'border-paper-300'
                      }`}
                      placeholder="简要介绍自己的技术背景、项目经验和职业目标..."
                    />
                    <div className="flex justify-between mt-1">
                      {errors.summary && (
                        <p className="text-sm text-accent-red">{errors.summary}</p>
                      )}
                      <p className="text-xs text-paper-400 ml-auto">
                        {(formData.summary || '').length}/500
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="border-t border-paper-200 pt-6">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-paper-800 font-medium">技能特长</h2>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={handleAddSkill}
                  >
                    添加技能
                  </Button>
                </div>

                {formData.skills && formData.skills.length > 0 ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {formData.skills.map((skill, index) => (
                      <div
                        key={index}
                        className="flex flex-col gap-2 p-3 bg-paper-50 rounded-lg border border-paper-300"
                      >
                        <div>
                          <label className="block text-sm font-medium text-paper-700 mb-1">
                            技能名称
                          </label>
                          <SkillSearch
                            value={skill.skillId || null}
                            onChange={(skillId, skillName) => handleSkillSelect(index, skillId, skillName)}
                            placeholder="搜索并选择技能"
                          />
                        </div>
                        <div>
                          <label className="block text-sm font-medium text-paper-700 mb-1">
                            熟练度
                          </label>
                          <select
                            value={skill.proficiencyLevel}
                            onChange={(e) =>
                              handleSkillChange(index, 'proficiencyLevel', e.target.value)
                            }
                            className="w-full px-2 py-1.5 border border-paper-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50 text-sm"
                          >
                            <option value="BEGINNER">初级</option>
                            <option value="INTERMEDIATE">中级</option>
                            <option value="ADVANCED">高级</option>
                            <option value="EXPERT">专家</option>
                          </select>
                        </div>
                        <div className="flex justify-end">
                          <button
                            type="button"
                            onClick={() => handleRemoveSkill(index)}
                            className="px-3 py-1 text-sm text-accent-red hover:bg-accent-red/10 rounded-lg transition-colors"
                          >
                            删除
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-paper-400 text-sm">暂无技能，点击上方按钮添加</p>
                )}
              </div>

              <div className="border-t border-paper-200 pt-6">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-paper-800 font-medium">工作经历</h2>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={handleAddWorkExperience}
                  >
                    添加工作经历
                  </Button>
                </div>

                {formData.workExperiences && formData.workExperiences.length > 0 ? (
                  <div className="space-y-3">
                    {formData.workExperiences.map((exp, index) => (
                      <div key={index} className="p-3 bg-paper-50 rounded-lg">
                        <div className="grid grid-cols-2 gap-4 mb-3">
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">公司名称</label>
                            <input
                              type="text"
                              value={exp.companyName}
                              onChange={(e) => handleWorkExperienceChange(index, 'companyName', e.target.value)}
                              className="w-full px-3 py-2 border border-paper-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50"
                              placeholder="公司名称"
                            />
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">职位</label>
                            <input
                              type="text"
                              value={exp.position}
                              onChange={(e) => handleWorkExperienceChange(index, 'position', e.target.value)}
                              className="w-full px-3 py-2 border border-paper-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50"
                              placeholder="职位"
                            />
                          </div>
                        </div>
                        <div className="grid grid-cols-2 gap-4 mb-3">
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">开始日期</label>
                            <input
                              type="date"
                              value={exp.startDate}
                              onChange={(e) => handleWorkExperienceChange(index, 'startDate', e.target.value)}
                              className="w-full px-3 py-2 border border-paper-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50"
                            />
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">结束日期</label>
                            <input
                              type="date"
                              value={exp.endDate || ''}
                              onChange={(e) => handleWorkExperienceChange(index, 'endDate', e.target.value || undefined)}
                              className="w-full px-3 py-2 border border-paper-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50"
                            />
                          </div>
                        </div>
                        <div className="flex items-center mb-3">
                          <input
                            type="checkbox"
                            checked={exp.isCurrent || false}
                            onChange={(e) => handleWorkExperienceChange(index, 'isCurrent', e.target.checked)}
                            className="mr-2"
                          />
                          <label className="text-sm text-paper-700">当前公司</label>
                        </div>
                        <div className="mb-3">
                          <label className="block text-sm font-medium text-paper-700 mb-1">工作描述</label>
                          <textarea
                            value={exp.description || ''}
                            onChange={(e) => handleWorkExperienceChange(index, 'description', e.target.value)}
                            rows={2}
                            className="w-full px-3 py-2 border border-paper-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50 resize-none"
                            placeholder="工作描述"
                          />
                        </div>
                        <div className="flex justify-end">
                          <button
                            type="button"
                            onClick={() => handleRemoveWorkExperience(index)}
                            className="px-3 py-1 text-sm text-accent-red hover:bg-accent-red/10 rounded-lg transition-colors"
                          >
                            删除
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-paper-400 text-sm">暂无工作经历，点击上方按钮添加</p>
                )}
              </div>

              <div className="border-t border-paper-200 pt-6">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-paper-800 font-medium">项目经历</h2>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={handleAddProject}
                  >
                    添加项目经历
                  </Button>
                </div>

                {formData.projects && formData.projects.length > 0 ? (
                  <div className="space-y-3">
                    {formData.projects.map((project, index) => (
                      <div key={index} className="p-3 bg-paper-50 rounded-lg">
                        <div className="grid grid-cols-2 gap-4 mb-3">
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">项目名称</label>
                            <input
                              type="text"
                              value={project.projectName}
                              onChange={(e) => handleProjectChange(index, 'projectName', e.target.value)}
                              className="w-full px-3 py-2 border border-paper-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50"
                              placeholder="项目名称"
                            />
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">担任角色</label>
                            <input
                              type="text"
                              value={project.role || ''}
                              onChange={(e) => handleProjectChange(index, 'role', e.target.value)}
                              className="w-full px-3 py-2 border border-paper-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50"
                              placeholder="担任角色"
                            />
                          </div>
                        </div>
                        <div className="grid grid-cols-2 gap-4 mb-3">
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">开始日期</label>
                            <input
                              type="date"
                              value={project.startDate}
                              onChange={(e) => handleProjectChange(index, 'startDate', e.target.value)}
                              className="w-full px-3 py-2 border border-paper-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50"
                            />
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">结束日期</label>
                            <input
                              type="date"
                              value={project.endDate || ''}
                              onChange={(e) => handleProjectChange(index, 'endDate', e.target.value || undefined)}
                              className="w-full px-3 py-2 border border-paper-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50"
                            />
                          </div>
                        </div>
                        <div className="flex items-center mb-3">
                          <input
                            type="checkbox"
                            checked={project.isOngoing || false}
                            onChange={(e) => handleProjectChange(index, 'isOngoing', e.target.checked)}
                            className="mr-2"
                          />
                          <label className="text-sm text-paper-700">进行中</label>
                        </div>
                        <div className="mb-3">
                          <label className="block text-sm font-medium text-paper-700 mb-1">项目描述</label>
                          <textarea
                            value={project.description || ''}
                            onChange={(e) => handleProjectChange(index, 'description', e.target.value)}
                            rows={2}
                            className="w-full px-3 py-2 border border-paper-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50 resize-none"
                            placeholder="项目描述"
                          />
                        </div>
                        <div className="mb-3">
                          <label className="block text-sm font-medium text-paper-700 mb-1">技术栈</label>
                          <div className="flex flex-wrap gap-2 mb-2">
                            {parseTechStack(project.techStack).map((tech, techIndex) => (
                              <span
                                key={techIndex}
                                className="inline-flex items-center gap-1 px-2 py-1 bg-accent-amber/20 text-paper-700 rounded-md text-sm"
                              >
                                {tech}
                                <button
                                  type="button"
                                  onClick={() => handleRemoveTech(index, techIndex)}
                                  className="text-paper-500 hover:text-accent-red transition-colors"
                                >
                                  ×
                                </button>
                              </span>
                            ))}
                          </div>
                          <div className="flex gap-2">
                            <input
                              type="text"
                              value={newTechInputs[index] || ''}
                              onChange={(e) => setNewTechInputs(prev => ({ ...prev, [index]: e.target.value }))}
                              onKeyPress={(e) => {
                                if (e.key === 'Enter') {
                                  e.preventDefault();
                                  handleAddTech(index);
                                }
                              }}
                              className="flex-1 px-3 py-2 border border-paper-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50 text-sm"
                              placeholder="输入技术名称，按回车添加"
                            />
                            <button
                              type="button"
                              onClick={() => handleAddTech(index)}
                              className="px-3 py-2 bg-accent-amber text-paper-800 rounded-lg hover:bg-accent-amber/90 transition-colors text-sm font-medium"
                            >
                              添加
                            </button>
                          </div>
                        </div>
                        <div className="flex justify-end">
                          <button
                            type="button"
                            onClick={() => handleRemoveProject(index)}
                            className="px-3 py-1 text-sm text-accent-red hover:bg-accent-red/10 rounded-lg transition-colors"
                          >
                            删除
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-paper-400 text-sm">暂无项目经历，点击上方按钮添加</p>
                )}
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-paper-200">
                <Button type="button" variant="outline" onClick={() => navigate('/resumes')}>
                  取消
                </Button>
                <Button type="submit" disabled={saving} leftIcon={<Save className="w-4 h-4" />}>
                  {saving ? '保存中...' : '保存'}
                </Button>
              </div>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
