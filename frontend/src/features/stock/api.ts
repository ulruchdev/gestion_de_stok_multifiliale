import { apiClient } from '@/shared/lib/api-client';
import type { ApiResponse } from '@/shared/types';
import type { StockListResponse } from './types';

/** Taille de page de la liste d'alertes (le backend borne à 100). */
export const TAILLE_PAGE_ALERTES = 20;

/** Statuts filtrés par la page : seuls ceux qui signalent une alerte. */
export const STATUTS_ALERTE = ['BAS', 'RUPTURE', 'ANOMALIE'] as const;
export type StatutAlerteFiltre = (typeof STATUTS_ALERTE)[number];

export function estStatutAlerteFiltre(valeur: string): valeur is StatutAlerteFiltre {
  return (STATUTS_ALERTE as readonly string[]).includes(valeur);
}

export interface ParametresAlertesStock {
  statutAlerte: StatutAlerteFiltre;
  page: number;
  size?: number;
}

/**
 * GET /api/v1/stock?statutAlerte= — le filtre est appliqué en SQL côté backend
 * (US-051), donc la pagination reste exacte : on ne filtre jamais côté client
 * une page déjà tronquée.
 */
export async function listerStock({
  statutAlerte,
  page,
  size = TAILLE_PAGE_ALERTES,
}: ParametresAlertesStock): Promise<StockListResponse> {
  const { data } = await apiClient.get<ApiResponse<StockListResponse>>('/stock', {
    params: { page, size, statutAlerte },
  });
  return data.data;
}
