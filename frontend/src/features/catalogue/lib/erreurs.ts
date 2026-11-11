import { getErrorMessage } from '@/shared/lib/api-client';
import type { ProblemResponse } from '@/shared/types';
import axios from 'axios';

/**
 * Codes métier du catalogue et de ses dépendances (Q8=B).
 * Source : `ErrorCode.java` backend. Un code inconnu retombe sur
 * `getErrorMessage()` (detail/title RFC 7807), jamais sur un message inventé.
 */
const MESSAGES_PAR_CODE: Record<string, string> = {
  RES_001: 'Cette ressource est introuvable.',
  RES_002: 'Ce code est déjà utilisé.',
  CAT_001: 'Ce taux de TVA n’est pas supporté.',
  GRP_002: 'Cette ressource appartient à un autre groupe.',
  SEC_001: 'Vous n’avez pas les droits pour cette opération.',
  SYS_001: 'Une erreur interne est survenue. Réessayez plus tard.',
  SYS_005: 'Certains champs sont invalides.',
};

export function codeDErreur(error: unknown): string | undefined {
  if (!axios.isAxiosError<ProblemResponse>(error)) return undefined;
  return error.response?.data?.errorCode;
}

export function messageDErreurCatalogue(error: unknown): string {
  const code = codeDErreur(error);
  if (code && MESSAGES_PAR_CODE[code]) return MESSAGES_PAR_CODE[code];
  return getErrorMessage(error);
}

/** Vrai pour un 404 métier : l'article n'existe pas ou sort du périmètre groupe. */
export function estIntrouvable(error: unknown): boolean {
  return (
    axios.isAxiosError(error) &&
    (error.response?.status === 404 || codeDErreur(error) === 'RES_001')
  );
}
