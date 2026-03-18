import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useResumeStore } from '@/store/resumeStore';
import { Button, Spinner } from '@/components/common';
import { Header } from '@/components/layout/Header';
import { ArrowLeft, Save } from 'lucide-react';
import { toast } from '@/store/toastStore';
import type { CreateResumeRequest, SkillItem } from '@/types/resume';

interface ReturnState {
  returnUrl?: string;
  returnPath?: string;
  applicationId?: number;
  companyName?: string;
  jobTitle?: string;
}

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

  return (
    <div className="h-screen flex flex-col">
      <Header />

      <div className="h-12 md:h-14 border-b flex items-center px-3 md:px-6 bg-paper-50">
        <button
          onClick={handleCancel}
          className="flex items-center gap-2 text-paper-600 hover:text-paper-800 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span className="text-sm font-medium">返回</span>
        </button>
        <h1 className="ml-4 text-paper-800 font-medium">创建简历</h1>
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
                      value={formData.resumeName}
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
                  <div className="space-y-3">
                    {formData.skills.map((skill, index) => (
                      <div
                        key={index}
                        className="flex items-center gap-3 p-3 bg-paper-50 rounded-lg"
                      >
                        <input
                          type="text"
                          value={skill.name}
                          onChange={(e) =>
                            handleSkillChange(index, 'name', e.target.value)
                          }
                          className="flex-1 px-3 py-2 border border-paper-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50"
                          placeholder="技能名称"
                        />
                        <select
                          value={skill.level}
                          onChange={(e) =>
                            handleSkillChange(index, 'level', e.target.value)
                          }
                          className="px-3 py-2 border border-paper-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-amber/50"
                        >
                          <option value="初级">初级</option>
                          <option value="中级">中级</option>
                          <option value="高级">高级</option>
                          <option value="专家">专家</option>
                        </select>
                        <button
                          type="button"
                          onClick={() => handleRemoveSkill(index)}
                          className="p-2 text-paper-400 hover:text-accent-red transition-colors"
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

              <div className="flex justify-end gap-3 pt-4 border-t border-paper-200">
                <Button type="button" variant="outline" onClick={handleCancel}>
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
