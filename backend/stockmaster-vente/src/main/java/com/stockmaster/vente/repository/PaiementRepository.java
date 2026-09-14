package com.stockmaster.vente.repository;

import com.stockmaster.vente.domain.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    List<Paiement> findAllByVenteIdAndSupprimeFalse(Long venteId);
}
