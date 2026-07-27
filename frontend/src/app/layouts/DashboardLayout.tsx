import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/features/auth';
import { RoleUtilisateur } from '@/shared/types';
import { Logo } from '@/shared/ui/Logo';
import { ToastContainer } from '@/shared/ui/Toast';
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
import { useState } from 'react';
import { cn } from '@/shared/lib/utils';

// Définition des entrées de menu par rôle (cf. GS-IA-2026-01)
interface MenuEntry {
  label: string;
  path: string;
  icon: React.ComponentType<{ className?: string }>;
  roles: RoleUtilisateur[];
  badge?: number;
}

const menuStructure: MenuEntry[] = [
  {
    label: 'Dashboard',
    path: '/dashboard',
    icon: LayoutDashboard,
    roles: Object.values(RoleUtilisateur),
  },
  {
    label: 'Groupe',
    path: '/groupe/filiales',
    icon: Building2,
    roles: [RoleUtilisateur.ADMIN_GROUPE],
  },
  {
    label: 'Transferts',
    path: '/transferts',
    icon: ArrowLeftRight,
    roles: [RoleUtilisateur.ADMIN_GROUPE],
  },
  {
    label: 'Catalogue',
    path: '/catalogue/articles',
    icon: Package,
    roles: [RoleUtilisateur.ADMIN_FILIALE, RoleUtilisateur.GESTIONNAIRE_STOCK],
  },
  {
    label: 'Tiers',
    path: '/tiers/clients',
    icon: Users,
    roles: [RoleUtilisateur.ADMIN_FILIALE, RoleUtilisateur.COMMERCIAL, RoleUtilisateur.RESPONSABLE_ACHATS],
  },
  {
    label: 'Achats',
    path: '/achats/commandes',
    icon: ShoppingCart,
    roles: [RoleUtilisateur.ADMIN_FILIALE, RoleUtilisateur.RESPONSABLE_ACHATS],
  },
  {
    label: 'Ventes',
    path: '/ventes/commandes-client',
    icon: Receipt,
    roles: [RoleUtilisateur.ADMIN_FILIALE, RoleUtilisateur.COMMERCIAL, RoleUtilisateur.CAISSIER],
  },
  {
    label: 'Stock',
    path: '/stock/mouvements',
    icon: Warehouse,
    roles: [RoleUtilisateur.ADMIN_FILIALE, RoleUtilisateur.GESTIONNAIRE_STOCK],
  },
  {
    label: 'Utilisateurs',
    path: '/utilisateurs',
    icon: Users,
    roles: [RoleUtilisateur.ADMIN_GROUPE, RoleUtilisateur.ADMIN_FILIALE],
  },
];

export function DashboardLayout() {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const filteredMenu = menuStructure.filter(
    (entry) => user && entry.roles.includes(user.role),
  );

  return (
    <div className="min-h-screen flex bg-[var(--brand-canvas-soft)]">
      {/* Sidebar — Style StockMaster */}
      <aside
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
              className="lg:hidden rounded-full p-1.5 text-white/70 hover:bg-white/10 transition-colors"
              onClick={() => setSidebarOpen(false)}
            >
              <X className="h-4 w-4" />
            </button>
          </div>

          {/* Navigation */}
          <nav className="flex-1 overflow-y-auto p-3 space-y-0.5">
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
          className="fixed inset-0 z-30 bg-black/50 lg:hidden animate-fade-in"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Main content */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Header mobile */}
        <header className="lg:hidden border-b border-[var(--brand-hairline)] h-14 flex items-center px-4 bg-background">
          <button
            className="rounded-lg p-2 hover:bg-accent transition-colors"
            onClick={() => setSidebarOpen(true)}
          >
            <Menu className="h-5 w-5" />
          </button>
          <Logo variant="default" size="sm" className="ml-3" showText={false} />
        </header>

        {/* Page content */}
        <main className="flex-1 p-6 overflow-auto animate-fade-in">
          <Outlet />
        </main>
      </div>

      {/* Toast notifications */}
      <ToastContainer />
    </div>
  );
}
