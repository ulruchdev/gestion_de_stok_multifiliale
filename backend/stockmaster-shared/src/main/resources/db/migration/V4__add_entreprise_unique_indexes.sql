-- ============================================================
-- V4__add_entreprise_unique_indexes.sql
-- Index UNIQUE partiels pour l'unicité stricte des entreprises
-- (US-082) — compatible soft-delete : ne contraint que les
-- enregistrements actifs (supprime = FALSE) et non NULL.
-- ============================================================

CREATE UNIQUE INDEX uq_entreprise_nif_actif
    ON entreprise(nif) WHERE supprime = FALSE AND nif IS NOT NULL;
CREATE UNIQUE INDEX uq_entreprise_telephone_actif
    ON entreprise(telephone) WHERE supprime = FALSE AND telephone IS NOT NULL;
CREATE UNIQUE INDEX uq_entreprise_email_actif
    ON entreprise(email) WHERE supprime = FALSE AND email IS NOT NULL;
