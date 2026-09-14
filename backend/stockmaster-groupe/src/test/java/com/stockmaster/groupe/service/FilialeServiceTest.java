package com.stockmaster.groupe.service;

import com.stockmaster.groupe.dto.request.FilialeCreateRequest;
import com.stockmaster.groupe.dto.response.FilialeResponse;
import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.domain.enums.PlanAbonnement;
import com.stockmaster.shared.domain.enums.TypeEntreprise;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * US-016 — Créer une filiale dans le groupe (P0).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FilialeService.creer(principal, request)")
class FilialeServiceTest {

    @Mock
    private TenantGroupRepository groupeRepository;

    @Mock
    private EntrepriseRepository entrepriseRepository;

    @Mock
    private ControleLimiteFilialesService controleLimiteFilialesService;

    @InjectMocks
    private FilialeService filialeService;

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

    private Entreprise maisonMere() {
        return Entreprise.builder()
                .id(10L)
                .typeEntreprise(TypeEntreprise.MERE)
                .nom("Distribo Sarl")
                .codeFiliale("SIEGE")
                .build();
    }

    private FilialeCreateRequest request() {
        FilialeCreateRequest request = new FilialeCreateRequest();
        request.setNom("Boutique Akwa");
        request.setCodeFiliale("DLA01");
        request.setVille("Douala");
        request.setQuartier("Akwa");
        return request;
    }

    @Nested
    @DisplayName("POST /api/v1/groupe/filiales")
    class Creer {

        @Test
        @DisplayName("✅ Sous la limite, code disponible → filiale créée, parent = maison mère")
        void shouldCreateFilialeWhenUnderLimitAndCodeAvailable() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupeActif()));
            when(entrepriseRepository.countByGroupeIdAndSiteOperationnelTrueAndActifTrueAndSupprimeFalse(1L))
                    .thenReturn(3L);
            when(controleLimiteFilialesService.peutAjouterFiliale(3L, 15)).thenReturn(true);
            when(entrepriseRepository.existsByGroupeIdAndCodeFiliale(1L, "DLA01")).thenReturn(false);
            when(entrepriseRepository.findFirstByGroupeIdAndTypeEntreprise(1L, TypeEntreprise.MERE))
                    .thenReturn(Optional.of(maisonMere()));
            when(entrepriseRepository.save(any(Entreprise.class))).thenAnswer(inv -> {
                Entreprise e = inv.getArgument(0);
                e.setId(42L);
                e.setActif(true);
                e.setSiteOperationnel(true);
                return e;
            });

            FilialeResponse response = filialeService.creer(principal, request());

            assertThat(response.getId()).isEqualTo(42L);
            assertThat(response.getNom()).isEqualTo("Boutique Akwa");
            assertThat(response.getCodeFiliale()).isEqualTo("DLA01");
            assertThat(response.getVille()).isEqualTo("Douala");
            assertThat(response.getQuartier()).isEqualTo("Akwa");
            assertThat(response.getParentId()).isEqualTo(10L);
            assertThat(response.getActif()).isTrue();
            assertThat(response.getSiteOperationnel()).isTrue();
        }

        @Test
        @DisplayName("❌ Limite de filiales atteinte → GRP_FILIALE_LIMIT_REACHED (403)")
        void shouldThrowWhenLimitReached() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupeActif()));
            when(entrepriseRepository.countByGroupeIdAndSiteOperationnelTrueAndActifTrueAndSupprimeFalse(1L))
                    .thenReturn(15L);
            when(controleLimiteFilialesService.peutAjouterFiliale(15L, 15)).thenReturn(false);

            assertThatThrownBy(() -> filialeService.creer(principal, request()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GRP_FILIALE_LIMIT_REACHED);
        }

        @Test
        @DisplayName("❌ Code filiale déjà utilisé dans le groupe → RES_DUPLICATE_FILIALE_CODE (409)")
        void shouldThrowWhenCodeFilialeAlreadyExists() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupeActif()));
            when(entrepriseRepository.countByGroupeIdAndSiteOperationnelTrueAndActifTrueAndSupprimeFalse(1L))
                    .thenReturn(3L);
            when(controleLimiteFilialesService.peutAjouterFiliale(3L, 15)).thenReturn(true);
            when(entrepriseRepository.existsByGroupeIdAndCodeFiliale(1L, "DLA01")).thenReturn(true);

            assertThatThrownBy(() -> filialeService.creer(principal, request()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_DUPLICATE_FILIALE_CODE);
        }

        @Test
        @DisplayName("❌ Maison mère introuvable (invariant violé) → SYS_INTERNAL_ERROR")
        void shouldThrowWhenParentMereNotFound() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupeActif()));
            when(entrepriseRepository.countByGroupeIdAndSiteOperationnelTrueAndActifTrueAndSupprimeFalse(1L))
                    .thenReturn(3L);
            when(controleLimiteFilialesService.peutAjouterFiliale(3L, 15)).thenReturn(true);
            when(entrepriseRepository.existsByGroupeIdAndCodeFiliale(1L, "DLA01")).thenReturn(false);
            when(entrepriseRepository.findFirstByGroupeIdAndTypeEntreprise(1L, TypeEntreprise.MERE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> filialeService.creer(principal, request()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SYS_INTERNAL_ERROR);
        }

        @Test
        @DisplayName("❌ Groupe inexistant → RES_ENTITY_NOT_FOUND (404)")
        void shouldThrowNotFoundWhenGroupDoesNotExist() {
            StockMasterPrincipal principal = principalWithGroup(42L);
            when(groupeRepository.findById(42L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> filialeService.creer(principal, request()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ Groupe soft-supprimé → RES_ENTITY_NOT_FOUND")
        void shouldThrowNotFoundWhenGroupSoftDeleted() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            TenantGroup supprime = groupeActif();
            supprime.setSupprime(true);
            when(groupeRepository.findById(1L)).thenReturn(Optional.of(supprime));

            assertThatThrownBy(() -> filialeService.creer(principal, request()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ Principal sans groupId → GRP_CROSS_GROUP_FORBIDDEN (403)")
        void shouldThrowForbiddenWhenPrincipalHasNoGroup() {
            StockMasterPrincipal principal = principalWithGroup(null);

            assertThatThrownBy(() -> filialeService.creer(principal, request()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
    }
}
