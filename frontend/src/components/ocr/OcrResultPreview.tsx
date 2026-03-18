import { clsx } from 'clsx';
import { CheckCircle, AlertCircle, RefreshCw } from 'lucide-react';
import { Button, Badge, Spinner } from '@/components/common';
import type { OcrResult, OcrImageType, ResumeInfo, JdInfo } from '@/types/ocr';

export interface OcrResultPreviewProps {
  result: OcrResult;
  mode: OcrImageType;
  onConfirm?: (data: ResumeInfo | JdInfo | string) => void;
  onRetry?: () => void;
  className?: string;
}

export function OcrResultPreview({
  result,
  mode,
  onConfirm,
  onRetry,
  className,
}: OcrResultPreviewProps) {
  if (result.status === 'pending') {
    return (
      <div className={clsx(
        'flex flex-col items-center justify-center gap-4 p-8',
        'bg-paper-50 rounded-xl border-2 border-paper-400',
        className
      )}>
        <Spinner size="xl" />
        <p className="text-paper-600 font-medium">正在识别图片内容...</p>
        <p className="text-paper-400 text-sm">请稍候，AI 正在解析</p>
      </div>
    );
  }

  if (result.status === 'failed') {
    return (
      <div className={clsx(
        'flex flex-col items-center justify-center gap-4 p-8',
        'bg-accent-red/5 rounded-xl border border-accent-red/20',
        className
      )}>
        <div className="w-12 h-12 rounded-full bg-accent-red/10 flex items-center justify-center">
          <AlertCircle className="w-6 h-6 text-accent-red" />
        </div>
        <p className="text-paper-700 font-medium">识别失败</p>
        <p className="text-paper-500 text-sm text-center max-w-xs">
          {result.error || '无法识别图片内容，请确保图片清晰可见'}
        </p>
        {onRetry && (
          <Button variant="outline" size="sm" onClick={onRetry}>
            <RefreshCw className="w-4 h-4" />
            重新识别
          </Button>
        )}
      </div>
    );
  }

  return (
    <div className={clsx(
      'bg-[#f5f0e6] rounded-xl border-2 border-paper-400 shadow-paper',
      className
    )}>
      <div className="flex items-center gap-3 p-4 border-b-2 border-paper-300">
        <div className="w-8 h-8 rounded-full bg-accent-green/10 flex items-center justify-center">
          <CheckCircle className="w-5 h-5 text-accent-green" />
        </div>
        <span className="text-paper-700 font-medium">识别成功</span>
        {result.confidence !== undefined && (
          <Badge variant="success" size="sm">
            置信度: {(result.confidence * 100).toFixed(0)}%
          </Badge>
        )}
      </div>

      <div className="p-4">
        {mode === 'resume' && result.resumeData && (
          <ResumeDataPreview data={result.resumeData} />
        )}

        {mode === 'jd' && result.jdData && (
          <JdDataPreview data={result.jdData} />
        )}

        {mode === 'general' && result.text && (
          <TextPreview text={result.text} />
        )}
      </div>

      {onConfirm && (
        <div className="flex justify-end gap-3 p-4 border-t-2 border-paper-300 bg-paper-50 rounded-b-xl">
          <Button variant="outline" onClick={onRetry}>
            重新上传
          </Button>
          <Button onClick={() => {
            const data = mode === 'resume' 
              ? result.resumeData! 
              : mode === 'jd' 
                ? result.jdData! 
                : result.text!;
            onConfirm(data);
          }}>
            确认使用
          </Button>
        </div>
      )}
    </div>
  );
}

function ResumeDataPreview({ data }: { data: ResumeInfo }) {
  return (
    <div className="space-y-4">
      {data.name && (
        <div>
          <label className="text-paper-500 text-sm">姓名</label>
          <p className="text-paper-700 font-medium">{data.name}</p>
        </div>
      )}

      <div className="grid grid-cols-2 gap-4">
        {data.workYears !== undefined && (
          <div>
            <label className="text-paper-500 text-sm">工作年限</label>
            <p className="text-paper-700">{data.workYears} 年</p>
          </div>
        )}
        {data.currentPosition && (
          <div>
            <label className="text-paper-500 text-sm">当前职位</label>
            <p className="text-paper-700">{data.currentPosition}</p>
          </div>
        )}
      </div>

      {data.skills && data.skills.length > 0 && (
        <div>
          <label className="text-paper-500 text-sm mb-2 block">技能标签</label>
          <div className="flex flex-wrap gap-2">
            {data.skills.map((skill, index) => (
              <Badge key={index} variant="secondary" size="sm">
                {skill}
              </Badge>
            ))}
          </div>
        </div>
      )}

      {data.projects && data.projects.length > 0 && (
        <div>
          <label className="text-paper-500 text-sm mb-2 block">项目经历</label>
          <div className="space-y-2">
            {data.projects.slice(0, 3).map((project, index) => (
              <div key={index} className="p-3 bg-paper-50 rounded-lg">
                <p className="text-paper-700 font-medium">{project.name}</p>
                {project.role && (
                  <p className="text-paper-500 text-sm">{project.role}</p>
                )}
              </div>
            ))}
            {data.projects.length > 3 && (
              <p className="text-paper-400 text-sm">
                还有 {data.projects.length - 3} 个项目...
              </p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function JdDataPreview({ data }: { data: JdInfo }) {
  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        {data.companyName && (
          <div>
            <label className="text-paper-500 text-sm">公司名称</label>
            <p className="text-paper-700 font-medium">{data.companyName}</p>
          </div>
        )}
        {data.jobTitle && (
          <div>
            <label className="text-paper-500 text-sm">职位名称</label>
            <p className="text-paper-700 font-medium">{data.jobTitle}</p>
          </div>
        )}
      </div>

      {data.skills && data.skills.length > 0 && (
        <div>
          <label className="text-paper-500 text-sm mb-2 block">技能要求</label>
          <div className="flex flex-wrap gap-2">
            {data.skills.map((skill, index) => (
              <Badge key={index} variant="info" size="sm">
                {skill}
              </Badge>
            ))}
          </div>
        </div>
      )}

      {data.requirements && data.requirements.length > 0 && (
        <div>
          <label className="text-paper-500 text-sm mb-2 block">任职要求</label>
          <ul className="space-y-1">
            {data.requirements.slice(0, 3).map((req, index) => (
              <li key={index} className="text-paper-600 text-sm flex items-start gap-2">
                <span className="text-accent-amber mt-1">•</span>
                {req}
              </li>
            ))}
            {data.requirements.length > 3 && (
              <li className="text-paper-400 text-sm">
                还有 {data.requirements.length - 3} 条要求...
              </li>
            )}
          </ul>
        </div>
      )}
    </div>
  );
}

function TextPreview({ text }: { text: string }) {
  return (
    <div className="bg-paper-50 rounded-lg p-4 max-h-64 overflow-y-auto">
      <pre className="text-paper-700 text-sm whitespace-pre-wrap font-ui">
        {text}
      </pre>
    </div>
  );
}
