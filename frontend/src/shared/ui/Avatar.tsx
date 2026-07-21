import { type HTMLAttributes, forwardRef } from 'react';
import { cn } from '../lib/utils';

type AvatarSize = 'sm' | 'md' | 'lg' | 'xl';
type AvatarShape = 'circle' | 'rounded' | 'square';
type AvatarStatus = 'online' | 'offline' | 'away';

interface AvatarProps extends HTMLAttributes<HTMLDivElement> {
  src?: string;
  alt?: string;
  fallback?: string;
  size?: AvatarSize;
  shape?: AvatarShape;
  status?: AvatarStatus;
}

const sizeMap: Record<AvatarSize, { container: string; text: string; status: string }> = {
  sm: { container: 'h-6 w-6', text: 'text-[10px]', status: 'h-1.5 w-1.5 border' },
  md: { container: 'h-8 w-8', text: 'text-xs', status: 'h-2 w-2 border' },
  lg: { container: 'h-10 w-10', text: 'text-sm', status: 'h-2.5 w-2.5 border-2' },
  xl: { container: 'h-14 w-14', text: 'text-base', status: 'h-3 w-3 border-2' },
};

const statusColorMap: Record<AvatarStatus, string> = {
  online: 'bg-[var(--stock-success)]',
  offline: 'bg-[var(--brand-ink-muted)]',
  away: 'bg-[var(--stock-warning)]',
};

const shapeClassMap: Record<AvatarShape, string> = {
  circle: 'rounded-full',
  rounded: 'rounded-lg',
  square: 'rounded-none',
};

function getInitials(fallback?: string): string {
  if (!fallback) return '?';
  const parts = fallback.trim().split(/\s+/);
  if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
  return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
}

const Avatar = forwardRef<HTMLDivElement, AvatarProps>(
  ({ className, src, alt = '', fallback, size = 'md', shape = 'circle', status, ...props }, ref) => {
    const sizeClasses = sizeMap[size];
    const initials = getInitials(fallback);

    return (
      <div
        ref={ref}
        className={cn('relative inline-flex shrink-0', className)}
        {...props}
      >
        {src ? (
          <img
            src={src}
            alt={alt}
            className={cn(
              'object-cover',
              sizeClasses.container,
              shapeClassMap[shape],
            )}
          />
        ) : (
          <div
            className={cn(
              'inline-flex items-center justify-center bg-[var(--brand-primary-bg)] text-[var(--brand-primary)] font-semibold',
              sizeClasses.container,
              sizeClasses.text,
              shapeClassMap[shape],
            )}
            aria-label={alt || fallback || 'Avatar'}
          >
            {initials}
          </div>
        )}

        {status && (
          <span
            className={cn(
              'absolute bottom-0 right-0 rounded-full border-white',
              statusColorMap[status],
              sizeClasses.status,
            )}
            aria-label={status}
          />
        )}
      </div>
    );
  },
);
Avatar.displayName = 'Avatar';

interface AvatarGroupProps extends HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  max?: number;
  size?: AvatarSize;
}

function AvatarGroup({ className, children, max = 4, size = 'md', ...props }: AvatarGroupProps) {
  const childrenArray = Array.isArray(children) ? children : [children];
  const visibleAvatars = childrenArray.slice(0, max);
  const overflow = childrenArray.length - max;

  const sizeOffsetMap: Record<AvatarSize, string> = {
    sm: '-ml-1.5',
    md: '-ml-2',
    lg: '-ml-2.5',
    xl: '-ml-3',
  };

  return (
    <div className={cn('flex items-center', className)} {...props}>
      {visibleAvatars.map((child, index) => (
        <div
          key={index}
          className={cn('ring-2 ring-background', index > 0 ? sizeOffsetMap[size] : '')}
        >
          {child}
        </div>
      ))}
      {overflow > 0 && (
        <div
          className={cn(
            'inline-flex items-center justify-center rounded-full ring-2 ring-background bg-[var(--brand-canvas-soft)] text-[var(--brand-ink-muted)] font-medium',
            sizeOffsetMap[size],
            sizeMap[size].container,
            sizeMap[size].text,
          )}
        >
          +{overflow}
        </div>
      )}
    </div>
  );
}
AvatarGroup.displayName = 'AvatarGroup';

export { Avatar, AvatarGroup };
export type { AvatarProps, AvatarSize, AvatarShape, AvatarStatus };
