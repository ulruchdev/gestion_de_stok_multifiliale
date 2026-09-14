package com.stockmaster.shared.repository;

import com.stockmaster.shared.domain.entity.TenantGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantGroupRepository extends JpaRepository<TenantGroup, Long> {

    boolean existsByNomGroupe(String nomGroupe);

    /** US-014 : unicité du nom parmi les groupes non supprimés, hors le groupe modifié lui-même. */
    boolean existsByNomGroupeAndSupprimeFalseAndIdNot(String nomGroupe, Long id);

    /** US-014 : unicité du NIF parmi les groupes non supprimés, hors le groupe modifié lui-même. */
    boolean existsByNifAndSupprimeFalseAndIdNot(String nif, Long id);
}
