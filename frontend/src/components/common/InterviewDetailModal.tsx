import { X } from 'lucide-react';
import type { InterviewRecord } from '@/types';

interface InterviewDetailModalProps {
  interview: InterviewRecord | null;
  onClose: () => void;
}

export function InterviewDetailModal({ interview, onClose }: InterviewDetailModalProps) {
  if (!interview) return null;

  const statusConfig = {
    SCHEDULED: { label: '已安排', color: 'text-accent-blue bg-accent-blue/10 border-accent-blue/30' },
    COMPLETED: { label: '已完成', color: 'text-accent-green bg-accent-green/10 border-accent-green/30' },
    CANCELLED: { label: '已取消', color: 'text-accent-red bg-accent-red/10 border-accent-red/30' },
  };

  const statusInfo = statusConfig[interview.status as keyof typeof statusConfig] || {
    label: interview.status,
    color: 'text-paper-600 bg-paper-100 border-paper-300'
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* 遮罩 */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* 模态框 */}
      <div className="relative bg-[#f5f0e6] rounded-xl shadow-2xl max-w-md w-full max-h-[90vh] overflow-y-auto border-2 border-paper-400">
        {/* 头部 */}
        <div className="flex items-center justify-between p-5 border-b-2 border-paper-300 sticky top-0 bg-[#f5f0e6] z-10">
          <div>
            <h3 className="font-serif text-paper-800 font-bold text-lg">面试详情</h3>
            <p className="text-xs text-paper-500 mt-1">面试记录 ID: {interview.id}</p>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-paper-200 rounded-lg text-paper-700 transition-colors border border-paper-400 hover:border-paper-500"
            aria-label="关闭"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* 内容 */}
        <div className="p-5 space-y-4">
          {/* 状态标签 */}
          <div className="flex items-center justify-between p-4 bg-white rounded-lg border-2 border-paper-300">
            <span className="text-sm font-medium text-paper-700">面试状态</span>
            <span className={`px-3 py-1 rounded-full text-sm font-medium border ${statusInfo.color}`}>
              {statusInfo.label}
            </span>
          </div>

          {/* 基本信息 */}
          <div className="space-y-3">
            <h4 className="text-sm font-bold text-paper-700 border-b-2 border-paper-300 pb-2">
              基本信息
            </h4>

            <div className="grid grid-cols-1 gap-3">
              {interview.interviewType && (
                <div className="flex justify-between items-center py-2 border-b border-paper-200">
                  <span className="text-sm text-paper-600">面试类型</span>
                  <span className="text-sm font-medium text-paper-800">{interview.interviewType}</span>
                </div>
              )}

              <div className="flex justify-between items-center py-2 border-b border-paper-200">
                <span className="text-sm text-paper-600">面试日期</span>
                <span className="text-sm font-medium text-paper-800">
                  {new Date(interview.interviewDate).toLocaleString('zh-CN', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </span>
              </div>

              {interview.durationMinutes && (
                <div className="flex justify-between items-center py-2 border-b border-paper-200">
                  <span className="text-sm text-paper-600">预计时长</span>
                  <span className="text-sm font-medium text-paper-800">{interview.durationMinutes} 分钟</span>
                </div>
              )}
            </div>
          </div>

          {/* 面试官信息 */}
          {(interview.interviewerName || interview.interviewerTitle) && (
            <div className="space-y-3">
              <h4 className="text-sm font-bold text-paper-700 border-b-2 border-paper-300 pb-2">
                面试官信息
              </h4>

              <div className="p-4 bg-white rounded-lg border-2 border-paper-300">
                <div className="space-y-2">
                  {interview.interviewerName && (
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-paper-600">姓名</span>
                      <span className="text-sm font-medium text-paper-800">{interview.interviewerName}</span>
                    </div>
                  )}

                  {interview.interviewerTitle && (
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-paper-600">职位</span>
                      <span className="text-sm font-medium text-paper-800">{interview.interviewerTitle}</span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* 备注 */}
          {interview.notes && (
            <div className="space-y-3">
              <h4 className="text-sm font-bold text-paper-700 border-b-2 border-paper-300 pb-2">
                备注
              </h4>

              <div className="p-4 bg-white rounded-lg border-2 border-paper-300">
                <p className="text-sm text-paper-700 whitespace-pre-wrap">{interview.notes}</p>
              </div>
            </div>
          )}

          {/* 元数据 */}
          <div className="pt-4 border-t-2 border-paper-300">
            <div className="grid grid-cols-2 gap-4 text-xs text-paper-500">
              <div>
                <span className="block">创建时间</span>
                <span className="block font-medium text-paper-700">
                  {new Date(interview.createdAt).toLocaleString('zh-CN')}
                </span>
              </div>
              {interview.updatedAt && (
                <div>
                  <span className="block">更新时间</span>
                  <span className="block font-medium text-paper-700">
                    {new Date(interview.updatedAt).toLocaleString('zh-CN')}
                  </span>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* 底部按钮 */}
        <div className="p-5 border-t-2 border-paper-300">
          <button
            onClick={onClose}
            className="w-full px-6 py-3 bg-paper-700 text-paper-50 rounded-lg hover:bg-paper-800 transition-colors text-base font-medium border-2 border-paper-600 shadow-md"
          >
            关闭
          </button>
        </div>
      </div>
    </div>
  );
}
