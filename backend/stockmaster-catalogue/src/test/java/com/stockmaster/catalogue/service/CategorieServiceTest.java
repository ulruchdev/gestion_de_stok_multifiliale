package com.stockmaster.catalogue.service;

import com.stockmaster.catalogue.domain.entity.Categorie;
import com.stockmaster.catalogue.dto.request.CategorieCreateRequest;
import com.stockmaster.catalogue.repository.CategorieRepository;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * US-027 — {@code POST /api/v1/categories} : créer une catégorie.
 *
 * <p>Tests service : le catalogue est <b>group-scoped</b> (DEC-002, V5 §6) —
 * le {@code groupId} vient du JWT, jamais du corps ; unicité
 * {@code (group_id, code)} → 409 {@code RES_002} ; taux TVA restreint aux
 * valeurs supportées → 400 {@code CAT_001} ; RBAC en défense en profondeur.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategorieService — US-027 : POST /api/v1/categories")
class CategorieServiceTest {

    @Mock
    private CategorieRepository categorieRepository;

    @Mock
    private TenantGroupRepository tenantGroupRepository;

    @Spy
    private PerimetreCatalogueGuard perimetreCatalogueGuard = new PerimetreCatalogueGuard();

    @Spy
    private TauxTvaAutorises tauxTvaAutorises = new TauxTvaAutorises(
            Set.of(new BigDecimal("19.25"), new BigDecimal("0")));

    @InjectMocks
    private CategorieService categorieService;

    // ─── fixtures ───────────────────────────────────────────────────────────

    private static final Long GROUP_ID = 1L;
    private static final Long ENTREPRISE_ID = 10L;

    private StockMasterPrincipal principal(String role, Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
        // lenient : le catalogue est group-scoped (DEC-002) — le service ne lit
        // que groupId et role ; l'entrepriseId du JWT peut rester non consommé.
        lenient().when(claims.get("groupId", Long.class)).thenReturn(groupId);
        lenient().when(claims.get("entrepriseId", Long.class)).thenReturn(entrepriseId);
        lenient().when(claims.get("role", String.class)).thenReturn(role);
        return new StockMasterPrincipal(99L, claims);
    }

    private TenantGroup groupeActif() {
        TenantGroup groupe = TenantGroup.builder()
                .nomGroupe("Boutique CM")
                .build();
        groupe.setId(GROUP_ID);
        groupe.setActif(true);
        groupe.setSupprime(false);
        return groupe;
    }

    // ─── cas nominal ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Cas nominal")
    class CasNominal {

        @Test
        @DisplayName("✅ GESTIONNAIRE_STOCK crée une catégorie rattachée à SON groupe (JWT)")
        void shouldCreerParGestionnaireAvecGroupeDuJwt() {
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "ALIM"))
                    .thenReturn(false);
            when(categorieRepository.save(any(Categorie.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var response = categorieService.creer(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    new CategorieCreateRequest("ALIM", "Alimentation générale",
                            new BigDecimal("19.25")));

            assertThat(response.getCode()).isEqualTo("ALIM");
            assertThat(response.getTauxTva()).isEqualByComparingTo("19.25");

            ArgumentCaptor<Categorie> captor = ArgumentCaptor.forClass(Categorie.class);
            verify(categorieRepository).save(captor.capture());
            Categorie sauvegardee = captor.getValue();
            assertThat(sauvegardee.getGroupe().getId()).isEqualTo(GROUP_ID);
            assertThat(sauvegardee.getCode()).isEqualTo("ALIM");
            assertThat(sauvegardee.getDesignation()).isEqualTo("Alimentation générale");
            assertThat(sauvegardee.getSupprime()).isFalse();
        }

        @Test
        @DisplayName("✅ ADMIN_FILIALE peut créer une catégorie (rôle autorisé)")
        void shouldAutoriserAdminFiliale() {
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "BOIS"))
                    .thenReturn(false);
            when(categorieRepository.save(any(Categorie.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var response = categorieService.creer(
                    principal("ADMIN_FILIALE", GROUP_ID, ENTREPRISE_ID),
                    new CategorieCreateRequest("BOIS", "Boissons", new BigDecimal("19.25")));

            assertThat(response.getCode()).isEqualTo("BOIS");
        }

        @Test
        @DisplayName("✅ ADMIN_GROUPE peut créer une catégorie (rôle autorisé)")
        void shouldAutoriserAdminGroupe() {
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "HYG"))
                    .thenReturn(false);
            when(categorieRepository.save(any(Categorie.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var response = categorieService.creer(
                    principal("ADMIN_GROUPE", GROUP_ID, null),
                    new CategorieCreateRequest("HYG", "Hygiène", new BigDecimal("19.25")));

            assertThat(response.getCode()).isEqualTo("HYG");
        }

        @Test
        @DisplayName("✅ TVA 0 % (exonéré) acceptée")
        void shouldAccepterTauxZero() {
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "EXP"))
                    .thenReturn(false);
            when(categorieRepository.save(any(Categorie.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var response = categorieService.creer(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    new CategorieCreateRequest("EXP", "Export exonéré", BigDecimal.ZERO));

            assertThat(response.getTauxTva()).isEqualByComparingTo("0");
        }
    }

    // ─── RBAC — défense en profondeur ───────────────────────────────────────

    @Nested
    @DisplayName("RBAC — défense en profondeur (au-delà du @PreAuthorize)")
    class Rbac {

        @Test
        @DisplayName("❌ 403 — CAISSIER refusé avant toute I/O")
        void shouldRefuserCaissierAvantToutIo() {
            var ex = catchThrowableOfType(
                    () -> categorieService.creer(
                            principal("CAISSIER", GROUP_ID, ENTREPRISE_ID),
                            new CategorieCreateRequest("ALIM", "Alimentation", new BigDecimal("19.25"))),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
            verifyNoInteractionsRepositories();
        }

        @Test
        @DisplayName("❌ 403 — RESP_ACHATS refusé avant toute I/O")
        void shouldRefuserRespAchats() {
            var ex = catchThrowableOfType(
                    () -> categorieService.creer(
                            principal("RESP_ACHATS", GROUP_ID, ENTREPRISE_ID),
                            new CategorieCreateRequest("ALIM", "Alimentation", new BigDecimal("19.25"))),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
            verifyNoInteractionsRepositories();
        }

        @Test
        @DisplayName("❌ 403 — COMMERCIAL refusé avant toute I/O")
        void shouldRefuserCommercial() {
            var ex = catchThrowableOfType(
                    () -> categorieService.creer(
                            principal("COMMERCIAL", GROUP_ID, ENTREPRISE_ID),
                            new CategorieCreateRequest("ALIM", "Alimentation", new BigDecimal("19.25"))),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
            verifyNoInteractionsRepositories();
        }

        private void verifyNoInteractionsRepositories() {
            verify(categorieRepository, never()).save(any());
            verify(tenantGroupRepository, never()).findByIdAndSupprimeFalse(any());
        }
    }

    // ─── unicité (group_id, code) ───────────────────────────────────────────

    @Nested
    @DisplayName("Unicité (group_id, code) — 409 RES_002")
    class Unicite {

        @Test
        @DisplayName("❌ 409 — code déjà utilisé dans le groupe (groupe non chargé : short-circuit)")
        void shouldRefuserCodeDuplique() {
            when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "ALIM"))
                    .thenReturn(true);

            var ex = catchThrowableOfType(
                    () -> categorieService.creer(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                            new CategorieCreateRequest("ALIM", "Alimentation générale",
                                    new BigDecimal("19.25"))),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_DUPLICATE_CODE);
            verify(tenantGroupRepository, never()).findByIdAndSupprimeFalse(any());
            verify(categorieRepository, never()).save(any());
        }
    }

    // ─── groupe hôte ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Groupe hôte (fail-fast après unicité)")
    class GroupeHote {

        @Test
        @DisplayName("❌ 403 GRP_002 — groupe inexistant / soft-supprimé")
        void shouldRefuserGroupeInexistant() {
            when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "ALIM"))
                    .thenReturn(false);
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.empty());

            var ex = catchThrowableOfType(
                    () -> categorieService.creer(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                            new CategorieCreateRequest("ALIM", "Alimentation générale",
                                    new BigDecimal("19.25"))),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
            verify(categorieRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — groupe désactivé (plan expiré / suspension)")
        void shouldRefuserGroupeInactif() {
            TenantGroup groupe = groupeActif();
            groupe.setActif(false);
            when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "ALIM"))
                    .thenReturn(false);
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupe));

            var ex = catchThrowableOfType(
                    () -> categorieService.creer(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                            new CategorieCreateRequest("ALIM", "Alimentation générale",
                                    new BigDecimal("19.25"))),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
            verify(categorieRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — groupId absent du JWT (principal non groupé)")
        void shouldRefuserPrincipalSansGroupId() {
            var ex = catchThrowableOfType(
                    () -> categorieService.creer(
                            principal("GESTIONNAIRE_STOCK", null, ENTREPRISE_ID),
                            new CategorieCreateRequest("ALIM", "Alimentation générale",
                                    new BigDecimal("19.25"))),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
            verify(categorieRepository, never()).save(any());
        }
    }

    // ─── taux de TVA ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Taux de TVA — 400 CAT_001 (critère US-027)")
    class TauxTva {

        @Test
        @DisplayName("❌ 400 — taux non supporté (8 % n'est ni 0, ni 6/9.25/19.25)")
        void shouldRefuserTauxNonSupporte() {
            var ex = catchThrowableOfType(
                    () -> categorieService.creer(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                            new CategorieCreateRequest("ALIM", "Alimentation générale",
                                    new BigDecimal("8.00"))),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CAT_TAUX_TVA_INVALIDE);
            verify(categorieRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ 400 — taux négatif")
        void shouldRefuserTauxNegatif() {
            var ex = catchThrowableOfType(
                    () -> categorieService.creer(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                            new CategorieCreateRequest("ALIM", "Alimentation générale",
                                    new BigDecimal("-1"))),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CAT_TAUX_TVA_INVALIDE);
            verify(categorieRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ 400 — taux > 100 %")
        void shouldRefuserTauxSuperieurCent() {
            var ex = catchThrowableOfType(
                    () -> categorieService.creer(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                            new CategorieCreateRequest("ALIM", "Alimentation générale",
                                    new BigDecimal("120"))),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CAT_TAUX_TVA_INVALIDE);
            verify(categorieRepository, never()).save(any());
        }

        @Test
        @DisplayName("✅ taux réduit CONFIGURÉ (ex. 6,6 %) accepté — critère US-027 « taux réduit configuré »")
        void shouldAccepterTauxReduitConfigure() {
            CategorieService service = new CategorieService(categorieRepository, tenantGroupRepository,
                    new TauxTvaAutorises(Set.of(new BigDecimal("19.25"), new BigDecimal("0"),
                            new BigDecimal("6.6"))),
                    perimetreCatalogueGuard);
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "MED"))
                    .thenReturn(false);
            when(categorieRepository.save(any(Categorie.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var response = service.creer(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    new CategorieCreateRequest("MED", "Médicaments", new BigDecimal("6.60")));

            assertThat(response.getTauxTva()).isEqualByComparingTo("6.60");
        }
    }
}
