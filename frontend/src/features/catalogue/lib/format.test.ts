import { describe, expect, it } from 'vitest';
import {
  APPARENCE_STATUT_ALERTE,
  formaterMontantXaf,
  formaterPourcentage,
  formaterStock,
} from './format';

const normaliser = (texte: string) => texte.replace(/[\u202f\u00a0]/g, ' ');

describe('formatage catalogue', () => {
  it('formate un montant XAF en fr-CM sans décimale', () => {
    expect(normaliser(formaterMontantXaf(12500))).toBe('12 500 FCFA');
    expect(normaliser(formaterMontantXaf(12500.6))).toBe('12 501 FCFA');
  });

  it('rend un tiret pour un montant indisponible', () => {
    expect(formaterMontantXaf(null)).toBe('—');
    expect(formaterMontantXaf(undefined)).toBe('—');
  });

  it('formate un taux en pourcentage avec 2 décimales maximum', () => {
    expect(normaliser(formaterPourcentage(19.25))).toBe('19,25 %');
    expect(normaliser(formaterPourcentage(0))).toBe('0 %');
    expect(formaterPourcentage(null)).toBe('—');
  });

  it('formate le stock sans décimale inutile', () => {
    expect(normaliser(formaterStock(0))).toBe('0');
    expect(normaliser(formaterStock(12.5))).toBe('12,5');
    expect(formaterStock(null)).toBe('—');
  });

  it('couvre les quatre statuts d’alerte du référentiel', () => {
    expect(Object.keys(APPARENCE_STATUT_ALERTE)).toEqual([
      'NORMAL',
      'BAS',
      'RUPTURE',
      'ANOMALIE',
    ]);
  });
});
