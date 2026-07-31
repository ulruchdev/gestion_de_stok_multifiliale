# Backlog Frontend — StockMaster CM
### Référence : GS-FRONTEND-BACKLOG-2026-01 | Version : 1.1 | Date : Juillet 2026 | Statut : Proposé
### Documents parents : GS-DESIGN-BACKLOG-2026-01 (maquettes), BACKLOG_StockMaster_CM.md (endpoints backend), GS-IA-2026-01 (navigation), GS-SEQ-2026-01 (séquences multi-écrans)

---

## 1. Objet et portée

`BACKLOG_StockMaster_CM.md` est entièrement backend (75 US, endpoints, DTOs). Aucune US ne couvre les écrans, le routing, la gestion d'état ou les composants côté client. Ce document comble ce trou avec le même formalisme (US + critères d'acceptation + story points + priorité), scopé cette fois aux livrables frontend.

**Règle de dépendance dure :** aucune US frontend ne démarre avant que la maquette Figma correspondante (`GS-DESIGN-BACKLOG-2026-01`) soit au statut "Validée" **et** que l'endpoint backend correspondant soit livré et documenté dans `test_postman.md`. Le séquencement précis est formalisé dans `GS-RACI-2026-01_matrice_livraison.md`.

**Convention de nommage :** `US-F0XX` (F = Frontend).

---

## 2. Hypothèse de stack technique

> ⚠️ Aucun stack frontend n'est actuellement spécifié dans le CDCT ou l'analyse fonctionnelle — celui-ci ne couvre que le backend (Java 21 / Spring Boot / Spring Modulith). Ce backlog **suppose** un stack par défaut cohérent avec l'écosystème déjà référencé côté design (shadcn/ui, recharts, lucide-react) :

| Couche | Choix par défaut | À confirmer |
|---|---|---|
| Framework | React 18 + TypeScript | ⚠️ Vue/Angular non exclus — à trancher formellement en ADR avant Sprint F0 |
| Build | Vite | — |
| Routing | React Router v6 | — |
| État serveur | TanStack Query (cache API, invalidation) | — |
| État client | Zustand (état UI léger : session, filtres) | — |
| UI Kit | Tailwind CSS + shadcn/ui | Doit correspondre 1:1 aux composants Figma (US-D003) |
| Formulaires | React Hook Form + Zod (validation miroir du backend) | — |
| Requêtes HTTP | Axios avec intercepteur JWT (refresh automatique) | — |
| Graphiques | Recharts | — |
| PWA / offline | Service Worker avec cache-first sur lecture, queue de requêtes en attente sur écriture | Répond à la contrainte "coupures réseau fréquentes" (GS-CDA-2026-01 §1.4) — **à valider en ADR, impact architecture significatif** |

**Action de suivi :** rédiger un ADR frontend dédié (`ADR-F01-choix-stack-frontend.md`) avant le Sprint F0 — ce backlog ne doit pas être interprété comme figeant ce choix.

---

## 3. EPIC-F00 — Fondations techniques

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-F001 | Initialisation du repo frontend (Vite + TS + linter + Prettier + Husky pre-commit) | 3 | **P0** | Repo séparé du monorepo backend ou sous-dossier dédié, décision alignée avec DevOps (GS-RACI) ; CI lint+build sur chaque PR |
| US-F002 | Mise en place du design system technique (tokens Tailwind générés depuis US-D001/D002/D005) | 5 | **P0** | Les tokens couleurs/typo/spacing du fichier Tailwind config correspondent exactement aux valeurs définies dans le design system Figma |
| US-F003 | Bibliothèque de composants partagés (Button, Input, Table, Badge, Modal, Toast) | 8 | **P0** | Chaque composant a une story de test visuel ; correspond 1:1 aux variantes définies en US-D003 |
| US-F004 | Client API centralisé (Axios + intercepteur JWT + refresh token automatique + gestion erreurs uniforme) | 5 | **P0** | Refresh transparent sans déconnexion utilisateur (AUTH-04) ; erreurs 4xx/5xx mappées vers un format d'erreur unique consommé par le composant Toast |
| US-F005 | Routing applicatif + garde de route par rôle (RBAC frontend miroir du backend) | 5 | **P0** | Un utilisateur non autorisé est redirigé avant tout appel API — reflète strictement GS-IA-2026-01 §9, jamais une source de vérité indépendante |
| US-F006 | Gestion de l'état d'authentification (session, rôle, entreprise_id courant) | 3 | **P0** | Token stocké de façon sécurisée (httpOnly cookie recommandé plutôt que localStorage) ; état de session partagé entre onglets |
| US-F007 | Layout applicatif par rôle (sidebar/menu dynamique selon GS-IA-2026-01) | 5 | **P0** | Le menu affiché correspond exactement à l'arborescence du rôle connecté (§1-6 de GS-IA-2026-01) |
| US-F008 | Gestion des états réseau (hors ligne, requête en attente, erreur de reconnexion) | 5 | P1 | Répond à US-D007 ; queue de mutation en cas de coupure réseau avec retry automatique |
| US-F009 | Internationalisation (fr par défaut, structure prête pour en) | 2 | P2 | Toutes les chaînes passent par un fichier de traduction, aucun texte en dur dans les composants |

**Sous-total EPIC-F00 : 41 SP**

---

## 4. EPIC-F01 — Onboarding public

*Écrans : US-D010 à US-D015 · Endpoints : AUTH-00, AUTH-01a, AUTH-01b, AUTH-02, AUTH-05*

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-F010 | Page d'accueil publique (statique) | 2 | **P0** | Correspond à US-D010 ; performance : LCP < 2.5s sur connexion 3G (contrainte réseau camerounaise) |
| US-F011 | Écran de choix du type d'inscription | 2 | **P0** | Correspond à US-D011 ; state routing vers formulaire A ou B |
| US-F012 | Formulaire Entreprise Unique + intégration `POST /api/v1/auth/inscription/entreprise-unique` | 5 | **P0** | Validation Zod miroir des contraintes serveur (§8.5 GS-CDA-2026-01) ; gestion de l'erreur 409 (email déjà utilisé) |
| US-F013 | Formulaire Groupe multi-sites + intégration `POST /api/v1/auth/inscription/groupe` | 5 | **P0** | Idem, 2 blocs (US-D013) |
| US-F014 | Écran de connexion + intégration `POST /api/v1/auth/login` | 3 | **P0** | Gestion des erreurs 401 avec message explicite, sans révéler si c'est l'email ou le mot de passe qui est invalide (sécurité) |
| US-F015 | Flow mot de passe oublié + réinitialisation (`POST /api/v1/auth/forgot-password`, `POST /api/v1/auth/reset-password`) | 5 | **P0** | Gestion du lien à expiration 1h (NOT-03) avec message clair si expiré |
| US-F016 | Écran d'activation de compte (lien email) | 2 | P1 | — |

**Sous-total EPIC-F01 : 24 SP**

---

## 5. EPIC-F02 — Dashboards

*Écrans : US-D020 à US-D023*

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-F020 | Dashboard Admin Groupe + intégration endpoint consolidé (GRP-06) | 8 | **P0** | Chargement progressif des widgets (skeleton loaders), pas de blocage total de l'écran si un widget échoue |
| US-F021 | Dashboard Admin Filiale | 5 | **P0** | Réutilise les composants de F020 sans duplication de logique |
| US-F022 | Dashboard Gestionnaire Stock / Caissier | 3 | P1 | — |
| US-F023 | Composant graphique CA (Recharts, intégration STAT-03) | 3 | P1 | Réactif au filtre de période (jour/mois) sans rechargement de page complet |

**Sous-total EPIC-F02 : 19 SP**

---

## 6. EPIC-F03 — Catalogue (Articles & Catégories)

*Écrans : US-D030 à US-D034 · Endpoints : CAT-01→05, ART-01→07*

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-F030 | Liste des articles (recherche, filtres, pagination) + intégration `GET /articles` | 5 | **P0** | Pagination serveur (pas de chargement de la liste complète) — respecte §8.1 (max 100/page) |
| US-F031 | Formulaire création/édition article (prix HT/TVA/TTC calculé) | 8 | **P0** | Le TTC affiché est **recalculé côté client pour l'aperçu uniquement** — la valeur persistée provient exclusivement de la réponse serveur (§8.4 : calcul jamais fait côté frontend pour la source de vérité) |
| US-F032 | Fiche détail article (stock temps réel, historique, badge alerte) | 5 | **P0** | Polling ou websocket léger si le stock doit refléter des mouvements concurrents en temps réel — sinon refetch au focus de la fenêtre |
| US-F033 | Gestion des catégories (CRUD + TVA par défaut) | 3 | **P0** | Message de blocage explicite si suppression refusée par le backend (catégorie non vide) |
| US-F034 | Upload photo article (intégration stockage cloud) | 3 | P2 | Prévisualisation avant upload, compression côté client si fichier volumineux |

**Sous-total EPIC-F03 : 24 SP**

---

## 7. EPIC-F04 — Tiers (Clients & Fournisseurs)

*Écrans : US-D040 à US-D042*

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-F040 | Liste + fiche client (avec historique commandes, CLI-05) | 5 | **P0** | Réutilise le composant de liste paginée de US-F030 |
| US-F041 | Liste + fiche fournisseur | 5 | **P0** | Idem |
| US-F042 | Formulaires création/édition client et fournisseur | 5 | **P0** | Champ adresse structuré (quartier/ville/région) |

**Sous-total EPIC-F04 : 15 SP**

---

## 8. EPIC-F05 — Commandes Fournisseur (Achats)

*Écrans : US-D050 à US-D053 · Séquence de référence : GS-SEQ-2026-01 §2*

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-F050 | Création de commande fournisseur (multi-lignes dynamiques) | 8 | **P0** | Ajout/suppression de ligne sans rechargement ; total recalculé en temps réel côté client (aperçu uniquement) |
| US-F051 | Liste des commandes fournisseur avec filtres par état | 3 | **P0** | Filtres synchronisés avec l'URL (partage de lien de recherche possible) |
| US-F052 | Écran de validation de commande (avec confirmation) | 3 | **P0** | Modal de confirmation avant appel `POST /commandes-fournisseur/{id}/valider` — action irréversible (GS-SEQ-2026-01 §2) |
| US-F053 | Détail commande + gestion des états EN_PREPARATION/VALIDEE/LIVREE | 3 | **P0** | Les actions disponibles (modifier/supprimer/valider) sont strictement conditionnées par l'état, désactivées visuellement sinon |

**Sous-total EPIC-F05 : 17 SP**

---

## 9. EPIC-F06 — Commandes Client & Vente Directe

*Écrans : US-D060 à US-D065 · Séquence de référence : GS-SEQ-2026-01 §3*

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-F060 | Création de commande client (multi-lignes) | 8 | **P0** | Réutilise le composant de saisie multi-lignes de US-F050 |
| US-F061 | Gestion de l'erreur "stock insuffisant" à la validation | 5 | **P0** | Affiche par article la quantité demandée vs disponible, retournée par le backend (422 avec `articlesEnRupture[]` — GS-SEQ-2026-01 §3) ; **aucun recalcul de disponibilité côté client** |
| US-F062 | Génération et téléchargement de facture PDF | 3 | P1 | Ouvre/télécharge le fichier retourné par `GET /commandes-client/{id}/facture` |
| US-F063 | Écran caisse / vente directe (recherche rapide, saisie tactile) | 8 | **P0** | Recherche d'article utilisable en < 2 interactions tactiles ; fonctionne en mode dégradé si latence réseau élevée |
| US-F064 | Ticket de caisse (impression navigateur + partage) | 3 | P1 | Format imprimable A4/thermique ; bouton de partage (lien ou image) |
| US-F065 | Écran d'annulation de vente | 3 | P1 | Uniquement actif le jour même avant clôture caisse (VNT-02) — désactivé visuellement sinon, pas juste bloqué en silence |

**Sous-total EPIC-F06 : 30 SP**

---

## 10. EPIC-F07 — Stock (mouvements, corrections, transferts)

*Écrans : US-D070 à US-D074 · Séquence de référence : GS-SEQ-2026-01 §4*

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-F070 | Historique des mouvements de stock (table filtrable) | 5 | **P0** | Filtres : type, date, article (§8.2) ; export non requis en V1 |
| US-F071 | Formulaire de correction manuelle (motif obligatoire) | 3 | **P0** | Champ motif requis côté client ET revalidé côté serveur — pas de contournement possible en désactivant le JS |
| US-F072 | Écran de transfert inter-filiales avec stock temps réel | 8 | P1 | Affiche le stock disponible de la filiale source via un appel dédié avant soumission (GS-SEQ-2026-01 §4) |
| US-F073 | Consultation du bon de transfert | 2 | P1 | — |
| US-F074 | Inventaire guidé | 5 | P2 | Hors périmètre V1 |

**Sous-total EPIC-F07 : 23 SP**

---

## 11. EPIC-F08 — Groupe / Filiales / Utilisateurs

*Écrans : US-D080 à US-D083*

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-F080 | Liste et création de filiale | 5 | **P0** | Réservé Admin Groupe (garde de route US-F005) |
| US-F081 | Liste et création d'utilisateur avec sélection de rôle | 5 | **P0** | Le sélecteur de rôle n'affiche que les rôles que l'utilisateur connecté a le droit d'attribuer (Admin Groupe ne crée pas d'Admin Filiale d'une autre filiale, etc.) |
| US-F082 | Paramètres groupe (logo, devise, infos fiscales) | 3 | P1 | — |
| US-F083 | Profil utilisateur (édition infos, mot de passe) | 3 | P1 | — |
| US-F110 | Composant `Logo` dynamique personnalisable par entreprise | 3 | P1 | Le composant `Logo.tsx` (aujourd'hui statique : icône Warehouse + texte "StockMaster") lit le branding depuis le store/contexte entreprise (`logo` + `titre`) et affiche le logo uploadé s'il existe, sinon le fallback StockMaster — appliqué partout (AuthLayout, DashboardLayout, pages auth, sidebar) |
| US-F111 | Page Paramètres entreprise (branding : logo + titre) + intégration `PATCH /api/v1/entreprises/{id}/branding` | 3 | P1 | Formulaire avec upload logo (prévisualisation, validation type PNG/JPG/SVG, max 2 Mo) + champ titre ; toast succès/erreur (US-F003) ; correspond à US-D110 et US-081 |
| US-F112 | Gestion des erreurs 409 d'unicité par champ dans les formulaires d'inscription | 3 | **P0** | L'erreur 409 (NIF/téléphone/email/nom déjà utilisé) s'affiche en toast global (App.tsx) ET inline sur le champ concerné via `getFieldErrors` — complète US-F012/US-F013 qui ne géraient que l'email |

**Sous-total EPIC-F08 : 25 SP**

---

## 12. EPIC-F09 — Statistiques & Reporting

*Écrans : US-D090 à US-D093*

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-F090 | Top articles vendus / clients fidèles | 3 | P1 | — |
| US-F091 | Alertes de rupture imminente (liste dédiée) | 3 | P1 | Distinct du badge d'alerte ponctuel sur fiche article |
| US-F092 | Comparaison inter-filiales | 5 | P2 | — |
| US-F093 | Export CSV/PDF | 2 | P2 | Déclenche le téléchargement du fichier retourné par le backend, aucune génération côté client |

**Sous-total EPIC-F09 : 13 SP**

---

## 13. EPIC-F10 — Back-office Super Admin

*Écrans : US-D100 à US-D102 — application distincte, thème visuel séparé*

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-F100 | Application/sous-domaine back-office distinct (build séparé ou route isolée avec layout dédié) | 5 | P1 | Aucun composant partagé avec l'espace client qui exposerait accidentellement des données commerciales |
| US-F101 | Liste des tenants/groupes (activer/suspendre/supprimer) | 5 | P1 | — |
| US-F102 | Métriques plateforme | 3 | P1 | — |
| US-F103 | Alertes techniques | 2 | P1 | — |

**Sous-total EPIC-F10 : 15 SP**

---

## 14. Récapitulatif

| EPIC | SP total | Priorité dominante | Dépend de (design) |
|---|---|---|---|
| EPIC-F00 — Fondations techniques | 41 | P0 | EPIC-D00 |
| EPIC-F01 — Onboarding public | 24 | P0 | EPIC-D01 |
| EPIC-F02 — Dashboards | 19 | P0/P1 | EPIC-D02 |
| EPIC-F03 — Catalogue | 24 | P0 | EPIC-D03 |
| EPIC-F04 — Tiers | 15 | P0 | EPIC-D04 |
| EPIC-F05 — Commandes fournisseur | 17 | P0 | EPIC-D05 |
| EPIC-F06 — Commandes client & Caisse | 30 | P0 | EPIC-D06 |
| EPIC-F07 — Stock | 23 | P0/P1 | EPIC-D07 |
| EPIC-F08 — Groupe/Filiales/Utilisateurs | 25 | P0/P1 | EPIC-D08 |
| EPIC-F09 — Statistiques | 13 | P1/P2 | EPIC-D09 |
| EPIC-F10 — Back-office Super Admin | 15 | P1 | EPIC-D10 |
| **Total** | **246 SP** | | |

---

## 15. Composants partagés transverses (à ne pas re-livrer par écran)

| Composant | Réutilisé dans | Livré par |
|---|---|---|
| Table paginée générique (recherche + filtres + tri) | F03, F04, F05, F06, F07, F09 | US-F003 / composant unique — toute nouvelle liste doit le réutiliser, pas en recréer un |
| Formulaire multi-lignes (article + quantité + PU) | F05, F06 | US-F050 — F060 réutilise le même composant |
| Badge d'état de commande | F05, F06 | Miroir exact de US-D004 |
| Modal de confirmation d'action irréversible | F05 (validation), F06 (validation, annulation) | US-F003 |

---

## 16. Points de vigilance techniques

- **Double calcul TVA/TTC (§8.4) :** le frontend peut afficher un aperçu calculé localement pour l'UX (retour instantané pendant la saisie), mais **la valeur persistée et facturée provient exclusivement de la réponse serveur**. Ne jamais soumettre un TTC calculé côté client comme valeur de vérité.
- **Vérification de stock (UC-03) :** même règle — le frontend ne doit jamais décider localement qu'un stock est suffisant ; il affiche le résultat retourné par le backend (GS-SEQ-2026-01 §3). Un affichage optimiste de "stock disponible" à titre informatif est acceptable, mais la validation finale reste serveur.
- **RBAC frontend = reflet, jamais source de vérité** (rappel de GS-IA-2026-01 §9) — chaque garde de route (US-F005) doit avoir son pendant `@PreAuthorize` vérifié côté backend ; sinon c'est une faille de sécurité présentée comme une fonctionnalité UX.
- **Mode dégradé réseau** (US-F008) : à cadrer précisément avec le backend avant implémentation — nécessite de savoir si une API d'écriture supporte l'idempotence (rejouer une requête après reconnexion sans dupliquer une vente).

---

## 17. Prochaine étape

Séquencer ces EPICs contre les EPICs backend et design dans `GS-RACI-2026-01_matrice_livraison.md`, en particulier pour déterminer si le frontend peut démarrer en parallèle du backend sur la base de contrats d'API mockés (recommandé) ou doit attendre la livraison réelle de chaque endpoint (plus lent, non recommandé compte tenu du volume de 237 SP à absorber).
