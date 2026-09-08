# RÉFÉRENTIEL — Partie 13 : Périmètre & planning (pointeurs)

### `GS-REF-2026-01 §13` | Version : 1.0 | Rédigée le 8 septembre 2026 | Statut : **🔍 Dérivée — l'autorité est `GS-PLAN` + `progress-ledger`**

> **Règle (DEC-001) :** les chiffres de périmètre et l'avancement vivent dans `03-pilotage/GS-PLAN-2026-01_planning_projet_reorganise.md` et `progress-ledger.md`. Ce fichier ne **réplique** pas les chiffres : il pose la structure.

## 13.1 Structure de livraison

- **V1** : les deux parcours (mono-boutique puis multi-filiale) — voir `01-vision-perimetre.md §1.3`.
- **V1.5** : applicatif différé (FEFO, code-barres, SMS, souscription) — `DEC-030`.
- Le schéma est **complet dès V5**.

## 13.2 Ordre de construction (acté)

1. **Parcours A (mono-boutique)** : socle → auth → catalogue → tiers → achats → stock → ventes B2B → caisse → alertes → reporting.
2. **Parcours B (multi-filiale)** : inscription groupe → filiales → dashboard consolidé → stock consolidé → transferts.
3. **Import CSV** (`DEC-038`) en parallèle de l'onboarding.

## 13.3 Compteur de périmètre

- Référence backlog : 291 SP ≈ 11 sprints.
- Après toutes les décisions (Lot 5) : **≈ 24,5 sprints** (voir journal §Compteur).
- **Alerte de pilotage** : à 3 personnes, cela représente ~15-16 mois. La ventilation V1 / V1.5 et l'ordre mono-d'abord visent à livrer un produit **utilisable** plus tôt que la totalité.

---

*Sources : `DEC-030`, `DEC-031`, `GS-PLAN`, `progress-ledger`, journal §Compteur.*