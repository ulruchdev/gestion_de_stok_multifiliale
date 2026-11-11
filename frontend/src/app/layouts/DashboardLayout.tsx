import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/features/auth';
import { Logo } from '@/shared/ui/Logo';
import {
  LayoutDashboard,
  Building2,
  Package,
  Users,
  ShoppingCart,
  Receipt,
  Warehouse,
  ArrowLeftRight,
  UserCircle,
  LogOut,
  Menu,
  X,
} from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { cn } from '@/shared/lib/utils';
import { ROLES_PAR_SECTION } from '../permissions';
import type { RoleUtilisateur } from '@/shared/types';

// Définition des entrées de menu par rôle (cf. GS-IA-2026-01)
interface MenuEntry {
  label: string;
  path: string;
  icon: React.ComponentType<{ className?: string }>;
  roles: readonly RoleUtilisateur[];
  badge?: number;
}

const menuStructure: MenuEntry[] = [
  {
    label: 'Dashboard',
    path: '/dashboard',
    icon: LayoutDashboard,
    roles: ROLES_PAR_SECTION.dashboard,
  },
  {
    label: 'Groupe',
    path: '/groupe/filiales',
    icon: Building2,
    roles: ROLES_PAR_SECTION.groupe,
  },
  {
    label: 'Transferts',
    path: '/transferts',
    icon: ArrowLeftRight,
    roles: ROLES_PAR_SECTION.transferts,
  },
  {
    label: 'Catalogue',
    path: '/catalogue/articles',
    icon: Package,
    roles: ROLES_PAR_SECTION.catalogue,
  },
  {
    label: 'Tiers',
    path: '/tiers/clients',
    icon: Users,
    roles: ROLES_PAR_SECTION.tiers,
  },
  {
    label: 'Achats',
    path: '/achats/commandes',
    icon: ShoppingCart,
    roles: ROLES_PAR_SECTION.achats,
  },
  {
    label: 'Ventes',
    path: '/ventes/commandes-client',
    icon: Receipt,
    roles: ROLES_PAR_SECTION.ventes,
  },
  {
    label: 'Stock',
    path: '/stock/mouvements',
    icon: Warehouse,
    roles: ROLES_PAR_SECTION.stock,
  },
  {
    label: 'Utilisateurs',
    path: '/utilisateurs',
    icon: Users,
    roles: ROLES_PAR_SECTION.utilisateurs,
  },
];

function useIsDesktop() {
  const [isDesktop, setIsDesktop] = useState(
    () =>
      typeof window !== 'undefined' &&
      typeof window.matchMedia === 'function' &&
      window.matchMedia('(min-width: 1024px)').matches,
  );

  useEffect(() => {
    if (typeof window.matchMedia !== 'function') return;
    const mql = window.matchMedia('(min-width: 1024px)');
    const onChange = (event: MediaQueryListEvent) => setIsDesktop(event.matches);
    mql.addEventListener('change', onChange);
    return () => mql.removeEventListener('change', onChange);
  }, []);

  return isDesktop;
}

export function DashboardLayout() {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const isDesktop = useIsDesktop();
  const asideRef = useRef<HTMLElement>(null);
  const closeButtonRef = useRef<HTMLButtonElement>(null);
  const menuButtonRef = useRef<HTMLButtonElement>(null);
  const wasOpenRef = useRef(false);

  const sidebarInactive = !sidebarOpen && !isDesktop;

  // Hors écran sur mobile : rend la barre latérale inerte pour le clavier et les lecteurs d'écran.
  useEffect(() => {
    if (asideRef.current) asideRef.current.inert = sidebarInactive;
  }, [sidebarInactive]);

  // Focus : entrée dans le menu à l'ouverture, retour sur le bouton hamburger à la fermeture.
  useEffect(() => {
    if (isDesktop) return;
    if (sidebarOpen) {
      wasOpenRef.current = true;
      closeButtonRef.current?.focus();
    } else if (wasOpenRef.current) {
      wasOpenRef.current = false;
      menuButtonRef.current?.focus();
    }
  }, [sidebarOpen, isDesktop]);

  // Échap ferme le menu mobile (l'overlay n'est pas focusable).
  useEffect(() => {
    if (!sidebarOpen) return;
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setSidebarOpen(false);
    };
    document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
  }, [sidebarOpen]);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const filteredMenu = menuStructure.filter(
    (entry) => user && entry.roles.includes(user.role),
  );

  return (
    <div className="min-h-screen flex bg-[var(--brand-canvas-soft)]">
      <a
        href="#contenu-principal"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:z-50 focus:rounded-md focus:bg-background focus:px-4 focus:py-2 focus:text-sm focus:shadow-lg"
      >
        Aller au contenu
      </a>

      {/* Sidebar — Style StockMaster */}
      <aside
        ref={asideRef}
        aria-hidden={sidebarInactive ? true : undefined}
        className={cn(
          'fixed inset-y-0 left-0 z-40 w-64 bg-[var(--brand-dark)] transform transition-transform duration-[var(--brand-duration-mid)] lg:relative lg:translate-x-0',
          sidebarOpen ? 'translate-x-0' : '-translate-x-full',
        )}
      >
        <div className="flex flex-col h-full">
          {/* Logo sidebar */}
          <div className="flex items-center justify-between h-14 px-5 border-b border-white/10">
            <Logo variant="sidebar" size="md" />
            <button
              ref={closeButtonRef}
              type="button"
              className="lg:hidden rounded-full p-1.5 text-white/70 hover:bg-white/10 transition-colors"
              onClick={() => setSidebarOpen(false)}
              aria-label="Fermer le menu"
            >
              <X className="h-4 w-4" />
            </button>
          </div>

          {/* Navigation */}
          <nav id="navigation-principale" aria-label="Navigation principale" className="flex-1 overflow-y-auto p-3 space-y-0.5">
            {filteredMenu.map((entry) => (
              <NavLink
                key={entry.path}
                to={entry.path}
                end={entry.path === '/dashboard'}
                className={({ isActive }) =>
                  cn(
                    'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-[var(--brand-duration-fast)]',
                    isActive
                      ? 'bg-white/10 text-white'
                      : 'text-white/60 hover:bg-white/5 hover:text-white/80',
                  )
                }
                onClick={() => setSidebarOpen(false)}
              >
                <entry.icon className="h-[18px] w-[18px] shrink-0" />
                <span>{entry.label}</span>
              </NavLink>
            ))}
          </nav>

          {/* Profil + Déconnexion */}
          <div className="border-t border-white/10 p-3 space-y-1">
            <div className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-white/60">
              <UserCircle className="h-[18px] w-[18px] shrink-0" />
              <span className="truncate">{user?.prenom} {user?.nom}</span>
            </div>
            <button
              type="button"
              onClick={handleLogout}
              className="flex items-center gap-3 w-full px-3 py-2.5 rounded-lg text-sm text-white/60 hover:bg-white/5 hover:text-white/80 transition-all duration-[var(--brand-duration-fast)]"
            >
              <LogOut className="h-[18px] w-[18px] shrink-0" />
              <span>Déconnexion</span>
            </button>
          </div>
        </div>
      </aside>

      {/* Overlay mobile */}
      {sidebarOpen && (
        <div
          aria-hidden="true"
          className="fixed inset-0 z-30 bg-black/50 lg:hidden animate-fade-in"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Main content */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Header mobile */}
        <header className="lg:hidden border-b border-[var(--brand-hairline)] h-14 flex items-center px-4 bg-background">
          <button
            ref={menuButtonRef}
            type="button"
            className="rounded-lg p-2 hover:bg-accent transition-colors"
            onClick={() => setSidebarOpen(true)}
            aria-label="Ouvrir le menu"
            aria-expanded={sidebarOpen}
            aria-controls="navigation-principale"
          >
            <Menu aria-hidden="true" className="h-5 w-5" />
          </button>
          <Logo variant="default" size="sm" className="ml-3" showText={false} />
        </header>

        {/* Page content */}
        <main id="contenu-principal" tabIndex={-1} className="flex-1 p-6 overflow-auto animate-fade-in">
          <Outlet />
        </main>
      </div>

    </div>
  );
}
