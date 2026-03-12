import { useState } from 'react';
import { apiClient } from '@/lib/apiClient';
import type { Company } from '@/types';
import { Building2, MapPin, Globe, Users } from 'lucide-react';

interface CreateCompanyFormProps {
  companyName: string;
  onSuccess: (company: Company) => void;
  onCancel: () => void;
}

export function CreateCompanyForm({ companyName, onSuccess, onCancel }: CreateCompanyFormProps) {
  const [formData, setFormData] = useState({
    name: companyName,
    industry: '',
    size: '',
    location: '',
    website: '',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      const response = await apiClient.post<number>('/companies', formData);
      // 获取刚创建的公司详情
      const companyResponse = await apiClient.get<Company>(`/companies/${response.data}`);
      onSuccess(companyResponse.data);
    } catch (err) {
      const message = err instanceof Error ? err.message : '创建公司失败';
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
            公司名称 <span className="text-accent-red">*</span>
          </label>
          <input
            type="text"
            required
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
          />
        </div>

        <div>
          <label className="block text-sm font-semibold text-paper-800 mb-2">
            <div className="flex items-center gap-1">
              <Building2 className="w-4 h-4" />
              行业
            </div>
          </label>
          <input
            type="text"
            placeholder="例如：互联网、金融、教育"
            value={formData.industry}
            onChange={(e) => setFormData({ ...formData, industry: e.target.value })}
            className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-semibold text-paper-800 mb-2">
              <div className="flex items-center gap-1">
                <Users className="w-4 h-4" />
                公司规模
              </div>
            </label>
            <select
              value={formData.size}
              onChange={(e) => setFormData({ ...formData, size: e.target.value })}
              className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
            >
              <option value="">请选择</option>
              <option value="1-10">1-10人</option>
              <option value="11-50">11-50人</option>
              <option value="51-200">51-200人</option>
              <option value="201-500">201-500人</option>
              <option value="501-1000">501-1000人</option>
              <option value="1000+">1000人以上</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-semibold text-paper-800 mb-2">
              <div className="flex items-center gap-1">
                <MapPin className="w-4 h-4" />
                所在地
              </div>
            </label>
            <input
              type="text"
              placeholder="例如：北京、上海"
              value={formData.location}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
              className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold text-paper-800 mb-2">
            <div className="flex items-center gap-1">
              <Globe className="w-4 h-4" />
              官网
            </div>
          </label>
          <input
            type="url"
            placeholder="https://example.com"
            value={formData.website}
            onChange={(e) => setFormData({ ...formData, website: e.target.value })}
            className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
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
                ✨ 创建公司
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
}
