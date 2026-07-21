package com.stockmaster.auth.repository;

import com.stockmaster.auth.domain.entity.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {
}
