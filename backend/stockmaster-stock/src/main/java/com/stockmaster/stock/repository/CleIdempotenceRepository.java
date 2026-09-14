package com.stockmaster.stock.repository;

import com.stockmaster.stock.domain.entity.CleIdempotence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface CleIdempotenceRepository extends JpaRepository<CleIdempotence, String> {

    Optional<CleIdempotence> findByCleAndDateExpirationAfter(String cle, Instant maintenant);

    /** Purge des clés expirées (job planifié, exploitation). */
    long deleteByDateExpirationBefore(Instant maintenant);
}
