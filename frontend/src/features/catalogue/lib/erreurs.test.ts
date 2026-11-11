import { AxiosError, AxiosHeaders } from 'axios';
import { describe, expect, it } from 'vitest';
import { codeDErreur, estIntrouvable, messageDErreurCatalogue } from './erreurs';

function erreurHttp(status: number, data: Record<string, unknown>) {
  const config = { headers: new AxiosHeaders() };
  return new AxiosError(
    'Request failed',
    'ERR_BAD_REQUEST',
    config,
    undefined,
    {
      data,
      status,
      statusText: '',
      headers: {},
      config,
    },
  );
}

describe('erreurs catalogue', () => {
  it('privilégie le message métier du code connu', () => {
    const erreur = erreurHttp(403, {
      errorCode: 'GRP_002',
      detail: 'Détail technique',
      title: 'Forbidden',
    });

    expect(codeDErreur(erreur)).toBe('GRP_002');
    expect(messageDErreurCatalogue(erreur)).toBe(
      'Cette ressource appartient à un autre groupe.',
    );
  });

  it('retombe sur le detail RFC 7807 pour un code inconnu', () => {
    const erreur = erreurHttp(400, {
      errorCode: 'XYZ_999',
      detail: 'Message backend',
    });

    expect(messageDErreurCatalogue(erreur)).toBe('Message backend');
  });

  it('gère une erreur non HTTP', () => {
    expect(messageDErreurCatalogue(new Error('réseau'))).toBe('réseau');
    expect(codeDErreur(new Error('réseau'))).toBeUndefined();
  });

  it('identifie un 404 métier', () => {
    expect(estIntrouvable(erreurHttp(404, { errorCode: 'RES_001' }))).toBe(true);
    expect(estIntrouvable(erreurHttp(403, { errorCode: 'GRP_002' }))).toBe(false);
    expect(estIntrouvable(new Error('boom'))).toBe(false);
  });
});
