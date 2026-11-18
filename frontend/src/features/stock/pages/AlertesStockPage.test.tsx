import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AlertesStockPage } from './AlertesStockPage';
import { listerStock } from '../api';
import type { StockArticle } from '../types';

vi.mock('../api', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../api')>()),
  listerStock: vi.fn(),
}));

const articleSousSeuil: StockArticle = {
  articleId: 7,
  codeArticle: 'RIZ50KG',
  designation: 'Riz 50 kg',
  stockActuel: 4,
  seuilAlerte: 10,
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
          <Route path="/stock/alertes" element={<AlertesStockPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('AlertesStockPage', () => {
  beforeEach(() => {
    vi.mocked(listerStock).mockReset();
  });

  it('liste les articles sous le seuil par défaut', async () => {
    vi.mocked(listerStock).mockResolvedValue({
      content: [articleSousSeuil],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
    });

    rendre('/stock/alertes');

    expect(await screen.findByText('Riz 50 kg')).toBeInTheDocument();
    expect(screen.getByText('RIZ50KG')).toBeInTheDocument();
    expect(screen.getByText('4')).toBeInTheDocument();
    expect(screen.getByText('10')).toBeInTheDocument();
    // Le périmètre filiale est annoncé explicitement (pas de vue groupe).
    expect(screen.getByText(/votre filiale de rattachement uniquement/)).toBeInTheDocument();
    expect(listerStock).toHaveBeenCalledWith({
      statutAlerte: 'BAS',
      page: 0,
      size: 20,
    });
  });

  it('recharge la liste sur le statut choisi et repart de la première page', async () => {
    vi.mocked(listerStock).mockResolvedValue({
      content: [articleSousSeuil],
      page: 2,
      size: 20,
      totalElements: 45,
      totalPages: 3,
    });

    rendre('/stock/alertes?page=3');
    expect(await screen.findByText('Riz 50 kg')).toBeInTheDocument();
    expect(listerStock).toHaveBeenCalledWith({
      statutAlerte: 'BAS',
      page: 2,
      size: 20,
    });

    vi.mocked(listerStock).mockResolvedValue({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    });

    fireEvent.click(screen.getByRole('button', { name: 'Rupture' }));

    await waitFor(() =>
      expect(listerStock).toHaveBeenLastCalledWith({
        statutAlerte: 'RUPTURE',
        page: 0,
        size: 20,
      }),
    );
  });

  it('affiche un état vide explicite quand aucune alerte ne remonte', async () => {
    vi.mocked(listerStock).mockResolvedValue({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    });

    rendre('/stock/alertes?statut=ANOMALIE');

    expect(await screen.findByText('Aucune alerte')).toBeInTheDocument();
    expect(
      screen.getByText('Aucune anomalie de stock détectée.'),
    ).toBeInTheDocument();
    expect(listerStock).toHaveBeenCalledWith({
      statutAlerte: 'ANOMALIE',
      page: 0,
      size: 20,
    });
  });
});
