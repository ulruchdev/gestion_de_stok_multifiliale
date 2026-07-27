import { Link } from 'react-router-dom';
import { Warehouse } from 'lucide-react';
import { cn } from '../lib/utils';

export interface LogoProps {
  variant?: 'default' | 'landing' | 'sidebar' | 'auth';
  showText?: boolean;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const sizeMap = {
  sm: { icon: 'h-5 w-5', box: 'w-7 h-7', text: 'text-sm', spacing: 'gap-1.5' },
  md: { icon: 'h-5 w-5', box: 'w-8 h-8', text: 'text-base', spacing: 'gap-2' },
  lg: { icon: 'h-6 w-6', box: 'w-10 h-10', text: 'text-lg', spacing: 'gap-2.5' },
};

const variantStyles = {
  default: {
    container: '',
    box: 'bg-[var(--brand-primary)] rounded-lg',
    icon: 'text-white',
    text: 'text-[var(--brand-ink)]',
    dot: 'text-[var(--brand-primary)]',
  },
  landing: {
    container: '',
    box: 'bg-[var(--brand-primary)] rounded-lg',
    icon: 'text-white',
    text: 'text-[var(--brand-ink)]',
    dot: 'text-[var(--brand-primary)]',
  },
  sidebar: {
    container: '',
    box: 'bg-white/10 rounded-lg',
    icon: 'text-white',
    text: 'text-white',
    dot: 'text-white/70',
  },
  auth: {
    container: 'mx-auto w-fit',
    box: 'bg-[var(--brand-primary)] rounded-xl shadow-sm',
    icon: 'text-white',
    text: 'text-[var(--brand-ink)]',
    dot: 'text-[var(--brand-primary)]',
  },
};

export function Logo({ variant = 'default', showText = true, size = 'md', className }: LogoProps) {
  const s = sizeMap[size];
  const v = variantStyles[variant];

  const content = (
    <div className={cn('inline-flex items-center', s.spacing, v.container, className)}>
      {/* Logomark */}
      <div className={cn(s.box, 'flex items-center justify-center shrink-0', v.box)}>
        <Warehouse className={cn(s.icon, v.icon)} />
      </div>

      {/* Brand name */}
      {showText && (
        <span className={cn('font-semibold font-display tracking-tight', s.text, v.text)}>
          StockMaster<span className={v.dot}>.</span>
        </span>
      )}
    </div>
  );

  // Landing/dashboard: clickable
  if (variant === 'landing') {
    return <Link to="/">{content}</Link>;
  }

  if (variant === 'sidebar') {
    return <Link to="/dashboard">{content}</Link>;
  }

  // Auth: clickable to home
  if (variant === 'auth') {
    return <Link to="/">{content}</Link>;
  }

  return content;
}
