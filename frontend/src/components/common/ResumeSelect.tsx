import { useState, useEffect } from 'react';
import { Plus, Check } from 'lucide-react';
import type { UserResume } from '@/types/resume';
import { resumeApi } from '@/services/resumeApi';
import { ResumeCreateModal } from './ResumeCreateModal';

interface ResumeSelectProps {
  value: number | undefined;
  onChange: (resumeId: number | undefined) => void;
}

export function ResumeSelect({ value, onChange }: ResumeSelectProps) {
  const [resumes, setResumes] = useState<UserResume[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);

  useEffect(() => {
    fetchResumes();
  }, []);

  const fetchResumes = async () => {
    setLoading(true);
    try {
      const resumeList = await resumeApi.getMyResumes();
      setResumes(resumeList);
    } catch (error) {
      console.error('获取简历列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateSuccess = (resumeId: number) => {
    setShowCreateModal(false);
    fetchResumes();
    onChange(resumeId);
  };

  return (
    <>
      <div className="space-y-2">
        <div className="relative">
          <select
            value={value || ''}
            onChange={(e) => onChange(e.target.value ? Number(e.target.value) : undefined)}
            disabled={loading}
            className="w-full px-4 py-3 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base disabled:opacity-50 appearance-none"
          >
            <option value="">{loading ? '加载中...' : '选择简历'}</option>
            {resumes.map(resume => (
              <option key={resume.resumeId} value={resume.resumeId}>
                {resume.resumeName} {resume.isDefault ? '(默认)' : ''}
              </option>
            ))}
          </select>
          {value && (
            <div className="absolute right-10 top-1/2 -translate-y-1/2">
              <Check className="w-4 h-4 text-green-500" />
            </div>
          )}
        </div>

        {resumes.length === 0 && !loading && (
          <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg text-center">
            <p className="text-xs text-amber-700 mb-2">还没有简历？创建一个来关联</p>
            <button
              type="button"
              onClick={() => setShowCreateModal(true)}
              className="w-full px-3 py-2 bg-amber-500 text-white rounded-lg hover:bg-amber-600 transition-colors text-sm font-medium flex items-center justify-center gap-1"
            >
              <Plus className="w-4 h-4" />
              创建新简历
            </button>
          </div>
        )}

        {resumes.length > 0 && (
          <button
            type="button"
            onClick={() => setShowCreateModal(true)}
            className="w-full text-xs text-amber-600 hover:text-amber-700 flex items-center justify-center gap-1 py-1"
          >
            <Plus className="w-3 h-3" />
            创建新简历
          </button>
        )}
      </div>

      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div
            className="absolute inset-0 bg-paper-100"
            onClick={() => setShowCreateModal(false)}
          />
          <div className="relative bg-paper-50 rounded-xl shadow-2xl w-full max-w-3xl max-h-[90vh] overflow-hidden flex flex-col mx-4">
            <ResumeCreateModal
              onClose={() => setShowCreateModal(false)}
              onSuccess={handleCreateSuccess}
            />
          </div>
        </div>
      )}
    </>
  );
}
