import { useState } from 'react';
import { resumeApi } from '@/services/interviewApi';
import { toast } from '@/store/toastStore';
import type { UserResume } from '@/types/resume';

interface CreateResumeFormProps {
  onSuccess: (resume: UserResume) => void;
  onCancel: () => void;
  initialName?: string;
}

export function CreateResumeForm({ onSuccess, onCancel, initialName = '' }: CreateResumeFormProps) {
  const [formData, setFormData] = useState({
    resumeName: initialName || '我的简历',
    workYears: undefined as number | undefined,
    currentPosition: '',
    summary: '',
  });
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.resumeName.trim()) {
      toast.error('请输入简历名称');
      return;
    }

    setSubmitting(true);
    try {
      const userId = localStorage.getItem('userId');
      if (!userId) {
        toast.error('请先登录');
        return;
      }

      const resume = await resumeApi.createResume({
        resumeName: formData.resumeName,
        workYears: formData.workYears,
        currentPosition: formData.currentPosition,
        summary: formData.summary,
      });
      
      toast.success('简历创建成功');
      onSuccess(resume);
    } catch (error) {
      console.error('创建简历失败:', error);
      toast.error('创建简历失败');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="block text-sm font-semibold text-paper-800 mb-2">
          简历名称 <span className="text-accent-red">*</span>
        </label>
        <input
          type="text"
          required
          value={formData.resumeName}
          onChange={(e) => setFormData({ ...formData, resumeName: e.target.value })}
          placeholder="例如：前端开发简历"
          className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-semibold text-paper-800 mb-2">
            工作年限
          </label>
          <input
            type="number"
            value={formData.workYears ?? ''}
            onChange={(e) => setFormData({ ...formData, workYears: e.target.value ? Number(e.target.value) : undefined })}
            placeholder="例如：3"
            className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
          />
        </div>

        <div>
          <label className="block text-sm font-semibold text-paper-800 mb-2">
            当前职位
          </label>
          <input
            type="text"
            value={formData.currentPosition}
            onChange={(e) => setFormData({ ...formData, currentPosition: e.target.value })}
            placeholder="例如：前端工程师"
            className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
          />
        </div>
      </div>

      <div>
        <label className="block text-sm font-semibold text-paper-800 mb-2">
          个人简介
        </label>
        <textarea
          value={formData.summary}
          onChange={(e) => setFormData({ ...formData, summary: e.target.value })}
          placeholder="简要介绍自己的技术背景和经验..."
          rows={3}
          className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base resize-none"
        />
      </div>

      <div className="flex justify-end space-x-3 pt-4 border-t-2 border-paper-300">
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 text-paper-700 border-2 border-paper-400 hover:bg-paper-200 rounded-lg transition-colors text-sm font-medium"
        >
          取消
        </button>
        <button
          type="submit"
          disabled={submitting}
          className="px-4 py-2 bg-accent-amber text-paper-800 rounded-lg hover:bg-accent-amber/90 transition-colors text-sm font-medium border-2 border-amber-500 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {submitting ? '创建中...' : '创建简历'}
        </button>
      </div>
    </form>
  );
}
