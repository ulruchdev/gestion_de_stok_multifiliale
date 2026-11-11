package com.stockmaster.vente.repository;

import com.stockmaster.vente.domain.entity.CommandeClient;
import com.stockmaster.vente.domain.enums.EtatCommandeClient;
import com.stockmaster.vente.domain.enums.EtatReglement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommandeClientRepository extends JpaRepository<CommandeClient, Long> {

    Optional<CommandeClient> findByIdAndSupprimeFalse(Long id);

    Page<CommandeClient> findAllByEntrepriseIdAndSupprimeFalse(Long entrepriseId, Pageable pageable);

    Page<CommandeClient> findAllByEntrepriseIdAndEtatCommandeAndSupprimeFalse(
            Long entrepriseId, EtatCommandeClient etat, Pageable pageable);

    /** Relances impayés : échéance dépassée et toujours NON_REGLEE (DEC-020). */
    Page<CommandeClient> findAllByEntrepriseIdAndEtatReglementAndDateEcheanceBeforeAndSupprimeFalse(
            Long entrepriseId, EtatReglement etatReglement, java.time.LocalDate date, Pageable pageable);

    boolean existsByEntrepriseIdAndCodeAndSupprimeFalse(Long entrepriseId, String code);

    /**
     * US-043 — historique de commandes d'un client, tous états confondus
     * (y compris {@code ANNULEE}) : sert la règle d'archivage exposée par
     * {@code CommandesClientPort}.
     */
    boolean existsByClientId(Long clientId);

    long countByEntrepriseIdAndEtatReglementAndSupprimeFalse(
            Long entrepriseId, EtatReglement etatReglement);
}
