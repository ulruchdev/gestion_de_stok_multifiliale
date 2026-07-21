import { create } from 'zustand';
import { CheckCircle, AlertCircle, AlertTriangle, Info, X } from 'lucide-react';
import { cn } from '../lib/utils';
import { useEffect, useState, useCallback } from 'react';

type ToastType = 'success' | 'error' | 'warning' | 'info';
type ToastPosition = 'bottom-right' | 'top-right' | 'top-center' | 'bottom-center';

interface Toast {
  id: string;
  type: ToastType;
  title: string;
  message?: string;
  duration?: number;
}

interface ToastStore {
  toasts: Toast[];
  addToast: (toast: Omit<Toast, 'id'>) => void;
  removeToast: (id: string) => void;
}

export const useToastStore = create<ToastStore>((set) => ({
  toasts: [],
  addToast: (toast) => {
    const id = Math.random().toString(36).substring(7);
    set((state) => ({
      toasts: [...state.toasts, { ...toast, id }],
    }));
    const duration = toast.duration || 5000;
    setTimeout(() => {
      set((state) => ({
        toasts: state.toasts.filter((t) => t.id !== id),
      }));
    }, duration);
  },
  removeToast: (id) => {
    set((state) => ({
      toasts: state.toasts.filter((t) => t.id !== id),
    }));
  },
}));

export const toast = {
  success: (title: string, message?: string) =>
    useToastStore.getState().addToast({ type: 'success', title, message }),
  error: (title: string, message?: string) =>
    useToastStore.getState().addToast({ type: 'error', title, message }),
  warning: (title: string, message?: string) =>
    useToastStore.getState().addToast({ type: 'warning', title, message }),
  info: (title: string, message?: string) =>
    useToastStore.getState().addToast({ type: 'info', title, message }),
};

const icons = {
  success: CheckCircle,
  error: AlertCircle,
  warning: AlertTriangle,
  info: Info,
};

const styles: Record<ToastType, string> = {
  success: 'border-l-[var(--stock-success)] bg-[var(--stock-success-bg)] text-[var(--stock-success)]',
  error: 'border-l-[var(--stock-danger)] bg-[var(--stock-danger-bg)] text-[var(--stock-danger)]',
  warning: 'border-l-[var(--stock-warning)] bg-[var(--stock-warning-bg)] text-[var(--stock-warning)]',
  info: 'border-l-[var(--brand-primary)] bg-[var(--brand-primary-bg)] text-[var(--brand-primary)]',
};

function ToastItem({ toast: t, onRemove }: { toast: Toast; onRemove: (id: string) => void }) {
  const Icon = icons[t.type];
  const [isVisible, setIsVisible] = useState(false);
  const [progress, setProgress] = useState(100);
  const duration = t.duration || 5000;

  useEffect(() => {
    const enterTimer = setTimeout(() => setIsVisible(true), 10);
    const interval = setInterval(() => {
      setProgress((prev) => Math.max(0, prev - (100 / (duration / 100))));
    }, 100);
    return () => {
      clearTimeout(enterTimer);
      clearInterval(interval);
    };
  }, [duration]);

  return (
    <div
      className={cn(
        'flex items-start gap-3 rounded-lg border-l-4 p-4 shadow-lg',
        'transition-all duration-[var(--brand-duration-slow)] ease-out',
        'relative overflow-hidden',
        styles[t.type],
        isVisible ? 'translate-x-0 opacity-100' : 'translate-x-full opacity-0',
      )}
      role="alert"
    >
      {/* Progress bar */}
      <div
        className="absolute bottom-0 left-0 h-1 bg-current opacity-20 transition-all duration-100 linear"
        style={{ width: `${progress}%` }}
      />

      <Icon className="h-5 w-5 shrink-0 mt-0.5" />
      <div className="flex-1 min-w-0">
        <p className="font-medium text-sm">{t.title}</p>
        {t.message && <p className="text-sm opacity-80 mt-0.5">{t.message}</p>}
      </div>
      <button
        onClick={() => onRemove(t.id)}
        className="shrink-0 rounded-full p-1 hover:bg-black/10 transition-colors"
      >
        <X className="h-4 w-4" />
      </button>
    </div>
  );
}

interface ToastContainerProps {
  position?: ToastPosition;
}

export function ToastContainer({ position = 'bottom-right' }: ToastContainerProps) {
  const toasts = useToastStore((s) => s.toasts);
  const removeToast = useToastStore((s) => s.removeToast);

  if (toasts.length === 0) return null;

  const positionClasses: Record<ToastPosition, string> = {
    'bottom-right': 'bottom-4 right-4',
    'top-right': 'top-4 right-4',
    'top-center': 'top-4 left-1/2 -translate-x-1/2',
    'bottom-center': 'bottom-4 left-1/2 -translate-x-1/2',
  };

  return (
    <div
      className={cn(
        'fixed z-[100] flex flex-col gap-2 w-full max-w-sm pointer-events-none',
        positionClasses[position],
      )}
    >
      {toasts.map((t) => (
        <div key={t.id} className="pointer-events-auto">
          <ToastItem toast={t} onRemove={removeToast} />
        </div>
      ))}
    </div>
  );
}
