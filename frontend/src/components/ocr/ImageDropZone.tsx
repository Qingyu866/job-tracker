import { useState, useRef, type DragEvent, type ChangeEvent } from 'react';
import { clsx } from 'clsx';
import { Upload, FileText, AlertCircle } from 'lucide-react';
import { Spinner } from '@/components/common';
import { validateImageFile, validateImageDimensions } from '@/services/ocrApi';
import type { OcrImageType } from '@/types/ocr';

export interface ImageDropZoneProps {
  accept?: string;
  onUpload: (file: File) => void;
  ocrMode?: OcrImageType;
  maxSize?: number;
  disabled?: boolean;
  className?: string;
}

export function ImageDropZone({
  accept = 'image/jpeg,image/png,image/jpg,application/pdf',
  onUpload,
  ocrMode = 'general',
  maxSize = 10 * 1024 * 1024,
  disabled = false,
  className,
}: ImageDropZoneProps) {
  const [dragging, setDragging] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const validateFile = async (file: File): Promise<boolean> => {
    setError(null);

    const typeValidation = validateImageFile(file);
    if (!typeValidation.valid) {
      setError(typeValidation.error || '文件验证失败');
      return false;
    }

    if (file.size > maxSize) {
      setError(`文件大小不能超过 ${maxSize / 1024 / 1024}MB`);
      return false;
    }

    const dimensionValidation = await validateImageDimensions(file);
    if (!dimensionValidation.valid) {
      setError(dimensionValidation.error || '图片尺寸验证失败');
      return false;
    }

    return true;
  };

  const handleFile = async (file: File) => {
    const isValid = await validateFile(file);
    if (!isValid) return;

    setProcessing(true);
    try {
      await onUpload(file);
    } finally {
      setProcessing(false);
    }
  };

  const handleDrop = async (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setDragging(false);

    const file = e.dataTransfer.files[0];
    if (file) {
      await handleFile(file);
    }
  };

  const handleDragOver = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    if (!disabled) {
      setDragging(true);
    }
  };

  const handleDragLeave = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setDragging(false);
  };

  const handleFileSelect = async (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      await handleFile(file);
    }
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleClick = () => {
    if (!disabled && !processing) {
      fileInputRef.current?.click();
    }
  };

  const getModeHint = () => {
    switch (ocrMode) {
      case 'resume':
        return '上传简历图片，自动解析简历信息';
      case 'jd':
        return '上传职位描述图片，自动提取技能要求';
      default:
        return '上传图片进行文字识别';
    }
  };

  return (
    <div
      className={clsx(
        'relative border-2 border-dashed rounded-xl p-8 text-center cursor-pointer',
        'transition-all duration-200',
        'bg-paper-50',
        dragging && 'border-accent-amber bg-accent-amber/5',
        !dragging && !disabled && 'border-paper-300 hover:border-accent-amber hover:bg-paper-100',
        disabled && 'opacity-50 cursor-not-allowed',
        processing && 'cursor-wait',
        className
      )}
      onDrop={handleDrop}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onClick={handleClick}
    >
      <input
        ref={fileInputRef}
        type="file"
        accept={accept}
        onChange={handleFileSelect}
        disabled={disabled || processing}
        className="hidden"
      />

      {processing ? (
        <div className="flex flex-col items-center gap-3">
          <Spinner size="lg" />
          <p className="text-paper-600 font-medium">OCR 识别中...</p>
          <p className="text-paper-400 text-sm">请稍候，正在解析图片内容</p>
        </div>
      ) : (
        <div className="flex flex-col items-center gap-3">
          <div className="w-12 h-12 rounded-full bg-paper-200 flex items-center justify-center">
            {error ? (
              <AlertCircle className="w-6 h-6 text-accent-red" />
            ) : (
              <Upload className="w-6 h-6 text-paper-500" />
            )}
          </div>
          
          <div>
            <p className="text-paper-700 font-medium">
              {error || '拖拽图片到此处'}
            </p>
            {!error && (
              <>
                <p className="text-paper-500 text-sm mt-1">或点击选择文件</p>
                <p className="text-paper-400 text-xs mt-2">{getModeHint()}</p>
              </>
            )}
          </div>

          <div className="flex items-center gap-2 text-paper-400 text-xs">
            <FileText className="w-4 h-4" />
            <span>支持: JPG, PNG, PDF（最大 10MB）</span>
          </div>
        </div>
      )}
    </div>
  );
}
