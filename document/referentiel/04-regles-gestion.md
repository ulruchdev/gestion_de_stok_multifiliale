# RÉFÉRENTIEL — Partie 4 : Règles de gestion par domaine

### `GS-REF-2026-01 §4` | Version : 1.0 | Rédigée le 8 septembre 2026 | Statut : **Validée (DEC-003, DEC-006, DEC-009, DEC-010, DEC-011, DEC-012, DEC-013, DEC-017, DEC-020, DEC-023, DEC-027, DEC-028, DEC-029, DEC-036)**

## 4.1 Montants et quantités (DEC-003)

- **Montants** : `INTEGER` (francs CFA, pas de subdivision).
- **Quantités** : `DECIMAL(12,3)` (vente au poids/litre).
- **Arrondi** : au franc, **appliqué au total de ligne**, jamais au prix unitaire. *(À confirmer fiscalement : ligne vs total — voir §9.4.)*
- Prix TTC calculé **côté serveur**, jamais saisi.

## 4.2 Stock (DEC-023, DEC-027, ADR-003)

- **Journal immuable** : `mouvement_stock` en deltas ; aucun UPDATE/DELETE, jamais. Le trigger `update_date_modification` est **exclu** de cette table.
- **Stock réel** = Σ ENTREE − Σ SORTIE, calculé à la volée.
- **Le stock ne peut jamais devenir négatif** (`DEC-023`). Une vente/correction qui le ferait passer sous zéro est **refusée (409)**.
- **Idempotence** : toute écriture créant un mouvement de stock exige un en-tête `Idempotency-Key` (`DEC-027`) — vente, réception, transfert, correction.
- **Types de mouvement** : `ENTREE`, `SORTIE`, `CORRECTION_POS`, `CORRECTION_NEG`, `TRANSFERT_ENTREE`, `TRANSFERT_SORTIE`, `ANNULATION_VENTE`, `REMBOURSEMENT` (compensatoires = entrées).
- `ANNULATION_VENTE` et `REMBOURSEMENT` sont des **entrées compensatoires** (signe +), jamais des sorties.
- **Seuils d'alerte** : `RUPTURE` si stock ≤ 0 ; `BAS` si 0 < stock ≤ seuil ; `ANOMALIE` si stock < 0 (erreur de donnée, jamais atteint en V1 sauf bug) ; `NORMAL` sinon. Alerte déclenchée sur tout mouvement décrémentant **et** levée automatiquement au réapprovisionnement (`DEC-004`).

## 4.3 Caisse (DEC-009, DEC-010, DEC-018)

- **Session de caisse** obligatoire : une vente est toujours rattachée à une session ouverte.
- **Paiement mixte** : table `paiement` (`vente_id`, mode, montant, référence) ; modes `ESPECES`, `MOBILE_MONEY`, `CARTE`.
- **Clôture** : fond de caisse, écart constaté ; la clôture est le contrôle anti-perte.
- **Remboursement** : en caisse, sans avoir ; mouvement de stock compensatoire ; machine `PAYEE → ANNULEE | REMBOURSEE`.
- **Annulation** : jour même, dans sa propre session (`DEC-018`).
- **Stock insuffisant** : `409 Conflict` (`DEC-017`).

## 4.4 Achats (DEC-006, DEC-013)

- Commande fournisseur : `COMMANDEE → PARTIELLEMENT_RECUE → RECEPTIONNEE | ANNULEE`.
- **Réception partielle** : quantité réellement reçue ; l'écart est tracé.
- **Conditionnement** : facteur de conversion unité d'achat / unité de gestion appliqué à la réception (`DEC-013`).
- Prix d'achat ≥ prix de vente : **avertissement non bloquant** (`DEC-029`).

## 4.5 Ventes B2B (DEC-011, DEC-020)

- Commande client : `VALIDEE → LIVREE | ANNULEE` + **état de règlement** (`NON_REGLEE`/`REGLEE`) + **date d'échéance**.
- **CA facturé** = ventes directes `PAYEE` + commandes `LIVREE`, HT, sur la période.
- **CA encaissé** = paiements enregistrés + commandes `REGLEE`, HT, à la date de règlement.
- **Jamais rétroactif** : un remboursement se déduit du CA du jour où il est effectué.
- CA attribué à la **filiale de la vente** (session), jamais à la filiale de rattachement de l'utilisateur.

## 4.6 Transferts inter-filiales (DEC-002, DEC-007)

- En-tête (`group_id`, source, cible, statut, dates) + **N lignes** (article, quantité demandée, expédiée, reçue).
- Machine : `DEMANDE → VALIDE → EN_TRANSIT → RECU | ECART`, sorties `REFUSE`, `ANNULE`.
- Écart = expédié − reçu, corrigé et motivé.
- Catalogue : article **unique au groupe** (`DEC-002`).

## 4.7 Catalogue (DEC-002, DEC-028, DEC-029)

- Article et catégorie rattachés au **groupe** ; stock/prix par filiale.
- Catégorie **non supprimable** si un article (actif ou archivé) y est rattaché — réaffectation proposée (`DEC-028`).
- Prix d'achat/vente : avertissement non bloquant (`DEC-029`).

## 4.8 Inventaire (DEC-036)

- Campagne datée, par filiale, sans gel ; comptage → écart figé → corrections en lot (`CORRECTION_POS/NEG`, motif « inventaire n°… »).
- Ouverture/comptage : `GESTIONNAIRE_STOCK` ; validation : `ADMIN_FILIALE` (séparation).
- Session ouverte et validée **le jour même**.

## 4.9 Notifications (DEC-004, DEC-014)

- Table `notification_alerte` refondue : `destinataire_id`, type extensible, `etat` (non lu/lu/résolu), `article_id` nullable.
- Canaux V1 : **in-app + e-mail** (interface `CanalNotification`). SMS en V1.5.
- Alerte levée automatiquement au réapprovisionnement.

---

*Sources : `DEC-002`, `DEC-003`, `DEC-004`, `DEC-006`, `DEC-007`, `DEC-009`, `DEC-010`, `DEC-011`, `DEC-012`, `DEC-013`, `DEC-014`, `DEC-017`, `DEC-018`, `DEC-020`, `DEC-023`, `DEC-027`, `DEC-028`, `DEC-029`, `DEC-036`.*