-- ============================================================
-- V5__schema_decisionnel.sql
-- Schéma cible décisionnel — applique les décisions du référentiel
-- Référentiel : document/referentiel/06-modele-donnees.md (§6.2)
-- Décisions : DEC-002, 003, 004, 007, 008, 009, 010, 011, 012, 013,
--             015, 016, 020, 024, 027, 033, 036
-- POSTGRES 16 / Flyway (les types enum sont des VARCHAR + CHECK)
-- ============================================================

-- ============================================================
-- Nettoyage d'index qui ciblent des colonnes transformées ci-dessous
-- (V2 + V4) — suppression anticipée pour permettre les ALTER TYPE.
-- ============================================================
DROP INDEX IF EXISTS idx_article_entreprise;
DROP INDEX IF EXISTS idx_categorie_entreprise;
DROP INDEX IF EXISTS idx_utilisateur_token_reset;
DROP INDEX IF EXISTS idx_transfert_source;
DROP INDEX IF EXISTS idx_transfert_cible;
DROP INDEX IF EXISTS idx_alerte_entreprise_non_lue;

-- ============================================================
-- 1. TENANT_GROUP — plans à 3 valeurs (DEC-015)
--    STARTER supprimé ; ENTERPRISE → PERSONNALISE
--    ajout de limite_utilisateurs (GRATUIT 10 / PRO 50 / PERSONNALISE négocié)
-- ============================================================
ALTER TABLE tenant_group DROP CONSTRAINT tenant_group_plan_abonnement_check;
ALTER TABLE tenant_group
    ADD CONSTRAINT chk_plan_abonnement
    CHECK (plan_abonnement IN ('GRATUIT','PRO','PERSONNALISE'));
ALTER TABLE tenant_group
    ADD COLUMN limite_utilisateurs INTEGER NOT NULL DEFAULT 10
    CHECK (limite_utilisateurs > 0);
COMMENT ON COLUMN tenant_group.limite_utilisateurs IS 'Décimal : 10 (GRATUIT) / 50 (PRO) / négocié (PERSONNALISE) — DEC-015.';

-- ============================================================
-- 2. ENTREPRISE — site_operationnel (DEC-015)
--              code_filiale NOT NULL (A-10 / DEC-015)
--              UNIQUE (id, group_id) pour la FK composite du transfert (DEC-007)
-- ============================================================
-- code_filiale : pour un dépôt vierge aucune ligne ; défensif sinon
UPDATE entreprise SET code_filiale = 'SIEGE' WHERE code_filiale IS NULL;
ALTER TABLE entreprise
    ALTER COLUMN code_filiale SET NOT NULL;

ALTER TABLE entreprise
    ADD COLUMN site_operationnel BOOLEAN NOT NULL DEFAULT TRUE;
COMMENT ON COLUMN entreprise.site_operationnel IS 'TRUE = détient du stock / compte dans limite_filiales ; la maison mère peut être un pur siège (FALSE) — DEC-015.';

-- Index unique (id, group_id) : sert de cible aux FK composites de transfert_stock.
ALTER TABLE entreprise
    ADD CONSTRAINT uq_entreprise_id_group UNIQUE (id, group_id);

-- ============================================================
-- 3. UTILISATEUR — email_verifie (DEC-016),
--          suppression des colonnes token_reset (mortes — DEC-024)
-- ============================================================
ALTER TABLE utilisateur
    ADD COLUMN email_verifie BOOLEAN NOT NULL DEFAULT FALSE;
COMMENT ON COLUMN utilisateur.email_verifie IS 'Compte inactif tant que FALSE (DEC-016) — distinct de actif (désactivation admin).';

ALTER TABLE utilisateur DROP COLUMN token_reset;
ALTER TABLE utilisateur DROP COLUMN token_reset_expiry;
-- ============================================================
-- 4. CATEGORIE — rattachée au GROUPE (DEC-002) : group_id remplace entreprise_id
-- ============================================================
ALTER TABLE categorie DROP CONSTRAINT uq_categorie_code_entreprise;
ALTER TABLE categorie DROP COLUMN entreprise_id;
ALTER TABLE categorie
    ADD COLUMN group_id BIGINT NOT NULL REFERENCES tenant_group(id) ON DELETE RESTRICT;
ALTER TABLE categorie
    ADD CONSTRAINT uq_categorie_code_groupe UNIQUE (group_id, code);
COMMENT ON TABLE categorie IS 'Catalogue partagé au niveau groupe (DEC-002).';

-- ============================================================
-- 5. ARTICLE — rattaché au GROUPE (DEC-002) ; prix (INTEGER XAF, DEC-003)
--    quantités DECIMAL(12,3) ; unités (DEC-013) ; lot/péremption (DEC-012)
-- ============================================================
ALTER TABLE article DROP CONSTRAINT uq_article_code_entreprise;
ALTER TABLE article DROP COLUMN entreprise_id;
ALTER TABLE article
    ADD COLUMN group_id BIGINT NOT NULL REFERENCES tenant_group(id) ON DELETE RESTRICT;
ALTER TABLE article
    ADD CONSTRAINT uq_article_code_groupe UNIQUE (group_id, code_article);

-- DEC-003 : quantités en DECIMAL(12,3) — seuil_alerte (quantité, pas montant)
ALTER TABLE article ALTER COLUMN seuil_alerte TYPE DECIMAL(12,3);

-- DEC-013 : conditionnement (unité d'achat ≠ unité de gestion)
ALTER TABLE article
    ADD COLUMN unite_gestion       VARCHAR(20)  NOT NULL DEFAULT 'UNITE';
ALTER TABLE article
    ADD COLUMN unite_achat         VARCHAR(20)  NOT NULL DEFAULT 'UNITE';
ALTER TABLE article
    ADD COLUMN facteur_conversion  DECIMAL(12,3) NOT NULL DEFAULT 1.000
    CHECK (facteur_conversion > 0);
COMMENT ON COLUMN article.facteur_conversion IS '1 unité d''achat = facteur_conversion unités de gestion (DEC-013).';

-- DEC-012 : schéma lot-ready (colonnes présentes ; FEFO/choix de lot en V1.5)
ALTER TABLE article
    ADD COLUMN lot             VARCHAR(50);
ALTER TABLE article
    ADD COLUMN date_peremption DATE;
COMMENT ON COLUMN article.lot IS 'Lot présente en V1 (schéma lot-ready) ; usage opérationnel (FEFO) en V1.5 — DEC-012.';

COMMENT ON TABLE article IS 'Catalogue partagé au niveau groupe (DEC-002). Stock et prix restent par filiale.';
-- ============================================================
-- 6. COMMANDE_FOURNISSEUR — machine à états (DEC-006)
--    COMMANDEE → PARTIELLEMENT_RECUE → RECEPTIONNEE | ANNULEE
-- ============================================================
ALTER TABLE commande_fournisseur DROP CONSTRAINT commande_fournisseur_etat_commande_check;
ALTER TABLE commande_fournisseur
    ADD CONSTRAINT chk_etat_commande_fournisseur
    CHECK (etat_commande IN ('COMMANDEE','PARTIELLEMENT_RECUE','RECEPTIONNEE','ANNULEE'));
ALTER TABLE commande_fournisseur ALTER COLUMN etat_commande SET DEFAULT 'COMMANDEE';

-- quantités DECIMAL(12,3) (DEC-003)
ALTER TABLE ligne_commande_fournisseur ALTER COLUMN quantite TYPE DECIMAL(12,3);

-- ============================================================
-- 7. COMMANDE_CLIENT — machine (DEC-006) + règlement/échéance (DEC-011, DEC-020)
-- ============================================================
ALTER TABLE commande_client DROP CONSTRAINT commande_client_etat_commande_check;
ALTER TABLE commande_client
    ADD CONSTRAINT chk_etat_commande_client
    CHECK (etat_commande IN ('EN_PREPARATION','VALIDEE','LIVREE','ANNULEE'));

ALTER TABLE commande_client
    ADD COLUMN etat_reglement  VARCHAR(20)   NOT NULL DEFAULT 'NON_REGLEE'
    CHECK (etat_reglement IN ('NON_REGLEE','REGLEE'));
ALTER TABLE commande_client
    ADD COLUMN date_echeance   DATE;
ALTER TABLE commande_client
    ADD COLUMN date_reglement  TIMESTAMPTZ;
COMMENT ON COLUMN commande_client.etat_reglement IS 'DEC-011 : pas d''acompte en V1 — un paiement partiel reste NON_REGLEE (décision assumée).';
COMMENT ON COLUMN commande_client.date_reglement IS 'DEC-020 : CA encaissé compté à cette date.';

-- quantités DECIMAL(12,3) (DEC-003)
ALTER TABLE ligne_commande_client ALTER COLUMN quantite TYPE DECIMAL(12,3);

-- ============================================================
-- 8. TRANSFERT_STOCK + LIGNE_TRANSFERT — multi-lignes, groupe, états (DEC-002/007)
--    Remplaçe l'ancienne table mono-article sans group_id.
-- ============================================================
DROP TABLE transfert_stock CASCADE;

CREATE TABLE transfert_stock (
    id                  BIGSERIAL PRIMARY KEY,
    group_id            BIGINT      NOT NULL REFERENCES tenant_group(id) ON DELETE RESTRICT,
    filiale_source_id   BIGINT      NOT NULL,
    filiale_cible_id    BIGINT      NOT NULL,
    demandeur_id        BIGINT      NOT NULL REFERENCES utilisateur(id) ON DELETE RESTRICT,
    approbateur_id      BIGINT      REFERENCES utilisateur(id) ON DELETE RESTRICT,
    reference           VARCHAR(30) NOT NULL,
    statut              VARCHAR(20) NOT NULL DEFAULT 'DEMANDE'
                        CHECK (statut IN ('DEMANDE','VALIDE','EN_TRANSIT','RECU','ECART','REFUSE','ANNULE')),
    motif_refus         TEXT,
    date_demande        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_validation     TIMESTAMPTZ,
    date_expedition     TIMESTAMPTZ,
    date_reception      TIMESTAMPTZ,
    date_creation       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modification   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime            BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_transfert_sites_differents CHECK (filiale_source_id <> filiale_cible_id),
    CONSTRAINT fk_transfert_source_meme_groupe
        FOREIGN KEY (group_id, filiale_source_id) REFERENCES entreprise(group_id, id),
    CONSTRAINT fk_transfert_cible_meme_groupe
        FOREIGN KEY (group_id, filiale_cible_id) REFERENCES entreprise(group_id, id)
);
COMMENT ON TABLE transfert_stock IS 'Bon de transfert multi-lignes, rattaché au groupe. FK composites (groupe,filiale) garantissent source et cible dans le même groupe (DEC-007).';
COMMENT ON COLUMN transfert_stock.statut IS 'DEMANDE → VALIDE → EN_TRANSIT → RECU | ECART ; sorties : REFUSE, ANNULE (DEC-002).';

CREATE TABLE ligne_transfert (
-- ============================================================
-- 8bis. MOUVEMENT_STOCK — retrait du trigger de mise à jour (C-12 / REF §6.2)
--       Le journal est IMMUABLE : aucun UPDATE ne doit exister.
--       V3 avait appliqué le trigger générique — on le retire.
-- ============================================================
DROP TRIGGER IF EXISTS trg_mouvement_stock_update_date_modification ON mouvement_stock;
-- ============================================================
-- 9. CAISSE — session_caisse + paiement (DEC-009, DEC-010, DEC-018)
-- ============================================================
CREATE TABLE session_caisse (
    id                  BIGSERIAL PRIMARY KEY,
    entreprise_id       BIGINT      NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    utilisateur_id      BIGINT      NOT NULL REFERENCES utilisateur(id) ON DELETE RESTRICT,
    statut              VARCHAR(20) NOT NULL DEFAULT 'OUVERTE'
                        CHECK (statut IN ('OUVERTE','CLOTUREE')),
    date_ouverture      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fond_caisse         INTEGER     NOT NULL DEFAULT 0,
    date_cloture        TIMESTAMPTZ,
    montant_constate    INTEGER,
    ecart               INTEGER,
    date_creation       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modification   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime            BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE session_caisse IS 'Session de caisse : toute vente y est rattachée (DEC-009, DEC-018). La clôture est le contrôle anti-perte.';
COMMENT ON COLUMN session_caisse.ecart IS 'Constitué à la clôture (constaté − attendu), peut être négatif.';

-- ============================================================
-- 10. VENTE — statut PAYEE | ANNULEE | REMBOURSEE (DEC-006/010/018)
--     client_id nullable (vendue à un client connu, DEC-010)
--     rattachée à une session de caisse (DEC-009)
-- ============================================================
ALTER TABLE vente DROP COLUMN annulee;
ALTER TABLE vente
    ADD COLUMN statut            VARCHAR(20) NOT NULL DEFAULT 'PAYEE'
    CHECK (statut IN ('PAYEE','ANNULEE','REMBOURSEE'));
ALTER TABLE vente
    ADD COLUMN client_id          BIGINT REFERENCES client(id) ON DELETE RESTRICT;
ALTER TABLE vente
    ADD COLUMN session_caisse_id  BIGINT NOT NULL REFERENCES session_caisse(id) ON DELETE RESTRICT;
COMMENT ON COLUMN vente.statut IS 'PAYEE → ANNULEE | REMBOURSEE (DEC-006, DEC-010) — jamais VALIDEE pour une vente directe.';

-- ============================================================
-- 11. PAIEMENT — paiement mixte (DEC-009)
-- ============================================================
CREATE TABLE paiement (
    id                    BIGSERIAL PRIMARY KEY,
    entreprise_id         BIGINT      NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    vente_id              BIGINT      NOT NULL REFERENCES vente(id) ON DELETE RESTRICT,
    mode                  VARCHAR(20) NOT NULL CHECK (mode IN ('ESPECES','MOBILE_MONEY','CARTE')),
    montant               INTEGER     NOT NULL CHECK (montant >= 0),
    reference_transaction VARCHAR(100),
    date_creation         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modification     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime              BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE paiement IS 'Paiement mixte espèce / Mobile Money / carte à la caisse (DEC-009).';

-- ============================================================
-- 12. MOUVEMENT_STOCK — immuable, quantités DECIMAL(12,3),
--     + ANNULATION_VENTE, REMBOURSEMENT (DEC-010), origine_type + ANNULATION_VENTE
-- ============================================================
ALTER TABLE mouvement_stock DROP CONSTRAINT mouvement_stock_type_mouvement_check;
ALTER TABLE mouvement_stock
-- ============================================================
-- 13. NOTIFICATION_ALERTE — refondue (DEC-004)
--     destinataire explicite, type extensible (pas de CHECK), etat (non lu/lu/résolu)
-- ============================================================
DROP TABLE notification_alerte;

CREATE TABLE notification_alerte (
    id                          BIGSERIAL PRIMARY KEY,
    entreprise_id               BIGINT      NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    destinataire_utilisateur_id BIGINT      REFERENCES utilisateur(id) ON DELETE RESTRICT,
    article_id                  BIGINT      REFERENCES article(id) ON DELETE RESTRICT,
    type_alerte                 VARCHAR(30) NOT NULL DEFAULT 'STOCK_BAS',
    stock_actuel                DECIMAL(12,3),
    seuil_alerte                DECIMAL(12,3),
    etat                        VARCHAR(20) NOT NULL DEFAULT 'NON_LUE'
                                CHECK (etat IN ('NON_LUE','LUE','RESOLUE')),
    date_creation               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modification           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime                    BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE notification_alerte IS 'Centre de notifications. type_alerte volontairement SANS CHECK : extensible (DEC-004) — STOCK_BAS, RUPTURE, SECURITY_ALERT… ; ECART_STOCK_DETECTE supprimé (DEC-037).';
COMMENT ON COLUMN notification_alerte.destinataire_utilisateur_id IS 'NULL = toute l''entreprise ; sinon destinataire précis (SECURITY_ALERT). Statut lu par destinataire (DEC-004).';

-- ============================================================
-- 14. IDEMPOTENCE — clé exigée sur toute écriture de stock (DEC-027)
-- ============================================================
CREATE TABLE cle_idempotence (
    cle                 VARCHAR(64)  PRIMARY KEY,
    entreprise_id       BIGINT       NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    methode_http        VARCHAR(10)  NOT NULL,
    chemin_api          VARCHAR(255) NOT NULL,
    code_http           INTEGER      NOT NULL,
    reponse             JSONB        NOT NULL,
    date_expiration     TIMESTAMPTZ  NOT NULL,
    date_creation       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE cle_idempotence IS 'En-tête Idempotency-Key exigé sur vente, réception, transfert, correction (DEC-027).';

-- ============================================================
-- 15. INVENTAIRE — session + lignes (DEC-036), sans gel du stock
-- ============================================================
CREATE TABLE session_inventaire (
    id                  BIGSERIAL PRIMARY KEY,
    entreprise_id       BIGINT      NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    utilisateur_id      BIGINT      NOT NULL REFERENCES utilisateur(id) ON DELETE RESTRICT,
    statut              VARCHAR(20) NOT NULL DEFAULT 'OUVERTE'
                        CHECK (statut IN ('OUVERTE','VALIDE')),
    date_ouverture      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_cloture        TIMESTAMPTZ,
    date_creation       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modification   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime            BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE session_inventaire IS 'Campagne d''inventaire datée, avec validation par ADMIN_FILIALE (DEC-036). Ouverte et validée le jour même.';

CREATE TABLE ligne_inventaire (
    id                    BIGSERIAL PRIMARY KEY,
    session_inventaire_id BIGINT      NOT NULL REFERENCES session_inventaire(id) ON DELETE CASCADE,
    article_id            BIGINT      NOT NULL REFERENCES article(id) ON DELETE RESTRICT,
    quantite_constatee    DECIMAL(12,3),
    stock_systeme         DECIMAL(12,3),
    ecart                 DECIMAL(12,3),
    statut                VARCHAR(20) NOT NULL DEFAULT 'A_COMPTER'
                          CHECK (statut IN ('A_COMPTER','COMPTEE','A_RECOMPTER','VALIDE')),
    date_creation         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
-- ============================================================
-- 16. INDEX finaux (socle V5)
-- ============================================================
-- Isolation tenant — catalogue partagé au niveau groupe (DEC-002)
CREATE INDEX idx_article_groupe
    ON article(group_id) WHERE supprime = FALSE;
CREATE INDEX idx_article_groupe_code
    ON article(group_id, code_article) WHERE supprime = FALSE;
CREATE INDEX idx_categorie_groupe
    ON categorie(group_id) WHERE supprime = FALSE;

-- Calcul stock réel (requête la plus fréquente) — colonnes en DECIMAL désormais
CREATE INDEX idx_mouvement_article_entreprise
    ON mouvement_stock(article_id, entreprise_id);
CREATE INDEX idx_mouvement_date
    ON mouvement_stock(date_mouvement DESC);

-- Alertes non lues par entreprise
CREATE INDEX idx_alerte_entreprise_non_lue
    ON notification_alerte(entreprise_id) WHERE etat = 'NON_LUE' AND supprime = FALSE;

-- Transferts par filiale (états)
CREATE INDEX idx_transfert_source
    ON transfert_stock(filiale_source_id, date_demande DESC);
CREATE INDEX idx_transfert_cible
    ON transfert_stock(filiale_cible_id, date_demande DESC);
CREATE INDEX idx_transfert_statut
    ON transfert_stock(statut, date_demande DESC);

-- Lignes de transfert
CREATE INDEX idx_ligne_transfert_transfert
    ON ligne_transfert(transfert_id);
CREATE INDEX idx_ligne_transfert_article
    ON ligne_transfert(article_id);

-- Paiements par vente (caisse)
CREATE INDEX idx_paiement_vente
    ON paiement(vente_id);

-- Inventaire : lignes par session
CREATE INDEX idx_ligne_inventaire_session
    ON ligne_inventaire(session_inventaire_id);

-- Idempotence : recherche par entreprise + expiration
CREATE INDEX idx_cle_idempotence_entreprise
    ON cle_idempotence(entreprise_id);
CREATE INDEX idx_cle_idempotence_expiration
    ON cle_idempotence(date_expiration);
    date_modification     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime              BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE ligne_inventaire IS 'Écart = constatée − stock(instant du comptage), figé sur la ligne (DEC-036). La ligne refusée (stock sous zéro) est marquée A_RECOMPTER, jamais appliquée en partie.';
    ADD CONSTRAINT chk_type_mouvement
    CHECK (type_mouvement IN ('ENTREE','SORTIE','CORRECTION_POS','CORRECTION_NEG',
                              'TRANSFERT_ENTREE','TRANSFERT_SORTIE','ANNULATION_VENTE','REMBOURSEMENT'));
ALTER TABLE mouvement_stock DROP CONSTRAINT mouvement_stock_origine_type_check;
ALTER TABLE mouvement_stock
    ADD CONSTRAINT chk_origine_type
    CHECK (origine_type IN ('COMMANDE_FOURNISSEUR','COMMANDE_CLIENT','VENTE',
                            'CORRECTION','TRANSFERT','ANNULATION_VENTE'));
ALTER TABLE mouvement_stock ALTER COLUMN quantite TYPE DECIMAL(12,3);
COMMENT ON COLUMN mouvement_stock.quantite IS 'DECIMAL(12,3) — vente au poids/litre (DEC-003).';
    id                  BIGSERIAL PRIMARY KEY,
    transfert_id        BIGINT      NOT NULL REFERENCES transfert_stock(id) ON DELETE CASCADE,
    article_id          BIGINT      NOT NULL REFERENCES article(id) ON DELETE RESTRICT,
    lot                 VARCHAR(50),
    quantite_demandee   DECIMAL(12,3) NOT NULL CHECK (quantite_demandee > 0),
    quantite_expediee   DECIMAL(12,3) NOT NULL DEFAULT 0 CHECK (quantite_expediee >= 0),
    quantite_recue      DECIMAL(12,3) NOT NULL DEFAULT 0 CHECK (quantite_recue >= 0),
    date_creation       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modification   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime            BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE ligne_transfert IS 'Lignes du transfert : écart = expédié − reçu (DEC-007). Le lot est porté par la ligne (DEC-012).';