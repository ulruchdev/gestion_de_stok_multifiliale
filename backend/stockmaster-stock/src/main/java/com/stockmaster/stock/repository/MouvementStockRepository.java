package com.stockmaster.stock.repository;

import com.stockmaster.stock.domain.entity.MouvementStock;
import com.stockmaster.stock.domain.enums.TypeMouvement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {

    List<MouvementStock> findAllByEntrepriseIdAndArticleIdOrderByDateMouvementDesc(
            Long entrepriseId, Long articleId);

    List<MouvementStock> findAllByTransfertId(Long transfertId);

    /**
     * GS-1240 — verrou d'écriture pessimiste sur le couple {@code (empresa,
     * article)} — {@code DEC-023} addendum : le stock étant calculé à la volée
     * (ADR-003), il n'y a aucune ligne de stock à verrouiller. Deux ventes
     * simultanées du dernier article liraient toutes deux {@code stock = 1} et
     * violeraient l'invariant "jamais négatif" sans sérialisation explicite.
     *
     * <p>{@code pg_advisory_xact_lock} est libéré automatiquement à la fin de la
     * transaction — appelé AVANT toute lecture du stock réel, par
     * {@code EcritureStockService}, dans sa transaction d'écriture.</p>
     */
    @Query(value = "SELECT pg_advisory_xact_lock(:entrepriseId, :articleId)", nativeQuery = true)
    void verrouilleCouple(@Param("entrepriseId") Long entrepriseId,
                          @Param("articleId") Long articleId);

    /**
     * GS-1240 — validité d'une écriture isolée en une requête : l'article existe
     * (actif) dans le groupe de l'entreprise et l'utilisateur est actif dans
     * cette même entreprise. Aucune de ces conditions ne suffit seule : un
     * utilisateur d'une autre filiale ou un article d'un autre groupe doit
     * renvoyer le 404 uniforme {@code RES_001} sans révéler l'existence de la
     * ressource (pattern US-033/US-052).
     */
    @Query(value = """
            SELECT EXISTS(
                SELECT 1
                FROM article a
                JOIN entreprise e ON e.group_id = a.group_id
                JOIN utilisateur u ON u.id = :utilisateurId
                WHERE a.id = :articleId
                  AND e.id = :entrepriseId
                  AND a.actif = TRUE
                  AND u.entreprise_id = e.id
                  AND u.actif = TRUE
            )
            """, nativeQuery = true)
    boolean referenceValide(@Param("articleId") Long articleId,
                            @Param("entrepriseId") Long entrepriseId,
                            @Param("utilisateurId") Long utilisateurId);

    /**
     * Stock réel d'un article dans une filiale — somme signée des mouvements
     * ({@code DEC-023}) via la table {@code effet_mouvement} (case-when appliqué
     * en SQL pour rester indexable sur la table partitionnée).
     */
    @Query(value = """
            SELECT COALESCE(SUM(
                       CASE m.type_mouvement
                           WHEN 'ENTREE'           THEN m.quantite
                           WHEN 'TRANSFERT_ENTREE' THEN m.quantite
                           WHEN 'ANNULATION_VENTE' THEN m.quantite
                           WHEN 'REMBOURSEMENT'    THEN m.quantite
                           WHEN 'CORRECTION_POS'   THEN m.quantite
                           WHEN 'SORTIE'           THEN -m.quantite
                           WHEN 'TRANSFERT_SORTIE' THEN -m.quantite
                           WHEN 'CORRECTION_NEG'   THEN -m.quantite
                       END
                   ), 0)
            FROM mouvement_stock m
            WHERE m.entreprise_id = :entrepriseId
              AND m.article_id    = :articleId
            """, nativeQuery = true)
    BigDecimal calculerStockReel(@Param("entrepriseId") Long entrepriseId,
                                 @Param("articleId") Long articleId);

    /** Stock réel de tous les articles d'une filiale en une requête (page dashboard). */
    @Query(value = """
            SELECT m.article_id                                            AS articleId,
                   COALESCE(SUM(
                       CASE m.type_mouvement
                           WHEN 'ENTREE'           THEN m.quantite
                           WHEN 'TRANSFERT_ENTREE' THEN m.quantite
                           WHEN 'ANNULATION_VENTE' THEN m.quantite
                           WHEN 'REMBOURSEMENT'    THEN m.quantite
                           WHEN 'CORRECTION_POS'   THEN m.quantite
                           WHEN 'SORTIE'           THEN -m.quantite
                           WHEN 'TRANSFERT_SORTIE' THEN -m.quantite
                           WHEN 'CORRECTION_NEG'   THEN -m.quantite
                       END
                   ), 0)                                                   AS stockReel
            FROM mouvement_stock m
            WHERE m.entreprise_id = :entrepriseId
            GROUP BY m.article_id
            """, nativeQuery = true)
    List<Object[]> calculerStockReelParArticle(@Param("entrepriseId") Long entrepriseId);

    long countByTypeMouvement(TypeMouvement typeMouvement);

    /**
     * US-051 — page « article × stock réel » : les articles non supprimés du
     * groupe, joints à la somme signée des mouvements de l'entreprise
     * (REF §4.2 / DEC-023 — même CASE que {@link #calculerStockReel}).
     *
     * <p>Le {@code statutAlerte} est dérivé <b>en SQL</b> (miroir de
     * {@code StatutAlerte.de}) pour que le filtre {@code ?statutAlerte=BAS}
     * préserve la pagination — filtrer après coup viderait la page.
     * Sentinelle : {@code statut = ""} = aucun filtre.</p>
     *
     * <p>Le LEFT JOIN agrège les mouvements dans une sous-requête pour ne
     * jamais créer de fan-out sur la table partitionnée.</p>
     */
    @Query(value = """
            SELECT a.id                      AS article_id,
                   a.code_article            AS code_article,
                   a.designation             AS designation,
                   a.seuil_alerte            AS seuil_alerte,
                   COALESCE(s.stock, 0)      AS stock_actuel
            FROM article a
            LEFT JOIN (
                SELECT m.article_id,
                       SUM(CASE m.type_mouvement
                               WHEN 'ENTREE'           THEN m.quantite
                               WHEN 'TRANSFERT_ENTREE' THEN m.quantite
                               WHEN 'ANNULATION_VENTE' THEN m.quantite
                               WHEN 'REMBOURSEMENT'    THEN m.quantite
                               WHEN 'CORRECTION_POS'   THEN m.quantite
                               WHEN 'SORTIE'           THEN -m.quantite
                               WHEN 'TRANSFERT_SORTIE' THEN -m.quantite
                               WHEN 'CORRECTION_NEG'   THEN -m.quantite
                           END) AS stock
                FROM mouvement_stock m
                WHERE m.entreprise_id = :entrepriseId
                GROUP BY m.article_id
            ) s ON s.article_id = a.id
            WHERE a.group_id = :groupId
              AND a.supprime = FALSE
              AND (:statut = ''
                   OR :statut = CASE
                          WHEN COALESCE(s.stock, 0) < 0 THEN 'ANOMALIE'
                          WHEN COALESCE(s.stock, 0) <= 0 THEN 'RUPTURE'
                          WHEN a.seuil_alerte > 0
                               AND COALESCE(s.stock, 0) <= a.seuil_alerte THEN 'BAS'
                          ELSE 'NORMAL' END)
            ORDER BY a.designation ASC
            """, nativeQuery = true, countQuery = """
            SELECT COUNT(*)
            FROM article a
            LEFT JOIN (
                SELECT m.article_id,
                       SUM(CASE m.type_mouvement
                               WHEN 'ENTREE'           THEN m.quantite
                               WHEN 'TRANSFERT_ENTREE' THEN m.quantite
                               WHEN 'ANNULATION_VENTE' THEN m.quantite
                               WHEN 'REMBOURSEMENT'    THEN m.quantite
                               WHEN 'CORRECTION_POS'   THEN m.quantite
                               WHEN 'SORTIE'           THEN -m.quantite
                               WHEN 'TRANSFERT_SORTIE' THEN -m.quantite
                               WHEN 'CORRECTION_NEG'   THEN -m.quantite
                           END) AS stock
                FROM mouvement_stock m
                WHERE m.entreprise_id = :entrepriseId
                GROUP BY m.article_id
            ) s ON s.article_id = a.id
            WHERE a.group_id = :groupId
              AND a.supprime = FALSE
              AND (:statut = ''
                   OR :statut = CASE
                          WHEN COALESCE(s.stock, 0) < 0 THEN 'ANOMALIE'
                          WHEN COALESCE(s.stock, 0) <= 0 THEN 'RUPTURE'
                          WHEN a.seuil_alerte > 0
                               AND COALESCE(s.stock, 0) <= a.seuil_alerte THEN 'BAS'
                          ELSE 'NORMAL' END)
            """)
    Page<Object[]> listerStockParArticle(@Param("groupId") Long groupId,
                                         @Param("entrepriseId") Long entrepriseId,
                                         @Param("statut") String statut,
                                         Pageable pageable);

    /**
     * US-052 — historique paginé des mouvements d'un article dans l'entreprise
     * du JWT, tri date DESC (critère US-052).
     *
     * <p>Sentinelles non nulles (aucun paramètre NULL côté JDBC) :
     * {@code type = ""} = tous les types, {@code debut} = EPOCH,
     * {@code fin} = borne haute du XXIe siècle — l'intervalle est
     * {@code [debut, fin[}. L'auteur est joint ({@code LEFT JOIN}) pour
     * afficher « prénom nom » sans charger l'entité.</p>
     */
    @Query(value = """
            SELECT m.id, m.type_mouvement, m.quantite, m.date_mouvement,
                   m.utilisateur_id, u.prenom, u.nom,
                   m.origine_type, m.origine_id, m.motif
            FROM mouvement_stock m
            LEFT JOIN utilisateur u ON u.id = m.utilisateur_id
            WHERE m.entreprise_id = :entrepriseId
              AND m.article_id    = :articleId
              AND (:type = '' OR m.type_mouvement = :type)
              AND m.date_mouvement >= :debut
              AND m.date_mouvement <  :fin
            ORDER BY m.date_mouvement DESC, m.id DESC
            """, nativeQuery = true, countQuery = """
            SELECT COUNT(*)
            FROM mouvement_stock m
            WHERE m.entreprise_id = :entrepriseId
              AND m.article_id    = :articleId
              AND (:type = '' OR m.type_mouvement = :type)
              AND m.date_mouvement >= :debut
              AND m.date_mouvement <  :fin
            """)
    Page<Object[]> listerMouvementsParArticle(@Param("entrepriseId") Long entrepriseId,
                                              @Param("articleId") Long articleId,
                                              @Param("type") String type,
                                              @Param("debut") Instant debut,
                                              @Param("fin") Instant fin,
                                              Pageable pageable);

    /**
     * US-052 — appartenance de l'article au groupe du JWT : contrôle fait ici
     * (couche contrat {@code catalogue.domain.entity} autorisée par ArchUnit
     * n°1) pour renvoyer le 404 uniforme {@code RES_001} sans jamais révéler
     * l'existence d'un article d'un autre groupe (pattern US-033).
     */
    @Query(value = """
            SELECT EXISTS(SELECT 1 FROM article a
                          WHERE a.id = :articleId
                            AND a.group_id = :groupId
                            AND a.supprime = FALSE)
            """, nativeQuery = true)
    boolean articleExisteDansGroupe(@Param("articleId") Long articleId,
                                    @Param("groupId") Long groupId);
}
