package com.stockmaster.groupe.service;

import com.stockmaster.groupe.dto.request.FilialeCreateRequest;
import com.stockmaster.groupe.dto.request.FilialeUpdateRequest;
import com.stockmaster.groupe.dto.response.FilialeResponse;
import com.stockmaster.shared.config.PaginationProperties;
import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.domain.enums.PlanAbonnement;
import com.stockmaster.shared.domain.enums.TypeEntreprise;
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.EntrepriseRepository;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.repository.UtilisateurRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * US-016 — Créer une filiale dans le groupe (P0).
 * US-017 — Lister les filiales du groupe, paginé (P0).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FilialeService")
class FilialeServiceTest {

    @Mock
    private TenantGroupRepository groupeRepository;

    @Mock
    private EntrepriseRepository entrepriseRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private ControleLimiteFilialesService controleLimiteFilialesService;

    @Mock
    private PaginationProperties paginationProperties;

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
            assertThat(response.getNombreEmployes()).isZero();
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

    @Nested
    @DisplayName("GET /api/v1/groupe/filiales (US-017)")
    class Lister {

        private Entreprise filiale(Long id, String nom) {
            return Entreprise.builder()
                    .id(id).typeEntreprise(TypeEntreprise.FILIALE).nom(nom)
                    .codeFiliale("C" + id).adresseVille("Douala").adresseQuartier("Akwa")
                    .actif(true).siteOperationnel(true)
                    .build();
        }

        @Test
        @DisplayName("✅ Retourne la page des filiales du groupe avec parentId et nombre d'employés")
        void shouldReturnPagedFilialesWithParentAndEmployeeCount() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(paginationProperties.getMaxPageSize()).thenReturn(100);
            when(entrepriseRepository.findFirstByGroupeIdAndTypeEntreprise(1L, TypeEntreprise.MERE))
                    .thenReturn(Optional.of(maisonMere()));
            Pageable demande = PageRequest.of(0, 20);
            Entreprise f1 = filiale(42L, "Boutique Akwa");
            when(entrepriseRepository.findFilialesDuGroupe(1L, TypeEntreprise.FILIALE, null, null, demande))
                    .thenReturn(new PageImpl<>(List.of(f1), demande, 1));
            when(utilisateurRepository.countByEntrepriseIdAndSupprimeFalse(42L)).thenReturn(3L);

            PageResponse<FilialeResponse> response = filialeService.lister(principal, null, null, demande);

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
            FilialeResponse item = response.getContent().get(0);
            assertThat(item.getId()).isEqualTo(42L);
            assertThat(item.getNom()).isEqualTo("Boutique Akwa");
            assertThat(item.getParentId()).isEqualTo(10L);
            assertThat(item.getNombreEmployes()).isEqualTo(3L);
        }

        @Test
        @DisplayName("✅ Transmet les filtres actif/ville tels quels au repository")
        void shouldForwardActifAndVilleFilters() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(paginationProperties.getMaxPageSize()).thenReturn(100);
            when(entrepriseRepository.findFirstByGroupeIdAndTypeEntreprise(1L, TypeEntreprise.MERE))
                    .thenReturn(Optional.of(maisonMere()));
            Pageable demande = PageRequest.of(0, 20);
            when(entrepriseRepository.findFilialesDuGroupe(1L, TypeEntreprise.FILIALE, true, "Douala", demande))
                    .thenReturn(new PageImpl<>(List.of(), demande, 0));

            filialeService.lister(principal, true, "Douala", demande);

            org.mockito.Mockito.verify(entrepriseRepository)
                    .findFilialesDuGroupe(1L, TypeEntreprise.FILIALE, true, "Douala", demande);
        }

        @Test
        @DisplayName("✅ Taille de page demandée bornée par stockmaster.pagination.max-page-size")
        void shouldCapPageSizeAtConfiguredMax() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(paginationProperties.getMaxPageSize()).thenReturn(100);
            when(entrepriseRepository.findFirstByGroupeIdAndTypeEntreprise(1L, TypeEntreprise.MERE))
                    .thenReturn(Optional.empty());
            when(entrepriseRepository.findFilialesDuGroupe(any(), any(), any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of()));

            filialeService.lister(principal, null, null, PageRequest.of(0, 500));

            org.mockito.ArgumentCaptor<Pageable> captor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
            org.mockito.Mockito.verify(entrepriseRepository)
                    .findFilialesDuGroupe(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq(TypeEntreprise.FILIALE),
                            any(), any(), captor.capture());
            assertThat(captor.getValue().getPageSize()).isEqualTo(100);
        }

        @Test
        @DisplayName("❌ Principal sans groupId → GRP_CROSS_GROUP_FORBIDDEN (403)")
        void shouldThrowForbiddenWhenPrincipalHasNoGroup() {
            StockMasterPrincipal principal = principalWithGroup(null);

            assertThatThrownBy(() -> filialeService.lister(principal, null, null, PageRequest.of(0, 20)))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/groupe/filiales/{id} (US-018)")
    class Modifier {

        private Entreprise filialeExistante(Long groupeId) {
            return Entreprise.builder()
                    .id(42L)
                    .groupe(TenantGroup.builder().id(groupeId).build())
                    .typeEntreprise(TypeEntreprise.FILIALE)
                    .nom("Boutique Akwa")
                    .codeFiliale("DLA01")
                    .adresseVille("Douala")
                    .adresseQuartier("Akwa")
                    .actif(true)
                    .siteOperationnel(true)
                    .supprime(false)
                    .build();
        }

        @Test
        @DisplayName("✅ Nom seul modifié → autres champs inchangés")
        void shouldUpdateOnlyNomWhenOnlyNameProvided() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(entrepriseRepository.findById(42L)).thenReturn(Optional.of(filialeExistante(1L)));
            when(entrepriseRepository.save(any(Entreprise.class))).thenAnswer(inv -> inv.getArgument(0));
            when(entrepriseRepository.findFirstByGroupeIdAndTypeEntreprise(1L, TypeEntreprise.MERE))
                    .thenReturn(Optional.of(maisonMere()));
            when(utilisateurRepository.countByEntrepriseIdAndSupprimeFalse(42L)).thenReturn(2L);

            FilialeUpdateRequest request = new FilialeUpdateRequest();
            request.setNom("Boutique Bonanjo");

            FilialeResponse response = filialeService.modifier(principal, 42L, request);

            assertThat(response.getNom()).isEqualTo("Boutique Bonanjo");
            assertThat(response.getCodeFiliale()).isEqualTo("DLA01");
            assertThat(response.getParentId()).isEqualTo(10L);
            assertThat(response.getNombreEmployes()).isEqualTo(2L);
        }

        @Test
        @DisplayName("✅ Nouveau codeFiliale disponible → mis à jour")
        void shouldUpdateCodeFilialeWhenAvailable() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(entrepriseRepository.findById(42L)).thenReturn(Optional.of(filialeExistante(1L)));
            when(entrepriseRepository.existsByGroupeIdAndCodeFilialeAndIdNot(1L, "DLA02", 42L)).thenReturn(false);
            when(entrepriseRepository.save(any(Entreprise.class))).thenAnswer(inv -> inv.getArgument(0));
            when(entrepriseRepository.findFirstByGroupeIdAndTypeEntreprise(1L, TypeEntreprise.MERE))
                    .thenReturn(Optional.of(maisonMere()));
            when(utilisateurRepository.countByEntrepriseIdAndSupprimeFalse(42L)).thenReturn(0L);

            FilialeUpdateRequest request = new FilialeUpdateRequest();
            request.setCodeFiliale("DLA02");

            FilialeResponse response = filialeService.modifier(principal, 42L, request);

            assertThat(response.getCodeFiliale()).isEqualTo("DLA02");
        }

        @Test
        @DisplayName("❌ Nouveau codeFiliale déjà pris par une autre filiale → RES_DUPLICATE_FILIALE_CODE (409)")
        void shouldThrowWhenCodeFilialeAlreadyTakenByAnother() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(entrepriseRepository.findById(42L)).thenReturn(Optional.of(filialeExistante(1L)));
            when(entrepriseRepository.existsByGroupeIdAndCodeFilialeAndIdNot(1L, "DEJA", 42L)).thenReturn(true);

            FilialeUpdateRequest request = new FilialeUpdateRequest();
            request.setCodeFiliale("DEJA");

            assertThatThrownBy(() -> filialeService.modifier(principal, 42L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_DUPLICATE_FILIALE_CODE);
        }

        @Test
        @DisplayName("❌ Filiale d'un autre groupe → RES_ENTITY_NOT_FOUND (404, jamais révéler l'existence)")
        void shouldThrowNotFoundWhenFilialeBelongsToAnotherGroup() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(entrepriseRepository.findById(42L)).thenReturn(Optional.of(filialeExistante(99L)));

            assertThatThrownBy(() -> filialeService.modifier(principal, 42L, new FilialeUpdateRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ Filiale inexistante → RES_ENTITY_NOT_FOUND (404)")
        void shouldThrowNotFoundWhenFilialeDoesNotExist() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(entrepriseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> filialeService.modifier(principal, 999L, new FilialeUpdateRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ Filiale soft-supprimée → RES_ENTITY_NOT_FOUND")
        void shouldThrowNotFoundWhenFilialeSoftDeleted() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            Entreprise supprimee = filialeExistante(1L);
            supprimee.setSupprime(true);
            when(entrepriseRepository.findById(42L)).thenReturn(Optional.of(supprimee));

            assertThatThrownBy(() -> filialeService.modifier(principal, 42L, new FilialeUpdateRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ Id référence la maison mère (pas une filiale) → RES_ENTITY_NOT_FOUND")
        void shouldThrowNotFoundWhenIdIsMaisonMere() {
            StockMasterPrincipal principal = principalWithGroup(1L);
            when(entrepriseRepository.findById(10L)).thenReturn(Optional.of(maisonMere()));

            assertThatThrownBy(() -> filialeService.modifier(principal, 10L, new FilialeUpdateRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ Principal sans groupId → GRP_CROSS_GROUP_FORBIDDEN (403)")
        void shouldThrowForbiddenWhenPrincipalHasNoGroup() {
            StockMasterPrincipal principal = principalWithGroup(null);

            assertThatThrownBy(() -> filialeService.modifier(principal, 42L, new FilialeUpdateRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
    }
}
