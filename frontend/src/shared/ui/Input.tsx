import { type InputHTMLAttributes, forwardRef, type ReactNode } from 'react';
import { cn } from '../lib/utils';

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  error?: string;
  warning?: string;
  success?: string;
  label?: string;
  description?: string;
  icon?: ReactNode;
  trailingIcon?: ReactNode;
  onTrailingIconClick?: () => void;
  inputSize?: 'default' | 'lg';
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, type, error, warning, success, label, description, icon, trailingIcon, onTrailingIconClick, inputSize, id, ...props }, ref) => {
    const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');

    return (
      <div className="space-y-1.5">
        {label && (
          <label
            htmlFor={inputId}
            className="text-sm font-medium leading-none text-[var(--brand-ink-muted)] peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
          >
            {label}
          </label>
        )}
        <div className="relative">
          {icon && (
            <div className="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-muted-foreground">
              {icon}
            </div>
          )}
          <input
            id={inputId}
            type={type}
            className={cn(
              'flex w-full rounded-lg border border-input bg-background text-sm ring-offset-background transition-all duration-[var(--brand-duration-fast)]',
              'file:border-0 file:bg-transparent file:text-sm file:font-medium',
              'placeholder:text-muted-foreground/50 placeholder:font-normal',
              'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-primary)]/30 focus-visible:ring-offset-0 focus-visible:border-[var(--brand-primary-border)]',
              'disabled:cursor-not-allowed disabled:opacity-50',
              icon ? 'pl-11' : 'px-3.5',
              trailingIcon ? 'pr-11' : icon ? 'pr-3.5' : '',
              inputSize === 'lg' ? 'h-12 py-3 text-base' : 'h-11 py-2.5 text-sm',
              error && 'border-destructive focus-visible:ring-destructive/30',
              warning && 'border-[var(--stock-warning)] focus-visible:ring-[var(--stock-warning)]/30',
              success && 'border-[var(--stock-success)] focus-visible:ring-[var(--stock-success)]/30',
              className,
            )}
            ref={ref}
            aria-invalid={!!error}
            aria-describedby={
              [error ? `${inputId}-error` : '', description ? `${inputId}-description` : ''].filter(Boolean).join(' ') || undefined
            }
            {...props}
          />
          {trailingIcon && (
            <button
              type="button"
              onClick={onTrailingIconClick}
              className="absolute inset-y-0 right-0 flex items-center pr-3.5 text-muted-foreground hover:text-[var(--brand-ink)] transition-colors"
              tabIndex={-1}
              aria-label={onTrailingIconClick ? 'Afficher/masquer le mot de passe' : undefined}
            >
              {trailingIcon}
            </button>
          )}
        </div>
        {description && !error && !warning && !success && (
          <p id={`${inputId}-description`} className="text-xs text-[var(--brand-ink-muted)]">{description}</p>
        )}
        {error && (
          <p id={`${inputId}-error`} className="text-sm text-destructive" role="alert">
            {error}
          </p>
        )}
        {warning && !error && (
          <p className="text-sm text-[var(--stock-warning)]" role="alert">
            {warning}
          </p>
        )}
        {success && !error && !warning && (
          <p className="text-sm text-[var(--stock-success)]" role="status">
            {success}
          </p>
        )}
      </div>
    );
  },
);

Input.displayName = 'Input';
