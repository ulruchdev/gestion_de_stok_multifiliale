# 09 — Commandes Client / Ventes B2B (EPIC 9)

## Cycle de Vie d'une Commande Client

```mermaid
graph LR
    Debut([Etat initial]) --> EnPreparation[EN_PREPARATION<br/>Cree par Commercial]
    EnPreparation --> EnPreparation2[Modification<br/>PUT /commandes-client/{id}]
    EnPreparation --> Validee[VALIDEE<br/>Validation bloquante<br/>POST /commandes-client/{id}/valider]
    Validee --> Livree[LIVREE<br/>Livraison<br/>POST /commandes-client/{id}/livrer]
    EnPreparation --> Supprimee[Supprimee<br/>soft delete]
    Validee --> Termine1[Terminal: Immutable]
    Livree --> Termine2[Terminal: Immutable]

    note right of EnPreparation
        Code: CC-2026-0001
        Prix snapshot fige
        TVA snapshot fige
    end note

    note right of Validee
        Verification STOCK bloquante:
        1. Verifier stock pour CHAQUE ligne
        2. Si un article manque -> 409 InsufficientStock
        3. Si OK -> creer SORTIE + valider (atomique)
    end note
```

## Flux de Validation avec Verification de Stock

```mermaid
sequenceDiagram
    actor CO as Commercial
    participant FE as Frontend
    participant API as CommandeClientController
    participant SVC as CommandeClientService
    participant STK as StockService
    participant DB as PostgreSQL

    CO->>FE: Clique Valider
    FE->>API: POST /commandes-client/{id}/valider
    API->>SVC: valider(id, utilisateur)

    SVC->>DB: SELECT commande + lignes
    SVC->>SVC: Verifier etat = EN_PREPARATION

    SVC->>STK: verifierDisponibilite(lignes)

    loop Pour chaque ligne
        STK->>DB: SELECT SUM(mouvements)
        STK->>STK: Stock = ENTREE - SORTIE + ...
    end

    alt Stock INSUFFISANT
        STK-->>SVC: InsufficientStockException
        SVC-->>API: 409 avec liste articles manquants
        API-->>FE: Stock insuffisant pour RIZ50KG (dispo: 3)
        FE-->>CO: Erreur detaillee

    else Stock SUFFISANT
        SVC->>DB: BEGIN TRANSACTION

        loop Pour chaque ligne
            SVC->>STK: creerMouvement(SORTIE, articleId, qte)
            STK->>DB: INSERT mouvement_stock
        end

        SVC->>DB: UPDATE commande SET etat = VALIDEE
        SVC->>DB: COMMIT TRANSACTION

        SVC-->>API: 200 OK
        API-->>FE: Commande validee
        FE-->>CO: Succes
    end
```
