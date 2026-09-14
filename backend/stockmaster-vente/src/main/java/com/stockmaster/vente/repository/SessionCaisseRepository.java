package com.stockmaster.vente.repository;

import com.stockmaster.vente.domain.entity.SessionCaisse;
import com.stockmaster.vente.domain.enums.StatutSessionCaisse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionCaisseRepository extends JpaRepository<SessionCaisse, Long> {

    Optional<SessionCaisse> findByIdAndSupprimeFalse(Long id);

    /** DEC-018 : une seule session OUVERTE par caissier (contrôle applicatif à l'ouverture). */
    Optional<SessionCaisse> findByUtilisateurIdAndStatutAndSupprimeFalse(
            Long utilisateurId, StatutSessionCaisse statut);

    Optional<SessionCaisse> findByEntrepriseIdAndUtilisateurIdAndStatutAndSupprimeFalse(
            Long entrepriseId, Long utilisateurId, StatutSessionCaisse statut);

    long countByEntrepriseIdAndSupprimeFalse(Long entrepriseId);
}
