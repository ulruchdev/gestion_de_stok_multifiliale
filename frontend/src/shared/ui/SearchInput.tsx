import { type InputHTMLAttributes, forwardRef, useEffect, useState, useCallback } from 'react';
import { cn } from '../lib/utils';
import { Search, X } from 'lucide-react';

interface SearchInputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'onChange' | 'value'> {
  value?: string;
  onChange?: (value: string) => void;
  debounceMs?: number;
  variant?: 'default' | 'pill';
}

const SearchInput = forwardRef<HTMLInputElement, SearchInputProps>(
  ({ className, value: controlledValue, onChange, debounceMs = 300, variant = 'default', placeholder = 'Rechercher...', ...props }, ref) => {
    const [internalValue, setInternalValue] = useState(controlledValue || '');

    useEffect(() => {
      setInternalValue(controlledValue ?? '');
    }, [controlledValue]);

    // Debounce effect
    useEffect(() => {
      if (!onChange) return;
      const timer = setTimeout(() => {
        onChange(internalValue);
      }, debounceMs);
      return () => clearTimeout(timer);
    }, [internalValue, debounceMs, onChange]);

    const handleClear = useCallback(() => {
      setInternalValue('');
      onChange?.('');
    }, [onChange]);

    return (
      <div className="relative">
        <Search className={cn(
          'absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[var(--brand-ink-muted)] pointer-events-none',
        )} />
        <input
          ref={ref}
          type="text"
          value={internalValue}
          onChange={(e) => setInternalValue(e.target.value)}
          placeholder={placeholder}
          className={cn(
            'flex w-full border border-input bg-background text-sm ring-offset-background transition-all duration-[var(--brand-duration-fast)]',
            'placeholder:text-muted-foreground/60',
            'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
            'disabled:cursor-not-allowed disabled:opacity-50',
            'pl-9 pr-8',
            variant === 'pill' ? 'rounded-full h-10 px-4 pl-10' : 'rounded-lg h-10',
            className,
          )}
          {...props}
        />
        {internalValue && (
          <button
            type="button"
            onClick={handleClear}
            className={cn(
              'absolute right-2.5 top-1/2 -translate-y-1/2 rounded-full p-0.5 hover:bg-accent transition-colors',
            )}
            aria-label="Effacer la recherche"
          >
            <X className="h-4 w-4 text-[var(--brand-ink-muted)]" />
          </button>
        )}
      </div>
    );
  },
);
SearchInput.displayName = 'SearchInput';

export { SearchInput };
export type { SearchInputProps };
