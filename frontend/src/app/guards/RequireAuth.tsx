import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/features/auth';
import type { RoleUtilisateur } from '@/shared/types';

interface RequireAuthProps {
  roles?: readonly RoleUtilisateur[];
}

/**
 * Garde d'accès authentification + rôle.
 * - non authentifié → /login
 * - authentifié mais rôle hors périmètre → /dashboard
 */
export function RequireAuth({ roles }: RequireAuthProps) {
  const { isAuthenticated, user, isLoading } = useAuthStore();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    );
  }

  if (!isAuthenticated) return <Navigate to="/login" replace />;

  // Un utilisateur authentifié sans rôle connu ne doit pas passer un guard restreint.
  if (roles && (!user || !roles.includes(user.role))) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
}
