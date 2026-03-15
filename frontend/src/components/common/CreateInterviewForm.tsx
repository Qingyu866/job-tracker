import { useState } from 'react';
import { apiClient } from '@/lib/apiClient';
import type { InterviewRecord } from '@/types';
import { Calendar, Clock, User, Briefcase } from 'lucide-react';

interface CreateInterviewFormProps {
  applicationId: number;
  onSuccess: (interview: InterviewRecord) => void;
  onCancel: () => void;
}

export function CreateInterviewForm({ applicationId, onSuccess, onCancel }: CreateInterviewFormProps) {
  const [formData, setFormData] = useState({
    interviewType: '',
    interviewDate: '',
    interviewTime: '',
    durationMinutes: 60,
    interviewerName: '',
    interviewerTitle: '',
    notes: '',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      console.log('[CreateInterviewForm] 开始提交表单:', formData);
      
      // 组合日期和时间
      const interviewDateTime = formData.interviewDate && formData.interviewTime 
        ? `${formData.interviewDate}T${formData.interviewTime}`
        : formData.interviewDate;

      const interviewData = {
        applicationId,
        interviewType: formData.interviewType || undefined,
        interviewDate: interviewDateTime,
        durationMinutes: formData.durationMinutes || undefined,
        interviewerName: formData.interviewerName || undefined,
        interviewerTitle: formData.interviewerTitle || undefined,
        notes: formData.notes || undefined,
      };

      const response = await apiClient.post<number>('/interviews', interviewData);
      console.log('[CreateInterviewForm] 创建成功，返回ID:', response.data);
      
      const companyId = response.data;
      if (!companyId || companyId <= 0) {
        throw new Error('创建面试失败：无效的面试ID');
      }
      
      let interviewResponse;
      try {
        interviewResponse = await apiClient.get<InterviewRecord>(`/interviews/${companyId}`);
        console.log('[CreateInterviewForm] 获取面试详情成功:', interviewResponse.data);
      } catch (err) {
        console.error('[CreateInterviewForm] 获取面试详情失败:', err);
        interviewResponse = {
          data: {
            id: companyId,
            ...interviewData,
            status: 'SCHEDULED',
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          } as InterviewRecord,
        };
        console.log('[CreateInterviewForm] 使用临时面试对象:', interviewResponse.data);
      }
      
      onSuccess(interviewResponse.data);
    } catch (err) {
      console.error('[CreateInterviewForm] 提交失败:', err);
      const message = err instanceof Error ? err.message : '创建面试失败';
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-4">
      {error && (
        <div className="p-3 bg-accent-red/10 border border-accent-red/30 rounded-lg text-accent-red text-sm">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-3">
        <div>
          <label className="block text-sm font-semibold text-paper-800 mb-2">
            <div className="flex items-center gap-1">
              <Briefcase className="w-4 h-4" />
              面试类型
            </div>
          </label>
          <select
            value={formData.interviewType}
            onChange={(e) => setFormData({ ...formData, interviewType: e.target.value })}
            className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
          >
            <option value="">请选择</option>
            <option value="电话面试">电话面试</option>
            <option value="视频面试">视频面试</option>
            <option value="现场面试">现场面试</option>
            <option value="技术面试">技术面试</option>
            <option value="HR面试">HR面试</option>
            <option value="终面">终面</option>
          </select>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-semibold text-paper-800 mb-2">
              <div className="flex items-center gap-1">
                <Calendar className="w-4 h-4" />
                面试日期 <span className="text-accent-red">*</span>
              </div>
            </label>
            <input
              type="date"
              required
              value={formData.interviewDate}
              onChange={(e) => setFormData({ ...formData, interviewDate: e.target.value })}
              className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
            />
          </div>

          <div>
            <label className="block text-sm font-semibold text-paper-800 mb-2">
              <div className="flex items-center gap-1">
                <Clock className="w-4 h-4" />
                面试时间
              </div>
            </label>
            <input
              type="time"
              value={formData.interviewTime}
              onChange={(e) => setFormData({ ...formData, interviewTime: e.target.value })}
              className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-paper-800 mb-2">
            <div className="flex items-center gap-1">
              <Clock className="w-4 h-4" />
              预计时长（分钟）
            </div>
          </label>
          <input
            type="number"
            min="15"
            max="180"
            step="15"
            value={formData.durationMinutes}
            onChange={(e) => setFormData({ ...formData, durationMinutes: parseInt(e.target.value) || 60 })}
            className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-semibold text-paper-800 mb-2">
              <div className="flex items-center gap-1">
                <User className="w-4 h-4" />
                面试官姓名
              </div>
            </label>
            <input
              type="text"
              placeholder="例如：张三"
              value={formData.interviewerName}
              onChange={(e) => setFormData({ ...formData, interviewerName: e.target.value })}
              className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
            />
          </div>

          <div>
            <label className="block text-sm font-semibold text-paper-800 mb-2">
              <div className="flex items-center gap-1">
                <Briefcase className="w-4 h-4" />
                面试官职位
              </div>
            </label>
            <input
              type="text"
              placeholder="例如：技术总监"
              value={formData.interviewerTitle}
              onChange={(e) => setFormData({ ...formData, interviewerTitle: e.target.value })}
              className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-paper-800 mb-2">
            备注
          </label>
          <textarea
            placeholder="记录面试相关的备注信息..."
            value={formData.notes}
            onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
            rows={3}
            className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base resize-none"
          />
        </div>

        <div className="flex justify-end space-x-3 pt-4 border-t-2 border-paper-300">
          <button
            type="button"
            onClick={onCancel}
            disabled={isSubmitting}
            className="px-6 py-3 text-paper-700 border-2 border-paper-400 hover:bg-paper-200 rounded-lg transition-colors text-base font-medium disabled:opacity-50"
          >
            取消
          </button>
          <button
            type="submit"
            disabled={isSubmitting}
            className="px-6 py-3 bg-paper-700 text-paper-50 rounded-lg hover:bg-paper-800 transition-colors text-base font-medium border-2 border-paper-600 shadow-md disabled:opacity-50 flex items-center gap-2"
          >
            {isSubmitting ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-paper-50"></div>
                创建中...
              </>
            ) : (
              <>
                ✨ 创建面试
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
}
