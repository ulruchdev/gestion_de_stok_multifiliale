import { type ReactNode } from 'react';
import ReactPhoneInput, { type Value, type Country } from 'react-phone-number-input';
import 'react-phone-number-input/style.css';
import { cn } from '../lib/utils';

export interface PhoneInputFieldProps {
  label?: string;
  error?: string;
  description?: string;
  icon?: ReactNode;
  disabled?: boolean;
  value?: Value;
  onChange?: (value?: Value) => void;
  defaultCountry?: Country;
  placeholder?: string;
  className?: string;
  id?: string;
}

export function PhoneInputField({
  label,
  error,
  description,
  icon,
  disabled,
  value,
  onChange,
  defaultCountry = 'CM',
  placeholder = '691234567',
  className,
  id,
}: PhoneInputFieldProps) {
  const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');

  return (
    <div className="space-y-1.5">
      {label && (
        <label
          htmlFor={inputId}
          className="text-sm font-medium leading-none text-[var(--brand-ink-muted)]"
        >
          {label}
        </label>
      )}
      <div className="relative">
        {icon && (
          <div className="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-muted-foreground z-10">
            {icon}
          </div>
        )}
        <ReactPhoneInput
          id={inputId}
          value={value}
          onChange={onChange || (() => {})}
          defaultCountry={defaultCountry}
          placeholder={placeholder}
          disabled={disabled}
          international
          countrySelectProps={{ 'aria-label': 'Sélectionner un pays' }}
          className={cn(
            'phone-input-wrapper',
            icon && 'has-icon',
            error && 'has-error',
            className,
          )}
          numberInputProps={{
            className: cn(
              'flex w-full rounded-lg border border-input bg-background text-sm ring-offset-background transition-all duration-[var(--brand-duration-fast)]',
              'placeholder:text-muted-foreground/50 placeholder:font-normal',
              'focus-visible:outline-none',
              'disabled:cursor-not-allowed disabled:opacity-50',
              icon ? 'pl-11' : 'px-3.5',
              'h-11 py-2.5',
              error && 'border-destructive',
            ),
          }}
        />
      </div>
      {description && !error && (
        <p className="text-xs text-[var(--brand-ink-muted)]">{description}</p>
      )}
      {error && (
        <p className="text-sm text-destructive" role="alert">
          {error}
        </p>
      )}
    </div>
  );
}
