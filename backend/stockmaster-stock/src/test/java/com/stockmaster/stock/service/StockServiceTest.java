package com.stockmaster.stock.service;

import com.stockmaster.shared.config.PaginationProperties;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.TenantGroupRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.stock.domain.enums.OrigineType;
import com.stockmaster.stock.domain.enums.StatutAlerte;
import com.stockmaster.stock.domain.enums.TypeMouvement;
import com.stockmaster.stock.dto.response.MouvementStockResponse;
import com.stockmaster.stock.dto.response.StockArticleResponse;
import com.stockmaster.stock.repository.MouvementStockRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests service stock — US-051 (consultation du stock réel) et US-052
 * (historique des mouvements).
 *
 * <p><b>Isolation multi-tenant</b> : le {@code groupId} et le {@code entrepriseId}
 * viennent du JWT (jamais du corps) — le stock est agrégé par {@code entreprise_id}
 * et la liste des articles est scoping au groupe.</p>
 *
 * <p><b>Statut d'alerte (REF §4.2)</b> : ANOMALIE &lt; 0, RUPTURE ≤ 0 (décision GO —
 * lecture littérale du référentiel, un article sans mouvement remonte RUPTURE),
 * BAS si 0 &lt; stock ≤ seuil (seuil 0 = alerte désactivée), NORMAL sinon.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StockService — US-051/US-052 : /api/v1/stock")
class StockServiceTest {

    @Mock
    private MouvementStockRepository mouvementStockRepository;

    @Mock
    private TenantGroupRepository tenantGroupRepository;

    private PaginationProperties paginationProperties;
    private StockService stockService;

    private static final Long GROUP_ID = 1L;
    private static final Long ENTREPRISE_ID = 10L;
    private static final Long ARTICLE_ID = 5L;

    @BeforeEach
    void setUp() {
        paginationProperties = new PaginationProperties(); // maxPageSize = 100
        stockService = new StockService(mouvementStockRepository, tenantGroupRepository,
                paginationProperties);
    }

    // ─── fixtures ───────────────────────────────────────────────────────────

    private StockMasterPrincipal principal(String role, Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
        lenient().when(claims.get("groupId", Long.class)).thenReturn(groupId);
        lenient().when(claims.get("entrepriseId", Long.class)).thenReturn(entrepriseId);
        lenient().when(claims.get("role", String.class)).thenReturn(role);
        return new StockMasterPrincipal(99L, claims);
    }

    private TenantGroup groupeActif() {
        TenantGroup groupe = TenantGroup.builder().nomGroupe("Boutique CM").build();
        groupe.setId(GROUP_ID);
        groupe.setActif(true);
        groupe.setSupprime(false);
        return groupe;
    }

    private void groupeValide() {
        when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                .thenReturn(Optional.of(groupeActif()));
    }

    /** Ligne brute du repository : [articleId, codeArticle, designation, seuil, stock]. */
    private Object[] ligneStock(long articleId, String code, String designation,
                                String seuil, String stock) {
        return new Object[]{articleId, code, designation,
                new BigDecimal(seuil), new BigDecimal(stock)};
    }

    /** Ligne brute du repository : [id, type, quantite, date, userId, prenom, nom, origine, origineId, motif]. */
    private Object[] ligneMouvement(long id, String type, String quantite, Instant date,
                                    long userId, String prenom, String nom,
                                    String origine, Long origineId, String motif) {
        return new Object[]{id, type, new BigDecimal(quantite), date, userId,
                prenom, nom, origine, origineId, motif};
    }

    // ─── US-051 — consultation du stock réel ────────────────────────────────

    @Nested
    @DisplayName("US-051 — GET /api/v1/stock : stock réel par article")
    class ListerStock {

        @Test
        @DisplayName("✅ liste paginée : stock 0 → statut RUPTURE (décision GO, REF §4.2)")
        void shouldListerStockAvecStatutRupture() {
            Page<Object[]> page = new PageImpl<>(
                    List.<Object[]>of(ligneStock(5L, "ART-001", "Riz sac 25kg", "10.000", "0.000")),
                    PageRequest.of(0, 20), 1L);
            groupeValide();
            when(mouvementStockRepository.listerStockParArticle(
                    eq(GROUP_ID), eq(ENTREPRISE_ID), eq(""), any(Pageable.class)))
                    .thenReturn(page);

            var response = stockService.listerStock(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), null, 0, 20);

            assertThat(response.getContent()).hasSize(1);
            StockArticleResponse ligne = response.getContent().get(0);
            assertThat(ligne.getArticleId()).isEqualTo(5L);
            assertThat(ligne.getCodeArticle()).isEqualTo("ART-001");
            assertThat(ligne.getDesignation()).isEqualTo("Riz sac 25kg");
            assertThat(ligne.getStockActuel()).isEqualByComparingTo("0.000");
            assertThat(ligne.getSeuilAlerte()).isEqualByComparingTo("10.000");
            assertThat(ligne.getStatutAlerte()).isEqualTo(StatutAlerte.RUPTURE);
            assertThat(response.getPage()).isEqualTo(0);
            assertThat(response.getSize()).isEqualTo(20);
            assertThat(response.getTotalElements()).isEqualTo(1L);
        }

        @Test
        @DisplayName("✅ statut BAS quand 0 < stock ≤ seuil")
        void shouldCalculerStatutBas() {
            Page<Object[]> page = new PageImpl<>(
                    List.<Object[]>of(ligneStock(6L, "ART-002", "Huile 1L", "5.000", "3.000")),
                    PageRequest.of(0, 20), 1L);
            groupeValide();
            when(mouvementStockRepository.listerStockParArticle(
                    eq(GROUP_ID), eq(ENTREPRISE_ID), eq(""), any(Pageable.class)))
                    .thenReturn(page);

            var response = stockService.listerStock(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), null, 0, 20);

            assertThat(response.getContent().get(0).getStatutAlerte()).isEqualTo(StatutAlerte.BAS);
        }

        @Test
        @DisplayName("✅ rôle non restreint : CAISSIER consulte aussi le stock (US-051 « tout utilisateur authentifié »)")
        void shouldAutoriserToutRoleAuthentifie() {
            Page<Object[]> page = new PageImpl<>(
                    List.<Object[]>of(ligneStock(5L, "ART-001", "Riz", "10.000", "0.000")),
                    PageRequest.of(0, 20), 1L);
            groupeValide();
            when(mouvementStockRepository.listerStockParArticle(
                    eq(GROUP_ID), eq(ENTREPRISE_ID), eq(""), any(Pageable.class)))
                    .thenReturn(page);

            var response = stockService.listerStock(
                    principal("CAISSIER", GROUP_ID, ENTREPRISE_ID), null, 0, 20);

            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("✅ filtre statutAlerte transmis au repository (pagination faite en SQL)")
        void shouldTransmettreFiltreStatut() {
            Page<Object[]> page = new PageImpl<>(
                    List.<Object[]>of(ligneStock(6L, "ART-002", "Huile", "5.000", "3.000")),
                    PageRequest.of(0, 20), 1L);
            groupeValide();
            when(mouvementStockRepository.listerStockParArticle(
                    eq(GROUP_ID), eq(ENTREPRISE_ID), eq("BAS"), any(Pageable.class)))
                    .thenReturn(page);

            var response = stockService.listerStock(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), "BAS", 0, 20);

            assertThat(response.getContent()).hasSize(1);
            verify(mouvementStockRepository).listerStockParArticle(
                    eq(GROUP_ID), eq(ENTREPRISE_ID), eq("BAS"), any(Pageable.class));
        }

        @Test
        @DisplayName("✅ taille de page bornée à stockmaster.pagination.max-page-size (100)")
        void shouldBornerTailleDePage() {
            Page<Object[]> page = new PageImpl<>(List.<Object[]>of(), PageRequest.of(0, 100), 0L);
            groupeValide();
            when(mouvementStockRepository.listerStockParArticle(
                    eq(GROUP_ID), eq(ENTREPRISE_ID), eq(""), any(Pageable.class)))
                    .thenReturn(page);

            stockService.listerStock(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), null, 0, 10000);

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(mouvementStockRepository).listerStockParArticle(
                    eq(GROUP_ID), eq(ENTREPRISE_ID), eq(""), captor.capture());
            assertThat(captor.getValue().getPageSize()).isEqualTo(100);
        }

        @Test
        @DisplayName("✅ page vide : contenu vide, totalElements 0")
        void shouldRetournerPageVide() {
            Page<Object[]> page = new PageImpl<>(List.<Object[]>of(), PageRequest.of(0, 20), 0L);
            groupeValide();
            when(mouvementStockRepository.listerStockParArticle(
                    eq(GROUP_ID), eq(ENTREPRISE_ID), eq(""), any(Pageable.class)))
                    .thenReturn(page);

            var response = stockService.listerStock(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), null, 0, 20);

            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isZero();
            assertThat(response.getTotalPages()).isZero();
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — groupId absent du JWT, avant toute I/O")
        void shouldRefuserPrincipalSansGroupId() {
            var ex = catchThrowableOfType(
                    () -> stockService.listerStock(
                            principal("GESTIONNAIRE_STOCK", null, ENTREPRISE_ID), null, 0, 20),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
            verify(tenantGroupRepository, never()).findByIdAndSupprimeFalse(any());
            verify(mouvementStockRepository, never()).listerStockParArticle(any(), any(), any(), any());
        }

        @Test
        @DisplayName("❌ 403 SEC_001 — entrepriseId absent du JWT, avant toute I/O")
        void shouldRefuserPrincipalSansEntrepriseId() {
            var ex = catchThrowableOfType(
                    () -> stockService.listerStock(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, null), null, 0, 20),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
            verify(tenantGroupRepository, never()).findByIdAndSupprimeFalse(any());
            verify(mouvementStockRepository, never()).listerStockParArticle(any(), any(), any(), any());
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — groupe inexistant / soft-supprimé")
        void shouldRefuserGroupeInexistant() {
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.empty());

            var ex = catchThrowableOfType(
                    () -> stockService.listerStock(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), null, 0, 20),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
            verify(mouvementStockRepository, never()).listerStockParArticle(any(), any(), any(), any());
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — groupe désactivé (suspension / plan expiré)")
        void shouldRefuserGroupeInactif() {
            TenantGroup groupe = groupeActif();
            groupe.setActif(false);
            when(tenantGroupRepository.findByIdAndSupprimeFalse(GROUP_ID))
                    .thenReturn(Optional.of(groupe));

            var ex = catchThrowableOfType(
                    () -> stockService.listerStock(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), null, 0, 20),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
            verify(mouvementStockRepository, never()).listerStockParArticle(any(), any(), any(), any());
        }

        @Test
        @DisplayName("❌ 400 SYS_002 — statutAlerte invalide (?statutAlerte=XYZ)")
        void shouldRefuserStatutInvalide() {
            groupeValide();

            var ex = catchThrowableOfType(
                    () -> stockService.listerStock(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID), "XYZ", 0, 20),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SYS_BAD_REQUEST);
            verify(mouvementStockRepository, never()).listerStockParArticle(any(), any(), any(), any());
        }
    }

    // ─── US-052 — historique des mouvements ─────────────────────────────────

    @Nested
    @DisplayName("US-052 — GET /api/v1/stock/articles/{id}/mouvements")
    class ListerMouvements {

        private void articleExiste() {
            when(mouvementStockRepository.articleExisteDansGroupe(ARTICLE_ID, GROUP_ID))
                    .thenReturn(true);
        }

        @Test
        @DisplayName("✅ historique paginé : mapping complet (type, quantité, date, utilisateur, origine)")
        void shouldListerMouvements() {
            Instant date = Instant.parse("2026-10-01T10:15:00Z");
            Page<Object[]> page = new PageImpl<>(
                    List.<Object[]>of(ligneMouvement(111L, "ENTREE", "12.000", date, 7L,
                            "Jean", "Mvogo", "COMMANDE_FOURNISSEUR", 42L, "Réception partielle")),
                    PageRequest.of(0, 20), 1L);
            groupeValide();
            articleExiste();
            when(mouvementStockRepository.listerMouvementsParArticle(
                    eq(ENTREPRISE_ID), eq(ARTICLE_ID), eq(""), any(Instant.class),
                    any(Instant.class), any(Pageable.class)))
                    .thenReturn(page);

            var response = stockService.listerMouvements(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    ARTICLE_ID, null, null, null, 0, 20);

            assertThat(response.getContent()).hasSize(1);
            MouvementStockResponse ligne = response.getContent().get(0);
            assertThat(ligne.getId()).isEqualTo(111L);
            assertThat(ligne.getTypeMouvement()).isEqualTo(TypeMouvement.ENTREE);
            assertThat(ligne.getQuantite()).isEqualByComparingTo("12.000");
            assertThat(ligne.getDateMouvement()).isEqualTo(date);
            assertThat(ligne.getUtilisateurId()).isEqualTo(7L);
            assertThat(ligne.getUtilisateurNom()).isEqualTo("Jean Mvogo");
            assertThat(ligne.getOrigineType()).isEqualTo(OrigineType.COMMANDE_FOURNISSEUR);
            assertThat(ligne.getOrigineId()).isEqualTo(42L);
            assertThat(ligne.getMotif()).isEqualTo("Réception partielle");
            assertThat(response.getTotalElements()).isEqualTo(1L);
        }

        @Test
        @DisplayName("✅ bornes de dates converties en UTC (dateFin inclusive : lendemain 00:00Z)")
        void shouldConvertirBornesDeDates() {
            Page<Object[]> page = new PageImpl<>(List.<Object[]>of(), PageRequest.of(0, 20), 0L);
            groupeValide();
            articleExiste();
            when(mouvementStockRepository.listerMouvementsParArticle(
                    eq(ENTREPRISE_ID), eq(ARTICLE_ID), eq(""), any(Instant.class),
                    any(Instant.class), any(Pageable.class)))
                    .thenReturn(page);

            stockService.listerMouvements(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    ARTICLE_ID, null,
                    LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), 0, 20);

            verify(mouvementStockRepository).listerMouvementsParArticle(
                    eq(ENTREPRISE_ID), eq(ARTICLE_ID), eq(""),
                    eq(Instant.parse("2026-10-01T00:00:00Z")),
                    eq(Instant.parse("2026-11-01T00:00:00Z")),
                    any(Pageable.class));
        }

        @Test
        @DisplayName("✅ sans bornes → sentinelles min/max (aucun filtre de date)")
        void shouldUtiliserSentinellesSansBornes() {
            Page<Object[]> page = new PageImpl<>(List.<Object[]>of(), PageRequest.of(0, 20), 0L);
            groupeValide();
            articleExiste();
            when(mouvementStockRepository.listerMouvementsParArticle(
                    eq(ENTREPRISE_ID), eq(ARTICLE_ID), eq(""), any(Instant.class),
                    any(Instant.class), any(Pageable.class)))
                    .thenReturn(page);

            stockService.listerMouvements(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    ARTICLE_ID, null, null, null, 0, 20);

            verify(mouvementStockRepository).listerMouvementsParArticle(
                    eq(ENTREPRISE_ID), eq(ARTICLE_ID), eq(""),
                    eq(Instant.EPOCH), eq(Instant.parse("2100-01-01T00:00:00Z")),
                    any(Pageable.class));
        }

        @Test
        @DisplayName("✅ tri date DESC porté par le SQL — Pageable sans tri")
        void shouldPageableSansTri() {
            Page<Object[]> page = new PageImpl<>(List.<Object[]>of(), PageRequest.of(0, 20), 0L);
            groupeValide();
            articleExiste();
            when(mouvementStockRepository.listerMouvementsParArticle(
                    eq(ENTREPRISE_ID), eq(ARTICLE_ID), eq(""), any(Instant.class),
                    any(Instant.class), any(Pageable.class)))
                    .thenReturn(page);

            stockService.listerMouvements(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    ARTICLE_ID, null, null, null, 0, 20);

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(mouvementStockRepository).listerMouvementsParArticle(
                    eq(ENTREPRISE_ID), eq(ARTICLE_ID), eq(""), any(Instant.class),
                    any(Instant.class), captor.capture());
            assertThat(captor.getValue().getSort().isUnsorted()).isTrue();
        }

        @Test
        @DisplayName("✅ filtre typeMouvement transmis au repository")
        void shouldTransmettreFiltreType() {
            Page<Object[]> page = new PageImpl<>(List.<Object[]>of(), PageRequest.of(0, 20), 0L);
            groupeValide();
            articleExiste();
            when(mouvementStockRepository.listerMouvementsParArticle(
                    eq(ENTREPRISE_ID), eq(ARTICLE_ID), eq("SORTIE"), any(Instant.class),
                    any(Instant.class), any(Pageable.class)))
                    .thenReturn(page);

            stockService.listerMouvements(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    ARTICLE_ID, "SORTIE", null, null, 0, 20);

            verify(mouvementStockRepository).listerMouvementsParArticle(
                    eq(ENTREPRISE_ID), eq(ARTICLE_ID), eq("SORTIE"),
                    any(Instant.class), any(Instant.class), any(Pageable.class));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — article hors du groupe du JWT (404 uniforme, jamais révéler l'existence)")
        void shouldRetourner404SiArticleHorsGroupe() {
            groupeValide();
            when(mouvementStockRepository.articleExisteDansGroupe(ARTICLE_ID, GROUP_ID))
                    .thenReturn(false);

            var ex = catchThrowableOfType(
                    () -> stockService.listerMouvements(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                            ARTICLE_ID, null, null, null, 0, 20),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND);
            verify(mouvementStockRepository, never())
                    .listerMouvementsParArticle(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("❌ 403 SEC_001 — CAISSIER refusé avant toute I/O (RBAC US-052)")
        void shouldRefuserCaissierAvantToutIo() {
            var ex = catchThrowableOfType(
                    () -> stockService.listerMouvements(
                            principal("CAISSIER", GROUP_ID, ENTREPRISE_ID),
                            ARTICLE_ID, null, null, null, 0, 20),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
            verify(tenantGroupRepository, never()).findByIdAndSupprimeFalse(any());
            verify(mouvementStockRepository, never()).articleExisteDansGroupe(any(), any());
            verify(mouvementStockRepository, never())
                    .listerMouvementsParArticle(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("❌ 403 SEC_001 — RESP_ACHATS refusé (rôle hors du trio stock)")
        void shouldRefuserRespAchats() {
            var ex = catchThrowableOfType(
                    () -> stockService.listerMouvements(
                            principal("RESP_ACHATS", GROUP_ID, ENTREPRISE_ID),
                            ARTICLE_ID, null, null, null, 0, 20),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
        }

        @Test
        @DisplayName("❌ 403 GRP_002 — groupId absent du JWT, avant toute I/O")
        void shouldRefuserPrincipalSansGroupId() {
            var ex = catchThrowableOfType(
                    () -> stockService.listerMouvements(
                            principal("GESTIONNAIRE_STOCK", null, ENTREPRISE_ID),
                            ARTICLE_ID, null, null, null, 0, 20),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.GRP_CROSS_GROUP_FORBIDDEN);
            verify(tenantGroupRepository, never()).findByIdAndSupprimeFalse(any());
            verify(mouvementStockRepository, never()).articleExisteDansGroupe(any(), any());
        }

        @Test
        @DisplayName("❌ 403 SEC_001 — entrepriseId absent du JWT")
        void shouldRefuserPrincipalSansEntrepriseId() {
            var ex = catchThrowableOfType(
                    () -> stockService.listerMouvements(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, null),
                            ARTICLE_ID, null, null, null, 0, 20),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SEC_ACCESS_DENIED);
        }

        @Test
        @DisplayName("❌ 400 SYS_002 — typeMouvement invalide (?typeMouvement=FOO)")
        void shouldRefuserTypeInvalide() {
            groupeValide();

            var ex = catchThrowableOfType(
                    () -> stockService.listerMouvements(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                            ARTICLE_ID, "FOO", null, null, 0, 20),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SYS_BAD_REQUEST);
            verify(mouvementStockRepository, never()).listerMouvementsParArticle(
                    any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("❌ 400 SYS_002 — dateDebut > dateFin")
        void shouldRefuserBornesIncoherentes() {
            groupeValide();

            var ex = catchThrowableOfType(
                    () -> stockService.listerMouvements(
                            principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                            ARTICLE_ID, null,
                            LocalDate.of(2026, 11, 1), LocalDate.of(2026, 10, 1), 0, 20),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SYS_BAD_REQUEST);
            verify(mouvementStockRepository, never()).listerMouvementsParArticle(
                    any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("✅ page vide (aucun mouvement pour l'article)")
        void shouldRetournerPageVide() {
            Page<Object[]> page = new PageImpl<>(List.<Object[]>of(), PageRequest.of(0, 20), 0L);
            groupeValide();
            articleExiste();
            when(mouvementStockRepository.listerMouvementsParArticle(
                    eq(ENTREPRISE_ID), eq(ARTICLE_ID), eq(""), any(Instant.class),
                    any(Instant.class), any(Pageable.class)))
                    .thenReturn(page);

            var response = stockService.listerMouvements(
                    principal("GESTIONNAIRE_STOCK", GROUP_ID, ENTREPRISE_ID),
                    ARTICLE_ID, null, null, null, 0, 20);

            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isZero();
        }
    }
}
