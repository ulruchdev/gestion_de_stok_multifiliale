import { type ReactNode, useEffect, useRef } from 'react';
import { cn } from '../lib/utils';
import { X } from 'lucide-react';
import { Button } from './Button';

interface ModalProps {
  open: boolean;
  onClose: () => void;
  title: string;
  description?: string;
  children?: ReactNode;
  footer?: ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  blur?: boolean;
}

export function Modal({ open, onClose, title, description, children, footer, size = 'md', blur = true }: ModalProps) {
  const overlayRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    if (open) {
      document.addEventListener('keydown', handleEscape);
      document.body.style.overflow = 'hidden';
    }
    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = 'unset';
    };
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div
      ref={overlayRef}
      className={cn(
        'fixed inset-0 z-50 flex items-center justify-center bg-black/30',
        'animate-fade-in',
        blur && 'backdrop-blur-sm',
      )}
      onClick={(e) => e.target === overlayRef.current && onClose()}
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-title"
    >
      <div
        className={cn(
          'relative bg-background rounded-lg shadow-xl max-h-[85vh] overflow-y-auto animate-scale-in',
          {
            'w-full max-w-sm': size === 'sm',
            'w-full max-w-lg': size === 'md',
            'w-full max-w-2xl': size === 'lg',
            'w-full max-w-4xl': size === 'xl',
          },
        )}
      >
        {/* Header */}
        <div className="flex items-center justify-between border-b border-[var(--brand-hairline)] p-5">
          <div className="pr-4">
            <h2 id="modal-title" className="text-lg font-semibold font-display">
              {title}
            </h2>
            {description && <p className="text-sm text-muted-foreground mt-1">{description}</p>}
          </div>
          <button
            onClick={onClose}
            className="rounded-full p-1.5 hover:bg-accent transition-colors shrink-0"
            aria-label="Fermer"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        {/* Content */}
        {children && <div className="p-5">{children}</div>}

        {/* Footer */}
        {footer && (
          <div className="flex items-center justify-end gap-3 border-t border-[var(--brand-hairline)] p-5">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
}

// Composant de confirmation rapide
interface ConfirmModalProps {
  open: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: 'default' | 'destructive';
  loading?: boolean;
}

export function ConfirmModal({
  open,
  onClose,
  onConfirm,
  title,
  message,
  confirmLabel = 'Confirmer',
  cancelLabel = 'Annuler',
  variant = 'default',
  loading = false,
}: ConfirmModalProps) {
  return (
    <Modal
      open={open}
      onClose={onClose}
      title={title}
      size="sm"
      footer={
        <>
          <Button variant="outline" onClick={onClose} disabled={loading}>
            {cancelLabel}
          </Button>
          <Button
            variant={variant === 'destructive' ? 'destructive' : 'default'}
            onClick={onConfirm}
            loading={loading}
          >
            {confirmLabel}
          </Button>
        </>
      }
    >
      <p className="text-sm text-muted-foreground">{message}</p>
    </Modal>
  );
}
