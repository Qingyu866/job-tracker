import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useResumeStore } from '@/store/resumeStore';
import { useUserStore } from '@/store/userStore';
import { ResumeCard } from '@/components/resume/ResumeCard';
import { Button, Spinner } from '@/components/common';
import { Header } from '@/components/layout/Header';
import { Plus, Download, Trash2, FileText } from 'lucide-react';
import { toast } from '@/store/toastStore';

type FilterType = 'all' | 'default';

export function ResumeListPage() {
  const navigate = useNavigate();
  const { userInfo } = useUserStore();
  const {
    resumes,
    listLoading,
    deleting,
    fetchResumes,
    deleteResume,
    setDefaultResume,
  } = useResumeStore();

  const [filter, setFilter] = useState<FilterType>('all');
  const [selectedResumes, setSelectedResumes] = useState<Set<number>>(new Set());

  useEffect(() => {
    if (userInfo?.id) {
      fetchResumes(userInfo.id);
    }
  }, [userInfo?.id]);

  const filteredResumes = resumes.filter((resume) => {
    if (filter === 'default') {
      return resume.isDefault;
    }
    return true;
  });

  const handleCreateResume = () => {
    navigate('/resumes/new');
  };

  const handleEditResume = (resumeId: number) => {
    navigate(`/resumes/${resumeId}/edit`);
  };

  const handleDeleteResume = async (resumeId: number) => {
    if (!confirm('确定要删除这份简历吗？此操作不可恢复。')) {
      return;
    }

    try {
      await deleteResume(resumeId);
      toast.success('简历已删除');
    } catch (error) {
      toast.error('删除失败，请重试');
    }
  };

  const handleSetDefault = async (resumeId: number) => {
    try {
      await setDefaultResume(resumeId);
      toast.success('已设为默认简历');
    } catch (error) {
      toast.error('设置失败，请重试');
    }
  };

  const handleSelectResume = (resumeId: number) => {
    setSelectedResumes((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(resumeId)) {
        newSet.delete(resumeId);
      } else {
        newSet.add(resumeId);
      }
      return newSet;
    });
  };

  const handleDeleteSelected = async () => {
    if (selectedResumes.size === 0) return;

    if (!confirm(`确定要删除选中的 ${selectedResumes.size} 份简历吗？此操作不可恢复。`)) {
      return;
    }

    try {
      for (const resumeId of selectedResumes) {
        await deleteResume(resumeId);
      }
      setSelectedResumes(new Set());
      toast.success('简历已删除');
    } catch (error) {
      toast.error('删除失败，请重试');
    }
  };

  const handleExportResumes = () => {
    if (resumes.length === 0) return;

    const data = resumes.map((resume) => ({
      resumeName: resume.resumeName,
      workYears: resume.workYears,
      currentPosition: resume.currentPosition,
      summary: resume.summary,
      skills: resume.skills,
      education: resume.education,
      experience: resume.experience,
      projects: resume.projects,
      isDefault: resume.isDefault,
      createdAt: resume.createdAt,
    }));

    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `简历列表_${new Date().toISOString().split('T')[0]}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="h-screen flex flex-col">
      <Header />

      <div className="h-12 md:h-14 border-b flex items-center px-3 md:px-6 space-x-3 md:space-x-4 bg-paper-50">
        <div className="flex items-center space-x-1 md:space-x-2 bg-paper-100 p-1 rounded-lg border border-paper-200 overflow-x-auto">
          <button
            onClick={handleCreateResume}
            className="px-2 md:px-3 py-1.5 rounded-md text-xs md:text-sm font-medium transition-all flex-shrink-0 flex items-center gap-1 md:gap-2 bg-paper-50 text-paper-700 shadow-paper border border-paper-200 hover:bg-paper-200"
          >
            <Plus className="w-4 h-4" />
            <span className="hidden sm:inline">新建简历</span>
          </button>

          <button
            onClick={handleExportResumes}
            disabled={resumes.length === 0}
            className="px-2 md:px-3 py-1.5 rounded-md text-xs md:text-sm font-medium transition-all flex-shrink-0 flex items-center gap-1 md:gap-2 text-paper-600 hover:bg-paper-200 disabled:opacity-50"
          >
            <Download className="w-4 h-4" />
            <span className="hidden sm:inline">导出</span>
          </button>

          <button
            onClick={handleDeleteSelected}
            disabled={selectedResumes.size === 0 || deleting}
            className="px-2 md:px-3 py-1.5 rounded-md text-xs md:text-sm font-medium transition-all flex-shrink-0 flex items-center gap-1 md:gap-2 text-paper-600 hover:bg-paper-200 disabled:opacity-50"
          >
            <Trash2 className="w-4 h-4" />
            <span className="hidden sm:inline">删除</span>
          </button>
        </div>

        <div className="flex items-center gap-1 bg-paper-100 p-1 rounded-lg border border-paper-200 ml-auto">
          {(['all', 'default'] as FilterType[]).map((f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-2 md:px-3 py-1 rounded-md text-xs md:text-sm font-medium transition-colors ${
                filter === f
                  ? 'bg-accent-amber text-paper-800'
                  : 'text-paper-600 hover:bg-paper-200'
              }`}
            >
              {f === 'all' ? '全部' : '默认'}
            </button>
          ))}
        </div>
      </div>

      <main className="flex-1 overflow-auto">
        <div className="max-w-6xl mx-auto px-4 py-6">
          {listLoading ? (
            <div className="flex items-center justify-center py-12">
              <Spinner size="lg" />
            </div>
          ) : filteredResumes.length === 0 ? (
            <div className="text-center py-12">
              <div className="w-16 h-16 rounded-full bg-paper-100 flex items-center justify-center mx-auto mb-4">
                <FileText className="w-8 h-8 text-paper-400" />
              </div>
              <h3 className="text-paper-700 font-medium mb-2">
                {filter === 'default' ? '暂无默认简历' : '暂无简历'}
              </h3>
              <p className="text-paper-500 text-sm mb-4">
                {filter === 'default' 
                  ? '请先设置一份默认简历' 
                  : '点击上方按钮创建你的第一份简历'}
              </p>
              {filter !== 'default' && (
                <Button onClick={handleCreateResume}>创建简历</Button>
              )}
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {filteredResumes.map((resume) => (
                <ResumeCard
                  key={resume.id}
                  resume={resume}
                  selected={selectedResumes.has(resume.id)}
                  onSelect={() => handleSelectResume(resume.id)}
                  onEdit={() => handleEditResume(resume.id)}
                  onDelete={() => handleDeleteResume(resume.id)}
                  onSetDefault={() => handleSetDefault(resume.id)}
                />
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
