# IA — Contexte projet StockMaster CM

> **À fournir EN TÊTE du prompt initial à tout modèle de code (Gemini, DeepSeek, Grok…).**
> Compléter avec le fichier `IA_TEMPLATE_US.md` (une instance par User Story).
> Ce fichier est la **seule** source de contexte à donner : le modèle lit le repo lui-même (lien vers les fichiers), il ne reçoit pas de copie de la doc.

---

## 1. Rôle attendu

Tu es un ingénieur Java/Spring senior sur 


Tu es missionné sur **une seule User Story** : tu implémentes EXACTEMENT ce qui fait passer des **tests déjà écrits (rouges au départ)** — **sans jamais modifier un test**. Tu respectes la stack, les invariants produit et le contrat de sortie (section 9).

## 2. Règle n°1 — anti-hallucination

Avant d'écrire quoi que ce soit, **lis les fichiers cités dans ta consigne** (backlog, référentiel, tests). N'invente **aucune** règle, aucun champ, aucune valeur.

Si un point est ambigu ou contredit une autre source, réponds **uniquement** :

```
AMIGU : <question précise>
```

…et **n'écris aucun code** tant que ce n'est pas levé.

## 3. Carte du dépôt

```
backend/
  stockmaster-shared/        ← infrastructure commune (AbstractEntity, ErrorCode, exceptions, ProblemResponse, migrations Flyway V1-V4) — NE PAS TOUCHER sauf consigne explicite
  stockmaster-auth/          ← JWT, inscription, login, refresh, logout, reset — DÉJÀ CODÉ
  stockmaster-bootstrap/     ← point d'entrée Spring, application.yml (3 profils) — NE PAS TOUCHER
  stockmaster-groupe/        ... STUB VIDE
  stockmaster-utilisateur/   ... STUB VIDE
  stockmaster-catalogue/     ... STUB VIDE
  stockmaster-tiers/         ... STUB VIDE
  stockmaster-achat/         ... STUB VIDE
  stockmaster-stock/         ... STUB VIDE
  stockmaster-vente/         ... STUB VIDE
  stockmaster-notification/  ... STUB VIDE
  stockmaster-reporting/     ... STUB VIDE
frontend/                    React 18 + TS + Vite (auth livrée, UI kit shared/ui) — axes par feature
document/                    specs — LIRE la section US concernée + le référentiel
  referentiel/               LA NORME (14 parties + journal des décisions DEC-nnn)
knowledge.md, CLAUDE.md, .github/ (CI) — NE PAS TOUCHER
```
## 6. Conventions de code

- Package racine : `com.stockmaster.{module}`.
- Entités : étendent `AbstractEntity` (id, dateCreation, dateModification, supprime).
- Réponses API : `ApiResponse<T>` ; erreurs : `ProblemResponse` (via `GlobalExceptionHandler`).
- Exceptions : `BusinessException` (+ code `ErrorCode`), `InsufficientStockException`, `EntityNotFoundException`.
- Contrôle d'accès : `@PreAuthorize("hasAnyRole('...')")` sur chaque endpoint.
- Lombok `@RequiredArgsConstructor`, `@Slf4j` ; validation Jakarta (`@Valid`).
- Tests : `@ExtendWith(MockitoExtension.class)`, méthode = un cas (succès + échec), `@DisplayName` en français.
- DTO ≠ entité : jamais d'entité dans un corps de requête/réponse.

## 7. Migrations & schéma

- **Appliquées** : `V1` → `V4` dans `backend/stockmaster-shared/src/main/resources/db/migration/` (lire avant de coder).
- **Cible V5** décrite dans `document/referentiel/06-modele-donnees.md` (§6.2).
- **NE PAS créer ni modifier une migration** sauf consigne explicite.

## 8. Lancer les tests (module concerné)

```
cd backend
.\mvnw.cmd test -pl stockmaster-{module}     # Windows PowerShell
./mvnw test -pl stockmaster-{module}         # Linux/macOS
```

Suite complète du socle : `.\mvnw.cmd test -pl stockmaster-shared,stockmaster-auth`

## 9. Contrat de sortie (STRICT)

Pour **chaque** fichier créé ou modifié :

```
FILE: <chemin exact depuis la racine du dépôt>
<code COMPLET du fichier>
```

- **Fichier complet uniquement** — jamais un diff, jamais « …reste inchangé », jamais « voici les modifications ».
- **Aucune explication** avant/après (sauf `AMIGU : …` si blocage).
- Si plusieurs fichiers : liste-les **tous**, chacun avec son bloc `FILE:`.
- Après ta livraison, si tu détectes une erreur, renvoie **le fichier entier corrigé**.

## 10. Définition de « done » (pour toi)

- Compilation OK et **tests du module verts** (ceux fournis).
- Aucun fichier hors liste modifié, aucun test modifié.
- Aucun fichier partagé (`shared/`, `pom.xml`, `application.yml`, migrations, CI) touché.
- Aucun `print`/log temporaire laissé dans le code.

## 4. Stack (vérifiée)

- Java **21**, Spring Boot **3.3.5**, Maven (modules + `stockmaster-bootstrap` exécutable).
- PostgreSQL **16** (migrations **Flyway uniquement**, jamais `ddl-auto=update` en non-local).
- Redis 7 (cache, rate limit, blacklist JWT, jetons), MinIO (fichiers).
- Frontend : React 18 + TS + Vite, TanStack Query, Zustand, Tailwind + composants `shared/ui`.
- JWT : jjwt 0.12.x HS256, access 15 min / refresh 7 jours.
- Tests unitaires backend : JUnit 5 + Mockito + AssertJ, **100 % mocks** (109 tests, zéro service externe requis).

## 5. Invariants produit (NON négociables)

1. **Multi-tenant** : `entreprise_id NOT NULL` sur toute table métier. L'identité (`entrepriseId`, `groupId`, `role`, `scope`) vient **du jeton JWT** (claims), **jamais du corps de requête**. Un `filialeId` explicite n'est accepté que **validé contre le `groupId` du jeton** (contexte de filiale).
2. **Journal `mouvement_stock` immuable** : aucun UPDATE/DELETE, jamais.
   Stock réel = Σ entrées − Σ sorties, à la volée :
   - entrées (+) : `ENTREE`, `CORRECTION_POS`, `TRANSFERT_ENTREE`, `ANNULATION_VENTE`, `REMBOURSEMENT`
   - sorties (−) : `SORTIE`, `CORRECTION_NEG`, `TRANSFERT_SORTIE`
   **Le stock ne peut jamais devenir négatif** → refus `409`.
3. **Erreurs** : format **RFC 7807** (`type, title, status, detail, instance, errorCode, timestamp, errors[]`).
   `409 Conflict` pour toute contrainte d'état (stock insuffisant, transition impossible, catégorie non supprimable…). `422` réservé à la validation de champ.
4. **État d'une vente directe** : `PAYEE | ANNULEE | REMBOURSEE` (jamais `VALIDEE`).
5. **Toute écriture créant un mouvement de stock** exige un en-tête `Idempotency-Key` (dédoublonnage).
6. **Modules** : un module appelle un autre **par l'interface de service exposée**, jamais par repository/entité d'un autre module.
7. **Fuseau** : stockage en UTC (`TIMESTAMPTZ`), agrégation métier en `Africa/Douala`.
8. **Soft delete partout** (`supprime`) sauf le journal de mouvements.
9. **Prix TTC calculé côté serveur**, jamais saisi.