import { http, HttpResponse } from 'msw';

const API_BASE = '/api/v1';

export const handlers = [
  // === Auth endpoints ===
  http.post(`${API_BASE}/auth/login`, async ({ request }) => {
    const body = (await request.json()) as { email: string; motDePasse: string };
    const mockUsers: Record<string, {
      accessToken: string;
      refreshToken: string;
      utilisateur: {
        id: number;
        email: string;
        nom: string;
        prenom: string;
        role: string;
        scope: string;
        entrepriseId: number;
        groupeId?: number;
        actif: boolean;
      };
    }> = {
      'admin@groupe.test': {
        accessToken: 'mock-token-admin-groupe',
        refreshToken: 'mock-refresh-admin-groupe',
        utilisateur: {
          id: 1, email: 'admin@groupe.test', nom: 'Admin', prenom: 'Groupe',
          role: 'ADMIN_GROUPE', scope: 'GROUPE', entrepriseId: 1, groupeId: 1, actif: true,
        },
      },
      'admin@filiale.test': {
        accessToken: 'mock-token-admin-filiale',
        refreshToken: 'mock-refresh-admin-filiale',
        utilisateur: {
          id: 2, email: 'admin@filiale.test', nom: 'Admin', prenom: 'Filiale',
          role: 'ADMIN_FILIALE', scope: 'FILIALE', entrepriseId: 2, groupeId: 1, actif: true,
        },
      },
      'caissier@filiale.test': {
        accessToken: 'mock-token-caissier',
        refreshToken: 'mock-refresh-caissier',
        utilisateur: {
          id: 3, email: 'caissier@filiale.test', nom: 'Kamga', prenom: 'Paul',
          role: 'CAISSIER', scope: 'FILIALE', entrepriseId: 2, groupeId: 1, actif: true,
        },
      },
    };

    const mockUser = mockUsers[body.email];
    if (mockUser && body.motDePasse === 'Test1234!') {
      return HttpResponse.json({
        success: true,
        message: 'Connexion réussie',
        data: mockUser,
        timestamp: new Date().toISOString(),
      });
    }
    return HttpResponse.json(
      {
        type: 'about:blank',
        title: 'Identifiants invalides',
        status: 401,
        detail: 'Email ou mot de passe incorrect',
        instance: '/api/v1/auth/login',
        timestamp: new Date().toISOString(),
      },
      { status: 401 },
    );
  }),

  http.post(`${API_BASE}/auth/refresh`, () => {
    return HttpResponse.json({
      success: true,
      message: 'Token rafraîchi',
      data: { accessToken: 'mock-access-token-refreshed' },
      timestamp: new Date().toISOString(),
    });
  }),

  http.post(`${API_BASE}/auth/logout`, () => {
    return HttpResponse.json({
      success: true,
      message: 'Déconnexion réussie',
      data: null,
      timestamp: new Date().toISOString(),
    });
  }),

  http.post(`${API_BASE}/auth/inscription/entreprise-unique`, async ({ request }) => {
    const body = await request.json();
    return HttpResponse.json({
      success: true,
      message: 'Entreprise créée avec succès',
      data: { id: 1, email: (body as { email: string }).email },
      timestamp: new Date().toISOString(),
    });
  }),

  http.post(`${API_BASE}/auth/inscription/groupe`, async ({ request }) => {
    const body = await request.json();
    return HttpResponse.json({
      success: true,
      message: 'Groupe créé avec succès',
      data: { groupId: 1, email: (body as { email: string }).email },
      timestamp: new Date().toISOString(),
    });
  }),

  // === Articles ===
  http.get(`${API_BASE}/articles`, () => {
    return HttpResponse.json({
      success: true,
      message: 'Liste des articles',
      data: {
        content: [
          { id: 1, codeArticle: 'ART-001', designation: 'Huile végétale 1L', prixVenteTtc: 2500, stockActuel: 45 },
          { id: 2, codeArticle: 'ART-002', designation: 'Riz parfumé 5kg', prixVenteTtc: 4500, stockActuel: 12 },
          { id: 3, codeArticle: 'ART-003', designation: 'Sucre en poudre 1kg', prixVenteTtc: 1200, stockActuel: 0 },
        ],
        totalElements: 3,
        totalPages: 1,
        number: 0,
        size: 20,
      },
      timestamp: new Date().toISOString(),
    });
  }),

  http.post(`${API_BASE}/auth/forgot-password`, () => {
    return HttpResponse.json({
      success: true,
      message: 'Email envoyé',
      data: null,
      timestamp: new Date().toISOString(),
    });
  }),

  http.post(`${API_BASE}/auth/reset-password`, () => {
    return HttpResponse.json({
      success: true,
      message: 'Mot de passe réinitialisé',
      data: null,
      timestamp: new Date().toISOString(),
    });
  }),

  // === Ventes directes ===
  http.get(`${API_BASE}/ventes`, () => {
    return HttpResponse.json({
      success: true,
      message: 'Liste des ventes',
      data: {
        content: [
          { id: 1, code: 'VNT-20260710-001', montantTtc: 18500, dateVente: new Date().toISOString(), statut: 'VALIDEE' },
          { id: 2, code: 'VNT-20260710-002', montantTtc: 5000, dateVente: new Date().toISOString(), statut: 'VALIDEE' },
        ],
        totalElements: 2,
      },
      timestamp: new Date().toISOString(),
    });
  }),

  // === Clients ===
  http.get(`${API_BASE}/clients`, () => {
    return HttpResponse.json({
      success: true,
      message: 'Liste des clients',
      data: {
        content: [
          { id: 1, nom: 'Kamga', prenom: 'Jean', telephone: '691234567', email: 'jean@example.com' },
          { id: 2, nom: 'Nkwi', prenom: 'Paul', telephone: '692345678', email: 'paul@example.com' },
        ],
        totalElements: 2,
      },
      timestamp: new Date().toISOString(),
    });
  }),
];

// Setup MSW worker
import { setupWorker } from 'msw/browser';
export const worker = setupWorker(...handlers);
