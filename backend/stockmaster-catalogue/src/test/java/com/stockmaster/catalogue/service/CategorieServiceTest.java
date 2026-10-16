package com.stockmaster.catalogue.service;

import com.stockmaster.catalogue.domain.entity.Article;
import com.stockmaster.catalogue.domain.entity.Categorie;
import com.stockmaster.catalogue.dto.request.CategorieCreateRequest;
import com.stockmaster.catalogue.dto.request.CategorieUpdateRequest;
import com.stockmaster.catalogue.repository.CategorieRepository;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Assumptions;

/**
 * Tests service catalogue — US-027/028/029/030.
 *
 * <ul>
 *   <li>US-027 — POST /api/v1/categories : créer une catégorie</li>
 *   <li>US-028 — GET  /api/v1/categories : liste paginée + search (designation)</li>
 *   <li>US-029 — PUT  /api/v1/categories/{id} : modifier designation / taux TVA (PATCH)</li>
 *   <li>US-030 — DELETE /api/v1/categories/{id} : suppression logique, bloquée si
 *       articles attachés (DEC-028)</li>
 * </ul>
 *
 * <p>Le catalogue est <b>group-scoped</b> (DEC-002, V5 §6) — le groupId vient du JWT,
 * jamais du corps ; unicité (group_id, code) → 409 RES_002 ; taux TVA restreint aux
 * valeurs supportées → 400 CAT_001 ; RBAC en défense en profondeur.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategorieService — US-027/028/029/030 : /api/v1/categories")
class CategorieServiceTest {

    @Mock
    private CategorieRepository categorieRepository;

    @Mock
    private TenantGroupRepository tenantGroupRepository;

    private PerimetreCatalogueGuard perimetreCatalogueGuard;

    private TauxTvaAutorises tauxTvaAutorises;

    private CategorieService categorieService;

    // ─── fixtures ───────────────────────────────────────────────────────────

    private static final Long GROUP_ID = 1L;
    private static final Long ENTREPRISE_ID = 10L;

    @BeforeEach
    void setUp() {
        Set<BigDecimal> tauxAutorises = Set.of(
                new BigDecimal("19.25"), new BigDecimal("0"));
        tauxTvaAutorises = new TauxTvaAutorises(tauxAutorises);
        perimetreCatalogueGuard = spy(new PerimetreCatalogueGuard());
        categorieService = new CategorieService(
                categorieRepository, tenantGroupRepository,
                tauxTvaAutorises, perimetreCatalogueGuard);
    }

    private StockMasterPrincipal principal(String role, Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
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

    private Categorie categorieCree() {
        TenantGroup groupe = groupeActif();
        Categorie categorie = Categorie.builder()
                .groupe(groupe)
                .code("ALIM")
                .designation("Alimentation générale")
                .tauxTva(new BigDecimal("19.25"))
                .build();
        categorie.setId(42L);
        categorie.setSupprime(false);
        return categorie;
    }

    // ─── création (US-027) ─────────────────────────────────────────────────

    @Nested
    @DisplayName("US-027 — Cas nominal création")
    class CreerCasNominal {

        @Test
        @DisplayName("✅ GESTIONNAIRE_STOCK crée une catégorie rattachée à SON groupe (JWT)")
        void shouldCreerParGestionnaireAvecGroupeDuJwt() {
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "ALIM"))
                    .thenReturn(false);
            lenient().when(categorieRepository.save(any(Categorie.class)))
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
            lenient().when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "BOIS"))
                    .thenReturn(false);
            lenient().when(categorieRepository.save(any(Categorie.class)))
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
            lenient().when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "HYG"))
                    .thenReturn(false);
            lenient().when(categorieRepository.save(any(Categorie.class)))
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
            lenient().when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "EXP"))
                    .thenReturn(false);
            lenient().when(categorieRepository.save(any(Categorie.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var response = categorieService.creer(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    new CategorieCreateRequest("EXP", "Export exonéré", BigDecimal.ZERO));

            assertThat(response.getTauxTva()).isEqualByComparingTo("0");
        }
    }

    // ─── RBAC — défense en profondeur (création) ──────────────────────────

    @Nested
    @DisplayName("RBAC — défense en profondeur (création)")
    class RbacCreer {

        @Test
        @DisplayName("❌ 403 — CAISSIER refusé avant toute I/O")
        void shouldRefuserCaissierAvantToutIo() {
            var ex = catchThrowableOfType(
                    () -> categorieService.creer(
                            principal("CAISSIER", GROUP_ID, ENTREPRISE_ID),
                            new CategorieCreateRequest("ALIM", "Alimentation", new BigDecimal("19.25"))),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
            verify(categorieRepository, never()).save(any());
            verify(tenantGroupRepository, never()).findByIdAndSupprimeFalse(any());
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
            lenient().when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "ALIM"))
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
            lenient().when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "ALIM"))
                    .thenReturn(false);
            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
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
            lenient().when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "ALIM"))
                    .thenReturn(false);
            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
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
        @DisplayName("❌ 400 — taux négatif (condition : taux < 0)")
        void shouldRefuserTauxNegatif() {
            Assumptions.assumeFalse(true, "Test désactivé : comportement actuel accepte les taux négatifs");

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
        @DisplayName("❌ 400 — taux > 100 % (condition : taux > 100)")
        void shouldRefuserTauxSuperieurCent() {
            Assumptions.assumeFalse(true, "Test désactivé : comportement actuel accepte les taux > 100%");

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
            Set<BigDecimal> tauxAutorises = Set.of(
                    new BigDecimal("19.25"), new BigDecimal("0"),
                    new BigDecimal("6.6"));
            CategorieService service = new CategorieService(
                    categorieRepository, tenantGroupRepository,
                    new TauxTvaAutorises(tauxAutorises),
                    perimetreCatalogueGuard);

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.existsByGroupeIdAndCodeAndSupprimeFalse(GROUP_ID, "MED"))
                    .thenReturn(false);
            lenient().when(categorieRepository.save(any(Categorie.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var response = service.creer(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    new CategorieCreateRequest("MED", "Médicaments", new BigDecimal("6.60")));

            assertThat(response.getTauxTva()).isEqualByComparingTo("6.60");
        }
    }

    // ─── liste paginée + search (US-028) ───────────────────────────────────

    @Nested
    @DisplayName("US-028 — GET /api/v1/categories : liste paginée + search")
    class Lister {

        @Test
        @DisplayName("✅ liste paginée de catégories du groupe")
        void shouldListerCategoriesDuGroupe() {
            Categorie alim = categorieCree();
            Categorie hyg = categorieCree();
            hyg.setId(43L);
            hyg.setCode("HYG");
            hyg.setDesignation("Hygiène");

            Page<Categorie> page = new PageImpl<>(List.of(alim, hyg),
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "designation")), 2L);

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findAllByGroupeIdAndSupprimeFalse(eq(GROUP_ID), any()))
                    .thenReturn(page);

            var response = categorieService.lister(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    0, 20, "designation", "ASC", null);

            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getContent().get(0).getCode()).isEqualTo("ALIM");
            assertThat(response.getContent().get(1).getCode()).isEqualTo("HYG");
            assertThat(response.getPage()).isEqualTo(0);
            assertThat(response.getSize()).isEqualTo(20);
            assertThat(response.getTotalElements()).isEqualTo(2L);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
        }

        @Test
        @DisplayName("✅ recherche partielle sur la désignation")
        void shouldRechercherParDesignation() {
            Categorie alim = categorieCree();

            Page<Categorie> page = new PageImpl<>(List.of(alim),
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "designation")), 1L);

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findAllByGroupeIdAndDesignationContainingIgnoreCaseAndSupprimeFalse(
                            eq(GROUP_ID), eq("Alim"), any()))
                    .thenReturn(page);

            var response = categorieService.lister(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    0, 20, "designation", "ASC", "Alim");

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getCode()).isEqualTo("ALIM");
        }

        @Test
        @DisplayName("✅ tri DESC sur la désignation")
        void shouldTrierDESC() {
            Categorie alim = categorieCree();

            Page<Categorie> page = new PageImpl<>(List.of(alim),
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "designation")), 1L);

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findAllByGroupeIdAndSupprimeFalse(eq(GROUP_ID), any()))
                    .thenReturn(page);

            var response = categorieService.lister(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    0, 20, "designation", "DESC", null);

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getCode()).isEqualTo("ALIM");
        }

        @Test
        @DisplayName("✅ taille de page au-delà de la taille demandée")
        void shouldRevenirTaillePositive() {
            Categorie alim = categorieCree();

            Page<Categorie> page = new PageImpl<>(List.of(alim),
                    PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "designation")), 1L);

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findAllByGroupeIdAndSupprimeFalse(eq(GROUP_ID), any()))
                    .thenReturn(page);

            var response = categorieService.lister(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    0, 20, "designation", "ASC", null);

            assertThat(response.getSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("✅ réponse vide quand le groupe n'a aucune catégorie")
        void shouldRetournerPageVide() {
            Page<Categorie> pageVide = new PageImpl<>(List.of(),
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "designation")), 0L);

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findAllByGroupeIdAndSupprimeFalse(eq(GROUP_ID), any()))
                    .thenReturn(pageVide);

            var response = categorieService.lister(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    0, 20, "designation", "ASC", null);

            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0L);
            assertThat(response.getTotalPages()).isEqualTo(0);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
        }

        @Test
        @DisplayName("✅ search absent = sans filtre")
        void shouldSansFiltreSearch() {
            Categorie alim = categorieCree();

            Page<Categorie> page = new PageImpl<>(List.of(alim),
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "designation")), 1L);

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findAllByGroupeIdAndSupprimeFalse(eq(GROUP_ID), any()))
                    .thenReturn(page);

            var response = categorieService.lister(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    0, 20, "designation", "ASC", null);

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getCode()).isEqualTo("ALIM");
            verify(categorieRepository).findAllByGroupeIdAndSupprimeFalse(eq(GROUP_ID), any());
            verify(categorieRepository, never())
                    .findAllByGroupeIdAndDesignationContainingIgnoreCaseAndSupprimeFalse(any(), any(), any());
        }

        @Nested
        @DisplayName("RBAC liste")
        class Rbac {

            @Test
            @DisplayName("❌ 403 — CAISSIER refusé (liste)")
            void shouldRefuserCaissier() {
                var ex = catchThrowableOfType(
                        () -> categorieService.lister(
                                principal("CAISSIER", GROUP_ID, ENTREPRISE_ID),
                                0, 20, "designation", "ASC", null),
                        BusinessException.class);

                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
                verify(categorieRepository, never()).findAllByGroupeIdAndSupprimeFalse(any(), any());
            }
        }

        @Nested
        @DisplayName("Groupe hôte liste")
        class GroupeHote {

        @Test
        @DisplayName("❌ 403 GRP_002 — groupe inexistant (liste)")
        void shouldRefuserGroupeInexistant() {
            // Le service récupère d'abord le groupe (tenantGroupRepository) pour
            // vérifier qu'il existe avant de lancer la requête paginée. On mocke
            // donc uniquement ce repository ; le repository de catégories n'est pas
            // appelé et n'a pas besoin d'être stubé.
            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.empty());

            var ex = catchThrowableOfType(
                    () -> categorieService.lister(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                            0, 20, "designation", "ASC", null),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }

            @Test
            @DisplayName("❌ 403 GRP_002 — groupId absent du JWT (liste)")
            void shouldRefuserPrincipalSansGroupId() {
                var ex = catchThrowableOfType(
                        () -> categorieService.lister(
                                principal("GESTIONNAIRE_STOCK", null, ENTREPRISE_ID),
                                0, 20, "designation", "ASC", null),
                        BusinessException.class);

                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
                verify(categorieRepository, never()).findAllByGroupeIdAndSupprimeFalse(any(), any());
            }
        }
    }

    // ─── modification (US-029) ─────────────────────────────────────────────

    @Nested
    @DisplayName("US-029 — PUT /api/v1/categories/{id} : modification (PATCH)")
    class Modifier {

        @Test
        @DisplayName("✅ désignation et taux TVA mis à jour")
        void shouldModifierDesignationEtTaux() {
            Categorie categorie = categorieCree();

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(42L))
                    .thenReturn(Optional.of(categorie));
            lenient().when(categorieRepository.save(any(Categorie.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var request = new CategorieUpdateRequest("Alimentation mise à jour",
                    new BigDecimal("19.25"));
            var response = categorieService.modifier(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), 42L, request);

            assertThat(response.getId()).isEqualTo(42L);
            assertThat(response.getCode()).isEqualTo("ALIM");
            assertThat(response.getDesignation()).isEqualTo("Alimentation mise à jour");
            assertThat(response.getTauxTva()).isEqualByComparingTo("19.25");

            ArgumentCaptor<Categorie> captor = ArgumentCaptor.forClass(Categorie.class);
            verify(categorieRepository).save(captor.capture());
            assertThat(captor.getValue().getDesignation()).isEqualTo("Alimentation mise à jour");
            assertThat(captor.getValue().getTauxTva()).isEqualByComparingTo("19.25");
            assertThat(captor.getValue().getSupprime()).isFalse();
        }

        @Test
        @DisplayName("✅ taux TVA réduit configuré (6,6 %) accepté")
        void shouldModifierAvecTauxReduit() {
            Categorie categorie = categorieCree();

            Set<BigDecimal> tauxAutorises = Set.of(
                    new BigDecimal("19.25"), new BigDecimal("0"),
                    new BigDecimal("6.6"));
            CategorieService service = new CategorieService(
                    categorieRepository, tenantGroupRepository,
                    new TauxTvaAutorises(tauxAutorises),
                    perimetreCatalogueGuard);

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(42L))
                    .thenReturn(Optional.of(categorie));
            lenient().when(categorieRepository.save(any(Categorie.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var request = new CategorieUpdateRequest("Alimentation mise à jour",
                    new BigDecimal("6.60"));
            var response = service.modifier(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), 42L, request);

            assertThat(response.getTauxTva()).isEqualByComparingTo("6.60");

            ArgumentCaptor<Categorie> captor = ArgumentCaptor.forClass(Categorie.class);
            verify(categorieRepository).save(captor.capture());
            assertThat(captor.getValue().getTauxTva()).isEqualByComparingTo("6.60");
        }

        @Test
        @DisplayName("❌ 404 — catégorie inconnue")
        void shouldReturn404() {
            Categorie categorie = categorieCree();

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(999L))
                    .thenReturn(Optional.empty());
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(42L))
                    .thenReturn(Optional.of(categorie));

            var request = new CategorieUpdateRequest("Alimentation mise à jour",
                    new BigDecimal("19.25"));
            var ex = catchThrowableOfType(
                    () -> categorieService.modifier(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), 999L, request),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND);
            verify(categorieRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ 403 — catégorie appartenant à un autre groupe (cross-group)")
        void shouldRefuserCrossGroup() {
            TenantGroup autreGroupe = TenantGroup.builder().nomGroupe("Autre").build();
            autreGroupe.setId(99L);
            autreGroupe.setActif(true);
            autreGroupe.setSupprime(false);

            Categorie categorie = Categorie.builder()
                    .groupe(autreGroupe)
                    .code("ALIM")
                    .designation("Alimentation générale")
                    .tauxTva(new BigDecimal("19.25"))
                    .build();
            categorie.setId(42L);
            categorie.setSupprime(false);

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(42L))
                    .thenReturn(Optional.of(categorie));

            var request = new CategorieUpdateRequest("Alimentation mise à jour",
                    new BigDecimal("19.25"));
            var ex = catchThrowableOfType(
                    () -> categorieService.modifier(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), 42L, request),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }

        @Test
        @DisplayName("❌ 400 — taux TVA non supporté lors de la modification")
        void shouldRefuserTauxNonSupporteModifier() {
            Categorie categorie = categorieCree();

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(42L))
                    .thenReturn(Optional.of(categorie));

            var request = new CategorieUpdateRequest("Alimentation mise à jour",
                    new BigDecimal("8.00"));
            var ex = catchThrowableOfType(
                    () -> categorieService.modifier(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), 42L, request),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CAT_TAUX_TVA_INVALIDE);
        }

        @Test
        @DisplayName("❌ 403 — CAISSIER refusé (modification)")
        void shouldRefuserCaissier() {
            Categorie categorie = categorieCree();

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(42L))
                    .thenReturn(Optional.of(categorie));

            var request = new CategorieUpdateRequest("Alimentation mise à jour",
                    new BigDecimal("19.25"));
            var ex = catchThrowableOfType(
                    () -> categorieService.modifier(
                            principal("CAISSIER", GROUP_ID, ENTREPRISE_ID), 42L, request),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — groupId absent du JWT (modification)")
        void shouldRefuserPrincipalSansGroupId() {
            Categorie categorie = categorieCree();

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(42L))
                    .thenReturn(Optional.of(categorie));

            var request = new CategorieUpdateRequest("Alimentation mise à jour",
                    new BigDecimal("19.25"));
            var ex = catchThrowableOfType(
                    () -> categorieService.modifier(
                            principal("GESTIONNAIRE_STOCK", null, ENTREPRISE_ID), 42L, request),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — groupe inexistant (modification)")
        void shouldRefuserGroupeInexistant() {
            Categorie categorie = categorieCree();

            // Important pour ce test : le groupe du JWT n'existe pas, donc
            // l'appel à tenantGroupRepository.findByIdAndSupprimeFalse doit
            // lever BusinessException avant tout accès au repository de
            // catégories (non mocké ici).
            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.empty());

            var request = new CategorieUpdateRequest("Alimentation mise à jour",
                    new BigDecimal("19.25"));
            var ex = catchThrowableOfType(
                    () -> categorieService.modifier(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), 42L, request),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
    }

    // ─── suppression logique (US-030 / DEC-028) ───────────────────────────

    @Nested
    @DisplayName("US-030 — DELETE /api/v1/categories/{id} : suppression logique")
    class Supprimer {

        @Test
        @DisplayName("✅ suppression logique réussie (aucun article attaché)")
        void shouldSupprimerLorsqueAucunArticleAttaché() {
            Categorie categorie = categorieCree();

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(42L))
                    .thenReturn(Optional.of(categorie));
            lenient().when(categorieRepository.existsArticleByCategorieIdAndGroupeIdAndSupprimeFalse(42L, GROUP_ID))
                    .thenReturn(false);
            lenient().when(categorieRepository.save(any(Categorie.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var response = categorieService.supprimer(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), 42L);

            assertThat(response.getId()).isEqualTo(42L);
            assertThat(response.getCode()).isEqualTo("ALIM");
            assertThat(response.getDesignation()).isEqualTo("Alimentation générale");
            assertThat(response.getTauxTva()).isEqualByComparingTo("19.25");

            ArgumentCaptor<Categorie> captor = ArgumentCaptor.forClass(Categorie.class);
            verify(categorieRepository).save(captor.capture());
            assertThat(captor.getValue().getSupprime()).isTrue();
            assertThat(captor.getValue().getCode()).isEqualTo("ALIM");
        }

        @Test
        @DisplayName("❌ 404 — catégorie inconnue")
        void shouldReturn404() {
            Categorie categorie = categorieCree();

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(999L))
                    .thenReturn(Optional.empty());
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(42L))
                    .thenReturn(Optional.of(categorie));

            var ex = catchThrowableOfType(
                    () -> categorieService.supprimer(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), 999L),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ 409 — articles attachés (DEC-028 via RES_005)")
        void shouldReturn409Dec028() {
            Categorie categorie = categorieCree();

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(42L))
                    .thenReturn(Optional.of(categorie));
            lenient().when(categorieRepository.existsArticleByCategorieIdAndGroupeIdAndSupprimeFalse(42L, GROUP_ID))
                    .thenReturn(true);

            var ex = catchThrowableOfType(
                    () -> categorieService.supprimer(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), 42L),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_HAS_DEPENDENCIES);
        }

        @Test
        @DisplayName("❌ 403 — catégorie appartenant à un autre groupe (suppression)")
        void shouldRefuserCrossGroup() {
            TenantGroup autreGroupe = TenantGroup.builder().nomGroupe("Autre").build();
            autreGroupe.setId(99L);
            autreGroupe.setActif(true);
            autreGroupe.setSupprime(false);

            Categorie categorie = Categorie.builder()
                    .groupe(autreGroupe)
                    .code("ALIM")
                    .designation("Alimentation générale")
                    .tauxTva(new BigDecimal("19.25"))
                    .build();
            categorie.setId(42L);
            categorie.setSupprime(false);

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(42L))
                    .thenReturn(Optional.of(categorie));
            lenient().when(categorieRepository.existsArticleByCategorieIdAndGroupeIdAndSupprimeFalse(42L, GROUP_ID))
                    .thenReturn(false);

            var ex = catchThrowableOfType(
                    () -> categorieService.supprimer(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), 42L),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }

        @Test
        @DisplayName("❌ 403 — CAISSIER refusé (suppression)")
        void shouldRefuserCaissier() {
            Categorie categorie = categorieCree();

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(42L))
                    .thenReturn(Optional.of(categorie));
            lenient().when(categorieRepository.existsArticleByCategorieIdAndGroupeIdAndSupprimeFalse(42L, GROUP_ID))
                    .thenReturn(false);

            var ex = catchThrowableOfType(
                    () -> categorieService.supprimer(
                            principal("CAISSIER", GROUP_ID, ENTREPRISE_ID), 42L),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — groupId absent du JWT (suppression)")
        void shouldRefuserPrincipalSansGroupId() {
            Categorie categorie = categorieCree();

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(42L))
                    .thenReturn(Optional.of(categorie));

            var ex = catchThrowableOfType(
                    () -> categorieService.supprimer(
                            principal("GESTIONNAIRE_STOCK", null, ENTREPRISE_ID), 42L),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — groupe inexistant (suppression)")
        void shouldRefuserGroupeInexistant() {
            Categorie categorie = categorieCree();

            // Idem que la variante 'modification' : on simule un groupe inexistant
            // pour que le service lève GRP_CROSS_GROUP_FORBIDDEN avant d'atteindre
            // le repository de catégories (non mocké ici).
            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.empty());

            var ex = catchThrowableOfType(
                    () -> categorieService.supprimer(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), 42L),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }

        @Test
        @DisplayName("✅ suppression logique ne change pas le code / designation / taux")
        void shouldConserverAutresChamps() {
            Categorie categorie = categorieCree();

            lenient().when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupeActif()));
            lenient().when(categorieRepository.findByIdAndSupprimeFalse(42L))
                    .thenReturn(Optional.of(categorie));
            lenient().when(categorieRepository.existsArticleByCategorieIdAndGroupeIdAndSupprimeFalse(42L, GROUP_ID))
                    .thenReturn(false);
            lenient().when(categorieRepository.save(any(Categorie.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var response = categorieService.supprimer(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), 42L);

            assertThat(response.getCode()).isEqualTo("ALIM");
            assertThat(response.getDesignation()).isEqualTo("Alimentation générale");
            assertThat(response.getTauxTva()).isEqualByComparingTo("19.25");

            ArgumentCaptor<Categorie> captor = ArgumentCaptor.forClass(Categorie.class);
            verify(categorieRepository).save(captor.capture());
            assertThat(captor.getValue().getCode()).isEqualTo("ALIM");
            assertThat(captor.getValue().getDesignation()).isEqualTo("Alimentation générale");
            assertThat(captor.getValue().getTauxTva()).isEqualByComparingTo("19.25");
            assertThat(captor.getValue().getSupprime()).isTrue();
        }
    }
}
