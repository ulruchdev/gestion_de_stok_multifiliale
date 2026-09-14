package com.stockmaster.groupe.service;

import com.stockmaster.groupe.dto.request.GroupeUpdateRequest;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.domain.enums.PlanAbonnement;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.shared.storage.MinioService;
import com.stockmaster.groupe.dto.response.GroupeResponse;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * US-015 — Consulter les informations du groupe (P0).
 * US-014 — Modifier partiellement le groupe : nom, logo (MinIO), infos fiscales (P0).
 *
 * <p>Le groupe consulté/modifié est TOUJOURS celui du JWT ({@code groupId} du principal) —
 * les endpoints ne prennent aucun identifiant, l'isolation multi-tenant est structurelle.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GroupeService")
class GroupeServiceTest {

    @Mock
    private TenantGroupRepository groupeRepository;

    @Mock
    private EntrepriseRepository entrepriseRepository;

    @Mock
    private MinioService minioService;

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

    @Nested
    @DisplayName("PUT /api/v1/groupe (US-014)")
    class Modifier {

        @Test
        @DisplayName("✅ Modification partielle du nom seul → autres champs inchangés, pas d'upload MinIO")
        void shouldUpdateOnlyNomGroupeWhenOnlyNameProvided() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupeActif()));
            when(groupeRepository.existsByNomGroupeAndSupprimeFalseAndIdNot("Nouveau Nom", 1L)).thenReturn(false);
            when(groupeRepository.save(any(TenantGroup.class))).thenAnswer(inv -> inv.getArgument(0));
            when(entrepriseRepository.countByGroupeIdAndTypeEntrepriseAndActifTrueAndSupprimeFalse(1L,
                    com.stockmaster.shared.domain.enums.TypeEntreprise.FILIALE)).thenReturn(7L);

            GroupeUpdateRequest request = new GroupeUpdateRequest();
            request.setNomGroupe("Nouveau Nom");

            GroupeResponse response = groupeService.modifier(principal, request);

            assertThat(response.getNomGroupe()).isEqualTo("Nouveau Nom");
            assertThat(response.getPlanAbonnement()).isEqualTo(PlanAbonnement.PRO);
            verify(minioService, never()).uploadImage(anyString(), any());
        }

        @Test
        @DisplayName("✅ Logo fourni → uploadé vers MinIO, URL persistée")
        void shouldUploadLogoToMinioWhenLogoProvided() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupeActif()));
            when(groupeRepository.save(any(TenantGroup.class))).thenAnswer(inv -> inv.getArgument(0));
            when(entrepriseRepository.countByGroupeIdAndTypeEntrepriseAndActifTrueAndSupprimeFalse(1L,
                    com.stockmaster.shared.domain.enums.TypeEntreprise.FILIALE)).thenReturn(7L);
            MockMultipartFile logo = new MockMultipartFile("logo", "logo.png", "image/png", "data".getBytes());
            when(minioService.uploadImage(eq("groupe/1"), eq(logo)))
                    .thenReturn("http://localhost:9000/stockmaster/groupe/1/abc.png");

            GroupeUpdateRequest request = new GroupeUpdateRequest();
            request.setLogo(logo);

            GroupeResponse response = groupeService.modifier(principal, request);

            assertThat(response.getLogo()).isEqualTo("http://localhost:9000/stockmaster/groupe/1/abc.png");
        }

        @Test
        @DisplayName("✅ Raison sociale + NIF fournis, NIF disponible → les deux mis à jour")
        void shouldUpdateRaisonSocialeAndNifWhenAvailable() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupeActif()));
            when(groupeRepository.existsByNifAndSupprimeFalseAndIdNot("M123456789", 1L)).thenReturn(false);
            when(groupeRepository.save(any(TenantGroup.class))).thenAnswer(inv -> inv.getArgument(0));
            when(entrepriseRepository.countByGroupeIdAndTypeEntrepriseAndActifTrueAndSupprimeFalse(1L,
                    com.stockmaster.shared.domain.enums.TypeEntreprise.FILIALE)).thenReturn(7L);

            GroupeUpdateRequest request = new GroupeUpdateRequest();
            request.setRaisonSociale("Distribo Sarl Cameroun");
            request.setNif("M123456789");

            GroupeResponse response = groupeService.modifier(principal, request);

            assertThat(response.getRaisonSociale()).isEqualTo("Distribo Sarl Cameroun");
            assertThat(response.getNif()).isEqualTo("M123456789");
        }

        @Test
        @DisplayName("❌ Nom déjà pris par un autre groupe → GRP_DUPLICATE_NOM_GROUPE (409)")
        void shouldThrowWhenNomGroupeAlreadyTaken() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupeActif()));
            when(groupeRepository.existsByNomGroupeAndSupprimeFalseAndIdNot("Deja Pris", 1L)).thenReturn(true);

            GroupeUpdateRequest request = new GroupeUpdateRequest();
            request.setNomGroupe("Deja Pris");

            assertThatThrownBy(() -> groupeService.modifier(principal, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GRP_DUPLICATE_NOM_GROUPE);
        }

        @Test
        @DisplayName("❌ NIF déjà pris par un autre groupe → RES_DUPLICATE_NIF (409)")
        void shouldThrowWhenNifAlreadyTaken() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(groupeRepository.findById(1L)).thenReturn(Optional.of(groupeActif()));
            when(groupeRepository.existsByNifAndSupprimeFalseAndIdNot("M999", 1L)).thenReturn(true);

            GroupeUpdateRequest request = new GroupeUpdateRequest();
            request.setNif("M999");

            assertThatThrownBy(() -> groupeService.modifier(principal, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_DUPLICATE_NIF);
        }

        @Test
        @DisplayName("❌ Groupe inexistant → RES_ENTITY_NOT_FOUND (404)")
        void shouldThrowNotFoundWhenGroupDoesNotExistOnModify() {
            StockMasterPrincipal principal = principalWithGroup(42L);
            when(groupeRepository.findById(42L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> groupeService.modifier(principal, new GroupeUpdateRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ Principal sans groupId → GRP_CROSS_GROUP_FORBIDDEN (403)")
        void shouldThrowForbiddenWhenPrincipalHasNoGroupOnModify() {
            StockMasterPrincipal principal = principalWithGroup(null);

            assertThatThrownBy(() -> groupeService.modifier(principal, new GroupeUpdateRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
    }
}
