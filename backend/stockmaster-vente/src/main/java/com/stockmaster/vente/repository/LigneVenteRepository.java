package com.stockmaster.vente.repository;

import com.stockmaster.vente.domain.entity.LigneVente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneVenteRepository extends JpaRepository<LigneVente, Long> {

    List<LigneVente> findAllByVenteIdAndSupprimeFalse(Long venteId);

    List<LigneVente> findAllByVenteIdInAndSupprimeFalse(List<Long> venteIds);
}
