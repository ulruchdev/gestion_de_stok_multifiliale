package com.stockmaster.catalogue.service;

import com.stockmaster.catalogue.domain.entity.Article;
import com.stockmaster.catalogue.domain.entity.Categorie;
import com.stockmaster.catalogue.dto.request.ArticleCreateRequest;
import com.stockmaster.catalogue.dto.response.ArticleResponse;
import com.stockmaster.catalogue.repository.ArticleRepository;
import com.stockmaster.catalogue.repository.CategorieRepository;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.shared.service.CalculTvaService;
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
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests service catalogue — US-031 « Créer un article ».
 *
 * <p>POST /api/v1/articles — group-scoped (DEC-002, V5 §6) : le groupId vient
 * du JWT, jamais du corps. TTC calculé côté serveur ({@code DEC-003}, jamais
 * saisi) via {@code CalculTvaService} ; taux TVA hérité de la catégorie si non
 * fourni, sinon restreint aux valeurs supportées ({@code CAT_001}) ; unicité
 * (group_id, code_article) → 409 RES_002 ; seuil alerte 0 = alerte désactivée
 * ({@code DEC-023}) ; prix d'achat ≥ prix de vente = avertissement non
 * bloquant ({@code DEC-029}).</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleService — US-031 : POST /api/v1/articles")
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CategorieRepository categorieRepository;

    @Mock
    private TenantGroupRepository tenantGroupRepository;

    private TauxTvaAutorises tauxTvaAutorises;
    private PerimetreCatalogueGuard perimetreCatalogueGuard;
    private CalculTvaService calculTvaService;
    private ArticleService articleService;

    private static final Long GROUP_ID = 1L;
    private static final Long CATEGORIE_ID = 3L;

    @BeforeEach
    void setUp() {
        Set<BigDecimal> tauxAutorises = Set.of(
                new BigDecimal("19.25"), new BigDecimal("0"));
        tauxTvaAutorises = new TauxTvaAutorises(tauxAutorises);
        perimetreCatalogueGuard = new PerimetreCatalogueGuard();
        calculTvaService = new CalculTvaService();
        articleService = new ArticleService(
                articleRepository, categorieRepository, tenantGroupRepository,
                tauxTvaAutorises, perimetreCatalogueGuard, calculTvaService);
    }

    // ─── fixtures ───────────────────────────────────────────────────────────

    private StockMasterPrincipal principal(String role, Long groupId) {
        Claims claims = mock(Claims.class);
        lenient().when(claims.get("groupId", Long.class)).thenReturn(groupId);
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

    private Categorie categorieDuGroupe(TenantGroup groupe) {
        Categorie categorie = Categorie.builder()
                .groupe(groupe)
                .code("CAT-RIZ")
                .designation("Riz et céréales")
                .tauxTva(new BigDecimal("19.25"))
                .build();
        categorie.setId(CATEGORIE_ID);
        return categorie;
    }

    private ArticleCreateRequest requeteValide() {
        return new ArticleCreateRequest(
                "RIZ50KG", "Riz parfumé 50 kg", CATEGORIE_ID,
                15000, 18500, new BigDecimal("19.25"), new BigDecimal("10"));
    }

    // ─── cas nominaux ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("✅ Cas nominaux")
    class Nominale {

        @Test
        @DisplayName("✅ Crée l'article, TTC calculé côté serveur et marge à la volée (18500 × 1.1925 = 22061)")
        void shouldCreerArticleWithTtcCalculated() {
            TenantGroup groupe = groupeActif();
            Categorie categorie = categorieDuGroupe(groupe);
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG")).thenReturn(false);
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID)).thenReturn(Optional.of(categorie));
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID)).thenReturn(Optional.of(groupe));
            when(articleRepository.save(any(Article.class))).thenAnswer(inv -> {
                Article a = inv.getArgument(0);
                a.setId(7L);
                return a;
            });

            ArticleResponse reponse = articleService.creer(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID), requeteValide());

            assertThat(reponse.getId()).isEqualTo(7L);
            assertThat(reponse.getCodeArticle()).isEqualTo("RIZ50KG");
            assertThat(reponse.getPrixVenteTtc()).isEqualTo(22061);
            assertThat(reponse.getTauxTva()).isEqualByComparingTo("19.25");
            assertThat(reponse.getSeuilAlerte()).isEqualByComparingTo("10");
            assertThat(reponse.getMargeBrutePct()).isEqualByComparingTo("23.33");
            assertThat(reponse.getGroupId()).isEqualTo(GROUP_ID);
            assertThat(reponse.getCategorieId()).isEqualTo(CATEGORIE_ID);
        }

        @Test
        @DisplayName("✅ Taux TVA hérité de la catégorie si non fourni")
        void shouldInheritTauxTvaFromCategorieWhenAbsent() {
            TenantGroup groupe = groupeActif();
            Categorie categorie = categorieDuGroupe(groupe);
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG")).thenReturn(false);
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID)).thenReturn(Optional.of(categorie));
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID)).thenReturn(Optional.of(groupe));
            when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));

            ArticleCreateRequest sansTva = requeteValide();
            sansTva.setTauxTva(null);
            ArticleResponse reponse = articleService.creer(
                    principal("ADMIN_FILIALE", GROUP_ID), sansTva);

            assertThat(reponse.getTauxTva()).isEqualByComparingTo("19.25");
            assertThat(reponse.getPrixVenteTtc()).isEqualTo(22061);
        }

        @Test
        @DisplayName("✅ seuilAlerte absent → 0 (alerte désactivée, DEC-023)")
        void shouldDefaultSeuilAlerteToZero() {
            TenantGroup groupe = groupeActif();
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG")).thenReturn(false);
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID)).thenReturn(Optional.of(categorieDuGroupe(groupe)));
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID)).thenReturn(Optional.of(groupe));
            ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
            when(articleRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            ArticleCreateRequest sansSeuil = requeteValide();
            sansSeuil.setSeuilAlerte(null);
            articleService.creer(principal("GESTIONNAIRE_STOCK", GROUP_ID), sansSeuil);

            assertThat(captor.getValue().getSeuilAlerte()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("✅ L'article est rattaché au groupe du JWT (isolation tenant)")
        void shouldSaveArticleScopedToGroup() {
            TenantGroup groupe = groupeActif();
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG")).thenReturn(false);
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID)).thenReturn(Optional.of(categorieDuGroupe(groupe)));
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID)).thenReturn(Optional.of(groupe));
            ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
            when(articleRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            articleService.creer(principal("GESTIONNAIRE_STOCK", GROUP_ID), requeteValide());

            assertThat(captor.getValue().getGroupe().getId()).isEqualTo(GROUP_ID);
        }

        @Test
        @DisplayName("✅ DEC-029 : prix d'achat ≥ prix de vente = avertissement, non bloquant")
        void shouldNotBlockWhenPrixAchatAbovePrixVente() {
            TenantGroup groupe = groupeActif();
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG")).thenReturn(false);
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID)).thenReturn(Optional.of(categorieDuGroupe(groupe)));
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID)).thenReturn(Optional.of(groupe));
            when(articleRepository.save(any(Article.class))).thenAnswer(inv -> {
                Article a = inv.getArgument(0);
                a.setId(7L);
                return a;
            });

            ArticleCreateRequest inverse = requeteValide();
            inverse.setPrixAchatHt(20000);
            ArticleResponse reponse = articleService.creer(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID), inverse);

            assertThat(reponse.getId()).isNotNull();
            assertThat(reponse.getMargeBrutePct()).isEqualByComparingTo("-7.50");
        }

        @Test
        @DisplayName("✅ Marge nulle si prix d'achat = 0 (pas de division par zéro)")
        void shouldReturnNullMargeWhenPrixAchatZero() {
            TenantGroup groupe = groupeActif();
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG")).thenReturn(false);
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID)).thenReturn(Optional.of(categorieDuGroupe(groupe)));
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID)).thenReturn(Optional.of(groupe));
            when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));

            ArticleCreateRequest gratuit = requeteValide();
            gratuit.setPrixAchatHt(0);
            ArticleResponse reponse = articleService.creer(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID), gratuit);

            assertThat(reponse.getMargeBrutePct()).isNull();
        }
    }

    // ─── cas d'erreur ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("❌ Cas d'erreur")
    class Erreurs {

        @Test
        @DisplayName("❌ 403 SEC_001 pour un rôle non autorisé (avant toute I/O)")
        void shouldThrow403WhenRoleNotAuthorized() {
            StockMasterPrincipal principal = principal("CAISSIER", GROUP_ID);
            ArticleCreateRequest request = requeteValide();
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.creer(principal, request), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
            verifyNoInteractions(articleRepository, categorieRepository, tenantGroupRepository);
        }

        @Test
        @DisplayName("❌ 403 GRP_002 si principal non groupé (groupId absent)")
        void shouldThrow403WhenPrincipalNotGrouped() {
            StockMasterPrincipal principal = principal("GESTIONNAIRE_STOCK", null);
            ArticleCreateRequest request = requeteValide();
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.creer(principal, request), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
            verifyNoInteractions(articleRepository, categorieRepository, tenantGroupRepository);
        }

        @Test
        @DisplayName("❌ 409 RES_002 si code article déjà utilisé dans le groupe")
        void shouldThrow409WhenCodeArticleDuplicate() {
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG")).thenReturn(true);

            StockMasterPrincipal principal = principal("GESTIONNAIRE_STOCK", GROUP_ID);
            ArticleCreateRequest request = requeteValide();
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.creer(principal, request), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_DUPLICATE_CODE);
            verify(articleRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ 404 RES_001 si catégorie inexistante")
        void shouldThrow404WhenCategorieNotFound() {
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG")).thenReturn(false);
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID)).thenReturn(Optional.empty());

            StockMasterPrincipal principal = principal("GESTIONNAIRE_STOCK", GROUP_ID);
            ArticleCreateRequest request = requeteValide();
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.creer(principal, request), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ 404 RES_001 si catégorie hors du groupe (jamais révéler l'existence)")
        void shouldThrow404WhenCategorieOutsideGroup() {
            TenantGroup autreGroupe = groupeActif();
            autreGroupe.setId(999L);
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG")).thenReturn(false);
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID))
                    .thenReturn(Optional.of(categorieDuGroupe(autreGroupe)));

            StockMasterPrincipal principal = principal("GESTIONNAIRE_STOCK", GROUP_ID);
            ArticleCreateRequest request = requeteValide();
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.creer(principal, request), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ 400 CAT_001 si taux TVA non supporté")
        void shouldThrow400WhenTauxNotSupported() {
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG")).thenReturn(false);
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID))
                    .thenReturn(Optional.of(categorieDuGroupe(groupeActif())));

            ArticleCreateRequest taux5 = requeteValide();
            taux5.setTauxTva(new BigDecimal("5.5"));
            StockMasterPrincipal principal = principal("GESTIONNAIRE_STOCK", GROUP_ID);
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.creer(principal, taux5), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CAT_TAUX_TVA_INVALIDE);
        }

        @Test
        @DisplayName("❌ 400 CAT_001 si taux TVA négatif")
        void shouldThrow400WhenTauxNegative() {
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG")).thenReturn(false);
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID))
                    .thenReturn(Optional.of(categorieDuGroupe(groupeActif())));

            ArticleCreateRequest tauxNeg = requeteValide();
            tauxNeg.setTauxTva(new BigDecimal("-1"));
            StockMasterPrincipal principal = principal("GESTIONNAIRE_STOCK", GROUP_ID);
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.creer(principal, tauxNeg), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CAT_TAUX_TVA_INVALIDE);
        }

        @Test
        @DisplayName("❌ 400 CAT_001 si taux TVA > 100 %")
        void shouldThrow400WhenTauxOver100() {
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG")).thenReturn(false);
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID))
                    .thenReturn(Optional.of(categorieDuGroupe(groupeActif())));

            ArticleCreateRequest tauxHaut = requeteValide();
            tauxHaut.setTauxTva(new BigDecimal("150"));
            StockMasterPrincipal principal = principal("GESTIONNAIRE_STOCK", GROUP_ID);
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.creer(principal, tauxHaut), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CAT_TAUX_TVA_INVALIDE);
        }

        @Test
        @DisplayName("❌ 403 GRP_002 si groupe inactif")
        void shouldThrow403WhenGroupInactive() {
            TenantGroup groupe = groupeActif();
            groupe.setActif(false);
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG")).thenReturn(false);
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID)).thenReturn(Optional.of(categorieDuGroupe(groupe)));
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID)).thenReturn(Optional.of(groupe));

            StockMasterPrincipal principal = principal("GESTIONNAIRE_STOCK", GROUP_ID);
            ArticleCreateRequest request = requeteValide();
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.creer(principal, request), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
        }
    }

}
