# RÉFÉRENTIEL — Partie 3 : Multi-tenant, rôles, isolation

### `GS-REF-2026-01 §3` | Version : 1.0 | Rédigée le 8 septembre 2026 | Statut : **Validée (DEC-002, DEC-008, DEC-015, DEC-016, DEC-018, DEC-019, DEC-021, DEC-022)**

## 3.1 Le modèle multi-tenant

```
tenant_group (le client SaaS, plan, limites)
   └── entreprise MERE (maison mère — siège, peut être opérationnelle)
         └── entreprise FILIALE × N (boutiques)
              └── utilisateurs (rattachés à une entreprise)
```

- **Isolation** : chaque `tenant_group` est isolé. `entreprise_id` est `NOT NULL` sur toute table métier (sauf `tenant_group` et `entreprise`). **`transfert_stock` porte `group_id`** (exception déclarée, `DEC-007`).
- **Identité** : `entreprise_id` et `group_id` proviennent **du jeton JWT** (claims), jamais du corps de requête (`DEC-019`). Un `filialeId` explicite peut être fourni par un utilisateur `GROUPE`, **validé** contre le `group_id` du jeton (contexte de filiale, `DEC-018`).
- **Parcours mono-boutique** : un client « entreprise unique » = un groupe à **1 seule entreprise** (`MERE`), qui est aussi la boutique. Le catalogue est au niveau groupe → identique au mono.
- **Parcours multi-filiale** : le groupe a une `MERE` + N `FILIALE`. Le catalogue est partagé (groupe) ; le stock, la caisse et les prix sont **par filiale**.

## 3.2 Les rôles (5 acteurs)

| Rôle | Portée | Droits essentiels | Notes |
|---|---|---|---|
| **SUPER_ADMIN** | plateforme | gestion des plans, support, jamais les données clients | `DEC-015` (seul à changer un plan) |
| **ADMIN_GROUPE** | groupe (lecture consolidée) | voit tout le groupe ; **écriture uniquement via contexte de filiale explicite** | `DEC-018` |
| **ADMIN_FILIALE** | sa filiale | tout sur sa filiale (catalogue, stock, caisse, employés) | |
| **GESTIONNAIRE_STOCK** | sa filiale | stock, inventaire (ouvre/compte), alertes | ne valide pas son propre inventaire (`DEC-036`) |
| **RESP_ACHATS** | sa filiale | commandes fournisseur | |
| **COMMERCIAL** | sa filiale | commandes client B2B ; **peut encaisser dans SA session** | `DEC-018` |
| **CAISSIER** | sa filiale | caisse (sa session) | |

**Règle caisse (`DEC-018`) :** *« Quiconque encaisse ouvre une session de caisse à son nom et en répond à la clôture. Quiconque a créé une vente peut l'annuler le jour même, dans sa propre session. »*

## 3.3 Isolation — règles à vérifier dans chaque US

1. **Lecture** : filtrer par `entreprise_id` (ou `group_id` pour les vues consolidées Admin Groupe) — jamais d'exception.
2. **Écriture** : `entreprise_id` depuis le jeton, **ou** `filialeId` explicite validé contre `group_id` du jeton (contexte de filiale).
3. **Transfert** : les deux filiales source/cible doivent appartenir au `group_id` du jeton ; contrainte en base (`CHECK`, `DEC-007`).
4. **Consolidé** : agrégation par `article_id` (unique au groupe, `DEC-002`) sur toutes les `entreprise_id` du groupe.
5. **Filiale désactivée** : connexion refusée (403) pour ses employés, **sauf** Admin Groupe (lecture seule) ; écritures refusées ; **stock inclus dans le consolidé** (`DEC-015`).
6. **E-mail utilisateur** : unique **au niveau plateforme** (`UNIQUE(email)`, `DEC-008`).

## 3.4 Comptes, plans et limites (DEC-015)

- 3 plans : **GRATUIT** (essai 90 j : 4 filiales, 10 utilisateurs, historique 30 j), **PRO** (15 filiales, 50 utilisateurs, historique illimité), **PERSONNALISE** (négocié).
- `limite_filiales` compte les **sites opérationnels** ; maison mère comptée si elle détient du stock.
- `limite_utilisateurs` compte les utilisateurs **actifs et non supprimés** du groupe, toutes filiales confondues ; contrôlée par `US-101`, jamais codée en dur.
- Un plan **expiré** (`date_expiration_plan` dépassée) retombe sur les limites de `GRATUIT` pour les deux compteurs.
- Fin d'essai → **lecture seule + export** (jamais de purge) ; expiration plan payant → 7 j de grâce puis lecture seule.
- Un seul essai par **NIF**.
- Changement de plan : **SUPER_ADMIN uniquement**, tracé.

## 3.5 Comptes utilisateurs (DEC-016)

- Compte **inactif à l'inscription** ; connexion refusée tant que l'e-mail n'est pas vérifié.
- **`email_verifie`** distinct de `actif` (deux codes d'erreur distincts).
- Jeton d'activation : Redis, usage unique, **TTL 48 h** ; endpoint « renvoyer le lien » obligatoire.
- E-mail envoyé **après commit** (asynchrone) → traçabilité de l'échec exigée.

---

*Sources : `DEC-002`, `DEC-007`, `DEC-008`, `DEC-015`, `DEC-016`, `DEC-018`, `DEC-019`, `DEC-021`, `DEC-022`, `DEC-036`.*