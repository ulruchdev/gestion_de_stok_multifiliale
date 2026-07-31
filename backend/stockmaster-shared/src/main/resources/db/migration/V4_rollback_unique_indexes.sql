-- ============================================================
-- V4_rollback_unique_indexes.sql
-- Rollback de V4__add_entreprise_unique_indexes.sql
-- ============================================================

DROP INDEX IF EXISTS uq_entreprise_nif_actif;
DROP INDEX IF EXISTS uq_entreprise_telephone_actif;
DROP INDEX IF EXISTS uq_entreprise_email_actif;
