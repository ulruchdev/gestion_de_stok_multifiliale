package com.stockmaster.stock.repository;

import com.stockmaster.stock.domain.entity.MouvementStock;
import com.stockmaster.stock.domain.enums.TypeMouvement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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
}
