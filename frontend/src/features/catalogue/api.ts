import { apiClient } from '@/shared/lib/api-client';
import type { ApiResponse } from '@/shared/types';
import type {
  Article,
  ArticleListResponse,
  CategorieListResponse,
} from './types';

/** Taille de page imposée par l'UI (le backend borne à 100). */
export const TAILLE_PAGE_CATALOGUE = 20;

export interface ParametresListeArticles {
  page: number;
  size?: number;
  search?: string;
}

export interface ParametresListeCategories {
  page: number;
  size?: number;
  designation?: string;
}

/**
 * GET /api/v1/articles — liste paginée groupe + recherche plein-texte
 * (code article ou désignation, insensible à la casse), tri designation ASC.
 */
export async function listerArticles({
  page,
  size = TAILLE_PAGE_CATALOGUE,
  search,
}: ParametresListeArticles): Promise<ArticleListResponse> {
  const { data } = await apiClient.get<ApiResponse<ArticleListResponse>>(
    '/articles',
    {
      params: {
        page,
        size,
        sort: 'designation',
        direction: 'ASC',
        search: search?.trim() || undefined,
      },
    },
  );
  return data.data;
}

/** GET /api/v1/articles/{id} — fiche article (404 hors groupe). */
export async function obtenirArticle(id: number): Promise<Article> {
  const { data } = await apiClient.get<ApiResponse<Article>>(`/articles/${id}`);
  return data.data;
}

/**
 * GET /api/v1/categories — liste paginée groupe + filtre designation.
 * Réservé à GESTIONNAIRE_STOCK / ADMIN_FILIALE / ADMIN_GROUPE (RBAC backend).
 */
export async function listerCategories({
  page,
  size = TAILLE_PAGE_CATALOGUE,
  designation,
}: ParametresListeCategories): Promise<CategorieListResponse> {
  const { data } = await apiClient.get<ApiResponse<CategorieListResponse>>(
    '/categories',
    {
      params: {
        page,
        size,
        sort: 'designation',
        direction: 'ASC',
        designation: designation?.trim() || undefined,
      },
    },
  );
  return data.data;
}
