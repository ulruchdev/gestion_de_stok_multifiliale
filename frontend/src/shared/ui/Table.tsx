import { type HTMLAttributes, type TdHTMLAttributes, type ThHTMLAttributes, forwardRef } from 'react';
import { cn } from '../lib/utils';

interface TableProps extends HTMLAttributes<HTMLTableElement> {
  variant?: 'default' | 'compact' | 'striped';
}

const Table = forwardRef<HTMLTableElement, TableProps>(
  ({ className, variant = 'default', ...props }, ref) => (
    <div className="relative w-full overflow-auto rounded-lg border border-[var(--brand-hairline)]">
      <table
        ref={ref}
        className={cn('w-full caption-bottom text-sm group', className)}
        data-variant={variant}
        {...props}
      />
    </div>
  ),
);
Table.displayName = 'Table';

const TableHeader = forwardRef<HTMLTableSectionElement, HTMLAttributes<HTMLTableSectionElement>>(
  ({ className, ...props }, ref) => (
    <thead
      ref={ref}
      className={cn(
        '[&_tr]:border-b border-[var(--brand-hairline)] bg-[var(--brand-canvas-soft)]',
        className,
      )}
      {...props}
    />
  ),
);
TableHeader.displayName = 'TableHeader';

const TableBody = forwardRef<HTMLTableSectionElement, HTMLAttributes<HTMLTableSectionElement>>(
  ({ className, ...props }, ref) => (
    <tbody
      ref={ref}
      className={cn(
        '[&_tr:last-child]:border-0',
        'group-data-[variant=striped]:[&_tr:nth-child(even)]:bg-[var(--brand-canvas-soft)]',
        className,
      )}
      {...props}
    />
  ),
);
TableBody.displayName = 'TableBody';

const TableRow = forwardRef<HTMLTableRowElement, HTMLAttributes<HTMLTableRowElement>>(
  ({ className, ...props }, ref) => (
    <tr
      ref={ref}
      className={cn(
        'border-b border-[var(--brand-hairline)] transition-colors duration-[var(--brand-duration-fast)]',
        'hover:bg-accent/30',
        'data-[state=selected]:bg-accent/50',
        className,
      )}
      {...props}
    />
  ),
);
TableRow.displayName = 'TableRow';

const TableHead = forwardRef<HTMLTableCellElement, ThHTMLAttributes<HTMLTableCellElement>>(
  ({ className, ...props }, ref) => (
    <th
      ref={ref}
      className={cn(
        'px-4 text-left align-middle text-xs font-semibold uppercase tracking-wider text-[var(--brand-ink-muted)]',
        '[&:has([role=checkbox])]:pr-0',
        'h-12 group-data-[variant=compact]:h-9',
        className,
      )}
      {...props}
    />
  ),
);
TableHead.displayName = 'TableHead';

const TableCell = forwardRef<HTMLTableCellElement, TdHTMLAttributes<HTMLTableCellElement>>(
  ({ className, ...props }, ref) => (
    <td
      ref={ref}
      className={cn(
        'align-middle [&:has([role=checkbox])]:pr-0',
        'p-3 group-data-[variant=compact]:p-2',
        className,
      )}
      {...props}
    />
  ),
);
TableCell.displayName = 'TableCell';

const TableCaption = forwardRef<HTMLTableCaptionElement, HTMLAttributes<HTMLTableCaptionElement>>(
  ({ className, ...props }, ref) => (
    <caption ref={ref} className={cn('mt-4 text-sm text-[var(--brand-ink-muted)]', className)} {...props} />
  ),
);
TableCaption.displayName = 'TableCaption';

export { Table, TableHeader, TableBody, TableRow, TableHead, TableCell, TableCaption, type TableProps };
