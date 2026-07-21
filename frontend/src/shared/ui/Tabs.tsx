import { type HTMLAttributes, type ReactNode, createContext, useContext, useState, forwardRef, useCallback, useId } from 'react';
import { cn } from '../lib/utils';

type TabsVariant = 'underline' | 'pills' | 'segmented';

interface TabsContextValue {
  value: string;
  onValueChange: (value: string) => void;
  variant: TabsVariant;
  baseId: string;
}

const TabsContext = createContext<TabsContextValue | null>(null);

function useTabsContext() {
  const ctx = useContext(TabsContext);
  if (!ctx) throw new Error('Tabs components must be used within a <Tabs> parent.');
  return ctx;
}

// --- Tabs (Root) ---
interface TabsProps extends HTMLAttributes<HTMLDivElement> {
  value?: string;
  defaultValue?: string;
  onValueChange?: (value: string) => void;
  variant?: TabsVariant;
  children: ReactNode;
}

const Tabs = forwardRef<HTMLDivElement, TabsProps>(
  ({ className, value: controlledValue, defaultValue, onValueChange, variant = 'underline', children, ...props }, ref) => {
    const [internalValue, setInternalValue] = useState(defaultValue || '');
    const baseId = useId();

    const isControlled = controlledValue !== undefined;
    const activeValue = isControlled ? controlledValue : internalValue;

    const handleValueChange = useCallback((newValue: string) => {
      if (!isControlled) setInternalValue(newValue);
      onValueChange?.(newValue);
    }, [isControlled, onValueChange]);

    return (
      <TabsContext.Provider value={{ value: activeValue, onValueChange: handleValueChange, variant, baseId }}>
        <div
          ref={ref}
          className={cn('w-full', className)}
          data-variant={variant}
          {...props}
        >
          {children}
        </div>
      </TabsContext.Provider>
    );
  },
);
Tabs.displayName = 'Tabs';

// --- TabsList ---
interface TabsListProps extends HTMLAttributes<HTMLDivElement> {}

const TabsList = forwardRef<HTMLDivElement, TabsListProps>(
  ({ className, ...props }, ref) => {
    const { variant } = useTabsContext();

    return (
      <div
        ref={ref}
        className={cn(
          'inline-flex items-center group',
          {
            // underline variant
            'gap-0 border-b border-[var(--brand-hairline)] w-full': variant === 'underline',
            // pills variant
            'gap-1 p-1 rounded-lg bg-[var(--brand-canvas-soft)]': variant === 'pills',
            // segmented variant
            'gap-0 p-0.5 rounded-lg bg-[var(--brand-canvas-soft)]': variant === 'segmented',
          },
          className,
        )}
        role="tablist"
        {...props}
      />
    );
  },
);
TabsList.displayName = 'TabsList';

// --- TabsTrigger ---
interface TabsTriggerProps extends HTMLAttributes<HTMLButtonElement> {
  value: string;
  disabled?: boolean;
}

const TabsTrigger = forwardRef<HTMLButtonElement, TabsTriggerProps>(
  ({ className, value, disabled, children, ...props }, ref) => {
    const { value: activeValue, onValueChange, variant, baseId } = useTabsContext();
    const isActive = activeValue === value;

    return (
      <button
        ref={ref}
        role="tab"
        type="button"
        disabled={disabled}
        data-state={isActive ? 'active' : 'inactive'}
        data-value={value}
        id={`${baseId}-tab-${value}`}
        aria-selected={isActive}
        aria-controls={`${baseId}-panel-${value}`}
        tabIndex={isActive ? 0 : -1}
        onClick={() => onValueChange(value)}
        className={cn(
          'inline-flex items-center justify-center whitespace-nowrap text-sm font-medium transition-all duration-[var(--brand-duration-fast)]',
          'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
          'disabled:pointer-events-none disabled:opacity-50',
          {
            // underline variant
            'px-4 py-2 border-b-2 border-transparent text-[var(--brand-ink-muted)] hover:text-[var(--brand-ink)]':
              variant === 'underline',
            'data-[state=active]:border-[var(--brand-primary)] data-[state=active]:text-[var(--brand-primary)]':
              variant === 'underline',

            // pills variant
            'px-4 py-2 rounded-md text-[var(--brand-ink-muted)] hover:text-[var(--brand-ink)]':
              variant === 'pills',
            'data-[state=active]:bg-background data-[state=active]:shadow-sm data-[state=active]:text-[var(--brand-ink)]':
              variant === 'pills',

            // segmented variant
            'px-3 py-1.5 rounded-md text-[var(--brand-ink-muted)] hover:text-[var(--brand-ink)]':
              variant === 'segmented',
            'data-[state=active]:bg-background data-[state=active]:shadow-sm data-[state=active]:text-[var(--brand-ink)]':
              variant === 'segmented',
          },
          className,
        )}
        {...props}
      >
        {children}
      </button>
    );
  },
);
TabsTrigger.displayName = 'TabsTrigger';

// --- TabsContent ---
interface TabsContentProps extends HTMLAttributes<HTMLDivElement> {
  value: string;
}

const TabsContent = forwardRef<HTMLDivElement, TabsContentProps>(
  ({ className, value, children, ...props }, ref) => {
    const { value: activeValue, baseId } = useTabsContext();
    const isActive = activeValue === value;

    if (!isActive) return null;

    return (
      <div
        ref={ref}
        role="tabpanel"
        id={`${baseId}-panel-${value}`}
        aria-labelledby={`${baseId}-tab-${value}`}
        className={cn(
          'mt-4 ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
          className,
        )}
        {...props}
      >
        {children}
      </div>
    );
  },
);
TabsContent.displayName = 'TabsContent';

export { Tabs, TabsList, TabsTrigger, TabsContent };
export type { TabsProps, TabsListProps, TabsTriggerProps, TabsContentProps, TabsVariant };
