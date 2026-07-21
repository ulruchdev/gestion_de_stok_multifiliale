import { type HTMLAttributes, type ReactNode, forwardRef } from 'react';
import { cn } from '../lib/utils';

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  variant?: 'default' | 'elevated' | 'bordered' | 'interactive';
  padding?: 'none' | 'sm' | 'md' | 'lg';
}

const Card = forwardRef<HTMLDivElement, CardProps>(
  ({ className, variant = 'default', padding = 'md', children, ...props }, ref) => {
    return (
      <div
        ref={ref}
        className={cn(
          'rounded-lg bg-background transition-all duration-[var(--brand-duration-fast)]',
          {
            'border border-[var(--brand-hairline)] shadow-sm': variant === 'default',
            'border border-[var(--brand-hairline)] shadow-md hover:shadow-lg': variant === 'elevated',
            'border-2 border-[var(--brand-hairline)]': variant === 'bordered',
            'border border-[var(--brand-hairline)] shadow-sm cursor-pointer hover:shadow-md hover:border-[var(--brand-primary-border)]': variant === 'interactive',
          },
          className,
        )}
        {...props}
      >
        {children}
      </div>
    );
  },
);
Card.displayName = 'Card';

interface CardHeaderProps extends HTMLAttributes<HTMLDivElement> {
  padding?: 'none' | 'sm' | 'md' | 'lg';
}

const CardHeader = forwardRef<HTMLDivElement, CardHeaderProps>(
  ({ className, padding = 'md', ...props }, ref) => {
    return (
      <div
        ref={ref}
        className={cn(
          'flex items-center justify-between border-b border-[var(--brand-hairline)]',
          {
            'p-0': padding === 'none',
            'p-3': padding === 'sm',
            'p-4': padding === 'md',
            'p-6': padding === 'lg',
          },
          className,
        )}
        {...props}
      />
    );
  },
);
CardHeader.displayName = 'CardHeader';

interface CardTitleProps extends HTMLAttributes<HTMLHeadingElement> {}

const CardTitle = forwardRef<HTMLHeadingElement, CardTitleProps>(
  ({ className, ...props }, ref) => {
    return (
      <h3
        ref={ref}
        className={cn('text-base font-semibold font-display', className)}
        {...props}
      />
    );
  },
);
CardTitle.displayName = 'CardTitle';

interface CardDescriptionProps extends HTMLAttributes<HTMLParagraphElement> {}

const CardDescription = forwardRef<HTMLParagraphElement, CardDescriptionProps>(
  ({ className, ...props }, ref) => {
    return (
      <p
        ref={ref}
        className={cn('text-sm text-[var(--brand-ink-muted)]', className)}
        {...props}
      />
    );
  },
);
CardDescription.displayName = 'CardDescription';

interface CardContentProps extends HTMLAttributes<HTMLDivElement> {
  padding?: 'none' | 'sm' | 'md' | 'lg';
}

const CardContent = forwardRef<HTMLDivElement, CardContentProps>(
  ({ className, padding = 'md', ...props }, ref) => {
    return (
      <div
        ref={ref}
        className={cn(
          {
            'p-0': padding === 'none',
            'p-3': padding === 'sm',
            'p-4': padding === 'md',
            'p-6': padding === 'lg',
          },
          className,
        )}
        {...props}
      />
    );
  },
);
CardContent.displayName = 'CardContent';

interface CardFooterProps extends HTMLAttributes<HTMLDivElement> {
  padding?: 'none' | 'sm' | 'md' | 'lg';
}

const CardFooter = forwardRef<HTMLDivElement, CardFooterProps>(
  ({ className, padding = 'md', ...props }, ref) => {
    return (
      <div
        ref={ref}
        className={cn(
          'flex items-center justify-end gap-2 border-t border-[var(--brand-hairline)]',
          {
            'p-0': padding === 'none',
            'p-3': padding === 'sm',
            'p-4': padding === 'md',
            'p-6': padding === 'lg',
          },
          className,
        )}
        {...props}
      />
    );
  },
);
CardFooter.displayName = 'CardFooter';

export { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter };
export type { CardProps, CardHeaderProps, CardTitleProps, CardDescriptionProps, CardContentProps, CardFooterProps };
