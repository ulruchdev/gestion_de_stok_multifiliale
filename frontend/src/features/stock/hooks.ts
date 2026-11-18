import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { listerStock, type ParametresAlertesStock } from './api';

/** Clés de cache — une entrée par statut et par page. */
export const clesStock = {
  alertes: (params: ParametresAlertesStock) =>
    ['stock', 'alertes', params.statutAlerte, params.page, params.size ?? ''] as const,
};

export function useStockAlertes(params: ParametresAlertesStock) {
  return useQuery({
    queryKey: clesStock.alertes(params),
    queryFn: () => listerStock(params),
    // Le tableau garde la page précédente pendant un changement d'onglet :
    // pas de retour au squelette à chaque clic.
    placeholderData: keepPreviousData,
  });
}
