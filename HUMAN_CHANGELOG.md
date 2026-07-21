# 📜 HUMAN_CHANGELOG — StockMaster CM

> Journal des changements en langage métier (non technique).
> Mis à jour par `doc-writer.ts` à chaque US terminée.

## Juillet 2026

### Sprint 0 — Fondations

- Mise en place de l'architecture Freebuff pour piloter le projet avec des agents spécialisés
- Initialisation des 6 agents : `session-bootstrap`, `task-architect`, `spring-module-guardian`, `react-frontend-guardian`, `doc-writer`, `git-committer`
- Organisation des documents en 4 dossiers thématiques dans `document/`
- Création des 4 formations exhaustives dans `formation/` pour l'équipe
- Mise en place de la matrice RACI (portes G1→G7) pour coordonner les livraisons
- Planning projet sur 16 sprints (~7-8 mois) avec l'équipe Ulrich, Stephan et Siko

### Sprint 1 — Design System StockMaster

- **Création du design système propriétaire StockMaster CM** dans le frontend React :
  - Palette de couleurs spécifique (indigo `#533afd`, textes contrastés, états stock warning/danger/success)
  - Mode sombre complet avec adaptation des contrastes
  - Tokens typographie, espacement (grille 8px), motion et bordures
- **Nouveaux composants UI créés** (9 composants) :
  - `Card` — Conteneur avec variantes (default, elevated, bordered, interactive)
  - `Avatar` — Avatar avec initiales, statut en ligne, groupe d'avatars
  - `Skeleton` — Loaders de chargement (text, circle, card, table)
  - `EmptyState` — États vides avec 5 variantes et CTA
  - `Tabs` — Navigation par onglets (underline, pills, segmented)
  - `Select` — Menu déroulant stylisé
  - `SearchInput` — Barre de recherche avec debounce et clear
  - `DataTable` — Tableau enrichi avec tri, pagination et gestion d'états
  - `OfflineBanner` — Gestion des coupures réseau (contexte camerounais)
- **Composants existants améliorés** :
  - `Button` : nouveaux variants brand, brand-outline, pill
  - `Input` : états success/warning/error, support icône
  - `Badge` : indicateur visuel (dot), supprimable, 10 variants
  - `Modal` : animation scale-in, taille xl
  - `Table` : variantes compact et striped
  - `Toast` : barre de progression, position configurable
- **Layouts mis à jour** :
  - `AuthLayout` : fond épuré, animation fade-in
  - `DashboardLayout` : sidebar sombre, navigation par opacité
- Migration du fichier `features/auth/index.ts` → `index.tsx` pour support JSX
- Documentation : DESIGN_CORRECTIONS.md (25 corrections), DESIGN_TOKENS_REFERENCE.md (guide complet)

---

*Ce fichier est automatiquement mis à jour par l'agent `doc-writer`. Ne pas éditer manuellement.*
