# Architecture de l'Information — StockMaster CM
### Référence : GS-IA-2026-01 | Version : 1.0 | Date : Juillet 2026 | Statut : Proposé
### Document parent : GS-CDA-2026-01 §2 (acteurs), §7.3 (visibilité par rôle)

> ⚠️ **Statut d'implémentation :** ce document décrit l'architecture **cible**. Seuls `stockmaster-shared`, `stockmaster-auth` et `stockmaster-bootstrap` contiennent du code réel à ce jour — tous les autres modules référencés ici (`groupe`, `utilisateur`, `catalogue`, `tiers`, `achat`, `stock`, `vente`, `notification`, `reporting`) sont des stubs vides (arborescence de packages sans classes). Voir `knowledge.md` et `document/03-pilotage/progress-ledger.md` pour l'état réel.

---

## Objet

GS-CDA-2026-01 décrit **ce que** chaque rôle peut faire (§2, §3) mais aucun document ne formalise **comment le menu/la navigation** traduit ce périmètre à l'écran. Ce document comble ce trou : une arborescence de navigation par rôle, directement dérivée de la matrice de visibilité §7.3 et des EPICs du backlog. Elle sert de spécification d'entrée pour le design (maquettes) et pour le développement frontend (routing, guards).

**Principe directeur :** un rôle ne doit jamais voir dans son menu une section à laquelle il n'a pas accès en API — la navigation est un **reflet strict** du RBAC backend, jamais une source de vérité indépendante.

---

## 1. Arborescence — Admin Groupe

```mermaid
graph TD
    A[Dashboard Groupe] --> A1[Vue consolidée: stock, CA, alertes toutes filiales]
    A --> B[Groupe]
    B --> B1[Mes filiales — liste, créer, activer/désactiver]
    B --> B2[Paramètres groupe — logo, devise, infos fiscales]
    B --> B3[Catalogue partagé - P2]
    A --> C[Transferts de stock]
    C --> C1[Nouveau transfert]
    C --> C2[Historique des bons de transfert]
    A --> D[Utilisateurs]
    D --> D1[Admins Filiale — créer, lister, désactiver]
    A --> E[Rapports]
    E --> E1[Comparaison inter-filiales - P2]
    E --> E2[Top articles / clients groupe]
    A --> F[Achats groupe]
    F --> F1[Commande centralisée multi-filiales - P2]
    A --> G[Profil]
```

## 2. Arborescence — Admin Filiale

```mermaid
graph TD
    A[Dashboard Filiale] --> A1[Stock, alertes, CA de sa filiale]
    A --> B[Catalogue]
    B --> B1[Articles]
    B --> B2[Catégories]
    A --> C[Tiers]
    C --> C1[Clients]
    C --> C2[Fournisseurs]
    A --> D[Achats]
    D --> D1[Commandes fournisseur]
    A --> E[Ventes]
    E --> E1[Commandes client]
    E --> E2[Ventes directes / caisse]
    A --> F[Stock]
    F --> F1[Mouvements / historique]
    F --> F2[Corrections manuelles]
    F --> F3[Demander un transfert — validation par Admin Groupe]
    A --> G[Utilisateurs]
    G --> G1[Employés de la filiale — créer, rôles, désactiver]
    A --> H[Rapports filiale]
    A --> I[Profil]
```

## 3. Arborescence — Employé : Gestionnaire Stock

```mermaid
graph TD
    A[Dashboard] --> A1[Alertes de rupture]
    A --> B[Catalogue]
    B --> B1[Articles — créer, modifier, historique]
    B --> B2[Catégories]
    A --> C[Stock]
    C --> C1[Mouvements]
    C --> C2[Corrections positives / négatives]
    C --> C3[Inventaire guidé - P2]
    A --> D[Profil]
```

## 4. Arborescence — Employé : Responsable Achats

```mermaid
graph TD
    A[Dashboard] --> A1[Commandes en attente de réception]
    A --> B[Fournisseurs]
    B --> B1[Liste, créer, modifier]
    A --> C[Commandes fournisseur]
    C --> C1[Nouvelle commande]
    C --> C2[Mes commandes — filtrable par état]
    C --> C3[Valider / Marquer livrée]
    A --> D[Profil]
```

## 5. Arborescence — Employé : Commercial

```mermaid
graph TD
    A[Dashboard] --> A1[Mes commandes en cours]
    A --> B[Clients]
    B --> B1[Liste, créer, historique client]
    A --> C[Commandes client]
    C --> C1[Nouvelle commande]
    C --> C2[Mes commandes — filtrable par état]
    C --> C3[Valider / Générer facture PDF]
    A --> D[Profil]
```

## 6. Arborescence — Employé : Caissier / Vendeur

```mermaid
graph TD
    A[Caisse] --> A1[Nouvelle vente directe]
    A --> A2[Historique de mes ventes du jour]
    A --> A3[Consultation stock — lecture seule]
    A --> A4[Ticket de caisse — imprimer / partager]
    A --> B[Profil]
```

## 7. Espace public (non authentifié)

```mermaid
graph TD
    A[Page d'accueil] --> B[Créer mon espace]
    B --> C{Choix du type}
    C -->|Une seule boutique| D[Formulaire Entreprise Unique]
    C -->|Plusieurs sites| E[Formulaire Groupe]
    A --> F[Connexion]
    F --> G[Mot de passe oublié]
    A --> H[Activation de compte — lien email]
```

## 8. Espace Super Admin (back-office séparé — sous-domaine distinct)

```mermaid
graph TD
    A[Back-office Super Admin] --> B[Tenants / Groupes]
    B --> B1[Activer / suspendre / supprimer]
    B --> B2[Plans d'abonnement]
    A --> C[Métriques plateforme]
    C --> C1[Nombre de clients, usage, revenus]
    A --> D[Alertes techniques]
    D --> D1[Erreurs critiques, dépassement quotas]
    Note1[Aucun accès aux données commerciales clients — séparation stricte]
```

---

## 9. Règle de garde (frontend) dérivée de cette arborescence

| Élément UI | Condition d'affichage | Source de vérité backend |
|---|---|---|
| Section "Mes filiales" | `role == ADMIN_GROUPE` | `@PreAuthorize("hasRole('ADMIN_GROUPE')")` sur `GroupeController` |
| Section "Transferts" (déclencher) | `role == ADMIN_GROUPE` | idem, endpoint `POST /transferts` |
| Section "Transferts" (demander) | `role == ADMIN_FILIALE` | endpoint dédié à la demande (à confirmer en implémentation) |
| Bouton "Facture PDF" | `role in (COMMERCIAL, ADMIN_*)` | endpoint `GET /commandes-client/{id}/facture` |
| Menu "Utilisateurs" | `role in (ADMIN_GROUPE, ADMIN_FILIALE)` | `UtilisateurController` RBAC |
| Toute section catalogue/stock/tiers | `entreprise_id` de l'utilisateur connecté | filtre isolation multi-tenant (§8.6 GS-CDA-2026-01) — **jamais géré côté frontend seul** |

> ⚠️ **Rappel de sécurité :** cette arborescence organise l'expérience utilisateur — elle ne remplace **jamais** le contrôle d'accès serveur. Masquer un menu côté frontend sans `@PreAuthorize` correspondant côté backend est une faille, pas une fonctionnalité.

---

## 10. Prochaines étapes

- Faire valider cette arborescence par un designer avant production des maquettes (GS-DESIGN-BACKLOG-2026-01).
- Ajouter la navigation mobile si une interface responsive distincte (pas juste adaptive) est prévue — GS-CDA-2026-01 §1.4 signale l'accès mobile comme dominant au Cameroun.
