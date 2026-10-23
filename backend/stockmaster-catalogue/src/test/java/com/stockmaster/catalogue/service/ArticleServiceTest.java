package com.stockmaster.catalogue.service;

import com.stockmaster.catalogue.domain.entity.Article;
import com.stockmaster.catalogue.domain.entity.Categorie;
import com.stockmaster.catalogue.dto.request.ArticleCreateRequest;
import com.stockmaster.catalogue.dto.request.ArticleUpdateRequest;
import com.stockmaster.catalogue.dto.response.ArticleListResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    /** Article comme persisté : TTC calculé côté serveur (DEC-003). */
    private Article articleExistant(TenantGroup groupe, Categorie categorie) {
        Article article = Article.builder()
                .groupe(groupe)
                .categorie(categorie)
                .codeArticle("RIZ50KG")
                .designation("Riz parfumé 50 kg")
                .prixAchatHt(15000)
                .prixVenteHt(18500)
                .tauxTva(new BigDecimal("19.25"))
                .prixVenteTtc(22061)
                .seuilAlerte(new BigDecimal("10"))
                .actif(true)
                .build();
        article.setId(ARTICLE_ID);
        return article;
    }

    private void stubArticleDansGroupe(Article article) {
        when(articleRepository.findByIdAndSupprimeFalse(ARTICLE_ID))
                .thenReturn(Optional.of(article));
    }

    private static final Long ARTICLE_ID = 7L;

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

    // ─── US-032 — lister ════════════════════════════════════════════════════

    @Nested
    @DisplayName("US-032 — GET /api/v1/articles (liste paginée)")
    class Lister {

        @Test
        @DisplayName("✅ Liste paginée group-scopée (supprime=false), metadata complètes")
        void shouldListerPaginatedGroupScoped() {
            TenantGroup groupe = groupeActif();
            Categorie categorie = categorieDuGroupe(groupe);
            Article article = articleExistant(groupe, categorie);
            when(articleRepository.findAllByGroupeIdAndSupprimeFalse(eq(GROUP_ID), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(article), PageRequest.of(0, 20), 1));

            StockMasterPrincipal principal = principal("CAISSIER", GROUP_ID);
            ArticleListResponse reponse = articleService.lister(principal, 0, 20, "designation", "ASC", null);

            assertThat(reponse.getTotalElements()).isEqualTo(1);
            assertThat(reponse.getContent()).hasSize(1);
            assertThat(reponse.getContent().get(0).getCodeArticle()).isEqualTo("RIZ50KG");
            // US-033 : TTC et marge reflètent les valeurs calculées
            assertThat(reponse.getContent().get(0).getPrixVenteTtc()).isEqualTo(22061);
            assertThat(reponse.getContent().get(0).getMargeBrutePct()).isEqualByComparingTo("23.33");
        }

        @Test
        @DisplayName("✅ Recherche plein-texte : route vers le repo de recherche (designation OU code)")
        void shouldRouteSearchToRepository() {
            TenantGroup groupe = groupeActif();
            when(articleRepository.rechercherParGroupeEtTexte(eq(GROUP_ID), eq("riz"), any(Pageable.class)))
                    .thenReturn(Page.empty(PageRequest.of(0, 20)));

            StockMasterPrincipal principal = principal("CAISSIER", GROUP_ID);
            ArticleListResponse reponse = articleService.lister(principal, 0, 20, "designation", "ASC", "riz");

            assertThat(reponse.getContent()).isEmpty();
            verify(articleRepository).rechercherParGroupeEtTexte(eq(GROUP_ID), eq("riz"), any(Pageable.class));
        }

        @Test
        @DisplayName("✅ Pagination disciplinée : size > 100 est bornée à 100 (critère AC)")
        void shouldClampSizeTo100() {
            TenantGroup groupe = groupeActif();
            when(articleRepository.findAllByGroupeIdAndSupprimeFalse(eq(GROUP_ID), any(Pageable.class)))
                    .thenReturn(Page.empty(PageRequest.of(0, 100)));

            StockMasterPrincipal principal = principal("CAISSIER", GROUP_ID);
            articleService.lister(principal, 0, 500, "designation", "ASC", null);

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(articleRepository).findAllByGroupeIdAndSupprimeFalse(eq(GROUP_ID), captor.capture());
            assertThat(captor.getValue().getPageSize()).isEqualTo(100);
        }

        @Test
        @DisplayName("❌ 403 GRP_002 si principal non groupé")
        void shouldThrowWhenPrincipalNotGrouped() {
            StockMasterPrincipal principal = principal("CAISSIER", null);
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.lister(principal, 0, 20, null, null, null), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
            verifyNoInteractions(articleRepository);
        }
    }

    // ─── US-033 — consulter ═════════════════════════════════════════════════

    @Nested
    @DisplayName("US-033 — GET /api/v1/articles/{id}")
    class Consulter {

        @Test
        @DisplayName("✅ Fiche article du groupe : TTC, marge, statut de groupe")
        void shouldConsulterArticle() {
            TenantGroup groupe = groupeActif();
            Categorie categorie = categorieDuGroupe(groupe);
            Article article = articleExistant(groupe, categorie);
            stubArticleDansGroupe(article);

            StockMasterPrincipal principal = principal("CAISSIER", GROUP_ID);
            ArticleResponse reponse = articleService.consulter(principal, ARTICLE_ID);

            assertThat(reponse.getId()).isEqualTo(ARTICLE_ID);
            assertThat(reponse.getPrixVenteTtc()).isEqualTo(22061);
            assertThat(reponse.getGroupId()).isEqualTo(GROUP_ID);
            assertThat(reponse.getActif()).isTrue();
        }

        @Test
        @DisplayName("❌ 404 RES_001 si article hors groupe — jamais révélé")
        void shouldThrow404WhenArticleOutsideGroup() {
            TenantGroup autreGroupe = groupeActif();
            autreGroupe.setId(999L);
            Categorie categorieAutre = categorieDuGroupe(autreGroupe);
            Article articleAutre = articleExistant(autreGroupe, categorieAutre);
            stubArticleDansGroupe(articleAutre);

            StockMasterPrincipal principal = principal("CAISSIER", GROUP_ID);
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.consulter(principal, ARTICLE_ID), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("❌ 404 RES_001 si article inconnu")
        void shouldThrow404WhenArticleNotFound() {
            when(articleRepository.findByIdAndSupprimeFalse(ARTICLE_ID)).thenReturn(Optional.empty());

            StockMasterPrincipal principal = principal("CAISSIER", GROUP_ID);
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.consulter(principal, ARTICLE_ID), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND);
        }
    }

    // ─── US-034 — modifier ══════════════════════════════════════════════════

    @Nested
    @DisplayName("US-034 — PUT /api/v1/articles/{id}")
    class Modifier {

        private ArticleUpdateRequest updateValide() {
            return new ArticleUpdateRequest(
                    "RIZ50KG", "Riz parfumé 50 kg", CATEGORIE_ID,
                    15000, 20000, null /* taux hérité catégorie */, new BigDecimal("15"));
        }

        @Test
        @DisplayName("✅ Modifie prix / seuil : TTC recalculé DEC-003 (20000 × 1.1925 = 23850), marge mise à jour")
        void shouldModifierArticleAndRecalculateTtc() {
            TenantGroup groupe = groupeActif();
            Categorie categorie = categorieDuGroupe(groupe);
            Article article = articleExistant(groupe, categorie);
            stubArticleDansGroupe(article);
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG"))
                    .thenReturn(true); // lui-même
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID)).thenReturn(Optional.of(categorie));
            when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));

            StockMasterPrincipal principal = principal("GESTIONNAIRE_STOCK", GROUP_ID);
            ArticleResponse reponse = articleService.modifier(principal, ARTICLE_ID, updateValide());

            assertThat(reponse.getPrixVenteHt()).isEqualTo(20000);
            assertThat(reponse.getPrixVenteTtc()).isEqualTo(23850);
            assertThat(reponse.getMargeBrutePct()).isEqualByComparingTo("33.33");
            ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
            verify(articleRepository).save(captor.capture());
            assertThat(captor.getValue().getSeuilAlerte()).isEqualByComparingTo("15");
        }

        @Test
        @DisplayName("✅ Taux TVA non fourni à la modif : hérite de la catégorie")
        void shouldInheritTauxFromCategorieOnModify() {
            TenantGroup groupe = groupeActif();
            Categorie categorie = categorieDuGroupe(groupe); // 19.25
            Article article = articleExistant(groupe, categorie);
            stubArticleDansGroupe(article);
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG"))
                    .thenReturn(true);
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID)).thenReturn(Optional.of(categorie));
            when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));

            StockMasterPrincipal principal = principal("ADMIN_FILIALE", GROUP_ID);
            ArticleResponse reponse = articleService.modifier(principal, ARTICLE_ID, updateValide());

            assertThat(reponse.getTauxTva()).isEqualByComparingTo("19.25");
            assertThat(reponse.getPrixVenteTtc()).isEqualTo(23850);
        }

        @Test
        @DisplayName("❌ 409 RES_002 si code modifié vers un code déjà pris par un autre article")
        void shouldThrow409WhenCodeTakenByOtherArticle() {
            TenantGroup groupe = groupeActif();
            Categorie categorie = categorieDuGroupe(groupe);
            Article article = articleExistant(groupe, categorie);
            stubArticleDansGroupe(article);
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "SUCRE1KG"))
                    .thenReturn(true);

            ArticleUpdateRequest miss = new ArticleUpdateRequest(
                    "SUCRE1KG", "Riz parfumé 50 kg", CATEGORIE_ID,
                    15000, 18500, null, new BigDecimal("10"));
            StockMasterPrincipal principal = principal("GESTIONNAIRE_STOCK", GROUP_ID);
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.modifier(principal, ARTICLE_ID, miss), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_DUPLICATE_CODE);
            verify(articleRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ 404 RES_001 si nouvelle catégorie hors groupe")
        void shouldThrow404WhenNewCategorieOutsideGroup() {
            TenantGroup groupe = groupeActif();
            Categorie categorie = categorieDuGroupe(groupe);
            Article article = articleExistant(groupe, categorie);
            stubArticleDansGroupe(article);
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG"))
                    .thenReturn(true); // lui-même
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID)).thenReturn(Optional.empty());

            StockMasterPrincipal principal = principal("GESTIONNAIRE_STOCK", GROUP_ID);
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.modifier(principal, ARTICLE_ID, updateValide()), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND);
            verify(articleRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ 400 CAT_001 si taux surchargé non supporté")
        void shouldThrow400WhenTauxSurchargedNotSupported() {
            TenantGroup groupe = groupeActif();
            Categorie categorie = categorieDuGroupe(groupe);
            Article article = articleExistant(groupe, categorie);
            stubArticleDansGroupe(article);
            when(articleRepository.existsByGroupeIdAndCodeArticleAndSupprimeFalse(GROUP_ID, "RIZ50KG"))
                    .thenReturn(true);
            when(categorieRepository.findByIdAndSupprimeFalse(CATEGORIE_ID)).thenReturn(Optional.of(categorie));

            ArticleUpdateRequest mauvais = new ArticleUpdateRequest(
                    "RIZ50KG", "Riz parfumé 50 kg", CATEGORIE_ID,
                    15000, 18500, new BigDecimal("5.5"), new BigDecimal("10"));
            StockMasterPrincipal principal = principal("GESTIONNAIRE_STOCK", GROUP_ID);
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.modifier(principal, ARTICLE_ID, mauvais), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CAT_TAUX_TVA_INVALIDE);
        }

        @Test
        @DisplayName("❌ 403 SEC_001 pour un rôle non autorisé (avant toute I/O)")
        void shouldThrow403WhenRoleNotAuthorized() {
            StockMasterPrincipal principal = principal("CAISSIER", GROUP_ID);
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.modifier(principal, ARTICLE_ID, updateValide()), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
            verifyNoInteractions(articleRepository);
        }
    }

    // ─── US-035 — supprimer ═════════════════════════════════════════════════

    @Nested
    @DisplayName("US-035 — DELETE /api/v1/articles/{id}")
    class Supprimer {

        @Test
        @DisplayName("✅ Suppression logique : supprime=true + actif=false")
        void shouldSoftDeleteArticle() {
            TenantGroup groupe = groupeActif();
            Categorie categorie = categorieDuGroupe(groupe);
            Article article = articleExistant(groupe, categorie);
            stubArticleDansGroupe(article);
            when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));

            StockMasterPrincipal principal = principal("GESTIONNAIRE_STOCK", GROUP_ID);
            ArticleResponse reponse = articleService.supprimer(principal, ARTICLE_ID);

            ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
            verify(articleRepository).save(captor.capture());
            assertThat(captor.getValue().getSupprime()).isTrue();
            assertThat(captor.getValue().getActif()).isFalse();
            assertThat(reponse.getActif()).isFalse();
        }

        @Test
        @DisplayName("❌ 404 RES_001 si article hors groupe — jamais révélé")
        void shouldThrow404WhenArticleOutsideGroup() {
            TenantGroup autreGroupe = groupeActif();
            autreGroupe.setId(999L);
            Categorie categorieAutre = categorieDuGroupe(autreGroupe);
            Article articleAutre = articleExistant(autreGroupe, categorieAutre);
            stubArticleDansGroupe(articleAutre);

            StockMasterPrincipal principal = principal("GESTIONNAIRE_STOCK", GROUP_ID);
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.supprimer(principal, ARTICLE_ID), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND);
            verify(articleRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ 403 SEC_001 pour un rôle non autorisé (avant toute I/O)")
        void shouldThrow403WhenRoleNotAuthorized() {
            StockMasterPrincipal principal = principal("CAISSIER", GROUP_ID);
            BusinessException ex = catchThrowableOfType(
                    () -> articleService.supprimer(principal, ARTICLE_ID), BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
            verifyNoInteractions(articleRepository);
        }
    }

}
