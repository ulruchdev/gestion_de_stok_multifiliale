package com.stockmaster.catalogue.service;

import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.security.StockMasterPrincipal;
import org.springframework.stereotype.Component;

/**
 * Garde-fous tenant du catalogue — un seul exemplaire pour les US-027+
 * (miroir module {@code utilisateur} de {@code PerimetreAdminGuard}).
 *
 * <p>Le catalogue est <b>group-scoped</b> ({@code DEC-002}, V5 §6) : l'ancre
 * tenant est le {@code groupId} du JWT — l'{@code entreprise_id} n'intervient
 * pas (le backlog US-027 parlait d'{@code entreprise_id} du JWT, la V5 a
 * remplacé cette colonne par {@code group_id} — le critère « isolation tenant,
 * jamais depuis le corps de la requête » est honoré par le groupe).</p>
 *
 * <p>Tous les refus sont <b>fail-fast</b> : avant toute I/O repository.</p>
 */
@Component
public class PerimetreCatalogueGuard {

    /**
     * Extrait le {@code groupId} du principal — {@code GRP_002} (403) s'il est
     * absent (principal non groupé).
     */
    public Long groupIdDuPrincipal(StockMasterPrincipal principal) {
        Long groupId = principal != null ? principal.getGroupId() : null;
        if (groupId == null) {
            throw new BusinessException(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
        return groupId;
    }

    /**
     * Extrait et normalise le rôle du principal — {@code SEC_001} (403) s'il
     * est absent ou illisible.
     */
    public String roleDuPrincipal(StockMasterPrincipal principal) {
        String role = principal != null ? principal.getRole() : null;
        if (role == null || role.isBlank()) {
            throw new BusinessException(ErrorCode.SEC_ACCESS_DENIED);
        }
        return role;
    }
}
