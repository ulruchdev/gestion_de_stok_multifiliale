import { create } from 'zustand';
import { apiClient } from '@/shared/lib/api-client';
import type {
  AuthState,
  LoginRequest,
  LoginResponse,
  Utilisateur,
} from '@/shared/types';

interface AuthActions {
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshSession: () => Promise<void>;
  setUser: (user: Utilisateur) => void;
  initialize: () => void;
}

type AuthStore = AuthState & AuthActions;

export const useAuthStore = create<AuthStore>((set, get) => ({
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: true,

  initialize: () => {
    const token = localStorage.getItem('access_token');
    const userStr = localStorage.getItem('user');
    if (token && userStr) {
      try {
        const user = JSON.parse(userStr) as Utilisateur;
        set({
          user,
          accessToken: token,
          isAuthenticated: true,
          isLoading: false,
        });
      } catch {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('user');
        set({ isLoading: false });
      }
    } else {
      set({ isLoading: false });
    }
  },

  login: async (credentials: LoginRequest) => {
    const response = await apiClient.post<{ data: LoginResponse }>(
      '/auth/login',
      credentials,
    );
    const { accessToken, refreshToken, utilisateur } = response.data.data;

    localStorage.setItem('access_token', accessToken);
    localStorage.setItem('refresh_token', refreshToken);
    localStorage.setItem('user', JSON.stringify(utilisateur));

    set({
      user: utilisateur,
      accessToken,
      refreshToken,
      isAuthenticated: true,
      isLoading: false,
    });
  },

  logout: async () => {
    try {
      await apiClient.post('/auth/logout');
    } catch {
      // Déconnexion locale même si la requête échoue
    } finally {
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('user');
      set({
        user: null,
        accessToken: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,
      });
    }
  },

  refreshSession: async () => {
    try {
      const refreshToken = localStorage.getItem('refresh_token');
      if (!refreshToken) throw new Error('No refresh token');

      const response = await apiClient.post<{ data: { accessToken: string } }>(
        '/auth/refresh',
        { refreshToken },
      );

      const newAccessToken = response.data.data.accessToken;
      localStorage.setItem('access_token', newAccessToken);
      set({ accessToken: newAccessToken });
    } catch {
      await get().logout();
    }
  },

  setUser: (user: Utilisateur) => {
    localStorage.setItem('user', JSON.stringify(user));
    set({ user });
  },
}));

// Provider d'authentification pour initialiser le store
import { useEffect, type ReactNode } from 'react';

export function AuthProvider({ children }: { children: ReactNode }) {
  const initialize = useAuthStore((s) => s.initialize);

  useEffect(() => {
    initialize();
  }, [initialize]);

  return <>{children}</>;
}
