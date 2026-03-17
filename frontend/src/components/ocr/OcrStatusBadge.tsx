import { clsx } from 'clsx';
import { Badge, type BadgeVariant } from '@/components/common';
import { Loader2, CheckCircle, AlertCircle, Clock } from 'lucide-react';
import type { OcrStatus } from '@/types/ocr';

export interface OcrStatusBadgeProps {
  status: OcrStatus;
  confidence?: number;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const statusConfig: Record<OcrStatus, {
  variant: BadgeVariant;
  text: string;
  icon: typeof Clock;
}> = {
  idle: {
    variant: 'secondary',
    text: '待识别',
    icon: Clock,
  },
  pending: {
    variant: 'info',
    text: '识别中',
    icon: Loader2,
  },
  success: {
    variant: 'success',
    text: '识别成功',
    icon: CheckCircle,
  },
  failed: {
    variant: 'error',
    text: '识别失败',
    icon: AlertCircle,
  },
};

export function OcrStatusBadge({
  status,
  confidence,
  size = 'md',
  className,
}: OcrStatusBadgeProps) {
  const config = statusConfig[status];
  const Icon = config.icon;
  const isAnimating = status === 'pending';

  return (
    <div className={clsx('inline-flex items-center gap-1.5', className)}>
      <Badge variant={config.variant} size={size}>
        <Icon className={clsx(
          'w-3 h-3',
          isAnimating && 'animate-spin'
        )} />
        {config.text}
      </Badge>
      {status === 'success' && confidence !== undefined && (
        <span className="text-paper-500 text-xs">
          {(confidence * 100).toFixed(0)}%
        </span>
      )}
    </div>
  );
}

export interface OcrStatusOverlayProps {
  status: OcrStatus;
  error?: string;
  className?: string;
}

export function OcrStatusOverlay({
  status,
  error,
  className,
}: OcrStatusOverlayProps) {
  if (status === 'idle') return null;

  const bgColors: Record<OcrStatus, string> = {
    idle: 'bg-transparent',
    pending: 'bg-status-pending/90',
    success: 'bg-status-success/90',
    failed: 'bg-status-error/90',
  };

  return (
    <div
      className={clsx(
        'absolute inset-0 flex items-center justify-center gap-2',
        'text-white text-xs font-medium',
        'transition-opacity duration-200',
        bgColors[status],
        className
      )}
    >
      {status === 'pending' && (
        <>
          <Loader2 className="w-3 h-3 animate-spin" />
          <span>识别中...</span>
        </>
      )}
      {status === 'success' && (
        <>
          <CheckCircle className="w-3 h-3" />
          <span>识别成功</span>
        </>
      )}
      {status === 'failed' && (
        <>
          <AlertCircle className="w-3 h-3" />
          <span>{error || '识别失败'}</span>
        </>
      )}
    </div>
  );
}
