package com.stockmaster.tiers.repository;

import com.stockmaster.tiers.domain.entity.Fournisseur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {

    Optional<Fournisseur> findByIdAndSupprimeFalse(Long id);

    Page<Fournisseur> findAllByEntrepriseIdAndSupprimeFalse(Long entrepriseId, Pageable pageable);

    /** Contrôle d'unicité NIF au niveau entreprise (NIF NULL autorisé, cf. normalisation). */
    boolean existsByEntrepriseIdAndNifAndSupprimeFalse(Long entrepriseId, String nif);

    /** Chargement batch pour les listes de commandes fournisseur. */
    List<Fournisseur> findAllByIdInAndSupprimeFalse(List<Long> ids);

    long countByEntrepriseIdAndSupprimeFalse(Long entrepriseId);
}
