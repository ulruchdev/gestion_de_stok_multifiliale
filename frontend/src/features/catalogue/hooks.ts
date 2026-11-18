import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { useMemo } from 'react';
import {
  obtenirArticle,
  listerArticles,
  listerCategories,
  type ParametresListeArticles,
  type ParametresListeCategories,
} from './api';

/** Clés de cache TanStack Query — une entrée par combinaison de filtres. */
export const clesCatalogue = {
  articles: (params: ParametresListeArticles) =>
    ['catalogue', 'articles', params.page, params.size, params.search ?? ''] as const,
  article: (id: number) => ['catalogue', 'article', id] as const,
  categories: (params: ParametresListeCategories) =>
    ['catalogue', 'categories', params.page, params.size, params.designation ?? ''] as const,
  toutesCategories: ['catalogue', 'categories', 'toutes'] as const,
};

export function useArticles(params: ParametresListeArticles) {
  return useQuery({
    queryKey: clesCatalogue.articles(params),
    queryFn: () => listerArticles(params),
    // Garde la page précédente pendant le changement de page / recherche :
    // le tableau ne repasse pas en squelette à chaque frappe.
    placeholderData: keepPreviousData,
  });
}

export function useArticle(id: number | null) {
  return useQuery({
    queryKey: clesCatalogue.article(id ?? -1),
    queryFn: () => obtenirArticle(id as number),
    enabled: id !== null,
  });
}

export function useCategories(params: ParametresListeCategories) {
  return useQuery({
    queryKey: clesCatalogue.categories(params),
    queryFn: () => listerCategories(params),
    placeholderData: keepPreviousData,
  });
}

/**
 * Table id → désignation pour afficher la catégorie d'un article.
 * Le backend ne renvoie que `categorieId` dans `ArticleResponse` : on lit la
 * première page (100 max) du référentiel, déjà mise en cache par `useCategories`.
 */
export function useCategoriesLookup() {
  const { data } = useCategories({ page: 0, size: 100 });

  return useMemo(
    () => new Map((data?.content ?? []).map((c) => [c.id, c.designation])),
    [data],
  );
}
