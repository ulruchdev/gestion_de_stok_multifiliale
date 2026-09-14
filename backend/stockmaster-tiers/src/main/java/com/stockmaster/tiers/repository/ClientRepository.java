package com.stockmaster.tiers.repository;

import com.stockmaster.tiers.domain.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByIdAndSupprimeFalse(Long id);

    Page<Client> findAllByEntrepriseIdAndSupprimeFalse(Long entrepriseId, Pageable pageable);

    long countByEntrepriseIdAndSupprimeFalse(Long entrepriseId);
}
