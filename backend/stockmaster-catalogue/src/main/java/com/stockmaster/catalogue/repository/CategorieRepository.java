package com.stockmaster.catalogue.repository;

import com.stockmaster.catalogue.domain.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    Optional<Categorie> findByIdAndSupprimeFalse(Long id);

    List<Categorie> findAllByGroupeIdAndSupprimeFalse(Long groupeId);

    boolean existsByGroupeIdAndCodeAndSupprimeFalse(Long groupeId, String code);
}
