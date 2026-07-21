import { cn } from '../lib/utils';

interface SkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: 'text' | 'circle' | 'rectangle' | 'card' | 'table-row';
  width?: string | number;
  height?: string | number;
}

function Skeleton({ className, variant = 'text', width, height, ...props }: SkeletonProps) {
  return (
    <div
      className={cn(
        'animate-pulse rounded-md bg-[var(--brand-hairline)]',
        {
          'h-4 w-full': variant === 'text',
          'h-10 w-10 rounded-full': variant === 'circle',
          'h-32 w-full': variant === 'rectangle',
          'h-48 w-full rounded-lg': variant === 'card',
          'h-12 w-full': variant === 'table-row',
        },
        className,
      )}
      style={{
        width: width ? (typeof width === 'number' ? `${width}px` : width) : undefined,
        height: height ? (typeof height === 'number' ? `${height}px` : height) : undefined,
      }}
      aria-hidden="true"
      {...props}
    />
  );
}

// Composition helpers
interface SkeletonTextProps {
  lines?: number;
  className?: string;
}

function SkeletonText({ lines = 3, className }: SkeletonTextProps) {
  return (
    <div className={cn('space-y-2', className)} aria-hidden="true">
      {Array.from({ length: lines }).map((_, i) => (
        <Skeleton
          key={i}
          variant="text"
          className={cn(
            i === lines - 1 ? 'w-3/4' : 'w-full',
            i === 0 ? 'h-5' : 'h-4',
          )}
        />
      ))}
    </div>
  );
}

interface SkeletonTableProps {
  rows?: number;
  columns?: number;
  className?: string;
}

function SkeletonTable({ rows = 5, columns = 4, className }: SkeletonTableProps) {
  return (
    <div className={cn('space-y-3', className)} aria-hidden="true">
      {/* Header */}
      <div className="flex gap-4">
        {Array.from({ length: columns }).map((_, i) => (
          <Skeleton key={`h-${i}`} className="h-5 flex-1" />
        ))}
      </div>
      {/* Rows */}
      {Array.from({ length: rows }).map((_, rowIdx) => (
        <div key={`r-${rowIdx}`} className="flex gap-4">
          {Array.from({ length: columns }).map((_, colIdx) => (
            <Skeleton key={`c-${rowIdx}-${colIdx}`} className="h-4 flex-1" />
          ))}
        </div>
      ))}
    </div>
  );
}

interface SkeletonCardProps {
  className?: string;
}

function SkeletonCard({ className }: SkeletonCardProps) {
  return (
    <div className={cn('space-y-4 rounded-lg border border-[var(--brand-hairline)] p-4', className)} aria-hidden="true">
      <div className="flex items-center gap-3">
        <Skeleton variant="circle" className="h-10 w-10" />
        <div className="flex-1 space-y-2">
          <Skeleton className="h-4 w-1/3" />
          <Skeleton className="h-3 w-1/4" />
        </div>
      </div>
      <SkeletonText lines={2} />
    </div>
  );
}

export { Skeleton, SkeletonText, SkeletonTable, SkeletonCard };
export type { SkeletonProps };
