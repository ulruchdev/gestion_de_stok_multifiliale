-- ============================================================
-- V5__schema_decisionnel.sql
-- Schéma cible décisionnel — applique les décisions du référentiel
-- Référentiel : document/referentiel/06-modele-donnees.md (§6.2)
-- Décisions : DEC-002, 003, 004, 006, 007, 008, 009, 010, 011, 012,
--             013, 015, 016, 020, 024, 027, 030, 033, 036, 037
-- Corrections : C-12 (trigger immuabilité), A-10 (code_filiale NOT NULL)
-- POSTGRES 16 / Flyway (types enum = VARCHAR + CHECK)
--
-- Toutes les transformations de données existantes sont préservées :
-- vente.annulee -> statut, états commande_fournisseur renommés,
-- catalogue rattaché au groupe de son entreprise (backfill).
-- ============================================================

-- ============================================================
-- 0. Nettoyage d'index qui ciblent des colonnes transformées
--    ou des tables reconstruites (V2) — suppression anticipée.
-- ============================================================
DROP INDEX IF EXISTS idx_article_entreprise;
DROP INDEX IF EXISTS idx_categorie_entreprise;
DROP INDEX IF EXISTS idx_utilisateur_token_reset;
DROP INDEX IF EXISTS idx_transfert_source;
DROP INDEX IF EXISTS idx_transfert_cible;
DROP INDEX IF EXISTS idx_alerte_entreprise_non_lue;

-- Objet mort, cité par le CDCT mais jamais créé (REF §6.2)
DROP VIEW IF EXISTS vue_stock_reel;

-- ============================================================
-- 1. TENANT_GROUP — plans à 3 valeurs (DEC-015)
--    STARTER supprimé ; ENTERPRISE → PERSONNALISE
--    ajout limite_utilisateurs (GRATUIT 10 / PRO 50 / PERSONNALISE négocié)
-- ============================================================
ALTER TABLE tenant_group DROP CONSTRAINT tenant_group_plan_abonnement_check;
-- Mapping des plans supprimés AVANT le nouveau CHECK (sans perte) :
-- STARTER → PRO (plan payant le plus proche) ; ENTERPRISE → PERSONNALISE.
UPDATE tenant_group SET plan_abonnement = 'PRO' WHERE plan_abonnement = 'STARTER';
UPDATE tenant_group SET plan_abonnement = 'PERSONNALISE' WHERE plan_abonnement = 'ENTERPRISE';
ALTER TABLE tenant_group
    ADD CONSTRAINT chk_plan_abonnement
    CHECK (plan_abonnement IN ('GRATUIT','PRO','PERSONNALISE'));
ALTER TABLE tenant_group
    ADD COLUMN limite_utilisateurs INTEGER NOT NULL DEFAULT 10
    CHECK (limite_utilisateurs > 0);
COMMENT ON COLUMN tenant_group.limite_utilisateurs IS '10 (GRATUIT) / 50 (PRO) / négocié (PERSONNALISE) — DEC-015.';

-- ============================================================
-- 2. ENTREPRISE — code_filiale NOT NULL (A-10 / DEC-015)
--              site_operationnel (DEC-015)
--              UNIQUE (id, group_id) pour FK composite transfert (DEC-007)
-- ============================================================
UPDATE entreprise SET code_filiale = 'SIEGE' WHERE code_filiale IS NULL;
ALTER TABLE entreprise ALTER COLUMN code_filiale SET NOT NULL;

ALTER TABLE entreprise
    ADD COLUMN site_operationnel BOOLEAN NOT NULL DEFAULT TRUE;
COMMENT ON COLUMN entreprise.site_operationnel IS 'TRUE = détient du stock / compte dans limite_filiales ; la maison mère peut être un pur siège (FALSE) — DEC-015.';

-- Index unique (id, group_id) : cible des FK composites de transfert_stock.
ALTER TABLE entreprise
    ADD CONSTRAINT uq_entreprise_id_group UNIQUE (id, group_id);

-- ============================================================
-- 3. UTILISATEUR — email_verifie (DEC-016),
--          suppression des colonnes token_reset (mortes — DEC-024)
-- ============================================================
ALTER TABLE utilisateur
    ADD COLUMN email_verifie BOOLEAN NOT NULL DEFAULT FALSE;
COMMENT ON COLUMN utilisateur.email_verifie IS 'Compte inactif tant que FALSE (DEC-016) — distinct de actif (désactivation admin). Connexion exige actif = TRUE ET email_verifie = TRUE.';

-- Les index V2/V4 restants ne dépendent pas de ces colonnes ; drop direct.
ALTER TABLE utilisateur DROP COLUMN token_reset;
ALTER TABLE utilisateur DROP COLUMN token_reset_expiry;

-- ============================================================
-- 4. COMMANDE_FOURNISSEUR — machine à états (DEC-006)
--    COMMANDEE → PARTIELLEMENT_RECUE → RECEPTIONNEE | ANNULEE
--    Migration des états existants : LIVREE → RECEPTIONNEE (la réception
--    physique crée l'entrée de stock) ; EN_PREPARATION et VALIDEE → COMMANDEE.
-- ============================================================
-- L'ancien CHECK (EN_PREPARATION|VALIDEE|LIVREE) est retiré AVANT la
-- migration des états, sinon les UPDATE vers RECEPTIONNEE/COMMANDEE
-- seraient rejetés par la contrainte sortante.
ALTER TABLE commande_fournisseur DROP CONSTRAINT commande_fournisseur_etat_commande_check;

UPDATE commande_fournisseur SET etat_commande = 'RECEPTIONNEE'
    WHERE etat_commande = 'LIVREE';
UPDATE commande_fournisseur SET etat_commande = 'COMMANDEE'
    WHERE etat_commande IN ('EN_PREPARATION','VALIDEE');

ALTER TABLE commande_fournisseur
    ADD CONSTRAINT chk_etat_commande_fournisseur
    CHECK (etat_commande IN ('COMMANDEE','PARTIELLEMENT_RECUE','RECEPTIONNEE','ANNULEE'));
ALTER TABLE commande_fournisseur ALTER COLUMN etat_commande SET DEFAULT 'COMMANDEE';

-- quantités DECIMAL(12,3) (DEC-003)
ALTER TABLE ligne_commande_fournisseur ALTER COLUMN quantite TYPE DECIMAL(12,3);

-- ============================================================
-- 5. COMMANDE_CLIENT — machine (DEC-006) + règlement/échéance (DEC-011, DEC-020)
-- ============================================================
ALTER TABLE commande_client DROP CONSTRAINT commande_client_etat_commande_check;
ALTER TABLE commande_client
    ADD CONSTRAINT chk_etat_commande_client
    CHECK (etat_commande IN ('EN_PREPARATION','VALIDEE','LIVREE','ANNULEE'));

ALTER TABLE commande_client
    ADD COLUMN etat_reglement  VARCHAR(20) NOT NULL DEFAULT 'NON_REGLEE'
    CHECK (etat_reglement IN ('NON_REGLEE','REGLEE'));
ALTER TABLE commande_client
    ADD COLUMN date_echeance   DATE;
ALTER TABLE commande_client
    ADD COLUMN date_reglement  TIMESTAMPTZ;
COMMENT ON COLUMN commande_client.etat_reglement IS 'DEC-011 : pas d''acompte en V1 — paiement partiel reste NON_REGLEE (décision assumée).';
COMMENT ON COLUMN commande_client.date_reglement IS 'DEC-020 : CA encaissé compté à cette date.';

-- quantités DECIMAL(12,3) (DEC-003)
ALTER TABLE ligne_commande_client ALTER COLUMN quantite TYPE DECIMAL(12,3);

-- ============================================================
-- 6. CATEGORIE — rattachée au GROUPE (DEC-002) : group_id remplace entreprise_id
--    Backfill : le groupe est celui de l'entreprise propriétaire.
--    NOTE : si deux filiales d'un même groupe portaient le même code
--    catégorie, l'unicité (group_id, code) échouera — collision à
--    résoudre manuellement, conséquence assumée de DEC-002.
-- ============================================================
ALTER TABLE categorie ADD COLUMN group_id BIGINT;
UPDATE categorie c
    SET group_id = e.group_id
    FROM entreprise e
    WHERE c.entreprise_id = e.id;
ALTER TABLE categorie ALTER COLUMN group_id SET NOT NULL;
ALTER TABLE categorie
    ADD CONSTRAINT fk_categorie_groupe
    FOREIGN KEY (group_id) REFERENCES tenant_group(id) ON DELETE RESTRICT;

ALTER TABLE categorie DROP CONSTRAINT uq_categorie_code_entreprise;
ALTER TABLE categorie DROP COLUMN entreprise_id;
ALTER TABLE categorie
    ADD CONSTRAINT uq_categorie_code_groupe UNIQUE (group_id, code);
COMMENT ON TABLE categorie IS 'Catalogue partagé au niveau groupe (DEC-002).';

-- ============================================================
-- 7. ARTICLE — rattaché au GROUPE (DEC-002) ; montants INTEGER XAF (DEC-003)
--    quantités DECIMAL(12,3) ; unités (DEC-013) ; lot/péremption (DEC-012)
-- ============================================================
ALTER TABLE article ADD COLUMN group_id BIGINT;
UPDATE article a
    SET group_id = e.group_id
    FROM entreprise e
    WHERE a.entreprise_id = e.id;
ALTER TABLE article ALTER COLUMN group_id SET NOT NULL;
ALTER TABLE article
    ADD CONSTRAINT fk_article_groupe
    FOREIGN KEY (group_id) REFERENCES tenant_group(id) ON DELETE RESTRICT;

ALTER TABLE article DROP CONSTRAINT uq_article_code_entreprise;
ALTER TABLE article DROP COLUMN entreprise_id;
ALTER TABLE article
    ADD CONSTRAINT uq_article_code_groupe UNIQUE (group_id, code_article);

-- DEC-003 : toutes les quantités en DECIMAL(12,3) — seuil_alerte est une quantité
ALTER TABLE article ALTER COLUMN seuil_alerte TYPE DECIMAL(12,3);

-- DEC-013 : conditionnement (unité d'achat ≠ unité de gestion)
ALTER TABLE article
    ADD COLUMN unite_gestion      VARCHAR(20)  NOT NULL DEFAULT 'UNITE';
ALTER TABLE article
    ADD COLUMN unite_achat        VARCHAR(20)  NOT NULL DEFAULT 'UNITE';
ALTER TABLE article
    ADD COLUMN facteur_conversion DECIMAL(12,3) NOT NULL DEFAULT 1.000
    CHECK (facteur_conversion > 0);
COMMENT ON COLUMN article.facteur_conversion IS '1 unité d''achat = facteur_conversion unités de gestion (DEC-013).';

-- DEC-012 : schéma lot-ready (usage opérationnel FEFO en V1.5)
ALTER TABLE article
    ADD COLUMN lot             VARCHAR(50);
ALTER TABLE article
    ADD COLUMN date_peremption DATE;
COMMENT ON COLUMN article.lot IS 'Lot présent en V1 (schéma lot-ready) ; usage opérationnel (FEFO) en V1.5 — DEC-012.';

-- DEC-003 : quantités des lignes de vente en DECIMAL(12,3) (les prix restent INTEGER XAF)
ALTER TABLE ligne_vente ALTER COLUMN quantite TYPE DECIMAL(12,3);

-- ============================================================
-- 8. TRANSFERT_STOCK + LIGNE_TRANSFERT — multi-lignes, groupe, états (DEC-002/007)
--    Remplace l'ancienne table mono-article sans group_id.
--    Les transferts existants (dev) ne sont pas migrés : modèle incompat-
--    ible (1 article → N lignes). Les mouvements qui les référencent
--    sont conservés, référence mise à NULL (section 12).
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
COMMENT ON TABLE transfert_stock IS 'Bon de transfert multi-lignes, rattaché au groupe. FK composites (groupe,filiale) garantissent source et cible dans le même groupe (DEC-007). Exception déclarée à la règle entreprise_id (REF §6.3).';
COMMENT ON COLUMN transfert_stock.statut IS 'DEMANDE → VALIDE → EN_TRANSIT → RECU | ECART ; sorties : REFUSE, ANNULE (DEC-002).';

CREATE TABLE ligne_transfert (
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

CREATE INDEX idx_transfert_group  ON transfert_stock(group_id, date_demande DESC);
CREATE INDEX idx_transfert_source ON transfert_stock(filiale_source_id);
CREATE INDEX idx_transfert_cible  ON transfert_stock(filiale_cible_id);
CREATE INDEX idx_ligne_transfert_article ON ligne_transfert(article_id);

-- ============================================================
-- 9. CAISSE — session_caisse (DEC-009, DEC-010, DEC-018)
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

CREATE INDEX idx_session_caisse_entreprise ON session_caisse(entreprise_id, statut);
CREATE INDEX idx_session_caisse_utilisateur ON session_caisse(utilisateur_id, date_ouverture DESC);

-- ============================================================
-- 10. VENTE — statut PAYEE | ANNULEE | REMBOURSEE (DEC-006/010/018)
--     client_id nullable ; rattachée à une session (DEC-009)
--     Migration des données : annulee = TRUE → ANNULEE, sinon PAYEE.
-- ============================================================
ALTER TABLE vente ADD COLUMN statut VARCHAR(20);
UPDATE vente SET statut = CASE WHEN annulee THEN 'ANNULEE' ELSE 'PAYEE' END;
ALTER TABLE vente ALTER COLUMN statut SET NOT NULL;
ALTER TABLE vente ALTER COLUMN statut SET DEFAULT 'PAYEE';
ALTER TABLE vente
    ADD CONSTRAINT chk_vente_statut
    CHECK (statut IN ('PAYEE','ANNULEE','REMBOURSEE'));
ALTER TABLE vente DROP COLUMN annulee;
COMMENT ON COLUMN vente.statut IS 'PAYEE → ANNULEE | REMBOURSEE (DEC-006, DEC-010) — jamais VALIDEE pour une vente directe.';

ALTER TABLE vente
    ADD COLUMN client_id BIGINT REFERENCES client(id) ON DELETE RESTRICT;

-- Session obligatoire applicativement (DEC-018 : aucune vente hors session).
-- Colonne nullable en base pour ne pas casser les ventes existantes ;
-- le module caisse pose la contrainte à la création (TDD US-087).
ALTER TABLE vente
    ADD COLUMN session_caisse_id BIGINT REFERENCES session_caisse(id) ON DELETE RESTRICT;
COMMENT ON COLUMN vente.session_caisse_id IS 'Session de la vente (DEC-018) — renseignée par le module caisse ; obligation appliquée au niveau applicatif.';

CREATE INDEX idx_vente_session ON vente(session_caisse_id);
CREATE INDEX idx_vente_date_entreprise ON vente(entreprise_id, date_vente DESC);

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

CREATE INDEX idx_paiement_vente ON paiement(vente_id);

-- ============================================================
-- 12. MOUVEMENT_STOCK — journal immuable, quantités DECIMAL(12,3) (DEC-003),
--     + ANNULATION_VENTE, REMBOURSEMENT (DEC-010), origine_type + ANNULATION_VENTE
--     C-12    : le trigger update_date_modification est EXCLU de la table.
--     DEC-033 : table PARTITIONNÉE par mois sur date_mouvement.
--     REF §6.3 : supprime interdit d'usage (CHECK).
--     La partition d'une table existante exige un rebuild : rename,
--     recréation partitionnée, reprise des données, synchronisation
--     de séquence, suppression de l'ancienne table.
-- ============================================================
DROP TRIGGER IF EXISTS trg_mouvement_stock_update_date_modification ON mouvement_stock;

ALTER TABLE mouvement_stock RENAME TO mouvement_stock_v4;

CREATE TABLE mouvement_stock (
    id                BIGSERIAL,
    entreprise_id     BIGINT      NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    article_id        BIGINT      NOT NULL REFERENCES article(id) ON DELETE RESTRICT,
    type_mouvement    VARCHAR(30) NOT NULL CHECK (type_mouvement IN (
                          'ENTREE','SORTIE','CORRECTION_POS','CORRECTION_NEG',
                          'TRANSFERT_ENTREE','TRANSFERT_SORTIE',
                          'ANNULATION_VENTE','REMBOURSEMENT')),
    quantite          DECIMAL(12,3) NOT NULL CHECK (quantite > 0),
    date_mouvement    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    utilisateur_id    BIGINT      NOT NULL REFERENCES utilisateur(id) ON DELETE RESTRICT,
    origine_id        BIGINT,
    origine_type      VARCHAR(30) CHECK (origine_type IN (
                          'COMMANDE_FOURNISSEUR','COMMANDE_CLIENT',
                          'VENTE','CORRECTION','TRANSFERT','ANNULATION_VENTE')),
    transfert_id      BIGINT      REFERENCES transfert_stock(id) ON DELETE RESTRICT,
    motif             TEXT,
    date_creation     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime          BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_mouvement_stock PRIMARY KEY (id, date_mouvement),
    CONSTRAINT chk_mouvement_stock_supprime_interdit CHECK (supprime = FALSE)
) PARTITION BY RANGE (date_mouvement);

-- Partition par défaut : reçoit l'existant et tout mouvement dont la
-- partition mensuelle n'a pas encore été créée par l'exploitation.
CREATE TABLE mouvement_stock_default PARTITION OF mouvement_stock DEFAULT;
COMMENT ON TABLE mouvement_stock IS 'Journal immuable des mouvements de stock (aucun UPDATE/DELETE — trigger exclu, C-12). Partitionné par mois sur date_mouvement (DEC-033) ; partitions mensuelles créées par l''exploitation, partition défaut sinon.';
COMMENT ON COLUMN mouvement_stock.quantite IS 'DECIMAL(12,3) — vente au poids/litre (DEC-003).';
COMMENT ON CONSTRAINT chk_mouvement_stock_supprime_interdit ON mouvement_stock IS 'REF §6.3 : colonne supprime préservée mais interdite d''usage — journal immuable.';

-- Les transferts référencés ont disparu (section 8) : les références
-- deviendraient des FK violées → mise à NULL, les lignes du journal
-- restent intégralement conservées.
UPDATE mouvement_stock_v4 SET transfert_id = NULL WHERE transfert_id IS NOT NULL;

-- Reprise des données : les nouveaux CHECK acceptent tous les anciens
-- type_mouvement et origine_type → transformation sans perte.
-- (Une ligne anciennement soft-supprimée ferait échouer le CHECK
-- chk_mouvement_stock_supprime_interdit : cas impossible par
-- construction, le journal n'a jamais eu de chemin de suppression.)
INSERT INTO mouvement_stock (
    id, entreprise_id, article_id, type_mouvement, quantite, date_mouvement,
    utilisateur_id, origine_id, origine_type, transfert_id, motif,
    date_creation, date_modification, supprime
)
SELECT
    id, entreprise_id, article_id, type_mouvement, quantite, date_mouvement,
    utilisateur_id, origine_id, origine_type, transfert_id, motif,
    date_creation, date_modification, supprime
FROM mouvement_stock_v4;

-- Synchronisation de la séquence après copie des ids explicites.
SELECT setval(
    pg_get_serial_sequence('mouvement_stock', 'id'),
    COALESCE((SELECT MAX(id) FROM mouvement_stock), 0) + 1,
    false
);

DROP TABLE mouvement_stock_v4;

-- Index de calcul du stock réel et de reporting (recréés sur la table partitionnée).
CREATE INDEX idx_mouvement_article_entreprise
    ON mouvement_stock(article_id, entreprise_id);
CREATE INDEX idx_mouvement_type ON mouvement_stock(type_mouvement);
CREATE INDEX idx_mouvement_date ON mouvement_stock(date_mouvement DESC);

-- ============================================================
-- 13. NOTIFICATION_ALERTE — refondue (DEC-004)
--     destinataire explicite, type extensible (sans CHECK), etat
--     Les alertes existantes (dev) sont perdues : table stub sans
--     destinataire, modèle incompatible (DEC-004 la refond).
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
COMMENT ON TABLE notification_alerte IS 'Centre de notifications. type_alerte VOLONTAIREMENT SANS CHECK : extensible (DEC-004) — STOCK_BAS, RUPTURE, SECURITY_ALERT… ; ECART_STOCK_DETECTE supprimé (DEC-037).';
COMMENT ON COLUMN notification_alerte.destinataire_utilisateur_id IS 'NULL = toute l''entreprise ; sinon destinataire précis (SECURITY_ALERT).';

CREATE INDEX idx_alerte_entreprise_non_lue
    ON notification_alerte(entreprise_id)
    WHERE etat = 'NON_LUE' AND supprime = FALSE;

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

CREATE INDEX idx_idempotence_expiration ON cle_idempotence(date_expiration);

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
COMMENT ON TABLE session_inventaire IS 'Campagne d''inventaire datée, validation par ADMIN_FILIALE (DEC-036). Ouverte et validée le jour même.';

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
    date_modification     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime              BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE ligne_inventaire IS 'Écart = constatée − stock(instant du comptage), figé sur la ligne (DEC-036). Ligne refusée (sous zéro) marquée A_RECOMPTER, jamais appliquée en partie.';

CREATE INDEX idx_ligne_inventaire_session ON ligne_inventaire(session_inventaire_id);

-- ============================================================
-- 16. TRIGGERS update_date_modification — tables créées ou reconstruites
--     par V5. mouvement_stock est volontairement EXCLU (C-12 : journal
--     immuable, aucun UPDATE ne doit le toucher). Les tables conservées
--     d'origine (tenant_group … ligne_vente) gardent leur trigger V3.
-- ============================================================
DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOR tbl IN
        SELECT unnest(ARRAY[
            'transfert_stock','ligne_transfert',
            'session_caisse','paiement',
            'notification_alerte',
            'session_inventaire','ligne_inventaire'
        ])
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
