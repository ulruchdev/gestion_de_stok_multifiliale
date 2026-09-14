package com.stockmaster.shared.security;

import io.jsonwebtoken.Claims;
import lombok.Getter;

import java.security.Principal;

/**
 * Principal Spring Security porté par le filtre JWT — contrat partagé.
 *
 * <p>Vit dans {@code shared} car tout module fonctionnel (groupe, catalogue,
 * vente…) doit pouvoir lire l'identité du tenant depuis le JWT sans dépendre
 * des internes d'auth (règle ArchUnit : accès inter-module sur la couche
 * contrat uniquement).</p>
 */
@Getter
public class StockMasterPrincipal implements Principal {

    private final Long userId;
    private final Claims claims;

    public StockMasterPrincipal(Long userId, Claims claims) {
        this.userId = userId;
        this.claims = claims;
    }

    @Override
    public String getName() {
        return userId.toString();
    }

    public Long getEntrepriseId() {
        return claims.get("entrepriseId", Long.class);
    }

    public Long getGroupId() {
        return claims.get("groupId", Long.class);
    }

    public String getRole() {
        return claims.get("role", String.class);
    }

    public String getScope() {
        return claims.get("scope", String.class);
    }
}
