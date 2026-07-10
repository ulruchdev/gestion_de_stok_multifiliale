# Addendum à l'Analyse Fonctionnelle — StockMaster CM
### Référence : GS-CDA-2026-02 | Version : 1.0 | Date : Juillet 2026 | Statut : Validé
### Document parent : GS-CDA-2026-01 (analyse_fonctionnelle_stockmaster_cm.docx)

---

## Objet

Ce document complète GS-CDA-2026-01 avec 3 décisions issues d'une revue de cohérence métier menée sur le module Vente Directe (EPIC 10) et le module Tiers (EPIC 6). Il ne remplace aucune section du document parent — il précise et corrige les points où le comportement réel du terrain n'était pas explicite ou entrait en contradiction avec une règle transverse déjà actée (immuabilité du journal de mouvements, §6.4 de GS-CDA-2026-01).

À appliquer avant tout développement des US-064, US-064b et US-067 du backlog (GS-BACKLOG-2026-01 v1.1).

---

## §1 — Vente Directe : comportement non bloquant sur le stock

### Constat initial
Le document parent ne précisait pas si la Vente Directe (caisse) devait bloquer une vente en cas de stock système insuffisant, contrairement à la Commande Client (UC-03) qui bloque explicitement.

### Décision validée
**La Vente Directe ne bloque jamais sur stock insuffisant.**

### Justification métier
En caisse, le client a le produit physiquement devant lui — le facteur limitant réel est le stock physique visible par le caissier, pas le stock système. Un blocage sur stock système ferait perdre une vente réelle à cause d'un problème de donnée (désynchronisation entre deux caisses, casse non enregistrée, écart d'inventaire), pas d'un problème de disponibilité réelle.

Cette règle diffère volontairement de celle de la Commande Client, où le stock retiré se trouve dans un entrepôt non visible au moment de la saisie — le contrôle système y a un sens réel.

### Mécanisme de compensation
Si le stock réel devient négatif après une vente directe, le système ne bloque pas mais déclenche une alerte de type `ECART_STOCK_DETECTE` à destination du Gestionnaire de Stock et de l'Admin Filiale, afin qu'un recomptage ou une correction manuelle (UC-04) soit effectué. Cette alerte est distincte de l'alerte `STOCK_BAS` (seuil métier configuré) — elle signale une anomalie de donnée, pas un seuil d'approvisionnement.

### Impact sur le modèle
Aucun changement de structure. Impact sur le comportement de service uniquement (`VenteService.enregistrerVente()` ne doit pas appeler de vérification bloquante de stock).

---

## §2 — Annulation de vente : mouvement compensatoire dédié

### Constat initial
Le document parent ne définissait pas de mécanisme d'annulation pour la Vente Directe. La première version du backlog (US-067 v1.0) utilisait un mouvement `CORRECTION_POS` et un booléen `annulee` — ce qui entre en contradiction avec la règle d'immuabilité et brouille la traçabilité (un `CORRECTION_POS` est censé signifier "écart d'inventaire constaté", pas "vente annulée").

### Décision validée
1. Le mouvement `SORTIE` original d'une vente n'est **jamais modifié ni supprimé** — l'immuabilité du journal (`MouvementStock`, §6.4 GS-CDA-2026-01) est respectée à 100%.
2. Un nouveau type de mouvement est introduit : **`ANNULATION_VENTE`** — une entrée compensatoire au même titre que `ENTREE` / `CORRECTION_POS` / `TRANSFERT_ENTREE` dans le calcul du stock réel, mais avec sa propre sémantique d'origine (`origine_type = ANNULATION_VENTE`, `origine_id` = id de la vente annulée).
3. L'entité `Vente` reçoit un véritable état (`statut` : `VALIDEE` | `ANNULEE`) au lieu d'un booléen `annulee` — elle rejoint ainsi la logique de machine à états déjà appliquée aux commandes fournisseur/client, jusqu'ici absente pour les ventes.

### Justification métier
C'est la pratique standard des systèmes de caisse professionnels : on ne réécrit jamais une transaction déjà enregistrée, on crée une écriture inverse traçable. Cela permet de distinguer sans ambiguïté, dans le journal, "un écart d'inventaire" d'une "vente annulée" — deux réalités métier différentes qui ne doivent pas partager le même type de mouvement.

### Contrainte métier conservée
La règle déjà prévue (VNT-02 / US-067 v1.0) reste en vigueur : annulation possible uniquement le jour même, avant clôture de caisse, avec motif obligatoire. Elle évite qu'une vente ancienne soit "annulée" a posteriori pour maquiller un vol ou un écart de caisse.

### Impact sur le modèle
- Enum `type_mouvement` (migration Flyway) : ajout de la valeur `ANNULATION_VENTE`.
- Entité `Vente` : ajout du champ `statut` (enum), suppression du booléen `annulee`.
- CDCT section entités : à mettre à jour en conséquence (voir action de suivi en fin de document).

---

## §3 — Fidélité client sur la Vente Directe

### Constat initial
STAT-02 (« Clients les plus fidèles ») et CLI-05 (historique client) ne s'appuyaient que sur `CommandeClient` (cycle B2B). Or, pour un commerce général, une pharmacie ou une quincaillerie camerounaise, l'essentiel des transactions au comptoir se fait via la Vente Directe — qui est « sans client identifié » par définition (VNT-01). Un client régulier achetant au comptoir restait donc invisible des statistiques de fidélité.

### Décision validée
La Vente Directe reste anonyme par défaut, mais un champ `client_id` **nullable** est ajouté à l'entité `Vente`, permettant au caissier d'associer optionnellement une vente à un client déjà enregistré — sans passer par le cycle complet de `CommandeClient`.

### Justification métier
Cela répare le gap sans changer le workflow existant : la vente anonyme reste possible (client de passage), et le rattachement à un client connu, quand c'est pertinent, alimente son historique (CLI-05) et les statistiques de fidélité (STAT-02).

### Impact sur le modèle
- Entité `Vente` : ajout du champ `client_id` (FK nullable vers `Client`).
- `GET /api/v1/clients/{id}/historique` : doit désormais agréger `CommandeClient` **et** `Vente` (là où `client_id` est renseigné).

---

## Actions de suivi

| Document à mettre à jour | Action |
|---|---|
| GS-CDA-2026-01 (analyse fonctionnelle) | Ajouter une note de renvoi vers ce document dans les sections 3.7 (Vente Directe), 6.2 (entité Vente) et 6.4 (règles d'intégrité) lors de la prochaine révision majeure du docx |
| GS-BACKLOG-2026-01 | Fait — v1.1 intègre US-064, US-064b, US-067 mis à jour |
| CDCT (Cahier des Charges Technique) | Ajouter la valeur d'enum `ANNULATION_VENTE`, le champ `statut` sur `Vente`, le champ `client_id` nullable sur `Vente`, et la nouvelle notification `ECART_STOCK_DETECTE` |
| Migrations Flyway | Nouvelle migration : `ALTER TYPE type_mouvement_enum ADD VALUE 'ANNULATION_VENTE'`, ajout colonnes `statut` et `client_id` sur `vente` |
