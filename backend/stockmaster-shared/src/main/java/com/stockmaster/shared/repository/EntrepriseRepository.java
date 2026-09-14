package com.stockmaster.shared.repository;

import com.stockmaster.shared.domain.entity.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {

    boolean existsByNifAndSupprimeFalse(String nif);

    boolean existsByTelephoneAndSupprimeFalse(String telephone);

    boolean existsByEmailAndSupprimeFalse(String email);

    long countByGroupeIdAndSupprimeFalse(Long groupeId);
}
