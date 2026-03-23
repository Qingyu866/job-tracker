import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useResumeStore } from '@/store/resumeStore';
import { Button } from '@/components/common';
import { Header } from '@/components/layout/Header';
import { ImageDropZone, OcrResultPreview } from '@/components/ocr';
import { SkillSearch } from '@/components/skill/SkillSearch';
import { ArrowLeft, Save, Wand2, PanelLeft, PanelRight, ChevronDown, ChevronUp } from 'lucide-react';
import { toast } from '@/store/toastStore';
import { ocrApi } from '@/services/ocrApi';
import type { CreateResumeRequest, SkillItem } from '@/types/resume';
import type { ResumeInfo, OcrResult } from '@/types/ocr';

interface ReturnState {
  returnUrl?: string;
  returnPath?: string;
  applicationId?: number;
  companyName?: string;
  jobTitle?: string;
}

type OcrPanelPosition = 'left' | 'right';

export function ResumeCreatePage() {
  const navigate = useNavigate();
  const location = useLocation();
  const returnState = (location.state as ReturnState) || {};
  const { saving, createCompleteResume } = useResumeStore();
  
  const returnPath = returnState.returnUrl || returnState.returnPath;

  const [formData, setFormData] = useState<CreateResumeRequest>({
    resumeName: '',
    workYears: undefined,
    currentPosition: '',
    targetLevel: 'MIDDLE',
    summary: '',
    skills: [],
    workExperiences: [],
    projects: [],
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [ocrResult, setOcrResult] = useState<OcrResult | null>(null);
  const [ocrUploading, setOcrUploading] = useState(false);
  const [showOcrPanel, setShowOcrPanel] = useState(true);
  const [ocrPanelPosition, setOcrPanelPosition] = useState<OcrPanelPosition>('right');
  const [ocrPanelCollapsed, setOcrPanelCollapsed] = useState(false);
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
    setFormData(prev => ({
      ...prev,
      projects: prev.projects?.map((p, i) =>
        i === projectIndex ? { ...p, techStack: stringifyTechStack(updatedTechs) } : p
      )
    }));
    setNewTechInputs(prev => ({ ...prev, [projectIndex]: '' }));
  };

  const handleRemoveTech = (projectIndex: number, techIndex: number) => {
    const project = formData.projects?.[projectIndex];
    if (!project) return;

    const currentTechs = parseTechStack(project.techStack);
    const updatedTechs = currentTechs.filter((_, i) => i !== techIndex);
    setFormData(prev => ({
      ...prev,
      projects: prev.projects?.map((p, i) =>
        i === projectIndex ? { ...p, techStack: stringifyTechStack(updatedTechs) } : p
      )
    }));
  };

  const handleOcrUpload = async (file: File) => {
    setOcrUploading(true);
    setOcrResult({ status: 'pending' });

    try {
      const resumeInfo = await ocrApi.recognizeResume(file);
      setOcrResult({
        status: 'success',
        resumeData: resumeInfo,
        confidence: 0.85,
      });
      toast.success('简历识别成功');
    } catch (error) {
      console.error('OCR 识别失败:', error);
      setOcrResult({
        status: 'failed',
        error: error instanceof Error ? error.message : '识别失败，请重试',
      });
      toast.error('简历识别失败');
    } finally {
      setOcrUploading(false);
    }
  };

  const handleOcrConfirm = (data: ResumeInfo | string) => {
    if (typeof data === 'string') return;
    
    const resumeInfo = data as ResumeInfo;
    const newFormData: CreateResumeRequest = { ...formData };

    if (resumeInfo.name && !formData.resumeName) {
      newFormData.resumeName = resumeInfo.name;
    }
    if (resumeInfo.workYears !== undefined && formData.workYears === undefined) {
      newFormData.workYears = resumeInfo.workYears;
    }
    if (resumeInfo.currentPosition && !formData.currentPosition) {
      newFormData.currentPosition = resumeInfo.currentPosition;
    }

    if (resumeInfo.skills && resumeInfo.skills.length > 0 && (!formData.skills || formData.skills.length === 0)) {
      newFormData.skills = resumeInfo.skills.map(skill => ({
        skillName: skill,
        proficiencyLevel: 'INTERMEDIATE' as const,
      }));
    }

    setFormData(newFormData);
    setOcrResult(null);
    toast.success('已填充识别结果，请检查并完善');
  };

  const handleOcrRetry = () => {
    setOcrResult(null);
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.resumeName.trim()) {
      newErrors.resumeName = '简历名称不能为空';
    } else if (formData.resumeName.length < 2) {
      newErrors.resumeName = '简历名称至少2个字符';
    } else if (formData.resumeName.length > 50) {
      newErrors.resumeName = '简历名称最多50个字符';
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

    if (!validate()) {
      return;
    }

    try {
      await createCompleteResume({
        resume: formData,
        skills: formData.skills || [],
        projects: formData.projects || [],
        workExperiences: formData.workExperiences || []
      });
      toast.success('简历创建成功');

      if (returnPath) {
        navigate(returnPath, { replace: true });
      } else {
        navigate('/resumes', { replace: true });
      }
    } catch (error) {
      toast.error('创建失败，请重试');
    }
  };

  const handleAddSkill = () => {
    setFormData((prev) => ({
      ...prev,
      skills: [...(prev.skills || []), { skillId: undefined, skillName: '', proficiencyLevel: 'INTERMEDIATE' }],
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

  const handleSkillSelect = (index: number, skillId: number, skillName: string) => {
    setFormData((prev) => ({
      ...prev,
      skills: prev.skills?.map((skill, i) =>
        i === index ? { ...skill, skillId, skillName } : skill
      ),
    }));
  };

  const handleCancel = () => {
    if (returnPath) {
      navigate(returnPath, { replace: true });
    } else {
      navigate('/resumes');
    }
  };

  const togglePanelPosition = () => {
    setOcrPanelPosition(prev => prev === 'left' ? 'right' : 'left');
  };

  const renderOcrPanel = () => (
    <div className={`
      w-full lg:w-72 flex-shrink-0
      ${ocrPanelCollapsed ? 'lg:w-12' : 'lg:w-72'}
      transition-all duration-300
    `}>
      <div className="rounded-xl border-2 border-paper-400 bg-paper-50 overflow-hidden max-h-[400px] flex flex-col">
        <div className="flex items-center justify-between p-3 border-b-2 border-paper-300 bg-paper-100">
          {!ocrPanelCollapsed && (
            <div className="flex items-center gap-2">
              <Wand2 className="w-4 h-4 text-accent-amber" />
              <span className="text-paper-800 font-medium text-sm">智能识别</span>
            </div>
          )}
          <div className="flex items-center gap-1">
            {!ocrPanelCollapsed && (
              <button
                type="button"
                onClick={togglePanelPosition}
                className="p-1.5 hover:bg-paper-200 rounded-lg text-paper-500 transition-colors"
                title={ocrPanelPosition === 'left' ? '移到右侧' : '移到左侧'}
              >
                {ocrPanelPosition === 'left' ? (
                  <PanelRight className="w-4 h-4" />
                ) : (
                  <PanelLeft className="w-4 h-4" />
                )}
              </button>
            )}
            <button
              type="button"
              onClick={() => setOcrPanelCollapsed(!ocrPanelCollapsed)}
              className="p-1.5 hover:bg-paper-200 rounded-lg text-paper-500 transition-colors"
            >
              {ocrPanelCollapsed ? (
                <ChevronDown className="w-4 h-4" />
              ) : (
                <ChevronUp className="w-4 h-4" />
              )}
            </button>
          </div>
        </div>

        {!ocrPanelCollapsed && (
          <div className="p-3 flex-1 overflow-y-auto">
            <p className="text-paper-500 text-xs mb-3">
              上传简历图片，AI 自动填充表单
            </p>

            {ocrResult ? (
              <OcrResultPreview
                result={ocrResult}
                mode="resume"
                onConfirm={handleOcrConfirm}
                onRetry={handleOcrRetry}
              />
            ) : (
              <ImageDropZone
                onUpload={handleOcrUpload}
                disabled={ocrUploading}
                compact
              />
            )}

            <div className="mt-3 text-center">
              <button
                type="button"
                onClick={() => setShowOcrPanel(false)}
                className="text-paper-400 text-xs hover:text-paper-600 underline"
              >
                跳过识别
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );

  return (
    <div className="h-screen flex flex-col">
      <Header />

      <div className="h-12 md:h-14 border-b-2 border-paper-300 flex items-center px-3 md:px-6 bg-[#f5f0e6]">
        <button
          onClick={handleCancel}
          className="flex items-center gap-2 text-paper-600 hover:text-paper-800 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span className="text-sm font-medium">返回</span>
        </button>
        <h1 className="ml-4 text-paper-800 font-medium">创建简历</h1>
        <div className="ml-auto flex items-center gap-2">
          {!showOcrPanel && (
            <button
              type="button"
              onClick={() => setShowOcrPanel(true)}
              className="flex items-center gap-1.5 px-3 py-1.5 text-sm text-accent-amber hover:bg-accent-amber/10 rounded-lg transition-colors border border-accent-amber/30"
            >
              <Wand2 className="w-4 h-4" />
              智能识别
            </button>
          )}
        </div>
      </div>

      <main className="flex-1 overflow-auto bg-[#f5f0e6]">
        <div className="max-w-4xl mx-auto px-4 py-6">
          <div className="flex flex-col lg:flex-row gap-4">
            {showOcrPanel && ocrPanelPosition === 'left' && renderOcrPanel()}

            <form onSubmit={handleSubmit} className="flex-1 min-w-0">
              <div className="bg-[#f5f0e6] rounded-xl border-2 border-paper-400 p-5 space-y-5">
              <div>
                <h2 className="text-paper-800 font-medium mb-3">基本信息</h2>

                <div className="space-y-3">
                  <div>
                    <label className="block text-sm font-medium text-paper-700 mb-1">
                      简历名称 <span className="text-accent-red">*</span>
                    </label>
                    <input
                      type="text"
                      value={formData.resumeName}
                      onChange={(e) =>
                        setFormData({ ...formData, resumeName: e.target.value })
                      }
                      className={`w-full px-3 py-2 border-2 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 ${
                          errors.resumeName ? 'border-accent-red' : 'border-paper-400'
                        }`}
                      placeholder="例如：前端开发工程师简历"
                    />
                    {errors.resumeName && (
                      <p className="mt-1 text-sm text-accent-red">{errors.resumeName}</p>
                    )}
                  </div>

                  <div className="grid grid-cols-2 gap-3">
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
                        className={`w-full px-3 py-2 border-2 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 ${
                          errors.workYears ? 'border-accent-red' : 'border-paper-400'
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
                        className={`w-full px-3 py-2 border-2 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 ${
                            errors.currentPosition ? 'border-accent-red' : 'border-paper-400'
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
                      rows={3}
                      className={`w-full px-3 py-2 border-2 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 resize-none ${
                          errors.summary ? 'border-accent-red' : 'border-paper-400'
                        }`}
                      placeholder="简要介绍自己的技术背景..."
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

              <div className="border-t-2 border-paper-200 pt-4">
                <div className="flex items-center justify-between mb-3">
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
                            className="w-full px-2 py-1.5 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 text-sm"
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

              <div className="border-t-2 border-paper-200 pt-4">
                <div className="flex items-center justify-between mb-3">
                  <h2 className="text-paper-800 font-medium">工作经历</h2>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => setFormData(prev => ({
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
                    }))}
                  >
                    添加工作经历
                  </Button>
                </div>

                {formData.workExperiences && formData.workExperiences.length > 0 ? (
                  <div className="space-y-3">
                    {formData.workExperiences.map((exp, index) => (
                      <div key={index} className="p-3 bg-paper-50 rounded-lg border border-paper-300">
                        <div className="grid grid-cols-2 gap-3 mb-2">
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">公司名称</label>
                            <input
                              type="text"
                              value={exp.companyName}
                              onChange={(event) => setFormData(prev => ({
                                ...prev,
                                workExperiences: prev.workExperiences?.map((exp, i) => 
                                  i === index ? { ...exp, companyName: event.target.value } : exp
                                )
                              }))}
                              className="w-full px-3 py-2 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 text-sm"
                              placeholder="公司名称"
                            />
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">职位</label>
                            <input
                              type="text"
                              value={exp.position}
                              onChange={(event) => setFormData(prev => ({
                                ...prev,
                                workExperiences: prev.workExperiences?.map((exp, i) => 
                                  i === index ? { ...exp, position: event.target.value } : exp
                                )
                              }))}
                              className="w-full px-3 py-2 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 text-sm"
                              placeholder="职位"
                            />
                          </div>
                        </div>
                        <div className="grid grid-cols-2 gap-3 mb-2">
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">开始日期</label>
                            <input
                              type="date"
                              value={exp.startDate}
                              onChange={(event) => setFormData(prev => ({
                                ...prev,
                                workExperiences: prev.workExperiences?.map((exp, i) => 
                                  i === index ? { ...exp, startDate: event.target.value } : exp
                                )
                              }))}
                              className="w-full px-3 py-2 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 text-sm"
                            />
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">结束日期</label>
                            <input
                              type="date"
                              value={exp.endDate || ''}
                              onChange={(event) => setFormData(prev => ({
                                ...prev,
                                workExperiences: prev.workExperiences?.map((exp, i) => 
                                  i === index ? { ...exp, endDate: event.target.value || undefined } : exp
                                )
                              }))}
                              className="w-full px-3 py-2 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 text-sm"
                            />
                          </div>
                        </div>
                        <div className="flex items-center mb-2">
                          <input
                            type="checkbox"
                            checked={exp.isCurrent || false}
                            onChange={(event) => setFormData(prev => ({
                              ...prev,
                              workExperiences: prev.workExperiences?.map((exp, i) => 
                                i === index ? { ...exp, isCurrent: event.target.checked } : exp
                              )
                            }))}
                            className="mr-2"
                          />
                          <label className="text-sm text-paper-700">当前公司</label>
                        </div>
                        <div className="mb-2">
                          <label className="block text-sm font-medium text-paper-700 mb-1">工作描述</label>
                          <textarea
                            value={exp.description || ''}
                            onChange={(event) => setFormData(prev => ({
                              ...prev,
                              workExperiences: prev.workExperiences?.map((exp, i) => 
                                i === index ? { ...exp, description: event.target.value } : exp
                              )
                            }))}
                            rows={2}
                            className="w-full px-3 py-2 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 text-sm resize-none"
                            placeholder="工作描述"
                          />
                        </div>
                        <div className="flex justify-end">
                          <button
                            type="button"
                            onClick={() => setFormData(prev => ({
                              ...prev,
                              workExperiences: prev.workExperiences?.filter((_, i) => i !== index)
                            }))}
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

              <div className="border-t-2 border-paper-200 pt-4">
                <div className="flex items-center justify-between mb-3">
                  <h2 className="text-paper-800 font-medium">项目经历</h2>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => setFormData(prev => ({
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
                    }))}
                  >
                    添加项目经历
                  </Button>
                </div>

                {formData.projects && formData.projects.length > 0 ? (
                  <div className="space-y-3">
                    {formData.projects.map((project, index) => (
                      <div key={index} className="p-3 bg-paper-50 rounded-lg border border-paper-300">
                        <div className="grid grid-cols-2 gap-3 mb-2">
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">项目名称</label>
                            <input
                              type="text"
                              value={project.projectName}
                              onChange={(e) => setFormData(prev => ({
                                ...prev,
                                projects: prev.projects?.map((p, i) => 
                                  i === index ? { ...p, projectName: e.target.value } : p
                                )
                              }))}
                              className="w-full px-3 py-2 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 text-sm"
                              placeholder="项目名称"
                            />
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">担任角色</label>
                            <input
                              type="text"
                              value={project.role || ''}
                              onChange={(e) => setFormData(prev => ({
                                ...prev,
                                projects: prev.projects?.map((p, i) => 
                                  i === index ? { ...p, role: e.target.value } : p
                                )
                              }))}
                              className="w-full px-3 py-2 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 text-sm"
                              placeholder="担任角色"
                            />
                          </div>
                        </div>
                        <div className="grid grid-cols-2 gap-3 mb-2">
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">开始日期</label>
                            <input
                              type="date"
                              value={project.startDate}
                              onChange={(e) => setFormData(prev => ({
                                ...prev,
                                projects: prev.projects?.map((p, i) => 
                                  i === index ? { ...p, startDate: e.target.value } : p
                                )
                              }))}
                              className="w-full px-3 py-2 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 text-sm"
                            />
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-paper-700 mb-1">结束日期</label>
                            <input
                              type="date"
                              value={project.endDate || ''}
                              onChange={(e) => setFormData(prev => ({
                                ...prev,
                                projects: prev.projects?.map((p, i) => 
                                  i === index ? { ...p, endDate: e.target.value || undefined } : p
                                )
                              }))}
                              className="w-full px-3 py-2 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 text-sm"
                            />
                          </div>
                        </div>
                        <div className="flex items-center mb-2">
                          <input
                            type="checkbox"
                            checked={project.isOngoing || false}
                            onChange={(e) => setFormData(prev => ({
                              ...prev,
                              projects: prev.projects?.map((p, i) => 
                                i === index ? { ...p, isOngoing: e.target.checked } : p
                              )
                            }))}
                            className="mr-2"
                          />
                          <label className="text-sm text-paper-700">进行中</label>
                        </div>
                        <div className="mb-2">
                          <label className="block text-sm font-medium text-paper-700 mb-1">项目描述</label>
                          <textarea
                            value={project.description || ''}
                            onChange={(e) => setFormData(prev => ({
                              ...prev,
                              projects: prev.projects?.map((p, i) => 
                                i === index ? { ...p, description: e.target.value } : p
                              )
                            }))}
                            rows={2}
                            className="w-full px-3 py-2 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 text-sm resize-none"
                            placeholder="项目描述"
                          />
                        </div>
                        <div className="mb-2">
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
                              className="flex-1 px-3 py-2 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 text-sm"
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
                            onClick={() => setFormData(prev => ({
                              ...prev,
                              projects: prev.projects?.filter((_, i) => i !== index)
                            }))}
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

              <div className="flex justify-end gap-3 pt-4 border-t-2 border-paper-200">
                <Button type="button" variant="outline" onClick={handleCancel}>
                  取消
                </Button>
                <Button type="submit" disabled={saving} leftIcon={<Save className="w-4 h-4" />}>
                  {saving ? '保存中...' : '保存'}
                </Button>
              </div>
            </div>
          </form>

          {showOcrPanel && ocrPanelPosition === 'right' && renderOcrPanel()}
        </div>
      </div>
      </main>
    </div>
  );
}
