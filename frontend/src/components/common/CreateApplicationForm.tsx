import { useState } from 'react';
import { Briefcase, FileText, Zap, TrendingUp } from 'lucide-react';
import { useApplicationStore } from '@/store/applicationStore';
import { CompanyAutocomplete } from './CompanyAutocomplete';
import { ResumeSelect } from './ResumeSelect';
import { STATUS_CONFIG } from '@/utils/constants';

interface CreateApplicationFormProps {
  onClose: () => void;
}

const SENIORITY_OPTIONS = [
  { value: 'JUNIOR', label: '初级 (0-2年)' },
  { value: 'MIDDLE', label: '中级 (2-5年)' },
  { value: 'SENIOR', label: '高级 (5-8年)' },
  { value: 'LEAD', label: '技术负责人 (8年+)' },
];

type SeniorityLevel = 'JUNIOR' | 'MIDDLE' | 'SENIOR' | 'LEAD';

export function CreateApplicationForm({ onClose }: CreateApplicationFormProps) {
  const { createApplication } = useApplicationStore();
  
  const [formData, setFormData] = useState({
    companyId: 0,
    companyName: '',
    jobTitle: '',
    jobDescription: '',
    jobType: '',
    workLocation: '',
    salaryMin: '',
    salaryMax: '',
    salaryCurrency: 'CNY',
    jobUrl: '',
    status: 'WISHLIST' as keyof typeof STATUS_CONFIG,
    applicationDate: new Date().toISOString().split('T')[0],
    priority: 1,
    notes: '',
    resumeId: undefined as number | undefined,
    seniorityLevel: '' as SeniorityLevel | '',
    skillsRequired: '',
  });

  const handleResumeChange = (resumeId: number | undefined) => {
    setFormData(prev => ({ ...prev, resumeId }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await createApplication({
        ...formData,
        companyId: formData.companyId || undefined,
        salaryMin: formData.salaryMin ? Number(formData.salaryMin) : undefined,
        salaryMax: formData.salaryMax ? Number(formData.salaryMax) : undefined,
        resumeId: formData.resumeId || undefined,
        seniorityLevel: formData.seniorityLevel || undefined,
        skillsRequired: formData.skillsRequired || undefined,
      });
      onClose();
    } catch (error) {
      console.error('创建失败:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="space-y-4">
        <div className="text-sm font-semibold text-paper-700 border-b-2 border-paper-300 pb-2">
          基本信息
        </div>

        <div>
          <label className="block text-sm font-semibold text-paper-800 mb-2">
            公司名称 <span className="text-accent-red">*</span>
          </label>
          <CompanyAutocomplete
            value={formData.companyName}
            onChange={(value, companyId) => setFormData({
              ...formData,
              companyName: value,
              companyId: companyId || 0
            })}
            placeholder="例如：字节跳动"
          />
        </div>

        <div>
          <label className="block text-sm font-semibold text-paper-800 mb-2">
            <div className="flex items-center gap-1">
              <Briefcase className="w-4 h-4" />
              职位名称 <span className="text-accent-red">*</span>
            </div>
          </label>
          <input
            type="text"
            required
            placeholder="例如：前端工程师"
            value={formData.jobTitle}
            onChange={(e) => setFormData({ ...formData, jobTitle: e.target.value })}
            className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-semibold text-paper-800 mb-2">
              <div className="flex items-center gap-1">
                <TrendingUp className="w-4 h-4" />
                岗位级别
              </div>
            </label>
            <select
              value={formData.seniorityLevel}
              onChange={(e) => setFormData({ ...formData, seniorityLevel: e.target.value as SeniorityLevel })}
              className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
            >
              <option value="">请选择</option>
              {SENIORITY_OPTIONS.map(opt => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-semibold text-paper-800 mb-2">
              <div className="flex items-center gap-1">
                <FileText className="w-4 h-4" />
                关联简历
              </div>
            </label>
            <ResumeSelect
              value={formData.resumeId}
              onChange={handleResumeChange}
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-paper-800 mb-2">
            <div className="flex items-center gap-1">
              <Zap className="w-4 h-4 text-amber-500" />
              技能要求
            </div>
          </label>
          <input
            type="text"
            placeholder="例如：React,TypeScript,Node.js（逗号分隔）"
            value={formData.skillsRequired}
            onChange={(e) => setFormData({ ...formData, skillsRequired: e.target.value })}
            className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
          />
          <p className="text-xs text-paper-500 mt-1">从JD中提取的关键技能，用于模拟面试</p>
        </div>
      </div>

      <div className="space-y-4">
        <div className="text-sm font-semibold text-paper-700 border-b-2 border-paper-300 pb-2">
          职位详情
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-semibold text-paper-800 mb-2">
              工作类型
            </label>
            <select
              value={formData.jobType}
              onChange={(e) => setFormData({ ...formData, jobType: e.target.value })}
              className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
            >
              <option value="">请选择</option>
              <option value="全职">全职</option>
              <option value="兼职">兼职</option>
              <option value="实习">实习</option>
              <option value="合同">合同</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-semibold text-paper-800 mb-2">
              工作地点
            </label>
            <input
              type="text"
              placeholder="例如：北京"
              value={formData.workLocation}
              onChange={(e) => setFormData({ ...formData, workLocation: e.target.value })}
              className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
            />
          </div>
        </div>

        <div className="grid grid-cols-3 gap-4">
          <div className="col-span-1">
            <label className="block text-sm font-semibold text-paper-800 mb-2">
              薪资下限
            </label>
            <input
              type="number"
              placeholder="例如：15"
              value={formData.salaryMin}
              onChange={(e) => setFormData({ ...formData, salaryMin: e.target.value })}
              className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
            />
          </div>
          <div className="col-span-1">
            <label className="block text-sm font-semibold text-paper-800 mb-2">
              薪资上限
            </label>
            <input
              type="number"
              placeholder="例如：25"
              value={formData.salaryMax}
              onChange={(e) => setFormData({ ...formData, salaryMax: e.target.value })}
              className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
            />
          </div>
          <div className="col-span-1">
            <label className="block text-sm font-semibold text-paper-800 mb-2">
              货币
            </label>
            <select
              value={formData.salaryCurrency}
              onChange={(e) => setFormData({ ...formData, salaryCurrency: e.target.value })}
              className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
            >
              <option value="CNY">CNY</option>
              <option value="USD">USD</option>
              <option value="EUR">EUR</option>
            </select>
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-paper-800 mb-2">
            职位链接
          </label>
          <input
            type="url"
            placeholder="例如：https://jobs.example.com/123"
            value={formData.jobUrl}
            onChange={(e) => setFormData({ ...formData, jobUrl: e.target.value })}
            className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
          />
        </div>

        <div>
          <label className="block text-sm font-semibold text-paper-800 mb-2">
            职位描述
          </label>
          <textarea
            placeholder="粘贴职位描述（可选）"
            value={formData.jobDescription}
            onChange={(e) => setFormData({ ...formData, jobDescription: e.target.value })}
            rows={4}
            className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base resize-none"
          />
        </div>
      </div>

      <div className="space-y-4">
        <div className="text-sm font-semibold text-paper-700 border-b-2 border-paper-300 pb-2">
          申请状态
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-semibold text-paper-800 mb-2">
              申请状态
            </label>
            <select
              value={formData.status}
              onChange={(e) => setFormData({ ...formData, status: e.target.value as keyof typeof STATUS_CONFIG })}
              className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
            >
              {Object.entries(STATUS_CONFIG).map(([key, { label }]) => (
                <option key={key} value={key}>
                  {label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-semibold text-paper-800 mb-2">
              优先级
            </label>
            <select
              value={formData.priority}
              onChange={(e) => setFormData({ ...formData, priority: Number(e.target.value) })}
              className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
            >
              <option value={1}>低优先级</option>
              <option value={2}>中优先级</option>
              <option value={3}>高优先级</option>
            </select>
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-paper-800 mb-2">
            申请日期
          </label>
          <input
            type="date"
            value={formData.applicationDate}
            onChange={(e) => setFormData({ ...formData, applicationDate: e.target.value })}
            className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
          />
        </div>

        <div>
          <label className="block text-sm font-semibold text-paper-800 mb-2">
            备注
          </label>
          <textarea
            placeholder="填写备注信息（可选）"
            value={formData.notes}
            onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
            rows={3}
            className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base resize-none"
          />
        </div>
      </div>

      <div className="flex justify-end space-x-3 pt-4 border-t-2 border-paper-300">
        <button
          type="button"
          onClick={onClose}
          className="px-6 py-3 text-paper-700 border-2 border-paper-400 hover:bg-paper-200 rounded-lg transition-colors text-base font-medium"
        >
          取消
        </button>
        <button
          type="submit"
          className="px-6 py-3 bg-paper-700 text-paper-50 rounded-lg hover:bg-paper-800 transition-colors text-base font-medium border-2 border-paper-600 shadow-md"
        >
          ✨ 创建申请
        </button>
      </div>
    </form>
  );
}
