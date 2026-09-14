# IA — Template d'instruction par User Story

> **À utiliser par le Product Owner pour générer chaque consigne de développement.**
> Un seul fichier par US ; le modèle n'a jamais la spec recopiée — on lui **pointe** les sources.

---

## Bloc 1 — Contexte (à coller en tête de chaque prompt)

```
RÉFÉRENTIEL : document/03-pilotage/IA_CONTEXTE_PROJET.md  (respecte-le intégralement)
```

## Bloc 2 — Consigne de la US (à remplir par le PO)

```
## MISSION — US-XXX : <titre court>

### LIS D'ABORD (source de vérité, ne réécris pas)
- BACKLOG  : document/02-backlogs/BACKLOG_StockMaster_CM.md → section « US-XXX »
- RÉFÉRENTIEL : document/referentiel/12-journal-decisions.md → DEC-nnn (la/les concernées)
  document/referentiel/0X-….md → section REF §x.y citée par la US
- TESTS    : <chemin(s) exact(s) des tests à faire passer> (lis-les, ne les modifie JAMAIS)
- MODÈLE   : <mention des entités/colonnes concernées, ex. « article (référentiel §6) » >

### TRAVAIL DEMANDÉ
Implémente <ce qui est demandé> dans le module <module> conformément aux sources ci-dessus.

### FICHIERS AUTORISÉS (liste EXACTE — ne crée/modifie QUE ceux-ci)
- <chemin 1>
- <chemin 2>
…

### FICHIERS INTERDITS (liste stricte)
- <chemins partagés : notamment pom.xml, application.yml, db/migration/*, shared/*, .github/*, tests fournis, referentiel/*>

### VALIDATION (avant de livrer)
- Exécute : cd backend && .\mvnw.cmd test -pl stockmaster-<module>
- Cible : BUILD SUCCESS, 0 failure, 0 error, TESTS VERTS (aucun test modifié).

### CONTRAT DE SORTIE
Applique la section 9 du contexte (bloc FILE: + fichier complet, rien d'autre).
```

---

## Bonnes pratiques PO

| Règle | Pourquoi |
|---|---|
| **Une US = un prompt** | réduction de l'erreur, diff atomique, CI lisible |
| **Pointer plutôt que recopier** | évite l'hallucination, garde les tokens du PO |
| **Interdits = partagés** | protège le socle (shared, migrations, pom, CI, tests) |
| **Ne jamais demander d'écrire des tests** | le modèle se validerait lui-même |
| **Exiger la commande de test exacte** | la boucle de contrôle est la CI, pas la confiance |
| **Faire relire le diff par le PO avant PR** | 10 min de revue humaine sur un petit diff |
| **Jamais 2 modules dans une même mission** | cross-module = décision de PO, pas de modèle |