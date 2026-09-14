package com.stockmaster.shared.repository;

import com.stockmaster.shared.domain.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    boolean existsByEmail(String email);

    Optional<Utilisateur> findByEmail(String email);

    long countByEntrepriseGroupeIdAndSupprimeFalse(Long groupeId);
}
