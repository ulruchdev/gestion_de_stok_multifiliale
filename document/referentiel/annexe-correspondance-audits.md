# Référentiel — Annexe : Correspondance des audits

### `GS-REF-2026-01 — Annexe A` | Consolidé le 7 septembre 2026

> **Règle de nomenclature :** l'identifiant **primaire** de toute anomalie est celui de `GS-AUDIT-2026-01` (format `X-nn`). Les autres numérotations (`docx`, `audit-09 M/I/R/S/Q`) sont des **alias** de lecture des pièces gelées. Quand un ticket, un diagramme ou un test cite une anomalie, il cite l'identifiant primaire et, si utile, la `DEC-nnn` qui la traite.

## 1. Correspondance anomalie → décision

| Primaire (GS-AUDIT) | Alias docx | Alias audit-09 | Décision(s) | Statut |
|---|---|---|---|---|
| A-01 | D-03, C-13 | — | `DEC-002` (catalogue groupe), `DEC-007` (multi-lignes, `group_id`) | ✅ |
| A-02 | E-07 | — | `DEC-002` (GRP-08 → P1 avant transfert) | ✅ |
| A-03 | D-01 | — | `DEC-002`/`DEC-006`/`DEC-007` (transfert à états + réception) | ✅ |
| A-04 | D-01 | — | `DEC-002` (machine `DEMANDE → VALIDE → EN_TRANSIT → RECU | ECART`) | ✅ |
| A-05 | F-03 | — | `DEC-018` (session de caisse, pas de rôle), `DEC-019` | ✅ |
| A-06 | — | — | `DEC-015` (maison mère = site opérationnel si stock) | ✅ |
| A-07 | E-07 | — | `DEC-015` (3 plans, limites, expiration, essai 90 j) | ✅ |
| A-08 | — | — | `DEC-015` (filiale désactivée incluse dans le consolidé) | ✅ |
| A-09 | A-06 | — | `DEC-006` (machine commande fournisseur) | ✅ |
| A-10 | H-03 | — | `DEC-013` (conditionnement), `DEC-031` (génération des codes) | ✅ |
| A-11 | B-05 | — | `DEC-031` (implémentation du compteur), vérification fiscale | ✅ |
| A-12 | C-13 | — | `DEC-007` (`group_id` sur le transfert) | ✅ |
| A-13 | A-04 | — | `DEC-004` (alertes à destinataires, levée automatique) | ✅ |
| A-14 | — | — | `DEC-031` (règle cache affichage/décision) | ✅ |
| B-01 | D-02, F-02 | — | `DEC-016` (compte inactif + `email_verifie` + renvoi, TTL 48 h) | ✅ |
| B-02 | C-09 | M1 (email) | `DEC-008` (unicité plateforme) | ✅ |
| B-03 | A-01, A-02, C-02 | M1 | `DEC-006`/`DEC-010` (mouvement compensatoire) + V5 | ✅ |
| B-04 | B-01 | — | `DEC-017` (409 partout) | ✅ |
| B-05 | C-01 | M1 | `DEC-003` (INTEGER XAF, `DECIMAL(12,3)`, arrondi) | ✅ |
| B-06 | A-10 | — | `DEC-029` (avertissement prix achat ≥ vente) | ✅ |
| B-07 | F-04 | M2 | `DEC-024` (Redis, TTL 1 h, usage unique) | ✅ |
| B-08 | C-03, C-04 | M3 | `DEC-004` (table refondue : destinataire, type extensible) | ✅ |
| B-09 | G-09 | — | `DEC-021` (UTC en base, Africa/Douala en métier) | ✅ |
| B-10 | G-08 | — | `DEC-021`, `DEC-031` (fuseau, seuils) | ✅ |
| B-11 | H-05 | — | `DEC-019` (appels synchrones par interface exposée) | ✅ |
| B-12 | F-01 | — | `DEC-031` (ordre des contrôles, anti-énumération) | ✅ |
| B-13 | C-06 | — | `DEC-022` (XAF figé, aucune colonne devise) | ✅ |
| B-14 | — | — | `DEC-021` (fuseau) | ✅ |
| B-15 | B-04, G-01, E, H | M6, M7, M8, I7–I11, S1–S5 | `DEC-031` (lot de stabilisation) | ✅ |
| C-01 | A-03, D-01 | — | `DEC-009` (paiements multiples + session caisse), `DEC-010` | ✅ |
| C-02 | E-01 | — | `DEC-011` (règlement B2B, échéance) | ✅ |
| C-03 | — | — | `DEC-006` (réception partielle) | ✅ |
| C-04 | — | — | `DEC-010` (remboursement en caisse, sans avoir) | ✅ |
| C-05 | X-12, A-06 | — | `DEC-006` (4 badge states, machines à états) | ✅ |
| C-06 | — | — | `DEC-012` (lots/péremption dès V5, FEFO en V1.5) | ✅ |
| C-07 | D-08, C-03 | — | `DEC-004`/`DEC-014` (in-app + e-mail, adaptateur) | ✅ |
| C-08 | D-05 | — | `DEC-013` (facteur de conditionnement) | ✅ |
| C-09 | — | — | `DEC-015` (plans) | ✅ |
| C-10 | — | — | `DEC-031` (alignement index GS-DATA `(entreprise_id, article_id)`) | ✅ |
| D-01 | — | — | `DEC-006`, `DEC-034` (VNT-04/multi-filiale retirés ou couverts) | ✅ |
| D-02 | — | Q-1…Q-10 | `DEC-030`…`DEC-041` (Lot 5) | ✅ |
| D-03 | — | — | `DEC-031` (A_JIRA/GIT, mélange main/master) | ✅ |

## 2. Correspondance audit technique (audit-09) → décision

| Réf audit-09 | Objet | Décision / traitement |
|---|---|---|
| I1, I2 (vente schéma) | V5 immédiate | `DEC-006`, `DEC-010`, `DEC-030` — migration V5 |
| I3 (token_reset DB morte) | colonnes mortes | `DEC-024` (Redis seul, colonnes supprimées au V5) |
| I4 (US-083 sans listener) | notification | `DEC-004` (module P0, critère réécrit) |
| I5 (RACI vs KICKOFF) | équipe | `DEC-031` (aligner RACI sur la réalité) |
| I6 (localStorage) | stockage tokens | `DEC-025` (access en mémoire, refresh httpOnly) |
| I7 (246 vs 237 SP) | totaux | `DEC-031` (somme vérifiable : 246) |
| I8 (statuts US) | journal unique | `DEC-031` (progress-ledger seul) |
| I9 (OWASP/SBOM) | contrôles annoncés | `DEC-031` (actualiser CDCT §28) |
| I10 (90 vs 109 tests, gate) | compteurs | `DEC-031` (109 (26+83), gate réel) |
| I11 (master inexistant) | branche | `DEC-026`, `DEC-031` (`main` seule) |
| I12 (secrets JWT) | démarrage | `DEC-031` (clés base64 ≥ 32 octets) |
| R1 (HEAD non compilable) | CI | `DEC-031` (commit `RedisHealthTracker` + tests) |
| R2 (secrets invalides) | auth cassée | `DEC-031` (S8) |
| R3 (change-password) | 500 Redis down | `DEC-039` |
| R4 (race rate limit) | sécurité | `DEC-031` (INCR+EXPIRE atomique) |
| R5 (format erreur RFC 7807) | contrat | `DEC-017`, `DEC-031` |
| R6 (session mono-appareil) | produit | `DEC-040` |
| S1–S5, S7 | mécaniques | `DEC-031` (checklist) |
| S6 | commit code GS-085 | `DEC-031` (P0 implémentation) |
| S8 | secrets | `DEC-031` (P0 implémentation) |
| Q-SCHEMA | V5 maintenant | résolue — V5 immédiate (Lot 5) |
| Q-CHANGE-PASSWORD | | `DEC-039` |
| Q-US083-CA | | `DEC-004` |
| Q-TOKENS-FRONTEND | | `DEC-025` |
| Q-NUMÉROS | | `DEC-031` |
| Q-STATUTS-DOCS | | `DEC-001`/`DEC-005` (référentiel = source, docs dérivés) |
| Q-GATE-COUVERTURE | | `DEC-031` (activer `jacoco:check` ≥ 80 % avec exclusions) |
| Q-EQUIPE | | `DEC-031` |
| Q-SESSION | | `DEC-040` |
| Q-IP | | `DEC-041` |

> **À l'implémentation :** quand un travail référence une anomalie, vérifier (1) la `DEC-nnn` correspondante, (2) la section du référentiel concernée. Les pièces gelées ne sont jamais une norme.