import { Outlet } from 'react-router-dom';
import { Logo } from '@/shared/ui/Logo';

export function AuthLayout() {
  return (
    <div className="min-h-screen flex flex-col bg-[var(--brand-canvas-soft)]">
      {/* Header avec logo */}
      <header className="border-b border-[var(--brand-hairline)] bg-background">
        <div className="container flex h-14 items-center">
          <Logo variant="auth" size="md" />
        </div>
      </header>

      {/* Contenu centré avec animation fade-in-up */}
      <main className="flex-1 flex items-center justify-center p-4 animate-fade-in-up">
        <div className="w-full max-w-sm">
          <Outlet />
        </div>
      </main>

      {/* Footer */}
      <footer className="py-4">
        <div className="container text-center text-xs text-[var(--brand-ink-muted)]">
          &copy; {new Date().getFullYear()} StockMaster CM — Solution de gestion de stock multi-filiales
        </div>
      </footer>
    </div>
  );
}
