-- ============================================================
-- V6 — US-014 : logo + informations fiscales du groupe
-- ============================================================
-- tenant_group n'exposait ni logo ni information fiscale (voir
-- referentiel/06-modele-donnees.md §6.2, cible V5) : l'ecran de
-- branding groupe (US-014, PUT /api/v1/groupe) en a besoin, distinct
-- du logo entreprise/filiale (US-081, deja present sur `entreprise`).
--
-- Types/longueurs alignes sur les colonnes equivalentes de `entreprise`
-- (V1__init_schema.sql) pour rester coherent :
--   entreprise.logo VARCHAR(500), entreprise.nif VARCHAR(20)
--   + index unique partiel sur nif (V4__add_entreprise_unique_indexes.sql)

ALTER TABLE tenant_group
    ADD COLUMN logo VARCHAR(500);
COMMENT ON COLUMN tenant_group.logo IS 'URL MinIO du logo du groupe (US-014) — distinct de entreprise.logo (US-081).';

ALTER TABLE tenant_group
    ADD COLUMN nif VARCHAR(20);
COMMENT ON COLUMN tenant_group.nif IS 'Numero d''Identifiant Fiscal du groupe (US-014).';

ALTER TABLE tenant_group
    ADD COLUMN raison_sociale VARCHAR(150);
COMMENT ON COLUMN tenant_group.raison_sociale IS 'Denomination legale du groupe (US-014) — distincte de nom_groupe (nom d''usage/commercial).';

-- Meme regle d'unicite que entreprise.nif : unique parmi les groupes non supprimes,
-- NIF absent autorise (plusieurs groupes peuvent ne pas l'avoir renseigne).
CREATE UNIQUE INDEX uq_tenant_group_nif_actif
    ON tenant_group(nif) WHERE supprime = FALSE AND nif IS NOT NULL;
