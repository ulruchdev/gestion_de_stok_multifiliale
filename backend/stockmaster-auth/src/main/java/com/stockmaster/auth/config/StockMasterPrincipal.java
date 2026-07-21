package com.stockmaster.auth.config;

import io.jsonwebtoken.Claims;
import lombok.Getter;

import java.security.Principal;

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
