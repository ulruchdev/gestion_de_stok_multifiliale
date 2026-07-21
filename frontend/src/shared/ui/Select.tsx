import { type SelectHTMLAttributes, forwardRef } from 'react';
import { cn } from '../lib/utils';
import { ChevronDown } from 'lucide-react';

interface SelectOption {
  value: string;
  label: string;
  disabled?: boolean;
}

interface SelectProps extends Omit<SelectHTMLAttributes<HTMLSelectElement>, 'children'> {
  error?: string;
  label?: string;
  options: SelectOption[];
  placeholder?: string;
  selectSize?: 'default' | 'lg';
}

const Select = forwardRef<HTMLSelectElement, SelectProps>(
  ({ className, error, label, options, placeholder = 'Sélectionner...', selectSize, id, ...props }, ref) => {
    const selectId = id || label?.toLowerCase().replace(/\s+/g, '-');

    return (
      <div className="space-y-1.5">
        {label && (
          <label
            htmlFor={selectId}
            className="text-sm font-medium leading-none text-[var(--brand-ink-muted)]"
          >
            {label}
          </label>
        )}
        <div className="relative">
          <select
            id={selectId}
            ref={ref}
            className={cn(
              'flex w-full rounded-lg border border-input bg-background text-sm ring-offset-background transition-all duration-[var(--brand-duration-fast)]',
              'appearance-none',
              'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
              'disabled:cursor-not-allowed disabled:opacity-50',
              selectSize === 'lg' ? 'h-12 px-4 py-3 text-base' : 'h-10 px-3 py-2',
              error && 'border-destructive focus-visible:ring-destructive',
              className,
            )}
            aria-invalid={!!error}
            aria-describedby={error ? `${selectId}-error` : undefined}
            {...props}
          >
            <option value="" disabled>
              {placeholder}
            </option>
            {options.map((option) => (
              <option
                key={option.value}
                value={option.value}
                disabled={option.disabled}
              >
                {option.label}
              </option>
            ))}
          </select>
          <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3">
            <ChevronDown className="h-4 w-4 text-[var(--brand-ink-muted)]" />
          </div>
        </div>
        {error && (
          <p id={`${selectId}-error`} className="text-sm text-destructive" role="alert">
            {error}
          </p>
        )}
      </div>
    );
  },
);
Select.displayName = 'Select';

export { Select };
export type { SelectProps, SelectOption };
