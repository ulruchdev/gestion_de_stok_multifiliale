package com.stockmaster.shared.repository;

import com.stockmaster.shared.domain.entity.TenantGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantGroupRepository extends JpaRepository<TenantGroup, Long> {

    boolean existsByNomGroupe(String nomGroupe);
}
