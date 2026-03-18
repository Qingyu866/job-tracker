import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useResumeStore } from '@/store/resumeStore';
import { Button } from '@/components/common';
import { Header } from '@/components/layout/Header';
import { ImageDropZone, OcrResultPreview } from '@/components/ocr';
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
  const { saving, createResume } = useResumeStore();
  
  const returnPath = returnState.returnUrl || returnState.returnPath;

  const [formData, setFormData] = useState<CreateResumeRequest>({
    resumeName: '',
    workYears: undefined,
    currentPosition: '',
    summary: '',
    skills: [],
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [ocrResult, setOcrResult] = useState<OcrResult | null>(null);
  const [ocrUploading, setOcrUploading] = useState(false);
  const [showOcrPanel, setShowOcrPanel] = useState(true);
  const [ocrPanelPosition, setOcrPanelPosition] = useState<OcrPanelPosition>('right');
  const [ocrPanelCollapsed, setOcrPanelCollapsed] = useState(false);

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
        name: skill,
        level: '中级' as const,
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
      await createResume(formData);
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
      skills: [...(prev.skills || []), { name: '', level: '中级' }],
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
      <div className="rounded-xl border-2 border-paper-400 bg-paper-50 overflow-hidden h-full flex flex-col">
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
                ocrMode="resume"
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
                    <div className="space-y-2">
                      {formData.skills.map((skill, index) => (
                        <div
                          key={index}
                          className="flex items-center gap-2 p-2 bg-paper-50 rounded-lg border border-paper-300"
                        >
                          <input
                            type="text"
                            value={skill.name}
                            onChange={(e) =>
                              handleSkillChange(index, 'name', e.target.value)
                            }
                            className="flex-1 px-2 py-1.5 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 text-sm"
                            placeholder="技能名称"
                          />
                          <select
                            value={skill.level}
                            onChange={(e) =>
                              handleSkillChange(index, 'level', e.target.value)
                            }
                            className="px-2 py-1.5 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-500 text-sm"
                          >
                            <option value="初级">初级</option>
                            <option value="中级">中级</option>
                            <option value="高级">高级</option>
                            <option value="专家">专家</option>
                          </select>
                          <button
                            type="button"
                            onClick={() => handleRemoveSkill(index)}
                            className="p-1.5 text-paper-400 hover:text-accent-red transition-colors border border-paper-300 rounded-lg hover:border-accent-red"
                          >
                            ×
                          </button>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-paper-400 text-sm">暂无技能，点击上方按钮添加</p>
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
