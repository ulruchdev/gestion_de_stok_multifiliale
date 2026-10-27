package com.stockmaster.tiers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockmaster.tiers.TiersTestApplication;
import com.stockmaster.tiers.dto.request.CreerFournisseurRequest;
import com.stockmaster.tiers.dto.response.FournisseurResponse;
import com.stockmaster.tiers.service.FournisseurService;
import com.stockmaster.shared.security.StockMasterPrincipal;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-036 — {@code POST /api/v1/fournisseurs}.
 *
 * <p>Contrats HTTP verrouillés :</p>
 * <ul>
 *   <li>{@code 201} + enveloppe {@code ApiResponse} sur succès ;</li>
 *   <li>{@code 400 SYS_005} raisonSociale vide / telephone hors format ;</li>
 *   <li>{@code 403} sur rôle non autorisé ({@code @PreAuthorize} CA1).</li>
 * </ul>
 *
 * <p>Le service est mocké : ce test ne porte que le contrat HTTP + RBAC.</p>
 */
@WebMvcTest
@ContextConfiguration(classes = TiersTestApplication.class)
@Import(FournisseurController.class)
@DisplayName("FournisseurController — US-036 : POST /api/v1/fournisseurs")
class FournisseurControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private FournisseurService fournisseurService;

    @Captor
    private ArgumentCaptor<CreerFournisseurRequest> requestCaptor;

    private static final Long GROUP_ID = 1L;
    private static final Long ENTREPRISE_ID = 10L;

    // ─── helpers ─────────────────────────────────────────────────────────────

    private TestingAuthenticationToken asAuthentication(String role, Long groupId, Long entrepriseId) {
        Claims claims = mock(Claims.class);
        when(claims.get("groupId", Long.class)).thenReturn(groupId);
        when(claims.get("entrepriseId", Long.class)).thenReturn(entrepriseId);
        when(claims.get("role", String.class)).thenReturn(role);
        return new TestingAuthenticationToken(
                new StockMasterPrincipal(99L, claims), null, "ROLE_" + role);
    }

    private String corpsValide() {
        return "{\"raisonSociale\":\"SARL Import Golfe\","
                + "\"telephone\":\"+237691234567\","
                + "\"nif\":\"M0126000001\","
                + "\"contact\":\"Jean Etoundi\","
                + "\"email\":\"achats@importgolfe.cm\","
                + "\"adresseVille\":\"Douala\"}";
    }

    private FournisseurResponse reponseCreee() {
        return FournisseurResponse.builder()
                .id(300L)
                .raisonSociale("SARL Import Golfe")
                .telephone("+237691234567")
                .nif("M0126000001")
                .contact("Jean Etoundi")
                .email("achats@importgolfe.cm")
                .adresseVille("Douala")
                .entrepriseId(ENTREPRISE_ID)
                .build();
    }

    // ─── création ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Création")
    class Creation {

        @ParameterizedTest(name = "✅ 201 — rôle {0}")
        @ValueSource(strings = {"RESP_ACHATS", "ADMIN_FILIALE", "ADMIN_GROUPE"})
        @DisplayName("✅ 201 — fournisseur créé, enveloppe ApiResponse")
        void shouldReturn201WhenRoleAutorise(String role) throws Exception {
            when(fournisseurService.creer(any(StockMasterPrincipal.class), any(CreerFournisseurRequest.class)))
                    .thenReturn(reponseCreee());

            mockMvc.perform(post("/api/v1/fournisseurs")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication(role, GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(corpsValide()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(300))
                    .andExpect(jsonPath("$.data.raisonSociale").value("SARL Import Golfe"))
                    .andExpect(jsonPath("$.data.telephone").value("+237691234567"))
                    .andExpect(jsonPath("$.data.entrepriseId").value(ENTREPRISE_ID));

            verify(fournisseurService).creer(any(StockMasterPrincipal.class), requestCaptor.capture());
            assertThat(requestCaptor.getValue().getRaisonSociale()).isEqualTo("SARL Import Golfe");
        }

        @Test
        @DisplayName("✅ 201 — un entrepriseId forcé dans le payload n'existe pas dans le DTO")
        void shouldReturn201WhenPayloadContientEntrepriseIdEtranger() throws Exception {
            when(fournisseurService.creer(any(StockMasterPrincipal.class), any(CreerFournisseurRequest.class)))
                    .thenReturn(reponseCreee());

            String corpsAvecAutreTenant = corpsValide().replaceFirst("\\{",
                    "{\"entrepriseId\":20,");

            mockMvc.perform(post("/api/v1/fournisseurs")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("RESP_ACHATS", GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(corpsAvecAutreTenant))
                    .andExpect(status().isCreated());

            verify(fournisseurService).creer(any(StockMasterPrincipal.class), requestCaptor.capture());
            assertThat(Arrays.stream(CreerFournisseurRequest.class.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet()))
                    .isNotEmpty()
                    .doesNotContain("entrepriseId", "entreprise", "groupId");
        }
    }

    // ─── accès refusé (CA1) ──────────────────────────────────────────────────

    @Nested
    @DisplayName("CA1 — accès refusé")
    class AccesRefuse {

        @ParameterizedTest(name = "❌ 403 — rôle {0}")
        @ValueSource(strings = {"CAISSIER", "COMMERCIAL", "GESTIONNAIRE_STOCK"})
        @DisplayName("❌ 403 — rôle hors périmètre achats")
        void shouldReturn403WhenRoleNonAutorise(String role) throws Exception {
            mockMvc.perform(post("/api/v1/fournisseurs")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication(role, GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(corpsValide()))
                    .andExpect(status().isForbidden());

            verify(fournisseurService, never()).creer(any(), any());
        }

        @Test
        @DisplayName("❌ 4xx — non authentifié")
        void shouldRefuserSansAuthentification() throws Exception {
            mockMvc.perform(post("/api/v1/fournisseurs")
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(corpsValide()))
                    .andExpect(status().is4xxClientError());

            verify(fournisseurService, never()).creer(any(), any());
        }
    }

    // ─── validation (CA3) ────────────────────────────────────────────────────

    @Nested
    @DisplayName("CA3 — validation du corps")
    class Validation {

        @Test
        @DisplayName("❌ 400 SYS_005 — raisonSociale vide")
        void shouldReturn400WhenRaisonSocialeVide() throws Exception {
            mockMvc.perform(post("/api/v1/fournisseurs")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("RESP_ACHATS", GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"raisonSociale\":\"   \"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SYS_005"));

            verify(fournisseurService, never()).creer(any(), any());
        }

        @Test
        @DisplayName("❌ 400 SYS_005 — telephone hors format camerounais")
        void shouldReturn400WhenTelephoneInvalide() throws Exception {
            mockMvc.perform(post("/api/v1/fournisseurs")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("RESP_ACHATS", GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"raisonSociale\":\"SARL Import Golfe\","
                                    + "\"telephone\":\"691234567\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SYS_005"));

            verify(fournisseurService, never()).creer(any(), any());
        }
    }
}
