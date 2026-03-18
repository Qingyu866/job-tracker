import { type HTMLAttributes } from 'react';
import { clsx } from 'clsx';
import { User } from 'lucide-react';

export type AvatarSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl';

export interface AvatarProps extends HTMLAttributes<HTMLDivElement> {
  src?: string;
  alt?: string;
  name?: string;
  size?: AvatarSize;
  fallbackIcon?: boolean;
}

const sizeStyles: Record<AvatarSize, { container: string; text: string; icon: string }> = {
  xs: { container: 'w-6 h-6', text: 'text-xs', icon: 'w-3 h-3' },
  sm: { container: 'w-8 h-8', text: 'text-sm', icon: 'w-4 h-4' },
  md: { container: 'w-10 h-10', text: 'text-base', icon: 'w-5 h-5' },
  lg: { container: 'w-12 h-12', text: 'text-lg', icon: 'w-6 h-6' },
  xl: { container: 'w-16 h-16', text: 'text-xl', icon: 'w-8 h-8' },
};

function getInitials(name: string): string {
  return name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
}

function stringToColor(str: string): string {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  const colors = [
    '#d4a574',
    '#8fbc8f',
    '#7ba3b5',
    '#9f7fb5',
    '#e53d3d',
    '#f59e0b',
    '#10b981',
    '#3b82f6',
  ];
  return colors[Math.abs(hash) % colors.length];
}

export function Avatar({
  src,
  alt,
  name,
  size = 'md',
  fallbackIcon = false,
  className,
  style,
  ...props
}: AvatarProps) {
  const styles = sizeStyles[size];
  const initials = name ? getInitials(name) : null;
  const bgColor = name ? stringToColor(name) : '#c9bd9f';

  if (src) {
    return (
      <div
        className={clsx(
          'relative rounded-full overflow-hidden bg-paper-200',
          styles.container,
          className
        )}
        {...props}
      >
        <img
          src={src}
          alt={alt || name || 'Avatar'}
          className="w-full h-full object-cover"
        />
      </div>
    );
  }

  if (initials && !fallbackIcon) {
    return (
      <div
        className={clsx(
          'rounded-full flex items-center justify-center text-white font-medium',
          styles.container,
          styles.text,
          className
        )}
        style={{ backgroundColor: bgColor, ...style }}
        {...props}
      >
        {initials}
      </div>
    );
  }

  return (
    <div
      className={clsx(
        'rounded-full flex items-center justify-center bg-paper-200 text-paper-500',
        styles.container,
        className
      )}
      {...props}
    >
      <User className={styles.icon} />
    </div>
  );
}

export interface AvatarGroupProps extends HTMLAttributes<HTMLDivElement> {
  max?: number;
  children: React.ReactNode;
}

export function AvatarGroup({ max = 4, children, className, ...props }: AvatarGroupProps) {
  const childArray = Array.isArray(children) ? children : [children];
  const visibleChildren = childArray.slice(0, max);
  const remainingCount = childArray.length - max;

  return (
    <div className={clsx('flex -space-x-2', className)} {...props}>
      {visibleChildren.map((child, index) => (
        <div key={index} className="ring-2 ring-paper-50 rounded-full">
          {child}
        </div>
      ))}
      {remainingCount > 0 && (
        <div className="w-8 h-8 rounded-full bg-paper-200 flex items-center justify-center text-xs font-medium text-paper-600 ring-2 ring-paper-50">
          +{remainingCount}
        </div>
      )}
    </div>
  );
}
