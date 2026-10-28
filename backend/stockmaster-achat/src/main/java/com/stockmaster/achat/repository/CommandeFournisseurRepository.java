package com.stockmaster.achat.repository;

import com.stockmaster.achat.domain.entity.CommandeFournisseur;
import com.stockmaster.achat.domain.enums.EtatCommandeFournisseur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
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

    /**
     * US-039 — commandes non closes d'un fournisseur (port
     * {@code tiers.port.CommandesFournisseurPort}). Aucun filtre tenant : l'id du
     * fournisseur est unique au niveau plateforme et a déjà été contrôlé contre
     * l'{@code entreprise_id} du JWT par l'appelant.
     */
    boolean existsByFournisseurIdAndEtatCommandeInAndSupprimeFalse(
            Long fournisseurId, Collection<EtatCommandeFournisseur> etats);
}
