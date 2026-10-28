package com.stockmaster.stock.service;

import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.exception.InsufficientStockException;
import com.stockmaster.stock.domain.enums.OrigineType;
import com.stockmaster.stock.domain.enums.TypeMouvement;
import com.stockmaster.stock.port.EcritureStockPort.MouvementEnregistre;
import com.stockmaster.stock.port.EcritureStockPort.RequeteMouvement;
import com.stockmaster.stock.port.EcritureStockPort;
import com.stockmaster.stock.repository.MouvementStockRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests du port d'écriture du stock — ticket GS-1240.
 *
 * <p><b>Invariant DEC-023 (addendum)</b> : le stock n'est jamais négatif —
 * toute écriture décrémentante au-delà du stock réel est refusée en
 * {@code STK_001} (409) et <b>rien n'est écrit</b>.</p>
 *
 * <p><b>Sérialisation (DEC-023 addendum)</b> : le verrou pessimiste sur le
 * couple {@code (entreprise, article)} doit être pris AVANT la lecture du
 * stock réel ({@code InOrder} — sinon deux écritures concurrentes liraient
 * le même stock et l'invariant serait faux).</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EcritureStockService — port d'écriture du stock (GS-1240)")
class EcritureStockServiceTest {

    private static final Long ENTREPRISE_ID = 10L;
    private static final Long ARTICLE_ID = 5L;
    private static final Long UTILISATEUR_ID = 100L;
    private static final Long MOUVEMENT_ID = 777L;

    @Mock
    private MouvementStockRepository mouvementStockRepository;

    @Mock
    private EntityManager entityManager;

    private EcritureStockService service;

    @BeforeEach
    void setUp() {
        service = new EcritureStockService(mouvementStockRepository, entityManager);
    }

    private RequeteMouvement requete(TypeMouvement type, String quantite,
                                     OrigineType origine, String motif) {
        return new RequeteMouvement(ENTREPRISE_ID, ARTICLE_ID, UTILISATEUR_ID,
                type, new BigDecimal(quantite), origine, 42L, motif);
    }

    // ============================================================
    // US-1240 — Cas nominations (ENTREE / SORTIE)
    // ============================================================
    @Nested
    @DisplayName("Enregistrement nominatif ✅")
    class Nominatif {

        @Test
        @DisplayName("✅ ENTREE : persiste et renvoie id + stock après")
        void shouldPersistEntreeAndReturnIdAndStock() {
            when(mouvementStockRepository.referenceValide(ARTICLE_ID, ENTREPRISE_ID, UTILISATEUR_ID))
                    .thenReturn(true);
            when(mouvementStockRepository.calculerStockReel(ENTREPRISE_ID, ARTICLE_ID))
                    .thenReturn(new BigDecimal("20"));
            when(mouvementStockRepository.save(any(com.stockmaster.stock.domain.entity.MouvementStock.class)))
                    .thenAnswer(inv -> {
                        var m = inv.getArgument(0);
                        org.springframework.test.util.ReflectionTestUtils.setField(m, "id", MOUVEMENT_ID);
                        return m;
                    });

            MouvementEnregistre resultat = service.enregistrer(
                    requete(TypeMouvement.ENTREE, "5", OrigineType.COMMANDE_FOURNISSEUR, "réception commande"));

            assertThat(resultat.mouvementId()).isEqualTo(MOUVEMENT_ID);
            assertThat(resultat.stockApres()).isEqualByComparingTo("25");

            ArgumentCaptor<com.stockmaster.stock.domain.entity.MouvementStock> captor =
                    ArgumentCaptor.forClass(com.stockmaster.stock.domain.entity.MouvementStock.class);
            verify(mouvementStockRepository).save(captor.capture());
            var sauvegarde = captor.getValue();
            assertThat(sauvegarde.getQuantite()).isEqualByComparingTo("5");
            assertThat(sauvegarde.getTypeMouvement()).isEqualTo(TypeMouvement.ENTREE);
            assertThat(sauvegarde.getDateMouvement()).isNotNull();
        }

        @Test
        @DisplayName("✅ SORTIE nominative : stock 20 − 5 = 15, persite")
        void shouldPersistSortieWithRemainingStock() {
            when(mouvementStockRepository.referenceValide(ARTICLE_ID, ENTREPRISE_ID, UTILISATEUR_ID))
                    .thenReturn(true);
            when(mouvementStockRepository.calculerStockReel(ENTREPRISE_ID, ARTICLE_ID))
                    .thenReturn(new BigDecimal("20"));
            when(mouvementStockRepository.save(any(com.stockmaster.stock.domain.entity.MouvementStock.class)))
                    .thenAnswer(inv -> {
                        var m = inv.getArgument(0);
                        org.springframework.test.util.ReflectionTestUtils.setField(m, "id", MOUVEMENT_ID);
                        return m;
                    });

            var resultat = service.enregistrer(
                    requete(TypeMouvement.SORTIE, "5", OrigineType.VENTE, null));

            assertThat(resultat.stockApres()).isEqualByComparingTo("15");
        }

        @Test
        @DisplayName("✅ Sérialisation : verrou pessimiste pris avant la lecture du stock")
        void shouldTakeLockBeforeReadingStock() {
            when(mouvementStockRepository.referenceValide(anyLong(), anyLong(), anyLong()))
                    .thenReturn(true);
            when(mouvementStockRepository.calculerStockReel(anyLong(), anyLong()))
                    .thenReturn(BigDecimal.TEN);
            when(mouvementStockRepository.save(any(com.stockmaster.stock.domain.entity.MouvementStock.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            service.enregistrer(requete(TypeMouvement.ENTREE, "1", OrigineType.CORRECTION, "don"));

            InOrder ordre = inOrder(mouvementStockRepository);
            ordre.verify(mouvementStockRepository).verrouilleCouple(ENTREPRISE_ID, ARTICLE_ID);
            ordre.verify(mouvementStockRepository).calculerStockReel(ENTREPRISE_ID, ARTICLE_ID);
        }
    }

    // ============================================================
    // ❌ Validation des champs
    // ============================================================
    @Nested
    @DisplayName("Validation des champs ❌")
    class ValidationChamps {

        @Test
        @DisplayName("❌ quantite <= 0 → SYS_002 (400) et rien d'écrit")
        void shouldRejectNonPositiveQuantity() {
            var thrown = catchThrowableOfType(
                    () -> service.enregistrer(requete(TypeMouvement.ENTREE, "0", OrigineType.CORRECTION, "m")),
                    BusinessException.class);

            assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.SYS_BAD_REQUEST);
            verify(mouvementStockRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ typeMouvement manquant → SYS_002 (400)")
        void shouldRejectMissingType() {
            var thrown = catchThrowableOfType(
                    () -> service.enregistrer(new RequeteMouvement(
                            ENTREPRISE_ID, ARTICLE_ID, UTILISATEUR_ID,
                            null, BigDecimal.ONE, OrigineType.CORRECTION, null, "m")),
                    BusinessException.class);

            assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.SYS_BAD_REQUEST);
        }
    }

    // ============================================================
    // ❌ Règles de correction (US-053/US-054)
    // ============================================================
    @Nested
    @DisplayName("Règles de correction ❌")
    class ReglesCorrection {

        @Test
        @DisplayName("❌ CORRECTION_NEG sans motif → STK_002 (400)")
        void shouldRejectCorrectionNegativeWithoutMotif() {
            var thrown = catchThrowableOfType(
                    () -> service.enregistrer(
                            requete(TypeMouvement.CORRECTION_NEG, "3", OrigineType.CORRECTION, null)),
                    BusinessException.class);

            assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.STK_MOTIF_REQUIRED);
            verify(mouvementStockRepository, never()).save(any());
        }

        @Test
        @DisplayName("✅ CORRECTION_NEG avec motif nominatif")
        void shouldAcceptCorrectionNegativeWithMotif() {
            when(mouvementStockRepository.referenceValide(ARTICLE_ID, ENTREPRISE_ID, UTILISATEUR_ID))
                    .thenReturn(true);
            when(mouvementStockRepository.calculerStockReel(ENTREPRISE_ID, ARTICLE_ID))
                    .thenReturn(new BigDecimal("10"));
            when(mouvementStockRepository.save(any(com.stockmaster.stock.domain.entity.MouvementStock.class)))
                    .thenAnswer(inv -> {
                        var m = inv.getArgument(0);
                        org.springframework.test.util.ReflectionTestUtils.setField(m, "id", MOUVEMENT_ID);
                        return m;
                    });

            var resultat = service.enregistrer(
                    requete(TypeMouvement.CORRECTION_NEG, "3", OrigineType.CORRECTION, "casse constatée"));

            assertThat(resultat.stockApres()).isEqualByComparingTo("7");
        }

        @Test
        @DisplayName("❌ CORRECTION_POS avec origineType ≠ CORRECTION → SYS_002 (400)")
        void shouldRejectCorrectionWithWrongOrigin() {
            var thrown = catchThrowableOfType(
                    () -> service.enregistrer(
                            requete(TypeMouvement.CORRECTION_POS, "5", OrigineType.VENTE, "m")),
                    BusinessException.class);

            assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.SYS_BAD_REQUEST);
        }
    }

    // ============================================================
    // ❌ Isolation multi-tenant
    // ============================================================
    @Nested
    @DisplayName("Isolation multi-tenant ❌")
    class IsolationMultiTenant {

        @Test
        @DisplayName("❌ article d'un autre groupe / inconnu / utilisateur d'une autre entreprise → RES_001 (404)")
        void shouldRejectForeignReference() {
            when(mouvementStockRepository.referenceValide(ARTICLE_ID, ENTREPRISE_ID, UTILISATEUR_ID))
                    .thenReturn(false);

            var thrown = catchThrowableOfType(
                    () -> service.enregistrer(
                            requete(TypeMouvement.ENTREE, "5", OrigineType.CORRECTION, "m")),
                    BusinessException.class);

            assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND);
            verify(mouvementStockRepository, never()).save(any());
        }
    }

    // ============================================================
    // ❌ Invariant DEC-023 — stock jamais négatif
    // ============================================================
    @Nested
    @DisplayName("Invariant stock jamais négatif ❌")
    class InvariantNonNegatif {

        @Test
        @DisplayName("❌ SORTIE de 10 sur un stock de 5 → STK_001 (409), rien n'écrit")
        void shouldRejectSortieBeyondStock() {
            when(mouvementStockRepository.referenceValide(ARTICLE_ID, ENTREPRISE_ID, UTILISATEUR_ID))
                    .thenReturn(true);
            when(mouvementStockRepository.calculerStockReel(ENTREPRISE_ID, ARTICLE_ID))
                    .thenReturn(new BigDecimal("5"));

            var thrown = catchThrowableOfType(
                    () -> service.enregistrer(
                            requete(TypeMouvement.SORTIE, "10", OrigineType.VENTE, null)),
                    InsufficientStockException.class);

            assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.STK_INSUFFICIENT_STOCK);
            verify(mouvementStockRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ CORRECTION_NEG au-delà du stock → STK_001 (409) — plafonnée au stock actuel (déc. 12-journal)")
        void shouldRejectCorrectionNegativeBeyondStock() {
            when(mouvementStockRepository.referenceValide(ARTICLE_ID, ENTREPRISE_ID, UTILISATEUR_ID))
                    .thenReturn(true);
            when(mouvementStockRepository.calculerStockReel(ENTREPRISE_ID, ARTICLE_ID))
                    .thenReturn(BigDecimal.ONE);

            var thrown = catchThrowableOfType(
                    () -> service.enregistrer(
                            requete(TypeMouvement.CORRECTION_NEG, "2", OrigineType.CORRECTION, "m")),
                    InsufficientStockException.class);

            assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.STK_INSUFFICIENT_STOCK);
        }

        @Test
        @DisplayName("✅ SORTIE exactement au niveau du stock → stock final 0")
        void shouldAcceptSortieDownToZero() {
            when(mouvementStockRepository.referenceValide(ARTICLE_ID, ENTREPRISE_ID, UTILISATEUR_ID))
                    .thenReturn(true);
            when(mouvementStockRepository.calculerStockReel(ENTREPRISE_ID, ARTICLE_ID))
                    .thenReturn(new BigDecimal("5"));
            when(mouvementStockRepository.save(any(com.stockmaster.stock.domain.entity.MouvementStock.class)))
                    .thenAnswer(inv -> {
                        var m = inv.getArgument(0);
                        org.springframework.test.util.ReflectionTestUtils.setField(m, "id", MOUVEMENT_ID);
                        return m;
                    });

            var resultat = service.enregistrer(
                    requete(TypeMouvement.SORTIE, "5", OrigineType.VENTE, null));

            assertThat(resultat.stockApres()).isEqualByComparingTo("0");
        }
    }
}
