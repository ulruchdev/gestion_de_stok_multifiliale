# Lot reporting — découpage technique F090 / F092 / F093

> Rédigé le 18 novembre 2026, sur `main` = `c9762ec`.
> Sources : `document/02-backlogs/GS-FRONTEND-BACKLOG-2026-01.md` §12 (EPIC-F09), issues #1225 / #1227 / #1228,
> `knowledge.md`, `document/03-pilotage/A_JIRA_ET_GIT_FLOW.md`.
> Les tickets GitHub correspondants sont listés en fin de document (§6).

---

## 1. État des lieux (vérifié dans le code, pas dans le backlog)

| Brique | Réalité du dépôt | Conséquence |
|---|---|---|
| `stockmaster-reporting` | un seul fichier : `CalculCaService` (+ son test), pur calcul HT/TVA/TTC (`DEC-020`, `DEC-003`) | aucun endpoint, aucune lecture de données, aucun accès BD |
| `stockmaster-vente` | entités `Vente`/`LigneVente`/`Client` (via `tiers`) + repositories, **pas de service ni de controller** ; US-064 « Enregistrer une vente directe » est **ouverte** (#1142) | pas de données de vente à agréger aujourd'hui |
| ArchUnit règle 1 (`ModuleDependencyArchTest`) | contrats autorisés : `catalogue`, `tiers`, `notification`, `stock` (`port..`, entités/enums/events). **Aucun `vente..`** | `reporting` **ne peut pas** lire `vente.repository` : il faut un port de lecture côté `vente` |
| Frontend | `recharts` déjà installé ; `@tanstack/react-query`, `react-router-dom` ; aucun `papaparse`/`jspdf`/`file-saver` | les graphiques sont couverts ; l'export doit venir **du backend** (AC F093), donc pas de dépendance PDF/CSV à ajouter |
| `GET /api/v1/stock?statutAlerte=` | existe (US-051) | F091 n'a besoin d'aucun backend — hors de ce lot |

Deux dépendances structurent donc tout le lot :

1. **Sans ventes enregistrées, F090 et F092 n'ont rien à afficher.** Les écrans doivent gérer l'état vide, mais la valeur métier n'arrive qu'avec #1142 (US-064). Le lot peut être développé et testé avec des jeux de données injectés, pas livré « utile » avant US-064.
2. **`reporting` ne peut lire `vente` que via un contrat.** Sans port de lecture, la seule alternative serait un `nativeQuery`/JDBC dans `reporting`, ce qui contourne l'architecture et sera refusé par la règle ArchUnit.

---

## 2. Décision d'architecture : port de lecture des ventes

Symétrique de `stock.port.EcritureStockPort` (#1240, clôturé) :

```java
package com.stockmaster.vente.port;

/** Lecture reporting des ventes — seul point d'entrée de `reporting` vers `vente`. */
public interface LectureVentePort {
    List<LigneVenteAgregat> lignesVendues(Long entrepriseId, Instant debut, Instant fin);
    List<LigneVenteAgregat> lignesVenduesGroupe(List<Long> entrepriseIds, Instant debut, Instant fin);

    record LigneVenteAgregat(
        Long entrepriseId,
        Long articleId,
        String articleLibelle,
        Long clientId,
        String clientNom,
        BigDecimal quantite,
        int prixUnitaireHt,   // XAF entier
        BigDecimal tauxTva,
        Instant dateVente
    ) {}
}
```

- Implémentation `LectureVenteService` dans `vente.service`, alimentée par `LigneVenteRepository` (jointures `ligne_vente → vente → client`, filtre `vente.statut = VALIDEE`, `entreprise_id` du JWT côté filiale).
- **Whitelist ArchUnit** : ajouter `com.stockmaster.vente.port..` et `com.stockmaster.vente.domain.enums..` à `CONTRACT_PACKAGES`, avec le commentaire renvoyant à ce document. Le test existant doit rester vert **sans** élargir aux `vente.repository`/`vente.service`.
- Le port ne renvoie que des agrégats de lignes : aucun DTO d'écran, aucune logique de présentation.

---

## 3. Contrats HTTP proposés

Conventions du dépôt : `ApiResponse<T>` / `PageResponse<T>`, tenant **uniquement** depuis le JWT (`StockMasterPrincipal`), montants XAF entiers (`DEC-003`), dates ISO-8601, refus `403` hors rôle, `400` sur période invalide.

| Méthode | Chemin | Rôle | Réponse |
|---|---|---|---|
| `GET` | `/api/v1/reporting/top-articles?debut=&fin=&limit=10&tri=quantite\|ca` | tout authentifié | `top: [{articleId, libelle, quantiteVendue, caHt}]` |
| `GET` | `/api/v1/reporting/top-clients?debut=&fin=&limit=10` | COMMERCIAL, ADMIN_FILIALE, ADMIN_GROUPE | `top: [{clientId, nom, caHt, nbVentes}]` |
| `GET` | `/api/v1/reporting/filiales?debut=&fin=` | **ADMIN_GROUPE** | `filiales: [{entrepriseId, nom, caHt, nbVentes, panierMoyen}]` |
| `GET` | `/api/v1/reporting/export?rapport=top-articles\|top-clients\|filiales&format=csv\|pdf&debut=&fin=` | idem rapport | flux binaire + `Content-Disposition: attachment; filename=…` |

Règles transverses :

- `debut`/`fin` par défaut = mois courant ; `fin < debut` → `400` (`SYS_BAD_REQUEST`), période > 366 jours refusée.
- `limit` borné à `[1, 50]` (défaut 10), `tri` inconnu → `400`.
- Isolation : `entrepriseId` toujours issu du JWT pour F090 ; pour F092 (`ADMIN_GROUPE`) la liste des filiales vient du groupe du principal, **jamais** d'un paramètre d'entrée.
- Calculs délégués à `CalculCaService` (`caHt`, `TVA`, `TTC`) — pas de formule dupliquée dans le controller ou le front.

---

## 4. Découpage en sous-tickets

| ID | Titre | Parent | SP | Dépend de |
|---|---|---|---|---|
| R0 | Socle reporting : port de lecture `vente` + whitelist ArchUnit + squelette `reporting` | — | 3 | — |
| R1 | API top articles vendus | #1225 | 3 | R0 |
| R2 | Écran Top articles / clients fidèles | #1225 | 3 | R1, R3 |
| R3 | API top clients fidèles | #1225 | 2 | R0 |
| R4 | API comparaison inter-filiales | #1227 | 3 | R0 |
| R5 | Écran Comparaison inter-filiales | #1227 | 3 | R4 |
| R6 | Export CSV/PDF côté backend | #1228 | 3 | R1, R3, R4 |
| R7 | Bouton et téléchargement d'export côté front | #1228 | 1 | R6 |

### R0 — Socle reporting (3 SP)

**Backend**
- `vente/port/LectureVentePort` + `vente/service/LectureVenteService` (lecture seule, `@Transactional(readOnly = true)`).
- Requêtes `LigneVenteRepository` : agrégats par `(article)`, par `(client)`, par `(entreprise)`, filtrés par période et statut de vente.
- `reporting/controller/ReportingController` (enveloppe `ApiResponse`, `@Validated`), `reporting/service/ReportingService` (orchestration + mapping).
- `CONTRACT_PACKAGES` : `com.stockmaster.vente.port..`, `com.stockmaster.vente.domain.enums..`.

**Tests**
- `LectureVenteServiceTest` (Mockito, pattern `StockServiceTest`) : filtres période/statut, isolation tenant.
- `ModuleDependencyArchTest` : règle 1 verte ; un test négatif (classe de test dédiée) vérifie que `reporting` → `vente.repository` **échoue**.
- `ReportingControllerTest` (MockMvc standalone) : `403` hors rôle, `400` période invalide.

**DoD** : `./mvnw test -pl stockmaster-reporting,stockmaster-vente,stockmaster-bootstrap -am` vert ; aucun `nativeQuery` dans `reporting`.

### R1 — API top articles (3 SP)

`GET /api/v1/reporting/top-articles`. Agrégation `quantiteVendue = Σ ligne_vente.quantite`, `caHt` via `CalculCaService` ; tri `quantite` (défaut) ou `ca` ; `limit` ; article sans vente exclu. Tests : tri, limite, période vide → liste vide (pas d'erreur), isolation entre deux filiales du même groupe.

### R3 — API top clients (2 SP)

`GET /api/v1/reporting/top-clients`. `caHt` et `nbVentes` (distinct `vente.id`) par client ; clients sans vente exclus ; RBAC COMMERCIAL / ADMIN_FILIALE / ADMIN_GROUPE ; `403` pour CAISSIER et GESTIONNAIRE_STOCK.

### R2 — Écran Top articles / clients (3 SP)

`frontend/src/features/reporting/` avec la structure du dépôt (`api.ts`, `hooks.ts`, `types.ts`, `pages/`), route protégée dans `app/`, `usePaginationUrl` si pagination.
- Onglets « Articles » / « Clients », sélecteur de période (mois courant par défaut), `BarChart` recharts + tableau triable.
- États : chargement (skeleton), vide (« Aucune vente sur la période »), erreur (toast US-F003).
- Montants formatés XAF, jamais recalculés côté client.

### R4 — API comparaison inter-filiales (3 SP)

`GET /api/v1/reporting/filiales`, **ADMIN_GROUPE uniquement** (`403` sinon). Une ligne par filiale du groupe du principal, y compris celles à 0 vente ; `panierMoyen = caTtc / nbVentes` (arrondi HALF_UP, division par zéro évitée). Tests : groupe de 3 filiales dont une inactive, refus d'un `ADMIN_FILIALE`, aucune fuite inter-groupes.

### R5 — Écran comparaison (3 SP)

Tableau filiales × indicateurs + `BarChart` groupé (CA HT / nb ventes), tri par CA décroissant, mention explicite « vue groupe » ; état vide si aucune filiale. Aucun sélecteur de filiale : le périmètre vient du JWT.

### R6 — Export CSV/PDF (3 SP)

- `format=csv` : `text/csv; charset=UTF-8`, séparateur `;`, BOM UTF-8 (Excel FR), en-têtes français, streaming (`StreamingResponseBody`) pour rester sous contrôle mémoire.
- `format=pdf` : **décision à prendre** — OpenPDF (fork iText 2, LGPL) est le candidat par défaut ; toute lib à licence non permissive est exclue. Document A4 paysage, logo/titre entreprise, période, tableau, pied de page de génération.
- `Content-Disposition` avec nom horodaté (`top-articles_2026-11-18.csv`) ; réutilise **exactement** les services de R1/R3/R4 (aucune requête dupliquée).
- Tests : contenu CSV (en-têtes, échappement `;`/guillemets), `Content-Type`, `404`/`400` sur `rapport`/`format` inconnus, PDF non vide avec `%PDF-` en tête.

### R7 — Téléchargement côté front (1 SP)

Helper unique `telechargerFichier()` dans `shared/lib` : `fetch` via `api-client`, `response.blob()`, nom lu dans `Content-Disposition`, `URL.createObjectURL` + révocation, gestion d'erreur (un `403`/`500` renvoyé en JSON ne doit pas produire un fichier corrompu). Bouton « Exporter » (CSV / PDF) sur chaque écran, état désactivé pendant le téléchargement.

---

## 5. Séquencement conseillé

1. **R0** seul (débloque tout, fait tomber la contrainte ArchUnit).
2. **R1 + R3** en parallèle (même controller, même service), puis **R2**.
3. **R4**, puis **R5**.
4. **R6**, puis **R7**.

F090 et F092 resteront sans données réelles tant que **#1142 (US-064)** n'est pas livrée : prévoir des jeux de données de test (Flyway de test ou fixtures d'intégration) pour valider les écrans, et ne pas afficher de « 0 » trompeur en production — l'état vide doit être explicite.

## 6. Tickets GitHub

| ID | Issue | Parent |
|---|---|---|
| R0 | #1251 | — (transverse) |
| R1 | #1252 | #1225 (F090) |
| R3 | #1253 | #1225 (F090) |
| R2 | #1254 | #1225 (F090) |
| R4 | #1255 | #1227 (F092) |
| R5 | #1256 | #1227 (F092) |
| R6 | #1257 | #1228 (F093) |
| R7 | #1258 | #1228 (F093) |

Le dépôt n'expose pas l'API GitHub des sub-issues (404) : le rattachement est documenté par commentaire sur chaque ticket parent, et l'ordre de dépendance par la colonne « Dépend de » du §4.
