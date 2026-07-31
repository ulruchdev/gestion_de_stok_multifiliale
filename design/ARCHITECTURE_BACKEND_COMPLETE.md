# 🏗️ Architecture Backend — StockMaster CM (Schéma Complet)

> **Référence :** GS-ARCH-2026-01 | **Version :** 1.0 | **Date :** Juillet 2026  
> **Conformité :** Backlog 13 EPICs, CDCT sections 21-30, décisions GS-CDA-2026-02

> ⚠️ **Statut d'implémentation :** ce document décrit l'architecture **cible**. Seuls `stockmaster-shared`, `stockmaster-auth` et `stockmaster-bootstrap` contiennent du code réel à ce jour — tous les autres modules référencés ici (`groupe`, `utilisateur`, `catalogue`, `tiers`, `achat`, `stock`, `vente`, `notification`, `reporting`) sont des stubs vides (arborescence de packages sans classes). Voir `knowledge.md` et `document/03-pilotage/progress-ledger.md` pour l'état réel.

---

## 1. Vue d'ensemble — Monolithe Modulaire (11 + 2 modules)

```
gestionulrich/
└── backend/
    ├── pom.xml                                    ← Parent POM (Spring Boot 3.3.5, Java 21)
    │                                                <dependencyManagement> centralisé
    │                                                Plugins : JaCoCo, Sonar, Flyway, Spring Boot
    │
    ├── stockmaster-shared/                        ← 🔷 Infrastructure commune (EPIC 1)
    ├── stockmaster-auth/                          ← 🔐 Authentification & Accès (EPIC 2)
    ├── stockmaster-groupe/                        ← 🏢 Groupe & Filiales (EPIC 3)
    ├── stockmaster-utilisateur/                   ← 👥 Gestion des Utilisateurs (EPIC 4)
    ├── stockmaster-catalogue/                     ← 📦 Catalogue (EPIC 5)
    ├── stockmaster-tiers/                         ← 🤝 Tiers Clients & Fournisseurs (EPIC 6)
    ├── stockmaster-achat/                         ← 📥 Commandes Fournisseur (EPIC 7)
    ├── stockmaster-stock/                         ← 📊 Gestion du Stock (EPIC 8 + 11)
    ├── stockmaster-vente/                         ← 🛒 Ventes B2B & Caisse (EPIC 9 + 10)
    ├── stockmaster-notification/                  ← 🔔 Notifications & Alertes (EPIC 12)
    ├── stockmaster-reporting/                     ← 📈 Statistiques & Reporting (EPIC 13)
    └── stockmaster-bootstrap/                     ← 🚀 Point d'entrée Spring Boot
```

---

## 2. Règles d'architecture strictes

| Règle | Description |
|---|---|
| **R1** | `spring-boot-maven-plugin` UNIQUEMENT dans `stockmaster-bootstrap` |
| **R2** | Tous les modules POM enfants ont `<relativePath>../pom.xml</relativePath>` |
| **R3** | Les dépendances inter-modules sont gérées par `<dependencyManagement>` dans le parent |
| **R4** | `stockmaster-shared` est une **library** — pas de `spring-boot-maven-plugin`, `<skip>true</skip>` |
| **R5** | Les modules vides (stubs) NE SONT PAS déclarés dans `bootstrap/pom.xml` |
| **R6** | Tous les modules suivent le même squelette de packages (sauf `shared` et `bootstrap`) |
| **R7** | Chaque module a ses propres migrations Flyway (dans `src/main/resources/db/migration/{module}/`) |
| **R8** | `entreprise_id` vient UNIQUEMENT du JWT (jamais du body) — isolation multi-tenant |

---

## 3. Squelette de package — Chaque module fonctionnel

Chaque module (sauf `shared` et `bootstrap`) suit cette structure :

```
stockmaster-{module}/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/stockmaster/{module}/
    │   │   ├── config/                  ← Configuration spécifique au module
    │   │   │   ├── {Module}Config.java
    │   │   │   └── {Module}Properties.java
    │   │   │
    │   │   ├── controller/              ← API REST (Spring MVC)
    │   │   │   ├── {Entite}Controller.java
    │   │   │   └── {Entite}Controller.java
    │   │   │
    │   │   ├── domain/
    │   │   │   ├── entity/              ← Entités JPA
    │   │   │   │   ├── {Entite}.java
    │   │   │   │   └── {Entite}.java
    │   │   │   ├── enums/               ← Énumérations métier
    │   │   │   │   └── {Enum}.java
    │   │   │   └── vo/                  ← Value Objects (DDD)
    │   │   │       └── {Vo}.java
    │   │   │
    │   │   ├── dto/
    │   │   │   ├── request/             ← DTOs d'entrée (requêtes)
    │   │   │   │   ├── Creer{Entite}Request.java
    │   │   │   │   ├── Modifier{Entite}Request.java
    │   │   │   │   └── Filtrer{Entite}Request.java
    │   │   │   └── response/            ← DTOs de sortie (réponses)
    │   │   │       ├── {Entite}Response.java
    │   │   │       └── {Entite}ListResponse.java
    │   │   │
    │   │   ├── mapper/                  ← MapStruct
    │   │   │   └── {Module}Mapper.java
    │   │   │
    │   │   ├── repository/              ← Spring Data JPA
    │   │   │   ├── {Entite}Repository.java
    │   │   │   └── {Entite}Repository.java
    │   │   │
    │   │   ├── service/
    │   │   │   ├── {Domaine}Service.java     ← Interface
    │   │   │   ├── impl/
    │   │   │   │   └── {Domaine}ServiceImpl.java
    │   │   │   └── validator/                ← Validateurs métier
    │   │   │       └── {Entite}Validator.java
    │   │   │
    │   │   ├── event/                   ← Événements Spring (asynchrone)
    │   │   │   ├── {Entite}CreatedEvent.java
    │   │   │   └── {Entite}UpdatedEvent.java
    │   │   │
    │   │   ├── exception/               ← Exceptions spécifiques au module
    │   │   │   └── {Module}Exception.java
    │   │   │
    │   │   └── spec/                    ← Specifications JPA (recherche dynamique)
    │   │       └── {Entite}Spec.java
    │   │
    │   └── resources/
    │       ├── application-{module}.yml    ← Config surchargeable
    │       └── db/migration/{module}/     ← Migrations Flyway du module
    │           ├── V4__{description}.sql
    │           └── V4_rollback_{description}.sql
    │
    └── test/
        └── java/com/stockmaster/{module}/
            ├── controller/
            │   └── {Entite}ControllerTest.java
            ├── service/
            │   └── impl/
            │       └── {Entite}ServiceImplTest.java
            ├── repository/
            │   └── {Entite}RepositoryTest.java
            └── mapper/
                └── {Module}MapperTest.java
```

---

## 4. Détail par module — Packages complets

### 4.1 `stockmaster-shared` — Infrastructure commune (EPIC 1)

```
stockmaster-shared/src/main/java/com/stockmaster/shared/
├── config/
│   ├── CorsProperties.java              ← @ConfigurationProperties(prefix = "stockmaster.cors")
│   ├── JwtProperties.java               ← @ConfigurationProperties(prefix = "stockmaster.jwt")
│   ├── PaginationProperties.java        ← @ConfigurationProperties(prefix = "stockmaster.pagination")
│   ├── RateLimitProperties.java         ← @ConfigurationProperties(prefix = "stockmaster.rate-limiting")
│   ├── RedisProperties.java             ← @ConfigurationProperties(prefix = "stockmaster.redis")
│   ├── MinioProperties.java             ← @ConfigurationProperties(prefix = "stockmaster.minio")
│   ├── MailProperties.java              ← @ConfigurationProperties(prefix = "stockmaster.mail")
│   ├── TenantProperties.java            ← @ConfigurationProperties(prefix = "stockmaster.tenant")
│   ├── AsyncConfig.java                 ← @EnableAsync, Executor configuré
│   ├── CacheConfig.java                 ← @EnableCaching, RedisCacheManager
│   └── WebConfig.java                   ← CORS, intercepteurs, Jackson
│
├── dto/
│   ├── request/
│   │   ├── PageRequest.java             ← Pagination générique
│   │   └── SearchRequest.java           ← Recherche générique
│   └── response/
│       ├── ApiResponse<T>.java          ← Enveloppe de réponse standard
│       ├── PageResponse<T>.java          ← Pagination générique
│       └── ProblemResponse.java         ← RFC 7807 (type, title, status, detail, instance, errorCode, errors[])
│
├── entity/
│   ├── AbstractEntity.java              ← @MappedSuperclass (id, dateCreation, dateModification, supprime)
│   └── AbstractAuditableEntity.java     ← + @CreatedBy, @LastModifiedBy (pour audit)
│
├── exception/
│   ├── ErrorCode.java                   ← Enum complet : AUTH_*, RES_*, CMD_*, GRP_*, STK_*, SEC_*, SYS_*
│   ├── BusinessException.java           ← Erreur métier (contient ErrorCode)
│   ├── EntityNotFoundException.java     ← 404 avec message paramétré
│   ├── InsufficientStockException.java  ← Stock insuffisant
│   ├── DuplicateResourceException.java  ← 409 Conflit (email, code, etc.)
│   ├── InvalidStateException.java       ← 409 Mauvaise transition d'état
│   └── TenantIsolationException.java    ← 403 Violation isolation
│
├── handler/
│   ├── GlobalExceptionHandler.java      ← @ControllerAdvice (toutes les exceptions)
│   ├── ValidationHandler.java           ← @Valid → MethodArgumentNotValidException
│   └── SecurityExceptionHandler.java    ← AccessDeniedException → 403 formaté
│
├── interceptor/
│   ├── TenantInterceptor.java           ← Vérifie entreprise_id dans chaque requête
│   ├── AuditLogInterceptor.java         ← Log structuré des opérations
│   └── RequestTimingInterceptor.java    ← Mesure temps de réponse (monitoring)
│
├── util/
│   ├── EncryptionUtil.java              ← AES/GCM pour données sensibles
│   ├── MoneyUtil.java                   ← Calculs monétaires (BigDecimal, TVA)
│   └── CodeGenerator.java              ← Génération codes (CF-2026-00001, etc.)
│
└── annotation/
    ├── RateLimited.java                 ← @RateLimited(endpoint = "login", maxAttempts = 5)
    └── RequireTenant.java               ← @RequireTenant (vérification isolation)
```

---

### 4.2 `stockmaster-auth` — Authentification & Accès (EPIC 2)

```
stockmaster-auth/src/main/java/com/stockmaster/auth/
├── config/
│   ├── JwtTokenProvider.java            ← Création/validation JWT (jjwt 0.12.6, HS256)
│   ├── JwtAuthenticationFilter.java     ← OncePerRequestFilter (extraction JWT → SecurityContext)
│   ├── RateLimitFilter.java             ← Global + per-endpoint rate limiting (Redis)
│   ├── SecurityConfig.java              ← SecurityFilterChain, CORS, CSRF, permitAll()
│   └── StockMasterPrincipal.java        ← Implémentation UserDetails + claims custom
│
├── controller/
│   └── AuthController.java              ← /api/v1/auth/* (inscription, login, refresh, logout, reset...)
│
├── domain/
│   ├── entity/
│   │   ├── Utilisateur.java             ← Compte utilisateur (email, motDePasse, actif)
│   │   ├── Entreprise.java              ← Filiale/Point de vente
│   │   ├── TenantGroup.java             ← Groupe multi-sites
│   │   ├── ResetToken.java              ← Token de reset password (UUID, expiration, utilisé)
│   │   └── AuditLog.java                ← Log structuré des événements auth
│   └── enums/
│       ├── RoleUtilisateur.java         ← ADMIN_GROUPE, ADMIN_FILIALE, GESTIONNAIRE_STOCK, RESP_ACHATS, COMMERCIAL, CAISSIER
│       ├── ScopeUtilisateur.java        ← GROUPE, FILIALE
│       ├── TypeEntreprise.java          ← MERE, FILIALE
│       └── PlanAbonnement.java          ← ESSENTIAL, BUSINESS, ENTERPRISE
│
├── dto/
│   ├── request/
│   │   ├── InscriptionEntrepriseUniqueRequest.java
│   │   ├── InscriptionGroupeRequest.java
│   │   ├── LoginRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── ForgotPasswordRequest.java
│   │   ├── ResetPasswordRequest.java
│   │   └── ChangePasswordRequest.java
│   └── response/
│       ├── InscriptionResponse.java
│       ├── LoginResponse.java
│       └── RefreshTokenResponse.java
│
├── mapper/
│   └── AuthMapper.java                  ← MapStruct (Entité → DTO, Request → Entité)
│
├── repository/
│   ├── UtilisateurRepository.java
│   ├── EntrepriseRepository.java
│   ├── TenantGroupRepository.java
│   └── ResetTokenRepository.java
│
├── service/
│   ├── AuthService.java                 ← Interface
│   ├── impl/
│   │   └── AuthServiceImpl.java         ← Inscription, login, refresh, logout, reset, changePassword
│   ├── TokenService.java                ← Gestion des tokens JWT + refresh (Redis)
│   ├── RateLimitService.java            ← Comptage tentatives + blacklist IP temporaire
│   └── validator/
│       └── PasswordValidator.java       ← Validation robustesse mot de passe
│
├── event/
│   ├── InscriptionSuccessEvent.java     ← Publié après création compte
│   ├── LoginSuccessEvent.java
│   ├── LoginFailedEvent.java
│   └── PasswordChangedEvent.java
│
└── listener/
    └── AuthEventLogger.java             ← Log structuré de tous les événements auth
```

---

### 4.3 `stockmaster-groupe` — Groupe & Filiales (EPIC 3)

```
stockmaster-groupe/src/main/java/com/stockmaster/groupe/
├── controller/
│   ├── GroupeController.java            ← GET/PUT /api/v1/groupe
│   ├── FilialeController.java           ← CRUD /api/v1/groupe/filiales
│   └── GroupeDashboardController.java   ← GET /api/v1/groupe/dashboard
│
├── domain/
│   ├── entity/
│   │   ├── Groupe.java                  ← Extension TenantGroup (logo, plan)
│   │   └── Filiale.java                 ← Extension Entreprise avec infos spécifiques
│   └── enums/
│       └── StatutFiliale.java           ← ACTIVE, SUSPENDUE
│
├── dto/
│   ├── request/
│   │   ├── CreerFilialeRequest.java     ← nom, ville, quartier, codeFiliale
│   │   ├── ModifierFilialeRequest.java
│   │   ├── ModifierGroupeRequest.java
│   │   └── ModifierProfilEntrepriseRequest.java  ← ville, quartier, rue, region, pays
│   └── response/
│       ├── GroupeResponse.java
│       ├── FilialeResponse.java
│       └── GroupeDashboardResponse.java
│
├── mapper/
│   └── GroupeMapper.java
│
├── repository/
│   ├── GroupeRepository.java
│   └── FilialeRepository.java
│
├── service/
│   ├── GroupeService.java
│   ├── FilialeService.java
│   ├── GroupeDashboardService.java      ← Stock consolidé + CA global + alertes
│   └── validator/
│       ├── FilialeValidator.java         ← Vérification limite plan + code unique
│       └── GroupeValidator.java
│
└── event/
    ├── FilialeCreatedEvent.java
    └── FilialeStatusChangedEvent.java
```

---

### 4.4 `stockmaster-utilisateur` — Gestion des Utilisateurs (EPIC 4)

```
stockmaster-utilisateur/src/main/java/com/stockmaster/utilisateur/
├── controller/
│   ├── AdminFilialeController.java      ← POST /api/v1/utilisateurs/admin-filiale
│   ├── EmployeController.java           ← CRUD /api/v1/utilisateurs/employes
│   ├── UtilisateurController.java       ← Liste, modification, désactivation
│   └── ProfilController.java            ← GET/PUT /api/v1/utilisateurs/profil
│
├── domain/
│   ├── entity/
│   │   ├── Employe.java                 ← Extension Utilisateur avec rôle métier
│   │   └── Invitation.java              ← Token d'invitation (UUID, expiration 48h)
│   └── enums/
│       └── RoleEmploye.java             ← GESTIONNAIRE_STOCK, RESP_ACHATS, COMMERCIAL, CAISSIER
│
├── dto/
│   ├── request/
│   │   ├── CreerAdminFilialeRequest.java
│   │   ├── CreerEmployeRequest.java
│   │   ├── ModifierUtilisateurRequest.java
│   │   └── ModifierProfilRequest.java
│   └── response/
│       ├── UtilisateurResponse.java
│       └── EmployeResponse.java
│
├── mapper/
│   └── UtilisateurMapper.java
│
├── repository/
│   ├── EmployeRepository.java
│   └── InvitationRepository.java
│
├── service/
│   ├── AdminFilialeService.java
│   ├── EmployeService.java
│   ├── UtilisateurService.java
│   ├── ProfilService.java
│   └── validator/
│       ├── EmployeValidator.java
│       └── InvitationValidator.java
│
└── event/
    ├── EmployeCreatedEvent.java
    ├── InvitationSentEvent.java
    └── UtilisateurDeactivatedEvent.java
```

---

### 4.5 `stockmaster-catalogue` — Catalogue (EPIC 5)

```
stockmaster-catalogue/src/main/java/com/stockmaster/catalogue/
├── controller/
│   ├── CategorieController.java         ← CRUD /api/v1/categories
│   └── ArticleController.java           ← CRUD /api/v1/articles
│
├── domain/
│   ├── entity/
│   │   ├── Categorie.java              ← code, designation, tauxTva
│   │   └── Article.java                ← codeArticle, designation, prixAchatHt, prixVenteHt, prixVenteTtc, tauxTva, seuilAlerte, photoUrl
│   └── enums/
│       ├── TauxTva.java                ← 19.25, 5.5, 0 (configurable par entreprise)
│       └── StatutAlerte.java           ← NORMAL, BAS, RUPTURE
│
├── dto/
│   ├── request/
│   │   ├── CreerCategorieRequest.java
│   │   ├── ModifierCategorieRequest.java
│   │   ├── CreerArticleRequest.java
│   │   └── ModifierArticleRequest.java
│   └── response/
│       ├── CategorieResponse.java
│       ├── ArticleResponse.java
│       └── ArticleListResponse.java     ← + stockActuel, statutAlerte
│
├── mapper/
│   └── CatalogueMapper.java
│
├── repository/
│   ├── CategorieRepository.java
│   └── ArticleRepository.java
│
├── service/
│   ├── CategorieService.java
│   ├── ArticleService.java
│   ├── ArticleSearchService.java        ← Full-text search avec index GIN
│   ├── PrixCalculatorService.java       ← Calcul HT/TVA/TTC, marge brute
│   └── validator/
│       ├── CategorieValidator.java
│       └── ArticleValidator.java
│
├── event/
│   ├── ArticleCreatedEvent.java
│   └── ArticleUpdatedEvent.java
│
└── spec/
    ├── CategorieSpec.java
    └── ArticleSpec.java
```

---

### 4.6 `stockmaster-tiers` — Clients & Fournisseurs (EPIC 6)

```
stockmaster-tiers/src/main/java/com/stockmaster/tiers/
├── controller/
│   ├── FournisseurController.java       ← CRUD /api/v1/fournisseurs
│   └── ClientController.java            ← CRUD /api/v1/clients
│
├── domain/
│   ├── entity/
│   │   ├── Fournisseur.java             ← raisonSociale, contact, telephone, email, adresse
│   │   ├── Client.java                  ← nom, prenom, telephone, email, adresse
│   │   └── Contact.java                 ← Value Object (telephone, email)
│   └── enums/
│       └── TypeTiers.java              ← PARTICULIER, ENTREPRISE
│
├── dto/
│   ├── request/
│   │   ├── CreerFournisseurRequest.java
│   │   ├── ModifierFournisseurRequest.java
│   │   ├── CreerClientRequest.java
│   │   └── ModifierClientRequest.java
│   └── response/
│       ├── FournisseurResponse.java
│       └── ClientResponse.java
│
├── mapper/
│   └── TiersMapper.java
│
├── repository/
│   ├── FournisseurRepository.java
│   └── ClientRepository.java
│
├── service/
│   ├── FournisseurService.java
│   ├── ClientService.java
│   └── validator/
│       └── TiersValidator.java
│
└── event/
    ├── FournisseurCreatedEvent.java
    └── ClientCreatedEvent.java
```

---

### 4.7 `stockmaster-achat` — Commandes Fournisseur (EPIC 7)

```
stockmaster-achat/src/main/java/com/stockmaster/achat/
├── controller/
│   └── CommandeFournisseurController.java    ← CRUD + valider + livrer
│
├── domain/
│   ├── entity/
│   │   ├── CommandeFournisseur.java          ← code, etat, commentaire, fournisseur
│   │   ├── LigneCommandeFournisseur.java     ← article, quantite, prixUnitaire, tauxTva (snapshot)
│   │   └── Reception.java                    ← Réception partielle (si dépréciation)
│   └── enums/
│       └── EtatCommandeFournisseur.java      ← EN_PREPARATION, VALIDEE, LIVREE, ANNULEE
│
├── dto/
│   ├── request/
│   │   ├── CreerCommandeFournisseurRequest.java
│   │   ├── ModifierCommandeFournisseurRequest.java
│   │   └── FiltrerCommandeRequest.java
│   └── response/
│       ├── CommandeFournisseurResponse.java
│       └── LigneCommandeResponse.java
│
├── mapper/
│   └── CommandeMapper.java
│
├── repository/
│   ├── CommandeFournisseurRepository.java
│   └── LigneCommandeFournisseurRepository.java
│
├── service/
│   ├── CommandeFournisseurService.java
│   ├── ValidationCommandeService.java        ← Validation + création mouvements stock
│   ├── EtatCommandeService.java              ← Machine à états (transitions)
│   └── validator/
│       ├── CommandeValidator.java
│       └── LigneCommandeValidator.java
│
├── event/
│   ├── CommandeValideeEvent.java
│   └── CommandeLivreeEvent.java
│
└── spec/
    └── CommandeFournisseurSpec.java
```

---

### 4.8 `stockmaster-stock` — Stock & Transferts (EPIC 8 + 11)

```
stockmaster-stock/src/main/java/com/stockmaster/stock/
├── controller/
│   ├── StockController.java                  ← GET /api/v1/stock
│   ├── MouvementController.java              ← GET /api/v1/stock/articles/{id}/mouvements
│   ├── CorrectionController.java             ← POST /api/v1/stock/corrections
│   └── TransfertController.java              ← POST/GET /api/v1/transferts (EPIC 11)
│
├── domain/
│   ├── entity/
│   │   ├── MouvementStock.java               ← type, quantite, article, entreprise, origine, motif, avant, apres
│   │   ├── Stock.java                        ← Ligne de stock (article, entreprise, quantite)
│   │   └── Transfert.java                    ← EPIC 11 : filialeSource, filialeDest, statut, lignes
│   └── enums/
│       ├── TypeMouvement.java                ← ENTREE, SORTIE, CORRECTION_POS, CORRECTION_NEG, TRANSFERT_ENTREE, TRANSFERT_SORTIE, ANNULATION_VENTE
│       ├── OrigineMouvement.java             ← COMMANDE_FOURNISSEUR, VENTE, CORRECTION, TRANSFERT, ANNULATION
│       ├── StatutAlerte.java                 ← NORMAL, BAS, RUPTURE
│       └── StatutTransfert.java              ← EN_COURS, VALIDE, ANNULE
│
├── dto/
│   ├── request/
│   │   ├── CorrectionStockRequest.java
│   │   ├── CreerTransfertRequest.java
│   │   ├── ValiderTransfertRequest.java
│   │   └── FiltrerStockRequest.java
│   └── response/
│       ├── StockResponse.java
│       ├── MouvementStockResponse.java
│       └── TransfertResponse.java
│
├── mapper/
│   └── StockMapper.java
│
├── repository/
│   ├── MouvementStockRepository.java
│   ├── StockRepository.java
│   └── TransfertRepository.java
│
├── service/
│   ├── StockService.java                     ← Consultation + calcul temps réel
│   ├── MouvementService.java                 ← Création mouvements atomique
│   ├── CorrectionService.java                ← Corrections inventaire
│   ├── AlerteStockService.java               ← Vérification seuil alerte
│   ├── StockConsolideService.java            ← Vue groupe
│   ├── TransfertService.java                 ← EPIC 11
│   └── validator/
│       ├── CorrectionValidator.java
│       └── TransfertValidator.java
│
├── event/
│   ├── StockUpdatedEvent.java
│   ├── AlerteStockEvent.java
│   └── TransfertValideEvent.java
│
└── spec/
    ├── MouvementStockSpec.java
    └── StockSpec.java
```

---

### 4.9 `stockmaster-vente` — Ventes B2B & Caisse (EPIC 9 + 10)

```
stockmaster-vente/src/main/java/com/stockmaster/vente/
├── controller/
│   ├── CommandeClientController.java         ← CRUD /api/v1/commandes-client (EPIC 9)
│   ├── VenteDirecteController.java           ← POST/GET /api/v1/ventes-directes (EPIC 10)
│   └── FideliteController.java               ← GET /api/v1/clients/{id}/fidelite (EPIC 10)
│
├── domain/
│   ├── entity/
│   │   ├── CommandeClient.java               ← code, client, etat, montants
│   │   ├── LigneCommandeClient.java           ← article, quantite, prixUnitaireVente, tauxTva
│   │   ├── VenteDirecte.java                  ← Caisse (client occasionnel ou fidèle)
│   │   ├── LigneVenteDirecte.java
│   │   └── PointFidelite.java                 ← Client → points cumulés (EPIC 10)
│   └── enums/
│       ├── EtatCommandeClient.java            ← EN_PREPARATION, VALIDEE, LIVREE, ANNULEE
│       └── TypeVente.java                    ← B2B, CAISSE
│
├── dto/
│   ├── request/
│   │   ├── CreerCommandeClientRequest.java
│   │   ├── CreerVenteDirecteRequest.java
│   │   └── FiltrerVenteRequest.java
│   └── response/
│       ├── CommandeClientResponse.java
│       ├── VenteDirecteResponse.java
│       └── FideliteResponse.java
│
├── mapper/
│   └── VenteMapper.java
│
├── repository/
│   ├── CommandeClientRepository.java
│   ├── VenteDirecteRepository.java
│   └── PointFideliteRepository.java
│
├── service/
│   ├── CommandeClientService.java
│   ├── ValidationVenteService.java           ← Validation + sortie de stock + check solvabilité
│   ├── VenteDirecteService.java
│   ├── FideliteService.java                  ← Cumul + utilisation points
│   └── validator/
│       ├── CommandeClientValidator.java
│       └── VenteDirecteValidator.java
│
├── event/
│   ├── CommandeClientValideeEvent.java
│   ├── VenteDirecteEffectueeEvent.java
│   └── FideliteMiseAJourEvent.java
│
└── spec/
    └── CommandeClientSpec.java
```

---

### 4.10 `stockmaster-notification` — Notifications & Alertes (EPIC 12)

```
stockmaster-notification/src/main/java/com/stockmaster/notification/
├── config/
│   └── MailConfig.java                       ← MailHog/Mailjet config
│
├── controller/
│   └── NotificationController.java           ← GET/PATCH /api/v1/notifications
│
├── domain/
│   ├── entity/
│   │   ├── Notification.java                 ← in-app (lue/non lue)
│   │   ├── AlerteStock.java                  ← Alerte seuil (résolue/active)
│   │   └── EmailQueue.java                   ← File d'attente email (retry)
│   └── enums/
│       ├── TypeNotification.java             ← ALERTE_STOCK, INFO_SYSTEME, INVITATION, SECURITE
│       ├── CanalNotification.java            ← IN_APP, EMAIL, SMS
│       └── StatutEmail.java                 ← EN_ATTENTE, ENVOYE, ECHEC
│
├── dto/
│   ├── request/
│   │   └── MarquerLueRequest.java
│   └── response/
│       ├── NotificationResponse.java
│       └── AlerteStockResponse.java
│
├── repository/
│   ├── NotificationRepository.java
│   ├── AlerteStockRepository.java
│   └── EmailQueueRepository.java
│
├── service/
│   ├── NotificationService.java
│   ├── AlerteStockService.java                ← Vérification + création alerte
│   ├── EmailService.java                      ← Envoi email (async + retry)
│   └── NotificationDispatcher.java           ← Routage canal (in-app / email / SMS)
│
└── listener/
    ├── StockEventListener.java                ← Écoute StockUpdatedEvent → vérifie alerte
    ├── AuthEventListener.java                 ← Écoute événements auth → notifications sécurité
    └── CommandeEventListener.java             ← Écoute commandes → notifications utilisateur
```

---

### 4.11 `stockmaster-reporting` — Statistiques & Reporting (EPIC 13)

```
stockmaster-reporting/src/main/java/com/stockmaster/reporting/
├── config/
│   └── ReportingConfig.java
│
├── controller/
│   ├── StatsController.java                  ← GET /api/v1/stats/...
│   └── ExportController.java                 ← GET /api/v1/exports/...
│
├── domain/
│   ├── entity/
│   │   ├── RapportStock.java                 ← Rapport périodique (cache/agrégé)
│   │   └── RapportVente.java
│   └── enums/
│       ├── PeriodeRapport.java               ← JOUR, SEMAINE, MOIS, ANNEE
│       └── TypeExport.java                   ← PDF, EXCEL, CSV
│
├── dto/
│   ├── request/
│   │   └── GenererRapportRequest.java
│   └── response/
│       ├── DashboardStatsResponse.java
│       ├── RapportStockResponse.java
│       ├── RapportVenteResponse.java
│       └── ExportResponse.java
│
├── service/
│   ├── DashboardService.java                 ← KPI temps réel (CA, stock total, alertes)
│   ├── RapportStockService.java              ← Historique mouvements agrégé
│   ├── RapportVenteService.java              ← Top ventes, CA par période
│   ├── ExportService.java                    ← Génération fichiers (PDF/Excel/CSV)
│   └── StockProjectionService.java           ← Projections tendancielles
│
└── spec/
    └── RapportSpec.java
```

---

### 4.12 `stockmaster-bootstrap` — Point d'entrée

```
stockmaster-bootstrap/src/main/java/com/stockmaster/bootstrap/
├── StockMasterApplication.java               ← @SpringBootApplication
└── ServletInitializer.java                   ← Pour déploiement WAR (optionnel)
```

> ⚠️ **Seul module avec `spring-boot-maven-plugin`**  
> Les autres modules ont `<skip>true</skip>` pour ce plugin

---

## 5. Dépendances inter-modules

```
stockmaster-bootstrap
├── stockmaster-shared        → (obligatoire)
├── stockmaster-auth          → stockmaster-shared
├── stockmaster-groupe        → stockmaster-shared
├── stockmaster-utilisateur   → stockmaster-shared, stockmaster-auth, stockmaster-groupe
├── stockmaster-catalogue     → stockmaster-shared
├── stockmaster-tiers         → stockmaster-shared
├── stockmaster-achat         → stockmaster-shared, stockmaster-catalogue, stockmaster-tiers
├── stockmaster-stock          → stockmaster-shared, stockmaster-catalogue
├── stockmaster-vente          → stockmaster-shared, stockmaster-catalogue, stockmaster-tiers, stockmaster-stock
├── stockmaster-notification   → stockmaster-shared
└── stockmaster-reporting     → stockmaster-shared, stockmaster-stock, stockmaster-vente, stockmaster-achat
```

**Règle** : Les dépendances vont de l'amont vers l'aval. Un module aval peut dépendre d'un module amont, mais jamais l'inverse.

```
Amont (peu de dépendances)           Aval (beaucoup de dépendances)
shared → auth → groupe → utilisateur → catalogue → tiers → achat → stock → vente → notification → reporting
```

---

## 6. Correspondance EPIC ↔ Module ↔ US

| EPIC | Module | US concernées | Sprint |
|---|---|---|---|
| EPIC 1 — Fondations | `shared` + `bootstrap` | US-001 à US-005 | Sprint 1 |
| EPIC 2 — Auth | `auth` | US-006 à US-017 | Sprints 2-3 |
| EPIC 3 — Groupe | `groupe` | US-014 à US-020 | Sprints 3-4 |
| EPIC 4 — Utilisateurs | `utilisateur` | US-021 à US-026 | Sprints 3-4 |
| EPIC 5 — Catalogue | `catalogue` | US-027 à US-035 | Sprint 4 |
| EPIC 6 — Tiers | `tiers` | US-036 à US-043 | Sprint 5 |
| EPIC 7 — Achats | `achat` | US-044 à US-050 | Sprint 6 |
| EPIC 8 — Stock | `stock` | US-051 à US-055 | Sprints 7-8 |
| EPIC 9 — Ventes B2B | `vente` | US-056 à US-063 | Sprint 8 |
| EPIC 10 — Caisse | `vente` | US-064 à US-069 | Sprint 9 |
| EPIC 11 — Transferts | `stock` | US-070 à US-072 | Sprint 10 |
| EPIC 12 — Notifications | `notification` | US-073 à US-076 | Sprint 10 |
| EPIC 13 — Reporting | `reporting` | US-077 à US-080 | Sprint 11 |

---

## 7. Problèmes actuels et corrections

| # | Problème constaté | Correction |
|---|---|---|
| P1 | **Modules stubs dans `bootstrap/pom.xml`** (groupe, utilisateur, catalogue, etc. sans code) → `POM missing` warning + build fail si cache vidé | Ne déclarer que les modules avec du code existant dans `bootstrap/pom.xml`. Ajouter les autres quand leur implémentation commence |
| P2 | **`auth/` a des entités qui ne lui appartiennent pas** (`Entreprise`, `TenantGroup` sont du domaine Groupe) | Les entités partagées (`AbstractEntity`) restent dans `shared`. Les entités métier (`Entreprise`, `TenantGroup`) doivent être dans le module approprié ou mutualisées dans `shared` temporairement |
| P3 | **Pas de scripts de build** → l'utilisateur doit connaître l'ordre `mvn install -pl X` manuellement | Créer `backend/build.sh` + `backend/build.cmd` : `mvn clean install -DskipTests && mvn spring-boot:run -pl stockmaster-bootstrap` |
| P4 | **`stockmaster-auth` contient trop de responsabilités** (entités Entreprise, TenantGroup, Utilisateur + repositories) | À terme, migrer les entités Entreprise/TenantGroup vers `stockmaster-groupe`, Utilisateur vers `stockmaster-utilisateur`. Garder uniquement la logique d'authentification dans `auth` |
| P5 | **Pas d'isolation module** — `auth` et `bootstrap` partagent les mêmes ressources | Ajouter `spring-modulith` ou un test d'architecture ArchUnit pour vérifier que les dépendances inter-modules respectent le graphe (section 5) |
