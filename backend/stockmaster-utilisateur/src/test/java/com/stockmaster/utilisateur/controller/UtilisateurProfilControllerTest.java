package com.stockmaster.utilisateur.controller;

import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.UtilisateurTestApplication;
import com.stockmaster.utilisateur.dto.response.UtilisateurProfilResponse;
import com.stockmaster.utilisateur.service.UtilisateurProfilService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-026 — {@code GET /api/v1/utilisateurs/profil} et
 * {@code PUT /api/v1/utilisateurs/profil}. Tests contrôleur : codes HTTP,
 * enveloppe ApiResponse, self-service (authentification obligatoire),
 * multipart JSON + photo.
 */
@WebMvcTest
@ContextConfiguration(classes = UtilisateurTestApplication.class)
@Import(UtilisateurProfilController.class)
@DisplayName("UtilisateurProfilController — US-026 : GET/PUT /api/v1/utilisateurs/profil")
class UtilisateurProfilControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UtilisateurProfilService utilisateurProfilService;

    // ─── helpers ────────────────────────────────────────────────────────────

    private TestingAuthenticationToken asAuthentication(RoleUtilisateur role) {
        Claims claims = mock(Claims.class);
        when(claims.get("groupId", Long.class)).thenReturn(1L);
        when(claims.get("entrepriseId", Long.class)).thenReturn(10L);
        return new TestingAuthenticationToken(new StockMasterPrincipal(42L, claims), null, "ROLE_" + role.name());
    }

    private UtilisateurProfilResponse profilResponse() {
        return UtilisateurProfilResponse.builder()
                .id(42L)
                .email("employe@boutique.cm")
                .prenom("Jean").nom("Mbarga")
                .role(RoleUtilisateur.CAISSIER)
                .actif(true)
                .adresseVille("Douala")
                .photo("http://minio:9000/stockmaster/utilisateur/42/uuid.png")
                .build();
    }

    private static final MockMultipartFile PHOTO =
            new MockMultipartFile("photo", "avatar.png", "image/png", new byte[]{1, 2, 3});

    // ─── consultation ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /profil")
    class Consultation {

        @Test
        @DisplayName("✅ 200 — ApiResponse avec mon profil, tous rôles admis")
        void shouldConsulterAvec200() throws Exception {
            when(utilisateurProfilService.consulter(any(StockMasterPrincipal.class))).thenReturn(profilResponse());

            mockMvc.perform(get("/api/v1/utilisateurs/profil")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication(RoleUtilisateur.GESTIONNAIRE_STOCK))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(42))
                    .andExpect(jsonPath("$.data.email").value("employe@boutique.cm"))
                    .andExpect(jsonPath("$.data.adresseVille").value("Douala"))
                    .andExpect(jsonPath("$.data.motDePasse").doesNotExist());
        }

        @Test
        @DisplayName("❌ refusé sans authentification (endpoint jamais public)")
        void shouldRejeterSansAuthentification() throws Exception {
            mockMvc.perform(get("/api/v1/utilisateurs/profil"))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── modification multipart ─────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /profil — multipart JSON + photo")
    class Modification {

        @Test
        @DisplayName("✅ 200 — part JSON + photo : ApiResponse avec profil à jour")
        void shouldModifierAvecPhoto() throws Exception {
            when(utilisateurProfilService.mettreAJour(
                    any(StockMasterPrincipal.class),
                    any(com.stockmaster.utilisateur.dto.request.ProfilUtilisateurRequest.class),
                    any(org.springframework.web.multipart.MultipartFile.class)))
                    .thenReturn(profilResponse());

            MockMultipartFile partJson = new MockMultipartFile("profil", null,
                    MediaType.APPLICATION_JSON_VALUE,
                    "{\"prenom\":\"Jean-Paul\",\"nom\":\"Mbarga\",\"adresseVille\":\"Yaoundé\"}".getBytes());

            mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/utilisateurs/profil")
                            .file(partJson)
                            .file(PHOTO)
                            .with(csrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication(RoleUtilisateur.GESTIONNAIRE_STOCK)))
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(42))
                    .andExpect(jsonPath("$.data.photo").value("http://minio:9000/stockmaster/utilisateur/42/uuid.png"));
        }

        @Test
        @DisplayName("✅ 200 — JSON seul, sans photo (champs null = inchangés)")
        void shouldModifierSansPhoto() throws Exception {
            when(utilisateurProfilService.mettreAJour(
                    any(StockMasterPrincipal.class),
                    any(com.stockmaster.utilisateur.dto.request.ProfilUtilisateurRequest.class),
                    eq(null)))
                    .thenReturn(profilResponse());

            MockMultipartFile partJson = new MockMultipartFile("profil", null,
                    MediaType.APPLICATION_JSON_VALUE, "{\"nom\":\"Mbarga-Nkoulou\"}".getBytes());

            mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/utilisateurs/profil")
                            .file(partJson)
                            .with(csrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication(RoleUtilisateur.CAISSIER)))
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.nom").value("Mbarga"));
        }

        @Test
        @DisplayName("❌ 40x — sans authentification (401/403 selon le slice de sécurité)")
        void shouldRejeterModificationSansAuthentification() throws Exception {
            MockMultipartFile partJson = new MockMultipartFile("profil", null,
                    MediaType.APPLICATION_JSON_VALUE, "{\"nom\":\"X\"}".getBytes());

            mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/utilisateurs/profil")
                            .file(partJson)
                            .with(csrf())
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().is4xxClientError());
        }
    }
}
