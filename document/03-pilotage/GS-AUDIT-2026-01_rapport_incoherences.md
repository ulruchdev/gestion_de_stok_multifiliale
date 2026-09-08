# Rapport d'audit documentaire — StockMaster CM
### Référence : GS-AUDIT-2026-01 | Version : 1.0 | Date : 10 août 2026 | Statut : **🧊 Gelé au 17 août 2026 — pièce justificative, plus maintenue**
### Périmètre audité : intégralité du dossier `document/` (39 fichiers), confronté au code et au schéma Flyway réellement appliqués

> 🧊 **Document gelé — pièce justificative, pas une norme.**
> Il décrit l'état du corpus **au moment de l'audit** et propose des corrections dont plusieurs ont depuis été
> **tranchées autrement, voire rendues sans objet** par le journal des décisions. N'appliquez **aucune**
> recommandation de ce fichier sans la confronter à `document/referentiel/12-journal-decisions.md`
> (exemple : l'ajout de `ECART_STOCK_DETECTE`, ici recommandé, est **supprimé** par `DEC-037`).
> L'autorité est le référentiel `GS-REF-2026-01` (`document/referentiel/`).

---

## 0. Comment lire ce document en réunion

Ce rapport n'est **pas** une proposition de refonte. Son objectif est l'inverse : **stabiliser ce qui existe déjà** pour en faire une vision unique et exploitable. Rien de ce qui fonctionne n'est remis en cause — la section 8 liste explicitement ce qui est sain et ne doit **pas** être retouché.

Chaque anomalie est présentée dans un format constant :

| Champ | Contenu |
|---|---|
| **Gravité** | 🔴 bloquant V1 · 🟠 important · 🟡 à trancher avant le module concerné |
| **Constat** | Fait vérifié, avec citation du fichier et de la section. Aucune supposition. |
| **Analogie** | Traduction du problème en situation concrète, pour arbitrage non technique |
| **Conséquence si non traité** | Ce qui se produit réellement en production |
| **Options** | 2 à 4 résolutions réalistes, sans option de complaisance |
| **Recommandation** | Une seule option retenue, avec le motif |
| **Documents à corriger** | La liste exacte, section par section |

**Règle de travail appliquée :** aucune règle métier n'a été inventée pour combler un trou. Là où l'information manque, le rapport le dit et pose la question. Là où deux documents se contredisent, les deux sont cités.

**Ordre d'arbitrage conseillé :** décision structurante n°0 (section 3) → bloc A → B-01/B-02/C-07 → D-01 → le reste.

---

## 1. Synthèse exécutive

### 1.1 Le chiffre

**42 anomalies** confirmées, dont **19 bloquantes** pour une mise en production réelle.

| Bloc | Objet | Total | 🔴 | 🟠 | 🟡 |
|---|---|---|---|---|---|
| **A** | Multi-filiale — le cœur différenciant du produit | 14 | 9 | 5 | 0 |
| **B** | Contradictions entre documents | 15 | 5 | 10 | 0 |
| **C** | Trous fonctionnels pour un usage réel | 10 | 4 | 5 | 1 |
| **D** | Benchmark et mesure du succès | 3 | 1 | 2 | 0 |
| | **Total** | **42** | **19** | **22** | **1** |

### 1.2 Les cinq constats qui doivent sortir de cette réunion

**1 — Le transfert inter-filiales, présenté comme le différenciateur produit, n'est pas réalisable avec le modèle de données actuel.**
Le catalogue est isolé par filiale : le même sac de riz porte deux identifiants différents dans deux boutiques. Le bon de transfert n'a qu'une seule case « article ». Le cas d'usage UC-05 exige explicitement que l'article « existe dans les deux » filiales — ce que le schéma ne sait pas exprimer. *(A-01, A-02)*

**2 — L'Admin Groupe ne peut structurellement rien écrire dans une filiale.**
Son compte est rattaché à la maison mère, et la règle de sécurité impose de prendre l'identité d'entreprise dans le jeton, jamais dans la requête. Toute action qu'il croit faire « à Douala » est enregistrée au siège. Or six documents lui accordent ces droits. *(A-05)*

**3 — Le parcours d'entrée du client se referme sur lui-même.**
Le document d'autorité exige l'annulation de l'inscription si l'e-mail échoue ; le backlog exige exactement l'inverse. Combiné à l'activation par e-mail (priorité P1, donc après la mise en production) et à un module de notification vide (0 fichier de code), le résultat est qu'aujourd'hui **personne ne peut ni activer son compte ni récupérer un mot de passe oublié**. *(B-01, C-07)*

**4 — La caisse ne sait pas encaisser.**
Les diagrammes affichent « Espèces / Mobile Money / Carte », « Payé 60 000 », « Monnaie 3 200 ». Aucun de ces éléments n'existe dans une spécification, un modèle ou un backlog. La règle « annulation avant clôture de caisse » s'appuie sur un objet « clôture de caisse » qui n'a jamais été défini. *(C-01)*

**5 — Le produit est vendu à un segment qu'il ne peut pas servir.**
Les pharmacies sont citées comme cible dans le document fonctionnel et dans le premier exemple d'inscription. Il n'existe ni numéro de lot, ni date de péremption, ni traçabilité de lot — trois éléments réglementairement obligatoires pour le médicament. *(C-06)*

### 1.3 Ce que le projet a de solide

Il faut le dire aussi clairement, parce que le volume d'anomalies pourrait faire croire le contraire : **l'ossature du produit est bonne**. L'isolation multi-tenant est posée correctement et systématiquement. Le journal de mouvements immuable avec stock calculé à la volée est le bon choix d'architecture, documenté dans un ADR. Le durcissement de sécurité (rotation de jeton, détection de rejeu) est au niveau attendu du secteur. La stratégie de test et la chaîne d'intégration continue fonctionnent réellement.

**Les 42 anomalies ne sont pas des erreurs de conception : ce sont des écarts de synchronisation.** Le produit a évolué plus vite que ses documents. La correction est un travail de mise en cohérence, pas de reconstruction. Voir section 8.

### 1.4 Charge estimée de correction

| Nature | Charge | Bloquant pour |
|---|---|---|
| Corrections purement documentaires (aucun code) | ~3 jours | Rien — peut démarrer immédiatement |
| Décisions d'arbitrage métier (cette réunion) | 1 session | Tout le bloc A |
| Migration de schéma (catalogue groupe, transferts, alertes) | ~5 jours + tests | EPIC 5, 8, 11, 12 |
| Nouvelles spécifications (encaissement, réception partielle) | ~4 jours de rédaction | EPIC 7, 10 |
| Étude terrain (benchmark) | ~2 semaines calendaires | Grille tarifaire, priorisation |

---

## 2. Méthode et périmètre

### 2.1 Ce qui a été lu

| Catégorie | Fichiers | Traitement |
|---|---|---|
| `00-fonctionnel/` | 2 | Lus intégralement, **y compris le `.docx`** (GS-CDA-2026-01, 1 337 lignes extraites) |
| `01-architecture/` | 4 | Lus intégralement |
| `02-backlogs/` | 4 | Lus intégralement (dont le backlog backend, 2 007 lignes) |
| `03-pilotage/` | 6 | Lus intégralement |
| `diagrams/` | 15 | Lus intégralement |
| Racine `document/` | 4 | 3 lus ; `postman_collection.json` lu partiellement (doublon de `test_postman.md`) |
| **Non lu** | 1 | `representation_graphique_structure_et_flow.pdf` — format binaire graphique |

### 2.2 Confrontation au réel

Conformément au protocole anti-hallucination de `knowledge.md`, les documents ont été confrontés à l'état réel du dépôt, et non pris pour argent comptant :

- `git log` et `git status` (état des branches et des fusions réelles) ;
- lecture directe des migrations Flyway **réellement appliquées** : `V1__init_schema.sql` → `V4__add_entreprise_unique_indexes.sql` ;
- vérification de l'existence et du contenu des modules (`backend/stockmaster-*`) et du dossier `design/`.

**Plusieurs anomalies majeures n'apparaissent qu'à cette confrontation** : elles sont invisibles si l'on ne lit que les documents entre eux. C'est notamment le cas de A-01, B-03 et C-07.

### 2.3 Correspondance avec l'audit préliminaire

Les identifiants de ce rapport (A-01 à D-03) remplacent la numérotation Q1–Q42 de la restitution orale. Correspondance : `A-01 = Q1` … `A-14 = Q14`, `B-01 = Q15` … `B-15 = Q29`, `C-01 = Q30` … `C-10 = Q39`, `D-01 = Q40` … `D-03 = Q42`.

---

## 3. Décision structurante n°0 — la cause racine

> **Cette décision doit être prise en premier. Une trentaine d'anomalies du bloc B en découlent directement.**

### Le constat

Trois documents se déclarent chacun source de vérité sur le **même objet** — le modèle de données :

| Document | Auto-déclaration |
|---|---|
| `00-fonctionnel/…docx` §6 (GS-CDA-2026-01) | *« En cas de conflit, les Use Cases de la section 5 de l'Analyse Fonctionnelle font autorité »* (cité par le backlog) |
| `01-architecture/GS-DATA-2026-01` §Objet | *« Il devient la **source de vérité unique** pour le schéma logique (MLD) »* |
| `01-architecture/CDCT…Sections22-30` §23.3 | Contient les scripts SQL de création, présentés comme le contrat technique |

Et un quatrième acteur, non déclaré mais réellement contraignant : **le schéma Flyway effectivement appliqué en base**, qui diverge des trois.

`GS-DATA` tente une règle de départage — *« si ce diagramme diverge de `V1__init_schema.sql`, **ce fichier a tort** »* — mais l'applique de façon incohérente : il affirme dans le même document des colonnes (`vente.statut`, `vente.client_id`) qui n'existent pas dans le `V1` réel.

### L'analogie

> Un chantier avec **trois plans d'architecte différents**, chacun estampillé « plan de référence », plus un bâtiment déjà construit qui ne correspond exactement à aucun des trois. Le maçon qui arrive demain ne peut pas travailler : quel que soit le plan qu'il suive, il aura tort selon deux autres. Ce n'est pas un problème de compétence du maçon — c'est un problème de maîtrise d'ouvrage.

### La conséquence si rien n'est décidé

Chaque nouveau module (il en reste 9 sur 11) reproduira l'arbitrage au cas par cas, en fonction du document que le développeur a ouvert ce jour-là. Le nombre de contradictions croîtra mécaniquement avec le nombre de modules livrés.

### Les options

**Option A — Hiérarchie descendante stricte**
`GS-CDA` (métier, le « pourquoi ») > `GS-DATA` (modèle logique, le « quoi ») > `CDCT` (implémentation, le « comment ») > migrations Flyway (l'exécution). Toute divergence descendante est un bug à corriger vers le bas.

**Option B — Le code fait foi**
Les migrations Flyway sont la référence ; tous les documents sont des vues descriptives, mises à jour après coup.

**Option C — Séparation par nature**
`GS-CDA` fait autorité sur les **règles métier** (ce qui doit se passer). `GS-DATA` fait autorité sur le **modèle logique** (structure, cardinalités, contraintes). Le `CDCT` ne contient plus de SQL — il renvoie aux migrations, qui deviennent la seule expression physique du schéma. Un même fait n'est décrit qu'à un seul endroit.

### Recommandation : **Option C**

Les options A et B partagent le même défaut : elles maintiennent **la même information dupliquée à trois endroits**, ce qui garantit une nouvelle désynchronisation, quelle que soit la règle de priorité. On ne résout pas un problème de duplication en décidant quelle copie a raison.

L'option C supprime la duplication à la source. Concrètement :

- le bloc SQL du `CDCT §23.3` (plus de 300 lignes de `CREATE TABLE`) est **supprimé** et remplacé par un renvoi vers `backend/stockmaster-shared/src/main/resources/db/migration/` ;
- `GS-DATA-2026-01` conserve le diagramme entité-relation et les règles d'intégrité, et devient le document que toute migration doit mettre à jour **dans la même demande de fusion** — règle déjà écrite dans le document, mais jamais appliquée ;
- `GS-CDA §6` cesse de décrire les colonnes et les types, et se limite aux règles métier portant sur les entités.

**Coût :** environ une demi-journée. **Bénéfice :** supprime la cause racine de 15 anomalies du bloc B et empêche leur réapparition.

**Documents à corriger :**

| Document | Section | Action |
|---|---|---|
| `01-architecture/CDCT…Sections22-30.md` | §23.3 (scripts SQL complets) | **Supprimer** le SQL, remplacer par un renvoi au dossier de migrations |
| `01-architecture/GS-DATA-2026-01…md` | §Objet + §5 | Reformuler le périmètre : autorité sur le **modèle logique** uniquement |
| `00-fonctionnel/…docx` | §6.2 | Retirer les types et précisions techniques ; conserver les règles de gestion |
| `02-backlogs/BACKLOG…md` | Pied de page | Remplacer la mention d'autorité par la hiérarchie retenue |
| `03-pilotage/A_JIRA_ET_GIT_FLOW.md` | §Mise à jour des documents | Ajouter la règle : toute migration ⇒ mise à jour de `GS-DATA` dans la même PR |

---

## 4. BLOC A — MULTI-FILIALE

> Le multi-filiale est présenté comme le différenciateur central du produit (`GS-CDA §1.3` et `§1.4`). C'est aussi le domaine où la documentation est la moins exécutable. Neuf des quatorze anomalies de ce bloc sont bloquantes.

---

### A-01 🔴 — Le catalogue est isolé par filiale : le transfert inter-filiales n'est pas réalisable

**Prismes :** Dev (bloquant) · Terrain · Business

**Constat**

`GS-CDA-2026-01 §5.5 (UC-05)` pose en pré-condition du transfert : *« Deux filiales distinctes, **article existant dans les deux**, stock suffisant sur filiale source »*.

Or, dans le schéma réel (`V1__init_schema.sql`) :
- `article.entreprise_id` est `NOT NULL`, avec `UNIQUE (entreprise_id, code_article)` ;
- le même produit physique porte donc **deux identifiants distincts** dans deux filiales ;
- mais `transfert_stock` ne possède **qu'un seul `article_id`**.

Conséquence en chaîne : le mouvement `TRANSFERT_ENTREE` créé sur la filiale cible (`GS-SEQ §4`, `CDCT §26`, `diagrams/11`) porte `entreprise_id = cible` et `article_id` = l'article **de la source**. Cela viole frontalement l'invariant posé par `GS-DATA §4` et `GS-CDA §8.6`, et rend le stock de la cible incalculable : la requête d'agrégation `WHERE article_id = X AND entreprise_id = cible` porte sur un article inconnu du catalogue de la cible.

Le même défaut invalide `US-055` / `MVT-08` (stock consolidé), dont le critère d'acceptation est : *« Agrégation par `article_id` sur toutes les `entreprise_id` du groupe »*. Cette agrégation ne consolide rien si chaque filiale possède son propre `article_id` pour le même produit.

**Analogie**

> Deux boutiques d'un même patron tiennent chacune leur cahier de stock, avec leur propre numérotation. Le riz 50 kg est « l'article 12 » à Akwa et « l'article 47 » à Bassa. Le bon de transfert imprimé ne comporte qu'une seule case « n° article ». Le magasinier d'Akwa écrit « 12 » et charge le camion. À Bassa, on ouvre le cahier à l'article 12 : c'est de l'huile. La marchandise arrive, elle est enregistrée en huile. Les deux stocks sont désormais faux, et rien dans le système ne le signale.

**Conséquence si non traité**

EPIC 11 (transferts) est indéveloppable en l'état. Toute tentative produira soit une erreur de clé étrangère, soit — pire — un enregistrement silencieux d'un mouvement sur un article étranger à la filiale, c'est-à-dire une **fuite d'isolation multi-tenant à l'intérieur du groupe**. Le stock consolidé (US-055) affichera des lignes dupliquées, une par filiale, au lieu d'un total par produit.

**Options**

| | Description | Coût | Risque |
|---|---|---|---|
| **A** | **Catalogue au niveau groupe** : `article.group_id` et `categorie.group_id` ; le stock reste par filiale via `mouvement_stock.entreprise_id`. Un identifiant unique par produit dans tout le groupe. | Migration + reprise EPIC 5 | Faible — le stock reste isolé |
| **B** | Catalogue par filiale conservé ; `transfert_stock` porte `article_source_id` **et** `article_cible_id`, avec appariement manuel à maintenir | Faible en code | Élevé — charge de maintenance irréaliste pour une PME |
| **C** | Appariement implicite par `code_article` identique entre filiales, résolu au moment du transfert | Très faible | Élevé — échec silencieux dès qu'une filiale renomme un code |
| **D** | Retirer EPIC 11 et US-055 de la V1 | Nul | Perte du différenciateur annoncé |

**Recommandation : Option A**

C'est le modèle standard de tout progiciel multi-sites, et le seul qui rende le stock consolidé réellement calculable. Il supprime en outre la re-saisie intégrale du catalogue à chaque nouvelle filiale — friction d'accueil majeure pour un distributeur à cinq sites, et raison fréquente d'abandon en phase d'essai.

L'option B impose de tenir à jour une table d'appariement manuelle : sur un catalogue de 800 références et cinq filiales, c'est inapplicable. L'option C échoue dès qu'un responsable de boutique corrige un libellé, et l'échec est silencieux.

**Point d'attention :** l'option A a une conséquence à trancher séparément — les **prix** doivent-ils rester par filiale ? Voir A-02, option C.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` (GS-CDA-2026-01) | §6.2 « Article », §6.3 relations | `Article` et `Categorie` rattachés au **groupe**, plus à l'entreprise |
| `00-fonctionnel/…docx` | §5.5 UC-05 | Reformuler la pré-condition : article unique au groupe, stock vérifié sur la source |
| `00-fonctionnel/…docx` | §8.6 isolation | Préciser : catalogue isolé au **groupe**, données transactionnelles isolées à l'**entreprise** |
| `01-architecture/GS-DATA-2026-01…md` | §1 ERD, §3 index, §4 isolation | `ARTICLE` et `CATEGORIE` rattachés à `TENANT_GROUP` ; index `(group_id, code_article)` |
| `01-architecture/CDCT…md` | §23.3 (ou renvoi, cf. décision n°0) | Nouvelle migration `V5` |
| `02-backlogs/BACKLOG…md` | US-027, US-031, US-055, US-068 | Critères d'isolation réécrits ; US-055 recalculable |
| `diagrams/05-catalogue.md` | Schéma « Structure Categories et Articles » | `Entreprise → isole` devient `Groupe → isole` |
| `diagrams/13-modele-donnees-erd.md` | ERD complet | Idem |
| `diagrams/11-transferts-inter-filiales.md` | Les deux schémas | Un seul `article_id`, valide dans les deux filiales |
| `diagrams/03-groupe-filiales.md` | Structure multi-tenant | Ajouter le catalogue au niveau groupe |
| **Code** | `backend/…/db/migration/` | Migration `V5__catalogue_groupe.sql` + rollback |

---

### A-02 🔴 — Inversion de priorité : le transfert (P1) dépend du catalogue partagé (P2)

**Prismes :** Business (feuille de route) · Dev

**Constat**

`GS-CDA-2026-01 §3.2` classe :
- **GRP-08 « Catalogue partagé »** — *« Articles et catégories définis au niveau groupe, accessibles par toutes filiales »* — en **P2** ;
- **GRP-07 « Transfert de stock inter-filiales »** en **P1** ;
- `§3.9 MVT-07` (transfert) et **MVT-08** (stock consolidé) en **P1**.

`GS-IA-2026-01 §1` confirme en navigation : « Catalogue partagé - **P2** ». Le prérequis de trois fonctionnalités P1 est donc planifié en V2.

**Analogie**

> On a inscrit au planning la construction du tablier du pont pour le mois prochain, et celle des piles pour l'année prochaine. Chaque tâche est correctement estimée. L'ordre est impossible.

**Conséquence si non traité**

EPIC 11 sera abordé au Sprint 8 selon le planning, se heurtera à A-01, et provoquera soit un arrêt de sprint, soit un contournement bricolé (option C de A-01) qui produira des erreurs silencieuses en production.

**Options**

- **A** — Remonter GRP-08 en P0/P1, avant EPIC 11.
- **B** — Descendre GRP-07 / MVT-07 / MVT-08 en P2, en cohérence avec GRP-08.
- **C** — **Découpler** : le **référentiel** partagé (code, désignation, catégorie, taux de TVA) passe en P0 ; la **personnalisation des prix par filiale** reste en P2.

**Recommandation : Option C**

Ce qui est réellement optionnel en V1 n'est pas le catalogue partagé, c'est la **tarification différenciée par site**. Le référentiel commun est un prérequis dur. Le découplage débloque les transferts et le consolidé sans imposer une gestion tarifaire multi-sites dès la première version — et il correspond à la réalité d'un distributeur qui applique le plus souvent la même grille de prix sur tous ses points de vente au démarrage.

L'option B est la solution de repli honnête si le comité estime que le multi-filiale n'est pas prioritaire — mais elle doit alors être assumée dans le discours commercial, puisque `GS-CDA §1.3` en fait une promesse de valeur explicite.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §3.2 tableau | GRP-08 scindé : « référentiel groupe » **P0**, « prix par filiale » **P2** |
| `01-architecture/GS-IA-2026-01…md` | §1 arborescence Admin Groupe | Retirer la mention « - P2 » du catalogue partagé |
| `diagrams/15-navigation-frontend-par-role.md` | Arborescence Admin Groupe | Idem |
| `02-backlogs/BACKLOG…md` | EPIC 5 + Matrice de dépendances | EPIC 11 dépend explicitement du référentiel groupe |
| `03-pilotage/KICKOFF…md` | §4 planning | Replacer le référentiel groupe avant le Sprint 8 |

---

### A-03 🔴 — Le transfert est instantané : ni transit, ni réception, ni écart

**Prismes :** Terrain (bloquant) · Business · Dev

**Constat**

`transfert_stock` (schéma réel `V1`) ne comporte **qu'une seule date** (`date_transfert`) et **aucun statut**. `GS-SEQ §4`, `diagrams/11` et `CDCT §26` créent les deux mouvements — sortie source et entrée cible — **dans la même transaction**, présentée comme une qualité (« atomique, tout ou rien »).

Aucun mécanisme n'existe pour : refuser une réception, constater un écart à l'arrivée (casse, vol, quantité manquante), annuler un transfert déjà parti. `diagrams/03` (cycle de vie d'une filiale) ne mentionne aucun transfert en attente.

**Analogie**

> Au moment précis où le camion démarre de Douala, on raye les 10 sacs du cahier de Douala **et** on les inscrit au cahier de Yaoundé. Pendant les deux jours de route, Yaoundé compte dans son stock une marchandise que personne ne peut toucher — et peut donc la promettre à un client. Si le camion a un accident ou si trois sacs sont éventrés à l'arrivée, les deux cahiers sont faux et aucune ligne du système ne le dit.

**Conséquence si non traité**

Le stock des deux filiales est faux pendant toute la durée du transport, soit un à trois jours au Cameroun. C'est exactement la promesse `GS-CDA §1.3` — *« Transférer du stock entre sites en un clic avec traçabilité complète »* — qui se retourne : la traçabilité affichée est fausse, et l'utilisateur ne peut pas le savoir. Le premier client qui se voit promettre un article qui roule encore sur la nationale perdra confiance dans l'outil.

**Options**

| | Description |
|---|---|
| **A** | Transfert en **deux temps** : `EXPEDIE` (mouvement `TRANSFERT_SORTIE` sur la source) → `RECU` (mouvement `TRANSFERT_ENTREE` sur la cible, **avec la quantité réellement reçue**, saisie par la filiale cible). L'écart génère une correction motivée rattachée au transfert. |
| **B** | Instantané conservé ; l'écart est corrigé après coup par une correction manuelle sur la cible. |
| **C** | Instantané en V1, deux temps en V2. |

**Recommandation : Option A**

L'atomicité présentée comme une garantie technique est en réalité une **erreur de modélisation métier** : la transaction informatique est atomique, mais le fait physique ne l'est pas. Il faut modéliser le transit, pas le nier.

L'option B reporte l'écart sur une correction d'inventaire sans lien avec le transfert — ce qui reproduit exactement le défaut que la décision `GS-CDA-2026-02 §2` avait corrigé pour l'annulation de vente (ne pas confondre deux réalités métier dans le même type de mouvement). Ce serait revenir en arrière sur un arbitrage déjà rendu.

**Note de conception :** cette option se combine naturellement avec A-04 (demande de transfert). Une seule machine à états couvre les deux : `DEMANDE → APPROUVE → EXPEDIE → RECU`, avec `REFUSE` et `ANNULE` comme sorties.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §4.8 flow transfert | Réécrire en deux temps, avec saisie de la quantité reçue |
| `00-fonctionnel/…docx` | §5.5 UC-05 | Post-conditions et cas d'erreur : réception partielle, refus |
| `00-fonctionnel/…docx` | §7.2 machine à états | Ajouter la machine à états du transfert (absente aujourd'hui) |
| `01-architecture/GS-DATA-2026-01…md` | §1 ERD, §2 intégrité | `TRANSFERT_STOCK` : `statut`, `date_expedition`, `date_reception`, `quantite_recue` |
| `01-architecture/GS-SEQ-2026-01…md` | §4 | Séquence scindée en deux appels distincts |
| `01-architecture/CDCT…md` | §26 | Idem |
| `02-backlogs/BACKLOG…md` | US-068, + nouvelle US de réception | Réécriture des critères d'acceptation |
| `diagrams/11-transferts-inter-filiales.md` | Les deux schémas | Ajouter l'état de transit |
| `diagrams/03-groupe-filiales.md` | Cycle de vie | Mentionner les transferts en attente |
| `02-backlogs/GS-FRONTEND-BACKLOG…md` | US-F072, US-F073 | Écran de réception à ajouter |
| **Code** | Migration | Colonnes de statut et de réception sur `transfert_stock` |

---

### A-04 🔴 — La « demande de transfert » existe dans la navigation, nulle part ailleurs

**Prismes :** Business · Dev · Terrain

**Constat**

`GS-CDA-2026-01 §2.2.3` (Admin Filiale) : *« Peut **demander un transfert** de stock depuis une autre filiale (l'Admin Groupe valide) »*.
`GS-IA-2026-01 §2` et `diagrams/15` affichent l'entrée de menu « Demander un transfert — validation par Admin Groupe ».
`GS-IA §9` reconnaît le trou : *« endpoint dédié à la demande (**à confirmer en implémentation**) »*.

Face à cela : **aucune user story** dans le backlog (US-068 est réservée à `hasRole('ADMIN_GROUPE')`), **aucun point d'entrée d'API**, **aucun état `DEMANDE`**, **aucun champ demandeur** dans le modèle.

**Analogie**

> Le plan d'étage affiché à l'accueil indique une porte « Service Demandes » au fond du couloir. On y va : c'est un mur. La porte a été dessinée sur le plan, elle n'a jamais été percée.

**Conséquence si non traité**

Le responsable de la boutique en rupture — c'est-à-dire la seule personne qui **sait** qu'elle a besoin de marchandise — n'a aucun moyen de le signaler dans l'outil. Il téléphonera au siège. Le produit n'apporte alors rien sur son cas d'usage le plus fréquent, et le menu affiche une promesse non tenue à chaque connexion.

**Options**

- **A** — Retirer l'entrée du menu (`GS-IA §2`, `diagrams/15`) et la phrase de `GS-CDA §2.2.3`.
- **B** — Créer la user story : `DEMANDE → APPROUVE | REFUSE → EXPEDIE → RECU`, avec motif de refus obligatoire.
- **C** — Autoriser l'Admin Filiale à initier directement un transfert entrant, sans validation du groupe.

**Recommandation : Option B**, fusionnée avec A-03

Une seule machine à états couvre la demande et le transit. C'est le terrain qui commande le besoin, et le siège qui arbitre l'allocation — c'est exactement le fonctionnement réel d'un réseau de distribution.

**L'option C est à écarter formellement** : elle permettrait à une filiale de se servir dans le stock d'une autre sans arbitrage, ce qui est une source de conflit interne garantie et un contournement du rôle même de l'Admin Groupe.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `02-backlogs/BACKLOG…md` | EPIC 11 | **Nouvelle US** : demande de transfert (Admin Filiale) + approbation (Admin Groupe) |
| `00-fonctionnel/…docx` | §3.2 tableau | Nouvelle ligne GRP-09 « Demande de transfert », acteur Admin Filiale |
| `00-fonctionnel/…docx` | §5.5 UC-05 | Acteur principal : ajouter le déclencheur « demande » |
| `01-architecture/GS-IA-2026-01…md` | §9 tableau des gardes | Remplacer « à confirmer en implémentation » par le point d'entrée réel |
| `01-architecture/GS-DATA-2026-01…md` | §1 ERD | `TRANSFERT_STOCK` : `demandeur_id`, `approbateur_id`, `motif_refus` |
| `02-backlogs/GS-FRONTEND-BACKLOG…md` | EPIC-F07 | Écran de demande + écran d'approbation |
| `02-backlogs/GS-DESIGN-BACKLOG…md` | EPIC-D07 | Maquettes correspondantes |

---

### A-05 🔴 — L'Admin Groupe ne peut structurellement rien écrire dans une filiale

**Prismes :** Dev (bloquant) · Business · Terrain

**Constat**

Trois faits qui, combinés, rendent la situation impossible :

1. `utilisateur.entreprise_id` est `NOT NULL` (schéma réel) ; l'Admin Groupe est rattaché à la **maison mère**.
2. Le jeton d'authentification ne porte **qu'un seul `entrepriseId`** (`US-008`, `StockMasterPrincipal`, code réel).
3. `US-027`, `US-031` et `GS-CDA §8.6` imposent : *« `entreprise_id` extrait du jeton, **jamais** du corps de la requête »*.

Conclusion mécanique : si l'Admin Groupe crée un article, il le crée **sur la maison mère**, jamais sur une filiale.

Or `diagrams/04` (matrice des permissions) lui accorde F4 Catalogue, F5 Mouvements, F6 Corrections, F7 Commandes fournisseur, F8 Commandes client et **F9 Vente directe** ; et `US-027`, `US-031`, `US-044`, `US-056`, `US-064` l'incluent tous dans leurs `hasAnyRole`. `GS-CDA §7.3` dit seulement que l'Admin Groupe voit *« toutes les données de toutes les entreprises du groupe (filtre `group_id`) »* — la lecture est spécifiée, **l'écriture ne l'est pas**.

Aucun mécanisme de changement de contexte de filiale n'est décrit dans aucun document.

**Analogie**

> Le directeur général possède les clés des cinq boutiques. Mais son badge d'accès est programmé « Siège ». Chaque fois qu'il enregistre une vente en se tenant physiquement dans la boutique de Douala, le système l'inscrit au siège de Yaoundé. À la fin du mois, le siège affiche un chiffre d'affaires sans avoir jamais ouvert au public, et Douala n'a rien vendu.

**Conséquence si non traité**

Chaque écriture de l'Admin Groupe pollue les données de la maison mère et fausse le consolidé, sans erreur ni avertissement. Le défaut est invisible en test unitaire (où il n'y a qu'une entreprise) et n'apparaît qu'en production, sur des données déjà corrompues.

**Options**

| | Description |
|---|---|
| **A** | **Contexte explicite** : paramètre `filialeId`, **validé côté serveur contre le `group_id` du jeton**, obligatoire pour toute écriture d'un utilisateur de portée `GROUPE`, et journalisé |
| **B** | **Lecture consolidée seule** : retirer F4 à F9 de `diagrams/04` et de tous les `hasAnyRole` du backlog ; toute écriture passe par un Admin Filiale |
| **C** | Points d'entrée dédiés : `/groupe/filiales/{id}/articles`, `/groupe/filiales/{id}/stock`… |

**Recommandation : A et B combinés**

Lecture consolidée par défaut — c'est le besoin réel du dirigeant, et cela couvre 90 % de son usage. Écriture opérationnelle possible, mais **uniquement** via un contexte de filiale explicite, validé et tracé.

Cette combinaison ne viole pas la règle « jamais depuis le corps de la requête » : le `filialeId` n'est pas cru sur parole, il est vérifié comme appartenant au groupe du jeton. C'est la différence entre *faire confiance à une donnée client* et *autoriser une donnée client après vérification serveur* — la seconde est le fonctionnement normal de tout contrôle d'accès.

Bénéfice secondaire : l'audit reste lisible. On sait qui a écrit, et au nom de quelle filiale.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §7.3 visibilité par rôle | Distinguer explicitement **lecture** (groupe) et **écriture** (filiale, contexte explicite) |
| `00-fonctionnel/…docx` | §8.6 isolation | Ajouter la règle du contexte de filiale validé serveur |
| `00-fonctionnel/…docx` | §2.2.2 Admin Groupe | Préciser le périmètre d'écriture |
| `01-architecture/CDCT…md` | §28 (contrôle d'accès) | Documenter le mécanisme de contexte |
| `02-backlogs/BACKLOG…md` | US-027, 031, 044, 056, 064 + toutes les US avec `ADMIN_GROUPE` en écriture | Ajouter le paramètre de contexte aux critères |
| `diagrams/04-gestion-utilisateurs.md` | Matrice des permissions | Distinguer visuellement lecture et écriture pour R1 |
| `02-backlogs/GS-FRONTEND-BACKLOG…md` | US-F005, US-F007 | Sélecteur de filiale actif dans l'en-tête |
| `01-architecture/GS-IA-2026-01…md` | §1 et §9 | Ajouter le sélecteur de contexte à l'arborescence Admin Groupe |

---

### A-06 🟠 — Le statut opérationnel de la maison mère n'est pas défini

**Prismes :** Business · Terrain · Dev

**Constat**

`entreprise.type` vaut `MERE` ou `FILIALE`, et la maison mère porte un `entreprise_id` comme les autres : elle **peut** donc porter des articles, du stock et des ventes.

Mais les documents décrivent deux réalités différentes :
- inscription « entreprise unique » (`US-006`) : la maison mère **est** la boutique ;
- inscription « groupe » (`US-007`) et `diagrams/03` : la maison mère est un **siège** (« Holding, Siège Yaoundé »).

Non tranché : la maison mère d'un groupe détient-elle du stock ? Compte-t-elle dans la `limite_filiales` que contrôle `US-016` ? Le `code_filiale` lui est-il obligatoire ?

**Analogie**

> Sur l'organigramme, on ne sait pas si le bâtiment du siège contient aussi l'entrepôt. Selon la réponse, le comptable compte cinq sites ou six, et le gérant paie pour cinq licences ou six.

**Conséquence si non traité**

Le contrôle de `limite_filiales` (US-016) donnera un résultat différent selon le développeur qui l'implémente : un client au plan « 5 filiales » pourra en créer 4 ou 5. C'est un litige commercial direct.

**Options**

- **A** — La maison mère est une entité juridique sans stock ; seules les filiales opèrent.
- **B** — La maison mère est un site opérationnel comme les autres.
- **C** — Cela dépend du type d'inscription (unique = opérationnelle, groupe = siège), matérialisé par un **champ explicite**.

**Recommandation : Option C, avec un booléen `site_operationnel` explicite**

Les deux réalités existent effectivement sur le terrain : le grossiste dont le siège est aussi l'entrepôt principal, et la holding purement administrative. Il faut donc les deux.

En revanche, **déduire ce comportement de l'enum `type`** — qui sert déjà à exprimer la relation parent/enfant — est une source de bug garantie : un jour, quelqu'un créera une filiale non opérationnelle, ou une mère opérationnelle, et la déduction tombera. Un champ dédié coûte une colonne et supprime toute ambiguïté.

Règle associée à trancher : `limite_filiales` compte-t-elle les sites opérationnels ou les entités ? **Recommandation : compter les sites opérationnels** — c'est ce que le client comprend quand il lit « 5 points de vente ».

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §6.2 « Entreprise » | Ajouter `site_operationnel` et sa règle |
| `00-fonctionnel/…docx` | §3.2 GRP-03 | Préciser le décompte de la limite |
| `01-architecture/GS-DATA-2026-01…md` | §1 ERD | Ajouter le champ |
| `02-backlogs/BACKLOG…md` | US-006, US-007, US-015, US-016 | Définir la valeur à la création et la règle de décompte |
| `diagrams/03-groupe-filiales.md` | Structure multi-tenant | Clarifier le statut de la maison mère |
| **Code** | Migration | Colonne `site_operationnel` |

---

### A-07 🔴 — Les plans d'abonnement ne sont définis nulle part

**Prismes :** Business (modèle de revenu) · Terrain · Dev

**Constat**

`tenant_group` porte `plan_abonnement` (`GRATUIT | STARTER | PRO | ENTERPRISE`), `limite_filiales` et `date_expiration_plan`.
`US-015` retourne « le plan et la limite ». `US-016` refuse la création au-delà de la limite. `GS-CDA §2.2.1` confie la gestion des plans au Super Admin.

**Aucun document ne définit la grille** : ni les limites par plan, ni les prix, ni les fonctionnalités incluses.

Les valeurs réellement constatées sont d'ailleurs incohérentes entre elles :

| Source | Valeur |
|---|---|
| `V1__init_schema.sql` | `limite_filiales INTEGER NOT NULL DEFAULT 1` |
| Code réel (`AuthServiceImpl`, inscription groupe) | `limiteFiliales = 5` en dur, plan `GRATUIT` |
| `diagrams/03-groupe-filiales.md` | « Plan : PRO — Limite : 10 filiales » |

Manquent également : le flux de changement de plan, le paiement, et surtout **le comportement à l'expiration** — `date_expiration_plan` n'est lue par aucune user story. Lecture seule ? Blocage ? Période de grâce ?

**Analogie**

> Le restaurant affiche quatre formules au tableau — Gratuite, Starter, Pro, Enterprise — mais la carte des prix n'a jamais été imprimée, et personne en cuisine ne sait ce que contient chaque formule. Le serveur prend les commandes quand même. Le premier client qui demande « qu'est-ce que je gagne à passer en Pro ? » met tout le monde en défaut.

**Conséquence si non traité**

US-015 et US-016 sont en P0, Sprint 3 : elles seront codées avec des valeurs arbitraires qui deviendront de fait la grille commerciale. Et sans règle d'expiration, un client dont l'abonnement se termine gardera un accès complet indéfiniment — ou sera brutalement bloqué, selon l'humeur du code.

**Options**

- **A** — Définir la grille complète maintenant : limites, prix, fonctionnalités, règle d'expiration.
- **B** — V1 mono-plan, monétisation reportée en V2.
- **C** — Plans attribués manuellement par le Super Admin, sans paiement en ligne.

**Recommandation : B et C pour la V1**

Aucun paiement en ligne en V1 : intégrer Mobile Money en flux entrant B2B est un chantier entier (rapprochement, échecs, remboursements) qui n'a sa place ni dans le backlog ni dans le planning actuels.

**Mais la grille des limites doit être écrite dès maintenant**, car `US-015` et `US-016` la référencent déjà et sont en P0 Sprint 3. Il faut au minimum un tableau à quatre lignes : plan → nombre de filiales autorisé.

Sur l'expiration, une position claire est nécessaire même en V1 — **recommandation : lecture seule avec bandeau d'avertissement, jamais blocage de connexion**. Un client qui ne peut plus consulter son stock ne renouvelle pas : il part, et emporte ses données dans un cahier.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §2.2.1 Super Admin | **Nouveau tableau** : grille des plans (limites, expiration, comportement) |
| `00-fonctionnel/…docx` | §6.2 TenantGroup | Documenter la règle de `date_expiration_plan` |
| `02-backlogs/BACKLOG…md` | US-015, US-016 | Référencer la grille ; **nouvelle US** : comportement à expiration |
| `diagrams/03-groupe-filiales.md` | Bloc TenantGroup | Aligner l'exemple sur la grille réelle |
| **Code** | `AuthServiceImpl` | Retirer le `5` en dur, lire depuis la grille |
| `03-pilotage/GS-PLAN…md` | Phase 1 | Marquer la tarification comme dépendante de D-01 (benchmark) |

---

### A-08 🔴 — Une filiale désactivée ne bloque en réalité personne

**Prismes :** Terrain · Dev · Business

**Constat**

`GS-CDA §8.7` : *« Une filiale désactivée ne peut plus recevoir de nouvelles commandes ni mouvements de stock »*.
`US-019` : *« Filiale désactivée → plus aucun mouvement de stock ni commande accepté sur ce site »*.

Or le flux de connexion (`diagrams/02`, `US-008`, code réel) ne vérifie que **`utilisateur.actif`** et **`tenantGroup.actif`** — **jamais `entreprise.actif`**. Les employés d'une filiale suspendue se connectent donc normalement et travaillent normalement.

Non spécifié également : le sort des commandes `VALIDEE` non encore `LIVREE`, les transferts vers ou depuis une filiale inactive, et l'inclusion ou non de son stock dans le consolidé (`US-020`, `US-055`).

**Analogie**

> On baisse le rideau de la boutique et on met l'écriteau « Fermé ». Mais la porte de derrière reste ouverte et les badges de tous les employés fonctionnent encore. Ils continuent d'encaisser. Le patron, lui, croit le site fermé.

**Conséquence si non traité**

La fonctionnalité `US-019` ne produit aucun effet observable : c'est un interrupteur qui n'est branché à rien. Un Admin Groupe qui suspend un site pour cause de litige ou d'inventaire découvrira que l'activité a continué.

**Options**

| | Connexion | Écritures | Stock dans le consolidé |
|---|---|---|---|
| **A** | Refusée (403) pour tous sauf Admin Groupe | Refusées | **Exclu** |
| **B** | Autorisée, lecture seule | Refusées | **Inclus** |
| **C** | Autorisée | Refusées | Inclus |

**Recommandation : Option B**

Une fermeture temporaire — inventaire annuel, travaux, saison morte — ne doit pas empêcher le responsable de consulter son historique. Et surtout : **le stock physique existe toujours**. L'exclure du consolidé (option A) ferait mentir le pilotage du groupe et pourrait déclencher un réapprovisionnement inutile.

L'option C ne se distingue de B que par l'absence de mention explicite de la lecture seule — ce qui laisserait à nouveau la porte ouverte à une interprétation.

Ce point doit impérativement être tranché en réunion : **les trois options produisent des chiffres de tableau de bord différents**, et donc des décisions d'achat différentes.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §8.7 cohérence des états | Détailler : connexion, écritures, consolidé, commandes en cours, transferts |
| `02-backlogs/BACKLOG…md` | US-008 | Ajouter le contrôle `entreprise.actif` aux critères |
| `02-backlogs/BACKLOG…md` | US-019 | Détailler les effets réels |
| `02-backlogs/BACKLOG…md` | US-020, US-055 | Préciser l'inclusion du stock des filiales inactives |
| `diagrams/02-authentification-acces.md` | Flux de connexion | Ajouter le contrôle de la filiale |
| `diagrams/03-groupe-filiales.md` | Cycle de vie | Documenter les effets de la suspension |

---

### A-09 🟠 — La suppression d'une filiale est dessinée sans être spécifiée

**Prismes :** Terrain · Business · Dev

**Constat**

`diagrams/03` montre deux transitions : `Active → Supprimée` et `Suspendue → Supprimée`.
`GS-CDA §6.4` et `GS-DATA §2` posent : *« Entreprise (filiale) : ne peut être supprimée si elle contient des articles, des mouvements ou des commandes »*.

Mais **aucune user story** ne couvre la suppression : US-016 à US-019 traitent la création, la liste, la modification et l'activation/désactivation. Aucun point d'entrée d'API. Et rien ne dit ce que devient le **stock résiduel** d'un site fermé définitivement.

**Analogie**

> Fermer définitivement un magasin en y laissant la marchandise sur les étagères, puis perdre la clé. Sur le papier le magasin n'existe plus ; dans la rue, il est toujours plein.

**Options**

- **A** — Retirer la transition du diagramme : une filiale ne se supprime jamais, elle s'archive.
- **B** — Suppression possible **après transfert obligatoire du stock résiduel** vers une autre filiale.
- **C** — Suppression libre en suppression logique, stock résiduel gelé.

**Recommandation : Option B**

Une filiale ne ferme jamais avec un stock nul. L'option C ferait disparaître de la vue consolidée un stock physique qui existe encore — écart d'inventaire garanti à l'échelle du groupe, et perte sèche non expliquée dans les comptes.

L'option B a en outre l'avantage de forcer un geste métier sain : avant de fermer un site, on rapatrie la marchandise.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `02-backlogs/BACKLOG…md` | EPIC 3 | **Nouvelle US** : fermeture de filiale avec transfert du stock résiduel |
| `00-fonctionnel/…docx` | §3.2 | Nouvelle ligne, ou préciser que GRP-05 couvre la fermeture définitive |
| `00-fonctionnel/…docx` | §6.4 | Préciser la condition (stock nul après transfert) |
| `diagrams/03-groupe-filiales.md` | Cycle de vie | Ajouter la condition sur la transition « Supprimée » |
| `01-architecture/GS-DATA-2026-01…md` | §2 | Aligner la règle d'intégrité |

---

### A-10 🟠 — Les références de documents ne portent pas la filiale

**Prismes :** Terrain · Business · Dev

**Constat**

`CDCT §29` génère `CF-{ANNEE}-{SEQUENCE}`, `CC-{ANNEE}-{SEQUENCE}`, `VNT-{DATE}-{SEQUENCE}` avec une séquence **par entreprise**, et la contrainte d'unicité est `UNIQUE (entreprise_id, code)`.

Conséquence : au sein d'un même groupe, la filiale de Douala et celle de Yaoundé émettent chacune un bon `CF-2026-00042`.

Par ailleurs `code_filiale` est **nullable**, la maison mère créée à l'inscription n'en possède aucun (voir A-06), et la contrainte `UNIQUE (group_id, code_filiale)` tolère plusieurs valeurs nulles en PostgreSQL — l'unicité annoncée par `US-016` n'est donc pas garantie pour les entités sans code.

**Analogie**

> Deux bons de commande portant le numéro 42 circulent dans la même entreprise, parfois chez le même fournisseur. Au téléphone : « je vous appelle pour le 42 ». Lequel ? On ouvre les deux, on compare les lignes, on perd dix minutes — à chaque appel.

**Options**

- **A** — Intégrer le code filiale : `CF-DLA01-2026-00042`.
- **B** — Séquence au niveau **groupe** : unicité garantie, mais la filiale n'est pas lisible dans la référence.
- **C** — Statu quo : la filiale figure en clair sur le document imprimé, pas dans la référence.

**Recommandation : Option A**, et rendre `code_filiale` obligatoire (maison mère incluse)

La référence est ce qui est cité au téléphone, dans un e-mail ou en litige. Elle doit être non ambiguë **sans avoir le document sous les yeux**. C'est un coût de développement quasi nul pour une friction quotidienne évitée.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `01-architecture/CDCT…md` | §29 génération des codes | Nouveau format incluant le code filiale |
| `00-fonctionnel/…docx` | §6.2 « Entreprise » | `code_filiale` obligatoire |
| `01-architecture/GS-DATA-2026-01…md` | §1 ERD | Idem |
| `02-backlogs/BACKLOG…md` | US-016, US-044, US-056, US-064, US-068 | Format de code mis à jour |
| `diagrams/07`, `diagrams/08`, `diagrams/10`, `diagrams/11` | Exemples de codes | Aligner les exemples |
| **Code** | Migration | `code_filiale NOT NULL` + valeur pour les mères existantes |

---

### A-11 🔴 — La génération des références par comptage produit collisions et doublons

**Prismes :** Dev · Terrain (bloquant en caisse) · Business (numérotation fiscale)

**Constat**

`CDCT §29` génère les références ainsi :

```java
long sequence = venteRepository.countByEntrepriseIdAndDate(entrepriseId, LocalDate.now()) + 1;
return String.format("VNT-%s-%04d", date, sequence);
```

Deux défauts distincts :

1. **Collision en concurrence.** Deux caissiers encaissant dans la même seconde obtiennent le même `VNT-20260610-0023` ⇒ violation de `uq_vente_code` ⇒ **erreur 500 au comptoir, client devant**.
2. **Réutilisation après suppression.** La suppression étant logique (`GS-CDA §8.3`), le comptage décroît après une suppression ⇒ **réémission d'une référence déjà utilisée**.

**Analogie**

> Deux caissiers partagent un seul carnet à souche et arrachent chacun un feuillet au même instant : les deux tickets portent le numéro 23. Et quand on retire un feuillet du carnet, le suivant reprend le numéro du retiré.

**Conséquence si non traité**

En caisse, c'est le scénario le plus visible et le plus destructeur pour la confiance : une erreur technique brute, en pleine transaction, avec un client qui attend. Et deux ventes distinctes portant la même référence rendent tout rapprochement comptable impossible.

**Options**

- **A** — Séquence PostgreSQL dédiée par (entreprise, année).
- **B** — Table de compteurs avec verrou pessimiste dans la même transaction.
- **C** — Nouvelle tentative applicative sur violation d'unicité.

**Recommandation : Option B**

La séquence PostgreSQL (A) est atomique mais **crée des trous** en cas d'annulation de transaction. Si la numérotation des ventes doit être continue au sens fiscal, c'est disqualifiant.

Un compteur verrouillé dans la même transaction donne une numérotation **continue et sans doublon**, ce qui est l'exigence réelle. L'option C traite le symptôme et échoue sous forte concurrence.

> **⚠️ Point réglementaire à faire trancher par un comptable :** la numérotation des ventes doit-elle être **continue sans trou** au sens de l'administration fiscale camerounaise ? Ce rapport ne peut pas l'affirmer. Si la réponse est non, l'option A suffit et coûte moins cher.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `01-architecture/CDCT…md` | §29 | Remplacer intégralement le mécanisme de comptage |
| `00-fonctionnel/…docx` | §8.5 validation | Ajouter la règle de continuité de la numérotation (selon arbitrage fiscal) |
| `02-backlogs/BACKLOG…md` | US-044, US-056, US-064, US-068 | Critère : unicité garantie en concurrence |
| `03-pilotage/strategie_test.md` | §6.4 | Ajouter un cas de test de concurrence sur la génération de codes |

---

### A-12 🟠 — La table des transferts n'est rattachée à aucun groupe

**Prismes :** Dev · Sécurité

**Constat**

`transfert_stock` (schéma réel) ne porte **ni `entreprise_id` ni `group_id`**. L'isolation ne repose que sur les clés étrangères source et cible, contrôlées **en service uniquement** (`US-068` : *« Les deux filiales appartiennent au même groupe → sinon 403 »*), jamais en base.

C'est la **seule table métier hors du pattern** `entreprise_id NOT NULL` posé par `GS-DATA §4`.

Conséquence directe et vérifiable : `CDCT §29` appelle `transfertRepository.countByGroupIdAndAnnee(groupId, …)` sur une **colonne qui n'existe pas**.

**Analogie**

> Le registre des transferts de marchandises ne mentionne pas à quelle entreprise il appartient. Tant que le gardien vérifie bien à l'entrée, tout va bien. Le jour où le gardien s'absente — une ligne de code oubliée dans un nouveau module — n'importe qui peut y écrire.

**Conséquence si non traité**

C'est la seule table où une erreur de code peut déplacer du stock **entre deux entreprises clientes distinctes** : la faille multi-tenant la plus grave possible sur un produit SaaS. Un incident de ce type est un incident contractuel, pas un bug.

**Options**

- **A** — Ajouter `group_id NOT NULL` + contrainte de vérification que source et cible appartiennent à ce groupe.
- **B** — Contrôle en service seul (statu quo) et corriger `CDCT §29`.
- **C** — Dériver le groupe par jointure à chaque requête.

**Recommandation : Option A**

Le niveau de garantie doit être proportionné au niveau de risque. Ici, le risque maximal est une fuite inter-clients : il justifie une contrainte en base, pas seulement une vérification applicative qu'un futur module pourrait contourner.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `01-architecture/GS-DATA-2026-01…md` | §1 ERD, §4 isolation | Ajouter `group_id` à `TRANSFERT_STOCK` |
| `01-architecture/CDCT…md` | §29 | Corriger l'appel (colonne inexistante) |
| `00-fonctionnel/…docx` | §6.2 / §8.6 | Rattacher explicitement le transfert au groupe |
| `02-backlogs/BACKLOG…md` | US-068 | Contrainte en base mentionnée dans les critères |
| **Code** | Migration | `group_id NOT NULL` + `CHECK` de cohérence |

---

### A-13 🟠 — Un transfert qui vide une filiale ne déclenche aucune alerte

**Prismes :** Terrain · Business · Dev

**Constat**

`GS-CDA §7.4` : *« Cette vérification est effectuée après chaque mouvement de type **SORTIE ou CORRECTION_NEG** »*. `TRANSFERT_SORTIE` n'y figure pas — alors que c'est précisément le mouvement qui vide la filiale source.

Trois formulations concurrentes coexistent :

| Source | Déclencheur |
|---|---|
| `GS-CDA §7.4` | `SORTIE` ou `CORRECTION_NEG` uniquement |
| `US-071` et `GS-SEQ §5` | Tout `StockUpdatedEvent` |
| `diagrams/12` | « Après tout mouvement modifiant le stock » |

**Analogie**

> L'alarme de niveau bas de la citerne se déclenche quand on sert un client au robinet, mais reste muette quand on vide la moitié de la citerne dans un camion-citerne. Le seul moment où l'on retire vraiment beaucoup, c'est celui où l'alarme ne sonne pas.

**Point aggravant identifié**

Aucun document ne dit **quand une alerte cesse d'être active**. `US-073` ne propose qu'un « marquer comme lue » manuel. Un article réapprovisionné conservera donc son alerte de rupture affichée jusqu'à intervention humaine.

**Options**

- **A** — Alerte sur tout mouvement décrémentant (`SORTIE`, `CORRECTION_NEG`, `TRANSFERT_SORTIE`).
- **B** — Maintenir `§7.4` à la lettre.
- **C** — Alerte sur **tout** mouvement, y compris entrants — ce qui permet la **levée automatique** de l'alerte.

**Recommandation : Option C**

Elle corrige les deux problèmes d'un coup : le déclenchement manquant sur transfert, et l'absence de mécanisme de fin de vie de l'alerte. Un centre d'alertes qui ne se vide que manuellement est abandonné par ses utilisateurs en quelques semaines.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §7.4 alertes de stock | Élargir à tout mouvement + définir la levée automatique |
| `00-fonctionnel/…docx` | §3.9 MVT-05 | Aligner (voir aussi B-10 sur le comparateur) |
| `02-backlogs/BACKLOG…md` | US-071, US-072, US-073 | Ajouter la levée automatique |
| `01-architecture/GS-SEQ-2026-01…md` | §5 | Ajouter la branche de levée |
| `diagrams/12-notifications-alertes.md` | Flux d'alerte | Idem |
| `diagrams/08-gestion-stock.md` | Calcul et alerte | Idem |

---

### A-14 🟠 — Le consolidé groupe est mis en cache sans invalidation

**Prismes :** Terrain · Dev · Business

**Constat**

| Source | Durée de cache |
|---|---|
| `US-020` (dashboard groupe) | Redis, 5 minutes |
| `US-055` (stock consolidé) | Redis, 2 minutes |
| `diagrams/03` | 5 minutes |

Aucune de ces user stories ne prévoit d'invalidation sur mouvement de stock — contrairement au catalogue (`US-028`, `US-031`), qui possède bien un `@CacheEvict`.

**Analogie**

> Le tableau d'affichage du stock à l'entrée du siège est rafraîchi toutes les cinq minutes. Le directeur le lit, décide d'expédier 20 sacs de Douala vers Yaoundé, et signe le bon. Pendant ces cinq minutes, Douala en a vendu 15. Le transfert part sur une information périmée.

**Conséquence si non traité**

Une décision **irréversible** (le transfert) est prise sur une donnée pouvant avoir jusqu'à cinq minutes de retard, dans un contexte où la filiale source vend en continu.

**Options**

- **A** — Invalidation du cache groupe sur chaque `StockUpdatedEvent`.
- **B** — Cache conservé pour l'affichage, mais **relecture non mise en cache obligatoire** au moment de la validation du transfert.
- **C** — Aligner les deux durées et **afficher l'horodatage de fraîcheur** de la donnée.

**Recommandation : B et C**

L'option A annule l'intérêt du cache dans un groupe actif : l'invalidation serait quasi permanente, et le cache deviendrait une charge sans bénéfice.

Le principe à retenir est plus simple : **une donnée affichée peut être approximative si sa fraîcheur est visible ; une donnée qui déclenche une écriture ne doit jamais venir du cache.**

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `02-backlogs/BACKLOG…md` | US-020, US-055 | Aligner les durées + afficher la fraîcheur |
| `02-backlogs/BACKLOG…md` | US-068 | Relecture non mise en cache avant validation du transfert |
| `01-architecture/CDCT…md` | Section cache | Poser la règle générale affichage/décision |
| `diagrams/03-groupe-filiales.md` | Dashboard consolidé | Aligner la durée affichée |
| `02-backlogs/GS-FRONTEND-BACKLOG…md` | US-F020, US-F072 | Afficher l'horodatage de la donnée |

---

## 5. BLOC B — CONTRADICTIONS ENTRE DOCUMENTS

> Ce bloc ne contient **aucune opinion**. Chaque ligne oppose deux ou plusieurs documents du projet qui affirment des choses incompatibles sur le même sujet. La décision structurante n°0 (section 3) supprime la cause racine de la plupart d'entre elles ; les arbitrages ci-dessous restent nécessaires pour trancher le contenu.

---

### B-01 🔴 — Inscription : rollback ou non si l'e-mail échoue — et impasse d'activation

**Prismes :** Business (bloquant, impasse d'acquisition) · Terrain · Dev

**Constat**

Deux règles frontalement opposées, dont l'une émane du document déclaré prioritaire :

| Source | Règle |
|---|---|
| `GS-CDA-2026-01 §5.1 (UC-01)`, « Règle métier » | *« La création est atomique : **si l'email échoue, la création BDD est rollbackée** »* |
| `US-006`, critère d'acceptation | *« L'envoi de l'email **ne bloque pas** la transaction d'inscription — l'utilisateur est créé même si l'email échoue (comportement volontaire) »* |

En combinant `GS-CDA §3.1 AUTH-07` (validation du compte par e-mail), `§4.1` étape 8 (*« Son compte est activé »*) et `US-008` (403 `ACCOUNT_DISABLED` si `actif = false`) : **si le compte est créé inactif et que l'e-mail échoue, l'utilisateur ne peut jamais se connecter et n'a aucun recours** — aucun point d'entrée de renvoi d'e-mail d'activation n'existe dans le backlog.

Aggravant : `AUTH-01` (inscription) est en **P0**, `AUTH-07` (activation) en **P1**. À la fin du périmètre MVP, on peut donc s'inscrire mais pas activer.

**Analogie**

> Le client pousse la porte de l'agence, remplit le formulaire, et la porte se referme derrière lui — verrouillée. La clé lui sera apportée par un coursier dont on a écrit noir sur blanc qu'il peut ne jamais arriver. Et il n'y a pas de sonnette pour rappeler le coursier.

**Conséquence si non traité**

Tout échec d'envoi — adresse mal saisie, boîte pleine, filtre anti-spam, panne SMTP — produit un compte définitivement inutilisable et un client perdu, sans aucune trace exploitable par le support. C'est la fuite la plus coûteuse possible : elle se produit à l'instant exact où le client a décidé d'essayer le produit.

**Options**

| | Description |
|---|---|
| **A** | Compte créé **actif** ; e-mail de bienvenue purement informatif ; vérification d'adresse différée et non bloquante (bandeau dans l'interface) |
| **B** | Compte inactif + activation obligatoire, avec `AUTH-07` remonté en P0, un point d'entrée de renvoi d'e-mail, et rollback si l'envoi échoue |
| **C** | Compte actif avec fonctionnalités limitées jusqu'à vérification |

**Recommandation : Option A**

`US-006` promet explicitement *« créer mon espace StockMaster en moins de 2 minutes sans configuration complexe »*. Dans un contexte où la délivrabilité e-mail est incertaine, une activation bloquante détruit le taux d'activation — qui est précisément le premier indicateur de succès proposé par `GS-PLAN` (voir D-02).

L'option B est défendable sur le plan de la sécurité, mais elle coûte trois développements supplémentaires (renvoi, expiration, relance) pour un bénéfice faible sur un produit B2B où l'adresse est de toute façon vérifiée au premier mot de passe oublié.

**Corollaire obligatoire :** la règle métier d'`UC-01` doit être corrigée dans le `.docx`. En l'état, c'est elle qui fait autorité, et elle contredit le code déjà livré.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §5.1 UC-01, « Règle métier » | **Supprimer** la clause de rollback sur échec d'e-mail |
| `00-fonctionnel/…docx` | §4.1 flow d'inscription, étapes 6-9 | Retirer l'étape d'activation bloquante |
| `00-fonctionnel/…docx` | §3.1 AUTH-07 | Reclasser en « vérification d'adresse », non bloquante |
| `02-backlogs/BACKLOG…md` | US-006, US-007 | Préciser : compte créé **actif** |
| `02-backlogs/BACKLOG…md` | US-074, US-075 | Aligner sur la vérification non bloquante |
| `diagrams/02-authentification-acces.md` | Flux d'inscription | Retirer l'étape d'activation du chemin critique |
| `01-architecture/GS-SEQ-2026-01…md` | §1 | Note QA à corriger (elle affirme le comportement inverse) |
| `01-architecture/GS-IA-2026-01…md` | §7 espace public | « Activation de compte » devient optionnelle |

---

### B-02 🔴 — Unicité de l'e-mail utilisateur : trois règles incompatibles

**Prismes :** Business · Terrain · Dev · Sécurité

**Constat**

| Source | Règle |
|---|---|
| `GS-CDA §6.2` « Utilisateur » (**autorité**) | *« email : **unique au niveau de l'entreprise** »* |
| `GS-DATA §3` index critiques | `UNIQUE (entreprise_id, email)` |
| `US-021`, `US-082` **et le schéma réel** (`V1` : `CONSTRAINT uq_utilisateur_email UNIQUE (email)`) | Unique **au niveau plateforme** |

Impact concret sur le multi-filiale : avec la règle plateforme, un même employé ne peut pas disposer d'un compte dans deux filiales du même groupe — cas réel du gestionnaire itinérant ou du comptable partagé. Et deux entreprises clientes sans aucun lien ne peuvent pas partager une adresse, alors que le partage d'une adresse de boutique est fréquent sur le marché cible.

**Analogie**

> Le règlement intérieur dit que le badge est unique par boutique. Le lecteur de badge à l'entrée, lui, refuse tout badge déjà enregistré n'importe où dans le pays. Le comptable qui travaille deux jours à Douala et trois à Yaoundé ne peut avoir qu'un seul badge, donc qu'une seule boutique.

**Options**

- **A** — Unicité plateforme (statu quo du code) ⇒ corriger `GS-CDA §6.2` et `GS-DATA §3`.
- **B** — Unicité par entreprise ⇒ migration, **et** le formulaire de connexion doit alors demander un identifiant de tenant.
- **C** — Unicité plateforme + notion de compte rattaché à plusieurs filiales.

**Recommandation : Option A**

L'option B casse la connexion par e-mail seul (`US-008`), qui est le socle de tout le frontend déjà livré et testé — elle imposerait un champ supplémentaire sur l'écran de connexion, dégradant l'expérience pour tous afin de servir un cas minoritaire.

L'option C est la bonne cible fonctionnelle à terme, mais c'est une refonte du modèle d'identité (un utilisateur ↔ N rattachements) : à poser en V2, dans un lot dédié, pas maintenant.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §6.2 « Utilisateur » | Corriger : unique **au niveau plateforme** |
| `01-architecture/GS-DATA-2026-01…md` | §3 index critiques | Corriger l'index en `UNIQUE (email)` |
| `02-backlogs/BACKLOG…md` | US-021, US-022 | Déjà correct — aucune action |
| `02-backlogs/GS-FRONTEND-BACKLOG…md` | US-F081 | Message d'erreur explicite sur doublon inter-groupes |

---

### B-03 🔴 — `ANNULATION_VENTE` : signe inversé, formule fausse, type absent du schéma

**Prismes :** Dev (bloquant) · Business (stock faux ⇒ ventes perdues)

**Constat**

Quatre défauts distincts sur la même décision (`GS-CDA-2026-02 §2`) :

| # | Défaut | Source |
|---|---|---|
| 1 | `diagrams/08` place `ANNULATION_VENTE` **en soustraction** (`- ANNULATION_VENTE`), alors que l'addendum, `US-067`, `GS-DATA` et `diagrams/10` en font une **entrée compensatoire** (addition) | `diagrams/08` vs 4 autres documents |
| 2 | La vue matérialisée `vue_stock_reel` **omet `ANNULATION_VENTE` des deux sommes** | `CDCT §23.3` |
| 3 | Le `CHECK` sur `origine_type` n'accepte pas `'ANNULATION_VENTE'`, alors que `US-067` l'exige comme valeur | `CDCT §23.3` **et `V1` réel** |
| 4 | Le `V1` réellement appliqué ne contient ni `ANNULATION_VENTE` dans `type_mouvement`, ni `vente.statut`, ni `vente.client_id` — il a encore `annulee BOOLEAN` | Schéma réel vérifié |

Le `CDCT` signale le point 4 en commentaire ; `GS-DATA` affirme l'inverse tout en écrivant *« si ce diagramme diverge du Flyway réel, ce fichier a tort »*.

**Analogie**

> L'écriture de contrepassation est passée au débit au lieu du crédit. Annuler une vente ne remet pas la marchandise en stock : **elle la retire une seconde fois**. Le stock diminue deux fois pour une vente qui n'a jamais eu lieu.

**Conséquence si non traité**

Défaut 1 appliqué tel quel : chaque annulation crée un écart de stock du double de la quantité vendue. Défaut 2 : toute lecture passant par la vue matérialisée est fausse dès la première annulation. Défauts 3 et 4 : le module Vente ne peut simplement pas démarrer.

**Options**

- **A** — Corriger les quatre points ; `ANNULATION_VENTE` est un mouvement **entrant**.
- **B** — Abandonner le type dédié et réutiliser `CORRECTION_POS` — ce qui revient sur la décision de l'addendum.
- **C** — Retirer `vue_stock_reel` du `CDCT` (vue non maintenue contenant une formule fausse).

**Recommandation : A + C**

La décision de l'addendum est saine : distinguer « écart d'inventaire » de « vente annulée » est un vrai besoin d'audit, et l'option B effacerait cet acquis.

L'option C s'ajoute : `vue_stock_reel` est marquée « V2, non utilisée » **tout en étant créée par la migration `V3`**. Une vue non maintenue contenant une formule fausse finira par être requêtée un jour, par quelqu'un qui ne saura pas qu'elle est obsolète. La retirer coûte moins cher que la maintenir « au cas où ».

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `diagrams/08-gestion-stock.md` | Calcul du stock réel | **Inverser le signe** : `+ ANNULATION_VENTE` |
| `01-architecture/CDCT…md` | §23.3 `vue_stock_reel` | **Supprimer** la vue (ou l'inclure correctement) |
| `01-architecture/CDCT…md` | §23.3 `CHECK origine_type` | Ajouter `'ANNULATION_VENTE'` |
| `00-fonctionnel/…docx` | §7.1 formule du stock réel | Ajouter `ANNULATION_VENTE` en entrée |
| `00-fonctionnel/…docx` | §6.2 `MouvementStock` | Ajouter le type à l'énumération |
| `01-architecture/GS-DATA-2026-01…md` | §1 ERD | Aligner (déjà partiellement correct) |
| `02-backlogs/BACKLOG…md` | US-051 | Formule du stock réel à compléter |
| **Code** | Migration `V5` | `ANNULATION_VENTE`, `vente.statut`, `vente.client_id`, suppression de `annulee` |

---

### B-04 🟠 — Code HTTP « stock insuffisant » : 409 contre 422

**Prismes :** Dev (contrat d'API) · Business (reprise frontend)

**Constat**

| Code | Documents |
|---|---|
| **409** | `US-060`, `US-068`, `CDCT §25`, `CDCT §26`, `implementation.md` |
| **422** | `GS-SEQ §3`, `GS-SEQ §4`, `diagrams/09`, `diagrams/11`, **`US-F061`** |

`US-F061` est côté frontend : **le code client est déjà écrit contre 422**. Or `GS-RACI §4` désigne précisément ce contrat d'erreur comme le plus critique à figer avant tout développement d'écran : *« Le flow d'erreur "stock insuffisant" ne peut pas être mocké fidèlement sans le contrat exact »*.

**Analogie**

> Deux moitiés de la même équipe se donnent rendez-vous à la même heure, à deux adresses différentes. Chacune est ponctuelle. Personne ne se rencontre.

**Options**

- **A** — 409 partout.
- **B** — 422 partout.
- **C** — 409 pour un conflit d'état, 422 pour une règle métier sur des données valides.

**Recommandation : Option A (409)**

Le code réel de `InsufficientStockException` renvoie déjà 409 (`implementation.md`, US-003), `GS-CDA §5.3` traite le cas comme un blocage d'état, et corriger cinq documents coûte moins qu'entretenir une divergence entre l'implémentation livrée et le contrat frontend.

**Priorité de correction : `US-F061` en premier**, avant que l'écran ne soit codé plus avant.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `01-architecture/GS-SEQ-2026-01…md` | §3 et §4 | 422 → 409 |
| `diagrams/09-commandes-client-ventes-b2b.md` | Cycle de vie + séquence | 422 → 409 |
| `diagrams/11-transferts-inter-filiales.md` | Flux de transfert | 422 → 409 |
| `02-backlogs/GS-FRONTEND-BACKLOG…md` | US-F061 | 422 → 409 |
| `document/test_postman.md` | Section Ventes | Documenter la réponse d'erreur réelle |

---

### B-05 🔴 — Montants : entiers ou décimaux, et règle d'arrondi absente

**Prismes :** Business (fiscal) · Dev · Terrain

**Constat**

Le document d'autorité se contredit lui-même :

| Source | Règle |
|---|---|
| `GS-CDA §8.4` et `§1.4` | *« La devise est le Franc CFA (XAF). Les montants sont des **entiers** (pas de centimes) »* |
| `GS-CDA §6.2` « Article » | `prix_unitaire_ht` : **BigDecimal, Precision 15,2** |
| `GS-CDA §6.2` `MouvementStock` | `quantite` : **BigDecimal** |
| `GS-DATA §1` | `decimal` sur tous les prix et toutes les quantités |
| `V1` réel | `INTEGER` partout |

Et surtout, **aucun document ne définit la règle d'arrondi** :
- `18 500 × 1,1925 = 22 061,25` → 22 061 ou 22 062 ?
- Sur une facture : la TVA se calcule-t-elle ligne par ligne puis s'additionne, ou sur le total hors taxes ?

Les deux méthodes donnent des résultats différents de plusieurs francs sur un document opposable.

**Analogie**

> Deux comptables calculent la même facture de dix lignes avec la même règle « TVA 19,25 % ». L'un arrondit chaque ligne, l'autre arrondit à la fin. Ils obtiennent deux totaux différents. Les deux sont défendables — mais un seul est celui que le client a réellement payé.

**Conséquence si non traité**

Un écart de 1 F répété sur trois mille factures mensuelles devient un litige fiscal. Et le montant affiché à l'écran pourra différer du montant imprimé, selon le chemin de calcul emprunté.

**Options**

- **A** — Entiers XAF partout ; arrondi **au plus proche** (`HALF_UP`) ; TVA calculée sur le **total hors taxes** de la facture.
- **B** — Entiers ; TVA calculée par ligne puis sommée.
- **C** — `BigDecimal` en interne, arrondi entier uniquement à l'affichage et à l'impression.

**Recommandation : Option A**, sous réserve de confirmation fiscale

> **⚠️ Point réglementaire non tranchable par ce rapport :** l'administration fiscale camerounaise impose-t-elle l'arrondi ligne à ligne ou en pied de facture ? À faire confirmer par un comptable avant codage.

Le point **non négociable** en revanche : la règle doit être écrite **une seule fois**, dans `GS-CDA §8.4`, et `§6.2` doit être corrigé pour ne plus la contredire. Aujourd'hui, deux développeurs lisant deux sections du même document produiraient deux implémentations différentes.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §8.4 calculs de prix | Ajouter la règle d'arrondi complète (mode + niveau d'application) |
| `00-fonctionnel/…docx` | §6.2 « Article » et `MouvementStock` | `BigDecimal` → entier |
| `01-architecture/GS-DATA-2026-01…md` | §1 ERD | `decimal` → `integer` sur prix et quantités |
| `02-backlogs/BACKLOG…md` | US-031, US-034, US-063 | Expliciter la règle d'arrondi |
| `02-backlogs/GS-FRONTEND-BACKLOG…md` | US-F031, §16 | L'aperçu client doit appliquer la même règle |
| `03-pilotage/strategie_test.md` | §6.4 | Ajouter des cas de test d'arrondi |

---

### B-06 🟠 — L'article n'a qu'un seul prix dans le document d'autorité

**Prismes :** Business · Dev

**Constat**

`GS-CDA §6.2` définit l'article avec `prix_unitaire_ht` et `prix_unitaire_ttc` — **un seul prix**.
Le `V1` réel, le `CDCT`, `GS-DATA` et `US-031` en définissent **deux** : `prix_achat_ht` et `prix_vente_ht`, plus une `margeBrutePct` retournée par `US-031` et `US-033`.

Le document d'autorité ne connaît donc **ni prix d'achat ni marge**, alors que la marge est affichée à l'écran.

Par ailleurs `diagrams/05` impose une règle absente de partout ailleurs : *« Prix achat HT inférieur au Prix vente HT ? → sinon 400 Prix incohérents »* — elle ne figure ni dans `US-031`, ni dans un `CHECK` SQL.

**Analogie**

> Le cahier des charges décrit une balance à un seul plateau, mais l'écran affiche la différence entre deux poids. Et une règle non écrite interdit au vendeur de brader un article en fin de série.

**Options**

- **A** — Mettre `GS-CDA §6.2` à jour (deux prix + marge) et **ne pas** interdire prix d'achat ≥ prix de vente : retirer la règle de `diagrams/05`.
- **B** — Conserver le blocage à 400.
- **C** — Autoriser avec un avertissement non bloquant.

**Recommandation : Option A, éventuellement C**

Vendre à perte est une pratique réelle et légitime : déstockage de fin de série, produit d'appel, article périssable proche de la date limite. Un blocage dur (option B) empêcherait une opération commerciale normale et pousserait l'utilisateur à contourner l'outil.

L'option C — avertissement — suffit à attraper la faute de frappe sans interdire la décision commerciale.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §6.2 « Article » | Deux prix + marge calculée |
| `00-fonctionnel/…docx` | §3.4 ART-01 | Mentionner le prix d'achat |
| `diagrams/05-catalogue.md` | Règles de création | Blocage → avertissement |
| `02-backlogs/BACKLOG…md` | US-031 | Ajouter la règle d'avertissement aux critères |

---

### B-07 🟠 — Jeton de réinitialisation : deux stockages, deux durées

**Prismes :** Terrain (réseau instable) · Dev · Sécurité

**Constat**

**Stockage** — deux emplacements concurrents, dont un mort :

| Source | Emplacement |
|---|---|
| `GS-CDA §6.2`, `V1` réel (`token_reset`, `token_reset_expiry`), `US-011` | Base de données, haché |
| `diagrams/02`, `implementation.md`, code réel | Redis, clé `reset:{token}` |

Les colonnes en base ne sont **jamais utilisées**.

**Durée** — deux valeurs :

| Source | Durée |
|---|---|
| `GS-CDA §3.11 NOT-03`, `US-011`, `US-F015` | **1 heure** |
| `implementation.md`, code réel | **15 minutes** |

Aggravant : `US-085` classe `reset-password` en « comportement inchangé » car *« échoue déjà naturellement si le token Redis est illisible »* — un simple redémarrage de Redis **invalide donc tous les liens de réinitialisation en cours**, sans message explicatif pour l'utilisateur.

**Analogie**

> Deux coffres-forts pour la même clé, dont un est vide et que personne n'ouvre jamais. Et l'étiquette collée dessus annonce « valable 1 heure » alors que la serrure se bloque au bout de quinze minutes.

**Options**

- **A** — Redis, 1 heure, colonnes de base supprimées par migration.
- **B** — Base de données, 1 heure, Redis abandonné pour ce cas.
- **C** — Redis, 15 minutes, et corriger la documentation.

**Recommandation : Option A**

Quinze minutes est intenable dans un contexte de délivrabilité e-mail lente et de coupures réseau fréquentes (`GS-CDA §1.4`) : un utilisateur qui consulte sa boîte une heure après aura systématiquement un lien mort, et recommencera en boucle.

Une heure est en outre la valeur déjà annoncée à l'utilisateur dans trois documents **et dans l'interface frontend livrée**.

Les colonnes mortes doivent partir : elles feront croire à un futur développeur que le jeton est en base, et produiront une correction au mauvais endroit.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §6.2 « Utilisateur » | **Supprimer** `token_reset` et `token_reset_expiry` |
| `01-architecture/GS-DATA-2026-01…md` | §1 ERD | Idem |
| `02-backlogs/BACKLOG…md` | US-011 | « stocké haché en base » → « stocké en Redis, TTL 1 h » |
| `document/implementation.md` | US-011 | Corriger 15 min → 1 h |
| `document/test_postman.md` | Section 1.6 | Aligner |
| **Code** | `AuthServiceImpl` + migration | TTL 1 h ; supprimer les colonnes |

---

### B-08 🔴 — Table des alertes : quatre jeux de types, et aucun destinataire

**Prismes :** Business · Terrain · Dev

**Constat**

**Types d'alerte** — quatre définitions concurrentes :

| Source | Valeurs |
|---|---|
| `V1` réel + `CDCT §23.3` | `'STOCK_BAS'`, `'RUPTURE'` |
| `GS-DATA §1` | `STOCK_BAS` seul |
| `GS-CDA-2026-02 §1` + `US-064` | Exigent `ECART_STOCK_DETECTE` |
| `diagrams/12` + `US-083` | Ajoutent `SECURITY_ALERT` |

Aucune migration n'est prévue pour les deux derniers.

**Plus grave — la table n'a aucune colonne destinataire.** Or :
- `US-071` et `GS-CDA §7.4` ciblent *« le Gestionnaire de Stock **et** l'Admin Filiale »* ;
- `US-083` cible *« l'utilisateur »* concerné par l'alerte de sécurité.

Avec `entreprise_id` seul, on ne peut ni adresser une alerte, ni gérer un statut « lu » par personne : `US-073` marque l'alerte lue **pour tout le monde**.

Enfin, une alerte de sécurité (`US-083`) est liée à un **utilisateur**, pas à un **article** — alors qu'`article_id` est la seule dimension disponible.

**Analogie**

> Une lettre sans nom de destinataire, déposée sur la table du hall de l'immeuble. Le premier locataire qui passe la lit, la trouve sans intérêt pour lui, et la jette. Le vrai destinataire ne saura jamais qu'elle est arrivée.

**Options**

- **A** — Ajouter `destinataire_utilisateur_id` (nul = toute l'entreprise), tous les types dans le `CHECK`, et rendre `article_id` nullable.
- **B** — Table de liaison `alerte_destinataire` avec statut de lecture **par personne**.
- **C** — Deux tables distinctes : alertes métier et alertes de sécurité.

**Recommandation : Option B**

Le statut « lu » par personne est le comportement attendu d'un centre de notifications (`US-072`, `NOT-06`). L'option A produirait un centre d'alertes où le gestionnaire de stock fait disparaître de l'écran de son directeur une alerte que celui-ci n'a jamais vue.

L'option C reste pertinente **en complément**, si les alertes de sécurité demeurent purement transactionnelles (e-mail uniquement, sans centre de notifications).

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §6.2 `NotificationAlerte` (à ajouter) | L'entité n'est pas détaillée aujourd'hui — la décrire |
| `00-fonctionnel/…docx` | §7.4 | Préciser le destinataire et le statut de lecture |
| `01-architecture/GS-DATA-2026-01…md` | §1 ERD | Table de liaison + tous les types |
| `01-architecture/CDCT…md` | §23.3 `CHECK type_alerte` | Ajouter `ECART_STOCK_DETECTE`, `SECURITY_ALERT` |
| `02-backlogs/BACKLOG…md` | US-071, US-072, US-073, US-083 | Destinataire explicite ; « lu » par personne |
| `diagrams/12-notifications-alertes.md` | Types de notifications | Ajouter `ECART_STOCK_DETECTE` (absent) |
| **Code** | Migration | Table de liaison + élargissement du `CHECK` |

---

### B-09 🟠 — Le diagramme 14 contredit l'ADR-005 sur le comportement en panne Redis

**Prismes :** Dev (immédiat) · Sécurité · Terrain

**Constat**

| Source | Règle |
|---|---|
| `diagrams/14` | *« Si Redis est DOWN : Login → 503, Refresh → 503, Logout → 503. **JAMAIS de fail-open** sur les opérations de sécurité »* |
| `CDCT §27 ADR-005` + `US-085` révisée | `login` / `logout` / `forgot-password` **tolérants** ; fail-closed réservé à `refresh` et au rate-limiting par point d'entrée |

Le diagramme est antérieur à la révision de juillet 2026. **La contradiction est active aujourd'hui** : c'est l'objet de la branche en cours `feature/GS-085-fail-closed-redis`.

**Analogie**

> Le plan d'évacuation affiché dans le couloir indique encore l'ancienne sortie de secours, murée depuis les travaux. En cas d'incendie, la moitié des gens ira vers le mur.

**Options**

- **A** — Corriger `diagrams/14` conformément à l'ADR-005.
- **B** — Revenir au fail-closed strict et abroger l'ADR-005.
- **C** — Fail-closed strict conditionné à la mise en place d'une haute disponibilité Redis.

**Recommandation : Option A**

La justification de l'ADR-005 est solide et documentée : Redis tourne en instance unique, sans Sentinel ni cluster ; un redémarrage de conteneur est un incident courant, pas un scénario d'attaque. Un fail-closed strict sur `login` transformerait chaque redémarrage en panne totale d'authentification pour toute l'entreprise cliente.

L'option C est la bonne cible à terme, mais elle suppose un investissement d'infrastructure qui ne figure ni au budget ni au planning.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `diagrams/14-architecture-deploiement-securite.md` | Bloc « 4. Comportement fail-closed » | Réécrire selon l'ADR-005 |
| `document/test_postman.md` | Section Auth | Documenter le 503 sur `/refresh` uniquement |

---

### B-10 🟠 — Seuil de rupture : quatre définitions, et le stock négatif non traité

**Prismes :** Business · Terrain · Dev

**Constat**

| Source | Règle |
|---|---|
| `diagrams/08` | `RUPTURE` si `stock ≤ 0` ; `BAS` si `stock ≤ seuil` |
| `US-071` | Type `RUPTURE` si `stock = 0` |
| `GS-CDA §3.9 MVT-05` | *« stock **<** seuil »* |
| `GS-CDA §7.4` et `US-051` | *« stock **≤** seuil »* |

Et le stock **peut devenir négatif** : `US-054` autorise une correction négative supérieure au stock, `US-064` autorise la vente directe sans blocage. **Aucun document ne dit quel statut afficher pour un stock négatif.**

**Analogie**

> Le voyant rouge du tableau de bord signifie soit « réservoir vide », soit « le réservoir fuit ». Ce n'est pas la même intervention : dans un cas on remplit, dans l'autre on répare. Un seul voyant pour les deux conduit à remplir un réservoir percé.

**Options**

- **A** — `RUPTURE` si `≤ 0` ; `BAS` si `0 < stock ≤ seuil` (avec `seuil > 0`) ; `NORMAL` sinon.
- **B** — Ajouter un quatrième statut `ANOMALIE` pour `stock < 0`.
- **C** — Statu quo avec `= 0`.

**Recommandation : Option B**

Un stock négatif n'est pas une rupture : c'est une **erreur de donnée**. Les confondre ferait déclencher un réapprovisionnement sur une information fausse, alors que l'action attendue est un recomptage physique.

C'est cohérent avec la distinction déjà actée par `GS-CDA-2026-02 §1` entre `STOCK_BAS` (seuil métier) et `ECART_STOCK_DETECTE` (anomalie de donnée) — il manque simplement la traduction de cette distinction **côté affichage**.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §3.9 MVT-05 | Harmoniser le comparateur (`≤`) |
| `00-fonctionnel/…docx` | §7.4 | Ajouter le cas du stock négatif |
| `02-backlogs/BACKLOG…md` | US-051, US-033, US-071 | Quatre statuts au lieu de trois |
| `diagrams/08-gestion-stock.md` | Bloc d'alerte | Ajouter `ANOMALIE` |
| `02-backlogs/GS-DESIGN-BACKLOG…md` | US-D004 | Badge supplémentaire |
| `02-backlogs/GS-FRONTEND-BACKLOG…md` | US-F032 | Idem |

---

### B-11 🟠 — Publication des événements : trois comportements pour le même mécanisme

**Prismes :** Dev

**Constat**

| Source | Comportement |
|---|---|
| `CDCT §22.4`, commentaire | *« Publication **APRÈS commit** »* |
| `CDCT §22.4`, code de l'exemple | `publishEvent()` appelé **à l'intérieur** de la méthode `@Transactional` |
| `CDCT §22.4`, écouteur | `@Async` + `@Transactional(REQUIRES_NEW)` — peut donc lire un stock non encore validé |
| `GS-SEQ §5` | `@TransactionalEventListener(phase = AFTER_COMMIT)` |
| `US-071` | `@EventListener` asynchrone + `REQUIRES_NEW` |

Le commentaire du `CDCT` contredit le code qu'il commente, dans le même bloc.

**Analogie**

> On appelle les pompiers **pendant** qu'on remplit encore la déclaration d'incendie. Ils arrivent parfois avant que le sinistre soit enregistré, trouvent un registre vide, et repartent.

**Conséquence si non traité**

Alertes calculées sur un stock périmé, non reproductibles en test, apparaissant uniquement sous charge. Et l'exemple de code fautif du cahier des charges technique **sera recopié tel quel** dans les neuf modules restants.

**Options**

- **A** — `@TransactionalEventListener(AFTER_COMMIT)` + `@Async`, normalisé partout.
- **B** — `@EventListener` + `REQUIRES_NEW` (statu quo du `CDCT`).
- **C** — Recalcul du stock dans l'écouteur, après validation.

**Recommandation : Option A**, et **corriger l'exemple de code du `CDCT §22.4`**

C'est le point le plus contaminant du bloc B : un exemple de code faux dans le document technique de référence se propage mécaniquement à chaque nouveau module.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `01-architecture/CDCT…md` | §22.4 | **Réécrire l'exemple de code** avec `@TransactionalEventListener` |
| `02-backlogs/BACKLOG…md` | US-071 | Préciser la phase de l'écouteur |
| `01-architecture/GS-SEQ-2026-01…md` | §5 | Déjà correct — aucune action |
| `diagrams/12-notifications-alertes.md` | Note de bas de flux | Préciser « après validation de la transaction » |

---

### B-12 🟠 — Le formulaire de connexion révèle l'existence d'un compte

**Prismes :** Sécurité · Business (support) · Terrain

**Constat**

`US-008` impose : *« Credentials invalides → 401 avec message générique (**ne pas révéler si l'email existe**) »*.

Mais la même user story renvoie `403 ACCOUNT_DISABLED` et `403 TENANT_SUSPENDED` — deux réponses qui **prouvent** que l'adresse existe.

`diagrams/02` aggrave le défaut : il place ces vérifications **avant** le contrôle de rate limiting **et avant** la vérification du mot de passe. Un attaquant peut donc énumérer les comptes sans jamais fournir de mot de passe valide, et sans déclencher de limitation.

**Analogie**

> Le vigile répond « ce badge est suspendu » au lieu de « accès refusé ». Il vient de confirmer que le badge existe, à qui ne le savait pas.

**Options**

- **A** — Vérifier le mot de passe **d'abord** ; ne renvoyer 403 qu'après authentification réussie.
- **B** — 401 générique dans tous les cas — mais l'utilisateur désactivé ne comprend alors pas pourquoi.
- **C** — Statu quo, énumération assumée.

**Recommandation : Option A**

Elle conserve le message actionnable — *« votre compte a été désactivé, contactez votre administrateur »* — indispensable au support terrain, **sans** donner d'information à qui ne connaît pas le mot de passe. C'est le meilleur des deux mondes, pour un coût nul : il s'agit uniquement de réordonner les étapes.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `diagrams/02-authentification-acces.md` | Flux de connexion | **Réordonner** : rate limit → mot de passe → statut du compte |
| `02-backlogs/BACKLOG…md` | US-008 | Préciser l'ordre des contrôles |
| `01-architecture/CDCT…md` | §28 checklist OWASP, ligne A07 | Ajouter la protection contre l'énumération |

---

### B-13 🟠 — La devise est-elle paramétrable ?

**Prismes :** Business · Dev

**Constat**

| Source | Position |
|---|---|
| `GS-CDA §8.4` | *« La devise **est** le Franc CFA (XAF) »* |
| `GS-CDA §1.4` | *« Devise **par défaut** XAF »* |
| `GS-CDA §3.2 GRP-02` | *« Nom, logo, informations fiscales, **devise** »* — modifiables |
| `GS-CDA §2.2.2` | *« Configure les paramètres globaux (catalogue partagé, TVA, **devise**) »* |
| `GS-IA §1`, `US-F082` | Paramètre « devise » affiché dans l'interface |
| **Modèle de données** | **Aucun champ devise, nulle part** |

**Analogie**

> Un sélecteur de devise bien visible dans les paramètres, relié à rien. Le premier client qui le manipule croit avoir changé quelque chose et ne comprend pas pourquoi ses factures restent en francs CFA.

**Options**

- **A** — XAF fixe en V1 : retirer « devise » de GRP-02, `GS-IA` et `US-F082`.
- **B** — Champ `devise` sur `tenant_group`, affichage seul, calculs inchangés.
- **C** — Vrai multi-devise avec taux de change et conversion.

**Recommandation : Option A**

La cible est explicitement camerounaise (`§1.4`). L'option C contaminerait toute la règle d'arrondi (B-05), tout le reporting et toute la facturation.

Laisser un sélecteur inopérant dans l'interface est pire que ne rien afficher : cela crée une attente que le produit ne tient pas.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §1.4 | « par défaut » → « unique » |
| `00-fonctionnel/…docx` | §3.2 GRP-02, §2.2.2 | Retirer « devise » |
| `01-architecture/GS-IA-2026-01…md` | §1 | Retirer « devise » des paramètres groupe |
| `diagrams/15-navigation-frontend-par-role.md` | Paramètres groupe | Idem |
| `02-backlogs/GS-FRONTEND-BACKLOG…md` | US-F082 | Idem |
| `02-backlogs/GS-DESIGN-BACKLOG…md` | US-D082 | Idem |

---

### B-14 🟠 — Aucun fuseau horaire métier n'est défini

**Prismes :** Terrain · Business (clôture de caisse) · Dev

**Constat**

Tous les horodatages sont en `TIMESTAMPTZ`, mais trois règles dépendent d'une notion de « journée » :

| Règle | Source |
|---|---|
| *« Annulation possible uniquement si `date_vente::date = CURRENT_DATE` »* | `US-067` |
| Référence de vente `VNT-{yyyyMMdd}` | `CDCT §29` |
| « Total journalier » du journal de caisse | `US-065` |

**Aucun document ne fixe le fuseau de référence.** Le Cameroun est en UTC+1 : sur un serveur configuré en UTC, la journée commerciale bascule à **1 h 00 heure locale**.

**Analogie**

> La caisse se ferme automatiquement à une heure du matin. Une vente encaissée à minuit trente appartient déjà à la veille : elle n'est plus annulable, et elle est comptée dans le total de la mauvaise journée.

**Options**

- **A** — Fuseau métier fixé à `Africa/Douala` (UTC+1), en constante applicative.
- **B** — Fuseau configurable par entreprise.
- **C** — Statu quo : fuseau du serveur, implicite.

**Recommandation : Option A**

Le Cameroun n'a qu'un seul fuseau et pas d'heure d'été ; l'option B est une complexité sans besoin.

Mais il faut l'écrire : aujourd'hui, le comportement dépend de la configuration du serveur de production, qui n'est spécifiée nulle part — et peut donc changer au premier redéploiement.

**Documents à corriger**

| Document | Section | Action |
|---|---|---|
| `00-fonctionnel/…docx` | §8.4 ou nouvelle §8.8 | Fixer le fuseau métier de référence |
| `01-architecture/CDCT…md` | §29 + §23 | Préciser le fuseau utilisé pour les dates de référence |
| `02-backlogs/BACKLOG…md` | US-065, US-067 | Préciser le fuseau |
| `document/guideconfiguration.md` | Variables d'environnement | Ajouter `TZ=Africa/Douala` |

---

### B-15 🟠 — Documents de pilotage et contrats d'API périmés

**Prismes :** Dev (contrat porte G4) · Business (pilotage)

**Constat**

Ces écarts appellent tous la même décision ; ils sont regroupés en une seule anomalie.

| Fichier | Écart vérifié |
|---|---|
| `implementation.md` | Daté « 16 juin 2026 » ; US-009/010/011/013 annoncées « en attente de PR » alors que le `git log` montre les fusions ; chemins obsolètes (`document/BACKLOG_…` au lieu de `document/02-backlogs/…`) ; « backlog 75 US » contre 82 |
| `test_postman.md` | DTO d'inscription obsolète (`nomBoutique`, `ville`, `quartier`) contredisant `US-006` actuelle ; `pm.environment.*` alors que `GS-PLAN` affirme la correction en `pm.collectionVariables` ; réponse de `/refresh` sans le nouveau `refreshToken` (US-083). **C'est le contrat opposable de la porte G4** |
| `strategie_test.md` | `mvn spring-boot:run -pl stockmaster-shared` et JAR dans `stockmaster-shared/target/` — faux, c'est `stockmaster-bootstrap` ; **deux sections numérotées « Étape 4 »** ; comptes de tests 72 contre 90 ; `ci.yml` contre `ci-backend.yml` |
| `A_JIRA_ET_GIT_FLOW.md` | Chemins obsolètes ; annonce « 11 étapes obligatoires » mais le résumé visuel final en compte 13 |
| `KICKOFF` contre `GS-RACI §2/§7` | Le kickoff nomme une équipe de trois (Stephan, Siko) ; le RACI déclare *« aucun designer ni développeur frontend identifié — risque bloquant »*. Planning : 16 sprints contre 11. US sécurité « US-081-084 » contre la renumérotation actée en US-083-086 |
| `GS-RACI §3`, porte **G3** | Exige une *« maquette Figma au statut Validée »* comme porte **bloquante**, alors que `GS-DESIGN-BACKLOG` se déclare *« gate Figma superseded »* et que `knowledge.md` acte « le code = le design ». **G3 est une porte morte présentée comme active** |
| Totaux de points | Design D00 : 25 en détail contre 23 au récapitulatif et au `progress-ledger` (US-D008 non comptée) ; Frontend : 246 (§14) contre 237 (§17 et RACI §5) ; Backlog : 82 US / 291 points contre 80 US / 285 au kickoff, contre « 75 US » dans `GS-FRONTEND §1` |
| `US-004` | Exige une stratégie `main` + **`develop`**, alors que `A_JIRA` et `knowledge.md` interdisent explicitement `develop` |

**Analogie**

> Le plan de la maison affiché à l'entrée date d'avant les travaux. Les visiteurs cherchent une pièce qui a été supprimée, et ne trouvent pas celle qui a été ajoutée. Personne n'a menti — le plan n'a simplement jamais été remis à jour.

**Options**

- **A** — Resynchroniser les huit lignes maintenant, avant tout nouveau développement.
- **B** — Ne corriger que `test_postman.md` (contrat opposable) et geler le reste en « historique », avec un bandeau explicite.
- **C** — **Supprimer `implementation.md`** (redondant avec `progress-ledger.md` et le `git log`) et resynchroniser le reste.

**Recommandation : Option C**

`implementation.md` et `progress-ledger.md` documentent la même chose et divergent **déjà**. Deux journaux de suivi tenus à la main finiront toujours par se contredire : c'est une loi, pas un accident. Un seul journal (le `progress-ledger`), complété par le `git log`, suffit et supprime la source de divergence.

`test_postman.md` doit être corrigé **en priorité absolue** : c'est lui qui fait foi pour le frontend au titre de la porte G4, et il décrit aujourd'hui une API qui n'existe plus.

Sur la porte **G3** : elle doit être soit supprimée, soit reformulée en « design system codé et validé ». La laisser en l'état comme porte bloquante alors qu'elle ne peut pas être franchie est un risque de blocage artificiel pour tout nouvel arrivant qui lira le RACI au pied de la lettre.

**Documents à corriger**

| Document | Action |
|---|---|
| `document/implementation.md` | **Supprimer**, après report des informations non redondantes vers `progress-ledger.md` |
| `document/test_postman.md` | Resynchroniser intégralement sur l'API réelle (priorité 1) |
| `document/postman_collection.json` | Idem, en cohérence stricte |
| `03-pilotage/strategie_test.md` | Corriger le module de lancement, la numérotation dupliquée, les comptes de tests, le nom du workflow |
| `03-pilotage/A_JIRA_ET_GIT_FLOW.md` | Corriger les chemins, harmoniser 11/13 étapes, retirer `implementation.md` de la liste des documents à mettre à jour |
| `03-pilotage/GS-RACI-2026-01…md` | §2 et §7 : aligner sur l'équipe réelle ; §3 : reformuler ou supprimer **G3** |
| `03-pilotage/KICKOFF…md` | §3 et §4 : aligner sur l'état réel et sur la numérotation US-083-086 |
| `02-backlogs/GS-DESIGN-BACKLOG…md` | Corriger le sous-total EPIC-D00 (25, pas 23) |
| `02-backlogs/GS-FRONTEND-BACKLOG…md` | §1, §2 (stack déjà tranchée), §14 et §17 : harmoniser les totaux |
| `02-backlogs/BACKLOG…md` | US-004 : retirer la mention de `develop` |
| `03-pilotage/progress-ledger.md` | Devient le journal unique |

---

*(Suite : bloc C — trous fonctionnels, bloc D — benchmark, puis plan de correction consolidé.)*
