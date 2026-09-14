package com.stockmaster.achat.repository;

import com.stockmaster.achat.domain.entity.LigneCommandeFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneCommandeFournisseurRepository extends JpaRepository<LigneCommandeFournisseur, Long> {

    List<LigneCommandeFournisseur> findAllByCommandeIdAndSupprimeFalse(Long commandeId);

    List<LigneCommandeFournisseur> findAllByCommandeIdInAndSupprimeFalse(List<Long> commandeIds);
}
