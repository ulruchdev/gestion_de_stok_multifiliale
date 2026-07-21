-- ============================================================
-- V2_rollback_indexes.sql
-- Rollback de V2__create_indexes.sql
-- ============================================================
DROP INDEX IF EXISTS idx_article_entreprise;
DROP INDEX IF EXISTS idx_categorie_entreprise;
DROP INDEX IF EXISTS idx_client_entreprise;
DROP INDEX IF EXISTS idx_fournisseur_entreprise;
DROP INDEX IF EXISTS idx_commande_fourn_entreprise;
DROP INDEX IF EXISTS idx_commande_client_entreprise;
DROP INDEX IF EXISTS idx_mouvement_article_entreprise;
DROP INDEX IF EXISTS idx_mouvement_type;
DROP INDEX IF EXISTS idx_mouvement_date;
DROP INDEX IF EXISTS idx_article_fulltext;
DROP INDEX IF EXISTS idx_utilisateur_email_actif;
DROP INDEX IF EXISTS idx_utilisateur_token_reset;
DROP INDEX IF EXISTS idx_alerte_entreprise_non_lue;
DROP INDEX IF EXISTS idx_transfert_source;
DROP INDEX IF EXISTS idx_transfert_cible;
