-- ============================================================
-- V5__schema_decisionnel.sql
-- Schéma cible décisionnel — applique les décisions du référentiel
-- Référentiel : document/referentiel/06-modele-donnees.md (§6.2)
-- Décisions : DEC-002, 003, 004, 006, 007, 008, 009, 010, 011, 012,
--             013, 015, 016, 017, 020, 023, 024, 027, 033, 036
-- POSTGRES 16 / Flyway (les types enum sont des VARCHAR + CHECK)
--
-- Corrections apportées lors de la revue avant merge, par rapport
-- au brouillon initial (voir échange de revue) :
--   1. Réassemblage de 3 blocs dont le corps avait été séparé de
--      l'en-tête (ligne_transfert, suite mouvement_stock,
--      ligne_inventaire) — le brouillon ne pouvait pas s'exécuter.
--   2. DEC-033 (partitionnement mensuel de mouvement_stock),
--      absent du brouillon, implémenté ci-dessous (bloc 12).
--   3. ligne_vente.quantite oublié dans la conversion DECIMAL(12,3)
--      (DEC-003) — ajouté (bloc 16).
--   4. FK mouvement_stock.transfert_id -> transfert_stock(id),
--      perdue par le DROP TABLE CASCADE du bloc 8, restaurée
--      explicitement (bloc 12bis).
--   5. article/categorie : ajout de group_id en deux temps
--      (colonne nullable + backfill depuis entreprise.group_id +
--      NOT NULL) au lieu d'un NOT NULL direct sans défaut, qui
--      aurait échoué sur toute ligne existante.
--   6. idx_mouvement_article_entreprise / idx_mouvement_date /
--      idx_mouvement_type recréés explicitement (ils disparaissent
--      avec l'ancienne table mouvement_stock et n'étaient pas tous
--      repris dans le brouillon).
--   7. DROP VIEW vue_stock_reel ajouté par cohérence avec REF §6.2,
--      bien que vérifié absent des migrations V1-V4 réellement
--      appliquées (l'audit source qui la mentionne est inexact sur
--      ce point précis — sans effet, IF EXISTS).
--
-- Hypothèse posée explicitement (à vérifier avant exécution en
-- environnement partagé) : aucun module métier (stock, vente,
-- achat, transfert) n'étant encore implémenté (stubs), les tables
-- transfert_stock et mouvement_stock ne portent aucune donnée
-- réelle à ce stade. Le bloc 12 copie néanmoins les données
-- existantes de mouvement_stock de façon défensive, au cas où des
-- données de test y auraient été insérées manuellement ; le lien
-- transfert_id ne peut pas être préservé pour ces lignes (voir
-- commentaire du bloc 12) puisque transfert_stock est totalement
-- redéfini (DEC-007) et perd ses anciens identifiants.
-- ============================================================


-- ============================================================
-- 0. Nettoyage d'index — uniquement pour les tables modifiées EN
--    PLACE (article, categorie, utilisateur). Les tables
--    intégralement recréées (transfert_stock, notification_alerte,
--    mouvement_stock) n'ont pas besoin de ce nettoyage préalable :
--    DROP TABLE supprime leurs index avec elles.
-- ============================================================
DROP INDEX IF EXISTS idx_article_entreprise;
DROP INDEX IF EXISTS idx_categorie_entreprise;
DROP INDEX IF EXISTS idx_utilisateur_token_reset;

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
--              UNIQUE (group_id, id) pour la FK composite du transfert (DEC-007)
--    Ordre des colonnes dans la contrainte volontairement identique
--    à celui utilisé dans la FK composite du bloc 8, par prudence.
-- ============================================================
UPDATE entreprise SET code_filiale = 'SIEGE' WHERE code_filiale IS NULL;
ALTER TABLE entreprise
    ALTER COLUMN code_filiale SET NOT NULL;

ALTER TABLE entreprise
    ADD COLUMN site_operationnel BOOLEAN NOT NULL DEFAULT TRUE;
COMMENT ON COLUMN entreprise.site_operationnel IS 'TRUE = détient du stock / compte dans limite_filiales ; la maison mère peut être un pur siège (FALSE) — DEC-015.';

ALTER TABLE entreprise
    ADD CONSTRAINT uq_entreprise_group_id UNIQUE (group_id, id);

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
--    group_id ajouté nullable, renseigné depuis entreprise.group_id,
--    puis verrouillé NOT NULL (sûr même si des lignes existent déjà).
-- ============================================================
ALTER TABLE categorie
    ADD COLUMN group_id BIGINT REFERENCES tenant_group(id) ON DELETE RESTRICT;
UPDATE categorie c
    SET group_id = e.group_id
    FROM entreprise e
    WHERE e.id = c.entreprise_id;
ALTER TABLE categorie
    ALTER COLUMN group_id SET NOT NULL;

ALTER TABLE categorie DROP CONSTRAINT uq_categorie_code_entreprise;
ALTER TABLE categorie DROP COLUMN entreprise_id;
ALTER TABLE categorie
    ADD CONSTRAINT uq_categorie_code_groupe UNIQUE (group_id, code);
COMMENT ON TABLE categorie IS 'Catalogue partagé au niveau groupe (DEC-002).';

-- ============================================================
-- 5. ARTICLE — rattaché au GROUPE (DEC-002) ; prix (INTEGER XAF, DEC-003)
--    quantités DECIMAL(12,3) ; unités (DEC-013) ; lot/péremption (DEC-012)
-- ============================================================
ALTER TABLE article
    ADD COLUMN group_id BIGINT REFERENCES tenant_group(id) ON DELETE RESTRICT;
UPDATE article a
    SET group_id = e.group_id
    FROM entreprise e
    WHERE e.id = a.entreprise_id;
ALTER TABLE article
    ALTER COLUMN group_id SET NOT NULL;

ALTER TABLE article DROP CONSTRAINT uq_article_code_entreprise;
ALTER TABLE article DROP COLUMN entreprise_id;
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
--    Remplace l'ancienne table mono-article sans group_id.
--    Redesign complet assumé : aucune ligne historique n'existe
--    (module stock jamais implémenté à ce stade) ; le CASCADE
--    supprime aussi l'ancienne FK mouvement_stock.transfert_id,
--    restaurée explicitement au bloc 12bis une fois cette nouvelle
--    table en place.
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
--     rattachée à une session de caisse (DEC-009) ; l'identité du
--     caissier est portée par session_caisse.utilisateur_id — pas
--     de colonne caissier_id redondante sur vente (REF §6.2, "caissier_id/session").
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
-- 12. MOUVEMENT_STOCK — reconstruction complète (et non un simple
--     ALTER) pour trois raisons qui exigent toutes une recréation :
--       a) DEC-033 : partitionnement PARTITION BY RANGE(date_mouvement)
--          — PostgreSQL ne sait pas convertir une table ordinaire en
--          table partitionnée par ALTER TABLE.
--       b) DEC-003 : quantite en DECIMAL(12,3).
--       c) C-12 (REF §6.2) : retrait du trigger générique
--          update_date_modification (appliqué par V3 à mouvement_stock,
--          vérifié dans V3__functions_and_triggers.sql). Recréer la
--          table sans ce trigger règle le point sans DROP TRIGGER
--          séparé : l'ancien trigger disparaît avec l'ancienne table.
--     Une contrainte PARTITION BY impose que la clé de partition
--     fasse partie de toute clé primaire : PRIMARY KEY (id, date_mouvement)
--     remplace PRIMARY KEY (id) seul. Aucune autre table ne référence
--     mouvement_stock(id) (vérifié), ce changement est donc sans impact.
-- ============================================================
ALTER TABLE mouvement_stock RENAME TO mouvement_stock_v1_v4;

CREATE TABLE mouvement_stock (
    id                BIGSERIAL,
    entreprise_id     BIGINT        NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    article_id        BIGINT        NOT NULL REFERENCES article(id) ON DELETE RESTRICT,
    type_mouvement    VARCHAR(30)   NOT NULL,
    quantite          DECIMAL(12,3) NOT NULL,
    date_mouvement    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    utilisateur_id    BIGINT        NOT NULL REFERENCES utilisateur(id) ON DELETE RESTRICT,
    origine_id        BIGINT,
    origine_type      VARCHAR(30),
    transfert_id      BIGINT        REFERENCES transfert_stock(id) ON DELETE RESTRICT,
    motif             TEXT,
    date_creation     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    supprime          BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_mouvement_stock PRIMARY KEY (id, date_mouvement),
    CONSTRAINT chk_mouvement_quantite_positive CHECK (quantite > 0),
    CONSTRAINT chk_type_mouvement
        CHECK (type_mouvement IN ('ENTREE','SORTIE','CORRECTION_POS','CORRECTION_NEG',
                                  'TRANSFERT_ENTREE','TRANSFERT_SORTIE','ANNULATION_VENTE','REMBOURSEMENT')),
    CONSTRAINT chk_origine_type
        CHECK (origine_type IN ('COMMANDE_FOURNISSEUR','COMMANDE_CLIENT','VENTE',
                                'CORRECTION','TRANSFERT','ANNULATION_VENTE'))
) PARTITION BY RANGE (date_mouvement);
COMMENT ON TABLE mouvement_stock IS 'Journal immuable des mouvements de stock (REF §4.2) — partitionné par mois (DEC-033). Aucun UPDATE/DELETE ; trigger update_date_modification volontairement absent (C-12).';
COMMENT ON COLUMN mouvement_stock.quantite IS 'DECIMAL(12,3) — vente au poids/litre (DEC-003).';

-- Partitions couvrant le mois courant et les deux suivants (référence :
-- 10 septembre 2026) + une partition DEFAULT pour tout le reste, afin
-- qu'aucun INSERT ne puisse échouer faute de partition. La création
-- des partitions futures est une tâche opérationnelle récurrente
-- (job planifié / pg_partman), hors du périmètre de cette migration —
-- à tracer dans document/referentiel/10-exploitation.md si ce n'est
-- pas déjà outillé.
CREATE TABLE mouvement_stock_2026_09 PARTITION OF mouvement_stock
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');
CREATE TABLE mouvement_stock_2026_10 PARTITION OF mouvement_stock
    FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');
CREATE TABLE mouvement_stock_2026_11 PARTITION OF mouvement_stock
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');
CREATE TABLE mouvement_stock_default PARTITION OF mouvement_stock DEFAULT;

-- Reprise défensive des données existantes. Le lien transfert_id ne
-- peut pas être préservé : transfert_stock a été entièrement redéfini
-- au bloc 8 (nouveaux identifiants, DEC-007) ; toute ancienne valeur
-- ne correspond plus à rien. Il est donc mis à NULL pour les lignes
-- reprises — ce qui n'a d'effet que si des données de test existaient
-- déjà, aucun module métier n'écrivant encore dans cette table.
INSERT INTO mouvement_stock (
    id, entreprise_id, article_id, type_mouvement, quantite, date_mouvement,
    utilisateur_id, origine_id, origine_type, transfert_id, motif,
    date_creation, date_modification, supprime
)
SELECT
    m.id, m.entreprise_id, m.article_id, m.type_mouvement, m.quantite::DECIMAL(12,3), m.date_mouvement,
    m.utilisateur_id, m.origine_id, m.origine_type, NULL, m.motif,
    m.date_creation, m.date_modification, m.supprime
FROM mouvement_stock_v1_v4 m;

-- Resynchronisation de la séquence BIGSERIAL après reprise d'IDs explicites.
SELECT setval(
    pg_get_serial_sequence('mouvement_stock', 'id'),
    COALESCE((SELECT MAX(id) FROM mouvement_stock), 1),
    (SELECT MAX(id) FROM mouvement_stock) IS NOT NULL
);

-- CASCADE retire au passage l'ancien trigger trg_mouvement_stock_update_date_modification
-- (appliqué par V3) et les anciens index de l'ancienne table — C-12 est ainsi réglé.
DROP TABLE mouvement_stock_v1_v4 CASCADE;

CREATE INDEX idx_mouvement_article_entreprise
    ON mouvement_stock(article_id, entreprise_id);
CREATE INDEX idx_mouvement_type
    ON mouvement_stock(type_mouvement);
CREATE INDEX idx_mouvement_date
    ON mouvement_stock(date_mouvement DESC);

-- ============================================================
-- 12bis. Remarque sur transfert_id : la table mouvement_stock étant
--        entièrement recréée ci-dessus (bloc 12, pas un simple ALTER),
--        sa FK vers transfert_stock(id) est déjà déclarée en ligne
--        dans le CREATE TABLE et pointe directement vers la nouvelle
--        transfert_stock du bloc 8 — aucune restauration séparée
--        n'est nécessaire ici (contrairement à une approche par ALTER,
--        où le DROP TABLE ... CASCADE du bloc 8 aurait supprimé la
--        contrainte sans la recréer).
-- ============================================================

-- Suppression défensive d'un objet mort mentionné par REF §6.2 (C-12).
-- Vérifié : cette vue n'existe dans aucune migration V1-V4 réellement
-- appliquée (l'audit source qui l'attribue à V3 est inexact) — sans
-- effet, conservé par cohérence documentaire avec le référentiel.
DROP VIEW IF EXISTS vue_stock_reel CASCADE;

-- ============================================================
-- 13. NOTIFICATION_ALERTE — refondue (DEC-004)
--     destinataire explicite, type extensible (pas de CHECK), etat (non lu/lu/résolu)
--     Redesign complet assumé : module notification jamais implémenté,
--     aucune donnée réelle attendue.
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
    session_inventaire_id BIGINT        NOT NULL REFERENCES session_inventaire(id) ON DELETE CASCADE,
    article_id            BIGINT        NOT NULL REFERENCES article(id) ON DELETE RESTRICT,
    quantite_constatee    DECIMAL(12,3),
    stock_systeme         DECIMAL(12,3),
    ecart                 DECIMAL(12,3),
    statut                VARCHAR(20)   NOT NULL DEFAULT 'A_COMPTER'
                          CHECK (statut IN ('A_COMPTER','COMPTEE','A_RECOMPTER','VALIDE')),
    date_creation         TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    date_modification     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    supprime              BOOLEAN       NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE ligne_inventaire IS 'Écart = constatée − stock(instant du comptage), figé sur la ligne (DEC-036). La ligne refusée (stock sous zéro) est marquée A_RECOMPTER, jamais appliquée en partie.';

-- ============================================================
-- 16. QUANTITÉS RESTANTES EN DECIMAL(12,3) (DEC-003)
--     ligne_vente avait été omise du brouillon initial ; les deux
--     autres tables de lignes sont déjà traitées aux blocs 6 et 7.
-- ============================================================
ALTER TABLE ligne_vente ALTER COLUMN quantite TYPE DECIMAL(12,3);

-- ============================================================
-- 17. INDEX finaux (socle V5) — hors mouvement_stock, déjà traité au bloc 12
-- ============================================================
-- Isolation tenant — catalogue partagé au niveau groupe (DEC-002)
CREATE INDEX idx_article_groupe
    ON article(group_id) WHERE supprime = FALSE;
CREATE INDEX idx_article_groupe_code
    ON article(group_id, code_article) WHERE supprime = FALSE;
CREATE INDEX idx_categorie_groupe
    ON categorie(group_id) WHERE supprime = FALSE;

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