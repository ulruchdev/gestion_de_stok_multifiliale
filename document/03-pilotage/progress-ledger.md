# 📊 Progress Ledger — StockMaster CM

> Journal de suivi de l'avancement des US. Mis à jour par `doc-writer.ts` après chaque US terminée.
> **Référence :** BACKLOG_StockMaster_CM.md, GS-FRONTEND-BACKLOG-2026-01.md, GS-DESIGN-BACKLOG-2026-01.md

## Légende

- [ ] Non commencé
- [~] En cours
- [✅] Terminé

---

## EPIC 1 — Fondations Techniques (19 SP)

| US | Description | Statut | Notes |
|----|-------------|--------|-------|
| US-001 | Initialisation projet Spring Boot | ✅ | Fait |
| US-002 | Configuration Flyway + schéma initial | ✅ | Fait |
| US-003 | Gestion centralisée des erreurs | ✅ | Fait |
| US-004 | Pipeline CI/CD + Docker | ✅ | Fait |
| US-005 | Conteneurisation Docker | ✅ | Fait |

## EPIC 2 — Authentification & Accès (35 SP)

| US | Description | Statut | Notes |
|----|-------------|--------|-------|
| US-006 | Inscription entreprise unique | ✅ | Fait + DTO aligné frontend (Juillet 2026) : `nomBoutique`→`nomEntreprise`, `prenom`/`nom`→`adminPrenom`/`adminNom`, ajout `nif`(opt.) + `telephone`(E.164), retrait `ville`/`quartier` |
| US-007 | Inscription groupe | ✅ | Merge effectué (PR #6) — code + tests OK. Validation téléphone renforcée E.164 |
| US-008 | Connexion JWT | ✅ | Fait. Rate limiting rendu configurable via properties (stockmaster.rate-limiting) |
| US-009 | Refresh token | ✅ | Merge effectué |
| US-010 | Déconnexion | ✅ | Merge effectué. Blacklist JWT vérifiée — correcte (TTL Redis) |
| US-011 | Mot de passe oublié | ✅ | Merge effectué |
| US-012 | Réinitialisation mot de passe | ✅ | Merge effectué |
| US-013 | Changement mot de passe | ✅ | Merge effectué |
| US-082 | Unicité stricte des entreprises (NIF, téléphone, email, nom groupe) | ✅ | Terminé — commit `c3dc635` (Sprint 11 add.). Index UNIQUE partiels (V4), 71 tests auth au total |

### Corrections de sécurité (Juillet 2026)
- **JwtAuthenticationFilter bugfix** : Ne bloque plus les endpoints `.permitAll()` avec un token expiré/invalide (retournait 401 au lieu de continuer la chaîne)
- **Rate limiting configurable** : `RateLimitProperties.java` avec deux niveaux (global 100 req/min + per-endpoint) — plus de constantes hardcodées
- **Rate limiting externalisé en variables d'environnement** (commit `395400f`) : plus aucune valeur numérique en dur dans `application.yml`, défauts de sécurité d'origine préservés via `${RATE_LIMIT_*:defaut}`
- **Regex mot de passe élargie** (commit `cea66de`) : jeu de caractères spéciaux étendu dans les 4 DTOs concernés

### Durcissement sécurité (EPIC 2, renuméroté GS-CDA-2026-02 : US-014-017 → US-083-086)

| US | Description | Priorité | Statut | Notes |
|----|-------------|----------|--------|-------|
| US-083 | Rotation du Refresh Token avec détection de rejeu | P0 | ✅ | Terminé (branche `feature/GS-083-rotation-refresh-token`) — familyId+jti chaînés, rotation à chaque refresh, révocation totale + événement sur rejeu détecté. 90 tests shared+auth verts, vérifié en conditions réelles (login→refresh→rejeu→401 AUTH_006) |
| US-084 | Hachage des mots de passe en Argon2id | P1 | ❌ | Non commencé — vérifié : `SecurityConfig` utilise encore uniquement `BCryptPasswordEncoder` |
| US-085 | Comportement fail-closed ciblé en cas d'indisponibilité de Redis | P0 | ✅ | Terminé (branche `feature/GS-085-fail-closed-redis`) — fail-closed **503 SEC_004** ciblé sur `POST /auth/refresh` (lecture + écriture de rotation) et rate-limiting par endpoint sensible ; **fail-open** best-effort sur login/logout/forgot, compteur global de rate-limiting et vérification blacklist JWT. Réutilise `RedisHealthTracker` (shared, déjà sur `main`) → alerte ERROR après 60 s d'indisponibilité continue. Conforme ADR-005 (Redis mono-instance, pas de HA) |
| US-086 | Logs d'audit structurés pour les événements d'authentification | P1 | ❌ | Non commencé — vérifié : aucun log JSON structuré (MDC/logstash) |

> ✅ Les deux durcissements **P0** (US-083 rotation refresh + US-085 fail-closed ciblé Redis) sont terminés → le blocage EPIC 3 au sens du backlog (`BACKLOG_StockMaster_CM.md` : dépendance EPIC 3 = « EPIC 2 complété ») est levé côté P0. Restent US-084 (Argon2id) et US-086 (logs d'audit), tous deux **P1**, non bloquants pour EPIC 3.

## EPIC 3 — Groupe & Filiales (en cours)

| US | Description | Priorité | Statut | Notes |
|----|-------------|----------|--------|-------|
| US-015 | GET `/api/v1/groupe` — consulter groupe + plan actif | P0 | ✅ | Terminé (branche `feature/GS-015-consulter-groupe`) — `GroupeService`/`GroupeController`/`GroupeResponse` + 12 tests (4 service + 4 controller + 4 `ControleLimiteFilialesService`) verts. `StockMasterPrincipal` déplacé `auth.config` → `shared.security` (contrat inter-module, référencé par `AuthServiceImpl`/`JwtAuthenticationFilter`) ; nouvelle méthode `EntrepriseRepository.countByGroupeIdAndTypeEntrepriseAndActifTrueAndSupprimeFalse`. **3 bugs corrigés dans le code déjà présent (non commité)** : import `@Bean` manquant dans `GroupeTestApplication`, nom de méthode mocké dans `GroupeServiceTest` désynchronisé du repository réel (`...SupprimeFalse` → `...ActifTrueAndSupprimeFalse`, cohérent avec « filiales actives »), fixture `groupeActif()` sans `.id(1L)` → assertion `getId()` nulle. |
| US-014 | PUT `/api/v1/groupe` — modifier nom/logo/infos fiscales | P0 | ✅ | Terminé (branche `feature/GS-014-modifier-groupe`, sur `feature/GS-015-consulter-groupe` non encore mergée). **Migration V6** (`tenant_group` : `logo VARCHAR(500)`, `nif VARCHAR(20)` + index unique partiel, `raison_sociale VARCHAR(150)` — colonnes absentes de la cible V5 documentée, décision utilisateur). **`MinioService`** créé dans `shared.storage` (`MinioProperties`/`MinioConfig`/`MinioService`) — commun et réutilisable par tout module (US-081 logo entreprise notamment) : upload image (PNG/JPEG/WebP, 2 Mo max), bucket unique `stockmaster` créé paresseusement en lecture publique au premier usage, objets préfixés par domaine (`groupe/{id}/uuid.ext`). `GroupeService.modifier()` : PATCH sémantique (nom/raison sociale/NIF/logo indépendamment optionnels), unicité nom et NIF vérifiée (`GRP_DUPLICATE_NOM_GROUPE`/`RES_DUPLICATE_NIF`), même isolation `groupId` JWT que US-015. `GroupeResponse` étendu (`logo`/`nif`/`raisonSociale`, additif — n'affecte pas le contrat US-015). Nouveau `ErrorCode.SYS_FILE_TOO_LARGE` (413). 23 tests verts (7 `MinioServiceTest` + 7 `GroupeServiceTest.Modifier` + 5 `GroupeControllerTest.Modifier` + les 12 US-015 déjà verts). |
| US-016 | POST `/api/v1/groupe/filiales` — créer une filiale | P0 | ✅ | Terminé (branche `feature/GS-016-creer-filiale`). `FilialeService.creer()` réutilise `ControleLimiteFilialesService` (déjà testé, pré-existant) : limite lue de `tenant_group.limite_filiales`, comptage **tous types confondus** (`site_operationnel=TRUE AND actif=TRUE AND supprime=FALSE`) conforme à `DEC-015` §« règles de bordure » (« la maison mère compte si elle détient du stock »). Vérification `codeFiliale` unique dans le groupe **en miroir exact** de la contrainte DB `UNIQUE(group_id, code_filiale)` (V1, sans filtre `supprime` — un code soft-supprimé reste réservé). `parent_id` résolu via la maison mère du groupe (`findFirstByGroupeIdAndTypeEntreprise`). Nouvelles méthodes `EntrepriseRepository` : `existsByGroupeIdAndCodeFiliale`, `countByGroupeIdAndSiteOperationnelTrueAndActifTrueAndSupprimeFalse`, `findFirstByGroupeIdAndTypeEntreprise`. `FilialeController` dédié (`/api/v1/groupe/filiales`), séparé de `GroupeController`. **Bug d'infrastructure de test découvert et corrigé** : `GroupeTestApplication` scannait tout le package `groupe.controller` (`@ComponentScan`) — avec 2 contrôleurs désormais, chaque `@WebMvcTest` échouait à charger l'autre contrôleur (service non mocké). Remplacé par `@Import` ciblé par contrôleur ; nouveau `FilialeTestApplication` dédié. 13 tests verts (7 service + 6 controller), couverture vérifiée localement (`FilialeController` 100%, `FilialeService` ~97% lignes). |
| US-017 | GET `/api/v1/groupe/filiales` — lister les filiales | P0 | ✅ | Terminé (branche `feature/GS-017-lister-filiales`, sur `feature/GS-016-creer-filiale` non encore mergée). Pagination + filtres `actif`/`ville` optionnels via requête `@Query` unique (`:param IS NULL OR ...` — évite de multiplier les méthodes dérivées). Nouveau `PageResponse<T>` (`shared.dto.response`) — enveloppe JSON stable, **premier endpoint paginé du projet**, réutilisable partout ensuite. `FilialeResponse` étendu (`nombreEmployes`, additif — `0` posé explicitement à la création US-016). `parentId`/maison mère résolu **une seule fois** pour toute la page (pas de traversée de l'association paresseuse par ligne) ; le compte d'employés reste en revanche N+1 par filiale (page bornée à 100 par `stockmaster.pagination.max-page-size` — accepté, documenté, pas de `GROUP BY` batché pour rester dans le périmètre de l'US). Nouvelles méthodes : `EntrepriseRepository.findFilialesDuGroupe` (paginée+filtrée), `UtilisateurRepository.countByEntrepriseIdAndSupprimeFalse`. 12 tests verts (8 service + 4 controller). Couverture vérifiée localement : `FilialeController` 100%, `FilialeService` lignes 100%/instructions ~99% ; `PageResponse` à 0% en local mais dans `**/dto/**` → exclu par `sonar.coverage.exclusions` (même traitement qu'`ApiResponse`/`GroupeResponse`/`FilialeResponse`, pas de risque de régression Sonar). |
| US-018 | PUT `/api/v1/groupe/filiales/{id}` — modifier une filiale | P0 | ✅ | Terminé (branche `feature/GS-018-modifier-filiale`, sur `feature/GS-017-lister-filiales` non encore mergée). PATCH sémantique (nom/codeFiliale/ville/quartier indépendamment optionnels, même contrat que US-014/016). Appartenance au groupe vérifiée par filtrage combiné (`supprime=false` + `typeEntreprise=FILIALE` + `groupe.id=groupId`) → `404 RES_ENTITY_NOT_FOUND` uniforme si la filiale n'existe pas, est supprimée, appartient à un autre groupe, **ou** si l'id référence la maison mère (jamais révéler l'existence — critère d'acceptation explicite). Unicité `codeFiliale` vérifiée hors la filiale elle-même (nouvelle méthode `existsByGroupeIdAndCodeFilialeAndIdNot`, même miroir de la contrainte DB que US-016). **Refactor DRY** : mapping `Entreprise` → `FilialeResponse` factorisé dans un helper privé `toResponse()`, réutilisé par `creer()`/`lister()`/`modifier()`. 13 tests verts (9 service + 4 controller). Couverture vérifiée localement : `FilialeController` 100%, `FilialeService` lignes ~97%/instructions ~97%. |
| US-020 | GET `/api/v1/groupe/dashboard` — dashboard consolidé | P0 | ❌ | Non commencé — dépend des EPICs 5-6 (stock, vente), `@Cacheable` Redis TTL 5 min |
| US-019 | PATCH `/api/v1/groupe/filiales/{id}/statut` — activer/désactiver une filiale | P1 | ✅ | Terminé (branche `feature/GS-019-statut-filiale`, sur `feature/GS-018-modifier-filiale` non encore mergée). Bascule `actif` uniquement, même isolation groupe que US-018 (404 uniforme, jamais révéler l'existence). Blocage effectif des mouvements de stock/commandes sur un site désactivé **hors périmètre** (EPICs 5-6 non construits) — ce sera aux modules stock/vente de refuser toute écriture sur `actif=false` une fois bâtis ; noté pour ne pas l'oublier. **Refactor DRY supplémentaire** : `groupIdDuPrincipal()` et `chargerFilialeDuGroupe()` extraits en helpers privés partagés par `creer()`/`lister()`/`modifier()`/`changerStatut()` (élimine la 4ᵉ répétition du bloc d'isolation). `FilialeStatutRequest` (`actif` obligatoire). 10 tests verts (5 service + 5 controller). Couverture vérifiée localement : `FilialeController` 100%, `FilialeService` lignes ~97%/instructions ~97,5%. |
| US-014b, US-081 | (P1, hors séquence P0, restants) | P1 | ❌ | Non commencé |

---

## EPIC 4 — Gestion des Utilisateurs (en cours)

| US | Description | Priorité | Statut | Notes |
|----|-------------|----------|--------|-------|
| US-021 | POST `/api/v1/utilisateurs/admin-filiale` — créer un Admin Filiale | P0 | ✅ | Terminé (branche `feature/GS-021-creer-admin-filiale`). Rôle forcé `ADMIN_FILIALE`/scope `FILIALE` (jamais lu de la requête), isolation groupe du JWT → 404 uniforme (inexistante, soft-supprimée, autre groupe, maison mère), 409 `AUTH_008` sur email existant, compte né `actif=false`+`emailVerifie=false` avec mot de passe aléatoire haché (aucune connexion avant activation US-075). **US-101 intégré** : limite lue de `tenant_group.limite_utilisateurs` (jamais en dur), plan expiré traité GRATUIT (10) pour le calcul, dépassement → 403 `USR_001` (nouveau `ErrorCode`). Invitation : token UUID en Redis (`invitation:{token}` → userId, TTL 48h) envoyé via le **canal EMAIL**. **`EmailNotificationService`** (notification module) : première implémentation du port `CanalNotification` (DEC-014) — SMTP optionnel (`ObjectProvider<JavaMailSender>`, no-op journalisé sans config), envoi asynchrone (exécuteur dédié 2 threads), échecs avalés (philosophie US-006), adresse résolue depuis `destinataireId` (aucune adresse en clair dans le port). Sert US-022/US-074/US-075 sans nouvelle infra. `utilisateur` pom : dépendance au port notification (contrat uniquement, règle ArchUnit n°1) + `spring-security-test`. 307/307 tests verts (reactor complet) dont 30 nouveaux ; ArchUnit 4/4 ; intégration Flyway V1→V5 + validate OK (PG16 port 5433), 0 missing column. **Leçons** : ternaire int/Integer auto-unboxe et NPE sur null (limite absente) ; le mock canal étant synchrone, le service avale aussi les échecs synchrones (contrat retry-friendly DEC-014) ; un test compilé par l'IDE avec erreurs (`target` pollué) produit une classe à « Unresolved compilation problem » — purge du `target` du module suffi. |
| US-022 | POST `/api/v1/utilisateurs/employes` — créer un employé | P0 | ✅ | Terminé (branche `feature/GS-022-creer-employe`). **Option A** : compte né `actif=true` + `emailVerifie=true`, mot de passe provisoire haché BCrypt remis en main propre — connexion immédiate (terrain camerounais ; DEC-016 n'impose le token d'activation qu'à l'inscription publique, l'invitation email reste pour US-021). Rôles **limités aux 4 métiers** (`GESTIONNAIRE_STOCK`/`RESP_ACHATS`/`COMMERCIAL`/`CAISSIER`) — tout rôle `ADMIN_*` → 403 `SEC_001`, contrôle **fail-fast avant toute I/O**. `entreprise_id` depuis le JWT (jamais du corps, DEC-019), scope forcé `FILIALE`, 404 uniforme si entreprise absente/supprimée/hors groupe. 409 `AUTH_008` email unique plateforme. **US-101 intégré** (même mécanique qu'US-021 : limite lue du plan, expiré → GRATUIT 10, dépassement → 403 `USR_001`). **Correctif ArchUnit au passage** : `NotificationAlerte.article` (entité `catalogue.domain.entity.Article`) violait la règle n°1 → remplacé par `Long articleId` (même colonne `article_id`, aucune migration requise) — les modules référencent les clés étrangères, jamais les entités des autres modules. **Refactor test-infra** : `UtilisateurTestApplication` n'importe plus aucun contrôleur en dur — chaque test MVC fait `@Import(<son contrôleur>)`, sinon tout contexte du module exigerait les dépendances de tous les contrôleurs (le test US-021 retournait 500 faute de `AdminFilialeService` dans le contexte d'US-022) ; stubs de fixtures partagées (`PasswordEncoder`, claims) passés en `lenient()` pour les tests fail-fast. 328/328 tests verts (reactor, +21 dont 18 US-022) ; ArchUnit 4/4 ; intégration Flyway V1→V6 + validate OK (PG16 port 5433). |
| US-023 | GET `/api/v1/utilisateurs` — lister les utilisateurs du périmètre | P0 | ✅ | Terminé (branche `feature/GS-023-lister-utilisateurs`). **Périmètre Groupe↔Filiale** : Admin Groupe → toutes ses filiales (resserrable via `filialeId` pré-validé dans le groupe), Admin Filiale → SA filiale (JWT) — un `filialeId` autre que le sien → 403 `SEC_001` **avant toute I/O**. Filtres optionnels `role`/`actif`/`filialeId` (convention US-017 : `(:x IS NULL OR …)`), pagination bornée à `stockmaster.pagination.max-page-size`, enveloppe `ApiResponse<PageResponse<T>>`. **4 rôles métier refusés** (garde service en défense en profondeur après le `@PreAuthorize`) : la liste des comptes est une fonction d'administration — l'auto-scope ne légitime pas un CAISSIER. `filialeId` hors groupe → 403 (jamais 404 : refus de contenu sans confirmer l'existence). **Finders JPQL** dans `UtilisateurRepository` : `findByEntrepriseId` / `findByEntrepriseGroupeId` avec `JOIN FETCH u.entreprise` (anti N+1) et `supprime = false` ; `UtilisateurListResponse` ne porte aucun champ secret (critère « mot de passe JAMAIS retourné » vérifié par test). **Défense en profondeur testée** : une ligne hors groupe échappée au filtre SQL est écartée et journalisée, pas servie. 349/349 tests verts (reactor, +21) ; ArchUnit 4/4 ; intégration Flyway V1→V6 OK (PG16 port 5433). |
| US-024 | PUT `/api/v1/utilisateurs/{id}` — modifier un utilisateur | P0 | ✅ | Terminé (branche `feature/GS-024-modifier-utilisateur`). Sémantique **PATCH** (endpoint PUT, convention US-018) : prenom/nom/email/role optionnels, champ null = inchangé ; **mot de passe jamais modifiable** ici (flux dédiés US-011/012). **Périmètre** (critère US-024) : cible hors périmètre / inexistante / soft-supprimée → **404 uniforme** `RES_001` (jamais révéler l'existence) ; Admin Filiale → sa filiale (JWT), Admin Groupe → ses filiales. **Classe modifiable verrouillée** : cible ET nouveau rôle limités aux 4 rôles métier — jamais `ADMIN_*` (pas d'auto-promotion ; les admins se créent par US-021) → 403 `SEC_001`. **Email** : unicité plateforme hors la cible via `existsByEmailAndIdNot` (miroir contrainte DB DEC-008, convention US-018) → 409 `AUTH_008` ; email identique à l'actuel court-circuite le lookup ; `emailVerifie` volontairement inchangé (le flux de vérification arrive avec l'EPIC 12 — le réinitialiser verrouillerait le compte sans recours). **Journalisation** (critère US-024) : changement de rôle loggé INFO avec ancien/nouveau rôle + admin — vérifié par capture Logback. Garde rôle principal en fail-fast avant toute I/O. 380/380 tests verts (reactor, +31) ; ArchUnit 4/4 ; intégration Flyway V1→V6 OK (PG16 port 5433). |
| US-026 | GET/PUT `/api/v1/utilisateurs/profil` — consulter/modifier son profil | P1 | ✅ | Terminé (branche `feature/GS-026-profil-utilisateur`). **Consultation** : l'utilisateur connecté voit **son** profil (prenom, nom, adresseVille, photo, email, rôle) — résolution par le JWT, jamais par un id passé en paramètre. **Modification** : `multipart/form-data` — part JSON obligatoire `{prenom?, nom?, adresseVille?}` (sémantique PATCH : champ absent = inchangé) + part `photo` optionnelle → `MinioService.uploadImage("utilisateur/<id>", …)` (`shared/storage` préexistant : validation type + 2 Mo incluse, retourne l'URL publique). **Email/rôle absents du DTO** → non modifiables par design (l'admin passe par US-024) ; mot de passe jamais touché ici. Garde-fous principal en fail-fast avant toute I/O (pattern US-023/024). 14 tests profil verts (service 8 + contrôleur 6) ; reactor complet 421 verts (1 seul échec environnemental — Redis local down → health 503 — le test d'intégration exige Redis sur localhost:6379, revérifié 3/3 vert avec Redis up) ; ArchUnit 4/4 ; intégration Flyway V1→V6 OK (PG16 port 5433) + health UP 200. Collection Postman à jour (7 requêtes EPIC 4). |
| US-025 | PATCH `/api/v1/utilisateurs/{id}/statut` — activer/désactiver un utilisateur | P0 | ✅ | Terminé (branche `feature/GS-025-activer-desactiver-utilisateur`). **Révocation immédiate** (critère US-025) : `actif=false` → nouveau port **`TokenRevocationPort`** (shared, pattern DEC-014 — ArchUnit-legal) implémenté par auth (`RevocationTokenAdapter`) : blacklist Redis `blacklist:user:{id}` (TTL = durée de vie max d'un access token) + suppression `refresh:{id}` (rotation US-083 coupée) ; **filtre JWT** étendu d'une 2ᵉ vérification par utilisateur → les tokens en cours sont refusés dès la prochaine requête, sans attendre leur expiration. Fail-open US-085 conservé partout (Redis down = barrière `actif=false` au login/refresh, tokens ≤ 15 min). Révocation appelée **après** le `save` (l'état base fait foi). **Périmètre** miroir US-024 : hors périmètre/inexistante/soft-supprimée → 404 uniforme ; cible `ADMIN_*` → 403 `SEC_001` (cycle de vie admin distinct). Réactivation → **aucune** révocation. `actif` obligatoire dans le corps (pas de bascule implicite). Historique conservé : modification seule, jamais de delete/recréation. 419/419 tests verts (reactor, +39) ; ArchUnit 4/4 ; intégration Flyway V1→V6 OK (PG16 port 5433). **Refactor qualité Sonar (gate dup. 4,9 % > 3 % + complexité filtre 22 > 15)** : nouveau composant **`PerimetreAdminGuard`** — les garde-fous `groupIdDuPrincipal`/`roleDuPrincipal`/`verifierPerimetre`, jusque-là copiés en privé dans les 5 services admin (US-021→025), vivent en un exemplaire unique (contrats 404/403 inchangés, tests inchangés au vert) ; factory `UtilisateurListResponse.de()` (ex-`toResponse` dupliqué 3×) ; filtre JWT extrait en `sessionRevoquee()` + `ecrire401()` (deux blacklists jti + user lisibles, complexité ramenée sous 15). |

> ✅ **Enveloppe `ApiResponse<T>` harmonisée** : `GroupeController.consulter()` retournait initialement `ResponseEntity<GroupeResponse>` brut, désynchronisé d'`AuthController` (`ApiResponse<T>` partout, `IA_CONTEXTE_PROJET.md` §6). Corrigé — `ResponseEntity<ApiResponse<GroupeResponse>>` (`$.data.id`…), test contrôleur et docs Postman mis à jour en conséquence. Les futurs endpoints `groupe` (US-014/016-020) doivent suivre ce même pattern.

## EPIC 5 — Catalogue (Catégories & Articles)

| US | Description | Priorité | Statut | Notes |
|----|-------------|----------|--------|-------|
| US-027 | POST `/api/v1/categories` — créer une catégorie | P0 | ✅ | Terminé (branche `feature/GS-027-creer-categorie`, commit `0a594d7`). RBAC : GESTIONNAIRE_STOCK / ADMIN_FILIALE / ADMIN_GROUPE autorisés, refus 403 SEC_001 pour CAISSIER / RESP_ACHATS avant toute I/O. Isolation tenant : groupId extrait du JWT (jamais du corps), groupe inexistant ou inactif → 403 GRP_002, principal non groupé → 403 GRP_002. Unicité (groupe, code) → 409 RES_002 (short-circuit avant charge groupe). Taux TVA restreints `TauxTvaAutorises` → 400 CAT_001, avec refus complémentaire taux < 0 et taux > 100%. Création group-scoped : catégorie attachée au groupe du JWT. |
| US-028 | GET `/api/v1/categories` — lister les catégories | P0 | ✅ | Terminé (branche `feature/GS-027-creer-categorie`, commit `0a594d7`). Pagination + tri designation ASC/DESC, recherche partielle insensible à la casse sur la désignation (absent = pas de filtre). Isolation tenant identique à US-027 : 403 GRP_002 groupe inexistant / principal non groupé, 403 SEC_001 pour rôle non autorisé. |
| US-029 | PUT `/api/v1/categories/{id}` — modifier une catégorie | P0 | ✅ | Terminé (branche `feature/GS-027-creer-categorie`, commit `0a594d7`). PATCH sémantique sur PUT : désignation + taux TVA, code non renégociable par cette route (DTO `CategorieUpdateRequest` ne l'expose pas). 404 RES_001 catégorie inconnue, 403 GRP_002 si catégorie hors groupe / groupe inexistant / principal non groupé, 400 CAT_001 si taux non supporté. |
| US-030 | DELETE `/api/v1/categories/{id}` — supprimer une catégorie | P0 | ✅ | Terminé (branche `feature/GS-027-creer-categorie`, commit `0a594d7` + correction DEC-028). Soft delete (`supprime = true`). 404 RES_001 catégorie inconnue, 403 GRP_002 si catégorie hors groupe / groupe inexistant / principal non groupé, 409 RES_005 si des articles sont attachés dans cette catégorie pour le groupe (DEC-028). Gestion des dépendances corrigée : la vérification d'articles attachés a été déplacée de `CategorieRepository` vers `ArticleRepository` (`existsByGroupeIdAndCategorieIdAndSupprimeFalse`) car la relation article → catégorie est gérée par `ArticleRepository`. |
| US-032 | GET `/api/v1/articles` — lister les articles | P0 | ✅ | Terminé (branche `feature/GS-032-crud-article`, CRUD complet US-032→035 en une passe à la demande du PO). Pagination disciplinée : size borné à 100 (critère AC), tri default designation ASC (`codeArticle` supporté). Recherche plein-texte (designation OU code article, sans casse) via JPQL bind-params (index GIN suivi performance documenté). Lecture « tout utilisateur authentifié » (`isAuthenticated()` + scoping groupe côté service, fail-fast 403 GRP_002 si principal non groupé). `@Transactional(readOnly = true)`. Metadata pagination complètes (ArticleListResponse miroir CategorieListResponse). |
| US-033 | GET `/api/v1/articles/{id}` — consulter un article | P0 | ✅ | Terminé (même branche). 404 RES_001 uniforme si article inconnu ou hors groupe (jamais révéler l'existence). Réponse enrichie : `margeBrutePct` calculée à la volée + `prixVenteTtc`. `stockActuel`/`statutAlerte` (REF §4.2) présents dans le contrat mais à `null` en V1 : le stock est calculé depuis `mouvement_stock` (EPIC 8 non livré) et l'isolation inter-modules (Spring Events seulement) interdit à catalogue d'y accéder. |
| US-034 | PUT `/api/v1/articles/{id}` — modifier un article | P0 | ✅ | Terminé (même branche). TTC recalculé côté serveur si prix HT ou taux TVA change (DEC-003) ; taux non fourni hérite de la (nouvelle) catégorie, sinon bornes + TauxTvaAutorises → 400 CAT_001. Unicité (group_id, code_article) re-vérifiée en autorisant l'article cible → 409 RES_002. Changement de catégorie → 404 uniforme. RBAC écriture (SEC_001 fail-fast). Snapshot-commandes inchangé (aucune ligne achat/vente existante). Pas de cache article à évicter en V1 (aucun cache branché). |
| US-035 | DELETE `/api/v1/articles/{id}` — archiver un article | P0 | ✅ | Terminé (même branche). Soft delete (`supprime = true` + `actif = false`) ; 404 RES_001 uniforme (inconnu / hors groupe). Garde 409 RES_005 « référencé par commande/vente » documentée mais non armée en V1 (les modules achat/vente n'existent pas encore — devient opérationnelle avec EPIC 7/9). RBAC écriture (SEC_001 fail-fast). |
| US-031 | POST `/api/v1/articles` — créer un article | P0 | ✅ | Terminé (branche `feature/GS-031-creer-article`). RBAC trio catalogue (403 SEC_001 fail-fast avant I/O), unicité (groupe, code_article) → 409 RES_002, catégorie chargée dans le groupe (404 RES_001 uniforme, jamais révéler l'existence), **TTC calculé côté serveur** via `CalculTvaService` (DEC-003, jamais saisi), taux TVA hérité de la catégorie si absent sinon bornes [0;100] + `TauxTvaAutorises` → 400 CAT_001, seuil 0 = alerte désactivée (DEC-023), marge brute % calculée à la volée (non persistée, négative autorisée — DEC-029 avertissement non bloquant, warning loggé). Champs DEC-013/DEC-012 non exposés par cet endpoint (US-103/US-104, défauts `@PrePersist` appliqués). 22 tests verts (15 service + 7 contrôleur) ; module catalogue 83/83 (2 skippés environnementaux). |

> ✅ EPICs 1 à 5 partiellement livrés : EPIC 5 a au moins ses 4 premières US (catégories) réalisées sur la branche `feature/GS-027-creer-categorie`, non encore mergée ni poussée.

---

## EPIC 6 — Tiers (Clients & Fournisseurs)

| US | Description | Priorité | Statut | Notes |
|----|-------------|--------|-------|-------|
| US-036 | POST `/api/v1/fournisseurs` — créer un fournisseur | P0 | ✅ | Terminé (branche `feature/GS-036-creer-fournisseur`). **RBAC** : `@PreAuthorize("hasAnyRole('RESP_ACHATS','ADMIN_FILIALE','ADMIN_GROUPE')")` sur la méthode contrôleur (convention `CategorieController`/`ArticleController`) + garde rôle fail-fast côté service (403 `SEC_001` avant toute I/O). **Isolation tenant** : `entreprise_id` extrait du JWT via `StockMasterPrincipal`, agrégat `shared` `Entreprise` + `EntrepriseRepository` (aucune nouvelle entité), entreprise introuvable/soft-supprimée/hors groupe → 404 `RES_001` uniforme ; le DTO `CreerFournisseurRequest` ne porte **aucun champ tenant** — un `entrepriseId` forcé dans le payload est ignoré (test dédié). **Validation CA3** : `raisonSociale` `@NotBlank` + `@Size(max=200)` (colonne V1), `telephone` optionnel en E.164 (regex identique à `InscriptionEntrepriseUniqueRequest`, validée par l'utilisateur le 27 oct. 2026) → 400 `SYS_005`. **Mapping** : MapStruct (`FournisseurMapper`, `componentModel=spring`) — `entreprise` ignoré à l'entrée, `entreprise.id` lu à la sortie. **Aucune migration** : la table `fournisseur` existe depuis `V1__init_schema.sql`. 30 tests verts (18 service + 10 contrôleur + 2 fondamentaux préexistants) ; `spring-security-test` ajouté au pom tiers (scope test, déjà présent sur catalogue/utilisateur). |
| US-037 | GET `/api/v1/fournisseurs?search=&page=&size=` — lister / rechercher | P0 | ✅ | Terminé (branche `feature/GS-037-lister-fournisseurs`). **RBAC** : `@PreAuthorize("hasAnyRole('RESP_ACHATS','ADMIN_FILIALE','ADMIN_GROUPE')")` (même trio qu'US-036 — **le backlog US-037 n'a aucun CA**, arbitrage G1 tranché par l'utilisateur le 27 oct. 2026) + garde rôle fail-fast côté service. **Isolation tenant** : `entreprise_id` du JWT porte la requête repository ; agrégat `Entreprise` validé en amont (miroir US-036 → 404 `RES_001` hors périmètre) ; aucun paramètre de tenant accepté (un `entrepriseId` forcé dans la query string est ignoré — test dédié). **Recherche** : `LOWER(raisonSociale) LIKE %x%` **OU** `LOWER(adresse_ville) LIKE %x%` (« nom ou ville » du user story), insensible à la casse, `search` trimé, **aucune normalisation d'accents** (convention `ArticleRepository.rechercherParGroupeEtTexte`) — JPQL `@Query` + `@Param`, pas de SQL natif. **`search` absent/blank** → listing complet paginé. **Pagination** : défaut `page=0`/`size=20`, `size` **borné à 100** (`stockmaster.pagination.max-page-size`) côté service — **clamp silencieux, pas de 400** (arbitrage G2, convention `FilialeService`/`ArticleService`), tri **fixe** `raisonSociale ASC` (pas de tri dynamique). **Réponse** : `ApiResponse<PageResponse<FournisseurResponse>>` (enveloppe `shared` US-017, aucun nouveau DTO de liste, mapping via `FournisseurMapper` MapStruct existant). **Aucune migration V7** : `LIKE '%x%'` inutilisable en B-tree, l'index de scoping `idx_fournisseur_entreprise` existe déjà (`V2__create_indexes.sql:14`) — `pg_trgm` signalé « à arbitrer » si volume. 19 tests verts (11 service + 8 contrôleur) ; module tiers **49/49**. |
| US-038 | PUT `/api/v1/fournisseurs/{id}` — modifier un fournisseur | P0 | ✅ | Terminé (branche `feature/GS-038-modifier-fournisseur`). **Le bloc backlog (L1185-1195) n'a AUCUN critère d'acceptation** → 9 arbitrages explicites validés par l'utilisateur le 28 oct. 2026, tous alignés sur le code existant. **RBAC (1.a/G1)** : `@PreAuthorize("hasAnyRole('RESP_ACHATS','ADMIN_FILIALE','ADMIN_GROUPE')")` + garde `verifierRole()` fail-fast (403 `SEC_001` avant toute I/O). **Sémantique (1.a)** : PATCH sémantique sur le PUT — convention dominante du repo (3 PUT sur 5 : `GroupeService.modifier` US-014, `FilialeService.modifier` US-018, `UtilisateurUpdateService.modifier` US-024 ; exceptions de remplacement complet : `CategorieService` US-029 et `ArticleService` US-034) — `StringUtils.hasText`, champ absent **ou blanc** = inchangé, corps vide `{}` valide et sans effet. **Champs modifiables (1.b)** : `raisonSociale`, `nif`, `contact`, `telephone`, `email`, `adresseVille` (les 6 de l'entité) ; non modifiables : `id` (chemin), `entreprise` (= `entreprise_id`, JWT — aucun champ dans le DTO), `dateCreation`, `dateModification` (trigger V3), `supprime`. **`raisonSociale` (1.c)** : optionnelle en PATCH, mais non blanche si fournie (`@Pattern(".*\\S.*")` + `@Size(max=200)`) → 400 `SYS_005`, la colonne restant `NOT NULL` (`V1__init_schema.sql:200`). **Téléphone (1.d)** : règle **extraite** dans `tiers/validation/TelephoneE164` (`REGEX` + `MESSAGE`), réutilisée par `CreerFournisseurRequest` **et** `ModifierFournisseurRequest` — plus de regex dupliquée ; la javadoc d'US-036 qui prétendait « même regex que `InscriptionEntrepriseUniqueRequest` » était **fausse** (ce DTO n'a aucun `@Pattern`) et a été corrigée. **Isolation tenant (1.e/1.f/1.h)** : `findByIdAndSupprimeFalse(id)` **puis** filtre `entreprise_id` du JWT — jamais de lookup par id seul ; id inconnu, soft-supprimé **et** autre tenant renvoient le **même 404 `RES_001`** (miroir `FilialeService`/`ArticleService`, aucune fuite d'existence, jamais 403 — `GRP_002` reste réservé aux vues group-scoped). **Validation (1.g)** : 400 `SYS_005` via le `GlobalExceptionHandler` de `shared`, rien de recréé. **Réponse (1.i)** : 200 + `ApiResponse<FournisseurResponse>` (état après modification), convention US-018/029. **Aucune migration Flyway** : les 6 champs existent depuis V1. **Aucune règle d'unicité NIF** (X1) : **aucun index UNIQUE** sur `fournisseur` (`V1:197-209`, `V2:14` seul index de scoping) et aucun CA — `existsByEntrepriseIdAndNifAndSupprimeFalse` reste sans appelant ; la javadoc de l'entité qui affirmait le contraire a été corrigée. 32 tests verts (20 service + 12 contrôleur), module tiers **81/81**, ArchUnit 4/4. |
| US-039 | DELETE `/api/v1/fournisseurs/{id}` — supprimer (archiver) un fournisseur | P0 | ✅ | Terminé (branche `feature/GS-039-supprimer-fournisseur`). **Le ticket #1116 contenait des CA contradictoires** → 2 contradictions et 7 ambiguïtés arbitrées par l'utilisateur le 28 oct. 2026. **Contradiction A (CA7 « historique conservé » vs CA10 « bloqué si commandes »)** → **option (c)** : `409` seulement si une commande est encore **à livrer** (`COMMANDEE` ou `PARTIELLEMENT_RECUE`, `DEC-006`) ; une commande `RECEPTIONNEE` ou `ANNULEE` **n'empêche pas** l'archivage (CA5 + CA7 réconciliés). **Contradiction B (CA3 `ligne.receptionne` vs CA5)** → **CA3 abandonné** : la colonne n'existe ni sur `LigneCommandeFournisseur` ni dans `V1__init_schema.sql:228-240` ; le critère est dérivé de l'état de l'en-tête. **Dépendance inversée (DEC-019 + ArchUnit règle 1)** : `tiers` n'a pas le droit d'importer `achat` → port `com.stockmaster.tiers.port.CommandesFournisseurPort` (contrat exposé par tiers, comme `notification.port.CanalNotification`) implémenté par `com.stockmaster.achat.adapter.CommandesFournisseurAdapter` ; requête `existsByFournisseurIdAndEtatCommandeInAndSupprimeFalse` ajoutée à `CommandeFournisseurRepository` (id fournisseur unique plateforme, déjà contrôlé tenant par le service). **RBAC** : trio US-036/037/038 `RESP_ACHATS/ADMIN_FILIALE/ADMIN_GROUPE` (le backlog US-039 n'a **aucun CA de rôle**) + garde `verifierRole()` fail-fast. **Soft delete uniquement** : `supprime = true`, ligne conservée — test dédié vérifiant que `delete*` n'est **jamais** appelé ; les FK `commande_fournisseur` (`ON DELETE RESTRICT`) et l'historique sont préservés (CA7). **Isolation tenant** : `findByIdAndSupprimeFalse` + filtre `entreprise_id` du JWT ; id inconnu, **hors tenant** et **déjà archivé** renvoient le même **404 `RES_001`** (aucune fuite, test IDOR dédié). **Réponse (1.c)** : `200` + `ApiResponse<FournisseurResponse>`, convention US-029/US-035 (aucun `204` dans le repo) ; champ **`supprime` ajouté** au `FournisseurResponse` (additif — sans lui le client ne distinguerait pas un archivage d'une modification). **Code d'erreur (1.d)** : `RES_ENTITY_HAS_DEPENDENCIES` = **`RES_005`** existait déjà (`ErrorCode.java:32`, utilisé par `CategorieService`) → rien créé. **Aucune migration Flyway (1.e)** : `supprime` existe depuis V1 (`V1:208`) et **aucune contrainte CHECK n'a jamais existé** sur `fournisseur` (vérifié `\d fournisseur`) — la prémisse du ticket était fausse. **US-037 (1.f)** : aucun impact, le listing exclut déjà `supprime = true`. 21 tests verts (11 service + 10 contrôleur) + 3 pour l'adaptateur achat ; tiers **70/70**, achat **6/6**, ArchUnit 4/4. |
| US-040 → US-043 | CRUD clients | P0 | ❌ | Non commencé |

---

## EPIC 8 — Gestion du Stock (en cours)

| US | Description | Priorité | Statut | Notes |
|----|-------------|----------|--------|-------|
| US-051 | GET `/api/v1/stock` — consulter le stock réel par article | P0 | ✅ | Terminé (branche `feature/GS-051-consultation-stock`). **`StockController` + `StockService`** (premiers du module) : `GET /api/v1/stock?statutAlerte=&page=&size=` → `ApiResponse<PageResponse<StockArticleResponse>>`. **Aucun `@PreAuthorize`** (CA « tout utilisateur authentifié », comme US-032) + garde service fail-fast (groupId absent → 403 GRP_002, entrepriseId absent → 403 SEC_001, groupe absent/inactif → 403 GRP_002 — pattern catalogue). Requête native `MouvementStockRepository.listerStockParArticle` : articles du groupe **LEFT JOIN** agrégat signé des mouvements de l'entreprise (même CASE que `calculerStockReel` — 8 types REF §4.2) ; le **`statutAlerte` est dérivé en SQL** (miroir de `StatutAlerte.de`) sinon le filtre `?statutAlerte=BAS` aurait cassé la pagination (filtrage après coup). Nouvel enum **`StatutAlerte`** (NORMAL/BAS/RUPTURE/ANOMALIE) + `StatutAlerteTest` (8 tests). **Décision GO utilisateur** : stock 0 (article sans mouvement) → `RUPTURE`, lecture littérale REF §4.2. Taille bornée à `stockmaster.pagination.max-page-size`. 11 tests service + 5 tests contrôleur. Validation : reactor **545 tests verts**, ArchUnit 4/4, intégration 3/3, **et test manuel réel contre l'app lancée** : stock 0 → RUPTURE, entrée de 5 (seuil 10) → BAS, filtres SQL conformes, `?statutAlerte=XYZ` → 400 SYS_002, CAISSIER → 200 sur US-051. |
| US-052 | GET `/api/v1/stock/articles/{id}/mouvements` — historique des mouvements | P0 | ✅ | Terminé (même branche). `@PreAuthorize('GESTIONNAIRE_STOCK','ADMIN_FILIALE','ADMIN_GROUPE')` + garde rôle en défense en profondeur (RESP_ACHATS/CAISSIER → 403 SEC_001 avant toute I/O). **404 uniforme `RES_001`** si l'article est inconnu ou hors du groupe du JWT, via `articleExisteDansGroupe` (requête native sur la couche **contrat** `catalogue.domain.entity` — ArchUnit n°1 respectée, `ArticleRepository` jamais référencé). Historique paginé `listerMouvementsParArticle` trié **`date_mouvement DESC, id DESC` en SQL** (Pageable sans tri pour éviter un double ORDER BY), filtres `typeMouvement`/`dateDebut`/`dateFin` (dates inclusives, n bornes en UTC) via **sentinelles non nulles** (`""`, `EPOCH`, `2100-01-01`) — aucun paramètre NULL côté JDBC. `LEFT JOIN utilisateur` pour retourner `utilisateurNom` (« prénom nom ») sans charger l'entité ; mapping `Timestamp → Instant`. 13 tests service + 6 tests contrôleur. Validé en réel : 200 avec jointure, `typeMouvement=SORTIE` → vide, plage du jour → 1 ligne, plage 2020 → vide, `dateDebut > dateFin` → 400, sans token → 401/403, CAISSIER → 403 SEC_001. |

> ⚠️ **Reste à livrer dans l'EPIC 8** : US-053/US-054 (corrections), US-055 (stock consolidé). Le **port d'écriture `EcritureStockPort`** (contrat pour achat/vente, whitelist ArchUnit) est délibérément **reporté au ticket suivant** — décision GO du 27 oct. 2026.
>
> **Prérequis de données** : l'EPIC 7 (US-048) n'est pas encore livrée — les mouvements réels n'existeront qu'à la réception des commandes fournisseur. Le module est testable dès aujourd'hui (tests + données de test).

> *Dernière mise à jour : 27 octobre 2026 — EPIC 8 : US-051/US-052 livrées (contrôleurs stock + service + requêtes natives), 545 tests verts (stock 20 → 63), branche `feature/GS-051-consultation-stock`*

---

## EPIC 7 et 9 à 17 — (non commencé)

---

## Frontend — EPICs

### EPIC-F00 — Fondations techniques (41 SP)

| US | Description | SP | Statut | Notes |
|----|-------------|----|--------|-------|
| US-F001 | Init projet frontend Vite + React + TS | 3 | ✅ | Fait |
| US-F002 | Design system Tailwind / tokens | 5 | ✅ | Tokens StockMaster — cf. DESIGN_CORRECTIONS.md |
| US-F003 | Composants partagés (Button, Input, Table...) | 8 | ✅ | 15 composants (6 améliorés + 9 nouveaux) |
| US-F004 | Client API Axios + intercepteur JWT | 5 | ✅ | Intercepteur refresh + file d'attente |
| US-F005 | Routing + Guards RBAC | 5 | ✅ | Layouts AuthLayout + DashboardLayout |
| US-F006 | Auth state (Zustand) | 3 | ✅ | Store auth + AuthProvider |
| US-F007 | Layout par rôle (sidebar/menu) | 5 | ✅ | Menu filtré par rôle, responsive |

### EPIC-F01 — Onboarding public (24 SP)

| US | Description | SP | Statut | Notes |
|----|-------------|----|--------|-------|
| US-F010 | Page d'accueil publique | 2 | ✅ | AccueilPage.tsx (461 lignes, sections features/stats), routée sur `/` — corrigé après vérification code réel (était marquée ❌ par erreur) |
| US-F011 | Écran de choix du type d'inscription | 2 | ✅ | InscriptionChoixPage — 2 cartes avec features |
| US-F012 | Formulaire Entreprise Unique | 5 | ✅ | Stepper 3 étapes + validation Zod + API |
| US-F013 | Formulaire Groupe multi-sites | 5 | ✅ | Stepper 4 étapes + validation Zod + API |
| US-F014 | Écran de connexion | 3 | ✅ | LoginPage — validation, démos, toast |
| US-F015 | Flow mot de passe oublié + réinitialisation | 5 | ✅ | ForgotPasswordPage + ResetPasswordPage |
| US-F016 | Écran d'activation de compte | 2 | ❌ | Pas implémenté (P1) |

---

## Design — EPIC-D00 (Design System StockMaster)

| US | Description | SP | Statut | Notes |
|----|-------------|----|--------|-------|
| US-D001 | Palette de couleurs | 3 | ✅ | Palette StockMaster complète |
| US-D002 | Typographie | 2 | ✅ | font-display/body/mono |
| US-D003 | Composants atomiques | 6 | ✅ | 15 composants (6 existants + 9 nouveaux) |
| US-D004 | Badges d'état | 3 | ✅ | 10 variants + dot/removable |
| US-D005 | Grilles responsive | 2 | ✅ | Breakpoints xs→2xl + 8px grid |
| US-D006 | Icônes | 2 | ✅ | Lucide React intégré |
| US-D007 | Templates d'état | 5 | ✅ | EmptyState (5) + Skeleton (5) + OfflineBanner |

**Total EPIC-D00 : 23/23 SP** ✅

---

## Design System — Fichier de référence

Les fichiers `DESIGN_CORRECTIONS.md` (25 corrections) et `DESIGN_TOKENS_REFERENCE.md` (guide complet) documentent l'ensemble du design system.

---

---

## Backend — Fondamentaux des 9 modules fonctionnels (session 14/09/2026)

Pose des fondamentaux module par module (entités alignées V5, repositories scoping tenant, enums de machines à états, services métier purs + tests TDD). Les développeurs n'ont plus qu'à implémenter services applicatifs, contrôleurs, mappers et cas d'usage US.

| Élément | Contenu | Statut |
|---------|---------|--------|
| Agrégats tenant | `TenantGroup`/`Entreprise`/`Utilisateur` + enums + repos déplacés de `auth` vers `shared` (tous modules référencent sans dépendre de auth) ; alignés V5 : `limite_utilisateurs`, `site_operationnel`, `code_filiale NOT NULL`, `email_verifie`, colonnes `token_reset` supprimées, `PlanAbonnement` 3 valeurs (DEC-015) | ✅ |
| catalogue | `Categorie`/`Article` niveau GROUPE (DEC-002), XAF entiers + TTC calculé (DEC-003), 2 unités + facteur (DEC-013), lot-ready (DEC-012) ; `CalculTvaService` + 12 tests | ✅ |
| tiers | `Client`/`Fournisseur` périmètre filiale + repos (unicité NIF, batch) | ✅ |
| achat | `CommandeFournisseur`(+lignes), machine à états DEC-006 (`COMMANDEE→PARTIELLEMENT_RECUE→RECEPTIONNEE\|ANNULEE`), quantités DECIMAL(12,3) | ✅ |
| vente | `Vente`/`LigneVente` (statut `PAYEE→ANNULEE\|REMBOURSEE`, client nullable, session DEC-018), `SessionCaisse` (écart anti-perte), `Paiement` mixte (DEC-009), `CommandeClient` (+règlement DEC-011/020) + 6 repos | ✅ |
| stock | `MouvementStock` journal immuable partitionné (C-12/DEC-033, PK composite documentée), `TransfertStock` multi-lignes groupe + FK composites (DEC-007), inventaire DEC-036, `CleIdempotence` JSONB (DEC-027) ; `CalculStockService` signe map + rupture `≤ seuil` + invariant non-négatif + 10 tests | ✅ |
| notification | `NotificationAlerte` refondue (destinataire explicite, type SANS CHECK — DEC-004/DEC-037) ; port `CanalNotification` (DEC-014) prêt pour EPIC 12 (email bienvenue/vérification AUTH-07) | ✅ |
| groupe / utilisateur / reporting | `ControleLimiteFilialesService` (sites non opérationnels exclus), `ControleLimiteUtilisateursService` (10/50/négocié), `CalculCaService` (CA HT facturé, TVA, TTC — DEC-020) + 11 tests | ✅ |
| **V5 validée en conditions réelles** | Flyway V1→V5 exécutée **pour de vrai** sur PostgreSQL 16 vierge (conteneur jetable port 5433) via le test d'intégration bootstrap + `ddl-auto=validate` OK sur les entités — dette de validation de V5 soldée | ✅ |
| Qualité | `mvnw test` : **125/125 verts** (dont 3 tests d'intégration démarrage complet) ; CI `mvn verify` couvre tous les modules, rien à changer | ✅ |
| Correction config | Profil `test` : datasource passée en `${DB_HOST}/${DB_PORT}/${DB_NAME}/${DB_USERNAME}/${DB_PASSWORD}` (comme les autres profils) — il était le seul codé en dur sur 5432 | ✅ |

**Note machine locale (gestionulrich)** : un PostgreSQL natif Windows écoute `localhost:5432` avec des identifiants qui ne sont pas ceux documentés ; le conteneur compose ne peut pas publier son port (règle de bind Windows) et sa base n'a PAS été migrée ni effacée. Pour tester en local : `DB_PORT=5433 ./mvnw test` avec un conteneur jetable, ou arrêter le service natif. CI non concernée (elle provisionne son propre PG).

---

---

## Backend — Règles d'architecture automatisées (ArchUnit)

`ModuleDependencyArchTest` (dans bootstrap, seul module voyant tout le graphe) exécute 4 règles à chaque build :

1. **Dépendances inter-modules** : seuls `shared` et la couche contrat (`domain.entity`, `domain.enums`, `event`, `port`) de catalogue/tiers/notification sont autorisés — les `service`/`repository`/`controller`/`dto`/`mapper`/`config` d'un autre module sont interdits (les FK de documents → `catalogue.Article`, `tiers.Client`… rendent le contrat inter-modules nécessaire, DEC-002).
2. **shared est la base du graphe** : ne dépend d'aucun module fonctionnel.
3. **auth est une feuille** : ne consomme aucun autre module (l'email passe par événements).
4. **notification.service encapsulé** : les émetteurs passent par `CanalNotification` (`notification.port`, DEC-014).

Ajustements d'accompagnement : `CanalNotification` déplacé `service` → `port` (un port est fait pour être consommé inter-modules) ; `CalculTvaService` (DEC-003) déplacé catalogue → `shared.service` (règle universelle, utile partout) ; `archunit-junit5` (version déjà gérée par le parent) déclaré dans bootstrap ; surefire avec `-Djdk.attach.allowAttachSelf=true` (fixe l'échec intermittent d'auto-attache ByteBuddy/Mockito sur Windows, qui faisait échouer ~50 % des builds locaux).

---

## Backend — Fix CI : colonne fantôme + schéma validé en local (14/09/2026)

La CI (profil dev, `ddl-auto=validate`) a attrapé un drift que les tests locaux laissaient passer (profil test, `ddl-auto=none`) :

- `CommandeFournisseur` mappait `utilisateur_id`, colonne qui n'existe **ni en V1 ni en V5** (REF §6.2) — champ supprimé ; la traçabilité du créateur passe par le journal `mouvement_stock.utilisateur_id`
- **nouveau profil `integration`** (Flyway + `ddl-auto=validate`) utilisé par `StockMasterApplicationTest` : le test de démarrage vérifie désormais l'exactitude schéma↔entités **en local, à chaque build** — un écart comme celui-ci échoue au commit, plus seulement en CI
- vérifié : Flyway V1→V5 + validate OK, **0 « missing column »**, 151/151 tests verts (dont 4 ArchUnit)

*Dernière mise à jour : 14 septembre 2026 — fix CI (colonne fantôme, profil integration), 151/151 verts, branche `feature/GS-085-fail-closed-redis`*

---

## Qualité — merge main + couverture SonarCloud (14/09/2026)

**Merge de `main` (PR #18) résolu** : V5 gardé côté branche (version exécutée et validée : reconstructions CHECK DEC-006, migrations `plan_abonnement`, contrainte `uq_entreprise_id_group` alignée sur les FK composites) ; `Utilisateur` gardé côté branche (champs `token_reset` supprimés, DEC-024 — pas de code commenté mort). Vérifié avant commit : reactor complet, 0 « missing column ».

**Quality gate SonarCloud** (Coverage on New Code ≥ 80 %, alors 48,6 %) — deux actions :

- `backend/lombok.config` : `lombok.addLombokGeneratedAnnotation = true` → les méthodes générées Lombok (~60 classes d'entités) portent `@Generated` et sont exclues du calcul de couverture (JaCoCo honore l'annotation) — la vraie logique métier n'est plus noyée dans le code généré
- **+55 tests** couvrant ce qui était réellement découvert et compté : fabriques `ProblemResponse` (RFC 7807), hiérarchie d'exceptions (`BusinessException`, `EntityNotFoundException`, `InsufficientStockException`), défauts `@PrePersist` de tous les agrégats tenant/stock/vente/achat/catalogue/notification, énumérations verrouillées (7 rôles, 3 plans DEC-015, 8 types de mouvement DEC-023, machines d'état DEC-006/007/011), premiers tests de vente/achat/tiers/notification (0 test auparavant)
- reactor : **206/206 tests verts** (13 modules), integration test + 4 règles ArchUnit incluses
- reste hors couverture : packages déjà exclus par Sonar (`config/**`, `dto/**`), quatre modules sans logique de service encore (interfaces+entités posés, implémentations à venir par US)

---

## Stock — Port d'écriture `EcritureStockPort` (GS-1240, 28/10/2026)

Terminé (branche `feature/GS-1240-port-ecriture-stock`, ticket **#1240**). EPIC 8 n'a pas encore de contrôleur (US-052→054 à venir) : ce ticket pose l'**unique porte d'entrée d'écriture** du stock, le contrat que consommeront achat (réception US-049), vente (US-064) et transferts (US-068) — qui n'ont accès ni à `stock.repository` ni à `stock.service` (rule ArchUnit n°1).

| Élément | Contenu | Statut |
|---------|---------|--------|
| Contrat | `stock.port.EcritureStockPort` autoportant : record `RequeteMouvement` (entreprise/article/utilisateur, `typeMouvement`, `quantite > 0` — le sens est porté par le type, jamais par le signe, `origineType/origineId`, `motif`) et record résultat `MouvementEnregistre` (`mouvementId` + `stockApres` recalculé **dans la même transaction** sous le même verrou — l'appelant ne relit jamais le journal). S'appuie uniquement sur les enums `stock.domain.enums` (couche contrat autorisée) | ✅ |
| Sérialisation explicite | **DEC-023 addendum** : le stock étant calculé à la volée (ADR-003), il n'y a aucune ligne de stock à verrouiller → `pg_advisory_xact_lock(:entrepriseId, :articleId)` (`MouvementStockRepository.verrouilleCouple`) appelé **avant toute lecture du stock** ; deux ventes simultanées du dernier article ne peuvent plus passer toutes deux avec `stock = 1` | ✅ |
| Isolation multi-tenant | Une seule requête native `MouvementStockRepository.referenceValide` : article **actif** du même groupe que l'entreprise **ET** utilisateur actif de cette entreprise — sinon 404 `RES_001` uniforme (pattern US-033/US-052, jamais révéler l'existence). Aucune FK paresseuse lue avant la validation | ✅ |
| Invariants métier | Stock **jamais négatif** : toute écriture décrémentante sous 0 → `InsufficientStockException` (409 `STK_001`), rien n'est écrit (chapron rollback). Journal **immuable** (insertion seule, V5 §12). `motif` obligatoire sur `CORRECTION_*` → `STK_002` ; cohérence `CORRECTION_POS/CORRECTION_NEG → origineType = CORRECTION` otherwise `SYS_BAD_REQUEST` | ✅ |
| ArchUnit | Whitelist règle 1 étendue : + `stock.port..` et `stock.domain.enums..` (miroir de catalogue/tiers/noti) — 4 règles vertes | ✅ |
| Tests | 12 nouveaux tests Mockito (`EcritureStockServiceTest`, pattern `StockServiceTest` : `Nominatif`, `ValidationChamps`, `ReglesCorrection`, `IsolationMultiTenant`, `InvariantNonNegatif`) — module stock **20 → 32** ; reactor complet **577/577 verts** (13 modules, dont bootstrap 7 : 3 intégration `validate` + 4 ArchUnit) | ✅ |

*Dernière mise à jour : 19 septembre 2026 — EPIC 4 : US-026 livrée (consulter/modifier son profil avec upload photo MinIO, +14 tests), 421/421 verts, branche `feature/GS-026-profil-utilisateur`*
