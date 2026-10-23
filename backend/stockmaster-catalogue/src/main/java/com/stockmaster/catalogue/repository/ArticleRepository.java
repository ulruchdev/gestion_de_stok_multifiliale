package com.stockmaster.catalogue.repository;

import com.stockmaster.catalogue.domain.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Optional<Article> findByIdAndSupprimeFalse(Long id);


    Page<Article> findAllByGroupeIdAndSupprimeFalse(Long groupeId, Pageable pageable);

    Page<Article> findAllByGroupeIdAndCategorieIdAndSupprimeFalse(
            Long groupeId, Long categorieId, Pageable pageable);

    /** Alerte stock bas : le calcul du stock réel est fait par stockmaster-stock (DEC-023). */
    List<Article> findAllByGroupeIdAndSupprimeFalseAndSeuilAlerteGreaterThan(
            Long groupeId, BigDecimal seuilMinimal);

    /** Chargement batch des lignes de documents, scoping groupe obligatoire. */
    List<Article> findAllByIdInAndGroupeIdAndSupprimeFalse(Collection<Long> ids, Long groupeId);

    boolean existsByGroupeIdAndCodeArticleAndSupprimeFalse(Long groupeId, String codeArticle);

    /**
     * US-032 — recherche plein-texte simple (désignation OU code article, sans
     * casse). Bind params JPQL uniquement — aucune concaténation de chaînes
     * (anti-pattern interdit). L'index GIN PostgreSQL du search avancé est un
     * critère de performance documenté, couvert en V1 par ces LIKE évalués sur
     * la page du groupe (catalogue de taille PME). Vérifie aussi
     * l'unicité au niveau groupe.
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.groupe.id = :groupeId
              AND a.supprime = false
              AND (
                    LOWER(a.designation) LIKE LOWER(CONCAT('%', :recherche, '%'))
                 OR LOWER(a.codeArticle) LIKE LOWER(CONCAT('%', :recherche, '%'))
              )
            """)
    Page<Article> rechercherParGroupeEtTexte(
            @Param("groupeId") Long groupeId,
            @Param("recherche") String recherche,
            Pageable pageable);

    /**
     * DEC-028 — existence d'articles attachés à cette catégorie dans le groupe
     * (pour bloquer la suppression logique).
     */
    boolean existsByGroupeIdAndCategorieIdAndSupprimeFalse(Long groupeId, Long categorieId);
}
