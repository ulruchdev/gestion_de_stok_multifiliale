package com.stockmaster.groupe.service;

import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.domain.enums.PlanAbonnement;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.groupe.dto.response.GroupeResponse;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * US-015 — Consulter les informations du groupe (P0).
 *
 * <p>Le groupe consulté est TOUJOURS celui du JWT ({@code groupId} du principal) —
 * l'endpoint ne prend aucun identifiant, l'isolation multi-tenant est structurelle.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("US-015 — GroupeService.consulter(principal)")
class GroupeServiceTest {

    @Mock
    private TenantGroupRepository groupeRepository;

    @Mock
    private EntrepriseRepository entrepriseRepository;

    @InjectMocks
    private GroupeService groupeService;

    private StockMasterPrincipal principalWithGroup(Long groupId) {
        Claims claims = mock(Claims.class);
        when(claims.get("groupId", Long.class)).thenReturn(groupId);
        return new StockMasterPrincipal(9L, claims);
    }

    private TenantGroup groupeActif() {
        return TenantGroup.builder()
                .id(1L)
                .nomGroupe("Distribo Sarl")
                .planAbonnement(PlanAbonnement.PRO)
                .actif(true)
                .supprime(false)
                .limiteFiliales(15)
                .limiteUtilisateurs(50)
                .dateExpirationPlan(LocalDate.of(2027, 6, 30))
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/groupe")
    class Consulter {

        @Test
        @DisplayName("✅ Admin Groupe → nom, plan, limite filiales, nombre de filiales actives, expiration")
        void shouldReturnGroupeInfoForOwnGroup() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupeActif()));
            when(entrepriseRepository.countByGroupeIdAndTypeEntrepriseAndActifTrueAndSupprimeFalse(1L,
                    com.stockmaster.shared.domain.enums.TypeEntreprise.FILIALE)).thenReturn(7L);

            GroupeResponse response = groupeService.consulter(principal);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getNomGroupe()).isEqualTo("Distribo Sarl");
            assertThat(response.getPlanAbonnement()).isEqualTo(PlanAbonnement.PRO);
            assertThat(response.getLimiteFiliales()).isEqualTo(15);
            assertThat(response.getNombreFiliales()).isEqualTo(7L);
            assertThat(response.getDateExpirationPlan()).isEqualTo(LocalDate.of(2027, 6, 30));
        }

        @Test
        @DisplayName("❌ Groupe inexistant → RES_ENTITY_NOT_FOUND (404)")
        void shouldThrowNotFoundWhenGroupDoesNotExist() {
            StockMasterPrincipal principal = principalWithGroup(42L);
            when(groupeRepository.findById(42L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> groupeService.consulter(principal))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ Groupe soft-supprimé → RES_ENTITY_NOT_FOUND (jamais révéler l'existence)")
        void shouldThrowNotFoundWhenGroupSoftDeleted() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            TenantGroup supprime = groupeActif();
            supprime.setSupprime(true);
            when(groupeRepository.findById(1L)).thenReturn(Optional.of(supprime));

            assertThatThrownBy(() -> groupeService.consulter(principal))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ Principal sans groupId (scope ENTREPRISE) → GRP_CROSS_GROUP_FORBIDDEN (403)")
        void shouldThrowForbiddenWhenPrincipalHasNoGroup() {
            StockMasterPrincipal principal = principalWithGroup(null);

            assertThatThrownBy(() -> groupeService.consulter(principal))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
    }
}
