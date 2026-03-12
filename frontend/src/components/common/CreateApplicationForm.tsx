import { useState } from 'react';
import { useApplicationStore } from '@/store/applicationStore';
import { STATUS_CONFIG } from '@/utils/constants';

interface CreateApplicationFormProps {
  onClose: () => void;
}

export function CreateApplicationForm({ onClose }: CreateApplicationFormProps) {
  const { createApplication } = useApplicationStore();
  const [formData, setFormData] = useState({
    companyId: 0,
    companyName: '',
    jobTitle: '',
    jobType: '',
    workLocation: '',
    status: 'WISHLIST' as keyof typeof STATUS_CONFIG,
    applicationDate: new Date().toISOString().split('T')[0],
    priority: 1,
    notes: '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await createApplication({
        ...formData,
        companyId: 1, // 临时使用固定公司 ID
      });
      onClose();
    } catch (error) {
      console.error('创建失败:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-paper-700 mb-1">
          公司名称 *
        </label>
        <input
          type="text"
          required
          value={formData.companyName}
          onChange={(e) => setFormData({ ...formData, companyName: e.target.value })}
          className="w-full px-3 py-3 border border-paper-200 rounded-lg bg-paper-100 text-paper-700 focus:outline-none focus:ring-2 focus:ring-accent-amber text-base"
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-paper-700 mb-1">
          职位名称 *
        </label>
        <input
          type="text"
          required
          value={formData.jobTitle}
          onChange={(e) => setFormData({ ...formData, jobTitle: e.target.value })}
          className="w-full px-3 py-3 border border-paper-200 rounded-lg bg-paper-100 text-paper-700 focus:outline-none focus:ring-2 focus:ring-accent-amber text-base"
        />
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-paper-700 mb-1">
            状态
          </label>
          <select
            value={formData.status}
            onChange={(e) => setFormData({ ...formData, status: e.target.value as keyof typeof STATUS_CONFIG })}
            className="w-full px-3 py-2 border border-paper-200 rounded-lg bg-paper-100 text-paper-700 focus:outline-none focus:ring-2 focus:ring-accent-amber text-base"
          >
            {Object.entries(STATUS_CONFIG).map(([key, { label }]) => (
              <option key={key} value={key}>
                {label}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-paper-700 mb-1">
            优先级
          </label>
          <select
            value={formData.priority}
            onChange={(e) => setFormData({ ...formData, priority: Number(e.target.value) })}
            className="w-full px-3 py-2 border border-paper-200 rounded-lg bg-paper-100 text-paper-700 focus:outline-none focus:ring-2 focus:ring-accent-amber text-base"
          >
            <option value={1}>⭐</option>
            <option value={2}>⭐⭐</option>
            <option value={3}>⭐⭐⭐</option>
          </select>
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-paper-700 mb-1">
          申请日期
        </label>
        <input
          type="date"
          value={formData.applicationDate}
          onChange={(e) => setFormData({ ...formData, applicationDate: e.target.value })}
          className="w-full px-3 py-3 border border-paper-200 rounded-lg bg-paper-100 text-paper-700 focus:outline-none focus:ring-2 focus:ring-accent-amber text-base"
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-paper-700 mb-1">
          备注
        </label>
        <textarea
          value={formData.notes}
          onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
          rows={3}
          className="w-full px-3 py-3 border border-paper-200 rounded-lg bg-paper-100 text-paper-700 focus:outline-none focus:ring-2 focus:ring-accent-amber text-base resize-none"
        />
      </div>

      <div className="flex justify-end space-x-3 pt-4">
        <button
          type="button"
          onClick={onClose}
          className="px-6 py-3 text-paper-600 hover:bg-paper-200 rounded-lg transition-colors text-base"
        >
          取消
        </button>
        <button
          type="submit"
          className="px-6 py-3 bg-accent-amber text-white rounded-lg hover:bg-accent-amber/90 transition-colors text-base font-medium"
        >
          创建
        </button>
      </div>
    </form>
  );
}
