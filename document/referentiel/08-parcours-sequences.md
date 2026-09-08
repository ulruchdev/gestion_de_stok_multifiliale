# RÉFÉRENTIEL — Partie 8 : Parcours & séquences

### `GS-REF-2026-01 §8` | Version : 1.0 | Rédigée le 8 septembre 2026 | Statut : **🔍 Dérivée — les diagrammes de séquence font foi**

> **Règle (DEC-001) :** les parcours de référence sont les diagrammes `diagrams/01→15` (vues dérivées). Cette partie liste les **parcours critiques V1** et leurs contraintes ; en cas de conflit avec les diagrammes, **cette partie + le journal gagnent**.

## 8.1 Parcours critiques V1

| Parcours | Étapes | Contraintes |
|---|---|---|
| **Inscription boutique** | inscription entreprise unique → e-mail de vérification → compte actif | `DEC-016` (inactif jusqu'à e-mail vérifié, renvoi possible) |
| **Première vente** | ouverture session caisse → sélection article → paiement (mixte) → clôture | `DEC-009`, `DEC-018` (session au nom du caissier) |
| **Réapprovisionnement** | commande fournisseur → réception (partielle) → entrée stock | `DEC-006`, `DEC-013` (conditionnement) |
| **Transfert inter-filiales** | demande → approbation → expédition → réception (écart) | `DEC-002`, `DEC-007` |
| **Inventaire** | ouverture session → comptage → écart → validation → corrections en lot | `DEC-036` |
| **Import mise en service** | télécharger gabarit → remplir CSV → importer (rapport d'erreurs) | `DEC-038` |

## 8.2 Séquences à écrire en priorité (pour l'implémentation)

1. Vente directe avec paiement mixte + clôture (le flux le plus visible).
2. Transfert complet avec écart.
3. Inscription + activation e-mail + renvoi.
4. Réception partielle de commande fournisseur.

---

*Sources : `DEC-006`, `DEC-007`, `DEC-009`, `DEC-016`, `DEC-018`, `DEC-036`, `DEC-038`.*