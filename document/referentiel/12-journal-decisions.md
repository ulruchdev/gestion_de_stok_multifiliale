# RÉFÉRENTIEL — Partie 12 : Journal des décisions

### `GS-REF-2026-01 §12` | Ouvert le 17 août 2026

---

## Comment ce journal fonctionne

Une ligne par décision, jamais amendée : une décision qui change reçoit une **nouvelle** entrée qui *remplace* l'ancienne (statut `remplacée par DEC-0xx`). C'est ce qui permet de savoir, six mois plus tard, **pourquoi** une règle est ce qu'elle est.

Les colonnes `Sources` renvoient aux deux audits gelés : `docx X-nn` pour `INCOHERENCES_DOCUMENTAIRES`, `aud X-nn` pour `GS-AUDIT-2026-01`.

| Statut | Sens |
|---|---|
| ✅ tranchée | décidée, propagation à faire |
| 🔧 propagée | décidée **et** répercutée dans le référentiel, le code et les tickets |
| ⏳ ouverte | identifiée, pas encore arbitrée |

> **Note de procédure — 17 août 2026.** Un premier passage d'arbitrage a couvert les lots 2 à 4. Il a été **annulé et reprend au lot 2**, à la demande du porteur du projet : l'addition de périmètre des choix successifs n'était pas visible au moment de chaque décision. Le journal intègre désormais un **compteur de périmètre** mis à jour après chaque lot. Les décisions des lots 0 et 1 sont conservées.

---

## Compteur de périmètre — backend

| Étape | Périmètre |
|---|---|
| Référence : BACKLOG backend | 291 SP ≈ **11 sprints** |
| + `DEC-002` catalogue groupe + transfert à états | +2 |
| + `DEC-004` module notification en P0 | +1 |
| **Total après lot 1** | **≈ 14 sprints** |
| + `DEC-006` réception partielle | +0,5 |
| + `DEC-007` bon de transfert multi-lignes | +0,5 |
| **Total après lot 2** | **≈ 15 sprints** |
| + `DEC-009` paiements multiples + clôture de caisse | +1 |
| + `DEC-010` remboursement en caisse | +0,5 |
| + `DEC-011` indicateur de règlement + échéance | +0,3 |
| + `DEC-012` schéma lot-ready | +0,5 |
| **Total après lot 3** | **≈ 17,3 sprints** |
| + `DEC-014` in-app + e-mail derrière un adaptateur | +0 |
| + `DEC-013` facteur de conditionnement | +0,5 |
| **Total après lot 3 bis** | **≈ 17,8 sprints** |
| + `DEC-025` refresh token en cookie httpOnly (reprise de code livré) | +0,3 |
| + `DEC-027` clé d'idempotence sur les écritures de stock | +0,3 |
| **Total après lot 4** | **≈ 18,4 sprints** |
| + `DEC-015` plans, limites, expiration, essai 90 j | +1,8 |
| + `DEC-016` activation par e-mail (jeton, renvoi, AUTH-07) | +0,3 |
| + `DEC-020` CA facturé / CA encaissé, `date_reglement` | +0,3 |
| + `DEC-023` verrou de concurrence sur les écritures de stock | +0,3 |
| **Total après lot 4 bis** | **≈ 21,1 sprints** |
| + `DEC-036` campagne d'inventaire sans gel | +1,5 |
| **Total après lot 4 ter** | **≈ 22,6 sprints** |
| + Lot 5 — `DEC-030…034` (phasage, stabilisation, sauvegarde, volumétrie, retirés) | +0,9 |
| + Lot 5 — `DEC-038` import de mise en service CSV | +1,0 |
| + Lot 5 — `DEC-039`, `DEC-040`, `DEC-041` (alignements sans coût) | +0 |
| **Total après lot 5** | **≈ 24,5 sprints** |

Estimations personnelles, à confirmer par l'équipe. Le KICKOFF prévoit 7-8 mois pour trois personnes sur la base de 11 sprints.

> **Alerte de pilotage.** Le périmètre est passé de **11 à 22,6 sprints**, soit **+105 % — il a doublé**. À trois personnes, cela déplace la livraison de ~7-8 mois à **~15-16 mois**. Quatre postes portent l'essentiel : `DEC-002` (+2), `DEC-015` (+1,8), `DEC-036` (+1,5) et `DEC-009` (+1). C'est ce que `DEC-030` (phasage V1 / V1.5) doit trancher — et ce point ne peut plus être traité en dernier. **Résolue le 7 septembre 2026** — voir `DEC-030` (Lot 5).

---

## Table des décisions

| Réf | Objet | Coût | Statut |
|---|---|---|---|
| `DEC-001` | Document maître unique | 0 | ✅ |
| `DEC-002` | Catalogue au niveau groupe, transfert à états | +2 | ✅ |
| `DEC-003` | Montants `INTEGER` XAF, quantités `DECIMAL(12,3)` | 0 | ✅ |
| `DEC-004` | Module notification complet en P0 | +1 | ✅ |
| `DEC-005` | 14 parties numérotées + index | 0 | ✅ |
| `DEC-006` | Machines à états minimales + réception partielle | +0,5 | ✅ |
| `DEC-007` | Bon de transfert multi-lignes | +0,5 | ✅ |
| `DEC-008` | E-mail unique par plateforme | 0 | ✅ |
| `DEC-009` | Paiements multiples + clôture de caisse | +1 | ✅ |
| `DEC-010` | Remboursement en caisse, sans avoir | +0,5 | ✅ |
| `DEC-011` | Règlement B2B : indicateur + échéance | +0,3 | ✅ |
| `DEC-012` | Schéma lot-ready, FEFO en V1.5 | +0,5 | ✅ |
| `DEC-013` | Conditionnement en V1, code-barres en V1.5 | +0,5 | ✅ |
| `DEC-014` | In-app + e-mail derrière un adaptateur | 0 | ✅ |
| `DEC-015` | Trois plans : GRATUIT 90 j, PRO, PERSONNALISÉ | +1,8 | ✅ |
| `DEC-016` | Compte inactif jusqu'à vérification de l'e-mail | +0,3 | ✅ |
| `DEC-017` | « Stock insuffisant » → `409` | 0 | ✅ |
| `DEC-018` | Le contrôle porte sur la session de caisse | 0 | ✅ |
| `DEC-019` | Appels synchrones par interface exposée | 0 | ✅ |
| `DEC-020` | CA en HT, facturé et encaissé, jamais rétroactif | +0,3 | ✅ |
| `DEC-021` | UTC en base, `Africa/Douala` en métier | 0 | ✅ |
| `DEC-022` | XAF figé, aucune colonne devise | 0 | ✅ |
| `DEC-023` | Le stock ne peut jamais devenir négatif | +0,3 | ✅ |
| `DEC-024` | Jeton de réinitialisation : Redis, TTL 1 h | 0 | ✅ |
| `DEC-025` | Access token en mémoire, refresh en cookie httpOnly | +0,3 | ✅ |
| `DEC-026` | `main` seule | 0 | ✅ |
| `DEC-027` | Clé d'idempotence sur les écritures de stock | +0,3 | ✅ |
| `DEC-028` | Catégorie non supprimable si un article y est rattaché | 0 | ✅ |
| `DEC-029` | Prix d'achat ≥ prix de vente : avertissement | 0 | ✅ |
| `DEC-030` | Phasage V1 / V1.5 — schéma complet dès V5 | 0 | ✅ |
| `DEC-031` | Lot de stabilisation (~60 points mécaniques) — checklist dédiée | +0,5 | ✅ |
| `DEC-032` | Politique sauvegarde / restauration / rétention / purge | +0,2 | ✅ |
| `DEC-033` | Volumétrie cible + archivage `mouvement_stock` (partitionnement) | +0,2 | ✅ |
| `DEC-034` | Éléments retirés : abandon définitif (point de fidélité) | 0 | ✅ |
| `DEC-035` | `ECART_STOCK_DETECTE` déclenché par le refus | 0 | ❌ remplacée par `DEC-037` |
| `DEC-036` | Campagne d'inventaire en V1, sans gel | +1,5 | ✅ |
| `DEC-037` | `ECART_STOCK_DETECTE` supprimé | 0 | ✅ |
| `DEC-038` | Import de mise en service CSV en **V1** : catalogue + stock initial + tiers | +1,0 | ✅ |
| `DEC-039` | `change-password` : best-effort si Redis down (aligné sur `US-085`) | 0 | ✅ |
| `DEC-040` | Session mono-appareil **assumée** en V1 (multi-appareils en V1.5) | 0 | ✅ |
| `DEC-041` | Rate limit IP : proxy de confiance seul autorisé à écraser `X-Forwarded-For` | 0 | ✅ |

**40 décisions actives** (41 entrées, `DEC-035` remplacée par `DEC-037`). **Aucun point ouvert** : les 6 points restants (`DEC-030` → `DEC-034`, `DEC-038`) ont été tranchés le 7 septembre 2026 au **Lot 5**, avec défauts retenus à entériner par le porteur (voir « Validation porteur » en fin de journal).

---

## Lot 0 — Stratégie documentaire

### DEC-001 — Document maître unique ✅
**Question :** comment corriger ≈ 96 contradictions sans recréer le mécanisme qui les a produites ?
**Sources :** `docx H-01` (cinq documents se déclarent « source de vérité unique ») · `aud §3`
**Options écartées :** carte d'autorité + registre (coût moindre, mais laisse 41 documents vivants) ; correction sur place (recrée la divergence au prochain changement).
**Décision :** un **référentiel unique** absorbe les règles normatives ; les autres fichiers deviennent *dérivés*, *pointeurs* ou *archives*. Deux garde-fous : rien de dérivable du code n'est recopié ; une règle = un paragraphe citable.
**Impacts :** les 41 fichiers de `document/` — reclassement complet, voir `00-INDEX.md`.

---

## Lot 1 — Décisions structurantes

### DEC-002 — Catalogue au niveau groupe, puis transfert à états ✅
**Question :** le transfert inter-filiales est P1 et structurellement irréalisable (catalogue isolé par filiale, `transfert_stock` à un seul `article_id`, pas de statut, pas de `group_id`), son prérequis GRP-08 est P2.
**Sources :** `aud A-01, A-02, A-03, A-04, A-05` · `docx D-03, C-13`
**Options écartées :** appariement par code article (consolidation groupe fausse par construction, dette permanente) ; transfert hors V1 (le pitch multi-site perd son argument opérationnel).
**Décision :** l'article est défini **au niveau groupe** (prix, seuils et stock par filiale). **GRP-08 passe de P2 à P1** et précède le transfert. Le transfert reçoit la machine à états `DEMANDE → VALIDE → EN_TRANSIT → RECU | ECART`.
**Motif décisif :** le module catalogue est encore un stub vide (0 fichier `.java` — vérifié). Le coût de cette décision est quasi nul aujourd'hui et de plusieurs semaines après le sprint 5.
**Coût :** ~2 sprints.
**Impacts :** `article`, `transfert_stock`, ordre des EPICs, BACKLOG (GRP-08), REF §4 et §5.

### DEC-003 — Montants entiers (XAF), quantités décimales ✅
**Question :** `INTEGER` (schéma réel, AF §8.4) ou `BigDecimal(15,2)` (GS-DATA, AF §6.2) ?
**Sources :** `docx C-01` (bloquante) · `aud B-05`
**Options écartées :** tout `INTEGER` (interdit la vente au poids — rédhibitoire pour une boutique camerounaise) ; tout `DECIMAL` (fait apparaître des centimes de franc CFA qui n'existent pas).
**Décision :** montants en **`INTEGER`** (XAF, pas de subdivision) ; quantités en **`DECIMAL(12,3)`** (riz au kg, huile au litre, fractionnement pharmacie). **Arrondi mathématique au franc, appliqué au total de ligne**, jamais au prix unitaire.
**Coût :** nul (une migration, déjà nécessaire).
**Impacts :** V5 sur `article`, `mouvement_stock`, toutes les tables de lignes ; AF §6.2 ; GS-DATA ; REF §2 (glossaire arrondi) et §6.

### DEC-004 — Module notification complet en P0 ✅
**Question :** le module `stockmaster-notification` est un stub vide, dont dépendent l'activation de compte, la réinitialisation de mot de passe, les alertes de stock et l'alerte de sécurité d'US-083 — **déjà marquée livrée**. La table `notification_alerte` n'a aucun destinataire et n'accepte que deux types.
**Sources :** `aud B-08, C-07` · `docx C-03, C-04, D-08`
**Options écartées :** e-mail transactionnel seul (les alertes de stock n'atteignent pas un gérant absent de son écran) ; retirer les promesses e-mail (un « mot de passe oublié » sans e-mail n'est pas exploitable).
**Décision :** module complet en **P0**, avant l'EPIC alertes. Table refondue : `destinataire_id`, `type` extensible (plus de CHECK figé), `etat` (non lu / lu / résolu), `article_id` **nullable**. Envoi asynchrone. Déduplication en base, et non seulement sur l'envoi d'e-mail.
**Coût :** ~1 sprint.
**Impacts :** `notification_alerte` ; US-083 (critère aujourd'hui irréalisable), US-072, US-071, US-006/007, US-011/012.
**Point resté ouvert :** le canal réel (SMS / WhatsApp) — voir `DEC-014`.

---

## Lot 2 — Forme du référentiel et modèle de données

### DEC-005 — Forme physique : 14 parties numérotées + index ✅
**Décision :** `document/referentiel/`, un fichier par partie, `00-INDEX.md` comme colonne vertébrale. Citation `REF §4.3.2`.
**Options écartées :** fichier unique de ~5 000 lignes (jamais relu intégralement, diffs de revue illisibles) ; document Word ou PDF (sort du dépôt et se périme, les développeurs ne l'ouvrent pas).
**Coût :** nul.

### DEC-006 — Machines à états minimales, avec réception partielle ✅
**Question :** le badge d'état du design system (correction C-011) définit 7 états — `en_preparation`, `validee`, `livree`, `annulee`, `en_attente`, `partiel`, `rembourse` — dont **4 n'existent dans aucune machine à états du modèle**. Par ailleurs aucun état ne correspond à une commande fournisseur passée et non encore reçue, alors que le tableau de bord du responsable achats affiche « commandes en attente de réception ».
**Sources :** `docx X-12, A-06` · `aud C-05`
**Options écartées :** minimale stricte à 3 états (coût nul, mais une livraison partielle obligerait à valider la totalité puis corriger le stock à la main) ; les 7 états du design system (+2 sprints, et ouvre mécaniquement l'avoir, le remboursement et l'encours).
**Décision :**

| Objet | États |
|---|---|
| Commande fournisseur | `COMMANDEE` → `PARTIELLEMENT_RECUE` → `RECEPTIONNEE` \| `ANNULEE` |
| Commande client B2B | `VALIDEE` → `LIVREE` \| `ANNULEE` |
| Vente directe caisse | `PAYEE` \| `ANNULEE` |

**Amendée par `DEC-010`** — la machine de la vente reçoit `REMBOURSEE`.
**Confirmée par `DEC-018`** — contre `BACKLOG:1678` (US-067) qui propose `VALIDEE | ANNULEE`, la vente conserve `PAYEE | ANNULEE | REMBOURSEE`.
**Résout :** `docx A-06` — l'état « commandée, pas encore reçue » qui n'existait dans aucun modèle. `VALIDEE` de la commande fournisseur est renommée `RECEPTIONNEE` : c'est la réception physique qui crée l'entrée de stock, pas la validation.
**Conséquence sur le design system :** le badge est **réduit à 4 états**. `en_preparation`, `en_attente` et `rembourse` sont retirés du composant, et non instruits côté métier.
**Coût :** +0,5 sprint.
**Impacts :** V5, US-048, US-049, `diagrams/07`, DESIGN_CORRECTIONS (C-011), REF §5.

### DEC-007 — Bon de transfert multi-lignes ✅
**Question :** `transfert_stock` porte un seul `article_id`, et n'est rattachée à aucun groupe alors que le CDCT §29 définit `countByGroupIdAndAnnee(groupId)` sur son repository.
**Sources :** `aud A-03, A-12` · `docx C-13`
**Option écartée :** mono-article + statut (migration minimale, mais déplacer 12 références imposerait 12 validations séparées de l'Admin Groupe et 12 lignes d'historique pour un seul camion).
**Décision :** en-tête (`group_id`, source, cible, statut de `DEC-002`, dates) + **N lignes** (article, quantité demandée, expédiée, reçue). L'écart de réception est la différence expédié / reçu.
**Coût :** +0,5 sprint.
**Impacts :** V5 (2 tables), US-068, `diagrams/11`, CDCT §29, GS-DATA §4 (déclarer l'exception à la règle `entreprise_id`), REF §5 et §6.

### DEC-008 — E-mail utilisateur unique par plateforme ✅
**Question :** AF §6.2 et GS-DATA §3 disent « unique par entreprise » ; le schéma réel, US-021, US-082 et le code livré disent `UNIQUE(email)`.
**Sources :** `docx C-09` · `aud B-02`
**Option écartée :** `UNIQUE(entreprise_id, email)` — permettrait la multi-appartenance, mais imposerait de choisir son entreprise à la connexion, casserait la réinitialisation par e-mail seul, et invaliderait des tests verts.
**Décision :** **unicité plateforme** — l'implémentation en place fait autorité.
**Conséquence assumée :** une même personne ne peut pas être employée de deux entreprises clientes distinctes.
**Coût :** nul.
**Impacts :** AF §6.2 et GS-DATA §3 à corriger (et non l'inverse).

---

## Lot 3 — Encaissement et cycle de vie commercial

### DEC-009 — Paiements multiples et clôture de caisse ✅
**Question :** `diagrams/10` affiche « Espèces / Mobile Money / Carte », « Payé 60 000 / Monnaie 3 200 » et un « point de fidélité ». **Aucun de ces éléments n'existe dans une spécification, un ticket ou une colonne.** La table `vente` ne sait pas comment elle a été payée.
**Sources :** `aud C-01` · `docx D-01` (VNT-04 sans ticket backend)
**Options écartées :** mode unique par vente (+0,3 — interdit le paiement mixte et ne permet aucun rapprochement de caisse) ; espèces seules (ne pas enregistrer le Mobile Money rend la caisse inexploitable au Cameroun).
**Décision :** table **`paiement`** (`vente_id`, `mode`, `montant`, `reference_transaction`) autorisant le **paiement mixte** ; table **`session_caisse`** (ouverture, fond de caisse, clôture, écart constaté). Modes : `ESPECES`, `MOBILE_MONEY`, `CARTE`.
**Motif terrain :** MTN Mobile Money et Orange Money sont souvent le moyen principal, et le paiement partie espèces / partie MoMo est courant. La clôture est le seul contrôle contre la perte en caisse.
**Non retenu :** le « point de fidélité » de `diagrams/10` — aucune spécification n'existe. **Retiré du diagramme**, non instruit (voir `DEC-034`).
**Coût :** +1 sprint.
**Impacts :** 2 tables en V5, US-064, US-F063, `diagrams/10`, REF §4 et §5.

### DEC-010 — Remboursement en caisse, sans avoir ✅
**Question :** rien ne modélise un retour de marchandise ; `REMBOURSEE` n'existe dans aucune machine à états.
**Sources :** `aud C-04` · `docx X-12`
**Options écartées :** avoir + remboursement (+1 — couvrait aussi le B2B, mais impose une table d'avoirs, un suivi de solde et une politique d'avoirs dormants) ; avoir seul (inacceptable pour un client de boutique) ; hors V1 (obligerait à annuler toute une vente parce qu'un article sur cinq est rapporté).
**Décision :** une vente peut être **remboursée en caisse** : sortie d'argent du tiroir et mouvement de stock compensatoire. **Amende `DEC-006`** : la machine de la vente devient `PAYEE → ANNULEE | REMBOURSEE`.
**Conséquence assumée :** pas d'avoir réutilisable, donc pas de mécanisme de retour adapté au B2B (commande client d'un distributeur). À reprendre au jalon suivant si le segment distributeur est priorisé.
**Coût :** +0,5 sprint.
**Impacts :** V5, `mouvement_stock` (type compensatoire), `DEC-009` (le remboursement affecte la session de caisse), REF §4 et §5.

### DEC-011 — Règlement client B2B : indicateur et échéance ✅
**Question :** une commande client `LIVREE` ne dit pas si elle est payée. Aucun encours, aucune échéance, aucun acompte n'existe au modèle.
**Sources :** `aud C-02`
**Options écartées :** règlements partiels + plafond d'encours (+1 — la vente à crédit par acomptes est la norme entre grossiste et détaillant, mais coût triple) ; hors V1 (aucun suivi de créance).
**Décision :** deux champs sur la commande client — **état de règlement** (`NON_REGLEE` / `REGLEE`) et **date d'échéance**. Permet un écran « factures en retard ».
**Conséquence assumée, à dire explicitement au commercial :** **l'acompte n'est pas géré.** Un client qui paie 200 000 sur 500 000 reste « non réglée » sans trace du versement, et aucun plafond d'encours ne peut bloquer une nouvelle commande. La V1 équipe donc bien la **boutique** (dont le risque est la caisse, couvert par `DEC-009`) et faiblement le **distributeur** (dont le risque est l'impayé).
**Coût :** +0,3 sprint.
**Impacts :** V5 (`commande_client`), EPIC 9, `DEC-020` (définition du CA : facturé ou encaissé), REF §4.

### DEC-012 — Schéma lot-ready en V1, FEFO au jalon suivant ✅
**Question :** l'AF §1.4 cible explicitement les **pharmacies**, mais ni le lot ni la date de péremption n'existent au modèle — pas une colonne, pas un ticket, pas une règle.
**Sources :** `aud C-06`
**Options écartées :** lot + DLC + FEFO complets (+2 — servait réellement les pharmacies dès la V1) ; retirer les pharmacies de la cible (ajouter le lot plus tard imposerait de reprendre l'historique des mouvements déjà saisis).
**Décision :** `lot` et `date_peremption` **dès la migration V5**, et le stock est calculé par **article, lot et filiale** dès l'origine. La sortie **FEFO**, l'alerte de péremption et le choix de lot en caisse sont livrés au **jalon V1.5**.
**Motif :** le coûteux est la refonte du calcul de stock, pas les règles qui s'appuient dessus. On paie le structurel maintenant, où il est bon marché, et l'applicatif ensuite.
**Conséquence assumée :** la pharmacie n'est pas un segment vendable à la V1. Le discours commercial doit le dire.
**Coût :** +0,5 sprint.
**Impacts :** `article`, `mouvement_stock`, lignes de transfert (`DEC-007` : le lot est porté par la ligne), formule de stock (REF §2), ~2 sprints supplémentaires au jalon V1.5.

---

## Lot 3 bis — Conditionnement et canal de notification

### DEC-014 — In-app + e-mail, derrière un adaptateur ✅
**Question :** `DEC-004` a mis le module notification en P0, mais **aucun document du corpus ne dit par où sort une alerte de stock** — ni canal, ni fournisseur, ni coût par message. Le point était laissé ouvert par `DEC-004`.
**Sources :** `aud B-08, C-07` · `docx C-03, C-04, D-08`
**Options écartées :** e-mail + SMS via agrégateur (+0,5 — le SMS est le seul canal réellement lu par un gérant sur le terrain, mais introduit un coût par message à répercuter sur l'abonnement et un compte fournisseur à ouvrir) ; e-mail + WhatsApp Business (+1 — canal dominant des commerçants camerounais, mais la vérification Meta et l'approbation des gabarits placent un délai administratif de tiers sur le chemin critique d'un module P0) ; in-app seule (contredit US-006/007/011/012, qui exigent l'e-mail).
**Décision :** deux canaux en V1 — **in-app** (la cloche du tableau de bord, table `notification_alerte` de `DEC-004`) et **e-mail** (SMTP). Les deux passent par une interface **`CanalNotification`** : ajouter SMS ou WhatsApp au jalon suivant est une implémentation de plus, sans refonte.
**Motif :** l'e-mail est de toute façon obligatoire (activation de compte, réinitialisation de mot de passe) ; l'in-app est déjà payé par `DEC-004`. Le couple ne coûte donc rien de plus, et l'adaptateur préserve le choix futur au lieu de le fermer.
**Conséquence assumée :** une alerte de rupture n'atteint pas un gérant qui n'est ni devant son écran ni sur sa boîte e-mail. **Le SMS reste le canal manquant** — à rouvrir dès que le terrain le confirme.
**Coût :** nul.
**Impacts :** `stockmaster-notification` (interface + 2 implémentations), REF §4, `DEC-015` (pas de coût par message à répercuter sur les plans).

### DEC-013 — Conditionnement en V1, code-barres au jalon suivant ✅
**Question :** le modèle ne connaît qu'un article et une quantité. Un distributeur achète au carton de 24 et vend à l'unité ; le PDF montre une caisse « scan » qui n'a ni colonne ni ticket.
**Sources :** `docx D-05` · `aud C-08`
**Options écartées :** code-barres seul (+0,2 — rend la caisse plus rapide que le cahier, l'argument de vente n°1, mais laisse le distributeur ressaisir 24 unités à la main) ; les deux (+0,7 sur un compteur déjà à 17,5) ; aucun des deux (compteur inchangé, mais reporte une dette structurelle).
**Décision :** **facteur de conversion** unité d'achat / unité de gestion sur l'article, appliqué à la réception. Le **code-barres est reporté au jalon V1.5**.
**Motif décisif :** les deux moitiés n'ont pas la même urgence. Le code-barres est **différable sans dette** — c'est une colonne et une recherche, ajoutables à tout moment. Le conditionnement **change la signification de toute quantité déjà saisie** : l'ajouter après mise en production imposerait de reprendre l'historique des mouvements. Même piège que `DEC-012` sur le lot, même arbitrage.
**Conséquence assumée :** la caisse reste à la saisie manuelle en V1. L'argument « plus rapide que le cahier » ne peut pas être tenu au discours commercial avant le jalon V1.5.
**Coût :** +0,5 sprint.
**Impacts :** `article` (unité de gestion, unité d'achat, facteur), réception de commande fournisseur (`DEC-006`), valorisation du stock, REF §2 (glossaire : unité de gestion) et §4.

---

## Lot 4 — Règles techniques tranchées sur défaut motivé

> Ces dix décisions ne portent aucun enjeu commercial : elles ferment des divergences documentaires par le choix techniquement défendable. Elles sont **réversibles à faible coût** ; toute objection les rouvre.

### DEC-017 — « Stock insuffisant » renvoie 409 ✅
**Sources :** `docx B-04` (7 documents divergent, aucun code écrit)
**Option écartée :** `422` — réservé à une entité syntaxiquement valide mais sémantiquement rejetée, c'est-à-dire aux erreurs de **validation de champ**. L'y mêler obligerait le frontend à distinguer deux natures d'erreur sur un même code.
**Décision :** **`409 Conflict`**. La requête est valide en elle-même ; c'est l'**état serveur** qui s'y oppose. Même code pour toute contrainte d'état (quantité indisponible, transfert déjà reçu, vente déjà annulée).
**Coût :** nul. **Impacts :** `ErrorCode.java`, OpenAPI, REF §7.

### DEC-019 — Appels synchrones autorisés, par interface exposée ✅
**Sources :** `docx H-05` · `aud B-11`
**Option écartée :** événements exclusivement — cohérence à terme, mais perte de la transaction, débogage difficile et complexité sans bénéfice dans une **seule JVM**.
**Décision :** un module appelle un autre **par l'interface de service qu'il expose**, jamais par son repository ni par ses entités. L'événement Spring est réservé à ce qui est asynchrone par nature (notification, audit).
**Règle citable :** aucun `@Repository` d'un module n'est injecté hors de son module.
**Coût :** nul. **Impacts :** REF §9, revue de code, `stockmaster-notification`.

### DEC-021 — `Africa/Douala` pour l'affichage, UTC en base ✅
**Sources :** `docx G-09` · `aud B-13`
**Option écartée :** dates naïves sans fuseau — décale les rapports d'un jour selon le serveur.
**Décision :** stockage en **`TIMESTAMPTZ` UTC**, affichage et **agrégation métier** en `Africa/Douala` (UTC+1, sans heure d'été). La **journée comptable** court de 00:00 à 23:59 heure de Douala. Pour la caisse, c'est la **session de `DEC-009`** qui fait foi, pas la journée civile.
**Coût :** nul. **Impacts :** V5, rapports, REF §2 et §4.

### DEC-022 — XAF figé, aucune colonne devise ✅
**Sources :** `docx C-06`
**Option écartée :** colonne `devise` paramétrable — couverture apparemment bon marché, mais **fausse** : toute devise à subdivision invalide `DEC-003` (montants `INTEGER`). Une colonne sans logique donnerait l'illusion du multi-devise.
**Décision :** **XAF exclusivement**, écrit comme limite explicite du produit.
**Conséquence assumée :** sortir de la zone franc CFA imposerait de reprendre `DEC-003`, donc tous les montants. Ce n'est pas un ajout, c'est une refonte.
**Coût :** nul. **Impacts :** REF §1 (périmètre) et §2.

### DEC-024 — Jeton de réinitialisation en Redis, TTL 1 h ✅
**Sources :** `docx F-04` · `aud B-09`
**Options écartées :** colonnes en base (survivent aux sauvegardes, imposent une purge planifiée) ; TTL 15 min (trop court face aux délais réels de distribution SMTP — un lien expiré devient un ticket de support).
**Décision :** **Redis**, TTL **1 h**, jeton **à usage unique** invalidé à la consommation.
**Conséquence assumée :** Redis indisponible ⇒ réinitialisation indisponible. C'est cohérent avec le fail-closed ciblé d'**US-085**, et doit être dit à l'exploitation.
**Coût :** nul. **Impacts :** US-011/012, REF §7 et §10.

### DEC-025 — Access token en mémoire, refresh en cookie httpOnly ✅
**Sources :** `docx F-06` · `aud B-10`
**État vérifié du code :** `RefreshTokenResponse` renvoie aujourd'hui le refresh token **dans le corps JSON** — donc stocké côté client par le frontend, sans protection possible contre le XSS.
**Options écartées :** `localStorage` (coût nul, mais une faille XSS exfiltre la session en silence — et la rotation d'US-083 ne protège alors plus de rien : l'attaquant tourne le jeton à la place de l'utilisateur) ; les deux en mémoire (déconnexion à chaque rafraîchissement de page — inacceptable pour un poste de caisse).
**Décision :** **access token en mémoire** (jamais persisté), **refresh token en cookie `httpOnly` `Secure` `SameSite=Strict`**.
**Coût :** **+0,3 sprint** — reprend du code déjà livré (`AuthServiceImpl`, contrôleur, DTO) et impose d'instruire la protection CSRF.
**Impacts :** US-081/082/083 **déjà livrées**, frontend, REF §7 et §9.

### DEC-026 — `main` seule ✅
**Sources :** `docx H-06`
**Option écartée :** `main` + `develop` — utile pour maintenir plusieurs versions en parallèle ; ici une équipe, un environnement de production, donc cérémonie sans bénéfice.
**Décision :** **`main`** + branches `feature/GS-xxx-*` + PR obligatoire avec revue. Pas de `develop`.
**Anomalie — constat rectifié le 26 août 2026 après vérification du dépôt :** `main` **est** la branche par défaut réelle (`git remote show origin` → *HEAD branch: main*) et porte bien les fusions (`f86fc54`, PR #15). Ce qui est faux est purement **local** : le pointeur `refs/remotes/origin/HEAD` vise encore `feature/GS-001-initialize-spring-boot-project`. Correction : `git remote set-head origin -a`. Le premier constat, qui annonçait des PR basées sur `feature/GS-001`, était erroné.
**Coût :** nul. **Impacts :** `A_JIRA_ET_GIT_FLOW.md`, REF §14.

### DEC-027 — Clé d'idempotence dès maintenant, sur les écritures de stock ✅
**Sources :** `docx B-11` · `aud B-14`
**Option écartée :** différée — le rattrapage imposerait de reprendre des écritures déjà passées en production, sans moyen de distinguer après coup un doublon d'une vente réellement répétée.
**Décision :** en-tête **`Idempotency-Key`** exigé sur toute écriture créant un `mouvement_stock` : vente, réception, transfert, correction. Clé conservée avec la réponse.
**Motif terrain décisif :** connexion mobile instable ⇒ double soumission ⇒ **double mouvement de stock**, faute indétectable a posteriori puisque le stock est calculé depuis le journal (ADR-003).
**Coût :** **+0,3 sprint**.
**Impacts :** table de clés en V5, contrôleurs concernés, REF §7.

### DEC-028 — Catégorie non supprimable si un article y est rattaché ✅
**Sources :** `docx A-09`
**Option écartée :** bloquer sur les seuls articles **actifs** — laisserait orphelins les articles archivés, dont l'historique de mouvements reste dans les rapports par catégorie.
**Décision :** suppression bloquée par **tout** article rattaché, actif ou archivé. L'écran propose une **réaffectation** avant suppression.
**Coût :** nul. **Impacts :** US catégorie, `ErrorCode.java` (`409`, voir `DEC-017`), REF §4.

### DEC-029 — « Prix d'achat ≥ prix de vente » : avertissement, non bloquant ✅
**Sources :** `docx A-10`
**Options écartées :** bloquante (interdit le déstockage, la promotion et l'écoulement d'un produit proche de la péremption — des opérations légitimes et fréquentes) ; règle rejetée (perd un garde-fou gratuit contre la saisie inversée, faute courante).
**Décision :** **avertissement à la saisie**, la validation reste possible. L'avertissement est journalisé.
**Coût :** nul. **Impacts :** US article, frontend, REF §4.

---

## Lot 4 bis — Règles à enjeu commercial

### DEC-015 — Trois plans : GRATUIT (90 j), PRO, PERSONNALISÉ ✅
**Question :** les plans d'abonnement ne sont **définis nulle part**. Seuls existent quatre noms recopiés de la migration (`GRATUIT, STARTER, PRO, ENTERPRISE`) et trois chiffres contradictoires pour la même limite : `V1__init_schema.sql` = **1**, `AuthServiceImpl` = **5** en dur, `diagrams/03` = **10**. US-016 (« vérification de la limite selon le plan », P0 sprint 3) est donc inapplicable : elle serait codée avec une valeur arbitraire qui deviendrait *de fait* la grille commerciale.
**Sources :** `aud A-07` (🔴) · `docx E-07`
**Options écartées :** conserver 4 plans sans les définir (revient à laisser le code décider du tarif) ; plan unique sans limite (sort US-016 du périmètre, mais rend le multi-filiale — le différenciateur — non monnayable) ; attendre le benchmark `GS-BENCH` que `GS-PLAN:17` déclare inexistant (bloquerait un lot P0 du sprint 3).
**Décision — grille à trois plans :**

| | **GRATUIT** (essai) | **PRO** | **PERSONNALISÉ** |
|---|---|---|---|
| Durée | **90 jours** | abonnement | négocié |
| Filiales | **4** | **15** | négocié (30 et +) |
| Utilisateurs | **10** | 50 | négocié |
| Catalogue groupe, transferts, consolidation | ✅ | ✅ | ✅ |
| Caisse, paiement mixte, clôture | ✅ | ✅ | ✅ |
| Lots / péremption / FEFO *(jalon V1.5)* | — | ✅ | ✅ |
| Alertes SMS *(hors V1, voir `DEC-014`)* | — | quota | à l'usage |
| Historique consultable | **30 jours** | illimité | illimité |
| Support | e-mail | prioritaire | dédié |

**`STARTER` est supprimé** de l'énumération et du CHECK. `ENTERPRISE` devient `PERSONNALISE`. L'énumération passe donc à **trois** valeurs.

**Conséquence structurante assumée :** l'essai gratuit couvrant 4 filiales et 10 utilisateurs, **PRO ne se vend plus sur le volume mais sur les fonctions** — historique illimité, lots/FEFO, SMS, support. La limite de 15 filiales devient un plafond, non un argument. Le prix de PRO doit donc être accessible à un commerçant de 4 boutiques, pas calibré sur 15.
**Deuxième conséquence assumée :** l'historique limité à 30 jours pendant un essai de 90 jours est **délibéré** — c'est le levier de conversion (« passez en PRO pour retrouver votre trimestre »). Il impose une règle absolue : **la limite d'historique est un filtre d'affichage, jamais une purge.** Les données de l'essai restent intégralement en base.

**Règles de bordure retenues :**

| Règle | Décision |
|---|---|
| Ce que compte `limite_filiales` | les **sites opérationnels** — la maison mère compte si elle détient du stock (tranche `aud A-07`) |
| Filiale désactivée | ne compte pas |
| Fin de l'essai à J+91 | **lecture seule + export**, pas de blocage ; purge à J+90 après (`GS-PLAN:61`) |
| Expiration d'un plan payant | 7 jours de grâce avec bandeau, puis lecture seule |
| Nombre d'essais | **un par NIF** (le NIF existe déjà en base) |
| Rétrogradation en dépassement | refusée tant que le dépassement dure, message explicite |
| Qui change un plan | **SUPER_ADMIN uniquement**, avec trace horodatée — acte commercial, pas un réglage |

**Hors périmètre V1, dit explicitement :** aucun module de facturation n'existe dans les trois backlogs. Le plan est un **attribut administratif** ; l'encaissement se fait hors application. La souscription en ligne par Mobile Money est un chantier séparé, **+2 sprints** et dépendance à un agrégateur MTN/Orange.
**Ne figure sur aucune grille :** l'API et l'intégration comptable — elles n'existent pas et ne seraient pas honorables.
**Coût :** **+1,8 sprint** (limite d'utilisateurs +0,2 · activation de fonctions par plan +0,5 · écran SUPER_ADMIN +0,3 · expiration et lecture seule +0,5 · essai 90 j, relances, unicité NIF +0,3).
**Impacts :** `PlanAbonnement.java` (4 → 3 valeurs), CHECK de `tenant_group` en V5, `AuthServiceImpl` (`limiteFiliales = 5` en dur → 4, et `date_expiration_plan` aujourd'hui non renseignée), US-015, **US-016 (devient applicable)**, `diagrams/03` (« PRO — 10 filiales » à corriger), `GS-IA:138`, REF §1, §3 et §4.
**Reste ouvert :** les **prix**. La grille fixe les limites, pas les tarifs — `GS-PLAN:17` signale que le benchmark qui les fonderait n'existe pas.

### DEC-016 — Compte inactif jusqu'à vérification de l'e-mail ✅
**Question :** le PDF impose « compte inactif, connexion bloquée » jusqu'au clic ; US-006/007 créent l'utilisateur **même si l'envoi de l'e-mail échoue** ; US-021 impose `actif = false` ; et l'US `AUTH-07` qui porterait l'activation **n'existe pas**. C'est le premier écran que voit un prospect.
**Sources :** `docx D-02, F-02` · `aud B-06`
**État vérifié du code :** `V1__init_schema.sql` pose `actif BOOLEAN NOT NULL DEFAULT TRUE` ; `AuthServiceImpl` écrit `.actif(true)` aux quatre créations (l. 119, 144, 212, 235) ; la connexion contrôle `actif` sur l'utilisateur **et** sur le groupe (l. 275/281, 555/561). Aucune colonne `email_verifie`, aucun jeton d'activation n'existe.
**Options écartées :** actif avec vérification simplement incitée (coût nul, mais laisse vivre 90 jours des comptes injoignables qui ne convertiront jamais) ; blocage différé à J+7 (impose une tâche planifiée et un parcours de déblocage pour un gain marginal) ; actif + verrous ciblés sur l'invitation d'utilisateur et le passage en PRO (+0,3, même résultat sans jamais bloquer l'entrée).
**Décision :** **le compte est inactif à l'inscription** ; la connexion est refusée tant que l'e-mail n'est pas vérifié.

**Ne jamais surcharger `actif`.** Cette colonne signifie « désactivé par un administrateur » (employé parti, entreprise suspendue). Lui faire aussi porter « e-mail non vérifié » rendrait les deux situations indistinguables et produirait le même message d'erreur pour un caissier licencié et pour un prospect qui n'a pas cliqué. Un indicateur **`email_verifie`** distinct est ajouté ; la connexion exige `actif = true` **et** `email_verifie = true`, avec **deux codes d'erreur distincts**.

**Jeton d'activation :** même mécanisme que `DEC-024` — Redis, usage unique — mais **TTL 48 h** : un prospect consulte sa boîte le soir, pas dans l'heure.

**Conséquence obligatoire, non facultative.** L'envoi étant asynchrone (`DEC-004`), il a lieu **après** le commit : un échec d'envoi ne peut pas être annulé par un rollback. Le compte existe alors, verrouillé, et le prospect est perdu sans que personne ne le sache. Trois éléments deviennent donc indispensables et font partie de cette décision :
1. un endpoint **« renvoyer le lien d'activation »**, accessible sans être connecté ;
2. la **traçabilité de l'échec d'envoi**, remontée à l'exploitation ;
3. Redis indisponible ou vidé ⇒ jetons perdus ⇒ le renvoi est le seul recours (cohérent avec US-085).

**Corrections imposées :** US-006/007 sont **fausses** sur ce point et doivent être reprises ; **`AUTH-07` doit être créée** (activation + renvoi) ; `AuthServiceImpl` cesse d'écrire `actif(true)` pour l'utilisateur créé à l'inscription. Ici, c'est **le PDF qui a raison et le code qui est corrigé** — cas rare, à signaler en revue.
**Conséquence assumée :** un SMTP en panne bloque toutes les inscriptions. La supervision de l'envoi d'e-mail devient une exigence d'exploitation, pas un confort.
**Coût :** +0,3 sprint.
**Impacts :** V5 (`email_verifie` sur `utilisateur`), `AuthServiceImpl`, `ErrorCode.java` (2 codes), US-006, US-007, US-021, **US AUTH-07 à créer**, `stockmaster-notification` (`DEC-014`), frontend (page d'activation + renvoi), REF §3, §7 et §10.

### DEC-018 — Le contrôle porte sur la session de caisse, pas sur le rôle ✅
**Question :** le Commercial peut-il encaisser une vente directe ?
**Sources :** `docx F-03` · `aud B-07`
**État vérifié du corpus :** `BACKLOG:1592` (US-064, *enregistrer* une vente) autorise `CAISSIER, COMMERCIAL, ADMIN_FILIALE, ADMIN_GROUPE` ; `BACKLOG:1681` (US-067, *annuler*) autorise `CAISSIER, ADMIN_FILIALE, ADMIN_GROUPE` — **sans le Commercial**. En l'état, le Commercial peut donc encaisser mais **pas corriger sa propre erreur**. Personne n'a décidé cela : c'est un accident de rédaction, et c'est la pire des deux réponses. Ailleurs, le Commercial est clairement le rôle **B2B** (`GS-IA:155` bouton Facture PDF, `BACKLOG:1111` et `1436` commande client).
**Options écartées :** interdire l'encaissement au Commercial (coût nul, séparation vente / encaissement classique, mais immobilise le Caissier ou l'Admin Filiale dans une boutique à deux personnes — frottement réel sur le cœur de cible) ; l'autoriser sans session propre (**rend `session_caisse` inexploitable et annule `DEC-009`**).
**Argument décisif :** depuis `DEC-009`, la vraie question n'est pas *qui* encaisse mais **dans quelle session la vente tombe**. Encaisser dans la session d'un autre impute l'écart de clôture à quelqu'un qui n'a pas fait la vente, et détruit le seul contrôle anti-perte du produit.
**Décision — règle citable :** *« Quiconque encaisse **ouvre une session de caisse à son nom**, et en répond à la clôture. Quiconque a créé une vente peut l'annuler **le jour même, dans sa propre session**. »*

| Conséquence | Détail |
|---|---|
| US-064 | inchangée — `COMMERCIAL` reste autorisé |
| US-067 | **corrigée** — `COMMERCIAL` ajouté, mais restreint à **ses propres ventes** ; l'Admin Filiale garde l'annulation générale du jour |
| `session_caisse` (`DEC-009`) | une vente est **toujours** rattachée à une session ouverte ; aucune vente hors session |

**Réconciliation imposée :** `BACKLOG:1678` (US-067) renomme les états de la vente en `VALIDEE | ANNULEE`. **`DEC-006` et `DEC-010` prévalent** : les états restent `PAYEE | ANNULEE | REMBOURSEE` — `PAYEE` dit ce qui s'est réellement passé au comptoir, `VALIDEE` non.
**Coût :** nul.
**Impacts :** US-064, **US-067 (annotation de rôle et de portée à corriger)**, `DEC-009` (rattachement obligatoire à une session), `DEC-006` (nommage confirmé), REF §3 et §4.

### DEC-020 — Le chiffre d'affaires est HT, en deux indicateurs, jamais rétroactif ✅
**Question :** tout rapport financier du produit dépend de cette définition, et le corpus n'en donne qu'une, écrite deux fois : `BACKLOG:695` et `BACKLOG:1912` — *« CA = somme des commandes VALIDÉES + ventes directes de la période »*. Elle ne dit ni HT ni TTC, ne traite ni les annulations ni les remboursements.
**Sources :** `docx E-04, A-08` · `aud §398`, `§1074`
**Faute latente corrigée au passage :** depuis `DEC-006`, la commande client suit `VALIDEE → LIVREE | ANNULEE`. Prise au pied de la lettre, la définition existante ferait **sortir une commande du chiffre d'affaires au moment de sa livraison**. La formule est donc réécrite, pas seulement précisée.

**1 — Le chiffre d'affaires est HT.**
*Option écartée :* TTC (c'est ce que le commerçant voit dans son tiroir, mais cela surévalue son activité de 19,25 % — la TVA n'est pas un revenu, elle est due à l'État).
Les montants HT / TVA / TTC étant déjà calculés côté serveur (`US-064`), le **TTC est affiché à côté, sous son vrai nom : « encaissé TTC »**. L'arrondi est celui de `DEC-003` (au franc, sur le total de ligne), ce qui ferme le litige des deux comptables relevé par `aud §1074`.

**2 — Deux indicateurs distincts, pas un.**

| Indicateur | Définition |
|---|---|
| **CA facturé** | ventes directes `PAYEE` + commandes client `LIVREE`, HT, sur la période |
| **CA encaissé** | paiements enregistrés (`DEC-009`) + commandes client passées à `REGLEE`, HT, à la **date de règlement** |

*Options écartées :* CA facturé seul (gratuit, mais une commande de 500 000 jamais payée gonfle le chiffre — le distributeur pilote sur de l'argent qu'il n'a pas touché) ; CA encaissé seul (**non calculable en l'état** — `DEC-011` n'a qu'un indicateur, sans date).
**Conséquence :** une colonne **`date_reglement`** est ajoutée à la commande client. Elle répare en partie la faiblesse assumée de `DEC-011` sur le segment distributeur : « facturé 4 M, encaissé 2,6 M » est l'écran qui montre un impayé.

**3 — Principe : un chiffre d'affaires clos ne change jamais rétroactivement.**
L'annulation n'étant possible que le jour même (`US-067`), une vente annulée n'entre jamais dans un CA clos. Le **remboursement** (`DEC-010`), lui, peut survenir des semaines plus tard : il se déduit du CA **du jour où il est effectué**, jamais du jour de la vente d'origine. Sans cette règle, un mois déjà communiqué à un banquier ou à un associé se modifie tout seul.

**Corollaire d'attribution :** le CA est attribué à la **filiale de la vente** — celle de la session de caisse (`DEC-018`) — **jamais** à la filiale de rattachement de l'utilisateur. Sans cette règle, le cas décrit par `aud §398` se produit : un directeur au badge « Siège » vend à Douala, et le siège de Yaoundé affiche un chiffre d'affaires sans avoir jamais ouvert au public.
**Coût :** +0,3 sprint.
**Impacts :** V5 (`date_reglement` sur `commande_client`), **US-078 et US-015 (formule à réécrire)**, `GS-IA` (tableau de bord), `DEC-011`, `DEC-018`, REF §2 (glossaire : CA facturé, CA encaissé) et §4.

### DEC-023 — Le stock ne peut jamais devenir négatif ✅
**Question :** la décision validée `GS-CDA-2026-02 §1` — la vente directe ne bloque **jamais** sur stock insuffisant — s'étend-elle à la commande client, au transfert et à la correction d'inventaire ?
**Sources :** `aud B-10` · `GS-CDA-2026-02 §1`
**État vérifié du corpus :** quatre comportements différents pour une même contrainte. Vente directe **non bloquante** (`BACKLOG:1594`, alerte `ECART_STOCK_DETECTE` si le stock passe sous zéro) ; commande client **bloquante** (`BACKLOG:1514`, `409`, aucun mouvement créé) ; transfert **bloquant** (`BACKLOG:1716`, `409`) ; correction négative *« paramétrable »* (`BACKLOG:1384`) — un paramètre dont **aucun document ne dit qui le règle, où il est stocké, ni quelle est sa valeur par défaut** : une décision non prise déguisée en option de configuration. `aud B-10` relève en outre **quatre définitions concurrentes** du seuil de rupture (`≤ 0` dans `diagrams/08`, `= 0` dans US-071, `< seuil` dans `GS-CDA §3.9`, `≤ seuil` dans `GS-CDA §7.4` et US-051), et note qu'aucun document ne dit quel statut afficher pour un stock négatif.

**Décision — invariant produit :** *« Le stock d'un article dans une filiale ne peut jamais être négatif. Toute opération qui le ferait passer sous zéro est refusée. »*

| Opération | Comportement arrêté |
|---|---|
| Vente directe | **bloque** — `409 INSUFFICIENT_STOCK` (`DEC-017`), sans exception ni forçage |
| Commande client | bloque — inchangé |
| Transfert inter-filiales | bloque — inchangé |
| Correction négative | **plafonnée au stock actuel** ; le mot « paramétrable » disparaît de US-054 |
| Statut d'alerte | trois statuts : `RUPTURE` si `stock = 0`, `BAS` si `0 < stock ≤ seuil`, `NORMAL` sinon — **pas** de statut `ANOMALIE` |

Le comparateur est figé à **`≤ seuil`**, ce qui élimine les quatre définitions concurrentes de `aud B-10` à coût nul.

**Renversement assumé :** `GS-CDA-2026-02` porte le statut **« Validé »** et son §1 dit l'inverse. Il n'est pas supprimé : son §1 reçoit la mention *« annulé par `DEC-023` — 26 août 2026 »*, sans quoi le raisonnement d'origine devient introuvable.
**Options écartées :** maintenir l'asymétrie (coût nul, justifiée au comptoir par la présence physique du client et de la marchandise, mais laisse deux règles coexister pour une même contrainte) ; blocage avec **forçage tracé** réservé à l'Admin Filiale, motif obligatoire et mouvement marqué `force = true` (+0,3 ; garde la vente à l'intérieur du système au prix d'une exception nominative — écartée par le porteur au profit d'une règle unique).

**Conséquence assumée — la vente est refusée quand le stock système est faux.** Au démarrage et après tout écart non corrigé, un caissier peut avoir l'article en main et un client devant lui sans pouvoir vendre. Le risque n'est pas la perte de la vente mais **la vente hors système** : elle vide le journal `mouvement_stock`, qui est le cœur du produit. Contrepartie exigée en exploitation : l'inventaire initial devient un **prérequis de mise en service**, pas une tâche de rattrapage.

**Conséquence imposée — verrou de concurrence.** Le stock étant calculé à la volée depuis `mouvement_stock` (ADR-003), deux ventes simultanées du dernier article lisent toutes deux `stock = 1` et écrivent toutes deux un `SORTIE`. Tant que la vente ne bloquait pas, le résultat était un stock à −1 assorti d'une alerte — acceptable. L'invariant « jamais négatif » **exige désormais une sérialisation explicite** (verrou pessimiste sur le couple `(article, filiale)`, ou contrainte équivalente) sur toute écriture de stock. Ce n'est pas une option : sans elle l'invariant est faux en production dès la première file d'attente à deux caisses.
**Coût :** +0,3 sprint (verrou de concurrence). Le blocage lui-même est net nul : le backend économise le chemin non bloquant, le frontend doit étendre l'écran d'erreur `US-F061` à la caisse.
**Impacts :** `GS-CDA-2026-02 §1` (à annuler), `BACKLOG:1589` et `:1594` (US-064 à réécrire), `BACKLOG:1384` (US-054, « paramétrable » à supprimer), US-051, US-071, `diagrams/08`, `US-F061` et `US-D061` (écran d'erreur étendu à la caisse), `GS-SEQ-2026-01:232`, `DEC-017`, `DEC-027`, REF §4 et §5.

### DEC-035 — `ECART_STOCK_DETECTE` est déclenché par le refus, non par le stock négatif ❌ *remplacée par `DEC-037`*

> Entrée conservée telle qu'elle a été arrêtée le 26 août 2026, puis reprise le même jour à la demande du porteur. Le raisonnement reste consultable ; la règle applicable est celle de `DEC-037`.

**Question :** depuis `DEC-023`, aucune opération ne peut rendre un stock négatif. Le déclencheur historique de l'alerte — *« si le stock réel passe strictement en dessous de 0 après la vente »* (`BACKLOG:1595`, `GS-CDA-2026-02 §1`) — ne peut donc plus survenir. Faut-il supprimer le type d'alerte ?
**Sources :** `GS-CDA-2026-02 §1` · `BACKLOG:1595` · `aud §1212`, `§1250`
**Options écartées :** supprimer le type d'alerte (cohérent avec l'invariant, mais laisse le produit **sans aucun détecteur d'écart** entre deux inventaires) ; le rattacher à la correction manuelle (le gestionnaire qui saisit une correction sait déjà qu'il y a un écart — l'alerte n'apprendrait rien à personne).
**Décision :** `ECART_STOCK_DETECTE` est **conservé**, son déclencheur est déplacé. Il est émis **au refus d'une opération pour stock insuffisant** (`409 INSUFFICIENT_STOCK`, `DEC-017`), à destination du Gestionnaire de Stock et de l'Admin Filiale.
**Motif décisif :** `DEC-023` a déplacé le signal sans le supprimer. Auparavant l'écart se révélait par un stock négatif ; désormais le stock plancherait à zéro et **le seul instant où le système apprend qu'il se trompe est le refus** — le caissier a l'article en main, le système dit non. Sans cette alerte, l'écart resterait invisible jusqu'à l'inventaire suivant.
**Corollaire obligatoire — dédoublonnage :** un article refusé trente fois dans la journée ne doit pas produire trente alertes. Une alerte par couple `(article, filiale)` et par jour, portant le **nombre de refus**. Ce compteur est l'indicateur d'écart du produit : trié décroissant, il désigne exactement les articles à recompter en priorité.
**Conséquence assumée :** le refus en caisse est un signal **fort** (marchandise physiquement présente, constatée de visu). Le refus sur commande client ou transfert est un signal **faible** — le stock d'entrepôt n'est constaté par personne au moment de la saisie. Les deux alimentent le même compteur ; l'interprétation reste humaine.
**Coût :** nul — le type d'alerte, le canal (`DEC-014`) et les destinataires existent déjà ; le dédoublonnage relève du module notification de `DEC-004`.
**Impacts :** `BACKLOG:1595` (US-064, déclencheur à réécrire), `CDCT §23.3` (`CHECK type_alerte` — `ECART_STOCK_DETECTE` reste requis), `diagrams/12` (type absent, à ajouter), `DEC-017`, `DEC-023`, REF §4.

### DEC-037 — `ECART_STOCK_DETECTE` est supprimé ✅ *(remplace `DEC-035`)*
**Question :** reprise de `DEC-035` à la demande du porteur, le même jour.
**Sources :** `GS-CDA-2026-02 §1` · `BACKLOG:1595` · `aud §1212`, `§1248`, `§1250`
**Décision :** le type d'alerte `ECART_STOCK_DETECTE` est **supprimé du produit**. Aucune notification n'est émise lorsqu'une opération est refusée pour stock insuffisant.
**Motif :** la détection d'écart n'est pas portée par une notification, mais reportée sur l'inventaire, dont le périmètre est repris en `DEC-036`.
**Options écartées :** alerte au refus avec dédoublonnage (`DEC-035` — coût nul, mais fait reposer la détection d'un écart de données sur une notification que personne n'est tenu de traiter) ; alerte rattachée à la correction manuelle (le gestionnaire qui corrige connaît déjà l'écart).

**Conséquences vérifiées :**

1. **Trois corrections exigées par l'audit deviennent sans objet** : `aud §1248` (ajouter le type au `CHECK type_alerte` de `CDCT §23.3`), `aud §1250` (l'ajouter à `diagrams/12`), `aud §1212`. `SECURITY_ALERT` n'est pas concerné et reste à ajouter. Cette décision **retire** du travail au corpus au lieu d'en ajouter.
2. `BACKLOG:1595` (critère d'acceptation de US-064) est **supprimé**, non réécrit.
3. **Un refus ne laisse aucune trace en base.** `DEC-023` ne crée aucun mouvement lorsqu'elle refuse — c'est explicitement écrit à `BACKLOG:1514` : *« Aucun mouvement créé »*. Sans alerte, l'événement n'est ni notifié, ni enregistré, ni comptable a posteriori. L'écart entre stock système et stock physique n'est donc **détectable que par un inventaire**. C'est la charge que `DEC-036` doit assumer.

**Coût :** nul.
**Impacts :** `BACKLOG:1595` (critère à supprimer), `CDCT §23.3`, `diagrams/12`, `DEC-023`, `DEC-036`, REF §4.

### DEC-036 — Campagne d'inventaire en V1, sans gel du stock ✅
**Question :** l'inventaire doit-il entrer en V1, sous quelle forme et à quelle périodicité ?
**Sources :** `GS-IA:76` · `US-F074` · `US-D074` · `BACKLOG:1344` (US-053) et `:1373` (US-054)
**État vérifié du corpus :** l'inventaire existe **côté écran uniquement** — nœud de menu `GS-IA:76`, `US-F074` (5 SP) et `US-D074` (5 SP), les trois marquées **P2, hors périmètre V1**, rattachées à un `MVT-06` — et **aucune US backend ne le porte**. Le backlog serveur n'offre que les corrections unitaires `US-053` / `US-054`, en delta et article par article. Aucun import n'existe dans le produit : le seul mouvement de masse est `US-080`, un **export** CSV en P2, sprint 11.
**Options écartées :** statu quo P2 (coût nul, mais le stock dérive sans limite et sans signal) ; import CSV seul (+0,5 — le stock est juste au jour 1 et se dégrade ensuite) ; import + saisie par quantité constatée article par article (+0,8 — offre un outil de recomptage **sans jamais dire sur quoi le pointer**, ce qui n'a de valeur que si l'on connaît déjà l'écart).
**Argument décisif :** `DEC-037` ayant supprimé la seule détection automatique, l'inventaire n'est plus un confort de saisie — c'est **le seul organe par lequel le produit peut découvrir qu'un stock est faux**. S'y ajoute un motif comptable : la casse et la consommation interne sont des **pertes à justifier** ; sans session datée et nominative, une perte constatée en fin d'exercice n'est imputable ni à une période ni à une personne. C'est le litige que `DEC-018` a fermé sur la caisse, et qui resterait ouvert sur le stock, qui pèse plus lourd.

**Décision :** une **campagne d'inventaire** entre en V1 — session datée, liste de comptage, saisie des quantités constatées, feuille d'écarts, validation, corrections générées en lot. **Sans gel du stock** : les ventes continuent pendant le comptage.

**Mécanique du « sans gel ».** Le journal `mouvement_stock` étant un journal de **deltas** (ADR-003), aucune réapplication de mouvements n'est nécessaire — contrairement à ce qui avait été annoncé au porteur avant vérification :

| Étape | Règle |
|---|---|
| Ouverture | Session rattachée à **une filiale** (`entreprise_id`), jamais au groupe. Périmètre : tout le catalogue ou une catégorie |
| Comptage | Le gestionnaire saisit la **quantité constatée** ; la ligne enregistre l'horodatage de saisie et le stock système **à cet instant** |
| Écart | `écart = constatée − stock(instant du comptage)` — figé sur la ligne. Un mouvement survenu après le comptage est **réel** et ne doit pas être annulé : le delta reste arithmétiquement juste quoi qu'il arrive ensuite |
| Validation | Chaque ligne à écart non nul produit une `CORRECTION_POS` / `CORRECTION_NEG` avec `motif = inventaire n° …` |

**Interaction avec `DEC-023` — cas limite traité :** si, entre le comptage et la validation, les ventes ont fait descendre le stock au point qu'appliquer l'écart le ferait passer sous zéro, la ligne est **refusée et marquée à recompter**. Elle ne peut pas être appliquée en partie : le comptage est périmé, pas approximatif. C'est la contrepartie directe de l'invariant.
**Règle de durée — défaut retenu, non issu du corpus :** une session s'ouvre et se valide **le jour même**. Plus une campagne dure, plus les lignes périment au sens du paragraphe précédent. Ce délai s'aligne sur celui de l'annulation de vente (`US-067`).
**Rôles — défaut retenu :** ouverture et comptage par `GESTIONNAIRE_STOCK`, **validation par `ADMIN_FILIALE`**. Celui qui compte ne valide pas son propre comptage — c'est la séparation qui donne sa valeur probante à la trace.
**Périodicité :** non imposée par le produit. La campagne est déclenchée à la main ; aucune obligation n'est codée en V1.
**Conséquence assumée :** l'inventaire **initial** reste à faire à la main, article par article, puisque aucun import n'existe — voir `DEC-038`, ouverte pour ce motif.
**Coût :** +1,5 sprint.
**Impacts :** **V5 — deux tables nouvelles** (`session_inventaire`, `ligne_inventaire`), US backend à créer (aucune n'existe), `US-F074` et `US-D074` (P2 → P0, périmètre à réécrire), `GS-IA:76`, `MVT-06`, `DEC-023`, `DEC-037`, REF §4, §5 et §6.

---

## Lot 5 — Décisions complémentaires (tranchées le 7 septembre 2026)

> Ces neuf décisions clôturent les points ouverts du 26 août. Chacune pose un **défaut retenu**, appliqué par défaut ; la section « Validation porteur » liste les trois points que le porteur doit entériner avant le démarrage de l'implémentation. Toute évolution ultérieure passe par une nouvelle `DEC-nnn`.

### DEC-030 — Phasage V1 / V1.5, schéma complet dès V5 ✅
**Question :** livraison unique, ou jalons V1 / V1.5 avec schéma complet dès V5 ?
**Décision :** **deux jalons — V1 puis V1.5** — le **schéma est complet dès la migration V5** (catalogue groupe, transferts multi-lignes à états, paiements/sessions de caisse, lots et péremption, inventaire, alertes à destinataires, idempotence, `email_verifie`, plans à 3 valeurs). Ce qui est **différé en V1.5** est uniquement de l'applicatif : FEFO et choix de lot en caisse, code-barres, SMS, souscription en ligne.
**V1 — définition de done :** catalogue groupe + prix par filiale ; transferts avec demande/approbation/réception et écarts ; commandes fournisseur avec réception partielle ; commandes client B2B avec règlement/échéance ; caisse (paiement mixte, clôture, remboursement) ; inventaire sans gel ; alertes in-app + e-mail ; schéma lot-ready ; conditionnement ; idempotence ; import de mise en service (`DEC-038`).
**V1.5 — reporté sans dette :** FEFO, code-barres, SMS, souscription Mobile Money en ligne.
**Coût :** nul (rephasage).
**Impacts :** `GS-PLAN`, ventilation du compteur V1 / V1.5 à poser au planning (`DEC-031`).

### DEC-031 — Lot de stabilisation « machine et corpus » ✅
**Question :** ~60 points mécaniques (compteurs, chemins, CI, gouvernance) relevés par les trois audits.
**Décision :** les traiter en **un lot dédié, sans aucun changement de comportement, avant tout nouveau module**. Chaque item = une **petite PR isolée** avec une **preuve de sortie**. La référence de suivi est le fichier vivant **`document/03-pilotage/checklist-stabilisation-DEC-031.md`** — et non ce paragraphe.
**Coût :** +0,5 sprint.
**Impacts :** `strategie_test.md`, `A_JIRA_ET_GIT_FLOW.md`, `guideconfiguration.md`, `test_postman.md`, `postman_collection.json`, CI, `progress-ledger.md`.

### DEC-032 — Politique sauvegarde / restauration / rétention / purge ✅
**Décision (défaut retenu) :** PostgreSQL — `pg_dump` quotidien + archivage WAL, **RPO ≤ 24 h, RTO ≤ 4 h** ; Redis — RDB quotidien (rejouable, non critique) ; MinIO — versionné. Rétention : **30 jours sur site, 3 mois hors site**. Test de restauration **trimestriel**, tracé dans `progress-ledger`. **Aucune purge de donnée métier** (la limite d'historique est un filtre d'affichage, cf. `DEC-015`).
**Coût :** +0,2 sprint (documentation + scripts).
**Impacts :** REF §10 (à rédiger), `guideconfiguration.md`.

### DEC-033 — Volumétrie cible et archivage de `mouvement_stock` ✅
**Décision (défaut retenu) :** dimensionnement cible **10 M mouvements / groupe de 5 filiales / an**. Le journal étant **immuable**, l'archivage = déplacement vers un **partitionnement mensuel** dès V5 (`PARTITION BY RANGE` sur `date_mouvement`) — aucune suppression. Rapports par défaut sur 24 mois ; au-delà, lecture sur partitions d'archive.
**Coût :** +0,2 sprint (intégré au V5).
**Impacts :** V5, REF §10.

### DEC-034 — Éléments retirés : abandon définitif ✅
**Décision :** le **point de fidélité** de `diagrams/10` (aucune spécification, `DEC-009` l'a retiré) est **abandonné définitivement** — ni V1, ni V1.5. Toute demande future exigera une spécification complète nouvelle. Aucun autre élément « retiré sans spécification » n'est identifié par les trois audits.
**Coût :** nul.

### DEC-038 — Import de mise en service en V1 (CSV) ✅
**Question :** voir formulation initiale du 26 août (2 000 références à la main, blocage `DEC-023`).
**Décision :** **import CSV en V1** sur trois périmètres : **(1)** catalogue (catégories + articles), **(2)** stock initial par filiale (génère le mouvement d'ouverture `CORRECTION_POS`, motif « import mise en service »), **(3)** tiers (clients + fournisseurs). Gabarit téléchargeable + **rapport d'erreurs ligne à ligne** ; import **transactionnel** (tout ou rien par lot validé). Aucune reprise d'historique (ventes, transferts, commandes) en V1.
**Amendement de `DEC-036` :** la « conséquence assumée » (« inventaire initial à la main ») est **levée** — l'inventaire initial passe par l'import de stock.
**Coût :** +1,0 sprint.
**Impacts :** service d'import, EPIC onboarding, `GS-IA`, REF §1 et §4. Ce choix lève la friction d'entrée qui bloquait l'essai 90 jours (`DEC-015`).

### DEC-039 — `change-password` aligné sur US-085 (best-effort si Redis down) ✅
**Question (audit-09 R3, Q-change-password) :** `changePassword()` fait `redisTemplate.delete()` **sans try/catch** → 500 non protégé si Redis est indisponible.
**Décision :** comportement **best-effort identique à `logout`** : la suppression du refresh token en cache est tentée sans bloquer ; un échec Redis n'empêche pas le changement de mot de passe. La révocation reste garantie par le mot de passe lui-même.
**Coût :** nul. **Impacts :** `AuthServiceImpl`, test dédié, REF §7, `US-085` étendue au endpoint.

### DEC-040 — Session mono-appareil assumée en V1 (multi-appareils en V1.5) ✅
**Question (audit-09 R6, Q-session) :** clé Redis `refresh:{userId}` — un second login écrase la famille du premier.
**Décision :** **mono-appareil assumé en V1** : une seule famille de refresh par utilisateur ; toute nouvelle connexion révoque la précédente (cohérent avec la rotation et la détection de rejeu `US-083`). Le **multi-appareils** (clé par `device_id`) est posé en V1.5. L'interface affiche l'avertissement à la connexion.
**Coût :** nul (le code actuel l'implémente déjà).
**Impacts :** REF §7, `guideconfiguration.md`, message frontend.

### DEC-041 — Rate limit IP : derrière un proxy de confiance ✅
**Question (audit-09 Q-IP) :** le rate limit par IP lit `X-Forwarded-For`, header forgeable par le client.
**Décision :** règle de déploiement : **en production, l'application est derrière un proxy qui écrase `X-Forwarded-For`** (nginx / reverse proxy) et le filtre **ignore tout `X-Forwarded-For` non configuré** (liste blanche de proxies dans la configuration). Hors production, l'adresse directe fait foi.
**Coût :** nul (configuration).
**Impacts :** `guideconfiguration.md`, `docker-compose.yml`, REF §9.

---

## Validation porteur — 7 septembre 2026

| Point | Décision à entériner | Position par défaut (déjà appliquée) |
|---|---|---|
| Prix des plans (`DEC-015`) | grille tarifaire PRO | non bloquant pour le code (les limites sont fixées) |
| Phasage V1 / V1.5 (`DEC-030`) | définition de done V1 ci-dessus | à confirmer au premier comité de planification |
| Chiffres de volumétrie (`DEC-033`) | 10 M / an, rétention 24 mois | valeurs de dimensionnement, ajustables sans impact schéma |

> **Règle :** sauf contre-ordre écrit du porteur **avant le démarrage de l'implémentation**, les défauts retenus du Lot 5 **font foi**. Toute remise en cause ultérieure passe par une nouvelle `DEC-nnn`.

**Correspondance des audits :** la table entre `GS-AUDIT-2026-01`, `INCOHERENCES_DOCUMENTAIRES_StockMaster_CM.docx` et `audit-analyse-2026-09.md` est dans `document/referentiel/annexe-correspondance-audits.md`.
**Suivi des points mécaniques :** `document/03-pilotage/checklist-stabilisation-DEC-031.md`.
