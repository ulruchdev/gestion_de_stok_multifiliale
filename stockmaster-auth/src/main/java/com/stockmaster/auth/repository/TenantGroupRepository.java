package com.stockmaster.auth.repository;

import com.stockmaster.auth.domain.entity.TenantGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TenantGroupRepository extends JpaRepository<TenantGroup, Long> {

    boolean existsByNomGroupe(String nomGroupe);
}
