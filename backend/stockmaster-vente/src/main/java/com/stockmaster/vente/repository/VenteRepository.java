package com.stockmaster.vente.repository;

import com.stockmaster.vente.domain.entity.Vente;
import com.stockmaster.vente.domain.enums.StatutVente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VenteRepository extends JpaRepository<Vente, Long> {

    Optional<Vente> findByIdAndSupprimeFalse(Long id);

    Page<Vente> findAllByEntrepriseIdAndSupprimeFalse(Long entrepriseId, Pageable pageable);

    Page<Vente> findAllByEntrepriseIdAndStatutAndSupprimeFalse(
            Long entrepriseId, StatutVente statut, Pageable pageable);

    Page<Vente> findAllBySessionCaisseIdAndSupprimeFalse(Long sessionCaisseId, Pageable pageable);

    boolean existsByEntrepriseIdAndCodeAndSupprimeFalse(Long entrepriseId, String code);

    long countByEntrepriseIdAndSupprimeFalse(Long entrepriseId);
}
