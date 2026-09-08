# RÉFÉRENTIEL — Partie 6 : Modèle de données (vue dérivée)

### `GS-REF-2026-01 §6` | Version : 1.0 | Rédigée le 8 septembre 2026 | Statut : **🔍 Dérivée — l'autorité physique reste les migrations Flyway**

> **Règle (DEC-001) :** cette partie est une **vue lisible annotée** du schéma. Elle n'est jamais la source : en cas de divergence avec une migration Flyway appliquée, **la migration a raison** et cette partie doit être corrigée dans la même PR.

## 6.1 État actuel (migrations V1–V4)

Le schéma réellement appliqué (`V1__init_schema.sql` → `V4`) est l'état **de départ**. Il contient notamment : `tenant_group`, `entreprise`, `categorie`, `article`, `client`, `fournisseur`, `commande_fournisseur`, `ligne_commande_fournisseur`, `commande_client`, `vente` (`annulee BOOLEAN`), `mouvement_stock`, `transfert_stock` (mono-article, sans `group_id`), `notification_alerte` (2 types), `utilisateur` (`token_reset` mort, `UNIQUE(email)`).

## 6.2 Cible V5 (toutes les décisions de schéma)

> La migration `V5` (à écrire) applique **en une fois** l'état cible. Liste des changements par domaine :

| Domaine | Changement | Décision |
|---|---|---|
| Catalogue | `article`/`categorie` rattachés au **groupe** (`group_id`) ; index `(group_id, code_article)` ; `prix_achat_ht`/`prix_vente_ht` (INTEGER) ; `prix_vente_ttc` ; `seuil_alerte` ; unité de gestion/achat + facteur (`DEC-013`) ; `lot`/`date_peremption` (`DEC-012`) | DEC-002, 003, 012, 013 |
| Entreprise | `site_operationnel` booléen ; `code_filiale NOT NULL` | DEC-015, A-10 |
| Transfert | en-tête (`group_id`, source, cible, statut, dates) + table `ligne_transfert` (article, qty demandée/expédiée/reçue) ; `CHECK` source/cible même groupe | DEC-002, 007 |
| Caisse | tables `session_caisse` (ouverture, fond, clôture, écart) et `paiement` (`vente_id`, mode, montant, référence) | DEC-009 |
| Vente | `vente.statut` (`PAYEE|ANNULEE|REMBOURSEE`) ; `client_id` nullable ; suppression `annulee` ; `caissier_id`/session | DEC-010, B-03 |
| Mouvement | `ANNULATION_VENTE`, `REMBOURSEMENT` ajoutés au CHECK ; `origine_type` + `'ANNULATION_VENTE'` ; suppression de `vue_stock_reel` (objet mort) ; trigger `update_date_modification` exclu de la table | DEC-010, B-03, C-12 |
| Montants & quantités | montants en `INTEGER` (XAF) ; **toutes** les quantités en `DECIMAL(12,3)` — `article.seuil_alerte`, `mouvement_stock.quantite`, toutes les tables de lignes (`DEC-003`) | DEC-003 |
| Utilisateur | `email_verifie` booléen ; suppression `token_reset`/`token_reset_expiry` ; `UNIQUE(email)` conservé | DEC-008, 016, 024 |
| Alertes | `notification_alerte` refondue : `destinataire_utilisateur_id`, `type` extensible, `etat`, `article_id` nullable | DEC-004 |
| Plans | `tenant_group.plan_abonnement` → 3 valeurs (`GRATUIT|PRO|PERSONNALISE`) ; `limite_filiales` ; `date_expiration_plan` | DEC-015 |
| Commande client | `etat_reglement` (`NON_REGLEE|REGLEE`), `date_echeance`, `date_reglement` | DEC-011, 020 |
| Idempotence | table des clés d'idempotence (avec réponse) | DEC-027 |
| Inventaire | tables `session_inventaire`, `ligne_inventaire` | DEC-036 |
| Volumétrie | `mouvement_stock` **partitionné** par mois (`PARTITION BY RANGE` sur `date_mouvement`) | DEC-033 |

## 6.3 Règles d'intégrité transverses

- `entreprise_id NOT NULL` sur toute table métier, **sauf** `tenant_group`, `entreprise`, et `transfert_stock` (qui porte `group_id` — exception déclarée, `DEC-007`).
- `UNIQUE(email)` utilisateur au niveau plateforme (`DEC-008`).
- Stock jamais négatif : vérifié en service (`DEC-023`) ; pas de contrainte SQL directe (le calcul est à la volée).
- `mouvement_stock.supprime` : colonne préservée mais **interdite d'usage** (journal immuable) — à documenter en contrainte.
- Suppression logique partout (soft delete), sauf le journal.

---

*Sources : `DEC-002, 003, 004, 007, 008, 009, 010, 011, 012, 013, 015, 016, 020, 023, 024, 027, 033, 036` ; migrations `V1`–`V4` réelles.*