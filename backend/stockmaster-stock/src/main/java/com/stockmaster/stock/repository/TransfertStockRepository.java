package com.stockmaster.stock.repository;

import com.stockmaster.stock.domain.entity.TransfertStock;
import com.stockmaster.stock.domain.enums.StatutTransfert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransfertStockRepository extends JpaRepository<TransfertStock, Long> {

    Optional<TransfertStock> findByIdAndSupprimeFalse(Long id);

    Page<TransfertStock> findAllByGroupeIdAndSupprimeFalse(Long groupeId, Pageable pageable);

    Page<TransfertStock> findAllByGroupeIdAndStatutAndSupprimeFalse(
            Long groupeId, StatutTransfert statut, Pageable pageable);

    /** Transferts à réceptionner par la filiale (dashboard filiale cible). */
    Page<TransfertStock> findAllByFilialeCibleIdAndStatutInAndSupprimeFalse(
            Long filialeCibleId, java.util.Collection<StatutTransfert> statuts, Pageable pageable);

    /** Transferts à expédier par la filiale source. */
    Page<TransfertStock> findAllByFilialeSourceIdAndStatutInAndSupprimeFalse(
            Long filialeSourceId, java.util.Collection<StatutTransfert> statuts, Pageable pageable);

    boolean existsByGroupeIdAndReferenceAndSupprimeFalse(Long groupeId, String reference);

    long countByGroupeIdAndStatutAndSupprimeFalse(Long groupeId, StatutTransfert statut);
}
