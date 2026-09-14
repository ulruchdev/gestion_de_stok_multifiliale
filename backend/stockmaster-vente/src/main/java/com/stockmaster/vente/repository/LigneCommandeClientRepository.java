package com.stockmaster.vente.repository;

import com.stockmaster.vente.domain.entity.LigneCommandeClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneCommandeClientRepository extends JpaRepository<LigneCommandeClient, Long> {

    List<LigneCommandeClient> findAllByCommandeIdAndSupprimeFalse(Long commandeId);

    List<LigneCommandeClient> findAllByCommandeIdInAndSupprimeFalse(List<Long> commandeIds);
}
