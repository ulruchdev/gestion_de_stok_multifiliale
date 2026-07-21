import { cn } from '../lib/utils';
import { PackageOpen, AlertCircle, SearchX, Ban, type LucideIcon } from 'lucide-react';
import { Button } from './Button';
import type { ButtonProps } from './Button';

type EmptyStateVariant = 'default' | 'error' | 'no-results' | 'no-data' | 'no-permissions';

interface EmptyStateProps {
  variant?: EmptyStateVariant;
  title: string;
  description?: string;
  icon?: LucideIcon;
  action?: {
    label: string;
    onClick: () => void;
    variant?: ButtonProps['variant'];
  };
  className?: string;
}

const variantConfig: Record<EmptyStateVariant, {
  icon: LucideIcon;
  iconBg: string;
  iconColor: string;
}> = {
  default: {
    icon: PackageOpen,
    iconBg: 'bg-[var(--brand-primary-bg)]',
    iconColor: 'text-[var(--brand-primary)]',
  },
  error: {
    icon: AlertCircle,
    iconBg: 'bg-[var(--stock-danger-bg)]',
    iconColor: 'text-[var(--stock-danger)]',
  },
  'no-results': {
    icon: SearchX,
    iconBg: 'bg-[var(--brand-canvas-soft)]',
    iconColor: 'text-[var(--brand-ink-muted)]',
  },
  'no-data': {
    icon: PackageOpen,
    iconBg: 'bg-[var(--brand-canvas-soft)]',
    iconColor: 'text-[var(--brand-ink-muted)]',
  },
  'no-permissions': {
    icon: Ban,
    iconBg: 'bg-[var(--stock-warning-bg)]',
    iconColor: 'text-[var(--stock-warning)]',
  },
};

function EmptyState({
  variant = 'default',
  title,
  description,
  icon: CustomIcon,
  action,
  className,
}: EmptyStateProps) {
  const config = variantConfig[variant];
  const IconComponent = CustomIcon || config.icon;

  return (
    <div
      className={cn(
        'flex flex-col items-center justify-center py-16 px-4 text-center animate-fade-in',
        className,
      )}
    >
      <div className={cn(
        'flex items-center justify-center h-14 w-14 rounded-full mb-5',
        config.iconBg,
      )}>
        <IconComponent className={cn('h-7 w-7', config.iconColor)} />
      </div>

      <h3 className="text-base font-semibold font-display text-[var(--brand-ink)] mb-1.5">
        {title}
      </h3>

      {description && (
        <p className="text-sm text-[var(--brand-ink-muted)] max-w-sm mb-6">
          {description}
        </p>
      )}

      {action && (
        <Button variant={action.variant || 'default'} onClick={action.onClick}>
          {action.label}
        </Button>
      )}
    </div>
  );
}

EmptyState.displayName = 'EmptyState';

export { EmptyState };
export type { EmptyStateProps, EmptyStateVariant };
