-- ============================================================
-- V1_rollback_init_schema.sql
-- Rollback manuel de V1__init_schema.sql
-- A executer manuellement en cas de besoin
-- ============================================================
DROP TABLE IF EXISTS notification_alerte CASCADE;
DROP TABLE IF EXISTS mouvement_stock CASCADE;
DROP TABLE IF EXISTS transfert_stock CASCADE;
DROP TABLE IF EXISTS ligne_vente CASCADE;
DROP TABLE IF EXISTS vente CASCADE;
DROP TABLE IF EXISTS ligne_commande_client CASCADE;
DROP TABLE IF EXISTS commande_client CASCADE;
DROP TABLE IF EXISTS ligne_commande_fournisseur CASCADE;
DROP TABLE IF EXISTS commande_fournisseur CASCADE;
DROP TABLE IF EXISTS fournisseur CASCADE;
DROP TABLE IF EXISTS client CASCADE;
DROP TABLE IF EXISTS article CASCADE;
DROP TABLE IF EXISTS categorie CASCADE;
DROP TABLE IF EXISTS utilisateur CASCADE;
DROP TABLE IF EXISTS entreprise CASCADE;
DROP TABLE IF EXISTS tenant_group CASCADE;
