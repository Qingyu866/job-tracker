import { type HTMLAttributes } from 'react';
import { clsx } from 'clsx';

export type BadgeVariant = 'default' | 'secondary' | 'outline' | 'success' | 'warning' | 'error' | 'info';

export interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  variant?: BadgeVariant;
  size?: 'sm' | 'md' | 'lg';
}

const variantStyles: Record<BadgeVariant, string> = {
  default: 'bg-paper-200 text-paper-700',
  secondary: 'bg-paper-100 text-paper-600',
  outline: 'border border-paper-300 text-paper-600 bg-transparent',
  success: 'bg-accent-green/20 text-accent-green',
  warning: 'bg-amber-100 text-amber-700',
  error: 'bg-accent-red/20 text-accent-red',
  info: 'bg-accent-blue/20 text-accent-blue',
};

const sizeStyles: Record<'sm' | 'md' | 'lg', string> = {
  sm: 'px-1.5 py-0.5 text-xs',
  md: 'px-2 py-0.5 text-sm',
  lg: 'px-2.5 py-1 text-base',
};

export function Badge({
  variant = 'default',
  size = 'md',
  className,
  children,
  ...props
}: BadgeProps) {
  return (
    <span
      className={clsx(
        'inline-flex items-center font-medium rounded-md',
        variantStyles[variant],
        sizeStyles[size],
        className
      )}
      {...props}
    >
      {children}
    </span>
  );
}
