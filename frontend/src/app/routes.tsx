import { Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/features/auth';
import { RoleUtilisateur } from '@/shared/types';
import { DashboardLayout } from './layouts/DashboardLayout';
import { AuthLayout } from './layouts/AuthLayout';

// Lazy loading des pages
// Ces composants seront créés au fur et à mesure du développement
const PlaceholderPage = ({ title }: { title: string }) => (
  <div className="flex items-center justify-center h-64">
    <p className="text-muted-foreground text-lg">{title} — à implémenter</p>
  </div>
);

// Guards de route
function RequireAuth({ roles }: { roles?: RoleUtilisateur[] }) {
  const { isAuthenticated, user, isLoading } = useAuthStore();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    );
  }

  if (!isAuthenticated) return <Navigate to="/login" replace />;

  if (roles && user && !roles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
}

function RedirectIfAuthenticated() {
  const { isAuthenticated, isLoading } = useAuthStore();
  if (isLoading) return null;
  if (isAuthenticated) return <Navigate to="/dashboard" replace />;
  return <Outlet />;
}

export function AppRoutes() {
  return (
    <Routes>
      {/* Routes publiques (non authentifié) — cf. GS-IA-2026-01 §7 */}
      <Route element={<RedirectIfAuthenticated />}>
        <Route element={<AuthLayout />}>
          <Route path="/" element={<PlaceholderPage title="Accueil" />} />
          <Route path="/login" element={<PlaceholderPage title="Connexion" />} />
          <Route path="/inscription" element={<PlaceholderPage title="Choix type inscription" />} />
          <Route path="/inscription/entreprise-unique" element={<PlaceholderPage title="Inscription entreprise unique" />} />
          <Route path="/inscription/groupe" element={<PlaceholderPage title="Inscription groupe" />} />
          <Route path="/mot-de-passe-oublie" element={<PlaceholderPage title="Mot de passe oublié" />} />
          <Route path="/reset-password" element={<PlaceholderPage title="Réinitialisation mot de passe" />} />
        </Route>
      </Route>

      {/* Routes protégées (authentifié) */}
      <Route element={<RequireAuth />}>
        <Route element={<DashboardLayout />}>
          {/* Dashboard — GS-IA-2026-01 §1-2 */}
          <Route path="/dashboard" element={<PlaceholderPage title="Dashboard" />} />

          {/* Groupe & Filiales — GS-IA-2026-01 §1 (Admin Groupe uniquement) */}
          <Route element={<RequireAuth roles={[RoleUtilisateur.ADMIN_GROUPE]} />}>
            <Route path="/groupe/filiales" element={<PlaceholderPage title="Mes filiales" />} />
            <Route path="/groupe/parametres" element={<PlaceholderPage title="Paramètres groupe" />} />
            <Route path="/transferts" element={<PlaceholderPage title="Transferts de stock" />} />
            <Route path="/transferts/nouveau" element={<PlaceholderPage title="Nouveau transfert" />} />
            <Route path="/utilisateurs" element={<PlaceholderPage title="Gestion des utilisateurs" />} />
          </Route>

          {/* Catalogue — GS-IA-2026-01 §2-3 */}
          <Route path="/catalogue/articles" element={<PlaceholderPage title="Articles" />} />
          <Route path="/catalogue/articles/nouveau" element={<PlaceholderPage title="Nouvel article" />} />
          <Route path="/catalogue/articles/:id" element={<PlaceholderPage title="Détail article" />} />
          <Route path="/catalogue/categories" element={<PlaceholderPage title="Catégories" />} />

          {/* Tiers — GS-IA-2026-01 §2, 4 */}
          <Route path="/tiers/clients" element={<PlaceholderPage title="Clients" />} />
          <Route path="/tiers/clients/:id" element={<PlaceholderPage title="Détail client" />} />
          <Route path="/tiers/fournisseurs" element={<PlaceholderPage title="Fournisseurs" />} />

          {/* Achats / Commandes fournisseur — GS-IA-2026-01 §2, 4 */}
          <Route path="/achats/commandes" element={<PlaceholderPage title="Commandes fournisseur" />} />
          <Route path="/achats/commandes/nouveau" element={<PlaceholderPage title="Nouvelle commande" />} />
          <Route path="/achats/commandes/:id" element={<PlaceholderPage title="Détail commande" />} />

          {/* Ventes — GS-IA-2026-01 §2, 5-6 */}
          <Route path="/ventes/commandes-client" element={<PlaceholderPage title="Commandes client" />} />
          <Route path="/ventes/commandes-client/nouveau" element={<PlaceholderPage title="Nouvelle commande client" />} />
          <Route path="/ventes/directes" element={<PlaceholderPage title="Ventes directes / Caisse" />} />
          <Route path="/ventes/directes/nouveau" element={<PlaceholderPage title="Nouvelle vente directe" />} />

          {/* Stock — GS-IA-2026-01 §2-3 */}
          <Route path="/stock/mouvements" element={<PlaceholderPage title="Mouvements de stock" />} />
          <Route path="/stock/corrections" element={<PlaceholderPage title="Corrections manuelles" />} />

          {/* Profil */}
          <Route path="/profil" element={<PlaceholderPage title="Mon profil" />} />
        </Route>
      </Route>

      {/* 404 */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
