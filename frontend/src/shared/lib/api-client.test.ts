import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import axios, { type AxiosAdapter, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';

const REFRESH_TOKEN = 'refresh-1';

type ClientModule = typeof import('@/shared/lib/api-client');

let apiClient: ClientModule['apiClient'];

const initialLocation = window.location;

function ok(config: InternalAxiosRequestConfig): AxiosResponse {
  return {
    data: { success: true },
    status: 200,
    statusText: 'OK',
    headers: {},
    config,
  };
}

function unauthorized(config: InternalAxiosRequestConfig): Promise<never> {
  return Promise.reject({
    isAxiosError: true,
    config,
    response: {
      status: 401,
      statusText: 'Unauthorized',
      headers: {},
      config,
      data: { title: 'Non autorisé', detail: 'Token expiré', status: 401 },
    },
  });
}

/** Un module neuf par test : l'état `isRefreshing` / `failedQueue` est interne au module. */
async function loadClient(): Promise<ClientModule> {
  vi.resetModules();
  const mod = await import('@/shared/lib/api-client');
  apiClient = mod.apiClient;
  return mod;
}

function setAdapter(adapter: AxiosAdapter): void {
  apiClient.defaults.adapter = adapter;
}

beforeEach(async () => {
  localStorage.clear();
  await loadClient();
  Object.defineProperty(window, 'location', {
    configurable: true,
    value: { href: '/' },
  });
});

afterEach(() => {
  Object.defineProperty(window, 'location', {
    configurable: true,
    value: initialLocation,
  });
});

describe('api-client — intercepteur de requête', () => {
  it('ajoute le jeton d’accès stocké', async () => {
    localStorage.setItem('access_token', 'access-1');
    let authorization: unknown;

    setAdapter(async (config) => {
      authorization = config.headers.get('Authorization');
      return ok(config);
    });

    await apiClient.get('/tiers/clients');

    expect(authorization).toBe('Bearer access-1');
  });

  it('n’ajoute pas d’en-tête sans jeton', async () => {
    let authorization: unknown;

    setAdapter(async (config) => {
      authorization = config.headers.get('Authorization');
      return ok(config);
    });

    await apiClient.get('/tiers/clients');

    expect(authorization).toBeUndefined();
  });
});

describe('api-client — refresh sur 401', () => {
  it('rafraîchit, rejoue la requête et persiste le refresh token tourné', async () => {
    localStorage.setItem('access_token', 'access-1');
    localStorage.setItem('refresh_token', REFRESH_TOKEN);

    let calls = 0;
    setAdapter(async (config) => {
      calls += 1;
      return config.headers.get('Authorization') === 'Bearer access-2'
        ? ok(config)
        : unauthorized(config);
    });

    const post = vi.spyOn(axios, 'post').mockResolvedValue({
      data: { data: { accessToken: 'access-2', refreshToken: 'refresh-2' } },
    } as never);

    await expect(apiClient.get('/tiers/clients')).resolves.toMatchObject({ status: 200 });

    expect(post).toHaveBeenCalledTimes(1);
    expect(post).toHaveBeenCalledWith('/api/v1/auth/refresh', {
      refreshToken: REFRESH_TOKEN,
    });
    expect(calls).toBe(2);
    expect(localStorage.getItem('access_token')).toBe('access-2');
    expect(localStorage.getItem('refresh_token')).toBe('refresh-2');
  });

  it('ne lance qu’un seul refresh pour des 401 simultanés', async () => {
    localStorage.setItem('access_token', 'access-1');
    localStorage.setItem('refresh_token', REFRESH_TOKEN);

    setAdapter(async (config) =>
      config.headers.get('Authorization') === 'Bearer access-2'
        ? ok(config)
        : unauthorized(config),
    );

    let resolveRefresh!: (value: unknown) => void;
    const refresh = new Promise((resolve) => {
      resolveRefresh = resolve;
    });
    const post = vi.spyOn(axios, 'post').mockReturnValue(refresh as never);

    const first = apiClient.get('/tiers/clients');
    const second = apiClient.get('/fournisseurs');

    await vi.waitFor(() => expect(post).toHaveBeenCalledTimes(1));
    resolveRefresh({
      data: { data: { accessToken: 'access-2', refreshToken: 'refresh-2' } },
    });

    await expect(first).resolves.toMatchObject({ status: 200 });
    await expect(second).resolves.toMatchObject({ status: 200 });
    expect(post).toHaveBeenCalledTimes(1);
  });

  it('purge les jetons et renvoie vers /login si le refresh échoue', async () => {
    localStorage.setItem('access_token', 'access-1');
    localStorage.setItem('refresh_token', REFRESH_TOKEN);

    setAdapter(unauthorized as AxiosAdapter);
    vi.spyOn(axios, 'post').mockRejectedValue(new Error('refresh invalide'));

    await expect(apiClient.get('/tiers/clients')).rejects.toThrow('refresh invalide');

    expect(localStorage.getItem('access_token')).toBeNull();
    expect(localStorage.getItem('refresh_token')).toBeNull();
    expect(window.location.href).toBe('/login');
  });

  it('ne tente pas de refresh sur les endpoints /auth/', async () => {
    setAdapter(unauthorized as AxiosAdapter);
    const post = vi.spyOn(axios, 'post');

    await expect(
      apiClient.post('/auth/login', { email: 'a@b.cm', motDePasse: 'secret' }),
    ).rejects.toBeTruthy();

    expect(post).not.toHaveBeenCalled();
  });
});
