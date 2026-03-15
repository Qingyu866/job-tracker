import { Plus, Trash2, Star, AlertCircle } from 'lucide-react';
import { useState } from 'react';
import type { JobApplication, InterviewRecord } from '@/types';
import { dataApi } from '@/services/dataApi';
import { toast } from '@/store/toastStore';
import { Modal } from './Modal';
import { CreateInterviewForm } from './CreateInterviewForm';
import { InterviewDetailModal } from './InterviewDetailModal';
import { STATUS_CONFIG, canScheduleInterview, getInterviewDisabledReason } from '@/utils/constants';

interface ApplicationDetailModalProps {
  application: JobApplication | null;
  onClose: () => void;
  onUpdate?: () => void;
}

export function ApplicationDetailModal({
  application,
  onClose,
  onUpdate,
}: ApplicationDetailModalProps) {
  const [showCreateInterview, setShowCreateInterview] = useState(false);
  const [selectedInterview, setSelectedInterview] = useState<InterviewRecord | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  if (!application) return null;

  const canAddInterview = canScheduleInterview(application.status);
  const disabledReason = getInterviewDisabledReason(application.status);

  const handleCreateInterview = async (_interview: InterviewRecord) => {
    setShowCreateInterview(false);
    toast.success('面试安排已创建！');
    onUpdate?.();
  };

  const handleDelete = async () => {
    if (!confirm('确定要删除这个申请吗？此操作不可恢复。')) {
      return;
    }

    setIsLoading(true);
    try {
      await dataApi.deleteApplication(application.id);
      toast.success('申请已删除');
      onUpdate?.();
      onClose();
    } catch (error) {
      console.error('删除失败:', error);
      toast.error('删除失败，请重试');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <Modal isOpen={!!application} onClose={onClose} title="申请详情">
        <div className="space-y-4">
          <div className="space-y-3">
            <h4 className="text-sm font-bold text-paper-700 border-b-2 border-paper-300 pb-2">
              公司信息
            </h4>
            <div className="p-4 bg-white rounded-lg border-2 border-paper-300 space-y-2">
              <div className="flex justify-between items-center">
                <span className="text-sm text-paper-600">公司名称</span>
                <span className="text-sm font-medium text-paper-800">
                  {application.company?.name || '-'}
                </span>
              </div>
              {application.company?.location && (
                <div className="flex justify-between items-center">
                  <span className="text-sm text-paper-600">所在地</span>
                  <span className="text-sm font-medium text-paper-800">
                    {application.company.location}
                  </span>
                </div>
              )}
              {application.company?.industry && (
                <div className="flex justify-between items-center">
                  <span className="text-sm text-paper-600">行业</span>
                  <span className="text-sm font-medium text-paper-800">
                    {application.company.industry}
                  </span>
                </div>
              )}
            </div>
          </div>

          <div className="space-y-3">
            <h4 className="text-sm font-bold text-paper-700 border-b-2 border-paper-300 pb-2">
              职位信息
            </h4>
            <div className="p-4 bg-white rounded-lg border-2 border-paper-300 space-y-2">
              <div className="flex justify-between items-center">
                <span className="text-sm text-paper-600">职位名称</span>
                <span className="text-sm font-medium text-paper-800">
                  {application.jobTitle}
                </span>
              </div>
              {application.jobType && (
                <div className="flex justify-between items-center">
                  <span className="text-sm text-paper-600">工作类型</span>
                  <span className="text-sm font-medium text-paper-800">
                    {application.jobType}
                  </span>
                </div>
              )}
              {application.workLocation && (
                <div className="flex justify-between items-center">
                  <span className="text-sm text-paper-600">工作地点</span>
                  <span className="text-sm font-medium text-paper-800">
                    {application.workLocation}
                  </span>
                </div>
              )}
              {application.salaryMin || application.salaryMax ? (
                <div className="flex justify-between items-center">
                  <span className="text-sm text-paper-600">薪资范围</span>
                  <span className="text-sm font-medium text-paper-800">
                    {application.salaryMin && application.salaryMax
                      ? `${application.salaryMin} - ${application.salaryMax} ${application.salaryCurrency || 'CNY'}`
                      : application.salaryMin
                      ? `${application.salaryMin}+ ${application.salaryCurrency || 'CNY'}`
                      : `${application.salaryMax} ${application.salaryCurrency || 'CNY'}`}
                  </span>
                </div>
              ) : null}
              {application.jobUrl && (
                <div className="flex justify-between items-center">
                  <span className="text-sm text-paper-600">职位链接</span>
                  <a
                    href={application.jobUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-sm font-medium text-accent-blue hover:underline"
                  >
                    查看详情
                  </a>
                </div>
              )}
            </div>
          </div>

          <div className="space-y-3">
            <h4 className="text-sm font-bold text-paper-700 border-b-2 border-paper-300 pb-2">
              申请信息
            </h4>
            <div className="p-4 bg-white rounded-lg border-2 border-paper-300 space-y-2">
              <div className="flex justify-between items-center">
                <span className="text-sm text-paper-600">申请状态</span>
                <span className="text-sm font-medium text-paper-800">
                  {STATUS_CONFIG[application.status as keyof typeof STATUS_CONFIG]?.label || application.status}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-paper-600">申请日期</span>
                <span className="text-sm font-medium text-paper-800">
                  {application.applicationDate || '-'}
                </span>
              </div>
              {application.priority !== null && application.priority !== undefined && (
                <div className="flex justify-between items-center">
                  <span className="text-sm text-paper-600">优先级</span>
                  <div className="flex items-center">
                    {[1, 2, 3].map((level) => (
                      <Star
                        key={level}
                        className={`w-4 h-4 mx-0.5 ${
                          level <= (application.priority ?? 0) ? 'fill-amber-500 text-amber-500' : 'text-paper-300'
                        }`}
                      />
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>

          {application.jobDescription && (
            <div className="space-y-3">
              <h4 className="text-sm font-bold text-paper-700 border-b-2 border-paper-300 pb-2">
                职位描述
              </h4>
              <div className="p-4 bg-white rounded-lg border-2 border-paper-300">
                <p className="text-sm text-paper-700 whitespace-pre-wrap">
                  {application.jobDescription}
                </p>
              </div>
            </div>
          )}

          {application.notes && (
            <div className="space-y-3">
              <h4 className="text-sm font-bold text-paper-700 border-b-2 border-paper-300 pb-2">
                备注
              </h4>
              <div className="p-4 bg-white rounded-lg border-2 border-paper-300">
                <p className="text-sm text-paper-700 whitespace-pre-wrap">
                  {application.notes}
                </p>
              </div>
            </div>
          )}

          <div className="space-y-3">
            <h4 className="text-sm font-bold text-paper-700 border-b-2 border-paper-300 pb-2">
              面试安排
            </h4>
            {!canAddInterview && (
              <div className="bg-gray-100 text-gray-500 p-3 rounded-lg text-sm flex items-start gap-2">
                <AlertCircle className="w-4 h-4 mt-0.5 flex-shrink-0" />
                <span>{disabledReason}</span>
              </div>
            )}
          </div>

          <div className="flex gap-2 pt-4 border-t-2 border-paper-300">
            <button
              onClick={() => setShowCreateInterview(true)}
              disabled={!canAddInterview}
              title={disabledReason || ''}
              className="flex-1 px-4 py-3 bg-paper-700 text-paper-50 rounded-lg hover:bg-paper-800 transition-colors font-medium border-2 border-paper-600 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Plus className="w-4 h-4" />
              添加面试安排
            </button>
            <button
              onClick={handleDelete}
              disabled={isLoading}
              className="px-4 py-3 bg-accent-red text-white rounded-lg hover:bg-red-700 transition-colors font-medium border-2 border-red-600 disabled:opacity-50 flex items-center justify-center gap-2"
            >
              <Trash2 className="w-4 h-4" />
              删除
            </button>
          </div>
        </div>
      </Modal>

      {showCreateInterview && (
        <div className="fixed inset-0 z-50 flex">
          <div
            className="flex-1 bg-black/40 backdrop-blur-sm transition-opacity"
            onClick={() => setShowCreateInterview(false)}
          />
          <div className="w-full max-w-md bg-[#f5f0e6] shadow-2xl overflow-y-auto animate-slide-in-right">
            <div className="p-6">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <h3 className="text-lg font-bold text-paper-800">添加面试安排</h3>
                  <p className="text-sm text-paper-500">为 {application.company?.name} 添加面试</p>
                </div>
                <button
                  onClick={() => setShowCreateInterview(false)}
                  className="p-2 hover:bg-paper-200 rounded-lg text-paper-700 transition-colors border border-paper-400"
                  aria-label="关闭"
                >
                  ✕
                </button>
              </div>
              <CreateInterviewForm
                applicationId={application.id}
                onSuccess={handleCreateInterview}
                onCancel={() => setShowCreateInterview(false)}
              />
            </div>
          </div>
        </div>
      )}

      {selectedInterview && (
        <InterviewDetailModal
          interview={selectedInterview}
          onClose={() => setSelectedInterview(null)}
          onUpdate={onUpdate}
        />
      )}
    </>
  );
}
