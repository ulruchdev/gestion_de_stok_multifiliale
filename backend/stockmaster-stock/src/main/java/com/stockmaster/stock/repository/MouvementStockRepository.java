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
