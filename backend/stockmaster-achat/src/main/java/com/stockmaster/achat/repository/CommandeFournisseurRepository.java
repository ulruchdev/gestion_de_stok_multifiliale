package com.stockmaster.achat.repository;

import com.stockmaster.achat.domain.entity.CommandeFournisseur;
import com.stockmaster.achat.domain.enums.EtatCommandeFournisseur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommandeFournisseurRepository extends JpaRepository<CommandeFournisseur, Long> {

    Optional<CommandeFournisseur> findByIdAndSupprimeFalse(Long id);

    Page<CommandeFournisseur> findAllByEntrepriseIdAndSupprimeFalse(Long entrepriseId, Pageable pageable);

    Page<CommandeFournisseur> findAllByEntrepriseIdAndEtatCommandeAndSupprimeFalse(
            Long entrepriseId, EtatCommandeFournisseur etat, Pageable pageable);

    boolean existsByEntrepriseIdAndCodeAndSupprimeFalse(Long entrepriseId, String code);

    /** Réceptions en cours (indicateur filiale, page d'accueil). */
    long countByEntrepriseIdAndEtatCommandeAndSupprimeFalse(
            Long entrepriseId, EtatCommandeFournisseur etat);
}
