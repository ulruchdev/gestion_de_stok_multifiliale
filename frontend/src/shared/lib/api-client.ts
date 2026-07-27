import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import type { ApiResponse, ProblemResponse } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// File d'attente des requêtes en attente de refresh
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
}> = [];

function processQueue(error: unknown, token: string | null = null) {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token!);
    }
  });
  failedQueue = [];
}

// Intercepteur Request : ajoute le token JWT
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('access_token');
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Intercepteur Response : refresh automatique sur 401
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ProblemResponse>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    // Ne pas intercepter les erreurs d'auth (login, register, refresh)
    const isAuthEndpoint =
      originalRequest.url?.includes('/auth/') ?? false;

    if (error.response?.status === 401 && !originalRequest._retry && !isAuthEndpoint) {
      if (isRefreshing) {
        // Si un refresh est en cours, mettre en file d'attente
        return new Promise((resolve, reject) => {
          failedQueue.push({
            resolve: (token: string) => {
              if (originalRequest.headers) {
                originalRequest.headers.Authorization = `Bearer ${token}`;
              }
              resolve(apiClient(originalRequest));
            },
            reject,
          });
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshToken = localStorage.getItem('refresh_token');
        const response = await axios.post<ApiResponse<{ accessToken: string }>>(
          `${API_BASE_URL}/auth/refresh`,
          { refreshToken },
        );

        const newAccessToken = response.data.data.accessToken;
        localStorage.setItem('access_token', newAccessToken);
        processQueue(null, newAccessToken);

        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        }
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        // Échec du refresh → déconnexion
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  },
);

// Helpers pour extraire les erreurs métier
export function getErrorMessage(error: unknown): string {
  if (error instanceof AxiosError && error.response?.data) {
    const problem = error.response.data;
    return problem.detail || problem.title || 'Une erreur est survenue';
  }
  if (error instanceof Error) return error.message;
  return 'Une erreur inattendue est survenue';
}

export function getFieldErrors(error: unknown): Record<string, string[]> | undefined {
  if (error instanceof AxiosError && error.response?.data) {
    return (error.response.data as ProblemResponse).errors;
  }
  return undefined;
}
