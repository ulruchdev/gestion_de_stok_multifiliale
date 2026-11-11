import { lazy, Suspense } from 'react';
import { Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/features/auth';
import { DashboardLayout } from './layouts/DashboardLayout';
import { AuthLayout } from './layouts/AuthLayout';
import { RequireAuth } from './guards/RequireAuth';
import { ROLES_PAR_SECTION } from './permissions';

// Lazy loading des pages
const AccueilPage = lazy(() => import('./pages/AccueilPage').then(m => ({ default: m.AccueilPage })));
const LoginPage = lazy(() => import('./pages/LoginPage').then(m => ({ default: m.LoginPage })));
const InscriptionChoixPage = lazy(() => import('./pages/InscriptionChoixPage').then(m => ({ default: m.InscriptionChoixPage })));
const InscriptionEntrepriseUniquePage = lazy(() => import('./pages/InscriptionEntrepriseUniquePage').then(m => ({ default: m.InscriptionEntrepriseUniquePage })));
const InscriptionGroupePage = lazy(() => import('./pages/InscriptionGroupePage').then(m => ({ default: m.InscriptionGroupePage })));
const ForgotPasswordPage = lazy(() => import('./pages/ForgotPasswordPage').then(m => ({ default: m.ForgotPasswordPage })));
const ResetPasswordPage = lazy(() => import('./pages/ResetPasswordPage').then(m => ({ default: m.ResetPasswordPage })));

// Placeholder pour les pages non encore implémentées
const PlaceholderPage = ({ title }: { title: string }) => (
  <div className="flex items-center justify-center h-64">
    <p className="text-muted-foreground text-lg">{title} — à implémenter</p>
  </div>
);

// Suspense fallback pour le lazy loading
function PageLoader() {
  return (
    <div className="flex items-center justify-center min-h-[300px]">
      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
    </div>
  );
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
      {/* Page d'accueil publique (pleine largeur, hors AuthLayout) */}
      <Route element={<RedirectIfAuthenticated />}>
        <Route path="/" element={<Suspense fallback={<PageLoader />}><AccueilPage /></Suspense>} />

        {/* Pages d'authentification simples (layout centré max-w-sm) */}
        <Route element={<AuthLayout />}>
          <Route path="/login" element={<Suspense fallback={<PageLoader />}><LoginPage /></Suspense>} />
          <Route path="/mot-de-passe-oublie" element={<Suspense fallback={<PageLoader />}><ForgotPasswordPage /></Suspense>} />
          <Route path="/reset-password" element={<Suspense fallback={<PageLoader />}><ResetPasswordPage /></Suspense>} />
        </Route>

        {/* Pages d'inscription (pleine largeur, hors AuthLayout) */}
        <Route path="/inscription" element={<Suspense fallback={<PageLoader />}><InscriptionChoixPage /></Suspense>} />
        <Route path="/inscription/entreprise-unique" element={<Suspense fallback={<PageLoader />}><InscriptionEntrepriseUniquePage /></Suspense>} />
        <Route path="/inscription/groupe" element={<Suspense fallback={<PageLoader />}><InscriptionGroupePage /></Suspense>} />
      </Route>

      {/* Routes protégées (authentifié) */}
      <Route element={<RequireAuth />}>
        <Route element={<DashboardLayout />}>
          {/* Dashboard — GS-IA-2026-01 §1-2 */}
          <Route path="/dashboard" element={<PlaceholderPage title="Dashboard" />} />

          {/* Groupe & Filiales — GS-IA-2026-01 §1 (Admin Groupe uniquement) */}
          <Route element={<RequireAuth roles={ROLES_PAR_SECTION.groupe} />}>
            <Route path="/groupe/filiales" element={<PlaceholderPage title="Mes filiales" />} />
            <Route path="/groupe/parametres" element={<PlaceholderPage title="Paramètres groupe" />} />
          </Route>

          {/* Transferts inter-filiales — Admin Groupe uniquement */}
          <Route element={<RequireAuth roles={ROLES_PAR_SECTION.transferts} />}>
            <Route path="/transferts" element={<PlaceholderPage title="Transferts de stock" />} />
            <Route path="/transferts/nouveau" element={<PlaceholderPage title="Nouveau transfert" />} />
          </Route>

          {/* Catalogue — GS-IA-2026-01 §2-3 */}
          <Route element={<RequireAuth roles={ROLES_PAR_SECTION.catalogue} />}>
            <Route path="/catalogue/articles" element={<PlaceholderPage title="Articles" />} />
            <Route path="/catalogue/articles/nouveau" element={<PlaceholderPage title="Nouvel article" />} />
            <Route path="/catalogue/articles/:id" element={<PlaceholderPage title="Détail article" />} />
            <Route path="/catalogue/categories" element={<PlaceholderPage title="Catégories" />} />
          </Route>

          {/* Tiers — GS-IA-2026-01 §2, 4 */}
          <Route element={<RequireAuth roles={ROLES_PAR_SECTION.tiers} />}>
            <Route path="/tiers/clients" element={<PlaceholderPage title="Clients" />} />
            <Route path="/tiers/clients/:id" element={<PlaceholderPage title="Détail client" />} />
            <Route path="/tiers/fournisseurs" element={<PlaceholderPage title="Fournisseurs" />} />
          </Route>

          {/* Achats / Commandes fournisseur — GS-IA-2026-01 §2, 4 */}
          <Route element={<RequireAuth roles={ROLES_PAR_SECTION.achats} />}>
            <Route path="/achats/commandes" element={<PlaceholderPage title="Commandes fournisseur" />} />
            <Route path="/achats/commandes/nouveau" element={<PlaceholderPage title="Nouvelle commande" />} />
            <Route path="/achats/commandes/:id" element={<PlaceholderPage title="Détail commande" />} />
          </Route>

          {/* Ventes — GS-IA-2026-01 §2, 5-6 */}
          <Route element={<RequireAuth roles={ROLES_PAR_SECTION.ventes} />}>
            <Route path="/ventes/commandes-client" element={<PlaceholderPage title="Commandes client" />} />
            <Route path="/ventes/commandes-client/nouveau" element={<PlaceholderPage title="Nouvelle commande client" />} />
            <Route path="/ventes/directes" element={<PlaceholderPage title="Ventes directes / Caisse" />} />
            <Route path="/ventes/directes/nouveau" element={<PlaceholderPage title="Nouvelle vente directe" />} />
          </Route>

          {/* Stock — GS-IA-2026-01 §2-3 */}
          <Route element={<RequireAuth roles={ROLES_PAR_SECTION.stock} />}>
            <Route path="/stock/mouvements" element={<PlaceholderPage title="Mouvements de stock" />} />
            <Route path="/stock/corrections" element={<PlaceholderPage title="Corrections manuelles" />} />
          </Route>

          {/* Utilisateurs — Admin Groupe (groupe) et Admin Filiale (sa filiale) */}
          <Route element={<RequireAuth roles={ROLES_PAR_SECTION.utilisateurs} />}>
            <Route path="/utilisateurs" element={<PlaceholderPage title="Gestion des utilisateurs" />} />
          </Route>

          {/* Profil */}
          <Route path="/profil" element={<PlaceholderPage title="Mon profil" />} />
        </Route>
      </Route>

      {/* 404 */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
