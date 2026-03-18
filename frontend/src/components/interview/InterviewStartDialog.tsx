import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FileText, Check, ChevronRight, ChevronLeft } from 'lucide-react';
import { Button, Badge, Spinner } from '@/components/common';
import { resumeApi } from '@/services/resumeApi';
import { useUserStore } from '@/store/userStore';
import type { UserResume } from '@/types/resume';

export interface InterviewStartDialogProps {
  applicationId: number;
  companyName: string;
  jobTitle: string;
  onStart: () => Promise<void>;
  onCancel: () => void;
  starting?: boolean;
}

export function InterviewStartDialog({
  applicationId: _applicationId,
  companyName = '未知公司',
  jobTitle = '未知职位',
  onStart,
  onCancel,
  starting = false,
}: InterviewStartDialogProps) {
  const navigate = useNavigate();
  const { userInfo } = useUserStore();
  const [step, setStep] = useState<'select-resume' | 'confirm'>('select-resume');
  const [resumes, setResumes] = useState<UserResume[]>([]);
  const [selectedResume, setSelectedResume] = useState<UserResume | null>(null);
  const [loading, setLoading] = useState(true);

  console.log('InterviewStartDialog render:', { 
    userInfo, 
    userId: userInfo?.id,
    resumesCount: resumes.length 
  });

  useEffect(() => {
    console.log('InterviewStartDialog useEffect triggered, userInfo:', userInfo);
    if (userInfo?.id) {
      loadResumes();
    } else {
      console.warn('InterviewStartDialog: userInfo.id is not available, userInfo:', userInfo);
      setLoading(false);
    }
  }, [userInfo?.id]);

  const loadResumes = async () => {
    if (!userInfo?.id) {
      console.warn('loadResumes: No user ID available');
      return;
    }
    
    console.log('Loading resumes for user:', userInfo.id);
    
    try {
      setLoading(true);
      const data = await resumeApi.getList(userInfo.id);
      console.log('Loaded resumes:', data);
      setResumes(data);
      const defaultResume = data.find((r: UserResume) => r.isDefault);
      if (defaultResume) {
        setSelectedResume(defaultResume);
      }
    } catch (error) {
      console.error('加载简历列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleStart = async () => {
    if (!selectedResume) return;
    await onStart();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 animate-fade-in">
      <div className="bg-white rounded-2xl shadow-paper-xl w-full max-w-lg mx-4 max-h-[90vh] overflow-hidden animate-scale-in">
        <div className="p-6 border-b border-paper-200">
          <h2 className="text-xl font-semibold text-paper-700">开始模拟面试</h2>
        </div>

        <div className="p-6 overflow-y-auto max-h-[60vh]">
          {step === 'select-resume' && (
            <div className="space-y-4">
              <div>
                <h3 className="text-paper-700 font-medium mb-1">选择要使用的简历</h3>
                <p className="text-paper-500 text-sm">面试官会基于你的简历内容提问</p>
              </div>

              {loading ? (
                <div className="flex items-center justify-center py-8">
                  <Spinner size="lg" />
                </div>
              ) : resumes.length === 0 ? (
                <div className="text-center py-8">
                  <FileText className="w-12 h-12 text-paper-300 mx-auto mb-3" />
                  <p className="text-paper-500 mb-4">暂无简历，请先创建简历</p>
                  <Button variant="outline" onClick={() => navigate('/resumes/new', { state: { returnUrl: window.location.pathname } })}>
                    创建简历
                  </Button>
                </div>
              ) : (
                <div className="space-y-3">
                  {resumes.map((resume) => (
                    <ResumeCard
                      key={resume.resumeId}
                      resume={resume}
                      selected={selectedResume?.resumeId === resume.resumeId}
                      onSelect={() => setSelectedResume(resume)}
                    />
                  ))}
                </div>
              )}
            </div>
          )}

          {step === 'confirm' && selectedResume && (
            <div className="space-y-4">
              <h3 className="text-paper-700 font-medium">确认面试信息</h3>

              <div className="bg-paper-50 rounded-lg p-4 space-y-3">
                <InfoRow label="目标公司" value={companyName} />
                <InfoRow label="职位" value={jobTitle} />
                <InfoRow label="使用简历" value={selectedResume.resumeName} />
                {selectedResume.workYears !== undefined && (
                  <InfoRow label="工作年限" value={`${selectedResume.workYears} 年`} />
                )}
              </div>

              <div className="bg-accent-blue/10 rounded-lg p-4 flex gap-3">
                <div className="text-accent-blue flex-shrink-0">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <p className="text-paper-600 text-sm">
                  面试将围绕你的简历内容和岗位要求进行，预计需要 15-20 分钟
                </p>
              </div>
            </div>
          )}
        </div>

        <div className="p-6 border-t border-paper-200 bg-paper-50 flex justify-between">
          {step === 'select-resume' ? (
            <>
              <Button variant="outline" onClick={onCancel}>
                取消
              </Button>
              <Button
                disabled={!selectedResume || resumes.length === 0}
                onClick={() => setStep('confirm')}
                rightIcon={<ChevronRight className="w-4 h-4" />}
              >
                下一步
              </Button>
            </>
          ) : (
            <>
              <Button
                variant="outline"
                onClick={() => setStep('select-resume')}
                leftIcon={<ChevronLeft className="w-4 h-4" />}
              >
                上一步
              </Button>
              <Button
                onClick={handleStart}
                loading={starting}
                disabled={!selectedResume}
              >
                开始面试
              </Button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

interface ResumeCardProps {
  resume: UserResume;
  selected: boolean;
  onSelect: () => void;
}

function ResumeCard({ resume, selected, onSelect }: ResumeCardProps) {
  return (
    <button
      onClick={onSelect}
      className={`w-full text-left p-4 rounded-lg border-2 transition-all hover:border-accent-amber/50 ${
        selected
          ? 'border-accent-amber bg-accent-amber/5'
          : 'border-paper-200 bg-white'
      }`}
    >
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="flex items-center gap-2">
            <FileText className="w-5 h-5 text-paper-400" />
            <span className="text-paper-700 font-medium">{resume.resumeName}</span>
            {resume.isDefault && (
              <Badge variant="secondary" size="sm">默认</Badge>
            )}
          </div>
          
          <div className="mt-2 flex items-center gap-4 text-sm text-paper-500">
            {resume.workYears !== undefined && (
              <span>工作年限: {resume.workYears} 年</span>
            )}
            {resume.currentPosition && (
              <span>当前职位: {resume.currentPosition}</span>
            )}
          </div>

          {resume.skills && resume.skills.length > 0 && (
            <div className="mt-2 flex flex-wrap gap-1">
              {resume.skills.slice(0, 5).map((skill, index) => (
                <Badge key={index} variant="outline" size="sm">
                  {skill.name}
                </Badge>
              ))}
              {resume.skills.length > 5 && (
                <Badge variant="outline" size="sm">
                  +{resume.skills.length - 5}
                </Badge>
              )}
            </div>
          )}
        </div>

        <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center flex-shrink-0 ${
          selected
            ? 'border-accent-amber bg-accent-amber text-white'
            : 'border-paper-300'
        }`}>
          {selected && <Check className="w-3 h-3" />}
        </div>
      </div>
    </button>
  );
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between">
      <span className="text-paper-500 text-sm">{label}</span>
      <span className="text-paper-700 font-medium">{value}</span>
    </div>
  );
}
