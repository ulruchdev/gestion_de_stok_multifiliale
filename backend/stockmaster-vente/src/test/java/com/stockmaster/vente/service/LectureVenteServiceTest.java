package com.stockmaster.vente.service;

import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.vente.port.LectureVentePort.LigneVenteAgregat;
import com.stockmaster.vente.repository.LigneVenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests du port de lecture des ventes — GS-1251 (socle reporting R0).
 *
 * <p>Les filtres métier (statut {@code PAYEE}, période, filiale) sont portés par
 * le JPQL du repository : ces tests vérifient les arguments transmis, la
 * projection des lignes brutes et les garde-fous de période / périmètre.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LectureVenteService — GS-1251 : lecture reporting des ventes")
class LectureVenteServiceTest {

    @Mock
    private LigneVenteRepository ligneVenteRepository;

    private LectureVenteService lectureVenteService;

    private static final Long ENTREPRISE_ID = 10L;
    private static final Instant DEBUT = Instant.parse("2026-11-01T00:00:00Z");
    private static final Instant FIN = Instant.parse("2026-12-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        lectureVenteService = new LectureVenteService(ligneVenteRepository);
    }

    /** Ligne brute : [vente, entreprise, article, libellé, clientId, clientNom, qté, PU, TVA, date]. */
    private Object[] ligne(long venteId, long entrepriseId, long articleId, String libelle,
                           Long clientId, String clientNom, String quantite,
                           int prixUnitaire, String tauxTva, Instant dateVente) {
        return new Object[]{venteId, entrepriseId, articleId, libelle, clientId, clientNom,
                new BigDecimal(quantite), prixUnitaire, new BigDecimal(tauxTva), dateVente};
    }

    @Nested
    @DisplayName("Lecture filiale — lignesVendues")
    class LignesVendues {

        @Test
        @DisplayName("✅ projection complète d'une ligne avec client identifié")
        void shouldProjeterLigneComplete() {
            Instant dateVente = Instant.parse("2026-11-12T09:30:00Z");
            when(ligneVenteRepository.lignesVendues(List.of(ENTREPRISE_ID), DEBUT, FIN))
                    .thenReturn(List.<Object[]>of(ligne(100L, ENTREPRISE_ID, 5L, "Riz sac 25 kg",
                            7L, "Alice Ngo", "2.500", 12000, "19.25", dateVente)));

            List<LigneVenteAgregat> lignes = lectureVenteService
                    .lignesVendues(ENTREPRISE_ID, DEBUT, FIN);

            assertThat(lignes).hasSize(1);
            LigneVenteAgregat ligne = lignes.get(0);
            assertThat(ligne.venteId()).isEqualTo(100L);
            assertThat(ligne.entrepriseId()).isEqualTo(ENTREPRISE_ID);
            assertThat(ligne.articleId()).isEqualTo(5L);
            assertThat(ligne.articleLibelle()).isEqualTo("Riz sac 25 kg");
            assertThat(ligne.clientId()).isEqualTo(7L);
            assertThat(ligne.clientNom()).isEqualTo("Alice Ngo");
            assertThat(ligne.quantite()).isEqualByComparingTo("2.500");
            assertThat(ligne.prixUnitaireHt()).isEqualTo(12000);
            assertThat(ligne.tauxTva()).isEqualByComparingTo("19.25");
            assertThat(ligne.dateVente()).isEqualTo(dateVente);
        }

        @Test
        @DisplayName("✅ client de passage conservé : clientId/clientNom null (pas d'exclusion du CA)")
        void shouldConserverClientDePassage() {
            when(ligneVenteRepository.lignesVendues(List.of(ENTREPRISE_ID), DEBUT, FIN))
                    .thenReturn(List.<Object[]>of(ligne(101L, ENTREPRISE_ID, 6L, "Huile 1 L",
                            null, null, "1.000", 900, "19.25", FIN.minusSeconds(3600))));

            List<LigneVenteAgregat> lignes = lectureVenteService
                    .lignesVendues(ENTREPRISE_ID, DEBUT, FIN);

            assertThat(lignes.get(0).clientId()).isNull();
            assertThat(lignes.get(0).clientNom()).isNull();
        }

        @Test
        @DisplayName("✅ aucun paramètre de tenant dans la signature : la filiale est celle reçue")
        void shouldTransmettreLaFilialeDemandee() {
            when(ligneVenteRepository.lignesVendues(List.of(ENTREPRISE_ID), DEBUT, FIN))
                    .thenReturn(List.of());

            lectureVenteService.lignesVendues(ENTREPRISE_ID, DEBUT, FIN);

            verify(ligneVenteRepository).lignesVendues(List.of(ENTREPRISE_ID), DEBUT, FIN);
        }

        @Test
        @DisplayName("✅ période sans vente : liste vide, pas d'erreur")
        void shouldRetournerListeVide() {
            when(ligneVenteRepository.lignesVendues(List.of(ENTREPRISE_ID), DEBUT, FIN))
                    .thenReturn(List.of());

            assertThat(lectureVenteService.lignesVendues(ENTREPRISE_ID, DEBUT, FIN)).isEmpty();
        }

        @Test
        @DisplayName("❌ 400 SYS_002 — entrepriseId null, avant toute I/O")
        void shouldRefuserEntrepriseNulle() {
            var ex = catchThrowableOfType(
                    () -> lectureVenteService.lignesVendues(null, DEBUT, FIN),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SYS_BAD_REQUEST);
            verify(ligneVenteRepository, never()).lignesVendues(anyList(), any(), any());
        }

        @Test
        @DisplayName("❌ 400 SYS_002 — borne de période nulle, avant toute I/O")
        void shouldRefuserBorneNulle() {
            var ex = catchThrowableOfType(
                    () -> lectureVenteService.lignesVendues(ENTREPRISE_ID, DEBUT, null),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SYS_BAD_REQUEST);
            verify(ligneVenteRepository, never()).lignesVendues(anyList(), any(), any());
        }

        @Test
        @DisplayName("❌ 400 SYS_002 — borne haute non strictement postérieure")
        void shouldRefuserPeriodeInversee() {
            var ex = catchThrowableOfType(
                    () -> lectureVenteService.lignesVendues(ENTREPRISE_ID, FIN, DEBUT),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SYS_BAD_REQUEST);
            verify(ligneVenteRepository, never()).lignesVendues(anyList(), any(), any());
        }
    }

    @Nested
    @DisplayName("Lecture groupe — lignesVenduesGroupe")
    class LignesVenduesGroupe {

        @Test
        @DisplayName("✅ plusieurs filiales transmises en une requête")
        void shouldLirePlusieursFiliales() {
            List<Long> filiales = List.of(10L, 11L, 12L);
            when(ligneVenteRepository.lignesVendues(filiales, DEBUT, FIN))
                    .thenReturn(List.<Object[]>of(
                            ligne(200L, 10L, 5L, "Riz", 7L, "Alice", "1.000", 12000, "19.25", DEBUT),
                            ligne(201L, 12L, 5L, "Riz", 8L, "Bob", "3.000", 12000, "19.25", DEBUT)));

            List<LigneVenteAgregat> lignes = lectureVenteService
                    .lignesVenduesGroupe(filiales, DEBUT, FIN);

            assertThat(lignes).hasSize(2);
            assertThat(lignes).extracting(LigneVenteAgregat::entrepriseId)
                    .containsExactly(10L, 12L);
            verify(ligneVenteRepository).lignesVendues(filiales, DEBUT, FIN);
        }

        @Test
        @DisplayName("❌ 400 SYS_002 — liste de filiales vide")
        void shouldRefuserListeVide() {
            var ex = catchThrowableOfType(
                    () -> lectureVenteService.lignesVenduesGroupe(List.of(), DEBUT, FIN),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SYS_BAD_REQUEST);
            verify(ligneVenteRepository, never()).lignesVendues(anyList(), any(), any());
        }

        @Test
        @DisplayName("❌ 400 SYS_002 — filiale null dans la liste")
        void shouldRefuserFilialeNulle() {
            var ex = catchThrowableOfType(
                    () -> lectureVenteService.lignesVenduesGroupe(
                            java.util.Arrays.asList(10L, null), DEBUT, FIN),
                    BusinessException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SYS_BAD_REQUEST);
            verify(ligneVenteRepository, never()).lignesVendues(anyList(), any(), any());
        }
    }
}
