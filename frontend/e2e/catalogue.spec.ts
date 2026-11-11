import { expect, request as apiRequest, test, type Page } from '@playwright/test';

/**
 * US-F030/F032/F033 — parcours catalogue sur backend Spring Boot réel.
 *
 * Chaque exécution inscrit une entreprise jetable, crée son référentiel via
 * l'API (1 catégorie, 23 articles, 1 caissier) puis pilote l'UI. Aucun mock :
 * le stock affiché (`0` / `RUPTURE`) vient du module stock.
 */

const API = 'http://localhost:8080/api/v1';
const MOT_DE_PASSE = 'E2e@2026!';
const SUFFIXE = Date.now().toString(36);

interface Session {
  token: string;
  utilisateur: Record<string, unknown>;
}

let admin: Session;
let caissier: Session;
let articleRizId: number;

async function appel(
  chemin: string,
  options: { method?: string; body?: unknown; token?: string } = {},
) {
  const contexte = await apiRequest.newContext();
  const reponse = await contexte.fetch(`${API}${chemin}`, {
    method: options.method ?? 'GET',
    data: options.body,
    headers: options.token
      ? { Authorization: `Bearer ${options.token}` }
      : undefined,
  });
  const corps = await reponse.json();
  await contexte.dispose();
  return { statut: reponse.status(), corps };
}

async function creerArticle(
  token: string,
  categorieId: number,
  codeArticle: string,
  designation: string,
) {
  const { statut, corps } = await appel('/articles', {
    method: 'POST',
    token,
    body: {
      codeArticle,
      designation,
      categorieId,
      prixAchatHt: 15000,
      prixVenteHt: 18500,
      tauxTva: 19.25,
      seuilAlerte: 10,
    },
  });
  expect(statut, JSON.stringify(corps)).toBe(201);
  return corps.data.id as number;
}

async function connecter(page: Page, session: Session) {
  await page.addInitScript((donnees) => {
    localStorage.setItem('access_token', donnees.token);
    localStorage.setItem('user', donnees.utilisateur);
  }, { token: session.token, utilisateur: JSON.stringify(session.utilisateur) });
}

test.beforeAll(async () => {
  const email = `e2e-catalogue-${SUFFIXE}@stockmaster.test`;

  const inscription = await appel('/auth/inscription/entreprise-unique', {
    method: 'POST',
    body: {
      nomEntreprise: `E2E Catalogue ${SUFFIXE}`,
      email,
      telephone: `+2376${Math.floor(10000000 + Math.random() * 89999999)}`,
      adminNom: 'Admin',
      adminPrenom: 'E2E',
      motDePasse: MOT_DE_PASSE,
    },
  });
  expect(inscription.statut, JSON.stringify(inscription.corps)).toBe(201);

  const login = await appel('/auth/login', {
    method: 'POST',
    body: { email, motDePasse: MOT_DE_PASSE },
  });
  expect(login.statut).toBe(200);
  admin = {
    token: login.corps.data.accessToken,
    utilisateur: login.corps.data.utilisateur,
  };

  const categorie = await appel('/categories', {
    method: 'POST',
    token: admin.token,
    body: { code: `CERE${SUFFIXE.slice(-4).toUpperCase()}`, designation: 'Céréales', tauxTva: 19.25 },
  });
  expect(categorie.statut, JSON.stringify(categorie.corps)).toBe(201);

  for (let i = 1; i <= 22; i += 1) {
    await creerArticle(
      admin.token,
      categorie.corps.data.id,
      `ART${String(i).padStart(2, '0')}${SUFFIXE.slice(-3).toUpperCase()}`,
      `Article ${String(i).padStart(2, '0')}`,
    );
  }
  articleRizId = await creerArticle(
    admin.token,
    categorie.corps.data.id,
    `RIZ50KG${SUFFIXE.slice(-3).toUpperCase()}`,
    'Riz 50 kg',
  );

  const employe = await appel('/utilisateurs/employes', {
    method: 'POST',
    token: admin.token,
    body: {
      prenom: 'Caisse',
      nom: 'E2E',
      email: `e2e-caissier-${SUFFIXE}@stockmaster.test`,
      role: 'CAISSIER',
      motDePasseProvisoire: MOT_DE_PASSE,
    },
  });
  expect(employe.statut, JSON.stringify(employe.corps)).toBe(201);

  const loginCaissier = await appel('/auth/login', {
    method: 'POST',
    body: {
      email: `e2e-caissier-${SUFFIXE}@stockmaster.test`,
      motDePasse: MOT_DE_PASSE,
    },
  });
  expect(loginCaissier.statut, JSON.stringify(loginCaissier.corps)).toBe(200);
  caissier = {
    token: loginCaissier.corps.data.accessToken,
    utilisateur: loginCaissier.corps.data.utilisateur,
  };
});

test('liste réelle : article, catégorie, prix TTC et alerte stock', async ({ page }) => {
  await connecter(page, admin);
  await page.goto('/catalogue/articles?q=Riz');

  await expect(page.getByRole('heading', { name: 'Articles' })).toBeVisible();
  const ligne = page.getByRole('row', { name: /Riz 50 kg/ });
  await expect(ligne).toBeVisible();
  await expect(ligne.getByText('Céréales')).toBeVisible();
  await expect(ligne.getByText('22 061 FCFA')).toBeVisible();
  await expect(ligne.getByText('Rupture')).toBeVisible();
  await expect(ligne.getByText('0', { exact: true })).toBeVisible();
});

test('recherche : filtre serveur puis aucun résultat', async ({ page }) => {
  await connecter(page, admin);
  await page.goto('/catalogue/articles');

  const recherche = page.getByPlaceholder(
    'Rechercher un article (code ou désignation)',
  );
  await recherche.fill('Riz');
  await expect(page.getByRole('row', { name: /Riz 50 kg/ })).toBeVisible();
  await expect(page).toHaveURL(/q=Riz/);

  await recherche.fill('introuvable-xyz');
  await expect(page.getByText('Aucun résultat')).toBeVisible();
  await expect(page.getByText('Aucun élément ne correspond à "introuvable-xyz"')).toBeVisible();
});

test('pagination : page 2 pilotée par l’URL', async ({ page }) => {
  await connecter(page, admin);
  await page.goto('/catalogue/articles');

  await expect(page.getByText('Page 1 sur 2')).toBeVisible();
  await page.getByRole('button', { name: 'Page 2' }).click();

  await expect(page).toHaveURL(/page=2/);
  await expect(page.getByText('Page 2 sur 2')).toBeVisible();
  await expect(page.getByRole('row', { name: /Riz 50 kg/ })).toBeVisible();
});

test('états de chargement et d’erreur, puis réessai', async ({ page }) => {
  await connecter(page, admin);

  // Chargement : la fiche retarde la réponse, le squelette est annoncé.
  await page.route(`**/api/v1/articles/${articleRizId}`, async (route) => {
    await new Promise((resolve) => setTimeout(resolve, 800));
    await route.continue();
  });
  await page.goto(`/catalogue/articles/${articleRizId}`);
  await expect(page.locator('[aria-busy="true"]')).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Riz 50 kg' })).toBeVisible();
  await page.unroute(`**/api/v1/articles/${articleRizId}`);

  // Erreur réseau sur la liste, puis réessai réussi.
  await page.route('**/api/v1/articles?**', (route) => route.abort('failed'));
  await page.goto('/catalogue/articles');
  await expect(page.getByText('Impossible de charger les articles')).toBeVisible();

  await page.unroute('**/api/v1/articles?**');
  await page.getByRole('button', { name: 'Réessayer' }).click();
  await expect(page.getByRole('row', { name: /Article 01/ })).toBeVisible();
});

test('refus par rôle : le caissier est renvoyé au tableau de bord', async ({ page }) => {
  await connecter(page, caissier);
  await page.goto('/catalogue/categories');

  // Guard frontend (ROLES_PAR_SECTION.catalogue) : redirection immédiate…
  await expect(page).toHaveURL(/\/dashboard$/);
  // …et la section Catalogue reste absente du menu.
  await expect(
    page.getByRole('navigation', { name: 'Navigation principale' }).getByRole('link', { name: 'Catalogue' }),
  ).toHaveCount(0);
});
