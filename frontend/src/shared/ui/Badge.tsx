import { cn } from '../lib/utils';
import { cva, type VariantProps } from 'class-variance-authority';
import { X } from 'lucide-react';

const badgeVariants = cva(
  'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2',
  {
    variants: {
      variant: {
        default: 'border-transparent bg-primary text-primary-foreground hover:bg-primary/80',
        secondary: 'border-transparent bg-secondary text-secondary-foreground hover:bg-secondary/80',
        destructive: 'border-transparent bg-destructive text-destructive-foreground hover:bg-destructive/80',
        outline: 'text-foreground',
        soft: 'border-transparent bg-[var(--brand-primary-bg)] text-[var(--brand-primary)]',
        success: 'border-transparent bg-[var(--stock-success-bg)] text-[var(--stock-success)]',
        warning: 'border-transparent bg-[var(--stock-warning-bg)] text-[var(--stock-warning)]',
        // Statuts de commande StockMaster (C-011) — utilisant les tokens
        en_preparation: 'border-transparent bg-[var(--stock-warning-bg)] text-[var(--stock-warning)]',
        validee: 'border-transparent bg-[var(--stock-success-bg)] text-[var(--stock-success)]',
        livree: 'border-transparent bg-[var(--brand-primary-bg)] text-[var(--brand-primary)]',
        annulee: 'border-transparent bg-[var(--stock-danger-bg)] text-[var(--stock-danger)]',
        en_attente: 'border-transparent bg-[var(--brand-primary-bg)] text-[var(--brand-ink-muted)]',
        partiel: 'border-transparent bg-[var(--stock-warning-bg)] text-[var(--stock-warning)]',
        rembourse: 'border-transparent bg-[var(--stock-success-bg)] text-[var(--stock-success)]',
      },
      size: {
        default: 'px-2.5 py-0.5 text-xs',
        sm: 'px-2 py-0 text-[10px]',
        lg: 'px-3 py-1 text-sm',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  },
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {
  removable?: boolean;
  onRemove?: () => void;
  dot?: boolean;
}

export function Badge({ className, variant, size, removable, onRemove, dot, children, ...props }: BadgeProps) {
  return (
    <div className={cn(badgeVariants({ variant, size }), className)} {...props}>
      {dot && (
        <span
          className={cn(
            'mr-1.5 h-1.5 w-1.5 rounded-full',
            variant === 'success' || variant === 'validee' || variant === 'rembourse' ? 'bg-current' :
            variant === 'warning' || variant === 'en_preparation' || variant === 'partiel' ? 'bg-current' :
            variant === 'destructive' || variant === 'annulee' ? 'bg-current' :
            'bg-current',
          )}
        />
      )}
      {children}
      {removable && (
        <button
          onClick={(e) => {
            e.stopPropagation();
            onRemove?.();
          }}
          className="ml-1.5 rounded-full p-0.5 hover:bg-black/10 transition-colors"
          aria-label="Supprimer"
        >
          <X className="h-3 w-3" />
        </button>
      )}
    </div>
  );
}
