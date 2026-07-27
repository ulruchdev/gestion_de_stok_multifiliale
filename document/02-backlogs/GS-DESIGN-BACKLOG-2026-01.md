# Backlog Design (UX/UI) — StockMaster CM
### Référence : GS-DESIGN-BACKLOG-2026-01 | Version : 1.0 | Date : Juillet 2026 | Statut : Proposé
### Documents parents : GS-CDA-2026-01 (analyse fonctionnelle), GS-IA-2026-01 (architecture de l'information), BACKLOG_StockMaster_CM.md (backend)

---

## 1. Objet et portée

Ce backlog découpe la production des livrables UX/UI en user stories de design, au même format que le backlog backend (US + critères d'acceptation + story points + priorité), pour permettre un suivi Agile identique côté design.

**Ce backlog ne remplace pas GS-IA-2026-01** (l'arborescence) : il en est la suite opérationnelle — chaque écran listé ici correspond à un nœud de l'arborescence GS-IA-2026-01.

**Outil de production recommandé :** Figma (composants réutilisables, gestion de tokens, prototypage cliquable, hand-off développeur via Dev Mode). Aucun outil n'est actuellement en place dans le projet — c'est le premier blocage à lever avant EPIC-D01.

**Convention de nommage :** `US-D0XX` (D = Design) pour distinguer des `US-0XX` backend déjà utilisés dans `BACKLOG_StockMaster_CM.md`.

**Story points :** échelle Fibonacci (1, 2, 3, 5, 8), même convention que le backlog backend, mais calibrée sur l'effort design (recherche + wireframe + UI + prototype), pas sur l'effort de code.

---

## 2. EPIC-D00 — Fondations : Design System

> Prérequis bloquant pour tous les autres EPICs. Sans design system, chaque écran est produit isolément et l'incohérence visuelle (déjà un risque avec un produit destiné à des PME peu technophiles) devient certaine.

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-D001 | En tant que designer, je définis la palette de couleurs (primaire, secondaire, sémantique : succès/erreur/alerte/info) | 3 | **P0** | Palette validée avec ratio de contraste WCAG AA minimum sur texte/fond ; couleur d'alerte stock distincte de la couleur d'erreur générique |
| US-D002 | En tant que designer, je définis la typographie (échelle de tailles, poids, hiérarchie H1-H6, corps de texte) | 2 | **P0** | Échelle typographique documentée ; lisible sur écran mobile bas de gamme (contexte camerounais — GS-CDA-2026-01 §1.4) |
| US-D003 | En tant que designer, je crée la bibliothèque de composants atomiques (boutons, champs de saisie, badges d'état, tableaux, cartes) | 8 | **P0** | Composants Figma avec variantes (états : défaut, focus, erreur, désactivé, chargement) ; correspondance 1:1 avec les composants shadcn/ui ou équivalent utilisés côté frontend pour éviter la dérive design→code |
| US-D004 | En tant que designer, je définis les badges d'état de commande (EN_PREPARATION / VALIDEE / LIVREE) et de mouvement de stock | 2 | **P0** | Code couleur cohérent et distinct pour chaque état de la machine à états (GS-CDA-2026-01 §7.2) |
| US-D005 | En tant que designer, je définis les grilles responsive (mobile-first, breakpoints tablette/desktop) | 3 | **P0** | Grille testée sur 360px (mobile bas de gamme), 768px (tablette), 1280px+ (desktop) — cohérent avec l'accès mobile dominant signalé en §1.4 |
| US-D006 | En tant que designer, je documente les icônes utilisées (stock, alerte, transfert, facture, utilisateur) | 2 | P1 | Set d'icônes cohérent (lucide-react recommandé — déjà disponible côté frontend React) |
| US-D007 | En tant que designer, je produis les templates d'état vide, de chargement et d'erreur réseau | 3 | **P0** | Gère explicitement les coupures réseau fréquentes (§1.4) — état "hors ligne" / "resynchronisation" prévu, pas seulement un spinner infini |
| US-D008 | En tant que designer, je définis le logotype StockMaster (logomark, logotype, variantes, placement) | 2 | **P0** | Logo décliné en 4 variantes contextuelles (landing, sidebar, auth, default) ; tailles sm/md/lg ; cliquable sur toutes les pages sauf si variant=default ; logomark (icône Warehouse) + logotype "StockMaster." présent sur tous les layouts ; accessible (aria-label sur les liens) |

**Sous-total EPIC-D00 : 25 SP**

---

## 3. EPIC-D01 — Espace public et onboarding

*Correspond à GS-IA-2026-01 §7, flow GS-CDA-2026-01 §4.1*

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-D010 | Wireframe + UI : page d'accueil publique | 3 | **P0** | Value proposition claire en < 5 secondes de lecture ; CTA "Créer mon espace" visible sans scroll sur mobile |
| US-D011 | Wireframe + UI : écran de choix du type d'inscription (Entreprise unique vs Groupe) | 2 | **P0** | Les deux options sont présentées sans biais visuel favorisant l'une ou l'autre ; aide contextuelle expliquant la différence |
| US-D012 | Wireframe + UI : formulaire Entreprise Unique (1 écran) | 3 | **P0** | Formulaire tenant sur un seul écran mobile sans scroll excessif ; validation inline des champs |
| US-D013 | Wireframe + UI : formulaire Groupe multi-sites (2 blocs) | 3 | **P0** | Les 2 blocs (Groupe / Administrateur) sont visuellement séparés ; progression claire si multi-étapes |
| US-D014 | Wireframe + UI : écran de connexion + mot de passe oublié | 2 | **P0** | Cohérent avec AUTH-02, AUTH-05 ; logo StockMaster affiché en haut du card (via AuthLayout) |
| US-D015 | Prototype cliquable : parcours d'inscription complet (2 chemins) | 3 | **P0** | Prototype Figma naviguant les 2 chemins A/B de bout en bout, testable en revue client |

**Sous-total EPIC-D01 : 16 SP**

---

## 4. EPIC-D02 — Dashboards (Groupe / Filiale)

*Correspond à GS-IA-2026-01 §1-2, fonctionnalités GRP-06, MVT-08*

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-D020 | Wireframe + UI : Dashboard Admin Groupe (vue consolidée) | 5 | **P0** | Widgets : stock total, CA global, alertes toutes filiales, comparatif rapide inter-filiales |
| US-D021 | Wireframe + UI : Dashboard Admin Filiale | 3 | **P0** | Widgets scopés à une seule filiale ; réutilise les mêmes composants que D020 sans dupliquer la logique visuelle |
| US-D022 | Wireframe + UI : Dashboard Gestionnaire Stock / Caissier (vue simplifiée) | 3 | P1 | Vue épurée orientée tâche (alertes de rupture, ventes du jour) plutôt que reporting |
| US-D023 | UI : composant graphique d'évolution des ventes (courbe CA) | 3 | P1 | Compatible recharts (bibliothèque déjà disponible côté frontend React) |

**Sous-total EPIC-D02 : 14 SP**

---

## 5. EPIC-D03 — Catalogue (Articles & Catégories)

*Correspond à modules CAT/ART*

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-D030 | Wireframe + UI : liste des articles (recherche, filtres, pagination) | 3 | **P0** | Pattern de pagination cohérent avec les autres listes (Exigence transversale §8.1 GS-CDA-2026-01) — un seul pattern de liste réutilisé partout |
| US-D031 | Wireframe + UI : formulaire création/édition d'article (prix HT/TTC, TVA, seuil d'alerte) | 5 | **P0** | Le TTC est affiché en lecture seule calculé dynamiquement, jamais un champ saisissable (règle §8.4) |
| US-D032 | Wireframe + UI : fiche détail article (stock actuel, historique, statut alerte) | 3 | **P0** | Reprend le badge d'alerte (US-D004) quand stock ≤ seuil |
| US-D033 | Wireframe + UI : gestion des catégories (liste + TVA par défaut) | 2 | **P0** | Blocage visuel explicite si suppression impossible (catégorie non vide) |
| US-D034 | UI : upload photo article | 1 | P2 | Cohérent avec état de chargement (US-D007) |

**Sous-total EPIC-D03 : 14 SP**

---

## 6. EPIC-D04 — Tiers (Clients & Fournisseurs)

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-D040 | Wireframe + UI : liste + fiche client (avec historique commandes) | 3 | **P0** | Réutilise le pattern de liste (US-D030) |
| US-D041 | Wireframe + UI : liste + fiche fournisseur | 3 | **P0** | Idem |
| US-D042 | Wireframe + UI : formulaires création/édition client et fournisseur | 3 | **P0** | Champ adresse structuré (quartier/ville/région — spécificité camerounaise §1.4) |

**Sous-total EPIC-D04 : 9 SP**

---

## 7. EPIC-D05 — Commandes Fournisseur (Achats)

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-D050 | Wireframe + UI : création de commande fournisseur (sélection articles multi-lignes) | 5 | **P0** | UX de saisie de lignes optimisée mobile (ajout rapide, calcul du total à la volée) |
| US-D051 | Wireframe + UI : liste des commandes fournisseur avec filtres par état | 3 | **P0** | Badges d'état (US-D004) appliqués |
| US-D052 | Wireframe + UI : écran de validation de commande (état EN_PREPARATION → VALIDEE) | 3 | **P0** | Confirmation explicite avant action irréversible (règle GS-CDA-2026-01 §5.2) |
| US-D053 | Wireframe + UI : détail commande (lignes, totaux HT/TTC) | 2 | **P0** | — |

**Sous-total EPIC-D05 : 13 SP**

---

## 8. EPIC-D06 — Commandes Client & Vente Directe

*Le flow le plus critique côté UX — inclut le blocage sur stock insuffisant (UC-03) et la caisse*

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-D060 | Wireframe + UI : création de commande client | 5 | **P0** | Réutilise pattern multi-lignes de D050 |
| US-D061 | Wireframe + UI : écran d'erreur "stock insuffisant" à la validation | 3 | **P0** | Affiche clairement, par article, la quantité demandée vs disponible (GS-SEQ-2026-01 §3) — pattern d'erreur distinct d'une erreur de validation de formulaire classique |
| US-D062 | Wireframe + UI : génération et aperçu de facture PDF | 3 | P1 | — |
| US-D063 | Wireframe + UI : écran caisse / vente directe — optimisé rapidité de saisie | 5 | **P0** | Flow pensé pour un usage tactile rapide en point de vente ; recherche d'article en 1-2 taps |
| US-D064 | Wireframe + UI : ticket de caisse (impression / partage) | 2 | P1 | Format imprimable + partageable (WhatsApp — usage courant au Cameroun) |
| US-D065 | Wireframe + UI : écran d'annulation de vente | 2 | P1 | Reflète le comportement `ANNULATION_VENTE` (GS-CDA-2026-02) |

**Sous-total EPIC-D06 : 20 SP**

---

## 9. EPIC-D07 — Stock (mouvements, corrections, transferts)

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-D070 | Wireframe + UI : historique des mouvements de stock (table filtrable) | 3 | **P0** | Colonnes : type, quantité, date, origine, utilisateur — traçabilité visible en un coup d'œil |
| US-D071 | Wireframe + UI : formulaire de correction manuelle (motif obligatoire) | 3 | **P0** | Le champ motif est visuellement marqué obligatoire, pas seulement en validation serveur |
| US-D072 | Wireframe + UI : écran de transfert inter-filiales | 5 | P1 | Affiche le stock disponible en temps réel sur la filiale source pendant la saisie (GS-CDA-2026-01 §4.8 étape 4) |
| US-D073 | Wireframe + UI : bon de transfert (document consultable) | 2 | P1 | — |
| US-D074 | Wireframe + UI : inventaire guidé | 5 | P2 | Hors périmètre V1 (MVT-06 = P2) |

**Sous-total EPIC-D07 : 18 SP**

---

## 10. EPIC-D08 — Gestion Groupe / Filiales / Utilisateurs

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-D080 | Wireframe + UI : liste et création de filiale | 3 | **P0** | — |
| US-D081 | Wireframe + UI : liste et création d'utilisateur (avec sélection de rôle métier) | 3 | **P0** | Sélecteur de rôle avec description courte de chaque rôle (aide à la décision pour l'admin) |
| US-D082 | Wireframe + UI : paramètres groupe (logo, devise, infos fiscales) | 2 | P1 | — |
| US-D083 | Wireframe + UI : profil utilisateur (édition infos personnelles, mot de passe) | 2 | P1 | — |

**Sous-total EPIC-D08 : 10 SP**

---

## 11. EPIC-D09 — Statistiques & Reporting

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-D090 | Wireframe + UI : top articles vendus / clients fidèles | 3 | P1 | — |
| US-D091 | Wireframe + UI : alertes de rupture imminente (liste dédiée) | 2 | P1 | Distincte du badge d'alerte ponctuel — vue de pilotage |
| US-D092 | Wireframe + UI : comparaison inter-filiales | 3 | P2 | — |
| US-D093 | UI : export CSV/PDF (bouton + confirmation) | 1 | P2 | — |

**Sous-total EPIC-D09 : 9 SP**

---

## 12. EPIC-D10 — Back-office Super Admin

*Interface visuellement distincte — signale clairement à l'opérateur qu'il n'est pas dans l'espace client*

| ID | User Story | SP | Priorité | Critères d'acceptation |
|---|---|---|---|---|
| US-D100 | Wireframe + UI : liste des tenants/groupes (activer/suspendre/supprimer) | 3 | P1 | Thème visuel différent de l'espace client pour éviter toute confusion opérateur/client |
| US-D101 | Wireframe + UI : métriques plateforme (usage, revenus) | 3 | P1 | — |
| US-D102 | Wireframe + UI : alertes techniques | 2 | P1 | — |

**Sous-total EPIC-D10 : 8 SP**

---

## 13. Récapitulatif et priorisation

| EPIC | SP total | Priorité dominante | Sprint cible indicatif |
|---|---|---|---|
| EPIC-D00 — Design System | 23 | P0 | Sprint 0 (avant tout développement frontend) |
| EPIC-D01 — Onboarding public | 16 | P0 | Sprint 1 |
| EPIC-D02 — Dashboards | 14 | P0/P1 | Sprint 2 |
| EPIC-D03 — Catalogue | 14 | P0 | Sprint 2-3 |
| EPIC-D04 — Tiers | 9 | P0 | Sprint 3 |
| EPIC-D05 — Commandes fournisseur | 13 | P0 | Sprint 4 |
| EPIC-D06 — Commandes client & Caisse | 20 | P0 | Sprint 4-5 |
| EPIC-D07 — Stock | 18 | P0/P1 | Sprint 5-6 |
| EPIC-D08 — Groupe/Filiales/Utilisateurs | 10 | P0/P1 | Sprint 1-2 |
| EPIC-D09 — Statistiques | 9 | P1/P2 | Sprint 7+ |
| EPIC-D10 — Back-office Super Admin | 8 | P1 | Sprint 8+ |
| **Total** | **156 SP** | | |

**Règle de séquencement critique :** EPIC-D00 (Design System) doit être terminé et validé avant le démarrage de tout autre EPIC design — c'est un prérequis dur, pas une simple priorité haute. Un écran produit sans design system validé sera repris.

---

## 14. Lien avec le backlog frontend

Chaque écran livré ici (Figma, avec spécifications de Dev Mode : espacements, couleurs en tokens, comportements d'interaction) est l'**entrée** de la user story frontend correspondante dans `GS-FRONTEND-BACKLOG-2026-01.md`. Aucun développement frontend d'un écran ne doit démarrer sans que la maquette correspondante soit au statut "Validée" — voir `GS-RACI-2026-01_matrice_livraison.md` pour le séquencement inter-équipes formel.

---

## 15. Livrables attendus par user story de design

Chaque US ci-dessus, pour être considérée "Terminée", doit produire :

1. Wireframe basse fidélité (structure, sans style) — pour validation rapide du flow
2. UI haute fidélité (Figma, avec composants du design system)
3. Spécification responsive (mobile / tablette / desktop)
4. Annotations Dev Mode (espacement, couleurs en tokens, états d'interaction)
5. Prototype cliquable pour les flows multi-écrans (onboarding, commande, caisse)

---

## 16. Risques identifiés

| Risque | Impact | Mitigation |
|---|---|---|
| Aucun designer identifié à date sur le projet | Bloquant pour tout cet EPIC | Confirmer la ressource avant Sprint 0 — sinon ce backlog reste théorique |
| Décalage entre composants Figma et composants shadcn/ui réellement utilisés en frontend | Rework et incohérence visuelle | US-D003 impose la correspondance 1:1 dès la conception |
| Absence de test utilisateur avec de vraies PME camerounaises | Risque de UX inadaptée au contexte réel (coupures réseau, usage mobile) | Prévoir une session de test utilisateur après EPIC-D01 et EPIC-D06 (flows les plus fréquents) avant généralisation du pattern à tout le reste |
