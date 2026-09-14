package com.stockmaster.shared.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * StockMasterPrincipal — contrat d'identité partagé (JWT claims), consommé par
 * tout module fonctionnel (isolation multi-tenant via {@code entrepriseId}/{@code groupId}).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StockMasterPrincipal")
class StockMasterPrincipalTest {

    @Mock
    private Claims claims;

    @Test
    @DisplayName("getName() retourne l'userId sous forme de chaîne (contrat java.security.Principal)")
    void shouldReturnUserIdAsName() {
        StockMasterPrincipal principal = new StockMasterPrincipal(9L, claims);

        assertThat(principal.getName()).isEqualTo("9");
        assertThat(principal.getUserId()).isEqualTo(9L);
    }

    @Test
    @DisplayName("getEntrepriseId()/getGroupId()/getRole()/getScope() délèguent aux claims JWT")
    void shouldDelegateGettersToClaims() {
        when(claims.get("entrepriseId", Long.class)).thenReturn(5L);
        when(claims.get("groupId", Long.class)).thenReturn(1L);
        when(claims.get("role", String.class)).thenReturn("ADMIN_GROUPE");
        when(claims.get("scope", String.class)).thenReturn("GROUPE");
        StockMasterPrincipal principal = new StockMasterPrincipal(9L, claims);

        assertThat(principal.getEntrepriseId()).isEqualTo(5L);
        assertThat(principal.getGroupId()).isEqualTo(1L);
        assertThat(principal.getRole()).isEqualTo("ADMIN_GROUPE");
        assertThat(principal.getScope()).isEqualTo("GROUPE");
        assertThat(principal.getClaims()).isSameAs(claims);
    }

    @Test
    @DisplayName("Scope ENTREPRISE (sans groupId) → getGroupId() retourne null")
    void shouldReturnNullGroupIdForEntrepriseScope() {
        when(claims.get("groupId", Long.class)).thenReturn(null);
        StockMasterPrincipal principal = new StockMasterPrincipal(9L, claims);

        assertThat(principal.getGroupId()).isNull();
    }
}
