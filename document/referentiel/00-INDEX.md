# RÉFÉRENTIEL StockMaster CM — Index

### Référence : `GS-REF-2026-01` | Version : 0.2 (consolidée) | Ouvert le 17 août 2026 | Consolidé le 7 septembre 2026

---

## Ce qu'est ce référentiel

**Document maître unique** du projet StockMaster CM (décision `DEC-001`). Il est la **seule source normative** des règles du produit. Il remplace la dispersion des 41 fichiers de `document/`, dont l'audit croisé a relevé ≈ 96 contradictions uniques.

## Les deux règles qui le rendent tenable

**Règle 1 — Le référentiel ne contient que ce qui est normatif.**
Tout ce qui est dérivable du code n'y est **jamais recopié**, seulement référencé :

| Élément | Autorité réelle | Le référentiel |
|---|---|---|
| Schéma de données | migrations Flyway (`backend/stockmaster-shared/src/main/resources/db/migration/`) | en donne une vue lisible **annotée** |
| Contrats d'API | OpenAPI généré | y renvoie |
| Codes d'erreur | `ErrorCode.java` | y renvoie |
| Avancement des US | `document/03-pilotage/progress-ledger.md` | y renvoie |
| Périmètre / tickets | les 3 backlogs | y renvoie |
| Comptes de tests, totaux de SP | calculés | **ne les cite jamais** |

> Un chiffre écrit à deux endroits divergera. Le référentiel ne réplique pas : il pointe.

**Règle 2 — Une règle = un paragraphe numéroté et citable.**
Format de citation : `REF §4.3.2`. Les tickets, les diagrammes, les tests et les commentaires de code citent **la référence**, jamais le texte de la règle.

## Statut d'autorité

En cas de conflit entre ce référentiel et n'importe quel autre document de `document/` : **ce référentiel a raison**, sauf sur les éléments du tableau ci-dessus, où le code a raison — et où c'est alors le référentiel qui doit être corrigé.

---

## Les 14 parties

| # | Partie | Fichier | Absorbe | Statut |
|---|---|---|---|---|
| 1 | Vision, cible, périmètre V1/V2 | `01-vision-perimetre.md` | AF §1, KICKOFF §1 | 🔜 à rédiger |
| 2 | Glossaire normatif | `02-glossaire.md` | épars — **création** | 🔜 à rédiger |
| 3 | Multi-tenant, rôles, isolation | `03-multitenant-rbac.md` | AF §2.2, GS-IA §5 | 🔜 à rédiger |
| 4 | Règles de gestion par domaine | `04-regles-gestion.md` | AF §3, §7, §8, UC §5 | 🔜 à rédiger |
| 5 | Machines à états | `05-machines-etats.md` | épars — **création** | 🔜 à rédiger |
| 6 | Modèle de données (vue dérivée) | `06-modele-donnees.md` | GS-DATA, CDCT §23.3 | 🔜 à rédiger |
| 7 | Contrats d'API & erreurs (dérivé) | `07-api-erreurs.md` | test_postman | 🔜 à rédiger |
| 8 | Parcours & séquences | `08-parcours-sequences.md` | GS-SEQ, `diagrams/01→15`, PDF | 🔜 à rédiger |
| 9 | Architecture technique & ADR | `09-architecture-adr.md` | CDCT §22→30 | 🔜 à rédiger |
| 10 | Exploitation, sauvegarde, volumétrie | `10-exploitation.md` | néant — **création** | 🔜 à rédiger |
| 11 | Design system (pointeur) | `11-design-system.md` | DESIGN_TOKENS_REFERENCE | 🔜 à rédiger |
| 12 | **Journal des décisions** | `12-journal-decisions.md` | — | ✅ 40 décisions actives, **0 ouverte** (Lot 5, 7 sept. 2026) |
| 13 | Périmètre & planning (pointeurs) | `13-perimetre-planning.md` | GS-PLAN, KICKOFF §3-4 | 🔜 à rédiger |
| 14 | Workflow, CI, environnements | `14-workflow-ci.md` | A_JIRA_ET_GIT_FLOW, strategie_test, guideconfiguration, GS-RACI | 🔜 à rédiger |

---

## Pièces justificatives gelées

Ces documents ont produit le présent référentiel. Ils sont **gelés et ne sont plus maintenus** ; ils servent de **trace de l'état au moment de l'audit**, pas de norme :

- `document/03-pilotage/GS-AUDIT-2026-01_rapport_incoherences.md` — audit vertical par parcours (blocs A et B rédigés, 29 entrées) — gelé au 17 août 2026
- `document/03-pilotage/INCOHERENCES_DOCUMENTAIRES_StockMaster_CM.docx` — balayage horizontal du corpus (83 contradictions + 16 annexes) — gelé au 17 août 2026
- `document/03-pilotage/audit-analyse-2026-09.md` — audit technique du dépôt (code, CI, secrets, tests) — gelé au 7 septembre 2026

Recoupement : 28 paires communes entre les deux premiers ; l'audit technique apporte les points code/CI. Total de points uniques : ≈ 96.

**Table de correspondance** entre les trois audits et les décisions (nomenclature unique) : `document/referentiel/annexe-correspondance-audits.md`.
**Checklist de stabilisation** (points mécaniques `DEC-031`) : `document/03-pilotage/checklist-stabilisation-DEC-031.md`.
