-- ============================================================
-- V2__create_indexes.sql
-- Index de performance critiques
-- Conforme CDCT section 23.3
-- ============================================================

-- Isolation tenant — pattern systematique sur toutes les tables
CREATE INDEX idx_article_entreprise
    ON article(entreprise_id) WHERE supprime = FALSE;
CREATE INDEX idx_categorie_entreprise
    ON categorie(entreprise_id) WHERE supprime = FALSE;
CREATE INDEX idx_client_entreprise
    ON client(entreprise_id) WHERE supprime = FALSE;
CREATE INDEX idx_fournisseur_entreprise
    ON fournisseur(entreprise_id) WHERE supprime = FALSE;
CREATE INDEX idx_commande_fourn_entreprise
    ON commande_fournisseur(entreprise_id, etat_commande) WHERE supprime = FALSE;
CREATE INDEX idx_commande_client_entreprise
    ON commande_client(entreprise_id, etat_commande) WHERE supprime = FALSE;

-- Calcul stock reel — requete la plus frequente du systeme
CREATE INDEX idx_mouvement_article_entreprise
    ON mouvement_stock(article_id, entreprise_id);
CREATE INDEX idx_mouvement_type
    ON mouvement_stock(type_mouvement);
CREATE INDEX idx_mouvement_date
    ON mouvement_stock(date_mouvement DESC);

-- Recherche full-text articles (PostgreSQL natif)
CREATE INDEX idx_article_fulltext
    ON article USING gin(
        to_tsvector('french', designation || ' ' || code_article)
    );

-- Login et reset mot de passe
CREATE UNIQUE INDEX idx_utilisateur_email_actif
    ON utilisateur(email) WHERE supprime = FALSE;
CREATE INDEX idx_utilisateur_token_reset
    ON utilisateur(token_reset) WHERE token_reset IS NOT NULL;

-- Alertes non lues par entreprise
CREATE INDEX idx_alerte_entreprise_non_lue
    ON notification_alerte(entreprise_id) WHERE lue = FALSE AND supprime = FALSE;

-- Transferts par filiale
CREATE INDEX idx_transfert_source
    ON transfert_stock(filiale_source_id, date_transfert DESC);
CREATE INDEX idx_transfert_cible
    ON transfert_stock(filiale_cible_id, date_transfert DESC);
