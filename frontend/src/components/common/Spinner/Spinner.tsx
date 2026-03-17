import { clsx } from 'clsx';
import { Loader2 } from 'lucide-react';

export interface SpinnerProps {
  size?: 'sm' | 'md' | 'lg' | 'xl';
  className?: string;
}

const sizeStyles: Record<'sm' | 'md' | 'lg' | 'xl', string> = {
  sm: 'w-4 h-4',
  md: 'w-6 h-6',
  lg: 'w-8 h-8',
  xl: 'w-12 h-12',
};

export function Spinner({ size = 'md', className }: SpinnerProps) {
  return (
    <Loader2
      className={clsx(
        'animate-spin text-accent-amber',
        sizeStyles[size],
        className
      )}
    />
  );
}

export interface LoadingOverlayProps {
  message?: string;
}

export function LoadingOverlay({ message = '加载中...' }: LoadingOverlayProps) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 p-8">
      <Spinner size="lg" />
      <p className="text-paper-500 text-sm">{message}</p>
    </div>
  );
}
