package com.stockmaster.tiers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.exception.BusinessException;
import com.stockmaster.shared.exception.ErrorCode;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.tiers.TiersTestApplication;
import com.stockmaster.tiers.dto.request.CreerClientRequest;
import com.stockmaster.tiers.dto.request.ModifierClientRequest;
import com.stockmaster.tiers.dto.response.ClientResponse;
import com.stockmaster.tiers.service.ClientService;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-040 → US-043 — {@code /api/v1/clients}.
 *
 * <p>Contrats HTTP verrouillés :</p>
 * <ul>
 *   <li>{@code 201} / {@code 200} + enveloppe {@code ApiResponse} sur succès ;</li>
 *   <li>{@code 400 SYS_005} sur payload invalide (nom vide, téléphone hors E.164) ;</li>
 *   <li>{@code 403} sur rôle hors périmètre ({@code @PreAuthorize}) — le
 *       {@code CAISSIER} n'est admis que sur le {@code GET} (arbitrage du
 *       9 nov. 2026) ;</li>
 *   <li>{@code 404 RES_001} uniforme sur id inconnu / hors tenant ;</li>
 *   <li>{@code 409 RES_005} sur archivage d'un client ayant des commandes.</li>
 * </ul>
 *
 * <p>Le service est mocké : ce test ne porte que le contrat HTTP + RBAC.</p>
 */
@WebMvcTest
@ContextConfiguration(classes = TiersTestApplication.class)
@Import(ClientController.class)
@DisplayName("ClientController — US-040→043 : /api/v1/clients")
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ClientService clientService;

    @Captor
    private ArgumentCaptor<CreerClientRequest> requestCaptor;

    private static final Long GROUP_ID = 1L;
    private static final Long ENTREPRISE_ID = 10L;
    private static final Long ID_CIBLE = 200L;

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
        return "{\"nom\":\"Martin\","
                + "\"prenom\":\"Alice\","
                + "\"telephone\":\"+237691234567\","
                + "\"email\":\"alice.martin@example.cm\","
                + "\"adresseVille\":\"Douala\","
                + "\"adresseQuartier\":\"Bonapriso\"}";
    }

    private ClientResponse reponseClient() {
        return ClientResponse.builder()
                .id(300L)
                .nom("Martin")
                .prenom("Alice")
                .telephone("+237691234567")
                .email("alice.martin@example.cm")
                .adresseVille("Douala")
                .adresseQuartier("Bonapriso")
                .entrepriseId(ENTREPRISE_ID)
                .supprime(false)
                .build();
    }

    // ─── US-040 — création ───────────────────────────────────────────────────

    @Nested
    @DisplayName("US-040 — POST /api/v1/clients")
    class Creation {

        @ParameterizedTest(name = "✅ 201 — rôle {0}")
        @ValueSource(strings = {"COMMERCIAL", "ADMIN_FILIALE", "ADMIN_GROUPE"})
        @DisplayName("✅ 201 — client créé, enveloppe ApiResponse")
        void shouldReturn201WhenRoleAutorise(String role) throws Exception {
            when(clientService.creer(any(StockMasterPrincipal.class), any(CreerClientRequest.class)))
                    .thenReturn(reponseClient());

            mockMvc.perform(post("/api/v1/clients")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication(role, GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(corpsValide()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(300))
                    .andExpect(jsonPath("$.data.nom").value("Martin"))
                    .andExpect(jsonPath("$.data.prenom").value("Alice"))
                    .andExpect(jsonPath("$.data.telephone").value("+237691234567"))
                    .andExpect(jsonPath("$.data.entrepriseId").value(ENTREPRISE_ID))
                    .andExpect(jsonPath("$.data.supprime").value(false));

            verify(clientService).creer(any(StockMasterPrincipal.class), requestCaptor.capture());
            assertThat(requestCaptor.getValue().getNom()).isEqualTo("Martin");
            assertThat(requestCaptor.getValue().getAdresseQuartier()).isEqualTo("Bonapriso");
        }

        @ParameterizedTest(name = "❌ 403 — rôle {0}")
        @ValueSource(strings = {"CAISSIER", "RESP_ACHATS", "GESTIONNAIRE_STOCK"})
        @DisplayName("❌ 403 — rôle hors périmètre commercial")
        void shouldReturn403WhenRoleNonAutorise(String role) throws Exception {
            mockMvc.perform(post("/api/v1/clients")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication(role, GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(corpsValide()))
                    .andExpect(status().isForbidden());

            verify(clientService, never()).creer(any(), any());
        }

        @Test
        @DisplayName("❌ 4xx — non authentifié")
        void shouldRefuserSansAuthentification() throws Exception {
            mockMvc.perform(post("/api/v1/clients")
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(corpsValide()))
                    .andExpect(status().is4xxClientError());

            verify(clientService, never()).creer(any(), any());
        }

        @Test
        @DisplayName("❌ 400 SYS_005 — nom vide")
        void shouldReturn400WhenNomVide() throws Exception {
            mockMvc.perform(post("/api/v1/clients")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("COMMERCIAL", GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nom\":\"   \"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SYS_005"));

            verify(clientService, never()).creer(any(), any());
        }

        @Test
        @DisplayName("❌ 400 SYS_005 — téléphone hors format E.164")
        void shouldReturn400WhenTelephoneInvalide() throws Exception {
            mockMvc.perform(post("/api/v1/clients")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("COMMERCIAL", GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nom\":\"Martin\",\"telephone\":\"691234567\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SYS_005"));

            verify(clientService, never()).creer(any(), any());
        }
    }

    // ─── US-041 — listing / recherche ────────────────────────────────────────

    @Nested
    @DisplayName("US-041 — GET /api/v1/clients")
    class Lister {

        private PageResponse<ClientResponse> pageDunClient() {
            return PageResponse.<ClientResponse>builder()
                    .content(List.of(reponseClient()))
                    .page(0)
                    .size(20)
                    .totalElements(1)
                    .totalPages(1)
                    .build();
        }

        @ParameterizedTest(name = "✅ 200 — rôle {0}")
        @ValueSource(strings = {"COMMERCIAL", "ADMIN_FILIALE", "ADMIN_GROUPE", "CAISSIER"})
        @DisplayName("✅ 200 — liste paginée du tenant, CAISSIER admis en lecture")
        void shouldReturn200WhenRoleAutorise(String role) throws Exception {
            when(clientService.lister(any(StockMasterPrincipal.class), eq("martin"), eq(0), eq(20)))
                    .thenReturn(pageDunClient());

            mockMvc.perform(get("/api/v1/clients?search=martin&page=0&size=20")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication(role, GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].id").value(300))
                    .andExpect(jsonPath("$.data.content[0].nom").value("Martin"))
                    .andExpect(jsonPath("$.data.content[0].entrepriseId").value(ENTREPRISE_ID))
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.size").value(20))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1));
        }

        @Test
        @DisplayName("✅ 200 — sans paramètre : search null, page 0, size 20 (défauts)")
        void shouldReturn200AvecLesDefauts() throws Exception {
            when(clientService.lister(any(StockMasterPrincipal.class), eq(null), eq(0), eq(20)))
                    .thenReturn(pageDunClient());

            mockMvc.perform(get("/api/v1/clients")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("COMMERCIAL", GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].nom").value("Martin"));
        }

        @Test
        @DisplayName("✅ 200 — recherche sans résultat → page vide (content = [], jamais 404)")
        void shouldReturn200WhenAucunResultat() throws Exception {
            when(clientService.lister(any(StockMasterPrincipal.class), eq("zzz"), eq(0), eq(20)))
                    .thenReturn(PageResponse.<ClientResponse>builder()
                            .content(List.of())
                            .page(0)
                            .size(20)
                            .totalElements(0)
                            .totalPages(0)
                            .build());

            mockMvc.perform(get("/api/v1/clients?search=zzz")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("COMMERCIAL", GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isEmpty())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @ParameterizedTest(name = "❌ 403 — rôle {0}")
        @ValueSource(strings = {"RESP_ACHATS", "GESTIONNAIRE_STOCK"})
        @DisplayName("❌ 403 — lecture hors périmètre commercial")
        void shouldReturn403WhenRoleNonAutorise(String role) throws Exception {
            mockMvc.perform(get("/api/v1/clients?search=martin")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication(role, GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isForbidden());

            verify(clientService, never()).lister(any(), any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("❌ 4xx — non authentifié")
        void shouldRefuserSansAuthentification() throws Exception {
            mockMvc.perform(get("/api/v1/clients")
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().is4xxClientError());

            verify(clientService, never()).lister(any(), any(), anyInt(), anyInt());
        }
    }

    // ─── US-042 — modification ───────────────────────────────────────────────

    @Nested
    @DisplayName("US-042 — PUT /api/v1/clients/{id}")
    class Modification {

        private ClientResponse reponseModifiee() {
            return ClientResponse.builder()
                    .id(ID_CIBLE)
                    .nom("Martin Ngo")
                    .prenom("Alicia")
                    .telephone("+237677445566")
                    .email("alicia.martin@example.cm")
                    .adresseVille("Yaounde")
                    .adresseQuartier("Bastos")
                    .entrepriseId(ENTREPRISE_ID)
                    .supprime(false)
                    .build();
        }

        @ParameterizedTest(name = "✅ 200 — rôle {0}")
        @ValueSource(strings = {"COMMERCIAL", "ADMIN_FILIALE", "ADMIN_GROUPE"})
        @DisplayName("✅ 200 — client modifié, enveloppe ApiResponse")
        void shouldReturn200WhenRoleAutorise(String role) throws Exception {
            when(clientService.modifier(any(StockMasterPrincipal.class), eq(ID_CIBLE), any()))
                    .thenReturn(reponseModifiee());

            mockMvc.perform(put("/api/v1/clients/" + ID_CIBLE)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication(role, GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nom\":\"Martin Ngo\",\"adresseVille\":\"Yaounde\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(ID_CIBLE))
                    .andExpect(jsonPath("$.data.nom").value("Martin Ngo"))
                    .andExpect(jsonPath("$.data.adresseVille").value("Yaounde"))
                    .andExpect(jsonPath("$.data.entrepriseId").value(ENTREPRISE_ID));

            verify(clientService).modifier(any(StockMasterPrincipal.class),
                    eq(ID_CIBLE), any(ModifierClientRequest.class));
        }

        @Test
        @DisplayName("❌ 400 SYS_005 — nom blanc")
        void shouldReturn400WhenNomBlanc() throws Exception {
            mockMvc.perform(put("/api/v1/clients/" + ID_CIBLE)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("COMMERCIAL", GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nom\":\"   \"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SYS_005"));

            verify(clientService, never()).modifier(any(), any(), any());
        }

        @Test
        @DisplayName("❌ 400 SYS_005 — téléphone hors format E.164")
        void shouldReturn400WhenTelephoneInvalide() throws Exception {
            mockMvc.perform(put("/api/v1/clients/" + ID_CIBLE)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("COMMERCIAL", GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"telephone\":\"691234567\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SYS_005"));

            verify(clientService, never()).modifier(any(), any(), any());
        }

        @Test
        @DisplayName("❌ 404 RES_001 — id inexistant / hors tenant (jamais 403)")
        void shouldReturn404WhenIdInexistant() throws Exception {
            when(clientService.modifier(any(StockMasterPrincipal.class), eq(9999L), any()))
                    .thenThrow(new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

            mockMvc.perform(put("/api/v1/clients/9999")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("COMMERCIAL", GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"adresseVille\":\"Yaounde\"}"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RES_001"));
        }

        @ParameterizedTest(name = "❌ 403 — rôle {0}")
        @ValueSource(strings = {"CAISSIER", "RESP_ACHATS", "GESTIONNAIRE_STOCK"})
        @DisplayName("❌ 403 — rôle hors périmètre commercial")
        void shouldReturn403WhenRoleNonAutorise(String role) throws Exception {
            mockMvc.perform(put("/api/v1/clients/" + ID_CIBLE)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication(role, GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"adresseVille\":\"Yaounde\"}"))
                    .andExpect(status().isForbidden());

            verify(clientService, never()).modifier(any(), any(), any());
        }

        @Test
        @DisplayName("❌ 4xx — non authentifié")
        void shouldRefuserSansAuthentification() throws Exception {
            mockMvc.perform(put("/api/v1/clients/" + ID_CIBLE)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"adresseVille\":\"Yaounde\"}"))
                    .andExpect(status().is4xxClientError());

            verify(clientService, never()).modifier(any(), any(), any());
        }
    }

    // ─── US-043 — suppression ────────────────────────────────────────────────

    @Nested
    @DisplayName("US-043 — DELETE /api/v1/clients/{id}")
    class Suppression {

        private ClientResponse reponseArchivee() {
            return ClientResponse.builder()
                    .id(ID_CIBLE)
                    .nom("Martin")
                    .prenom("Alice")
                    .telephone("+237691234567")
                    .entrepriseId(ENTREPRISE_ID)
                    .supprime(true)
                    .build();
        }

        @ParameterizedTest(name = "✅ 200 — rôle {0}")
        @ValueSource(strings = {"COMMERCIAL", "ADMIN_FILIALE", "ADMIN_GROUPE"})
        @DisplayName("✅ 200 — client archivé, supprime=true dans la réponse")
        void shouldReturn200WhenRoleAutorise(String role) throws Exception {
            when(clientService.supprimer(any(StockMasterPrincipal.class), eq(ID_CIBLE)))
                    .thenReturn(reponseArchivee());

            mockMvc.perform(delete("/api/v1/clients/" + ID_CIBLE)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication(role, GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(ID_CIBLE))
                    .andExpect(jsonPath("$.data.supprime").value(true));
        }

        @Test
        @DisplayName("❌ 409 RES_005 — historique de commandes")
        void shouldReturn409WhenDependances() throws Exception {
            when(clientService.supprimer(any(StockMasterPrincipal.class), eq(ID_CIBLE)))
                    .thenThrow(new BusinessException(ErrorCode.RES_ENTITY_HAS_DEPENDENCIES));

            mockMvc.perform(delete("/api/v1/clients/" + ID_CIBLE)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("COMMERCIAL", GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("RES_005"));
        }

        @Test
        @DisplayName("❌ 404 RES_001 — id inconnu / hors tenant / déjà archivé")
        void shouldReturn404WhenIdInexistant() throws Exception {
            when(clientService.supprimer(any(StockMasterPrincipal.class), eq(9999L)))
                    .thenThrow(new BusinessException(ErrorCode.RES_ENTITY_NOT_FOUND));

            mockMvc.perform(delete("/api/v1/clients/9999")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication("COMMERCIAL", GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RES_001"));
        }

        @ParameterizedTest(name = "❌ 403 — rôle {0}")
        @ValueSource(strings = {"CAISSIER", "RESP_ACHATS", "GESTIONNAIRE_STOCK"})
        @DisplayName("❌ 403 — rôle hors périmètre commercial")
        void shouldReturn403WhenRoleNonAutorise(String role) throws Exception {
            mockMvc.perform(delete("/api/v1/clients/" + ID_CIBLE)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(
                                    asAuthentication(role, GROUP_ID, ENTREPRISE_ID)))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isForbidden());

            verify(clientService, never()).supprimer(any(), any());
        }

        @Test
        @DisplayName("❌ 4xx — non authentifié")
        void shouldRefuserSansAuthentification() throws Exception {
            mockMvc.perform(delete("/api/v1/clients/" + ID_CIBLE)
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().is4xxClientError());

            verify(clientService, never()).supprimer(any(), any());
        }
    }
}
