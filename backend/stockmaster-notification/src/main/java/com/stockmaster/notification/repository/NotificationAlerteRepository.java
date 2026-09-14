package com.stockmaster.notification.repository;

import com.stockmaster.notification.domain.entity.NotificationAlerte;
import com.stockmaster.notification.domain.enums.EtatAlerte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationAlerteRepository extends JpaRepository<NotificationAlerte, Long> {

    Optional<NotificationAlerte> findByIdAndSupprimeFalse(Long id);

    /** Toutes les alertes d'une entreprise + celles adressées explicitement à l'utilisateur. */
    Page<NotificationAlerte> findAllByEntrepriseIdAndSupprimeFalse(Long entrepriseId, Pageable pageable);

    Page<NotificationAlerte> findAllByEntrepriseIdAndDestinataireIdAndSupprimeFalse(
            Long entrepriseId, Long destinataireId, Pageable pageable);

    Page<NotificationAlerte> findAllByEntrepriseIdAndEtatAndSupprimeFalse(
            Long entrepriseId, EtatAlerte etat, Pageable pageable);

    /** Badge non-lues (index partiel idx_alerte_entreprise_non_lue côté base). */
    long countByEntrepriseIdAndEtatAndSupprimeFalse(Long entrepriseId, EtatAlerte etat);
}
