# 08 — Gestion du Stock (EPIC 8)

## Calcul du Stock en Temps Reel

```mermaid
graph TD
    StockReel[Stock Reel] --> Calcul[= SOMME des mouvements]

    Calcul --> Entrees[+ ENTREE<br/>Commandes fournisseur validees]
    Calcul --> CorrectionsPos[+ CORRECTION_POS<br/>Inventaire surplus]
    Calcul --> TransfertsIn[+ TRANSFERT_ENTREE<br/>Recu d une autre filiale]
    Calcul --> Sorties[- SORTIE<br/>Commandes client validees]
    Calcul --> CorrectionsNeg[- CORRECTION_NEG<br/>Casse / Perte / Vol]
    Calcul --> TransfertsOut[- TRANSFERT_SORTIE<br/>Envoye a une autre filiale]
    Calcul --> Annulations[+ ANNULATION_VENTE<br/>Vente annulee (compensatoire)]
    Calcul --> Remboursements[+ REMBOURSEMENT<br/>Retour en caisse (compensatoire)]

    StockReel --> Alerte{Comparer avec<br/>seuil_alerte}
    Alerte -->|Stock strictement negatif| Anomalie[ANOMALIE<br/>erreur de donnee]
    Alerte -->|Stock egal a 0| Rupture[RUPTURE]
    Alerte -->|Stock inferieur ou egal au seuil| Bas[BAS]
    Alerte -->|Stock superieur au seuil| Normal[NORMAL]

    style Anomalie fill:#7b1fa2,color:white
    style Rupture fill:#f44336,color:white
    style Bas fill:#FF9800,color:white
    style Normal fill:#4CAF50,color:white
    style StockReel fill:#2196F3,color:white
```

## Flux de Correction de Stock

```mermaid
graph TD
    Correction[Correction manuelle] --> Type{Type de correction ?}

    Type -->|POSITIVE| Pos[Entree de stock]
    Type -->|NEGATIVE| Neg[Sortie de stock]

    Pos --> Motif{Motif renseigne ?}
    Neg --> Motif

    Motif -->|Non| Error[400 BAD REQUEST<br/>ErrorCode MOTIF_REQUIRED]
    Motif -->|Oui| Validate{Quantite superieure a 0 ?}

    Validate -->|Non| ErrorQty[400 Bad Request]
    Validate -->|Oui| CreateMvt[Creer mouvement<br/>type = CORRECTION_POS<br/>ou CORRECTION_NEG]

    CreateMvt --> Event[Publier StockUpdatedEvent]
    Event --> CheckAlerte[Verifier alerte stock bas]
    CheckAlerte --> Success[Stock mis a jour]

    style Error fill:#f44336,color:white
    style Success fill:#4CAF50,color:white
    style Correction fill:#2196F3,color:white
```

## Historique des Mouvements

```mermaid
graph LR
    Article[Article: RIZ50KG] --> Hist[Historique des mouvements]

    Hist --> M1[15/07 - ENTREE +50<br/>Origine: CF-2026-0001<br/>Par: Resp. Achats]
    Hist --> M2[16/07 - SORTIE -5<br/>Origine: CC-2026-0003<br/>Par: Commercial]
    Hist --> M3[17/07 - CORRECTION_POS +3<br/>Motif: Inventaire<br/>Par: Gest. Stock]
    Hist --> M4[18/07 - SORTIE -2<br/>Origine: Vente directe<br/>Par: Caissier]
    Hist --> M5[19/07 - TRANSFERT_SORTIE -10<br/>Vers: Filiale Bassa<br/>Par: Admin Groupe]

    style Article fill:#2196F3,color:white
    style Hist fill:#FF9800,color:white
```
