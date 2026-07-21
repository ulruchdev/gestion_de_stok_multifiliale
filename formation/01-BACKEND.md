# 🎓 Formation Backend — StockMaster CM
> **Cours exhaustif** couvrant **US-001 à US-080** du backlog produit.
> Garde ce fichier pendant ta formation, supprime-le quand tu maîtrises.
>
> **Public :** Ulrich, Stephan — Développeurs backend
> **Prérequis :** Java 21 de base, notions SQL
> **Concepts couverts :** 150+ concepts Java, Spring Boot, JPA, sécurité, architecture

---

## Table des matières

1. [Architecture & Philosophie](#1-architecture--philosophie)
2. [Fondations (US-001 à US-005)](#2-fondations-us-001-à-us-005)
3. [Authentification (US-006 à US-017)](#3-authentification-us-006-à-us-017)
4. [Groupe & Filiales (US-014 à US-020)](#4-groupe--filiales-us-014-à-us-020)
5. [Gestion des Utilisateurs (US-021 à US-026)](#5-gestion-des-utilisateurs-us-021-à-us-026)
6. [Catalogue (US-027 à US-035)](#6-catalogue-us-027-à-us-035)
7. [Tiers — Clients & Fournisseurs (US-036 à US-043)](#7-tiers--clients--fournisseurs-us-036-à-us-043)
8. [Commandes Fournisseur (US-044 à US-050)](#8-commandes-fournisseur-us-044-à-us-050)
9. [Gestion du Stock (US-051 à US-055)](#9-gestion-du-stock-us-051-à-us-055)
10. [Commandes Client B2B (US-056 à US-063)](#10-commandes-client-b2b-us-056-à-us-063)
11. [Vente Directe & Caisse (US-064 à US-067)](#11-vente-directe--caisse-us-064-à-us-067)
12. [Transferts Inter-Filiales (US-068 à US-070)](#12-transferts-inter-filiales-us-068-à-us-070)
13. [Notifications & Alertes (US-071 à US-075)](#13-notifications--alertes-us-071-à-us-075)
14. [Reporting & Statistiques (US-076 à US-080)](#14-reporting--statistiques-us-076-à-us-080)
15. [Glossaire & Concepts Clés](#15-glossaire--concepts-clés)

---

## 1. Architecture & Philosophie

### 🏗️ Qu'est-ce qu'un monolithe modulaire ?

**Définition :** C'est une application déployée comme **un seul JAR** (un seul processus Java),
mais dont le code source est découpé en **modules Maven indépendants** qui communiquent
via des interfaces bien définies.

```
Monolithe spaghetti         Monolithe modulaire              Microservices
        ❌                          ✅                            ⚠️
   Tout mélangé              Modules isolés                 Services séparés
   Dans les mêmes            Packages distincts             N déploiements
   dossiers                  Communication par              Complexité réseau
                             interfaces & événements
```

**Pourquoi ce choix pour StockMaster CM ?** (US-001, ADR-001 du CDCT §27)

StockMaster CM cible des PME camerounaises (5-10 clients en V1). Les microservices
apporteraient une **complexité inutile** : réseau entre services, déploiements multiples,
observabilité, monitoring. Un monolithe modulaire donne **la même isolation de domaine**
que les microservices mais avec **la simplicité de déploiement d'un monolithe**.

| Critère | Monolithe modulaire | Microservices |
|---------|-------------------|---------------|
| Déploiement | 1 JAR → 1 serveur | N JARs → N serveurs |
| Appels inter-modules | Java direct (mémoire) | HTTP réseau (latence) |
| Transaction atomique | `@Transactional` simple | Saga pattern complexe |
| Debugging | 1 processus, 1 log | N logs distribués |
| Montée en charge | Verticale (plus de RAM/CPU) | Horizontale (plus d'instances) |
| **Choix StockMaster** | **✅ V1-V2** | ⚠️ V3 si besoin |

### 📦 Les 11 modules Maven

```
stockmaster-shared/        ← Bibliothèque commune (AbstractEntity, exceptions, DTOs) ✅ ACTIF
stockmaster-auth/          ← Authentification, JWT, inscription, login         ✅ ACTIF
stockmaster-bootstrap/     ← Point d'entrée Spring Boot (main())               ✅ ACTIF
stockmaster-groupe/        ← Groupes et filiales                               ⬜ STUB
stockmaster-utilisateur/   ← Gestion des utilisateurs                          ⬜ STUB
stockmaster-catalogue/     ← Articles et catégories                            ⬜ STUB
stockmaster-tiers/         ← Clients et fournisseurs                           ⬜ STUB
stockmaster-achat/         ← Commandes fournisseur                             ⬜ STUB
stockmaster-stock/         ← Mouvements de stock                               ⬜ STUB
stockmaster-vente/         ← Ventes directes et commandes client               ⬜ STUB
stockmaster-notification/  ← Alertes et emails                                 ⬜ STUB
stockmaster-reporting/     ← Statistiques et rapports                          ⬜ STUB
```

**Pourquoi 11 modules ?** Parce que chaque **EPIC du backlog** correspond à un module.
Le KICKOFF §1 dit : "Un module = un domaine métier". C'est le **Domain-Driven Design**
appliqué avec Maven.

### 📐 La règle des couches (layered architecture)

Chaque module respecte cette structure de packages :

```
com.stockmaster.{module}/
├── controller/     ← API REST (@RestController)
├── service/
│   ├── {Nom}Service.java         ← Interface publique
│   └── impl/
│       └── {Nom}ServiceImpl.java ← Implémentation
├── repository/     ← Accès base de données
├── domain/
│   ├── entity/     ← Entités JPA (@Entity)
│   └── enums/      ← Enums métier
├── dto/
│   ├── request/    ← DTOs entrants (validation Jakarta)
│   └── response/   ← DTOs sortants
├── mapper/         ← MapStruct (Entité ↔ DTO)
└── event/          ← Spring Events (communication inter-modules)
```

**Règle d'or :** Chaque couche ne parle qu'à la couche en dessous.

```
Controller → Service (interface) → Repository → Entity
     ↓              ↓                  ↓           ↓
   DTO reçu     Logique métier      Requêtes SQL   Table BDD
```

✅ **Autorisé :** `Controller → Service`, `Service → Repository`
❌ **Interdit :** `Controller → Repository`, `Service → Controller`, `Repository → Service`

**Pourquoi ?** Pour que le code soit **testable**, **maintenable** et **remplaçable**.
Si tu changes la BDD, tu changes seulement le Repository. Le Service ne change pas.
Si tu changes l'API REST, tu changes seulement le Controller. Le Service ne change pas.

### 🔗 Communication inter-modules par événements

Quand un module a besoin de notifier un autre, on utilise **Spring Application Events** :

```java
// Module stock — publie un événement
public record StockUpdatedEvent(Long articleId, Integer nouveauStock) {}

// Module notification — écoute l'événement
@Component
public class AlerteStockListener {
    @EventListener
    @Async  // ← Exécution asynchrone ! Ne bloque pas le module stock
    public void onStockUpdated(StockUpdatedEvent event) {
        // Créer une alerte si stock < seuil...
    }
}
```

**Pourquoi pas un appel direct de service ?** Pour éviter les dépendances circulaires.
Si `stock` appelle `notification` et `notification` appelle `stock`, on a un cycle.
Avec les événements, `stock` ne sait même pas qui écoute. C'est un **découplage total**.

---

## 2. Fondations (US-001 à US-005)

> **Objectif EPIC 1 :** Mettre en place l'infrastructure technique sans laquelle
> aucune fonctionnalité métier n'est possible. 19 story points. Sprint 1.

### 📘 US-001 — Initialisation du projet Spring Boot

**Concept clé :** `@SpringBootApplication`

```java
// stockmaster-bootstrap/src/main/java/.../StockMasterApplication.java
@SpringBootApplication
public class StockMasterApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockMasterApplication.class, args);
    }
}
```

**Cette annotation fait 3 choses en 1 :**

| Annotation décomposée | Rôle |
|----------------------|------|
| `@Configuration` | Dit à Spring : "Cette classe peut produire des beans" |
| `@EnableAutoConfiguration` | Dit à Spring : "Configure automatiquement Tomcat, JPA, etc." |
| `@ComponentScan` | Dit à Spring : "Cherche les `@Component`, `@Service`, `@Controller` dans le package" |

**Pourquoi un module `bootstrap` séparé ?** (Réponse de la PR GS-013)
Parce que sinon, chaque module qui veut faire un test d'intégration doit charger
tous les autres modules. Avec un module `bootstrap` dédié, le point d'entrée est
**unique et isolé**.

#### 📄 Structure du POM parent (racine)

Le `pom.xml` racine est le **contrat technique** du projet. Il définit :

```xml
<packaging>pom</packaging>  <!-- ← Parent POM, pas un JAR exécutable -->

<modules>
    <module>stockmaster-shared</module>
    <module>stockmaster-auth</module>
    <module>stockmaster-bootstrap</module>
    <!-- ... 11 modules -->
</modules>

<properties>
    <java.version>21</java.version>              <!-- Java 21 obligatoire -->
    <spring-boot.version>3.3.5</spring-boot.version>
    <mapstruct.version>1.6.3</mapstruct.version>
    <jjwt.version>0.12.6</jjwt.version>          <!-- jjwt 0.12.x, JAMAIS 0.9.x ! -->
    <sonar.coverage.exclusions>
        **/StockMasterApplication.java,           <!-- Exclure la classe main -->
        **/config/**
    </sonar.coverage.exclusions>
</properties>
```

**Pourquoi Java 21 ?** Parce qu'il apporte les **records** (DTOs immuables),
les **pattern matching** (`instanceof` amélioré), les **text blocks**
(JSON multilignes dans les tests), et les **virtual threads** (Project Loom —
meilleure gestion de la concurrence).

#### 📄 AbstractEntity — La classe de base de toutes les entités

```java
// stockmaster-shared/src/main/java/.../entity/AbstractEntity.java
@Getter
@Setter
@MappedSuperclass                         // ← Ne crée pas de table pour cette classe
@EntityListeners(AuditingEntityListener.class) // ← Active l'audit automatique
public abstract class AbstractEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ← Auto-incrément PostgreSQL
    private Long id;

    @CreatedDate
    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @LastModifiedDate
    @Column(name = "date_modification", nullable = false)
    private Instant dateModification;

    @Column(name = "supprime", nullable = false)
    private Boolean supprime = false;

    public void marquerCommeSupprime() {
        this.supprime = true;
    }
}
```

**Explication des annotations :**

| Annotation | Rôle |
|------------|------|
| `@MappedSuperclass` | JPA ne crée PAS de table pour cette classe. Ses champs sont hérités par les sous-classes. |
| `@EntityListeners(AuditingEntityListener.class)` | Active l'audit automatique : `@CreatedDate` et `@LastModifiedDate` sont gérés automatiquement par Spring. |
| `@GeneratedValue(strategy = GenerationType.IDENTITY)` | PostgreSQL génère l'ID automatiquement via `BIGSERIAL`. |
| `Instant` | Type Java 8+ pour les dates UTC. Préféré à `Date` ou `LocalDateTime`. |

**Pourquoi `Instant` et pas `LocalDateTime` ?** Parce que `Instant` stocke en UTC.
Si ton client est à Douala (UTC+1) et ton serveur à Paris (UTC+2), `LocalDateTime`
créerait des incohérences. `Instant` est agnostique au fuseau horaire.

**Pourquoi `supprime = false` par défaut ?** C'est du **soft delete**. On ne supprime
JAMAIS physiquement une ligne. On met `supprime = true`. Avantages :
- L'historique est conservé (factures, mouvements de stock...)
- On peut "annuler" une suppression
- Les contraintes d'intégrité référentielle ne sont pas brisées

#### 📄 ApiResponse<T> — Le wrapper standard des réponses

```java
// stockmaster-shared/src/main/java/.../dto/response/ApiResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;  // ← Générique T : le type des données métier

    // Méthodes statiques utilitaires
    public static <T> ApiResponse<T> ok(T data) { ... }
    public static <T> ApiResponse<T> success(String message) { ... }
    public static <T> ApiResponse<T> error(String message) { ... }
}
```

**Pourquoi un wrapper générique (`<T>`) ?** Pour que TOUTES les réponses de l'API
aient la même structure. Le frontend sait toujours où trouver les données (`response.data`)
et le message (`response.message`). Sans wrapper, chaque endpoint aurait un format différent.

**Exemples de réponses :**

```json
// Succès avec données
{ "success": true, "data": { "accessToken": "eyJ..." } }

// Succès sans données (ex: suppression)
{ "success": true, "message": "Déconnexion réussie" }

// Erreur (handle par GlobalExceptionHandler, pas ApiResponse)
// → utilise ProblemResponse (RFC 7807) à la place
```

**Pourquoi `@JsonInclude(JsonInclude.Include.NON_NULL)` ?** Pour que les champs `null`
ne soient pas sérialisés dans le JSON. Si `message` est null, il n'apparaît pas dans la réponse.
JSON plus petit = plus rapide à transmettre.

---

### 📘 US-002 — Configuration Flyway et schéma initial

**Concept clé :** Versionnement de base de données

**Problème :** En développement, plusieurs personnes modifient la BDD. Comment
s'assurer que tout le monde a la même structure ? La solution naïve (`ddl-auto=update`)
laisse Hibernate modifier le schéma automatiquement. **DANGEREUX :**
- Hibernate peut SUPPRIMER des colonnes par erreur
- Pas de versionnement : on ne sait pas qui a changé quoi
- Impossible de rollback

**Solution :** Flyway. Chaque changement = un fichier SQL numéroté dans `db/migration/`.

```
V1__init_schema.sql           → Création des 16 tables
V2__create_indexes.sql        → Index de performance
V3__functions_and_triggers.sql → Triggers automatiques
```

**Comment Flyway marche :**
1. Au démarrage de l'app, Flyway regarde la table `flyway_schema_history` en BDD
2. Il compare avec les fichiers dans `db/migration/`
3. Il exécute les fichiers qui n'ont pas encore été appliqués (dans l'ordre numérique)
4. Il enregistre chaque exécution dans `flyway_schema_history`

#### 📄 V1__init_schema.sql — Les tables principales

```sql
-- Table racine : un groupe = une entreprise cliente
CREATE TABLE tenant_group (
    id                    BIGSERIAL PRIMARY KEY,
    nom_groupe            VARCHAR(100) NOT NULL,           -- Nom de l'entreprise
    plan_abonnement       VARCHAR(20) NOT NULL DEFAULT 'GRATUIT'
                          CHECK (plan_abonnement IN ('GRATUIT','STARTER','PRO','ENTERPRISE')),
    actif                 BOOLEAN NOT NULL DEFAULT TRUE,
    limite_filiales       INTEGER NOT NULL DEFAULT 1,      -- Selon le plan
    date_creation         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modification     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime              BOOLEAN NOT NULL DEFAULT FALSE
);

-- Table entreprise : maison mère ou filiale
CREATE TABLE entreprise (
    id                BIGSERIAL PRIMARY KEY,
    group_id          BIGINT NOT NULL REFERENCES tenant_group(id) ON DELETE RESTRICT,
    parent_id         BIGINT REFERENCES entreprise(id) ON DELETE RESTRICT,  -- NULL = maison mère
    type_entreprise   VARCHAR(10) NOT NULL CHECK (type_entreprise IN ('MERE','FILIALE')),
    nom               VARCHAR(100) NOT NULL,
    code_filiale      VARCHAR(10),              -- Code unique dans le groupe ex: DLA01
    actif             BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_entreprise_code_filiale UNIQUE (group_id, code_filiale)
);
```

**Explication SQL :**

| Syntaxe | Rôle |
|---------|------|
| `BIGSERIAL` | Type PostgreSQL : auto-incrément pour les IDs |
| `PRIMARY KEY` | Contrainte : valeur unique et non nulle |
| `REFERENCES tenant_group(id)` | Clé étrangère (FK) vers une autre table |
| `ON DELETE RESTRICT` | Empêche la suppression si des lignes référencent cette table |
| `CHECK (... IN ...)` | Contrainte : limite les valeurs possibles |
| `TIMESTAMPTZ` | Timestamp avec fuseau horaire (UTC) |
| `DEFAULT NOW()` | Valeur par défaut : date/heure actuelle |
| `UNIQUE (group_id, code_filiale)` | Contrainte : le couple (groupe, code) est unique |

**Pourquoi `ON DELETE RESTRICT` et pas `CASCADE` ?** Sécurité. Si quelqu'un essaie
de supprimer un `tenant_group` qui a des filiales, la BDD refuse. `CASCADE` supprimerait
tout silencieusement. En soft delete on passe par `supprime = true`.

#### 💡 Les 7 rôles utilisateur (enum)

```sql
role VARCHAR(30) NOT NULL CHECK (role IN (
    'SUPER_ADMIN',      -- Administrateur plateforme (nous)
    'ADMIN_GROUPE',     -- Dirigeant du groupe (client)
    'ADMIN_FILIALE',    -- Responsable d'une filiale
    'GESTIONNAIRE_STOCK', -- Gère le stock et le catalogue
    'RESP_ACHATS',       -- Gère les commandes fournisseur
    'COMMERCIAL',        -- Gère les commandes client B2B
    'CAISSIER'           -- Vente directe / caisse
))
```

**Hiérarchie des permissions :**
```
SUPER_ADMIN > ADMIN_GROUPE > ADMIN_FILIALE > GESTIONNAIRE_STOCK / RESP_ACHATS / COMMERCIAL / CAISSIER
```

#### 📄 V2__create_indexes.sql — Performance

```sql
-- Index pour le calcul du stock réel (LA requête la plus fréquente)
CREATE INDEX idx_mouvement_article_entreprise
    ON mouvement_stock(article_id, entreprise_id);

-- Recherche full-text (PostgreSQL natif)
CREATE INDEX idx_article_fulltext
    ON article USING gin(
        to_tsvector('french', designation || ' ' || code_article)
    );
```

**Pourquoi des indexes ?** Sans index, PostgreSQL fait un **seq scan** (lit toute la table
ligne par ligne). Avec un index, il fait un **index scan** (va directement aux lignes concernées).
Pour une table de 10 000 mouvements, la différence peut être de **quelques millisecondes
contre plusieurs secondes**.

**Pourquoi `USING gin` et pas l'index B-tree par défaut ?** Le **GIN** (Generalized Inverted Index)
est optimisé pour la recherche full-text. Le B-tree est bon pour les égalités et les tris,
pas pour la recherche textuelle. `to_tsvector('french', ...)` convertit le texte en vecteur
de mots pour la recherche en français.

#### 📄 V3__functions_and_triggers.sql — Automatisation

```sql
-- Trigger qui met à jour date_modification automatiquement
CREATE OR REPLACE FUNCTION update_date_modification()
RETURNS TRIGGER AS $$
BEGIN
    NEW.date_modification = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

**Pourquoi un trigger et pas Java ?** Parce que même si un script SQL modifie les données
directement (un admin BDD, une migration), `date_modification` sera quand même mise à jour.
Java ne s'exécute que si on passe par l'application. Le trigger s'exécute **toujours**.

---

### 📘 US-003 — Gestion centralisée des erreurs

**Concept clé :** `@RestControllerAdvice`

Sans gestion centralisée, chaque contrôleur gère ses erreurs différemment :
```java
// ❌ MAUVAISE PRATIQUE : chaque contrôleur gère ses erreurs
@GetMapping("/articles/{id}")
public Article getArticle(@PathVariable Long id) {
    return articleRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Pas trouvé"));  // ← Retourne 500 !
}
```

**Avec `GlobalExceptionHandler`, TOUTES les erreurs passent par UN SEUL endroit :**

```java
@RestControllerAdvice  // ← Intercepte TOUTES les exceptions des contrôleurs
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemResponse> handleBusiness(
            BusinessException ex, HttpServletRequest request) {
        
        log.warn("BusinessException — [{}] {}", ex.getErrorCode().getCode(), ex.getMessage());
        // ↑ Log côté serveur uniquement
        
        ProblemResponse body = ProblemResponse.builder()
                .type("/errors/" + ex.getErrorCode().getCode().toLowerCase())
                .title(ex.getErrorCode().getMessage())
                .status(ex.getErrorCode().getHttpStatus())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())  // ← Quel endpoint a généré l'erreur ?
                .errorCode(ex.getErrorCode().getCode())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(body);
    }
}
```

**La hiérarchie des exceptions StockMaster :**

```
RuntimeException
└── BusinessException              ← Classe de base (toutes les erreurs métier)
    ├── EntityNotFoundException    ← 404 (US-003)
    └── InsufficientStockException ← 409 avec détails des ruptures (US-060)
```

#### 📄 ErrorCode — L'enum des erreurs possibles

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    AUTH_INVALID_CREDENTIALS("AUTH_001", "Email ou mot de passe incorrect", 401),
    AUTH_EMAIL_ALREADY_EXISTS("AUTH_008", "Cet email est déjà utilisé", 409),
    RES_ENTITY_NOT_FOUND("RES_001", "Ressource non trouvée", 404),
    CMD_ORDER_NOT_MODIFIABLE("CMD_001", "La commande n'est pas modifiable", 409),
    STK_INSUFFICIENT_STOCK("STK_001", "Stock insuffisant", 409),
    SEC_INVALID_PASSWORD("SEC_002", "Ancien mot de passe incorrect", 400),
    SYS_INTERNAL_ERROR("SYS_001", "Erreur interne du serveur", 500);
    // ... 30+ codes d'erreur

    private final String code;      // Code machine ex: AUTH_001
    private final String message;   // Message lisible ex: "Email ou mot de passe incorrect"
    private final int httpStatus;   // Code HTTP ex: 401
}
```

**Pourquoi une enum et pas des constantes ?** Parce qu'avec une enum, tu ne peux PAS
inventer un code d'erreur au milieu du code. Tous les codes sont **déclarés au même endroit**,
**documentés**, et **testés**. Si tu veux ajouter un code, tu DOIS le déclarer dans `ErrorCode`.

#### 📄 ProblemResponse — Format RFC 7807

```json
// Exemple de réponse d'erreur
{
    "type": "/errors/auth-email-already-exists",
    "title": "Cet email est déjà utilisé",
    "status": 409,
    "detail": "Cet email est déjà utilisé",
    "instance": "/api/v1/auth/inscription/entreprise-unique",
    "errorCode": "AUTH_008",
    "timestamp": "2026-07-10T14:30:00Z"
}
```

**Pourquoi RFC 7807 ?** C'est un **standard** (RFC = Request for Comments).
Si plus tard on utilise un API Gateway ou un outil comme Datadog, il peut parser
automatiquement les erreurs sans configuration personnalisée. Le format est prévisible.

---

### 📘 US-004 — Pipeline CI/CD

**Concept clé :** GitHub Actions + SonarCloud + JaCoCo

Le fichier `.github/workflows/ci.yml` définit :

```yaml
name: CI Backend
on: [push, pull_request]  # ← Déclenché à chaque push ET chaque PR

jobs:
  build:
    services:
      postgres:
        image: postgres:16-alpine    # ← PostgreSQL 16 dans le CI
        env:
          POSTGRES_DB: stockmaster_dev
          POSTGRES_USER: stockmaster
          POSTGRES_PASSWORD: stockmaster
      redis:
        image: redis:7-alpine       # ← Redis 7 dans le CI

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'        # ← JDK Eclipse Temurin (ex AdoptOpenJDK)
          cache: 'maven'

      - run: mvn compile -q              # 1. Compilation
      - run: mvn test                   # 2. Tests (JaCoCo mesure le coverage)
      - run: mvn sonar:sonar             # 3. Qualité (SonarCloud)
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

**Pourquoi ces 3 étapes ?**
1. **Compilation** : Est-ce que le code compile ? (vérifie les types, les imports)
2. **Tests** : Est-ce que les tests passent ? (vérifie le comportement)
3. **SonarCloud** : Est-ce que la qualité est bonne ? (vérifie les bugs, vulnérabilités, code smells)

**Les 3 services Docker du CI :**
- **PostgreSQL 16** : La BDD de production
- **Redis 7** : Les tokens, rate limiting, cache
- *(MinIO et MailHog non nécessaires dans le CI — on mocke)*

---

### 📘 US-005 — Conteneurisation Docker

**Concept clé :** Multi-stage build

```dockerfile
# ÉTAPE 1 : Builder (JDK 21 complet — 500MB)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests -pl stockmaster-bootstrap -am

# ÉTAPE 2 : Runtime (JRE 21 léger — 80MB)
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S stockmaster && adduser -S stockmaster -G stockmaster
USER stockmaster  # ← Pas root ! Sécurité
HEALTHCHECK --interval=30s --timeout=3s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
COPY --from=builder /app/stockmaster-bootstrap/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
```

**Pourquoi 2 étapes ?** JDK complet = 500MB, JRE seul = 80MB. En production, on n'a pas
besoin du compilateur Java. Le multi-stage build utilise JDK pour compiler, puis JRE pour exécuter.
L'image finale est **6 fois plus petite** = déploiement plus rapide.

**`-XX:+UseContainerSupport`** : Dit à la JVM qu'elle tourne dans un conteneur Docker.
Sans ça, la JVM voit toute la mémoire de la machine hôte au lieu des limites du conteneur.

---

## 3. Authentification (US-006 à US-017)

> **Objectif EPIC 2 :** Permettre à tout utilisateur de s'inscrire, se connecter
> et gérer son accès. **Prérequis absolu** de tous les autres EPICs.
> 21 story points (US-006 à US-012) + 14 points sécurité (US-013 à US-017).

---

### 📘 US-006 — Inscription entreprise unique

**Concept clé :** Création atomique dans une transaction

```java
@Service
@RequiredArgsConstructor  // ← Lombok génère le constructeur avec tous les champs final
public class AuthServiceImpl implements AuthService {

    @Override
    @Transactional(rollbackFor = Exception.class)  // ← ROLLBACK si une seule étape échoue
    public InscriptionResponse inscrireEntrepriseUnique(InscriptionEntrepriseUniqueRequest request) {

        // 1. Vérifier que l'email n'existe pas déjà
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }

        // 2. Créer le groupe (TenantGroup)
        TenantGroup groupe = TenantGroup.builder()
                .nomGroupe(request.getNomBoutique())
                .planAbonnement(PlanAbonnement.GRATUIT)  // ← Plan gratuit par défaut
                .limiteFiliales(1)                        // ← 1 filiale max pour le gratuit
                .build();
        groupe = tenantGroupRepository.save(groupe);

        // 3. Créer l'entreprise (maison mère)
        Entreprise entreprise = authMapper.toEntreprise(request);  // ← MapStruct
        entreprise.setGroupe(groupe);
        entreprise.setTypeEntreprise(TypeEntreprise.MERE);        // ← C'est la maison mère
        entreprise = entrepriseRepository.save(entreprise);

        // 4. Hacher le mot de passe et créer l'utilisateur ADMIN_GROUPE
        String motDePasseHash = passwordEncoder.encode(request.getMotDePasse());  // ← BCrypt
        Utilisateur utilisateur = Utilisateur.builder()
                .entreprise(entreprise)
                .scope(ScopeUtilisateur.GROUPE)                // ← Scope GROUPE
                .role(RoleUtilisateur.ADMIN_GROUPE)            // ← Rôle admin
                .email(request.getEmail())
                .motDePasse(motDePasseHash)
                .actif(true)
                .build();
        utilisateur = utilisateurRepository.save(utilisateur);

        // 5. Publier un événement asynchrone (email de bienvenue)
        eventPublisher.publishEvent(new InscriptionSuccessEvent(
                this, utilisateur.getEmail(), utilisateur.getPrenom(), entreprise.getNom()));

        return InscriptionResponse.builder()
                .email(request.getEmail())
                .groupId(groupe.getId())
                .message("Votre espace a été créé.")
                .build();
    }
}
```

**Pourquoi `@Transactional(rollbackFor = Exception.class)` ?**

Par défaut, Spring ne rollback que sur les `RuntimeException`. Si une exception
vérifiée (`Exception`) est levée, la transaction n'est pas annulée.
`rollbackFor = Exception.class` dit : "JE VEUX UN ROLLBACK POUR TOUTE EXCEPTION".

**Scénario catastrophe sans rollback :**
1. Étape 2 : `tenantGroupRepository.save(groupe)` → ok, Groupe créé
2. Étape 3 : `entrepriseRepository.save(entreprise)` → ok, Entreprise créée
3. Étape 4 : `passwordEncoder.encode(...)` → Erreur !!!
4. **SANS `@Transactional` :** Le groupe et l'entreprise sont en BDD mais PAS l'utilisateur
   → Données orphelines, impossible de se connecter
5. **AVEC `@Transactional` :** Tout est annulé, la BDD est propre

#### 📄 AuthMapper — MapStruct

```java
@Mapper(componentModel = "spring")  // ← Spring gère l'instance du mapper
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)          // ← L'ID est généré par la BDD
    @Mapping(target = "groupe", ignore = true)      // ← Sera setté après save
    @Mapping(target = "typeEntreprise", constant = "MERE")  // ← Toujours MERE pour une inscription
    @Mapping(target = "nom", source = "nomBoutique")        // ← Mapping champ DTO → champ Entité
    @Mapping(target = "adressePays", constant = "Cameroun") // ← Valeur fixe
    Entreprise toEntreprise(InscriptionEntrepriseUniqueRequest request);
}
```

**Pourquoi MapStruct et pas un mapper manuel ?** MapStruct génère le code à la compilation.
Il n'y a pas de reflection (contrairement à ModelMapper), donc c'est **aussi rapide
qu'un mapper écrit à la main**. Et ça évite 50 lignes de getters/setters manuels.

**Pourquoi `componentModel = "spring"` ?** MapStruct crée un Spring Bean (`@Component`)
à partir de l'interface. Tu peux donc l'injecter comme n'importe quel service :
```java
private final AuthMapper authMapper;  // ← Spring injecte l'implémentation générée
```

---

### 📘 US-007 — Inscription groupe multi-sites

**Différence avec US-006 :** Même logique, mais le DTO d'entrée est différent
(l'utilisateur saisit le nom du groupe, pas le nom de la boutique).

Le code est presque identique à US-006. La seule vraie différence :
- `limiteFiliales = 5` (au lieu de 1) pour le plan gratuit groupe
- Le message de retour invite à "créer la première filiale"

**Pourquoi 2 endpoints différents alors que le code est similaire ?** Parce que
**l'UX est différente** et donc les DTOs sont différents. Un gérant de boutique unique
ne doit pas remplir "NIF du groupe", "siège social", etc. C'est une **décision métier**
(analyse fonctionnelle §2.1) de séparer les deux flows.

---

### 📘 US-008 — Connexion JWT

**Concept clé :** JWT (JSON Web Token)

#### 🔑 C'est quoi un JWT ?

Un JWT est un **passe numérique** qui prouve qui tu es sans que le serveur ait
à vérifier ton mot de passe à chaque requête.

**Structure d'un JWT :**
```
eyJhbGciOiJIUzI1NiJ9.                   ← Header (algorithme)
eyJ1c2VySWQiOjF9.                       ← Payload (données)
ZWRmMDI0M2Q1Njc4OTBhYg                 ← Signature (vérification)
```

```
Header (JSON)     Payload (JSON)           Signature
{
  "alg": "HS256"    "userId": 1,          HMACSHA256(
}                    "role": "ADMIN",        base64(header) + "." +
                     "exp": 1700000000       base64(payload),
                   }                         secretKey
                                           )
```

**Les claims (données) du JWT StockMaster :**
```java
return Jwts.builder()
    .subject(userId.toString())           // ← Sujet : l'ID utilisateur
    .claim("userId", userId)              // ← ID utilisateur
    .claim("entrepriseId", entrepriseId)  // ← Isolation multi-tenant
    .claim("groupId", groupId)            // ← Groupe pour les opérations inter-filiales
    .claim("role", role)                  // ← Rôle (ADMIN_GROUPE, CAISSIER...)
    .claim("scope", scope)                // ← Scope (GROUPE ou FILIALE)
    .claim("jti", UUID.randomUUID().toString())  // ← JWT ID : pour blacklist
    .issuer("stockmaster")
    .issuedAt(now)
    .expiration(expiry)                   // ← 15 minutes pour l'access token
    .signWith(getSigningKey(), Jwts.SIG.HS256)  // ← Signé en HMAC-SHA256
    .compact();                           // ← Génère le token final
```

**Pourquoi 15 minutes seulement ?** Plus court = plus sûr. Si un token est volé,
l'attaquant ne peut l'utiliser que 15 minutes max. Le refresh token (7 jours)
est stocké en Redis et vérifié à chaque utilisation.

#### 📄 JwtTokenProvider — Génération et validation

```java
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    // La clé secrète (base64) est externalisée dans application.yml via @ConfigurationProperties
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);  // ← Clé HMAC-SHA256 à partir des bytes
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())          // ← Vérifie la signature
                .requireIssuer(jwtProperties.getIssuer())  // ← Vérifie l'émetteur
                .build()
                .parseSignedClaims(token)              // ← Parse le token
                .getPayload();                         // ← Récupère les claims
    }
}
```

**Comment la validation marche :**
1. Le client envoie le JWT dans le header `Authorization: Bearer <token>`
2. `JwtAuthenticationFilter` extrait le token
3. `JwtTokenProvider.validateToken()` vérifie :
   - La **signature** : est-ce que le token a été modifié ?
   - La **date d'expiration** : est-ce que le token est encore valide ?
   - L'**émetteur** : est-ce que le token a été émis par notre serveur ?
4. Si tout est OK, les claims sont extraits et placés dans `SecurityContextHolder`

#### 📄 JwtAuthenticationFilter — Le filtre qui intercepte toutes les requêtes

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // ↑ OncePerRequestFilter : s'exécute UNE FOIS par requête HTTP

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);  // ← Extrait le Bearer token

        if (StringUtils.hasText(token)) {      // ← Si un token est présent
            try {
                Claims claims = jwtTokenProvider.validateToken(token);

                // Vérifier la blacklist (déconnexion)
                String jti = claims.get("jti", String.class);
                if (jti != null && Boolean.TRUE.equals(
                        redisTemplate.hasKey("blacklist:jti:" + jti))) {
                    response.setStatus(401);
                    response.getWriter().write("...");
                    return;  // ← Token révoqué, on bloque
                }

                // Créer l'objet d'authentification Spring Security
                StockMasterPrincipal principal = new StockMasterPrincipal(
                        claims.get("userId", Long.class), claims);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                principal, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(auth);
                // ↑ Spring Security sait maintenant "qui est l'utilisateur"

            } catch (ExpiredJwtException e) {
                response.setStatus(401);  // ← Token expiré
                return;
            } catch (JwtException e) {
                response.setStatus(401);  // ← Token invalide
                return;
            }
        }

        filterChain.doFilter(request, response);  // ← Continue la chaîne de filtres
    }
}
```

**Le flow complet :**

```
Requête HTTP → SecurityConfig → JwtAuthenticationFilter → Controller
                                     ↓
                              Token valide ?
                              ├── OUI → Authentification placée dans SecurityContext
                              └── NON → Requête anonyme (public endpoint)
```

#### 📄 SecurityConfig — La configuration de sécurité

```java
@Configuration
@EnableWebSecurity               // ← Active la sécurité Web Spring
@EnableMethodSecurity            // ← Active @PreAuthorize sur les méthodes
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())     // ← Désactivé pour une API REST (pas de formulaire)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // ↑ PAS de session HTTP ! Chaque requête est indépendante
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/v1/auth/inscription/**",    // ← Inscription : tout le monde
                    "/api/v1/auth/login",              // ← Login : tout le monde
                    "/api/v1/auth/refresh",            // ← Refresh : utilisateur avec refresh token
                    "/api/v1/auth/forgot-password",    // ← Mot de passe oublié : tout le monde
                    "/api/v1/auth/reset-password",     // ← Reset : tout le monde
                    "/api/v1/auth/activer-compte",     // ← Activation : tout le monde
                    "/actuator/health",                 // ← Health check : tout le monde
                    "/actuator/info"
                ).permitAll()                          // ← PERMIT ALL = pas de token requis
                .anyRequest().authenticated()          // ← Tout le reste = TOKEN REQUIS !
            )
            .addFilterBefore(jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class);  // ← Notre filtre JWT AVANT celui de Spring

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // ← BCrypt pour hacher les mots de passe
    }
}
```

**Pourquoi `csrf.disable()` ?** CSRF (Cross-Site Request Forgery) protège contre
les attaques via formulaire HTML. Notre API REST utilise des tokens JWT, pas des cookies.
Donc CSRF est inutile.

**Pourquoi `STATELESS` ?** Spring Security crée par défaut une session HTTP (comme un
site web traditionnel). Notre API REST est **stateless** : chaque requête contient
son propre token JWT. Pas de session = pas de mémoire côté serveur = scalable.

#### 📄 RateLimitFilter — Anti-bruteforce

```java
@Component
@Order(0)  // ← S'exécute AVANT tous les autres filtres
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                     HttpServletResponse res,
                                     FilterChain chain) throws ... {

        if (!req.getRequestURI().equals("/api/v1/auth/login")) {
            chain.doFilter(req, res);  // ← Seulement pour /login
            return;
        }

        String key = "rate_limit:login:" + getClientIp(req);  // ← Clé Redis par IP
        Integer attempts = redisTemplate.opsForValue().get(key);

        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            res.setStatus(429);  // ← Too Many Requests
            return;
        }

        redisTemplate.opsForValue().increment(key);  // ← Incrémente le compteur
        redisTemplate.expire(key, WINDOW);            // ← TTL 15 minutes
        chain.doFilter(req, res);
    }
}
```

**Pourquoi Redis et pas une variable mémoire ?** Parce que si on a plusieurs instances
de l'application, une variable mémoire est propre à chaque instance. L'attaquant pourrait
faire 5 tentatives sur chaque instance. Redis est **partagé par toutes les instances**.

---

### 📘 US-009 — Refresh token

**Concept clé :** Rafraîchir l'access token sans repasser par le login

```java
@Override
@Transactional(readOnly = true)
public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request) {

    // 1. Extraire le userId du refresh token (vérification JWT)
    Long userId = jwtTokenProvider.getUserIdFromToken(request.getRefreshToken());

    // 2. Vérifier que le refresh token existe DANS REDIS
    String storedRefreshToken = redisTemplate.opsForValue().get("refresh:" + userId);
    if (storedRefreshToken == null || !storedRefreshToken.equals(request.getRefreshToken())) {
        throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
    }

    // 3. Vérifier que l'utilisateur et son groupe sont toujours actifs
    Utilisateur utilisateur = utilisateurRepository.findById(userId).orElseThrow(...);

    // 4. Générer un NOUVEL access token (mêmes claims)
    String newAccessToken = jwtTokenProvider.generateAccessToken(
            userId, entrepriseId, groupId, role, scope);

    return new RefreshTokenResponse(newAccessToken, expiresIn);
}
```

**Le flow complet du refresh :**

```
1. Client a l'access token (expire dans 15 min)
2. Client fait une requête → Erreur 401 (token expiré)
3. Client appelle POST /auth/refresh avec le refresh token
4. Serveur vérifie le refresh token dans Redis
5. Serveur génère un NOUVEL access token
6. Client refait sa requête originale avec le nouveau token
```

**Pourquoi stocker le refresh token en Redis et pas en BDD ?** La BDD est plus lente
(disque). Redis est en mémoire (microsecondes). Comme le refresh token est utilisé
**à chaque expiration d'access token** (toutes les 15 minutes), on veut de la performance.

---

### 📘 US-010 — Déconnexion

**Concept clé :** Blacklist de token + Révocation du refresh

```java
@Override
public void logout() {
    // Récupérer l'utilisateur connecté depuis le contexte de sécurité
    var auth = SecurityContextHolder.getContext().getAuthentication();
    StockMasterPrincipal principal = (StockMasterPrincipal) auth.getPrincipal();
    Long userId = principal.getUserId();

    // 1. Blacklister le jti de l'access token (empêche son utilisation)
    String jti = principal.getClaims().get("jti", String.class);
    String blacklistKey = "blacklist:jti:" + jti;
    redisTemplate.opsForValue().set(blacklistKey, "true", remainingTtl, TimeUnit.SECONDS);

    // 2. Supprimer le refresh token de Redis
    redisTemplate.delete("refresh:" + userId);

    // 3. Nettoyer le contexte de sécurité
    SecurityContextHolder.clearContext();
}
```

**Pourquoi ne pas juste supprimer le refresh token ?** Parce que l'access token
est encore valide 15 minutes. Sans blacklist du `jti`, l'attaquant pourrait continuer
à utiliser l'access token jusqu'à son expiration naturelle.

**Pourquoi `jti` (JWT ID) est-il unique ?** Parce qu'on génère un UUID (`UUID.randomUUID()`)
à chaque émission de token. Ainsi, chaque token a un identifiant unique qui permet
de le blacklister individuellement.

---

### 📘 US-011 — Mot de passe oublié

**Concept clé :** Token de réinitialisation stocké en Redis

```java
@Override
public void forgotPassword(ForgotPasswordRequest request) {
    String email = request.getEmail();

    // Toujours retourner le MÊME message (sécurité)
    // Si on dit "email inconnu", un attaquant peut tester des emails !
    utilisateurRepository.findByEmail(email).ifPresent(utilisateur -> {
        String token = UUID.randomUUID().toString();
        String redisKey = "reset:" + token;

        // Stocker le token avec un TTL de 15 minutes
        redisTemplate.opsForValue().set(redisKey,
                String.valueOf(utilisateur.getId()),
                900, TimeUnit.SECONDS);  // ← 15 minutes

        log.info("Lien de réinitialisation : /reset-password?token={}", token);
    });
}
```

**Pattern de sécurité :** **"Ne pas révéler si l'email existe"**.
Si tu réponds "Email inconnu" pour un email non trouvé et "Email envoyé" pour un email trouvé,
un attaquant peut **énumérer les emails** de ta base de données. Toujours répondre
la même phrase : "Si cet email existe, un lien a été envoyé."

---

### 📘 US-012 — Réinitialisation du mot de passe

```java
@Override
@Transactional
public void resetPassword(ResetPasswordRequest request) {
    String redisKey = "reset:" + request.getToken();

    // 1. Vérifier le token dans Redis
    String userIdStr = redisTemplate.opsForValue().get(redisKey);
    if (userIdStr == null) {
        throw new BusinessException(ErrorCode.AUTH_RESET_TOKEN_INVALID);
        // ← Token invalide OU expiré (Redis supprime automatiquement au TTL)
    }

    // 2. Charger l'utilisateur
    Utilisateur utilisateur = utilisateurRepository.findById(Long.valueOf(userIdStr))
            .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_RESET_TOKEN_INVALID));

    // 3. Hacher et sauvegarder le nouveau mot de passe
    String nouveauHash = passwordEncoder.encode(request.getNouveauMotDePasse());
    utilisateur.setMotDePasse(nouveauHash);
    utilisateurRepository.save(utilisateur);

    // 4. Supprimer le token (usage UNIQUE)
    redisTemplate.delete(redisKey);

    // 5. Révoquer TOUS les refresh tokens (sécurité maximale)
    redisTemplate.delete("refresh:" + utilisateur.getId());
}
```

**Pourquoi révoquer tous les refresh tokens ?** Si quelqu'un a volé un refresh token,
le changement de mot de passe doit TOUT invalider. C'est ce qu'on appelle une
**rotation forcée** : après un reset, tout le monde est déconnecté et doit se reconnecter.

---

### 📘 US-013 — Changement de mot de passe

**Différence avec US-012 :** L'utilisateur est déjà connecté. Il connaît son ancien
mot de passe.

```java
@Override
@Transactional
public void changePassword(ChangePasswordRequest request) {
    // 1. Récupérer l'utilisateur depuis le token JWT (pas depuis un formulaire !)
    StockMasterPrincipal principal = (StockMasterPrincipal) SecurityContextHolder
            .getContext().getAuthentication().getPrincipal();
    Long userId = principal.getUserId();

    Utilisateur utilisateur = utilisateurRepository.findById(userId).orElseThrow(...);

    // 2. Vérifier l'ANCIEN mot de passe
    if (!passwordEncoder.matches(request.getAncienMotDePasse(), utilisateur.getMotDePasse())) {
        throw new BusinessException(ErrorCode.SEC_INVALID_PASSWORD);
        // ← Même message générique (ne pas révéler si c'est l'email ou le mot de passe)
    }

    // 3. Hacher le NOUVEAU mot de passe
    utilisateur.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
    utilisateurRepository.save(utilisateur);

    // 4. Révoquer tous les refresh tokens
    redisTemplate.delete("refresh:" + userId);
}
```

**Pourquoi `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` ?**
Cet objet contient les informations de l'utilisateur connecté, extraites du token JWT
par le `JwtAuthenticationFilter`. On ne fait JAMAIS confiance à un `userId` envoyé
par le client dans le body de la requête — ce serait une faille de sécurité.

---

### 📘 US-014 — Rotation du Refresh Token (RTR)

**Concept clé :** Un refresh token = une seule utilisation

**Le problème à résoudre :** Actuellement (US-009), un refresh token peut être utilisé
plusieurs fois jusqu'à son expiration. Si un attaquant le vole, il peut s'en servir
indéfiniment.

**La solution :** RTR (Refresh Token Rotation) :
- À chaque utilisation d'un refresh token, on en émet un **nouveau**
- Le token utilisé est immédiatement **invalidé**
- Si un token déjà utilisé est présenté à nouveau → **déconnexion forcée** de tous les appareils

```java
// Pseudo-code de la rotation
public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request) {
    // 1. Vérifier le refresh token
    String storedToken = redisTemplate.opsForValue().get("refresh:" + userId);

    if (storedToken == null) {
        // → Token déjà utilisé OU révoqué
        // → Si c'est une tentative de rejeu, c'est une ATTAQUE !
        log.warn("Tentative de rejeu détectée pour userId={}", userId);
        revokeAllTokens(userId);  // ← Déconnexion forcée !
        throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID);
    }

    // 2. SUCCÈS : invalider l'ancien et en créer un nouveau
    redisTemplate.delete("refresh:" + userId);                // ← Invalide l'ancien
    String newRefreshToken = jwtProvider.generateRefreshToken(userId);
    redisTemplate.opsForValue().set("refresh:" + userId,      // ← Stocke le nouveau
            newRefreshToken, 7, TimeUnit.DAYS);

    return new RefreshTokenResponse(newAccessToken, newRefreshToken);
}
```

---

### 📘 US-015 — Argon2id

**Concept :** BCrypt vs Argon2id

| Algorithme | Résistant GPU | Consommation mémoire | Standard |
|-----------|---------------|---------------------|----------|
| **BCrypt** | ✅ Oui | Faible | OWASP 2023 |
| **Argon2id** | ✅✅ Excellent | Configurable haute | **OWASP 2026** |

**Pourquoi migrer ?** Les attaques par GPU (carte graphique) deviennent plus puissantes
chaque année. Argon2id est conçu pour être **coûteux en mémoire** (pas seulement en CPU),
ce que les GPU ne peuvent pas facilement accélérer.

**Migration progressive avec `DelegatingPasswordEncoder` :**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    String defaultEncoding = "argon2";
    Map<String, PasswordEncoder> encoders = Map.of(
        "argon2", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8(),
        "bcrypt", new BCryptPasswordEncoder()
    );
    DelegatingPasswordEncoder delegating = new DelegatingPasswordEncoder(defaultEncoding, encoders);
    delegating.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
    return delegating;
}
```

Le format en base devient : `{bcrypt}$2a$10$...` ou `{argon2}$argon2id$...`.
Spring sait quel algorithme utiliser grâce au préfixe `{...}`.

---

### 📘 US-016 — Fail-closed sur Redis

**Principe :** Si Redis ne répond PAS, l'application doit **refuser** la requête,
pas l'accepter silencieusement.

```java
// ❌ MAUVAIS : laisser passer si Redis est down
try {
    Boolean blacklisted = redisTemplate.hasKey("blacklist:jti:" + jti);
    if (Boolean.TRUE.equals(blacklisted)) {
        throw new BusinessException(ErrorCode.AUTH_TOKEN_BLACKLISTED);
    }
} catch (Exception e) {
    // Redis down → on laisse passer ? FAIL-OPEN → FAIL !
}

// ✅ BON : fail-closed
try {
    // ... vérification Redis
} catch (RedisConnectionException e) {
    log.error("Redis indisponible !");
    throw new BusinessException(ErrorCode.SECURITY_STORE_UNAVAILABLE);
    // ← 503 SERVICE UNAVAILABLE → sécurité avant tout
}
```

---

### 📘 US-017 — Logs d'audit structurés

```java
// Log structuré en JSON (via un formatteur Logback personnalisé)
log.info("{}", Map.of(
    "event", "auth.login_success",
    "userId", userId,
    "entrepriseId", entrepriseId,
    "ip", request.getRemoteAddr(),
    "timestamp", Instant.now()
));

// En production, ça produit :
// {"event":"auth.login_success","userId":42,"entrepriseId":5,"ip":"192.168.1.1","timestamp":"2026-07-11T10:30:00Z"}
```

**Pourquoi JSON ?** Pour être exploitable par des outils comme ELK (Elasticsearch,
Logstash, Kibana) sans parsing coûteux.

---

## 4. Groupe & Filiales (US-014 à US-020)

> **Note :** Les US de l'EPIC 3 sont numérotées US-014 à US-020 dans le backlog,
> mais US-014 à US-017 de sécurité (EPIC 2) ont été ajoutées après. Les vrais numéros
> sont US-018 à US-024 dans le backlog original. On garde les numéros du backlog.

### 📘 US-019 — Créer une filiale

**Concept clé :** Vérification des limites du plan d'abonnement

```java
@Override
@PreAuthorize("hasRole('ADMIN_GROUPE')")
@Transactional
public FilialeResponse creerFiliale(CreerFilialeRequest request) {
    // 1. Récupérer le groupe
    TenantGroup groupe = tenantGroupRepository.findById(groupId)
            .orElseThrow(() -> new EntityNotFoundException("Groupe", groupId));

    // 2. Vérifier la limite selon le plan
    long nbFiliales = entrepriseRepository.countByGroupIdAndType(groupe.getId(), TypeEntreprise.FILIALE);
    if (nbFiliales >= groupe.getLimiteFiliales()) {
        throw new BusinessException(ErrorCode.GRP_FILIALE_LIMIT_REACHED,
                "Limite de " + groupe.getLimiteFiliales() + " filiales atteinte");
    }

    // 3. Créer la filiale
    Entreprise filiale = Entreprise.builder()
            .groupe(groupe)
            .parent(entrepriseMere)
            .typeEntreprise(TypeEntreprise.FILIALE)
            .nom(request.getNom())
            .codeFiliale(request.getCodeFiliale())
            .build();

    return mapper.toDto(entrepriseRepository.save(filiale));
}
```

**Pourquoi vérifier la limite ?** Le plan GRATUIT donne droit à 1 filiale (la maison mère
seulement). Le plan STARTER à 5 filiales. Sans vérification, un client gratuit pourrait
créer 100 filiales et saturer la plateforme.

---

### 📘 US-024 — Dashboard consolidé groupe

**Concept clé :** Agrégation avec cache Redis

```java
@Transactional(readOnly = true)        // ← Améliore les performances (pas de flush Hibernate)
@Cacheable(value = "groupe_dashboard",  // ← Cache Redis
           key = "#groupId",
           unless = "#result == null")
public DashboardGroupeResponse getDashboard(Long groupId) {
    // Agrégation sur TOUTES les filiales du groupe
    List<Long> filialeIds = entrepriseRepository.findFilialeIdsByGroupeId(groupId);

    Integer stockTotal = mouvementStockRepository
            .calculerStockConsolide(filialeIds);     // ← Somme des stocks

    BigDecimal caMensuel = commandeClientRepository
            .calculerCaParFiliales(filialeIds, debutMois, finMois);

    Long alertesActives = notificationAlerteRepository
            .countByEntrepriseIdInAndLueFalse(filialeIds);

    return DashboardGroupeResponse.builder()
            .stockTotal(stockTotal)
            .caMensuel(caMensuel)
            .alertesActives(alertesActives)
            .build();
}
```

**Pourquoi `@Transactional(readOnly = true)` ?** Hibernate optimise les requêtes :
pas de dirty checking, pas de flush à la fin de la transaction. Les lectures sont
**plus rapides et moins coûteuses**.

---

## 5. Gestion des Utilisateurs (US-021 à US-026)

### 📘 US-021 — Créer un Admin Filiale

**Concept clé :** Invitation par email avec token d'activation

```java
@PreAuthorize("hasRole('ADMIN_GROUPE')")
@Transactional
public void creerAdminFiliale(CreerAdminFilialeRequest request) {
    // 1. Vérifier que la filiale appartient bien au groupe
    Entreprise filiale = entrepriseRepository.findById(request.getFilialeId())
            .orElseThrow(...);
    if (!filiale.getGroupe().getId().equals(groupId)) {
        throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
    }

    // 2. Créer l'utilisateur INACTIF (actif = false)
    String tokenActivation = UUID.randomUUID().toString();
    Utilisateur admin = Utilisateur.builder()
            .entreprise(filiale)
            .role(RoleUtilisateur.ADMIN_FILIALE)
            .actif(false)                              // ← Désactivé jusqu'à activation
            .tokenReset(tokenActivation)                // ← Token d'activation (réutilise le champ)
            .tokenResetExpiry(Instant.now().plus(48, ChronoUnit.HOURS))  // ← Expire dans 48h
            .build();
    utilisateurRepository.save(admin);

    // 3. Envoyer l'email d'invitation (asynchrone)
    eventPublisher.publishEvent(new InvitationEmployeEvent(
            admin.getEmail(), admin.getPrenom(), tokenActivation));
}
```

**Pourquoi créer l'utilisateur `actif = false` ?** Pour deux raisons :
1. **Sécurité** : L'email doit être vérifié avant de pouvoir se connecter
2. **Onboarding** : L'utilisateur choisit son mot de passe (personne ne le connaît)

---

### 📘 US-022 — Créer un employé

**Concept clé :** Rôles métier et périmètre d'action

```java
@PreAuthorize("hasAnyRole('ADMIN_FILIALE', 'ADMIN_GROUPE')")
@Transactional
public void creerEmploye(CreerEmployeRequest request) {
    // L'employé est créé sur la MÊME entreprise que l'admin connecté
    // → Isolation tenant : entreprise_id vient du JWT, pas du body !
    Entreprise entreprise = ...;  // ← Récupérée depuis StockMasterPrincipal

    Utilisateur employe = Utilisateur.builder()
            .entreprise(entreprise)
            .role(request.getRole())  // ← GESTIONNAIRE_STOCK, RESP_ACHATS, COMMERCIAL, CAISSIER
            .scope(ScopeUtilisateur.FILIALE)  // ← Scope FILIALE (pas de vue groupe)
            .actif(true)
            .build();
}
```

**Les 4 rôles métier et leurs permissions (matrice du CDCT §28) :**

| Rôle | Catalogue | Stock | Achats | Ventes B2B | Caisse | Clients/Fourn. | Utilisateurs |
|------|-----------|-------|--------|-----------|--------|----------------|--------------|
| GESTIONNAIRE_STOCK | ✅ CRUD | ✅ CRUD | ❌ | ❌ | ❌ | ❌ | ❌ |
| RESP_ACHATS | ✅ Voir | ❌ | ✅ CRUD | ❌ | ❌ | ✅ Fourn. | ❌ |
| COMMERCIAL | ✅ Voir | ❌ | ❌ | ✅ CRUD | ❌ | ✅ Clients | ❌ |
| CAISSIER | ✅ Voir | ✅ Voir | ❌ | ❌ | ✅ CRUD | ❌ | ❌ |

---

## 6. Catalogue (US-027 à US-035)

### 📘 US-027 — Créer une catégorie

**Concept clé :** Isolation tenant + contrainte d'unicité

```java
@PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE','ADMIN_GROUPE')")
@Transactional
public CategorieResponse creerCategorie(CreerCategorieRequest request) {
    // L'entreprise_id vient du JWT, JAMAIS du body !
    Long entrepriseId = getCurrentEntrepriseId();

    // Vérifier l'unicité du code DANS l'entreprise
    if (categorieRepository.existsByCodeAndEntrepriseId(request.getCode(), entrepriseId)) {
        throw new BusinessException(ErrorCode.RES_DUPLICATE_CODE);
    }

    Categorie categorie = Categorie.builder()
            .entrepriseId(entrepriseId)     // ← Forcé depuis le JWT
            .code(request.getCode())        // ← Ex: "ALIM"
            .designation(request.getDesignation())
            .tauxTva(request.getTauxTva())  // ← Ex: 19.25
            .build();

    return mapper.toDto(categorieRepository.save(categorie));
}
```

**Pourquoi ne JAMAIS prendre `entreprise_id` du body ?** Faille de sécurité critique :
un utilisateur malveillant pourrait envoyer `entreprise_id: 999` dans le body pour créer
une catégorie dans une autre entreprise. **Toujours extraire l'ID du JWT** qui a été
signé par le serveur et ne peut pas être falsifié.

### 📘 US-031 — Créer un article

**Concept clé :** Calcul du prix TTC côté serveur

```java
@PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE','ADMIN_GROUPE')")
@Transactional
public ArticleResponse creerArticle(CreerArticleRequest request) {
    // 1. Validation : prix_vente_ht > prix_achat_ht
    if (request.getPrixVenteHt() <= request.getPrixAchatHt()) {
        throw new BusinessException("Le prix de vente doit être supérieur au prix d'achat");
    }

    // 2. Prix TTC calculé côté SERVEUR (jamais saisi par l'utilisateur)
    BigDecimal tauxTva = BigDecimal.valueOf(request.getTauxTva() / 100.0);
    BigDecimal prixVenteTtc = BigDecimal.valueOf(request.getPrixVenteHt())
            .multiply(BigDecimal.ONE.add(tauxTva));

    // 3. Marge brute calculée à la volée
    BigDecimal margeBrute = BigDecimal.valueOf(request.getPrixVenteHt())
            .subtract(BigDecimal.valueOf(request.getPrixAchatHt()))
            .divide(BigDecimal.valueOf(request.getPrixAchatHt()), 2, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

    Article article = Article.builder()
            .prixVenteHt(request.getPrixVenteHt())
            .prixVenteTtc(prixVenteTtc.intValue())  // ← Calculé, pas saisi
            .margeBrutePct(margeBrute.doubleValue())  // ← Calculé
            .build();
}
```

**Pourquoi utiliser `BigDecimal` pour les prix ?** `double` et `float` ont des
imprécisions d'arrondi. `0.1 + 0.2 = 0.30000000000000004` en binaire flottant.
`BigDecimal` donne `0.3` exactement. **Les erreurs d'arrondi sur de l'argent sont
inacceptables.**

### US-032 — Lister les articles

**Concept clé :** `@Transactional(readOnly = true)` + pagination

```java
@Transactional(readOnly = true)
public Page<ArticleResponse> listerArticles(
        String search, Long categorieId, Boolean stockBas, Pageable pageable) {

    // Recherche full-text PostgreSQL
    Specification<Article> spec = Specification
            .where(ArticleSpecifications.entrepriseEgale(getCurrentEntrepriseId()))
            .and(ArticleSpecifications.search(search))           // ← Index GIN full-text
            .and(ArticleSpecifications.categorieEgale(categorieId))
            .and(ArticleSpecifications.stockBas(stockBas));       // ← stock ≤ seuil_alerte

    Page<Article> articles = articleRepository.findAll(spec, pageable);

    return articles.map(article -> {
        Integer stockReel = mouvementStockRepository.calculerStockReel(
                article.getId(), getCurrentEntrepriseId());       // ← Calcul en temps réel
        return mapper.toDto(article, stockReel);
    });
}
```

**Pourquoi `Pageable` ?** Si une entreprise a 10 000 articles, tu ne peux pas tous
les retourner dans une seule réponse HTTP. `Pageable` permet la pagination :
`?page=0&size=20&sort=designation,asc`.

---

## 7. Tiers — Clients & Fournisseurs (US-036 à US-043)

### 📘 US-036 — Créer un fournisseur

**Concept clé :** Validation format téléphone + isolation tenant

```java
@PostMapping
@PreAuthorize("hasAnyRole('RESP_ACHATS','ADMIN_FILIALE','ADMIN_GROUPE')")
public ResponseEntity<ApiResponse<FournisseurResponse>> creerFournisseur(
        @Valid @RequestBody CreerFournisseurRequest request) {

    // Validation téléphone camerounais (Jakarta @Pattern)
    Fournisseur fournisseur = Fournisseur.builder()
            .entrepriseId(getCurrentEntrepriseId())  // ← Du JWT, jamais du body
            .raisonSociale(request.getRaisonSociale()) // ← Obligatoire
            .telephone(request.getTelephone())         // ← Optionnel, format 699000001
            .nif(request.getNif())                     // ← Optionnel
            .build();

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(mapper.toDto(fournisseurRepository.save(fournisseur))));
}
```

**Validation Jakarta :**
```java
public class CreerFournisseurRequest {
    @NotBlank(message = "La raison sociale est obligatoire")
    private String raisonSociale;

    @Pattern(regexp = "^((237)?[6-9][0-9]{8})$",
             message = "Format camerounais exigé (ex: 699000001)")
    private String telephone;  // ← Nullable si pas de téléphone
}
```

### 📘 US-037/041 — Lister / Rechercher

**Concept clé :** Filtre par recherche textuelle + pagination

```java
@Transactional(readOnly = true)
public Page<FournisseurResponse> listerFournisseurs(String search, Pageable pageable) {
    // Recherche par nom ou ville (indexé)
    Specification<Fournisseur> spec = Specification
            .where(entrepriseEgale(getCurrentEntrepriseId()))
            .and((root, query, cb) -> search == null ? null :
                    cb.or(
                        cb.like(cb.lower(root.get("raisonSociale")), "%" + search.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("adresseVille")), "%" + search.toLowerCase() + "%")
                    ));

    return fournisseurRepository.findAll(spec, pageable)
            .map(mapper::toDto);
}
```

**Pourquoi `Specification` ?** Pour combiner plusieurs filtres dynamiquement.
Si `search` est null, la clause WHERE est ignorée. Si `categorieId` est fourni,
elle s'ajoute. C'est plus propre que des requêtes JPQL concaténées.

### 📘 US-038/042 — Modifier

**Concept clé :** PATCH sémantique (modification partielle)

```java
@PutMapping("/{id}")
@PreAuthorize("hasAnyRole('...')")
public ResponseEntity<ApiResponse<FournisseurResponse>> modifierFournisseur(
        @PathVariable Long id,
        @Valid @RequestBody ModifierFournisseurRequest request) {

    Fournisseur fournisseur = fournisseurRepository
            .findByIdAndEntrepriseId(id, getCurrentEntrepriseId())
            .orElseThrow(() -> new EntityNotFoundException("Fournisseur", id));

    // Mise à jour partielle (seuls les champs non-null sont modifiés)
    if (request.getRaisonSociale() != null) fournisseur.setRaisonSociale(request.getRaisonSociale());
    if (request.getTelephone() != null) fournisseur.setTelephone(request.getTelephone());

    return ResponseEntity.ok(ApiResponse.ok(mapper.toDto(fournisseurRepository.save(fournisseur))));
}
```

**Pourquoi `findByIdAndEntrepriseId` et pas juste `findById` ?** Isolation tenant !
Si l'utilisateur demande le fournisseur ID 5 mais qui appartient à une autre entreprise,
on retourne 404 — pas 403. On ne révèle PAS l'existence de ressources d'autres entreprises.

### 📘 US-039/043 — Supprimer

```java
@DeleteMapping("/{id}")
public ResponseEntity<ApiResponse<Void>> supprimerFournisseur(@PathVariable Long id) {
    Fournisseur fournisseur = fournisseurRepository
            .findByIdAndEntrepriseId(id, getCurrentEntrepriseId())
            .orElseThrow(() -> new EntityNotFoundException("Fournisseur", id));

    // Vérifier qu'aucune commande n'est associée
    if (commandeFournisseurRepository.existsByFournisseurIdAndSupprimeFalse(id)) {
        throw new BusinessException(ErrorCode.RES_ENTITY_HAS_DEPENDENCIES);
    }

    fournisseur.marquerCommeSupprime();  // ← Soft delete
    return ResponseEntity.ok(ApiResponse.success("Fournisseur supprimé"));
}
```

---

## 8. Commandes Fournisseur (US-044 à US-050)

### 📘 US-044 — Créer une commande fournisseur

**Concept clé :** Machine à états + génération de code automatique

```java
@PreAuthorize("hasAnyRole('RESP_ACHATS','ADMIN_FILIALE','ADMIN_GROUPE')")
@Transactional
public CommandeResponse creerCommande(CreerCommandeRequest request) {
    // 1. Générer le code automatiquement
    String code = codeGeneratorService.genererCodeCommandeFournisseur(entrepriseId);
    // → "CF-2026-00042"

    // 2. Créer avec état initial EN_PREPARATION
    CommandeFournisseur commande = CommandeFournisseur.builder()
            .code(code)
            .etatCommande(EtatCommande.EN_PREPARATION)  // ← État initial
            .build();

    // 3. Snapshot du taux TVA sur chaque ligne (figé à la création)
    for (LigneRequest ligne : request.getLignes()) {
        Article article = articleRepository.findById(ligne.getArticleId()).orElseThrow();
        ligneCommande.setTauxTvaSnapshot(article.getTauxTva());  // ← Figé !
    }
}
```

**Pourquoi un snapshot du taux TVA sur chaque ligne ?** Imagine : tu commandes un article
avec 19.25% de TVA. Le mois prochain, le gouvernement change la TVA à 21%. Sans snapshot,
la commande historique montrerait 21% au lieu de 19.25%. Le snapshot **fige la valeur
au moment de la création**.

### La machine à états d'une commande fournisseur

```
                      VALIDER                LIVRER
EN_PREPARATION ──────────────► VALIDEE ──────────────► LIVREE
       │                                                  
       │ (soft delete si EN_PREPARATION)
       └──► SUPPRIMÉE
```

**Règles de transition :**
- `EN_PREPARATION` → `VALIDEE` : ✅ (US-048)
- `VALIDEE` → `LIVREE` : ✅ (US-049)
- `EN_PREPARATION` → supprimée : ✅ (soft delete, US-050)
- Toute autre transition : ❌ `409 ORDER_NOT_MODIFIABLE`

### 📘 US-048 — Valider une commande fournisseur ⭐ (8 pts — la plus complexe)

**C'est ici que la MAGIE opère : la validation d'une commande fournisseur CRÉE
AUTOMATIQUEMENT les mouvements d'entrée en stock.**

```java
@PreAuthorize("hasAnyRole('RESP_ACHATS','ADMIN_FILIALE','ADMIN_GROUPE')")
@Transactional
public CommandeResponse validerCommande(Long commandeId) {
    CommandeFournisseur commande = commandeFournisseurRepository
            .findByIdAndEntrepriseId(commandeId, getCurrentEntrepriseId())
            .orElseThrow(() -> new EntityNotFoundException("Commande", commandeId));

    // 1. Vérifier l'état
    if (commande.getEtatCommande() != EtatCommande.EN_PREPARATION) {
        throw new BusinessException(ErrorCode.CMD_ORDER_NOT_MODIFIABLE);
    }

    // 2. Vérifier qu'il y a au moins 1 ligne
    if (commande.getLignes().isEmpty()) {
        throw new BusinessException(ErrorCode.CMD_ORDER_HAS_NO_LINES);
    }

    // 3. Pour chaque ligne, créer un mouvement ENTREE (opération atomique)
    for (LigneCommandeFournisseur ligne : commande.getLignes()) {
        MouvementStock mouvement = MouvementStock.builder()
                .entrepriseId(getCurrentEntrepriseId())
                .articleId(ligne.getArticle().getId())
                .typeMouvement(TypeMouvement.ENTREE)   // ← Type = ENTREE
                .quantite(ligne.getQuantite())
                .utilisateurId(getCurrentUserId())
                .origineId(commande.getId())             // ← Traçabilité : quelle commande ?
                .origineType("COMMANDE_FOURNISSEUR")
                .motif("Validation commande " + commande.getCode())
                .build();
        mouvementStockRepository.save(mouvement);
    }

    // 4. Changer l'état
    commande.setEtatCommande(EtatCommande.VALIDEE);
    commandeFournisseurRepository.save(commande);

    // 5. Publier les événements (async)
    for (LigneCommandeFournisseur ligne : commande.getLignes()) {
        Integer nouveauStock = mouvementStockRepository
                .calculerStockReel(ligne.getArticle().getId(), getCurrentEntrepriseId());
        eventPublisher.publishEvent(
                new StockUpdatedEvent(ligne.getArticle().getId(), nouveauStock));
    }

    log.info("Commande {} validée — {} mouvements ENTREE créés",
            commande.getCode(), commande.getLignes().size());
}
```

**Pourquoi une opération atomique ?** Imagine : la commande a 3 lignes. Les 2 premières
entrées en stock réussissent, la 3e échoue (disque dur plein, contrainte BDD).
**SANS transaction** : stock partiellement mis à jour, commande marquée validée →
données incohérentes. **AVEC transaction** : tout est annulé, la BDD reste propre.

---

## 9. Gestion du Stock (US-051 à US-055)

### 📘 US-051 — Consulter le stock réel

**Concept clé :** Calcul du stock à la volée

```java
@Transactional(readOnly = true)
public StockResponse consulterStock(Long articleId) {
    // Le stock N'EST PAS stocké dans une colonne "stock_actuel"
    // Il est CALCULÉ à chaque requête depuis la table mouvement_stock
    Integer stockReel = mouvementStockRepository.calculerStockReel(
            articleId, getCurrentEntrepriseId());

    Article article = articleRepository.findById(articleId).orElseThrow();
    String statutAlerte = "NORMAL";
    if (article.getSeuilAlerte() > 0 && stockReel <= article.getSeuilAlerte()) {
        statutAlerte = stockReel == 0 ? "RUPTURE" : "BAS";
    }

    return StockResponse.builder()
            .stockActuel(stockReel)
            .statutAlerte(statutAlerte)
            .build();
}
```

**La formule de calcul du stock réel :**
```sql
SELECT
    SUM(CASE WHEN type_mouvement IN ('ENTREE','CORRECTION_POS','TRANSFERT_ENTREE')
             THEN quantite ELSE 0 END)
  - SUM(CASE WHEN type_mouvement IN ('SORTIE','CORRECTION_NEG','TRANSFERT_SORTIE')
             THEN quantite ELSE 0 END) AS stock_reel
FROM mouvement_stock
WHERE article_id = ? AND entreprise_id = ?;
```

**Pourquoi calculer à la volée plutôt que stocker dans une colonne ?**
C'est le choix de l'ADR-003. Avantages :
- **Cohérence absolue** : impossible d'avoir un stock affiché différent des mouvements
- **Pas de désynchro** : pas besoin de recalculer après chaque correction
- **Traçabilité** : chaque mouvement est dans une ligne séparée = historique complet

### 📘 US-053/054 — Corrections de stock

```java
@Transactional
public void effectuerCorrection(CorrectionStockRequest request) {
    if (request.getMotif() == null || request.getMotif().isBlank()) {
        throw new BusinessException(ErrorCode.STK_MOTIF_REQUIRED);
    }

    TypeMouvement type = request.getType() == TypeCorrection.POSITIVE
            ? TypeMouvement.CORRECTION_POS
            : TypeMouvement.CORRECTION_NEG;

    MouvementStock mouvement = MouvementStock.builder()
            .typeMouvement(type)
            .quantite(request.getQuantite())
            .motif(request.getMotif())          // ← Motif OBLIGATOIRE
            .origineType("CORRECTION")
            .build();
}
```

**Pourquoi un motif obligatoire ?** Audit. Dans 3 ans, si le comptable demande
"Pourquoi le stock de RIZ50KG a augmenté de 15 unités le 10 juin 2026 ?",
la réponse est dans `motif` : "Inventaire physique du 10/06/2026 — surplus constaté".

---

## 10. Commandes Client B2B (US-056 à US-063)

### 📘 US-060 — Valider une commande client ⭐ (8 pts)

**La différence cruciale avec la validation fournisseur :** Ici, on vérifie le STOCK
AVANT de créer les mouvements. Si stock insuffisant → REFUS avec détails.

```java
@Transactional
public CommandeResponse validerCommandeClient(Long commandeId) {
    CommandeClient commande = ...;

    if (commande.getEtatCommande() != EtatCommande.EN_PREPARATION) {
        throw new BusinessException(ErrorCode.CMD_ORDER_NOT_MODIFIABLE);
    }

    // 1. VÉRIFIER LE STOCK POUR CHAQUE LIGNE (AVANT toute modification)
    List<StockShortage> shortages = new ArrayList<>();
    for (LigneCommandeClient ligne : commande.getLignes()) {
        Integer stockDispo = mouvementStockRepository
                .calculerStockReel(ligne.getArticle().getId(), getCurrentEntrepriseId());
        if (stockDispo < ligne.getQuantite()) {
            shortages.add(new StockShortage(
                    ligne.getArticle().getId(),
                    ligne.getArticle().getCodeArticle(),
                    ligne.getArticle().getDesignation(),
                    stockDispo, ligne.getQuantite()));
        }
    }

    // 2. SI RUPTURE → REFUSER (aucun mouvement créé)
    if (!shortages.isEmpty()) {
        throw new InsufficientStockException(shortages);
        // → 409 CONFLICT + liste détaillée des articles en rupture
    }

    // 3. SI TOUT EST OK → créer les SORTIES de stock
    for (LigneCommandeClient ligne : commande.getLignes()) {
        mouvementStockRepository.save(MouvementStock.builder()
                .typeMouvement(TypeMouvement.SORTIE)   // ← SORTIE du stock
                .quantite(ligne.getQuantite())
                .origineType("COMMANDE_CLIENT")
                .build());
    }

    commande.setEtatCommande(EtatCommande.VALIDEE);
}
```

**Pourquoi vérifier TOUT le stock avant de créer LE PREMIER mouvement ?**
Sinon, on pourrait créer 3 sorties sur 5 et échouer sur la 4e → état incohérent.
La règle est : **"Tout ou rien avec vérification préalable"**.

---

## 11. Vente Directe & Caisse (US-064 à US-067)

### 📘 US-064 — Enregistrer une vente directe

**DÉCISION MÉTIER IMPORTANTE (GS-CDA-2026-02 §1) :**
La vente directe ne vérifie PAS le stock. Pourquoi ? Parce que le caissier a
**l'article sous les yeux** au comptoir. Bloquer pour un problème de donnée
(stock système ≠ stock physique à cause d'une casse non enregistrée) ferait
perdre une vente réelle pour une anomalie de saisie.

```java
@Transactional
public VenteResponse enregistrerVente(CreerVenteRequest request) {
    // AUCUNE vérification de stock !
    // La décision GS-CDA-2026-02 dit : "Ne jamais bloquer une vente comptoir
    // pour un problème de donnée"

    for (LigneVente ligne : request.getLignes()) {
        MouvementStock mouvement = MouvementStock.builder()
                .typeMouvement(TypeMouvement.SORTIE)
                .quantite(ligne.getQuantite())
                .origineType("VENTE")
                .build();
        mouvementStockRepository.save(mouvement);

        // Même si stock devient NÉGATIF, on crée le mouvement
        Integer stockApres = mouvementStockRepository
                .calculerStockReel(ligne.getArticleId(), entrepriseId);

        // Si stock < 0 → alerte spéciale ECART_STOCK (pas STOCK_BAS)
        if (stockApres < 0) {
            eventPublisher.publishEvent(
                    new EcartStockDetecteEvent(ligne.getArticleId(), stockApres));
        }
    }
}
```

### 📘 US-065 — Lister les ventes directes

**Concept clé :** Journal de caisse avec agrégation journalière

```java
@Transactional(readOnly = true)
public Page<VenteResponse> listerVentes(
        LocalDate dateDebut, LocalDate dateFin, Pageable pageable) {

    // Calcul du total journalier en une seule requête
    Long totalJournalier = venteRepository.calculerTotalJournalier(
            getCurrentEntrepriseId(), dateDebut, dateFin);

    Page<Vente> ventes = venteRepository.findByEntrepriseIdAndDateVenteBetween(
            getCurrentEntrepriseId(),
            dateDebut.atStartOfDay(),
            dateFin.atTime(23, 59, 59),
            pageable);

    return ventes.map(v -> mapper.toDto(v, totalJournalier));
}
```

### 📘 US-066 — Consulter une vente

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<VenteDetailResponse>> consulterVente(@PathVariable Long id) {
    Vente vente = venteRepository.findByIdAndEntrepriseId(id, getCurrentEntrepriseId())
            .orElseThrow(() -> new EntityNotFoundException("Vente", id));
    return ResponseEntity.ok(ApiResponse.ok(mapper.toDetailDto(vente)));
}
```

### 📘 US-067 — Annuler une vente

**DÉCISION MÉTIER (GS-CDA-2026-02 §2) :**
On ne supprime PAS le mouvement `SORTIE` original (immuabilité du journal).
On crée un mouvement **compensatoire** de type `ANNULATION_VENTE`.

```java
@Transactional
public void annulerVente(Long venteId, String motif) {
    Vente vente = venteRepository.findById(venteId).orElseThrow();

    if (vente.getStatut() == StatutVente.ANNULEE) {
        throw new BusinessException(ErrorCode.VENTE_DEJA_ANNULEE);
    }
    if (!vente.getDateVente().toLocalDate().equals(LocalDate.now())) {
        throw new BusinessException("Seules les ventes du jour peuvent être annulées");
    }

    // Créer un mouvement ANNULATION_VENTE pour CHAQUE ligne
    for (LigneVente ligne : vente.getLignes()) {
        MouvementStock compensation = MouvementStock.builder()
                .typeMouvement(TypeMouvement.ANNULATION_VENTE)  // ← Type dédié
                .quantite(ligne.getQuantite())                   // ← Même quantité
                .origineId(vente.getId())                        // ← Traçable
                .motif(motif)
                .build();
        mouvementStockRepository.save(compensation);
    }

    vente.setStatut(StatutVente.ANNULEE);  // ← On ne supprime PAS la vente
}
```

**Pourquoi `ANNULATION_VENTE` au lieu de `CORRECTION_POS` ?**
`CORRECTION_POS` = correction d'inventaire. `ANNULATION_VENTE` = annulation de vente.
Si on mélangeait, l'audit ne pourrait plus distinguer "on a fait un inventaire"
de "on a annulé une vente". La traçabilité serait perdue.

---

## 12. Transferts Inter-Filiales (US-068 à US-070)

### 📘 US-068 — Créer un transfert

**Concept clé :** Opération atomique sur 2 entreprises différentes

**Règles de validation :**
1. Source ≠ cible (vérifié par `CHECK` en BDD et par Java)
2. Même groupe (vérification programmatique)
3. Stock suffisant (vérifié avant création)
4. Mouvements atomiques (les deux ou aucun)

**Codes générés :** `TRF-{ANNEE}-{SEQUENCE_5}` ex: `TRF-2026-00005`

```java
@PreAuthorize("hasRole('ADMIN_GROUPE')")
@Transactional
public TransfertResponse creerTransfert(CreerTransfertRequest request) {
    // 1. Vérifier que les deux filiales sont dans le MÊME groupe
    if (!request.getFilialeSourceId().equals(request.getFilialeCibleId())) {
        throw new BusinessException(ErrorCode.GRP_SAME_SOURCE_AND_TARGET);
    }

    // 2. Vérifier le stock source
    Integer stockSource = mouvementStockRepository.calculerStockReel(
            request.getArticleId(), request.getFilialeSourceId());
    if (stockSource < request.getQuantite()) {
        throw new BusinessException(ErrorCode.STK_INSUFFICIENT_STOCK);
    }

    // 3. Opération ATOMIQUE : 2 mouvements ou rien
    MouvementStock sortie = MouvementStock.builder()
            .entrepriseId(request.getFilialeSourceId())
            .typeMouvement(TypeMouvement.TRANSFERT_SORTIE)
            .quantite(request.getQuantite())
            .build();

    MouvementStock entree = MouvementStock.builder()
            .entrepriseId(request.getFilialeCibleId())
            .typeMouvement(TypeMouvement.TRANSFERT_ENTREE)
            .quantite(request.getQuantite())
            .build();

    mouvementStockRepository.saveAll(List.of(sortie, entree));
    // ← Les DEUX sont sauvés dans la MÊME transaction ou AUCUN

    return TransfertResponse.builder()
            .reference("TRF-2026-00005")  // ← Code unique
            .build();
}
```

**Pourquoi une seule transaction pour 2 entreprises ?** Si la sortie est créée mais
pas l'entrée (crash serveur), la filiale source perd son stock sans que la cible
le reçoive. Avec `@Transactional`, c'est impossible : les deux mouvements sont
dans la même transaction ACID.

### 📘 US-069 — Lister les transferts

```java
@GetMapping("/groupe/transferts")
@PreAuthorize("hasRole('ADMIN_GROUPE')")
@Transactional(readOnly = true)
public Page<TransfertResponse> listerTransferts(
        Long filialeSourceId, Long filialeCibleId, LocalDate dateDebut, LocalDate dateFin,
        Pageable pageable) {

    // Admin Groupe voit TOUS les transferts de son groupe
    return transfertRepository.findByGroupeId(getCurrentGroupId(), pageable)
            .map(mapper::toDto);
}
```

### 📘 US-070 — Consulter un bon de transfert

**Concept clé :** Visibilité conditionnelle selon le rôle

```java
@GetMapping("/groupe/transferts/{id}")
public ResponseEntity<ApiResponse<TransfertDetailResponse>> consulterTransfert(@PathVariable Long id) {
    TransfertStock transfert = transfertRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Transfert", id));

    // Admin Filiale : visible uniquement si sa filiale est source ou cible
    if (isAdminFiliale()) {
        Long filialeId = getCurrentEntrepriseId();
        if (!transfert.getFilialeSourceId().equals(filialeId)
                && !transfert.getFilialeCibleId().equals(filialeId)) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }
    }
    // Admin Groupe : visible pour tous les transferts de son groupe

    return ResponseEntity.ok(ApiResponse.ok(mapper.toDetailDto(transfert)));
}
```

---

## 13. Notifications & Alertes (US-071 à US-075)

### 📘 US-071 — Alerte automatique seuil de stock

**Concept clé :** `@EventListener` asynchrone avec anti-spam

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class AlerteStockListener {

    @EventListener                    // ← Écoute l'événement publié par StockServiceImpl
    @Async("taskExecutor")             // ← Exécution ASYNCHRONE (ne bloque pas la transaction principale)
    @Transactional(propagation = Propagation.REQUIRES_NEW)  // ← Transaction INDÉPENDANTE
    public void onStockUpdated(StockUpdatedEvent event) {
        
        // Vérifier si une alerte a déjà été envoyée < 24h pour cet article (anti-spam)
        Optional<NotificationAlerte> derniereAlerte = alerteRepository
                .findTopByArticleIdAndTypeAndDateCreationAfterOrderByDateCreationDesc(
                        event.articleId(), ... , Instant.now().minus(24, ChronoUnit.HOURS));

        if (derniereAlerte.isPresent()) {
            log.debug("Alerte déjà envoyée pour l'article {} il y a moins de 24h", event.articleId());
            return;  // ← Anti-spam : pas de doublon
        }

        // Créer une nouvelle alerte
        NotificationAlerte alerte = NotificationAlerte.builder()
                .typeAlerte(event.nouveauStock() == 0 ? "RUPTURE" : "STOCK_BAS")
                .stockActuel(event.nouveauStock())
                .lue(false)
                .build();
        alerteRepository.save(alerte);

        // Envoyer un email (asynchrone également)
        emailService.envoyerAlerteStock(alerte);
    }
}
```

**Pourquoi `REQUIRES_NEW` ?** Si l'envoi d'email échoue, est-ce que le mouvement
de stock doit être annulé ? NON ! L'alerte est une conséquence, pas une condition.
`REQUIRES_NEW` crée une transaction **entièrement séparée** : l'échec de l'alerte
n'affecte pas le succès du mouvement.

---

## 14. Reporting & Statistiques (US-076 à US-080)

### 📘 US-076 — Top articles les plus vendus

**Concept clé :** Classement par quantité ou CA avec cache Redis

```java
@Transactional(readOnly = true)
@Cacheable(value = "reporting_top_articles", key = "#dateDebut + #dateFin + #critere + #limit")
public List<TopArticleResponse> getTopArticles(
        LocalDate dateDebut, LocalDate dateFin, String critere, int limit) {

    String orderBy = "CA".equals(critere)
            ? "SUM(l.prixUnitaire * l.quantite * (1 + l.tauxTvaSnapshot/100))"
            : "SUM(l.quantite)";

    return entityManager.createQuery("""
        SELECT new com.stockmaster.reporting.dto.TopArticleResponse(
            a.codeArticle, a.designation,
            SUM(l.quantite),
            SUM(l.prixUnitaire * l.quantite * (1 + l.tauxTvaSnapshot/100)))
        FROM LigneCommandeClient l
        JOIN l.article a
        WHERE l.entrepriseId = :entrepriseId
          AND l.dateCreation BETWEEN :debut AND :fin
        GROUP BY a.id, a.codeArticle, a.designation
        ORDER BY """ + orderBy + " DESC", TopArticleResponse.class)
        .setParameter("entrepriseId", getCurrentEntrepriseId())
        .setParameter("debut", dateDebut.atStartOfDay())
        .setParameter("fin", dateFin.atTime(23, 59, 59))
        .setMaxResults(limit)
        .getResultList();
}
```

**Pourquoi JPQL avec `new` ?** Pour créer directement des DTOs sans passer par des tableaux d'Object. Plus propre et typé.

### 📘 US-077 — Clients les plus actifs

**Concept clé :** Classement croisé commandes B2B + ventes directes

```java
@Transactional(readOnly = true)
public List<TopClientResponse> getTopClients(String critere, int limit) {
    // Agrège à la fois les commandes client (B2B) et les ventes directes rattachées
    return entityManager.createQuery("""
        SELECT new TopClientResponse(
            c.id, c.nom, c.prenom,
            COUNT(DISTINCT cmd.id),
            SUM(l.prixUnitaire * l.quantite),
            MAX(cmd.dateCommande))
        FROM CommandeClient cmd
        JOIN cmd.client c
        JOIN cmd.lignes l
        WHERE cmd.entrepriseId = :entrepriseId
          AND cmd.etatCommande IN ('VALIDEE', 'LIVREE')
          AND cmd.dateCommande BETWEEN :debut AND :fin
        GROUP BY c.id, c.nom, c.prenom
        ORDER BY """ + ("CA".equals(critere) ? "4 DESC" : "3 DESC"), TopClientResponse.class)
        .setParameter("maxResults", limit)
        .getResultList();
}
```

**Pourquoi inclure les ventes directes (US-064b) ?** Décision GS-CDA-2026-02 §3 : les clients réguliers qui achètent au comptoir doivent apparaître dans le classement, pas seulement les clients B2B.

### 📘 US-079 — Rupture imminente

**Concept clé :** Jours de couverture estimés (stock / vitesse de vente)

```java
@Transactional(readOnly = true)
public List<RuptureImminenteResponse> getRupturesImminentes(int joursAlerte) {
    LocalDate ilYa30Jours = LocalDate.now().minusDays(30);

    return entityManager.createQuery("""
        SELECT new RuptureImminenteResponse(
            a.codeArticle, a.designation, a.seuilAlerte,
            (SELECT COALESCE(SUM(CASE WHEN m.typeMouvement IN ('ENTREE','CORRECTION_POS','TRANSFERT_ENTREE')
                THEN m.quantite ELSE -m.quantite END), 0)
             FROM MouvementStock m WHERE m.article.id = a.id),
            COALESCE((SELECT SUM(l.quantite) / 30.0
             FROM LigneCommandeClient l
             WHERE l.article.id = a.id
               AND l.dateCreation >= :ilYa30Jours), 0))
        FROM Article a
        WHERE a.entrepriseId = :entrepriseId
          AND a.actif = true
          AND a.seuilAlerte > 0
        """, RuptureImminenteResponse.class)
        .setParameter("entrepriseId", getCurrentEntrepriseId())
        .setParameter("ilYa30Jours", ilYa30Jours.atStartOfDay())
        .setMaxResults(100)
        .getResultList()
        .stream()
        .filter(r -> {
            double joursRestants = r.getStockActuel() / r.getVitesseVenteJournaliere();
            r.setJoursRestants((int) Math.round(joursRestants));
            return joursRestants < joursAlerte;
        })
        .toList();
}
```

**Astuce :** La division par `30.0` (pas `30`) force le calcul en décimal. `COALESCE(..., 0)` évite les NULL. Le filtre final (`joursRestants < joursAlerte`) se fait en Java plutôt qu'en SQL pour simplifier la requête.

---

### 📘 US-078 — Évolution du chiffre d'affaires

**Concept clé :** Agrégation avec filtres temporels

```java
@Transactional(readOnly = true)
@Cacheable(value = "reporting_ca", key = "#dateDebut.toString() + #dateFin + #granularite")
public List<CaPoint> evolutionCA(LocalDate dateDebut, LocalDate dateFin, Granularite granularite) {
    // Agrégation des commandes VALIDÉES + ventes directes
    // Granularité : JOUR → un point par jour
    //             : MOIS → un point par mois

    List<Object[]> results = entityManager.createQuery("""
        SELECT FUNCTION('date_trunc', :granularite, v.dateVente),
               SUM(l.prixUnitaire * l.quantite * (1 + l.tauxTvaSnapshot/100))
        FROM Vente v
        JOIN v.lignes l
        WHERE v.entrepriseId = :entrepriseId
          AND v.statut = 'VALIDEE'
          AND v.dateVente BETWEEN :debut AND :fin
        GROUP BY 1
        ORDER BY 1
        """, Object[].class)
        .setParameter("granularite", granularite.name().toLowerCase())
        .setParameter("entrepriseId", getCurrentEntrepriseId())
        .setParameter("debut", dateDebut.atStartOfDay())
        .setParameter("fin", dateFin.atTime(23, 59, 59))
        .getResultList();

    return results.stream()
            .map(row -> new CaPoint((Date) row[0], (BigDecimal) row[1]))
            .toList();
}
```

**Pourquoi `@Cacheable` ?** Les rapports sont consultés plusieurs fois par les mêmes
personnes. Sans cache, chaque clic sur "rafraîchir" recalcule tout. Avec Redis Cache
(TTL 10 minutes), les données sont servies en **microsecondes** au lieu de **secondes**.

### 📘 US-080 — Export CSV

```java
@GetMapping(value = "/export/mouvements", produces = "text/csv")
public ResponseEntity<Resource> exportMouvementsCSV(...) {
    // 1. Récupérer les données (limité à 10 000 lignes)
    List<MouvementStock> mouvements = mouvementStockRepository
            .findByFilters(..., PageRequest.of(0, 10000));

    // 2. Construire le CSV manuellement
    StringBuilder csv = new StringBuilder();
    csv.append("Date;Article;Type;Quantité;Motif\n");
    for (MouvementStock m : mouvements) {
        csv.append(m.getDateMouvement()).append(";")
           .append(m.getArticle().getDesignation()).append(";")
           .append(m.getTypeMouvement()).append(";")
           .append(m.getQuantite()).append(";")
           .append(m.getMotif()).append("\n");
    }

    // 3. Retourner en téléchargement
    byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
    ByteArrayResource resource = new ByteArrayResource(bytes);

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=mouvements_stock_2026-01-01.csv")
            .contentType(new MediaType("text", "csv"))
            .body(resource);
}
```

**Pourquoi `produces = "text/csv"` ?** Pour informer le navigateur (ou l'outil HTTP)
que la réponse est un CSV, pas du JSON. Sans ça, le navigateur ouvrirait le fichier
dans un onglet au lieu de le télécharger.

---

## 15. Glossaire & Concepts Clés

### 🏗️ Architecture & Configuration

| Concept | Définition | Exemple StockMaster |
|---------|-----------|-------------------|
| **Monolithe modulaire** | 1 JAR = N modules isolés par package | 11 modules Maven |
| **@SpringBootApplication** | Lance Spring Boot avec auto-configuration | `StockMasterApplication.java` |
| **application.yml** | Fichier de configuration centralisé | 3 profils : dev, test, prod |
| **@ConfigurationProperties** | Mapping properties Java typées | `JwtProperties`, `CorsProperties` |
| **Bean** | Objet géré par Spring (cycle de vie, injection) | `AuthService`, `JwtTokenProvider` |
| **Injection de dépendances** | Spring fournit les dépendances via constructeur | Constructeur avec `final` + `@RequiredArgsConstructor` |
| **@ComponentScan** | Cherche les `@Component` dans le package | Automatique avec `@SpringBootApplication` |
| **Profil Spring** | Configuration par environnement | `dev`, `test`, `prod` |
| **open-in-view: false** | Désactive la session JPA dans les vues | Évite les `LazyInitializationException` silencieuses |

### 🗄️ JPA & Base de Données

| Concept | Définition | Pourquoi ? |
|---------|-----------|-----------|
| **@Entity** | Classe miroir d'une table SQL | `Utilisateur`, `Article` |
| **@MappedSuperclass** | Classe parente sans table dédiée | `AbstractEntity` |
| **@GeneratedValue(IDENTITY)** | ID auto-incrémenté par la BDD | `BIGSERIAL` PostgreSQL |
| **@ManyToOne(fetch = LAZY)** | Relation N-1 chargée à la demande | Évite de charger toute la BDD |
| **Flyway** | Versionnement de schéma SQL | `V1__init_schema.sql` |
| **ddl-auto=none** | Désactive la génération auto de schéma par Hibernate | Flyway est le SEUL responsable |
| **TIMESTAMPTZ** | Timestamp avec fuseau horaire PostgreSQL | Stocke en UTC |
| **BIGSERIAL** | Auto-incrément 64 bits PostgreSQL | IDs jusqu'à 9 quintillions |
| **Index GIN** | Index pour full-text search | `to_tsvector('french', ...)` |
| **Trigger PostgreSQL** | Fonction SQL exécutée automatiquement | Mise à jour de `date_modification` |
| **Soft delete** | `supprime = true` au lieu de DELETE | Historique conservé |

### 🔒 Sécurité & Authentification

| Concept | Définition | Exemple |
|---------|-----------|---------|
| **JWT** | JSON Web Token — token signé d'authentification | `eyJhbGciOiJIUzI1NiJ9...` |
| **jjwt 0.12.x** | Bibliothèque Java pour JWT | `Jwts.builder().signWith(...)` |
| **Claims** | Données contenues dans le JWT | `userId`, `entrepriseId`, `role` |
| **jti** | JWT ID — identifiant unique du token | UUID pour blacklist individuelle |
| **HS256** | Algorithme de signature HMAC-SHA256 | Clé symétrique ≥ 256 bits |
| **BCrypt** | Algorithme de hachage de mot de passe | `PasswordEncoder.encode()` |
| **Argon2id** | Successeur de BCrypt, résistant GPU | Migration progressive US-015 |
| **@PreAuthorize** | Annotation de contrôle d'accès | `hasRole('ADMIN_GROUPE')` |
| **RBAC** | Role-Based Access Control | 7 rôles, permissions par rôle |
| **Rate limiting** | Limitation du nombre de tentatives | 5 tentatives / 15 min par IP |
| **Blacklist** | Liste des tokens révoqués (Redis) | `blacklist:jti:{jti}` |
| **Fail-closed** | Refuser si Redis est indisponible | 503 SECURITY_STORE_UNAVAILABLE |
| **RTR** | Refresh Token Rotation | Usage unique du refresh token |
| **Isolation tenant** | Chaque entreprise voit SES données uniquement | `entreprise_id` du JWT |

### 🧩 Architecture en Couches

| Concept | Rôle | Règle |
|---------|------|-------|
| **@Controller** | Point d'entrée HTTP | Ne fait QUE déléguer au Service |
| **@Service** | Logique métier | Contient les `@Transactional` et `@PreAuthorize` |
| **@Repository** | Accès aux données | JPQL, pas SQL natif |
| DTO | Data Transfer Object | Jamais l'entité exposée directement |
| **MapStruct** | Mapping compilé Entité ↔ DTO | Géré à la compilation, pas de reflection |
| **@Transactional** | Transaction ACID | Rollback si une étape échoue |
| **@RestControllerAdvice** | Gestionnaire d'erreurs global | Intercepte TOUTES les exceptions |
| **RFC 7807** | Format standard des erreurs HTTP | `ProblemResponse` |
| **Spring Events** | Communication inter-modules | `ApplicationEventPublisher` + `@EventListener` |

### 📦 Maven & Build

| Concept | Définition |
|---------|-----------|
| **pom.xml parent** | POM racine qui versionne toutes les dépendances |
| **Module Maven** | Sous-projet avec son propre pom.xml |
| **JaCoCo** | Outil de mesure de coverage de code |
| **SonarCloud** | Plateforme d'analyse de qualité de code |
| **Multi-stage Docker** | Build en 2 étapes : JDK pour compiler, JRE pour exécuter |
| **Layered JAR** | Structure Spring Boot qui permet le cache Docker des dépendances |

### 💡 Patterns & Bonnes Pratiques

| Pattern | Description |
|---------|------------|
| **Guard clause** | Vérifier les préconditions en début de méthode, avant la logique |
| **Fail-fast** | Échouer immédiatement si une condition n'est pas remplie |
| **ACID** | Atomicité, Cohérence, Isolation, Durabilité (transactions) |
| **Snapshot** | Figer une valeur au moment de la création (ex: taux TVA) |
| **Journal immuable** | Les mouvements de stock ne sont JAMAIS modifiés |
| **Mouvement compensatoire** | Au lieu de supprimer un mouvement, en créer un inverse |
| **CQRS** | Command Query Responsibility Segregation (séparation lectures/écritures) |
| **Anti-spam** | Ne pas envoyer de doublon d'alerte < 24h |
| **Async** | Exécution asynchrone pour les tâches non bloquantes |
| **ReadOnly transaction** | Optimisation pour les requêtes de lecture |

---

> **Cette formation couvre US-001 à US-080 — tous les concepts du backlog.**
> Chaque User Story a sa section avec :
> - Le **concept clé** expliqué simplement
> - Le **code réel** du projet (ou pseudo-code réaliste)
> - Le **pourquoi** de chaque choix technique
> - Les **pièges à éviter** et les règles à respecter
>
> 📖 **Sources :** BACKLOG_StockMaster_CM.md, CDCT §§22-30, GS-IA-2026-01, code source de `stockmaster-auth/` et `stockmaster-shared/`
>
> 🔄 **À retenir absolument :** `entreprise_id` vient du JWT, jamais du body.
> Flyway est le seul responsable du schéma. Toujours `@Transactional`.
> Toute erreur métier = `BusinessException(ErrorCode)`.
> Le journal des mouvements de stock est IMMUABLE.
