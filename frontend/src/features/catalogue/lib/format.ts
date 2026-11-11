import type { StatutAlerte } from '../types';

/**
 * Formatage monétaire unique du catalogue (Q7) :
 * `Intl.NumberFormat('fr-CM', { style: 'currency', currency: 'XAF' })`
 * avec 0 décimale — le XAF n'en a pas et les montants backend sont entiers.
 */
const FORMATEUR_XAF = new Intl.NumberFormat('fr-CM', {
  style: 'currency',
  currency: 'XAF',
  maximumFractionDigits: 0,
});

const FORMATEUR_POURCENTAGE = new Intl.NumberFormat('fr-CM', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
});

export function formaterMontantXaf(montant: number | null | undefined): string {
  if (montant === null || montant === undefined) return '—';
  return FORMATEUR_XAF.format(montant);
}

/** Taux de TVA et marge brute : valeur numérique en pourcentage (ex. « 19,25 % »). */
export function formaterPourcentage(
  valeur: number | null | undefined,
): string {
  if (valeur === null || valeur === undefined) return '—';
  return `${FORMATEUR_POURCENTAGE.format(valeur)} %`;
}

export function formaterStock(stock: number | null | undefined): string {
  if (stock === null || stock === undefined) return '—';
  return new Intl.NumberFormat('fr-CM', {
    maximumFractionDigits: 2,
  }).format(stock);
}

interface ApparenceStatut {
  libelle: string;
  variant: 'success' | 'warning' | 'destructive';
}

/** Libellés et couleurs des statuts d'alerte (REF §4.2). */
export const APPARENCE_STATUT_ALERTE: Record<StatutAlerte, ApparenceStatut> = {
  NORMAL: { libelle: 'Normal', variant: 'success' },
  BAS: { libelle: 'Bas', variant: 'warning' },
  RUPTURE: { libelle: 'Rupture', variant: 'destructive' },
  ANOMALIE: { libelle: 'Anomalie', variant: 'destructive' },
};
