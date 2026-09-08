# RÉFÉRENTIEL — Partie 1 : Vision, cible, périmètre V1/V1.5

### `GS-REF-2026-01 §1` | Version : 1.0 | Rédigée le 8 septembre 2026 | Statut : **Validée (décisions DEC-015, DEC-030, DEC-002)**

---

## 1.1 La vision produit (une seule phrase)

> **StockMaster CM est l'outil de gestion de stock et de caisse qui fait tourner une boutique camerounaise dès le premier jour, et qui accompagne sa croissance vers plusieurs points de vente sans jamais la faire ressaisir son catalogue.**

Deux promesses, une seule base de code :
1. **Parcours MONO-BOUTIQUE** — une PME (boutique, épicerie, grossiste simple) gère son quotidien : catalogue, achats, stock, caisse, clients. **Vendable tel quel.**
2. **Parcours MULTI-FILIALE** — le même client, passé à 2, 3, 5 points de vente, transfère du stock entre sites, consolide son stock et son CA, sans recréer ses articles. **Le différenciateur.**

**Ordre de construction (acté le 8 septembre 2026) :** le parcours mono-boutique est construit **en premier** (il est le socle et le cas d'usage quotidien) ; le multi-filiale fait **partie de la V1 livrée** (décision `DEC-002` : catalogue groupe en P1 avant le transfert ; `DEC-030` : schéma complet dès V5).

## 1.2 Le client de référence V1

| Attribut | Valeur (cible) |
|---|---|
| Segment | Boutique / épicerie / grossiste simple camerounais (1 à 5 points de vente) |
| Contexte | Accès mobile dominant, coupures réseau fréquentes, TVA 19,25 %, devise XAF |
| Besoin n°1 | Ne jamais perdre la trace de son stock et de sa caisse |
| Besoin n°2 | Réapprovisionner sans erreur (commandes fournisseur) |
| Besoin n°3 | (en croissance) transférer du stock entre ses sites |
| Non-cible V1 | Pharmacie (lots/FEFO complets → V1.5, `DEC-012`), distributeur à acomptes (`DEC-011`), multi-devise (`DEC-022`) |

## 1.3 Les deux parcours et leurs user stories

### Parcours A — MONO-BOUTIQUE (construit en premier)

> Un utilisateur (Admin Filiale ou Employé) fait tourner **une** boutique. Toutes ces US sont livrées **avant** les US multi-filiales.

| Domaine | US concernées | Note |
|---|---|---|
| Socle technique | US-001 → US-005 | fondations, CI, conteneurisation |
| Inscription & auth | US-006, US-008 → US-013, US-082 → US-086 | entreprise unique + connexion + mots de passe |
| Groupe/profil | US-014, US-014b, US-015, US-081 | une entreprise = un groupe à 1 site |
| Utilisateurs | US-021 → US-026, US-074, US-075 | Admin Filiale + employés (4 rôles) |
| Catalogue | US-027 → US-033 | catégories + articles (prix achat/vente, seuil, TVA) |
| Tiers | US-034 → US-043 | clients + fournisseurs |
| Achats | US-044 → US-050 | commandes fournisseur avec réception partielle (`DEC-006`) |
| Stock | US-051 → US-054 | stock réel, historique, corrections |
| Ventes B2B | US-056 → US-062 | commandes client, livraison |
| Caisse | US-064 → US-067 | vente directe, session de caisse (`DEC-009`), annulation (`DEC-010`) |
| Alertes | US-071 → US-073 | seuil de stock (`DEC-004`) |
| Reporting | US-076 → US-080 | top articles, clients, CA, export CSV |

### Parcours B — MULTI-FILIALE (inclus dans la V1, construit après le parcours A)

> Un Admin Groupe gère plusieurs points de vente. Prérequis : `DEC-002` (catalogue au niveau groupe) et `DEC-007` (transfert multi-lignes à états).

| Domaine | US concernées | Note |
|---|---|---|
| Inscription groupe | US-007 | création groupe + maison mère |
| Filiales | US-016 → US-019 | créer/lister/modifier/activer-désactiver |
| Dashboard consolidé | US-020 | CA + stock du groupe |
| Stock consolidé | US-055 | agrégation par article sur toutes les filiales |
| Transferts | US-068 → US-070 | demande → approbation → expédition → réception (`DEC-002`, `DEC-006`) |
## 1.4 Périmètre V1 vs V1.5 (décision DEC-030)

**Règle : le schéma est complet dès la migration V5. Ce qui est différé en V1.5 est uniquement de l'applicatif.**

| Fonctionnalité | V1 | V1.5 |
|---|---|---|
| Catalogue groupe + prix par filiale | ✅ | — |
| Transferts avec états + écarts | ✅ | — |
| Commandes fournisseur, réception partielle | ✅ | — |
| Commande client B2B, règlement/échéance | ✅ | — |
| Caisse : paiement mixte, clôture, remboursement | ✅ | — |
| Inventaire sans gel | ✅ | — |
| Alertes in-app + e-mail | ✅ | — |
| Schéma lot-ready (colonnes) | ✅ | — |
| Conditionnement (facteur de conversion) | ✅ | — |
| Idempotence (`Idempotency-Key`) | ✅ | — |
| Import CSV mise en service | ✅ | — |
| **FEFO + choix de lot en caisse** | — | ✅ |
| **Code-barres** | — | ✅ |
| **SMS** | — | ✅ |
| **Souscription en ligne Mobile Money** | — | ✅ |
| **Pharmacie (segment)** | — | ✅ |

**Définition de done V1** (le produit est « utilisable sur le terrain » quand) :
1. Une boutique s'inscrit, crée son catalogue, reçoit une commande fournisseur, vend en caisse, et voit son stock réel à jour.
2. Un groupe à 2 sites transfère du stock avec traçabilité et voit son stock consolidé.
3. Les données d'un client réel peuvent être importées (CSV, `DEC-038`).
4. `main` compile, CI verte, 0 point ouvert de gouvernance.

## 1.5 Ce que le produit n'est PAS en V1 (limites assumées, à dire au commercial)

- Pas de pharmacie vendable (lots/FEFO → V1.5, `DEC-012`).
- Pas d'acomptes B2B ni d'encours (règlement simple NON_REGLEE/REGLEE + échéance, `DEC-011`).
- Pas de multi-devise (`DEC-022`), pas de SMS (`DEC-014`), pas de souscription en ligne (`DEC-015`).
- Pas de point de fidélité (abandonné définitivement, `DEC-034`).
- Pas d'avoir réutilisable (remboursement en caisse sans avoir, `DEC-010`).

## 1.6 Indicateurs de succès (mesure terrain)

| Indicateur | Cible V1 |
|---|---|
| Temps d'inscription → première vente | < 30 min (import CSV aidant, `DEC-038`) |
| Taux d'activation après inscription | > 60 % (compte actif dès l'e-mail vérifié, `DEC-016`) |
| Fidélité à 30 jours (boutique active) | > 70 % |
| Écart de caisse moyen à la clôture | < 0,5 % du CA encaissé (`DEC-009`) |

---

*Sources : `DEC-002`, `DEC-006`, `DEC-009`, `DEC-010`, `DEC-011`, `DEC-012`, `DEC-014`, `DEC-015`, `DEC-016`, `DEC-022`, `DEC-030`, `DEC-034`, `DEC-038` ; backlog `GS-BACKLOG-2026-01`.*