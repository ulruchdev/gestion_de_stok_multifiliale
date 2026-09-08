# Diagrammes de Séquence — StockMaster CM
### Référence : GS-SEQ-2026-01 | Version : 1.0 | Date : Juillet 2026 | Statut : Proposé
### Documents parents : GS-CDA-2026-01 §4 (flows en prose), GS-BACKLOG-2026-01 (endpoints)

> ⚠️ **Statut d'implémentation :** ce document décrit l'architecture **cible**. Seuls `stockmaster-shared`, `stockmaster-auth` et `stockmaster-bootstrap` contiennent du code réel à ce jour — tous les autres modules référencés ici (`groupe`, `utilisateur`, `catalogue`, `tiers`, `achat`, `stock`, `vente`, `notification`, `reporting`) sont des stubs vides (arborescence de packages sans classes). Voir `knowledge.md` et `document/03-pilotage/progress-ledger.md` pour l'état réel.

---

## Objet

GS-CDA-2026-01 §4 décrit les 8 flows métier en prose/ASCII. Ce niveau était suffisant pour un backend développé en solo. Ce document formalise les flows **les plus critiques** (ceux impliquant plusieurs couches : Frontend → API → Service → Repository → BDD, ou plusieurs modules internes via événements Spring) en diagrammes de séquence Mermaid, pour :

- Donner un contrat visuel exploitable par une équipe frontend externe.
- Clarifier les frontières entre modules (Spring Modulith) au moment où un événement inter-module est déclenché plutôt qu'un appel direct.
- Servir de support de revue technique (architecture) et de testing (QA peut dériver des cas de test directement de la séquence).

**Portée :** les flows représentés ici sont ceux qui génèrent des mouvements de stock automatiques (les plus sensibles) et le flow d'inscription (le plus complexe transactionnellement). Les flows CRUD simples (créer un article, un client) ne nécessitent pas de diagramme de séquence — un contrat d'endpoint (backlog) suffit.

---

## 1. Inscription — Groupe multi-sites (UC-01)

```mermaid
sequenceDiagram
    actor U as Visiteur
    participant FE as Frontend
    participant API as AuthController
    participant SVC as AuthService
    participant GRP as GroupeService (module groupe)
    participant DB as PostgreSQL
    participant MAIL as NotificationService (async)

    U->>FE: Soumet formulaire (groupe + admin)
    FE->>API: POST /api/v1/auth/inscription/groupe
    API->>SVC: validerEtCreerGroupe(dto)
    SVC->>SVC: Valider unicité email (query DB)
    alt Email déjà utilisé
        SVC-->>API: BusinessException(AUTH_EMAIL_EXISTS)
        API-->>FE: 409 ProblemResponse
        FE-->>U: "Un compte existe déjà avec cet email"
    else Email disponible
        SVC->>DB: BEGIN TRANSACTION
        SVC->>GRP: creerTenantGroup(dto)
        GRP->>DB: INSERT tenant_group
        SVC->>GRP: creerEntrepriseMere(tenantGroupId, dto)
        GRP->>DB: INSERT entreprise (type=MERE, parent_id=null)
        SVC->>DB: INSERT utilisateur (role=ADMIN_GROUPE, mdp hashé)
        SVC->>DB: COMMIT TRANSACTION
        SVC->>MAIL: publish(UtilisateurCreeEvent) [Spring Event, async]
        MAIL-->>MAIL: Envoi email de confirmation (non bloquant)
        SVC-->>API: RegisterResponse (succès)
        API-->>FE: 201 Created
        FE-->>U: "Compte créé — vérifiez vos emails"
    end
```

**Points de contrôle QA dérivés :**
- Rollback complet vérifié si l'INSERT `utilisateur` échoue après l'INSERT `entreprise` (transaction atomique — voir UC-01 "Règle métier").
- L'échec d'envoi d'email ne doit **jamais** annuler la transaction BDD déjà commitée (événement async, découplé).

---

## 2. Validation d'une commande fournisseur (UC-02)

```mermaid
sequenceDiagram
    actor R as Responsable Achats
    participant FE as Frontend
    participant API as CommandeFournisseurController
    participant SVC as CommandeFournisseurService
    participant STK as StockService (module stock)
    participant DB as PostgreSQL
    participant EVT as ApplicationEventPublisher

    R->>FE: Clique "Valider" sur commande EN_PREPARATION
    FE->>API: POST /api/v1/commandes-fournisseur/{id}/valider
    API->>SVC: valider(id, utilisateurConnecte)
    SVC->>DB: SELECT commande WHERE id AND entreprise_id
    alt Commande introuvable ou entreprise_id ne correspond pas
        SVC-->>API: EntityNotFoundException
        API-->>FE: 404 ProblemResponse
    else Commande trouvée
        SVC->>SVC: Vérifier état == EN_PREPARATION
        alt État invalide
            SVC-->>API: BusinessException(CMD_ETAT_INVALIDE)
            API-->>FE: 409 ProblemResponse
        else État valide
            SVC->>DB: BEGIN TRANSACTION
            loop Pour chaque ligne de commande
                SVC->>STK: creerMouvement(ENTREE, articleId, quantite, origineId=commandeId)
                STK->>DB: INSERT mouvement_stock
            end
            SVC->>DB: UPDATE commande SET etat_commande = VALIDEE
            SVC->>DB: COMMIT TRANSACTION
            SVC->>EVT: publish(CommandeFournisseurValideeEvent)
            EVT-->>EVT: NotificationModule écoute (email récap, async)
            SVC-->>API: CommandeResponse
            API-->>FE: 200 OK
            FE-->>R: "Commande validée — stock mis à jour"
        end
    end
```

**Points de contrôle QA dérivés :**
- Tous les mouvements sont créés dans la **même transaction** que le changement d'état — aucun mouvement orphelin possible en cas d'échec partiel.
- Test de concurrence : deux validations simultanées de la même commande ne doivent produire les mouvements qu'une seule fois (verrouillage optimiste ou pessimiste à spécifier — voir action de suivi §3).

---

## 3. Validation d'une commande client — avec vérification de stock bloquante (UC-03)

```mermaid
sequenceDiagram
    actor C as Commercial
    participant FE as Frontend
    participant API as CommandeClientController
    participant SVC as CommandeClientService
    participant STK as StockService
    participant DB as PostgreSQL

    C->>FE: Clique "Valider"
    FE->>API: POST /api/v1/commandes-client/{id}/valider
    API->>SVC: valider(id, utilisateurConnecte)
    SVC->>DB: SELECT commande + lignes WHERE id AND entreprise_id
    SVC->>SVC: Vérifier état == EN_PREPARATION
    SVC->>STK: verifierDisponibilite(lignes) — AVANT toute écriture
    loop Pour chaque ligne
        STK->>DB: SELECT SUM(mouvements) WHERE article_id AND entreprise_id
        STK->>STK: Calculer stock réel = Σ ENTREE − Σ SORTIE (+ variantes)
    end
    alt Au moins un article en stock insuffisant
        STK-->>SVC: InsufficientStockException(articlesEnRupture[])
        SVC-->>API: 409 ProblemResponse avec liste des articles + stock disponible (DEC-017)
        API-->>FE: Erreur détaillée
        FE-->>C: "Stock insuffisant pour X, Y — disponible : 3, 12"
        Note over SVC,DB: Validation bloquée — AUCUN mouvement créé
    else Stock suffisant pour toutes les lignes
        SVC->>DB: BEGIN TRANSACTION
        loop Pour chaque ligne
            SVC->>STK: creerMouvement(SORTIE, articleId, quantite, origineId=commandeId)
            STK->>DB: INSERT mouvement_stock
        end
        SVC->>DB: UPDATE commande SET etat_commande = VALIDEE
        SVC->>DB: COMMIT TRANSACTION
        SVC-->>API: CommandeResponse
        API-->>FE: 200 OK
        FE-->>C: "Commande validée"
    end
```

**Points de contrôle QA dérivés :**
- La vérification de disponibilité doit couvrir **la totalité des lignes** avant la moindre écriture (pas de validation partielle ligne par ligne).
- Race condition à tester explicitement : deux commandes concurrentes consommant le même stock limité — le second thread doit voir le stock déjà décrémenté par le premier (lecture cohérente au sein de la transaction, niveau d'isolation à documenter dans le CDCT si pas déjà fait).

---

## 4. Transfert inter-filiales — double mouvement atomique (UC-05)

```mermaid
sequenceDiagram
    actor AG as Admin Groupe
    participant FE as Frontend
    participant API as TransfertController
    participant SVC as TransfertService
    participant STK as StockService
    participant DB as PostgreSQL

    AG->>FE: Sélectionne filiale source, cible, article, quantité
    FE->>API: POST /api/v1/groupe/transferts
    API->>SVC: creerTransfert(dto, utilisateurConnecte)
    SVC->>SVC: Vérifier filiale_source != filiale_cible
    alt Source == Cible
        SVC-->>API: BusinessException(STK_TRANSFERT_IDENTIQUE)
        API-->>FE: 409 ProblemResponse
    else Filiales distinctes
        SVC->>STK: verifierStockSuffisant(articleId, filialeSourceId, quantite)
        alt Stock source insuffisant
            STK-->>SVC: InsufficientStockException
            SVC-->>API: 409 ProblemResponse (stock disponible affiché) — DEC-017
        else Stock suffisant
            SVC->>DB: BEGIN TRANSACTION
            SVC->>DB: INSERT transfert_stock (source, cible, article, quantite)
            SVC->>STK: creerMouvement(TRANSFERT_SORTIE, filialeSourceId, transfertId)
            STK->>DB: INSERT mouvement_stock (entreprise_id = source)
            SVC->>STK: creerMouvement(TRANSFERT_ENTREE, filialeCibleId, transfertId)
            STK->>DB: INSERT mouvement_stock (entreprise_id = cible)
            SVC->>DB: COMMIT TRANSACTION
            Note over SVC,DB: Les deux mouvements partagent le même transfert_id — tout ou rien
            SVC-->>API: TransfertResponse (bon de transfert)
            API-->>FE: 201 Created
            FE-->>AG: "Transfert effectué — bon disponible"
        end
    end
```

---

## 5. Alerte de stock critique — flow événementiel asynchrone (§7.4 GS-CDA-2026-01)

```mermaid
sequenceDiagram
    participant STK as StockService
    participant EVT as ApplicationEventPublisher
    participant NOT as NotificationModule (listener async)
    participant DB as PostgreSQL
    participant MAIL as Service Email (externe)

    Note over STK: Après tout mouvement SORTIE / CORRECTION_NEG / TRANSFERT_SORTIE
    STK->>STK: Calculer nouveau stock réel de l'article
    STK->>STK: Comparer avec seuil_alerte
    alt stock réel <= seuil_alerte ET seuil_alerte != 0
        STK->>EVT: publish(StockBasEvent(articleId, entrepriseId, stockActuel))
        EVT-->>NOT: @EventListener(async) reçoit l'événement
        NOT->>DB: INSERT notification_alerte (type=STOCK_BAS)
        NOT->>DB: SELECT date dernière alerte du même type pour cet article
        alt Dernière alerte > 24h ou aucune
            NOT->>MAIL: Envoyer email alerte
        else Dernière alerte < 24h
            Note over NOT: Pas de nouvel email — anti-spam
        end
    else stock réel > seuil_alerte OU seuil_alerte == 0
        Note over STK: Aucune action
    end
```

**Point d'architecture confirmé :** ce flow doit rester **découplé du chemin transactionnel principal** (publication d'événement Spring, écouteur `@Async` ou `@TransactionalEventListener(phase = AFTER_COMMIT)`) — une panne du service de notification ne doit jamais faire échouer une vente ou une commande.

---

## 6. Prochaines étapes

- Compléter avec le flow **Vente Directe non bloquante** (comportement révisé par GS-CDA-2026-02 — vente autorisée même en cas de stock insuffisant, à la différence de CommandeClient) dès que le comportement exact de compensation (`ANNULATION_VENTE`) est stabilisé en implémentation.
- Ajouter le flow d'**annulation de vente** une fois US-067 (revue) livrée.
- Si un frontend externe rejoint le projet, ce fichier devient le contrat de référence à valider avec l'équipe frontend avant tout développement d'écran impliquant plusieurs appels API séquencés.
