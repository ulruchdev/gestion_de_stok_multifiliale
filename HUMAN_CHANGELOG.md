
# 📜 HUMAN_CHANGELOG — StockMaster CM

> Journal des changements en langage métier (non technique).
> Mis à jour par `doc-writer.ts` à chaque US terminée.

## Juillet 2026

### Sprint 2 — Corrections sécurité & alignement DTO (Semaine 3)

- **Rate limiting rendu configurable** : Plus de valeurs hardcodées. Deux niveaux de protection :
  - **Global** : 100 requêtes/minute/IP pour détecter les bots
  - **Par endpoint** : login (5/15min), inscription (3/1h), refresh (5/15min), forgot-password (3/15min), reset-password (3/15min), change-password (5/15min)
  - Tout est modifiable dans `application.yml` via la section `stockmaster.rate-limiting`
- **Bug JWT critique corrigé** : Les endpoints publics (inscription, mot de passe oublié) ne sont plus bloqués quand le navigateur envoie un token périmé. Le filtre JWT ne retourne plus 401 sur les endpoints `.permitAll()`
- **DTO inscription entreprise unique aligné avec le frontend** :
  - `nomBoutique` → `nomEntreprise`
  - `ville` et `quartier` retirés (seront demandés dans le profil plus tard)
  - `prenom`/`nom` → `adminPrenom`/`adminNom`
  - `nif` ajouté (optionnel — les petites boutiques n'ont pas de NIF au Cameroun)
  - `telephone` ajouté (format international E.164 obligatoire, ex: +237691234567)
- **Téléphone avec indicatif pays** : `react-phone-number-input` intégré dans le frontend avec sélecteur de pays + détection automatique

### Sprint 1 — Design System StockMaster

- Mise en place de l'architecture Freebuff pour piloter le projet avec des agents spécialisés
- Initialisation des 6 agents : `session-bootstrap`, `task-architect`, `spring-module-guardian`, `react-frontend-guardian`, `doc-writer`, `git-committer`
- Organisation des documents en 4 dossiers thématiques dans `document/`
- Création des 4 formations exhaustives dans `formation/` pour l'équipe
- Mise en place de la matrice RACI (portes G1→G7) pour coordonner les livraisons
- Planning projet sur 16 sprints (~7-8 mois) avec l'équipe Ulrich, Stephan et Siko

### Sprint 1 — Design System StockMaster

- **Pages d'authentification créées (EPIC-F01)** :
  - `LoginPage` — Formulaire de connexion avec validation Zod, show/hide password, comptes de démonstration en dev
  - `InscriptionChoixPage` — Choix entre Entreprise unique et Groupe multi-sites (2 cartes interactives)
  - `InscriptionEntrepriseUniquePage` — Formulaire multi-étapes avec stepper, validation mot de passe forte
  - `InscriptionGroupePage` — Formulaire 4 étapes (Groupe → Filiale → Admin → Confirmation)
  - `ForgotPasswordPage` — Email de réinitialisation avec sécurité (ne révèle pas l'existence du compte)
  - `ResetPasswordPage` — Nouveau mot de passe avec validation Zod, gestion token expiré
  - Routes mises à jour avec lazy loading (React.lazy + Suspense)
  - Handlers MSW ajoutés pour les endpoints forgot-password et reset-password

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
