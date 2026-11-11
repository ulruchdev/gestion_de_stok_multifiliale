# Audit frontend — StockMaster CM

**Date** : 11 novembre 2026 · **Périmètre** : `frontend/` (41 fichiers `src`, ~5 100 lignes TS/TSX) + CI `ci-frontend.yml`
**Méthode** : lecture intégrale de `src/`, exécution réelle de `tsc --noEmit`, `eslint`, `vite build`, `npm audit`, vérifications par greps. Aucune supposition : chaque constat est rattaché à un fichier/ligne ou à une sortie de commande.

> Ce document décrit **l'état constaté**. Les arbitrages de correction restent à trancher (voir « Décisions à prendre »).

---

## 1. Verdict global

Le socle est **sain et cohérent** pour un frontend en début de produit : TypeScript `strict`, lint sans warning, build sans erreur, découpage par `lazy()`, design tokens centralisés, `react-hook-form` + `zod`, TanStack Query, aucun `dangerouslySetInnerHTML`, aucun `console.log`, aucun `any`.

Le périmètre fonctionnel est en revanche **très limité** : seules l'accueil et l'authentification/inscription sont implémentées ; **toutes les pages métier sont des placeholders** (`routes.tsx`), y compris les clients livrés côté backend (`/tiers/clients`). Ce n'est pas un défaut d'audit mais le point n°1 à intégrer au planning : le backend a ~5 EPIC d'avance sur l'UI.

| Axe | Note | Commentaire |
|---|---|---|
| Architecture / organisation | 🟢 Bon | `app/`, `features/`, `shared/ui`, alias `@/`, lazy routes |
| Qualité statique | 🟢 Bon | `tsc` 0 erreur, ESLint 0 warning (`--max-warnings 0`) |
| Sécurité | 🟠 À traiter | 5 vulnérabilités de prod, tokens en `localStorage`, pas de CSP |
| Accessibilité | 🟠 À traiter | focus non piégé dans les modales, contrôles clavier manquants, menu mobile hors écran focusable |
| Robustesse runtime | 🟠 À traiter | pas d'ErrorBoundary, 404 → `/`, refresh token uniquement réactif |
| Tests | 🔴 Absent | 0 test frontend, CI sans étape de test |
| Performance | 🟢 Acceptable | entrée 95 kB gz, chunk téléphone 50 kB gz en lazy |

**Vérifications exécutées**

```
npx tsc --noEmit      → 0 erreur
npm run lint          → 0 erreur, 0 warning
npx vite build        → succès, 2 067 modules, 12,5 s
npm audit --omit=dev  → 5 vulnérabilités (3 high, 2 moderate)
```

Taille du build : `index` 291,6 kB (95,2 kB gz) · `PhoneInput` 202,4 kB (50,3 kB gz, lazy) · CSS 43,3 kB (8,3 kB gz) · total 740 kB.

---

## 2. Constats priorisés

### 🔴 P0 — à traiter avant d'ouvrir les écrans métier

**F1. Cinq vulnérabilités en dépendances de production**

| Paquet | Version | Sévérité | Remède |
|---|---|---|---|
| `axios` | 1.18.1 | high (prototype pollution via adaptateurs) | `npm audit fix` (non cassant) |
| `follow-redirects` | transitif | high (fuite d'en-têtes cross-origin) | `npm audit fix` |
| `react-router` / `react-router-dom` | 6.30.4 | moderate (open redirect via `Link`/`useNavigate`) | montée majeure 7.x à planifier |
| `zod` | 3.25.76 | high (épuisement mémoire sur collections contraintes) | montée majeure 4.x à planifier |

Le risque réel est modéré (aucune navigation construite depuis une entrée utilisateur aujourd'hui), mais `axios` est utilisé pour **toutes** les requêtes authentifiées : le correctif non cassant doit passer tout de suite (`npm audit` complet : 19 vulnérabilités, dont 8 high avec les devDependencies).

**F2. Guards de route incohérents avec le menu — un écran mort pour `ADMIN_FILIALE`**

`DashboardLayout` affiche « Utilisateurs » pour `ADMIN_FILIALE` (`menuStructure`, `roles: [ADMIN_GROUPE, ADMIN_FILIALE]`), alors que `/utilisateurs` est enfermé dans un `RequireAuth roles={[ADMIN_GROUPE]}` (`routes.tsx`). Résultat : clic → redirection silencieuse vers `/dashboard`, sans explication.

Symétriquement, **seules 5 routes** portent un contrôle de rôle (`/groupe/*`, `/transferts*`, `/utilisateurs`) ; `/catalogue/*`, `/tiers/*`, `/achats/*`, `/ventes/*`, `/stock/*` sont accessibles à tout utilisateur authentifié. Le filtrage du menu est donc **cosmétique** : un `CAISSIER` peut ouvrir `/achats/commandes` par URL. Aucune donnée n'est exposée aujourd'hui (placeholders), mais le modèle est à corriger **avant** de brancher les écrans, sinon il faudra repasser sur chaque page.

**F3. Jetons d'authentification en `localStorage`**

`api-client.ts` et `useAuthStore.ts` stockent `access_token`, `refresh_token` et `user` en clair. Toute XSS exfiltre une session de 15 min **et** un refresh token à longue durée. `index.html` ne définit aucune CSP. Deux options à trancher :

1. conserver le stockage (choix SPA simple) et **ajouter une CSP stricte** + interdire tout script tiers ;
2. migrer le refresh token en cookie `HttpOnly`/`SameSite` côté backend (recommandé à terme, impacte `stockmaster-auth`).

À noter aussi : `initialize()` fait foi sur le contenu de `localStorage.user` (rôle inclus) et ne vérifie jamais l'expiration du jeton → un rôle obsolète pilote le menu jusqu'au premier 401.

**F4. Aucun test, aucune étape de test en CI**

`package.json` n'expose pas de script `test` ; `ci-frontend.yml` se limite à `install` → `lint` → `typecheck` → `build`. Les zones les plus risquées sont précisément celles qui ne sont pas testées : `api-client` (file d'attente de refresh, rotation), `useAuthStore`, `RequireAuth`. Un socle Vitest + Testing Library (aucune dépendance ajoutée au runtime) couvrirait ces trois-là en une dizaine de tests.

### 🟠 P1 — qualité perçue et accessibilité

**F5. Accessibilité des composants partagés** (impacte tous les écrans à venir)

| Constat | Fichier | Détail |
|---|---|---|
| Modale sans piège de focus, sans focus initial, sans restitution du focus | `Modal.tsx` | `role="dialog"` correct, mais `Tab` sort de la modale ; `aria-labelledby="modal-title"` est un id fixe (collision si deux modales) |
| Bouton œil du mot de passe inaccessible au clavier | `Input.tsx` | `tabIndex={-1}` sur le `trailingIcon` ; `aria-label` conditionné à la présence du handler, identique pour toute icône |
| Bouton de fermeture des toasts sans nom accessible | `Toast.tsx` | icône `X` seule |
| `aria-sort` absent sur les colonnes triables | `DataTable.tsx` | bouton de tri correct, mais l'état n'est pas annoncé |
| Onglets sans navigation flèches/Home/End | `Tabs.tsx` | `tabIndex` glissant présent (`-1` sur inactifs) sans gestionnaire clavier → onglets inatteignables au clavier |
| Barre latérale hors écran mais focusable | `DashboardLayout.tsx` | `-translate-x-full` sans `inert`/`aria-hidden` : le `Tab` mobile entre dans un menu invisible ; boutons hamburger/fermeture sans `aria-label`, ni `aria-expanded` |
| Pas de lien d'évitement | `DashboardLayout.tsx` | pas de « Aller au contenu » |

Points positifs à conserver : `lang="fr"`, `aria-invalid`/`aria-describedby` sur `Input`, `role="alert"` sur les erreurs de champ, `autoComplete` correct sur le login, focus visible stylé globalement.

**F6. Robustesse runtime**

- **Aucun ErrorBoundary** : une exception de rendu laisse un écran blanc.
- **404 silencieuse** : `<Route path="*" element={<Navigate to="/" replace />} />` — pas de page 404, l'utilisateur ne comprend pas l'URL erronée.
- **Refresh réactif uniquement** : pas de rafraîchissement proactif à partir de `expiresIn` ; sur échec, `window.location.href = '/login'` (rechargement complet, état Zustand non réinitialisé dans l'onglet courant).
- **Pas de titre de page par route** (`document.title` statique).
- **Code mort** : `getFieldErrors()` et `refreshSession()` ne sont appelés nulle part ; `OfflineBanner` est exporté mais **jamais monté** (`shared/ui/index.ts` seulement).

**F7. `OfflineBanner` défectueux s'il est activé**

Deux défauts cumulés : l'endpoint sondé est `/api/v1/actuator/health` alors que le backend autorise `/actuator/health` (préfixe `/api/v1` en trop → 404 interprété comme « hors ligne »), et la prop `offlineTimeout` n'est jamais utilisée. À corriger avant montage, ou supprimer le composant.

**F8. `public/` absent alors que `index.html` et MSW l'attendent**

`<link rel="icon" href="/favicon.svg">` pointe vers un fichier inexistant (404 en prod), et `VITE_ENABLE_MOCKS=true` échoue sur un clone neuf : `public/mockServiceWorker.js` est gitignoré et aucune commande `msw init` n'est documentée.

### 🟡 P2 — dette et finitions

**F9. Double source de tokens de design.** Les couleurs vivent à la fois en variables CSS (`--brand-primary`, thème clair/sombre) et en hex figés dans `tailwind.config.ts` (`brand.accent: '#533afd'`). Conséquence concrète : les classes `bg-brand-*` **ne suivent pas le mode sombre**, contrairement à `bg-[var(--brand-primary)]`. La police `SF Pro Display` est déclarée mais jamais chargée → rendu différent sur Android/Windows (majorité des utilisateurs camerounais).

**F10. CI perfectible** : `npm install` au lieu de `npm ci` (build non reproductible malgré `package-lock.json`), pas d'étape `npm audit`, Node 20 uniquement. `eslint@8` est en fin de vie, `vite@5` a deux majeures de retard — à planifier, sans urgence.

**F11. Doublons de titres** : `ForgotPasswordPage` et `ResetPasswordPage` rendent deux `<h1>` chacune.

**F12. Prérequis produits non posés** : pas de formatage monétaire XAF centralisé, pas d'i18n, pas de stratégie PWA/cache alors que le contexte projet (coupures réseau, mobile) est explicite. À cadrer **avant** les écrans de vente/stock, pas après.

---

## 3. Ce qui est déjà bien fait (à ne pas casser)

- Découpage `app / features / shared` avec alias `@/` ; UI kit maison cohérent (`Button` cva, `Input`, `Card`, `DataTable`, `Modal`, `Tabs`).
- Routes `lazy()` + `Suspense` : une page métier lourde n'alourdira pas le premier écran.
- `react-query` configuré globalement (`retry: 1`, `staleTime` 5 min, pas de refetch au focus).
- Validation formulaire déclarative (`zod` + `react-hook-form`), messages d'erreur français cohérents.
- Intercepteur 401 avec **file d'attente** et **rotation du refresh token** (GS-083) correctement implémentés.
- Aucune injection HTML, aucun secret en dur, `.env.example` fourni, `dist/` gitignoré.

---

## 4. Plan de correction proposé (ordre d'exécution)

| # | Action | Effort | Débloque |
|---|---|---|---|
| 1 | `npm audit fix` (axios, follow-redirects) + vérifier le build | XS | F1 |
| 2 | Aligner menu ↔ guards, ajouter `RequireAuth roles` sur les 5 familles de routes métier, corriger `/utilisateurs` | S | F2 |
| 3 | Ajouter CSP dans `index.html` + trancher le stockage des jetons | S/M | F3 |
| 4 | Socle Vitest + 10-15 tests (`api-client`, `useAuthStore`, `RequireAuth`) et étape CI | M | F4 |
| 5 | Corriger l'UI kit a11y (modale, œil, toasts, `aria-sort`, flèches onglets, sidebar `inert`) | M | F5 |
| 6 | ErrorBoundary + page 404 + `document.title` par route | S | F6 |
| 7 | Décider du sort d'`OfflineBanner` (corriger l'endpoint ou supprimer) et créer `public/favicon.svg` | XS | F7/F8 |
| 8 | Unifier les tokens (Tailwind piloté par variables CSS), charger une police ou retirer `SF Pro Display` | S | F9 |
| 9 | `npm ci` + `npm audit` en CI ; plan de montée react-router 7 / zod 4 | S/M | F10 |

---

## 5. Décisions à prendre (utilisateur)

1. **Jetons** : on garde `localStorage` + CSP, ou on migre le refresh token en cookie `HttpOnly` (impact backend) ?
2. **`OfflineBanner`** : composant à terminer (endpoint `/actuator/health`, montage dans `DashboardLayout`) ou à supprimer ?
3. **Montées majeures** (`react-router` 7, `zod` 4, `vite` 7) : maintenant, ou après le premier écran métier ?
4. **Tests frontend** : socle Vitest maintenant (recommandé), ou reporté à la première feature UI ?
