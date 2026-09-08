# RÉFÉRENTIEL — Partie 11 : Design system (pointeur)

### `GS-REF-2026-01 §11` | Version : 1.0 | Rédigée le 8 septembre 2026 | Statut : **🔍 Dérivée — le design system vit dans le code**

> **Règle (DEC-001) :** le design system vit dans le code (`frontend/src/shared/ui/`), pas dans Figma. Ce fichier est un **pointeur**.

## 11.1 Références

| Élément | Emplacement |
|---|---|
| Tokens de design | `document/01-architecture/DESIGN_TOKENS_REFERENCE.md` |
| Composants | `frontend/src/shared/ui/` (le code = le design) |
| Backlog design | `document/02-backlogs/GS-DESIGN-BACKLOG-2026-01.md` |
| Backlog frontend | `document/02-backlogs/GS-FRONTEND-BACKLOG-2026-01.md` |

## 11.2 Règles design normatives

- **4 badges d'état** seulement : `en_preparation`, `validee`, `livree`, `annulee` (`DEC-006`). Les badges `en_attente`, `partiel`, `rembourse` sont **retirés**.
- Accessible mobile d'abord (contexte terrain).
- Afficher l'**horodatage de fraîcheur** des données consolidées (cache, `DEC-031`).
- Avertissement non bloquant pour prix achat ≥ vente (`DEC-029`).

---

*Sources : `DEC-006`, `DEC-029`, `DEC-031`.*