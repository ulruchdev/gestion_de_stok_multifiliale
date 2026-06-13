-- ============================================================
-- V3__functions_and_triggers.sql
-- Fonctions PostgreSQL et triggers
-- Conforme CDCT section 23.3
-- ============================================================

-- --------------------------------------------------------
-- Trigger : mise a jour automatique de date_modification
-- --------------------------------------------------------
CREATE OR REPLACE FUNCTION update_date_modification()
RETURNS TRIGGER AS $$
BEGIN
    NEW.date_modification = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Application du trigger sur toutes les tables
DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOR tbl IN
        SELECT tablename FROM pg_tables
        WHERE schemaname = 'public'
          AND tablename IN (
              'tenant_group','entreprise','utilisateur','categorie','article',
              'client','fournisseur','commande_fournisseur','ligne_commande_fournisseur',
              'commande_client','ligne_commande_client','vente','ligne_vente',
              'mouvement_stock','transfert_stock','notification_alerte'
          )
    LOOP
        EXECUTE format(
            'CREATE TRIGGER trg_%I_update_date_modification
             BEFORE UPDATE ON %I
             FOR EACH ROW EXECUTE FUNCTION update_date_modification()',
            tbl, tbl
        );
    END LOOP;
END;
$$;
