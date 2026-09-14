package com.stockmaster.stock.repository;

import com.stockmaster.stock.domain.entity.LigneTransfert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneTransfertRepository extends JpaRepository<LigneTransfert, Long> {

    List<LigneTransfert> findAllByTransfertIdAndSupprimeFalse(Long transfertId);
}
