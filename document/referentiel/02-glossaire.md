# RÉFÉRENTIEL — Partie 2 : Glossaire normatif

### `GS-REF-2026-01 §2` | Version : 1.0 | Rédigée le 8 septembre 2026 | Statut : **Validée (DEC-005)**

> **Règle :** un terme = une définition, utilisée partout (code, tickets, diagrammes, tests). Toute ambiguïté constatée est signalée ici et tranchée par une `DEC-nnn`.

## 2.1 Termes métier

| Terme | Définition normative | Notes / pièges |
|---|---|---|
| **Boutique** | Un point de vente physique (entreprise `MERE` ou `FILIALE`) qui détient du stock et encaisse | « site opérationnel » = boutique avec stock (`DEC-015`) |
| **Groupe** | L'entité `tenant_group` : le client SaaS (une PME), racine de l'isolation multi-tenant | Ne pas confondre avec « catalogue groupe » |
| **Catalogue groupe** | Les articles/catégories définis **une fois au niveau du groupe**, partagés par toutes les filiales | `DEC-002` — un article = un identifiant dans tout le groupe |
| **Filiale** | Une entreprise `FILIALE` rattachée à un groupe, avec son propre stock et sa propre caisse | `type_entreprise = 'FILIALE'` |
| **Maison mère** | L'entreprise `MERE` du groupe ; peut être opérationnelle (siège-entrepôt) ou non (`site_operationnel`) | `DEC-015` |
| **Stock réel** | Σ ENTREE − Σ SORTIE calculé à la volée depuis `mouvement_stock` (journal immuable) | ADR-003 ; jamais dénormalisé |
| **Caisse** | La session de caisse (`DEC-009`) : ouverture, encaissements, clôture, écart | Une vente est toujours rattachée à une session |
| **Vente directe** | Vente au comptoir, encaissée immédiatement (caisse) | `PAYEE | ANNULEE | REMBOURSEE` |
| **Commande client (B2B)** | Vente à un client connu, livrée puis réglée | `VALIDEE → LIVREE | ANNULEE` + règlement/échéance |
| **Transfert** | Mouvement de stock entre deux filiales du même groupe, avec états | `DEMANDE → VALIDE → EN_TRANSIT → RECU | ECART` (`DEC-002`) |
| **Inventaire** | Campagne de comptage datée, sans gel du stock, générant des corrections en lot | `DEC-036` |
| **Conditionnement** | Facteur de conversion unité d'achat / unité de gestion | `DEC-013` |
| **Lot / péremption** | Colonnes présentes dès V5 ; FEFO et choix de lot en caisse en V1.5 | `DEC-012` |

## 2.2 Termes techniques

| Terme | Définition normative |
|---|---|
| **V1 / V1.5** | Deux jalons de livraison. Le schéma est complet dès V5 ; V1.5 = uniquement de l'applicatif (FEFO, code-barres, SMS, souscription) — `DEC-030` |
| **Migration V5** | La migration Flyway unique qui applique **toutes** les décisions de schéma (catalogue groupe, transferts, caisse, lots, `email_verifie`, plans…) |
| **`DEC-nnn`** | Numéro d'une décision du journal (`12-journal-decisions.md`). Format de citation obligatoire |
| **`REF §x.y`** | Citation d'une règle du référentiel. Les tickets/diagrammes citent la référence, pas le texte |
| **Jalon A / Jalon B** | (réservé) si un découpage interne des sprints est nécessaire — non utilisé pour l'instant |
| **Statut de document** | ✅ actif (norme) / 🔍 dérivé (pointe vers le référentiel) / 📦 gelé (pièce justificative d'audit) / 🔜 à rédiger |

## 2.3 Statuts de documents (à appliquer en tête de chaque fichier)

| Statut | Sens | Exemple |
|---|---|---|
| **✅ Norme** | Fait autorité ; cité par les tickets | `referentiel/*` |
| **🔍 Dérivé** | Se lit comme une vue ; en conflit, le référentiel gagne | `GS-DATA`, `diagrams/*`, `GS-IA` |
| **📦 Gelé** | Trace d'un audit passé ; plus jamais une norme | les 3 audits |
| **🔜 À rédiger** | Partie du référentiel encore vide | parties 03→11, 13, 14 |

---

*Sources : `DEC-001`, `DEC-002`, `DEC-005`, `DEC-009`, `DEC-012`, `DEC-013`, `DEC-015`, `DEC-030`, `DEC-036`.*