import { beforeEach, describe, expect, it, vi } from 'vitest';
import { useAuthStore } from './useAuthStore';
import { RoleUtilisateur, ScopeUtilisateur, type Utilisateur } from '@/shared/types';

const mocks = vi.hoisted(() => ({ post: vi.fn() }));

vi.mock('@/shared/lib/api-client', () => ({
  apiClient: { post: mocks.post },
}));

const utilisateur: Utilisateur = {
  id: 1,
  email: 'ada@stockmaster.cm',
  nom: 'Nkeng',
  prenom: 'Ada',
  role: RoleUtilisateur.ADMIN_FILIALE,
  scope: ScopeUtilisateur.FILIALE,
  entrepriseId: 10,
  groupId: 3,
};

beforeEach(() => {
  localStorage.clear();
  mocks.post.mockReset();
  useAuthStore.setState({
    user: null,
    accessToken: null,
    refreshToken: null,
    isAuthenticated: false,
    isLoading: true,
  });
});

describe('useAuthStore — initialize', () => {
  it('restaure la session depuis le stockage local', () => {
    localStorage.setItem('access_token', 'access-1');
    localStorage.setItem('user', JSON.stringify(utilisateur));

    useAuthStore.getState().initialize();

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user).toEqual(utilisateur);
    expect(state.accessToken).toBe('access-1');
    expect(state.isLoading).toBe(false);
  });

  it('purge une session corrompue', () => {
    localStorage.setItem('access_token', 'access-1');
    localStorage.setItem('refresh_token', 'refresh-1');
    localStorage.setItem('user', '{invalide');

    useAuthStore.getState().initialize();

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(false);
    expect(state.isLoading).toBe(false);
    expect(localStorage.getItem('access_token')).toBeNull();
    expect(localStorage.getItem('refresh_token')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
  });

  it('reste anonyme sans jeton', () => {
    useAuthStore.getState().initialize();

    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(useAuthStore.getState().isLoading).toBe(false);
  });
});

describe('useAuthStore — login / logout', () => {
  it('persiste les jetons et l’utilisateur après connexion', async () => {
    mocks.post.mockResolvedValue({
      data: {
        data: {
          accessToken: 'access-1',
          refreshToken: 'refresh-1',
          expiresIn: 900,
          role: utilisateur.role,
          scope: utilisateur.scope,
          utilisateur,
        },
      },
    });

    await useAuthStore.getState().login({
      email: utilisateur.email,
      motDePasse: 'secret',
    });

    expect(mocks.post).toHaveBeenCalledWith('/auth/login', {
      email: utilisateur.email,
      motDePasse: 'secret',
    });
    expect(localStorage.getItem('access_token')).toBe('access-1');
    expect(localStorage.getItem('refresh_token')).toBe('refresh-1');
    expect(JSON.parse(localStorage.getItem('user') as string)).toEqual(utilisateur);

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user).toEqual(utilisateur);
  });

  it('déconnecte localement même si l’appel serveur échoue', async () => {
    localStorage.setItem('access_token', 'access-1');
    localStorage.setItem('refresh_token', 'refresh-1');
    localStorage.setItem('user', JSON.stringify(utilisateur));
    useAuthStore.setState({
      user: utilisateur,
      accessToken: 'access-1',
      refreshToken: 'refresh-1',
      isAuthenticated: true,
      isLoading: false,
    });
    mocks.post.mockRejectedValue(new Error('réseau indisponible'));

    await useAuthStore.getState().logout();

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(false);
    expect(state.user).toBeNull();
    expect(localStorage.getItem('access_token')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
  });
});
