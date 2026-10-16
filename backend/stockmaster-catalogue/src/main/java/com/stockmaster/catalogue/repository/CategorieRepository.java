package com.stockmaster.catalogue.repository;

import com.stockmaster.catalogue.domain.entity.Categorie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    Optional<Categorie> findByIdAndSupprimeFalse(Long id);

    Page<Categorie> findAllByGroupeIdAndSupprimeFalse(Long groupeId, Pageable pageable);

    Page<Categorie> findAllByGroupeIdAndDesignationContainingIgnoreCaseAndSupprimeFalse(
            Long groupeId, String designation, Pageable pageable);

    boolean existsByGroupeIdAndCodeAndSupprimeFalse(Long groupeId, String code);

    boolean existsByGroupeIdAndCodeAndIdNotAndSupprimeFalse(
            Long groupeId, String code, Long id);

    /**
     * DEC-028 — existence d'articles attachés à cette catégorie dans le groupe
     * (pour bloquer la suppression logique).
     */
    boolean existsArticleByCategorieIdAndGroupeIdAndSupprimeFalse(Long categorieId, Long groupeId);
}
