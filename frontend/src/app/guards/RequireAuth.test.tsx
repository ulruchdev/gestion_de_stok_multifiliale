import { beforeEach, describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { useAuthStore } from '@/features/auth';
import { RoleUtilisateur, ScopeUtilisateur, type Utilisateur } from '@/shared/types';
import { ROLES_PAR_SECTION } from '../permissions';
import { RequireAuth } from './RequireAuth';

function makeUser(role: RoleUtilisateur): Utilisateur {
  return {
    id: 1,
    email: 'ada@stockmaster.cm',
    nom: 'Nkeng',
    prenom: 'Ada',
    role,
    scope: ScopeUtilisateur.FILIALE,
    entrepriseId: 10,
    groupId: 3,
  };
}

function renderAt(path: string, roles?: readonly RoleUtilisateur[]) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route element={<RequireAuth roles={roles} />}>
          <Route path="/stock/mouvements" element={<div>Mouvements de stock</div>} />
        </Route>
        <Route path="/dashboard" element={<div>Tableau de bord</div>} />
        <Route path="/login" element={<div>Connexion</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

beforeEach(() => {
  useAuthStore.setState({
    user: null,
    accessToken: null,
    refreshToken: null,
    isAuthenticated: false,
    isLoading: false,
  });
});

describe('RequireAuth', () => {
  it('redirige un visiteur non authentifié vers /login', () => {
    renderAt('/stock/mouvements', ROLES_PAR_SECTION.stock);

    expect(screen.getByText('Connexion')).toBeInTheDocument();
  });

  it('redirige vers /dashboard quand le rôle est hors périmètre', () => {
    useAuthStore.setState({
      user: makeUser(RoleUtilisateur.CAISSIER),
      isAuthenticated: true,
    });

    renderAt('/stock/mouvements', ROLES_PAR_SECTION.stock);

    expect(screen.getByText('Tableau de bord')).toBeInTheDocument();
    expect(screen.queryByText('Mouvements de stock')).not.toBeInTheDocument();
  });

  it('laisse passer un rôle autorisé', () => {
    useAuthStore.setState({
      user: makeUser(RoleUtilisateur.GESTIONNAIRE_STOCK),
      isAuthenticated: true,
    });

    renderAt('/stock/mouvements', ROLES_PAR_SECTION.stock);

    expect(screen.getByText('Mouvements de stock')).toBeInTheDocument();
  });

  it('refuse une session authentifiée sans utilisateur sur une route restreinte', () => {
    useAuthStore.setState({ user: null, isAuthenticated: true });

    renderAt('/stock/mouvements', ROLES_PAR_SECTION.stock);

    expect(screen.getByText('Tableau de bord')).toBeInTheDocument();
  });

  it('laisse passer tout utilisateur authentifié quand aucun rôle n’est exigé', () => {
    useAuthStore.setState({
      user: makeUser(RoleUtilisateur.COMMERCIAL),
      accessToken: 'access-1',
      isAuthenticated: true,
    });

    renderAt('/stock/mouvements');

    expect(screen.getByText('Mouvements de stock')).toBeInTheDocument();
  });

  it('affiche un chargement pendant la restauration de session', () => {
    useAuthStore.setState({ isLoading: true });

    const { container } = renderAt('/stock/mouvements', ROLES_PAR_SECTION.stock);

    expect(screen.queryByText('Connexion')).not.toBeInTheDocument();
    expect(screen.queryByText('Mouvements de stock')).not.toBeInTheDocument();
    expect(container.querySelector('.animate-spin')).toBeInTheDocument();
  });
});
