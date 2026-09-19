package com.stockmaster.utilisateur.service;

import com.stockmaster.shared.domain.entity.Entreprise;
import com.stockmaster.shared.domain.entity.TenantGroup;
import com.stockmaster.shared.domain.entity.Utilisateur;
import com.stockmaster.shared.domain.enums.PlanAbonnement;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import com.stockmaster.shared.domain.enums.TypeEntreprise;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.repository.UtilisateurRepository;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.shared.storage.MinioService;
import com.stockmaster.utilisateur.dto.request.ProfilUtilisateurRequest;
import com.stockmaster.utilisateur.dto.response.UtilisateurProfilResponse;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * US-026 — consulter et modifier son profil. Contrats verrouillés :
 *
 * <ul>
 *   <li><b>Self-service</b> : le service travaille TOUJOURS sur
 *       {@code principal.getUserId()} — aucun paramètre id externe, donc
 *       chaque utilisateur ne peut modifier que son propre profil ;</li>
 *   <li><b>Email et rôle non modifiables</b> (critère d'acceptation US-026) :
 *       le DTO de requête ne les porte pas et le service ne les touche jamais —
 *       la modification admin passe par US-024 ;</li>
 *   <li><b>Photo uploadée vers MinIO</b> (critère d'acceptation US-026) :
 *       délégation à {@link MinioService} avec préfixe {@code utilisateur/<id>},
 *       URL persistée en base ; l'upload a lieu AVANT le save (échec MinIO →
 *       rien n'est persisté) ;</li>
 *   <li><b>Sémantique PATCH</b> (convention US-024) : champs null = inchangés,
 *       photo absente = photo actuelle conservée.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UtilisateurProfilService — US-026 : consulter et modifier son profil")
class UtilisateurProfilServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private MinioService minioService;
    @InjectMocks
    private UtilisateurProfilService utilisateurProfilService;

    @Captor
    private ArgumentCaptor<Utilisateur> utilisateurCaptor;

    // ─── fixtures ───────────────────────────────────────────────────────────

    private static final Long MOI_ID = 42L;
    private static final Long FILIALE_ID = 10L;

    private StockMasterPrincipal principal(RoleUtilisateur role) {
        Claims claims = mock(Claims.class);
        lenient().when(claims.get("groupId", Long.class)).thenReturn(1L);
        lenient().when(claims.get("entrepriseId", Long.class)).thenReturn(FILIALE_ID);
        lenient().when(claims.get("role", String.class)).thenReturn(role.name());
        return new StockMasterPrincipal(MOI_ID, claims);
    }

    private Utilisateur moi() {
        Utilisateur u = new Utilisateur();
        u.setId(MOI_ID);
        u.setEmail("employe@boutique.cm");
        u.setPrenom("Jean");
        u.setNom("Mbarga");
        u.setRole(RoleUtilisateur.CAISSIER);
        u.setActif(true);
        u.setAdresseVille("Douala");
        u.setPhoto(null);
        Entreprise filiale = new Entreprise();
        filiale.setId(FILIALE_ID);
        filiale.setNom("Filiale 10");
        filiale.setTypeEntreprise(TypeEntreprise.FILIALE);
        filiale.setGroupe(new TenantGroup());
        filiale.getGroupe().setId(1L);
        filiale.getGroupe().setPlanAbonnement(PlanAbonnement.GRATUIT);
        u.setEntreprise(filiale);
        return u;
    }

    private void compteTrouve(Utilisateur u) {
        lenient().when(utilisateurRepository.findById(MOI_ID)).thenReturn(Optional.of(u));
        lenient().when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private ProfilUtilisateurRequest requete(String prenom, String nom, String ville) {
        ProfilUtilisateurRequest r = new ProfilUtilisateurRequest();
        r.setPrenom(prenom);
        r.setNom(nom);
        r.setAdresseVille(ville);
        return r;
    }

    private MockMultipartFile photoPng() {
        return new MockMultipartFile("photo", "avatar.png", "image/png", new byte[]{1, 2, 3});
    }

    @BeforeEach
    void urlMinioParDefaut() {
        lenient().when(minioService.uploadImage(anyString(), any(MultipartFile.class)))
                .thenReturn("http://minio:9000/stockmaster/utilisateur/42/uuid.png");
    }

    // ─── consultation ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Consultation — GET /profil")
    class Consultation {

        @Test
        @DisplayName("✅ retourne MON profil (principal.userId), sans aucun champ secret")
        void shouldRetournerMonProfil() {
            Utilisateur moi = moi();
            compteTrouve(moi);

            UtilisateurProfilResponse response = utilisateurProfilService.consulter(principal(RoleUtilisateur.CAISSIER));

            assertThat(response.getId()).isEqualTo(MOI_ID);
            assertThat(response.getEmail()).isEqualTo("employe@boutique.cm");
            assertThat(response.getPrenom()).isEqualTo("Jean");
            assertThat(response.getNom()).isEqualTo("Mbarga");
            assertThat(response.getRole()).isEqualTo(RoleUtilisateur.CAISSIER);
            assertThat(response.getActif()).isTrue();
            assertThat(response.getAdresseVille()).isEqualTo("Douala");
        }

        @Test
        @DisplayName("❌ 404 — compte soft-supprimé (uniforme, jamais révéler l'existence)")
        void shouldRejeterCompteSupprimeEnConsultation() {
            Utilisateur moi = moi();
            moi.setSupprime(true);
            when(utilisateurRepository.findById(MOI_ID)).thenReturn(Optional.of(moi));

            assertThatThrownBy(() -> utilisateurProfilService.consulter(principal(RoleUtilisateur.CAISSIER)))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND));
        }
    }

    // ─── modification ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Modification — PUT /profil")
    class Modification {

        @Test
        @DisplayName("✅ prenom/nom/ville fournis → mis à jour et sauvegardés")
        void shouldMettreAJourChampsFournis() {
            Utilisateur moi = moi();
            compteTrouve(moi);

            UtilisateurProfilResponse response = utilisateurProfilService.mettreAJour(
                    principal(RoleUtilisateur.GESTIONNAIRE_STOCK), requete("Jean-Paul", "Mbarga", "Yaoundé"), null);

            verify(utilisateurRepository).save(utilisateurCaptor.capture());
            assertThat(utilisateurCaptor.getValue().getPrenom()).isEqualTo("Jean-Paul");
            assertThat(utilisateurCaptor.getValue().getNom()).isEqualTo("Mbarga");
            assertThat(utilisateurCaptor.getValue().getAdresseVille()).isEqualTo("Yaoundé");
            assertThat(response.getPrenom()).isEqualTo("Jean-Paul");
        }

        @Test
        @DisplayName("✅ champs null = inchangés (sémantique PATCH, convention US-024)")
        void shouldConserverChampsNull() {
            Utilisateur moi = moi();
            compteTrouve(moi);

            utilisateurProfilService.mettreAJour(principal(RoleUtilisateur.GESTIONNAIRE_STOCK), requete(null, "Mbarga", null), null);

            verify(utilisateurRepository).save(utilisateurCaptor.capture());
            assertThat(utilisateurCaptor.getValue().getPrenom()).isEqualTo("Jean");
            assertThat(utilisateurCaptor.getValue().getAdresseVille()).isEqualTo("Douala");
        }

        @Test
        @DisplayName("✅ email et rôle JAMAIS modifiés via /profil (réservés à l'admin US-024)")
        void shouldJamaisModifierEmailNiRole() {
            Utilisateur moi = moi();
            compteTrouve(moi);

            utilisateurProfilService.mettreAJour(principal(RoleUtilisateur.GESTIONNAIRE_STOCK), requete("Jean-Paul", null, null), null);

            verify(utilisateurRepository).save(utilisateurCaptor.capture());
            assertThat(utilisateurCaptor.getValue().getEmail()).isEqualTo("employe@boutique.cm");
            assertThat(utilisateurCaptor.getValue().getRole()).isEqualTo(RoleUtilisateur.CAISSIER);
        }

        @Test
        @DisplayName("✅ photo fournie → upload MinIO préfixé utilisateur/<id>, URL persistée")
        void shouldUploaderPhotoVersMinio() {
            Utilisateur moi = moi();
            compteTrouve(moi);
            MockMultipartFile photo = photoPng();

            UtilisateurProfilResponse response = utilisateurProfilService.mettreAJour(
                    principal(RoleUtilisateur.GESTIONNAIRE_STOCK), requete(null, null, null), photo);

            verify(minioService).uploadImage(eq("utilisateur/" + MOI_ID), eq(photo));
            verify(utilisateurRepository).save(utilisateurCaptor.capture());
            assertThat(utilisateurCaptor.getValue().getPhoto())
                    .isEqualTo("http://minio:9000/stockmaster/utilisateur/42/uuid.png");
            assertThat(response.getPhoto()).isEqualTo("http://minio:9000/stockmaster/utilisateur/42/uuid.png");
        }

        @Test
        @DisplayName("✅ photo absente → MinIO jamais appelé, photo actuelle conservée")
        void shouldNePasToucherMinioSansPhoto() {
            Utilisateur moi = moi();
            moi.setPhoto("http://minio:9000/stockmaster/utilisateur/42/ancienne.png");
            compteTrouve(moi);

            UtilisateurProfilResponse response = utilisateurProfilService.mettreAJour(
                    principal(RoleUtilisateur.GESTIONNAIRE_STOCK), requete(null, "NouveauNom", null), null);

            verify(minioService, never()).uploadImage(anyString(), any(MultipartFile.class));
            verify(utilisateurRepository).save(utilisateurCaptor.capture());
            assertThat(utilisateurCaptor.getValue().getPhoto())
                    .isEqualTo("http://minio:9000/stockmaster/utilisateur/42/ancienne.png");
            assertThat(response.getPhoto()).isEqualTo("http://minio:9000/stockmaster/utilisateur/42/ancienne.png");
        }

        @Test
        @DisplayName("✅ échec MinIO → rien n'est persisté (upload AVANT le save)")
        void shouldNeRienPersisterSiEchecMinio() {
            Utilisateur moi = moi();
            compteTrouve(moi);
            when(minioService.uploadImage(anyString(), any(MultipartFile.class)))
                    .thenThrow(new BusinessException(ErrorCode.SYS_INTERNAL_ERROR));

            assertThatThrownBy(() -> utilisateurProfilService.mettreAJour(
                    principal(RoleUtilisateur.GESTIONNAIRE_STOCK), requete("Jean-Paul", null, null), photoPng()))
                    .isInstanceOf(BusinessException.class);

            verify(utilisateurRepository, never()).save(any(Utilisateur.class));
        }

        @Test
        @DisplayName("❌ 404 — compte soft-supprimé (uniforme, en modification aussi)")
        void shouldRejeterCompteSupprimeEnModification() {
            Utilisateur moi = moi();
            moi.setSupprime(true);
            when(utilisateurRepository.findById(MOI_ID)).thenReturn(Optional.of(moi));

            assertThatThrownBy(() -> utilisateurProfilService.mettreAJour(
                    principal(RoleUtilisateur.GESTIONNAIRE_STOCK), requete("Jean-Paul", null, null), null))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RES_ENTITY_NOT_FOUND));
        }
    }
}
