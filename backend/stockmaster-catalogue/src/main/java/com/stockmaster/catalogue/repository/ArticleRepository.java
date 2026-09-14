package com.stockmaster.catalogue.repository;

import com.stockmaster.catalogue.domain.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
