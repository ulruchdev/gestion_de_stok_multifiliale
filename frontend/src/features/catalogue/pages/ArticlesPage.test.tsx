import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError, AxiosHeaders } from 'axios';
import { ArticlesPage } from './ArticlesPage';
import { listerArticles, listerCategories } from '../api';
import type { Article } from '../types';

vi.mock('../api', () => ({
  listerArticles: vi.fn(),
  listerCategories: vi.fn(),
  obtenirArticle: vi.fn(),
}));

const article: Article = {
  id: 7,
  codeArticle: 'RIZ50KG',
  designation: 'Riz 50 kg',
  categorieId: 2,
  prixAchatHt: 15000,
  prixVenteHt: 18500,
  tauxTva: 19.25,
  prixVenteTtc: 22061,
  margeBrutePct: 23.33,
  seuilAlerte: 10,
  actif: true,
  groupId: 1,
  stockActuel: 4,
  statutAlerte: 'BAS',
};

function rendre(entree: string) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[entree]}>
        <Routes>
          <Route path="/catalogue/articles" element={<ArticlesPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('ArticlesPage', () => {
  beforeEach(() => {
    vi.mocked(listerArticles).mockReset();
    vi.mocked(listerCategories).mockReset();
    vi.mocked(listerCategories).mockResolvedValue({
      content: [
        { id: 2, code: 'CEREALES', designation: 'Céréales', tauxTva: 19.25, groupId: 1 },
      ],
      page: 0,
      size: 100,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
    });
  });

  it('affiche la page renvoyée par l’API avec stock, alerte et catégorie', async () => {
    vi.mocked(listerArticles).mockResolvedValue({
      content: [article],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
    });

    rendre('/catalogue/articles');

    expect(await screen.findByText('Riz 50 kg')).toBeInTheDocument();
    expect(screen.getByText('RIZ50KG')).toBeInTheDocument();
    expect(screen.getByText('Céréales')).toBeInTheDocument();
    expect(screen.getByText('Bas')).toBeInTheDocument();
    expect(listerArticles).toHaveBeenCalledWith({ page: 0, size: 20, search: '' });
  });

  it('lit recherche et page depuis l’URL et convertit la page pour l’API', async () => {
    vi.mocked(listerArticles).mockResolvedValue({
      content: [article],
      page: 1,
      size: 20,
      totalElements: 21,
      totalPages: 2,
      first: false,
      last: true,
    });

    rendre('/catalogue/articles?q=riz&page=2');

    expect(await screen.findByText('Riz 50 kg')).toBeInTheDocument();
    expect(listerArticles).toHaveBeenCalledWith({ page: 1, size: 20, search: 'riz' });
    expect(screen.getByText('Page 2 sur 2')).toBeInTheDocument();
  });

  it('affiche le message métier quand l’API refuse l’accès', async () => {
    const config = { headers: new AxiosHeaders() };
    vi.mocked(listerArticles).mockRejectedValue(
      new AxiosError('Forbidden', 'ERR_BAD_REQUEST', config, undefined, {
        data: {
          type: '/errors/grp-002',
          title: 'Forbidden',
          status: 403,
          detail: 'Interdit',
          instance: '/api/v1/articles',
          errorCode: 'GRP_002',
          timestamp: '2026-01-01T00:00:00Z',
        },
        status: 403,
        statusText: 'Forbidden',
        headers: {},
        config,
      }),
    );

    rendre('/catalogue/articles');

    expect(
      await screen.findByText('Cette ressource appartient à un autre groupe.'),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Réessayer' })).toBeInTheDocument();
  });
});
