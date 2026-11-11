import { useSearchParams } from 'react-router-dom';

/**
 * État de liste porté par l'URL (Q10=A) : partageable, rechargeable et
 * compatible bouton retour. La page est 1-based dans l'URL, 0-based côté API.
 */
export const PAGE_PAR_DEFAUT = 1;
export const TAILLE_PAGE = 20;

export function usePaginationUrl() {
  const [searchParams, setSearchParams] = useSearchParams();

  const query = searchParams.get('q') ?? '';
  const pageLue = Number.parseInt(searchParams.get('page') ?? '', 10);
  const page =
    Number.isFinite(pageLue) && pageLue > 0 ? pageLue : PAGE_PAR_DEFAUT;

  const ecrire = (nouvelles: { q?: string; page?: number }) => {
    const params = new URLSearchParams(searchParams);
    const q = nouvelles.q ?? query;
    const pageCible = nouvelles.page ?? page;

    if (q.trim()) params.set('q', q);
    else params.delete('q');

    if (pageCible > PAGE_PAR_DEFAUT) params.set('page', String(pageCible));
    else params.delete('page');

    setSearchParams(params, { replace: true });
  };

  return {
    query,
    page,
    /** Revenir en page 1 dès que la recherche change. */
    changerRecherche: (q: string) => ecrire({ q, page: PAGE_PAR_DEFAUT }),
    changerPage: (pageCible: number) => ecrire({ page: pageCible }),
  };
}
