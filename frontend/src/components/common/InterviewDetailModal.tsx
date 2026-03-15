import { X, Star, Edit2, Check, XCircle, UserX } from 'lucide-react';
import { useState, useEffect, useRef } from 'react';
import type { InterviewRecord } from '@/types';
import { dataApi } from '@/services/dataApi';
import { toast } from '@/store/toastStore';

interface InterviewDetailModalProps {
  interview: InterviewRecord | null;
  onClose: () => void;
  onUpdate?: () => void; // 刷新回调
}

export function InterviewDetailModal({
  interview,
  onClose,
  onUpdate,
}: InterviewDetailModalProps) {
  const [isCompleting, setIsCompleting] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);
  const [showNoShowConfirm, setShowNoShowConfirm] = useState(false);
  const [formData, setFormData] = useState({
    rating: interview?.rating || 0,
    feedback: interview?.feedback || '',
    technicalQuestions: interview?.technicalQuestions || '',
  });
  const justSavedRef = useRef(false);

  useEffect(() => {
    if (interview && !isEditing && !justSavedRef.current) {
      setFormData({
        rating: interview.rating || 0,
        feedback: interview.feedback || '',
        technicalQuestions: interview.technicalQuestions || '',
      });
    }
    justSavedRef.current = false;
  }, [interview, isEditing]);

  if (!interview) return null;

  const statusConfig = {
    SCHEDULED: {
      label: '已安排',
      color: 'text-accent-blue bg-accent-blue/10 border-accent-blue/30',
    },
    INTERVIEW: {
      label: '已安排',
      color: 'text-accent-blue bg-accent-blue/10 border-accent-blue/30',
    },
    COMPLETED: {
      label: '已完成',
      color: 'text-accent-green bg-accent-green/10 border-accent-green/30',
    },
    CANCELLED: {
      label: '已取消',
      color: 'text-accent-red bg-accent-red/10 border-accent-red/30',
    },
    NO_SHOW: {
      label: '未参加',
      color: 'text-paper-600 bg-paper-100 border-paper-300',
    },
  };

  const statusInfo =
    statusConfig[interview.status as keyof typeof statusConfig] || {
      label: interview.status,
      color: 'text-paper-600 bg-paper-100 border-paper-300',
    };

  // 处理标记完成
  const handleComplete = async () => {
    setIsLoading(true);
    try {
      await dataApi.completeInterview(
        interview.id,
        formData.rating || undefined,
        formData.feedback || undefined
      );
      toast.success('面试已标记为完成！');
      setIsCompleting(false);
      onUpdate?.(); // 刷新数据
      onClose();
    } catch (error) {
      console.error('标记完成失败:', error);
      toast.error('操作失败，请重试');
    } finally {
      setIsLoading(false);
    }
  };

  // 处理取消面试
  const handleCancel = async () => {
    setShowCancelConfirm(true);
  };

  // 确认取消面试
  const confirmCancel = async () => {
    setShowCancelConfirm(false);
    setIsLoading(true);
    try {
      await dataApi.cancelInterview(interview.id);
      setShowCancelConfirm(false);
      setShowNoShowConfirm(false);
      toast.success('面试已取消');
      onUpdate?.();
      onClose();
    } catch (error) {
      console.error('取消失败:', error);
      toast.error('操作失败，请重试');
    } finally {
      setIsLoading(false);
    }
  };

  // 处理标记为未参加
  const handleNoShow = async () => {
    setShowNoShowConfirm(true);
  };

  // 确认标记为未参加
  const confirmNoShow = async () => {
    setShowNoShowConfirm(false);
    setIsLoading(true);
    try {
      await dataApi.markAsNoShow(interview.id);
      setShowCancelConfirm(false);
      setShowNoShowConfirm(false);
      toast.success('已标记为未参加');
      onUpdate?.();
      onClose();
    } catch (error) {
      console.error('操作失败:', error);
      toast.error('操作失败，请重试');
    } finally {
      setIsLoading(false);
    }
  };

  // 保存编辑
  const handleSaveEdit = async () => {
    setIsLoading(true);
    try {
      // 更新反馈
      if (formData.feedback !== interview.feedback) {
        await dataApi.updateFeedback(interview.id, formData.feedback);
      }
      // 更新技术问题
      if (formData.technicalQuestions !== interview.technicalQuestions) {
        await dataApi.updateTechnicalQuestions(
          interview.id,
          formData.technicalQuestions
        );
      }
      toast.success('保存成功！');
      
      setIsEditing(false);
      onUpdate?.();
      onClose();
    } catch (error) {
      console.error('保存失败:', error);
      toast.error('保存失败，请重试');
    } finally {
      setIsLoading(false);
    }
  };

  // 渲染评分星星
  const renderStars = () => {
    return (
      <div className="flex gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            type="button"
            onClick={() => setFormData({ ...formData, rating: star })}
            className="transition-transform hover:scale-110"
            disabled={!isCompleting && !isEditing}
          >
            <Star
              className={`w-6 h-6 ${
                star <= (formData.rating || 0)
                  ? 'fill-yellow-400 text-yellow-400'
                  : 'text-gray-300'
              } ${
                !isCompleting && !isEditing ? 'cursor-not-allowed' : 'cursor-pointer'
              }`}
            />
          </button>
        ))}
      </div>
    );
  };

  return (
    <>
      {/* 主模态框 */}
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
            <h3 className="font-serif text-paper-800 font-bold text-lg">
              面试详情
            </h3>
            <p className="text-xs text-paper-500 mt-1">
              面试记录 ID: {interview.id}
            </p>
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
            <span
              className={`px-3 py-1 rounded-full text-sm font-medium border ${statusInfo.color}`}
            >
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
                  <span className="text-sm font-medium text-paper-800">
                    {interview.interviewType}
                  </span>
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
                  <span className="text-sm font-medium text-paper-800">
                    {interview.durationMinutes} 分钟
                  </span>
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
                      <span className="text-sm font-medium text-paper-800">
                        {interview.interviewerName}
                      </span>
                    </div>
                  )}

                  {interview.interviewerTitle && (
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-paper-600">职位</span>
                      <span className="text-sm font-medium text-paper-800">
                        {interview.interviewerTitle}
                      </span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* 评分和反馈区 */}
          <div className="space-y-3">
            <h4 className="text-sm font-bold text-paper-700 border-b-2 border-paper-300 pb-2">
              评分与反馈
            </h4>

            <div className="p-4 bg-white rounded-lg border-2 border-paper-300 space-y-3">
                {/* 评分 */}
                <div>
                  <label className="text-sm text-paper-600 block mb-2">
                    面试评分
                  </label>
                  {renderStars()}
                </div>

                {/* 反馈 */}
                <div>
                  <label className="text-sm text-paper-600 block mb-2">
                    面试反馈
                  </label>
                  <textarea
                    value={formData.feedback}
                    onChange={(e) =>
                      setFormData({ ...formData, feedback: e.target.value })
                    }
                    disabled={!isCompleting && !isEditing}
                    placeholder="记录面试反馈、印象等..."
                    className={`w-full p-3 rounded-lg border-2 border-paper-300 text-sm focus:border-accent-blue focus:outline-none resize-none ${
                      !isCompleting && !isEditing
                        ? 'bg-paper-50 cursor-not-allowed'
                        : 'bg-white'
                    }`}
                    rows={3}
                  />
                </div>

                {/* 技术问题 */}
                <div>
                  <label className="text-sm text-paper-600 block mb-2">
                    技术问题
                  </label>
                  <textarea
                    value={formData.technicalQuestions}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        technicalQuestions: e.target.value,
                      })
                    }
                    disabled={!isCompleting && !isEditing}
                    placeholder="记录面试中遇到的技术问题..."
                    className={`w-full p-3 rounded-lg border-2 border-paper-300 text-sm focus:border-accent-blue focus:outline-none resize-none ${
                      !isCompleting && !isEditing
                        ? 'bg-paper-50 cursor-not-allowed'
                        : 'bg-white'
                    }`}
                    rows={3}
                  />
                </div>
              </div>
            </div>
          

          {/* 备注 */}
          {interview.notes && !isCompleting && (
            <div className="space-y-3">
              <h4 className="text-sm font-bold text-paper-700 border-b-2 border-paper-300 pb-2">
                备注
              </h4>

              <div className="p-4 bg-white rounded-lg border-2 border-paper-300">
                <p className="text-sm text-paper-700 whitespace-pre-wrap">
                  {interview.notes}
                </p>
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

        {/* 底部操作按钮 */}
        <div className="p-5 border-t-2 border-paper-300 space-y-2">
          {isCompleting ? (
            // 标记完成模式的按钮
            <div className="flex gap-2">
              <button
                onClick={handleComplete}
                disabled={isLoading}
                style={{ backgroundColor: '#4a7c59', color: 'white' }}
                className="flex-1 px-4 py-3 rounded-lg hover:opacity-90 transition-opacity font-medium border-2 border-[#3d6649] disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                <Check className="w-4 h-4" />
                {isLoading ? '保存中...' : '确认完成'}
              </button>
              <button
                onClick={() => {
                  setIsCompleting(false);
                  setFormData({
                    rating: interview.rating || 0,
                    feedback: interview.feedback || '',
                    technicalQuestions: interview.technicalQuestions || '',
                  });
                }}
                disabled={isLoading}
                className="px-4 py-3 bg-paper-600 text-paper-50 rounded-lg hover:bg-paper-700 transition-colors font-medium border-2 border-paper-500 disabled:opacity-50"
              >
                取消
              </button>
            </div>
          ) : isEditing ? (
            // 编辑模式的按钮
            <div className="flex gap-2">
              <button
                onClick={handleSaveEdit}
                disabled={isLoading}
                style={{ backgroundColor: '#5a8ca8', color: 'white' }}
                className="flex-1 px-4 py-3 rounded-lg hover:opacity-90 transition-opacity font-medium border-2 border-[#4a7a94] disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                <Check className="w-4 h-4" />
                {isLoading ? '保存中...' : '保存'}
              </button>
              <button
                onClick={() => {
                  setIsEditing(false);
                  setFormData({
                    rating: interview.rating || 0,
                    feedback: interview.feedback || '',
                    technicalQuestions: interview.technicalQuestions || '',
                  });
                }}
                disabled={isLoading}
                className="px-4 py-3 bg-paper-600 text-paper-50 rounded-lg hover:bg-paper-700 transition-colors font-medium border-2 border-paper-500 disabled:opacity-50"
              >
                取消
              </button>
            </div>
          ) : (
            // 默认模式的按钮
            <>
              {(interview.status === 'SCHEDULED' || interview.status === 'INTERVIEW') && (
                <div className="space-y-2">
                  <button
                    onClick={() => setIsCompleting(true)}
                    disabled={isLoading}
                    style={{ backgroundColor: '#4a7c59', color: 'white' }}
                    className="w-full px-4 py-3 rounded-lg hover:opacity-90 transition-opacity font-medium border-2 border-[#3d6649] disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                  >
                    <Check className="w-5 h-5" />
                    标记完成
                  </button>
                  <div className="flex gap-2">
                    <button
                      onClick={handleNoShow}
                      disabled={isLoading}
                      className="flex-1 px-4 py-3 bg-paper-600 text-paper-50 rounded-lg hover:bg-paper-700 transition-colors font-medium border-2 border-paper-500 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                    >
                      <UserX className="w-4 h-4" />
                      未参加
                    </button>
                    <button
                      onClick={handleCancel}
                      disabled={isLoading}
                      style={{ backgroundColor: '#c44d4d', color: 'white' }}
                      className="flex-1 px-4 py-3 rounded-lg hover:opacity-90 transition-opacity font-medium border-2 border-[#a33d3d] disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                    >
                      <XCircle className="w-4 h-4" />
                      取消面试
                    </button>
                  </div>
                </div>
              )}

              {(interview.status === 'COMPLETED' ||
                interview.status === 'CANCELLED' ||
                interview.status === 'NO_SHOW') && (
                <button
                  onClick={() => setIsEditing(true)}
                  disabled={isLoading}
                  style={{ backgroundColor: '#5a8ca8', color: 'white' }}
                  className="w-full px-4 py-3 rounded-lg hover:opacity-90 transition-opacity font-medium border-2 border-[#4a7a94] disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                >
                  <Edit2 className="w-5 h-5" />
                  编辑反馈信息
                </button>
              )}

              <button
                onClick={onClose}
                className="w-full px-6 py-3 bg-paper-700 text-paper-50 rounded-lg hover:bg-paper-800 transition-colors text-base font-medium border-2 border-paper-600 shadow-md"
              >
                关闭
              </button>
            </>
          )}
        </div>
      </div>
    </div>

    {/* 确认弹窗 - 独立的 fixed 层，覆盖整个视口 */}
    {showCancelConfirm && (
      <div className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-fade-in">
        <div className="bg-white rounded-lg border-2 border-paper-300 shadow-xl p-6 max-w-sm w-full animate-scale-in">
          <div className="text-center">
            <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-accent-red/10 mb-4">
              <XCircle className="h-6 w-6 text-accent-red" />
            </div>
            <h3 className="text-lg font-serif font-bold text-paper-800 mb-2">
              取消面试
            </h3>
            <p className="text-sm text-paper-600 mb-6">
              确定要取消这个面试吗？此操作将把面试状态标记为已取消。
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setShowCancelConfirm(false)}
                disabled={isLoading}
                className="flex-1 px-4 py-2 bg-paper-100 text-paper-700 rounded-lg hover:bg-paper-200 transition-colors font-medium border-2 border-paper-300 disabled:opacity-50"
              >
                再想想
              </button>
              <button
                onClick={confirmCancel}
                disabled={isLoading}
                style={{ backgroundColor: '#c44d4d', color: 'white' }}
                className="flex-1 px-4 py-2 rounded-lg hover:opacity-90 transition-opacity font-medium border-2 border-[#a33d3d] disabled:opacity-50"
              >
                {isLoading ? '处理中...' : '确认取消'}
              </button>
            </div>
          </div>
        </div>
      </div>
    )}

    {showNoShowConfirm && (
      <div className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-fade-in">
        <div className="bg-white rounded-lg border-2 border-paper-300 shadow-xl p-6 max-w-sm w-full animate-scale-in">
          <div className="text-center">
            <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-paper-200 mb-4">
              <UserX className="h-6 w-6 text-paper-600" />
            </div>
            <h3 className="text-lg font-serif font-bold text-paper-800 mb-2">
              标记为未参加
            </h3>
            <p className="text-sm text-paper-600 mb-6">
              确定要标记为未参加吗？此操作将把面试状态标记为未参加。
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setShowNoShowConfirm(false)}
                disabled={isLoading}
                className="flex-1 px-4 py-2 bg-paper-100 text-paper-700 rounded-lg hover:bg-paper-200 transition-colors font-medium border-2 border-paper-300 disabled:opacity-50"
              >
                再想想
              </button>
              <button
                onClick={confirmNoShow}
                disabled={isLoading}
                className="flex-1 px-4 py-2 bg-paper-600 text-paper-50 rounded-lg hover:bg-paper-700 transition-colors font-medium border-2 border-paper-500 disabled:opacity-50"
              >
                {isLoading ? '处理中...' : '确认标记'}
              </button>
            </div>
          </div>
        </div>
      </div>
    )}
    </>
  );
}
