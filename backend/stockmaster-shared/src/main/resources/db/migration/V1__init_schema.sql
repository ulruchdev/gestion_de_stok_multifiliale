-- V1__init_schema.sql
-- Schema initial complet


-- tenant_group
CREATE TABLE tenant_group (
    id                    BIGSERIAL PRIMARY KEY,
    nom_groupe            VARCHAR(100) NOT NULL,
    plan_abonnement       VARCHAR(20)  NOT NULL DEFAULT 'GRATUIT'
                          CHECK (plan_abonnement IN ('GRATUIT','STARTER','PRO','ENTERPRISE')),
    actif                 BOOLEAN      NOT NULL DEFAULT TRUE,
    date_expiration_plan  DATE,
    limite_filiales       INTEGER      NOT NULL DEFAULT 1,
    date_creation         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    date_modification     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    supprime              BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_tenant_group_nom UNIQUE (nom_groupe)
);
COMMENT ON TABLE tenant_group IS 'Niveau racine du multi-tenant.';

-- entreprise
CREATE TABLE entreprise (
    id                BIGSERIAL PRIMARY KEY,
    group_id          BIGINT       NOT NULL REFERENCES tenant_group(id) ON DELETE RESTRICT,
    parent_id         BIGINT       REFERENCES entreprise(id) ON DELETE RESTRICT,
    type_entreprise   VARCHAR(10)  NOT NULL CHECK (type_entreprise IN ('MERE','FILIALE')),
    nom               VARCHAR(100) NOT NULL,
    code_filiale      VARCHAR(10),
    nif               VARCHAR(20),
    email             VARCHAR(150),
    telephone         VARCHAR(20),
    adresse_rue       VARCHAR(200),
    adresse_quartier  VARCHAR(100),
    adresse_ville     VARCHAR(100),
    adresse_region    VARCHAR(100),
    adresse_pays      VARCHAR(50) DEFAULT 'Cameroun',
    logo              VARCHAR(500),
    actif             BOOLEAN      NOT NULL DEFAULT TRUE,
    date_creation     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    supprime          BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_entreprise_code_filiale UNIQUE (group_id, code_filiale)
);
COMMENT ON COLUMN entreprise.parent_id IS 'NULL = maison mere.';

-- categorie
CREATE TABLE categorie (
    id                BIGSERIAL PRIMARY KEY,
    entreprise_id     BIGINT       NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    code              VARCHAR(30)  NOT NULL,
    designation       VARCHAR(150) NOT NULL,
    taux_tva          NUMERIC(5,2) NOT NULL DEFAULT 19.25
                      CHECK (taux_tva >= 0 AND taux_tva <= 100),
    date_creation     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    supprime          BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_categorie_code_entreprise UNIQUE (entreprise_id, code)
);

-- article
CREATE TABLE article (
    id                BIGSERIAL PRIMARY KEY,
    entreprise_id     BIGINT       NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    categorie_id      BIGINT       NOT NULL REFERENCES categorie(id) ON DELETE RESTRICT,
    code_article      VARCHAR(30)  NOT NULL,
    designation       VARCHAR(150) NOT NULL,
    prix_achat_ht     INTEGER      NOT NULL CHECK (prix_achat_ht >= 0),
    prix_vente_ht     INTEGER      NOT NULL CHECK (prix_vente_ht >= 0),
    taux_tva          NUMERIC(5,2) NOT NULL CHECK (taux_tva >= 0),
    prix_vente_ttc    INTEGER      NOT NULL CHECK (prix_vente_ttc >= 0),
    seuil_alerte      INTEGER      NOT NULL DEFAULT 0 CHECK (seuil_alerte >= 0),
    photo             VARCHAR(500),
    actif             BOOLEAN      NOT NULL DEFAULT TRUE,
    date_creation     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    supprime          BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_article_code_entreprise UNIQUE (entreprise_id, code_article)
);
COMMENT ON COLUMN article.prix_vente_ttc IS 'Calcule: prix_vente_ht * (1 + taux_tva/100).';
COMMENT ON COLUMN article.seuil_alerte IS '0 = alerte desactivee.';

-- client
CREATE TABLE client (
    id                BIGSERIAL PRIMARY KEY,
    entreprise_id     BIGINT       NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    nom               VARCHAR(100) NOT NULL,
    prenom            VARCHAR(100),
    telephone         VARCHAR(20),
    email             VARCHAR(150),
    adresse_ville     VARCHAR(100),
    adresse_quartier  VARCHAR(100),
    date_creation     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    supprime          BOOLEAN      NOT NULL DEFAULT FALSE
);

-- utilisateur
CREATE TABLE utilisateur (
    id                    BIGSERIAL PRIMARY KEY,
    entreprise_id         BIGINT      NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    scope                 VARCHAR(10) NOT NULL CHECK (scope IN ('GROUPE','FILIALE')),
    role                  VARCHAR(30) NOT NULL CHECK (role IN ('SUPER_ADMIN','ADMIN_GROUPE','ADMIN_FILIALE','GESTIONNAIRE_STOCK','RESP_ACHATS','COMMERCIAL','CAISSIER')),
    nom                   VARCHAR(100) NOT NULL,
    prenom                VARCHAR(100) NOT NULL,
    email                 VARCHAR(150) NOT NULL,
    mot_de_passe          VARCHAR(255) NOT NULL,
    photo                 VARCHAR(500),
    actif                 BOOLEAN     NOT NULL DEFAULT TRUE,
    date_naissance        DATE,
    adresse_ville         VARCHAR(100),
    token_reset           VARCHAR(255),
    token_reset_expiry    TIMESTAMPTZ,
    date_creation         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modification     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime              BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_utilisateur_email UNIQUE (email)
);

-- commande_client
CREATE TABLE commande_client (
    id                BIGSERIAL PRIMARY KEY,
    entreprise_id     BIGINT      NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    client_id         BIGINT      NOT NULL REFERENCES client(id) ON DELETE RESTRICT,
    code              VARCHAR(30) NOT NULL,
    date_commande     DATE        NOT NULL DEFAULT CURRENT_DATE,
    etat_commande     VARCHAR(20) NOT NULL DEFAULT 'EN_PREPARATION'
                      CHECK (etat_commande IN ('EN_PREPARATION','VALIDEE','LIVREE')),
    commentaire       TEXT,
    date_creation     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime          BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_commande_client_code UNIQUE (entreprise_id, code)
);

-- ligne_commande_client
CREATE TABLE ligne_commande_client (
    id                BIGSERIAL PRIMARY KEY,
    entreprise_id     BIGINT       NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    commande_id       BIGINT       NOT NULL REFERENCES commande_client(id) ON DELETE RESTRICT,
    article_id        BIGINT       NOT NULL REFERENCES article(id) ON DELETE RESTRICT,
    quantite          INTEGER      NOT NULL CHECK (quantite > 0),
    prix_unitaire     INTEGER      NOT NULL CHECK (prix_unitaire >= 0),
    taux_tva_snapshot NUMERIC(5,2) NOT NULL,
    date_creation     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    supprime          BOOLEAN      NOT NULL DEFAULT FALSE
);

-- vente
CREATE TABLE vente (
    id                BIGSERIAL PRIMARY KEY,
    entreprise_id     BIGINT      NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    utilisateur_id    BIGINT      NOT NULL REFERENCES utilisateur(id) ON DELETE RESTRICT,
    code              VARCHAR(30) NOT NULL,
    date_vente        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    annulee           BOOLEAN     NOT NULL DEFAULT FALSE,
    commentaire       TEXT,
    date_creation     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime          BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_vente_code UNIQUE (entreprise_id, code)
);

-- ligne_vente
CREATE TABLE ligne_vente (
    id                BIGSERIAL PRIMARY KEY,
    entreprise_id     BIGINT       NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    vente_id          BIGINT       NOT NULL REFERENCES vente(id) ON DELETE RESTRICT,
    article_id        BIGINT       NOT NULL REFERENCES article(id) ON DELETE RESTRICT,
    quantite          INTEGER      NOT NULL CHECK (quantite > 0),
    prix_unitaire     INTEGER      NOT NULL CHECK (prix_unitaire >= 0),
    taux_tva_snapshot NUMERIC(5,2) NOT NULL,
    date_creation     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    supprime          BOOLEAN      NOT NULL DEFAULT FALSE
);

-- transfert_stock
CREATE TABLE transfert_stock (
    id                  BIGSERIAL PRIMARY KEY,
    filiale_source_id   BIGINT      NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    filiale_cible_id    BIGINT      NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    article_id          BIGINT      NOT NULL REFERENCES article(id) ON DELETE RESTRICT,
    quantite            INTEGER     NOT NULL CHECK (quantite > 0),
    utilisateur_id      BIGINT      NOT NULL REFERENCES utilisateur(id) ON DELETE RESTRICT,
    reference           VARCHAR(30) NOT NULL,
    date_transfert      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_creation       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modification   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime            BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_transfert_sites_differents
        CHECK (filiale_source_id <> filiale_cible_id)
);
COMMENT ON TABLE transfert_stock IS 'Bon de transfert entre filiales.';

-- fournisseur
CREATE TABLE fournisseur (
    id                BIGSERIAL PRIMARY KEY,
    entreprise_id     BIGINT       NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    raison_sociale    VARCHAR(200) NOT NULL,
    nif               VARCHAR(20),
    contact           VARCHAR(100),
    telephone         VARCHAR(20),
    email             VARCHAR(150),
    adresse_ville     VARCHAR(100),
    date_creation     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    supprime          BOOLEAN      NOT NULL DEFAULT FALSE
);

-- commande_fournisseur
CREATE TABLE commande_fournisseur (
    id                BIGSERIAL PRIMARY KEY,
    entreprise_id     BIGINT      NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    fournisseur_id    BIGINT      NOT NULL REFERENCES fournisseur(id) ON DELETE RESTRICT,
    code              VARCHAR(30) NOT NULL,
    date_commande     DATE        NOT NULL DEFAULT CURRENT_DATE,
    etat_commande     VARCHAR(20) NOT NULL DEFAULT 'EN_PREPARATION'
                      CHECK (etat_commande IN ('EN_PREPARATION','VALIDEE','LIVREE')),
    commentaire       TEXT,
    date_creation     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime          BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_commande_fourn_code UNIQUE (entreprise_id, code)
);

-- ligne_commande_fournisseur
CREATE TABLE ligne_commande_fournisseur (
    id                BIGSERIAL PRIMARY KEY,
    entreprise_id     BIGINT       NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    commande_id       BIGINT       NOT NULL REFERENCES commande_fournisseur(id) ON DELETE RESTRICT,
    article_id        BIGINT       NOT NULL REFERENCES article(id) ON DELETE RESTRICT,
    quantite          INTEGER      NOT NULL CHECK (quantite > 0),
    prix_unitaire     INTEGER      NOT NULL CHECK (prix_unitaire >= 0),
    taux_tva_snapshot NUMERIC(5,2) NOT NULL,
    date_creation     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    supprime          BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON COLUMN ligne_commande_fournisseur.taux_tva_snapshot IS 'Taux TVA fige.';

-- mouvement_stock
CREATE TABLE mouvement_stock (
    id                BIGSERIAL PRIMARY KEY,
    entreprise_id     BIGINT      NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    article_id        BIGINT      NOT NULL REFERENCES article(id) ON DELETE RESTRICT,
    type_mouvement    VARCHAR(30) NOT NULL CHECK (type_mouvement IN (
                          'ENTREE','SORTIE','CORRECTION_POS','CORRECTION_NEG',
                          'TRANSFERT_ENTREE','TRANSFERT_SORTIE')),
    quantite          INTEGER     NOT NULL CHECK (quantite > 0),
    date_mouvement    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    utilisateur_id    BIGINT      NOT NULL REFERENCES utilisateur(id) ON DELETE RESTRICT,
    origine_id        BIGINT,
    origine_type      VARCHAR(30) CHECK (origine_type IN (
                          'COMMANDE_FOURNISSEUR','COMMANDE_CLIENT',
                          'VENTE','CORRECTION','TRANSFERT')),
    transfert_id      BIGINT      REFERENCES transfert_stock(id) ON DELETE RESTRICT,
    motif             TEXT,
    date_creation     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime          BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE mouvement_stock IS 'Journal immuable des mouvements de stock.';

-- notification_alerte
CREATE TABLE notification_alerte (
    id                BIGSERIAL PRIMARY KEY,
    entreprise_id     BIGINT      NOT NULL REFERENCES entreprise(id) ON DELETE RESTRICT,
    article_id        BIGINT      REFERENCES article(id) ON DELETE RESTRICT,
    type_alerte       VARCHAR(30) NOT NULL CHECK (type_alerte IN ('STOCK_BAS','RUPTURE')),
    stock_actuel      INTEGER,
    seuil_alerte      INTEGER,
    lue               BOOLEAN     NOT NULL DEFAULT FALSE,
    date_creation     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    date_modification TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    supprime          BOOLEAN     NOT NULL DEFAULT FALSE
);
