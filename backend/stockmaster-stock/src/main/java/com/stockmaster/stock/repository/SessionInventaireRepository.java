package com.stockmaster.stock.repository;

import com.stockmaster.stock.domain.entity.SessionInventaire;
import com.stockmaster.stock.domain.enums.StatutSessionInventaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionInventaireRepository extends JpaRepository<SessionInventaire, Long> {

    Optional<SessionInventaire> findByIdAndSupprimeFalse(Long id);

    /** DEC-036 : au plus une campagne OUVERTE à la fois par filiale (contrôle applicatif). */
    Optional<SessionInventaire> findByEntrepriseIdAndStatutAndSupprimeFalse(
            Long entrepriseId, StatutSessionInventaire statut);

    long countByEntrepriseIdAndSupprimeFalse(Long entrepriseId);
}
