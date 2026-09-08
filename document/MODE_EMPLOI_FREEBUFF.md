# Freebuff — Configuration StockMaster CM

## Installation (une fois, par Ulrich)

```bash
cd gestion_de_stok_multifiliale
npm i -g freebuff
freebuff
/init          # génère .agents/types/ (déjà présent si tu as déjà lancé /init)
```

## ✅ État actuel (Juillet 2026) — Organisation terminée

Tous les fichiers Freebuff sont en place :

```
gestion_de_stok_multifiliale/
├── knowledge.md                              ← Source de vérité unique (Freebuff ET Claude Code via CLAUDE.md → @knowledge.md)
├── CLAUDE.md                                 ← Auto-chargé par Claude Code, importe knowledge.md (ne pas dupliquer son contenu)
├── .agents/
│   ├── types/agent-definition.ts             ← Type AgentDefinition créé
│   ├── session-bootstrap.ts                  ← Ancrage anti-hallucination
│   ├── task-architect.ts                     ← Planificateur de ticket (spawn session-bootstrap)
│   ├── spring-module-guardian.ts             ← Auditeur backend Spring
│   ├── react-frontend-guardian.ts            ← Auditeur frontend React
│   ├── doc-writer.ts                         ← Documentation duale (technique + métier)
│   └── git-committer.ts                      ← Commiteur conventionnel (jamais push auto)
├── document/
│   ├── HUMAN_CHANGELOG.md                    ← Journal métier créé
│   ├── MODE_EMPLOI_FREEBUFF.md               ← Ce fichier
│   └── 03-pilotage/progress-ledger.md        ← Suivi d'avancement créé
└── .gitignore                                ← S'assurer que .env y est
```

Le journal d'avancement unique est `document/03-pilotage/progress-ledger.md`. `document/implementation.md` a été **supprimé** le 7 septembre 2026 (consolidation `GS-REF`).

> **Historique :** `document/knowledge.md` a existé un temps en parallèle du `knowledge.md` racine (contenu divergent, risque de dérive). Il a été fusionné dans le fichier racine et supprimé — `knowledge.md` (racine) est désormais la seule copie, lue automatiquement par Freebuff (convention Codebuff) et par Claude Code (via `CLAUDE.md`).

### Pourquoi `session-bootstrap` et pas juste `knowledge.md`

Freebuff relit `knowledge.md` à chaque session — mais ce fichier ne contient que des **règles**, pas l'**état réel** du repo (quels modules ont du vrai code, quelles branches sont mergées, ce que dit vraiment `test_postman.md` aujourd'hui). Sans vérification active, l'agent peut halluciner un état basé sur le backlog ou une session précédente.

`session-bootstrap` force une vérification par lecture/commande réelle (`git log`, `ls` des modules, lecture du backlog et du ledger) à chaque nouvelle session, avant que `task-architect` ou les deux gardiens ne proposent quoi que ce soit. Les agents de travail le spawnent automatiquement.

**Workflow type :**
```
@task-architect US-XXX → implémentation → @spring-module-guardian → @doc-writer → @git-committer
```

**Remarque :** Les fichiers `.ts` originaux dans `document/` peuvent être supprimés — les versions actives sont dans `.agents/`.


---



## Workflow par personne

### Ulrich (backend + DevOps)
```
freebuff
> @task-architect US-014 (Modifier les informations du groupe)
  -> lit BACKLOG_StockMaster_CM.md + GS-RACI, propose le plan, attend ton "GO"
> [implémentation du plan validé]
> @spring-module-guardian audite les changements sur stockmaster-groupe
  -> lance mvn test, vérifie isolation multi-tenant, bloque si violation
> @doc-writer documente le changement
> @git-committer prépare le commit (jamais push automatique)
```

### Stephan (fullstack — bascule bientôt vers frontend)
- Tant qu'il fait du backend : même workflow qu'Ulrich, avec `@spring-module-guardian`.
- Dès qu'il bascule frontend (Sprint 1, voir `KICKOFF_StockMaster_CM.md`) : remplace `@spring-module-guardian` par `@react-frontend-guardian`, US au format `US-FXXX`.

### Siko (frontend + design)
- **Phase design (EPIC-D00 à D10) : n'utilise pas Freebuff.** Ce travail se fait dans Figma, hors du repo. `task-architect` ne s'applique qu'au code.
- **Dès qu'un écran démarre en code** (après validation Figma, gate G3) :
```
freebuff
> @task-architect US-F002 (design system technique — tokens Tailwind)
  -> vérifie que la maquette Figma correspondante est "Validée" avant de proposer un plan
> [implémentation]
> @react-frontend-guardian audite
> @doc-writer + @git-committer
```

---

## Règle non négociable (rappel des 3 documents de recherche fournis)

1. **Aucune donnée réelle** (client, `.env`, token) ne transite dans un prompt Freebuff — le contenu de `knowledge.md` ne contient que des règles et des schémas, jamais de secrets.
2. **`git-committer` ne pousse jamais automatiquement** (`git push`) et ne commit jamais sans confirmation humaine explicite affichée à l'écran.
3. **`MAX_RETRIES` si vous utilisez un script d'auto-correction** (voir `self-healing-build-loop-freebuff.md` fourni) : gardez un plafond strict (5-8 tentatives), sans quoi un agent peut boucler indéfiniment sur la même erreur et consommer tout le quota du jour.
4. Le fichier de conversation Gemini que tu as fourni contient des affirmations non vérifiées (nombre de sous-agents, extensions tierces, attaques nommées) — **ne les intègre pas dans `knowledge.md`**. Seuls les 3 documents de recherche réels (`self-healing-build-loop`, `master-prompt-init`, `strategie-freebuff-spring-boot`) ont servi de base à cette configuration.
