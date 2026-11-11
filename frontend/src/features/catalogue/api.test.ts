import { beforeEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from '@/shared/lib/api-client';
import { listerArticles, listerCategories, obtenirArticle } from './api';
import type { ArticleListResponse, CategorieListResponse } from './types';

vi.mock('@/shared/lib/api-client', () => ({
  apiClient: { get: vi.fn() },
}));

const enveloppe = <T,>(data: T) => ({
  data: { success: true, message: 'OK', data, timestamp: '2026-01-01T00:00:00Z' },
});

const pageArticles: ArticleListResponse = {
  content: [],
  page: 1,
  size: 20,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
};

const pageCategories: CategorieListResponse = {
  content: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
};

describe('API catalogue', () => {
  beforeEach(() => {
    vi.mocked(apiClient.get).mockReset();
  });

  it('transmet pagination, tri et recherche nettoyée à GET /articles', async () => {
    vi.mocked(apiClient.get).mockResolvedValue(enveloppe(pageArticles));

    const resultat = await listerArticles({ page: 2, search: '  riz  ' });

    expect(apiClient.get).toHaveBeenCalledWith('/articles', {
      params: {
        page: 2,
        size: 20,
        sort: 'designation',
        direction: 'ASC',
        search: 'riz',
      },
    });
    expect(resultat).toEqual(pageArticles);
  });

  it('omet une recherche vide', async () => {
    vi.mocked(apiClient.get).mockResolvedValue(enveloppe(pageArticles));

    await listerArticles({ page: 0, search: '   ' });

    expect(vi.mocked(apiClient.get).mock.calls[0][1]?.params).toMatchObject({
      search: undefined,
    });
  });

  it('lit la fiche article dans data.data', async () => {
    const article = { id: 3, codeArticle: 'ART-3' };
    vi.mocked(apiClient.get).mockResolvedValue(enveloppe(article));

    await expect(obtenirArticle(3)).resolves.toBe(article);
    expect(apiClient.get).toHaveBeenCalledWith('/articles/3');
  });

  it('utilise le filtre designation pour GET /categories', async () => {
    vi.mocked(apiClient.get).mockResolvedValue(enveloppe(pageCategories));

    await listerCategories({ page: 1, designation: 'Boissons' });

    expect(apiClient.get).toHaveBeenCalledWith('/categories', {
      params: {
        page: 1,
        size: 20,
        sort: 'designation',
        direction: 'ASC',
        designation: 'Boissons',
      },
    });
  });
});
