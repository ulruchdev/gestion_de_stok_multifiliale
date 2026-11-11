package com.stockmaster.tiers.controller;

import com.stockmaster.shared.dto.response.ApiResponse;
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.tiers.dto.request.CreerClientRequest;
import com.stockmaster.tiers.dto.request.ModifierClientRequest;
import com.stockmaster.tiers.dto.response.ClientResponse;
import com.stockmaster.tiers.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Clients — EPIC 6 (Tiers), US-040 → US-043.
 *
 * <p>US-040 — {@code POST /api/v1/clients} : créer une fiche client.
 * <br>US-041 — {@code GET /api/v1/clients?search=&page=&size=} : lister /
 * rechercher par nom, prénom ou téléphone dans le tenant du JWT.
 * <br>US-042 — {@code PUT /api/v1/clients/{id}} : mettre à jour les coordonnées
 * (PATCH sémantique, {@code 200} + état après modification).
 * <br>US-043 — {@code DELETE /api/v1/clients/{id}} : archiver une fiche
 * (soft delete, {@code 200}, {@code 409 RES_005} si un historique de commandes
 * existe).</p>
 *
 * <p><b>RBAC (arbitrage du 9 nov. 2026)</b> : {@code COMMERCIAL | ADMIN_FILIALE |
 * ADMIN_GROUPE} en écriture ; {@code CAISSIER} <b>en lecture seule</b> — il
 * n'est donc admis que sur le {@code GET}, jamais sur {@code POST}/{@code PUT}/
 * {@code DELETE}.</p>
 *
 * <p>Le corps ne porte aucun identifiant tenant : {@code entreprise_id} vient du
 * JWT ({@code StockMasterPrincipal}), lu côté service.</p>
 */
@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Validated
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMMERCIAL','ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<ClientResponse>> creer(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @Valid @RequestBody CreerClientRequest request) {
        ClientResponse reponse = clientService.creer(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(reponse));
    }

    /**
     * US-041 — listing paginé + recherche.
     *
     * <p>{@code search} optionnel (absent ou blanc → tous les clients du
     * tenant) ; {@code page} défaut 0, {@code size} défaut 20 et borné à
     * {@code stockmaster.pagination.max-page-size} (100). Aucun paramètre de
     * tenant accepté.</p>
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('COMMERCIAL','ADMIN_FILIALE','ADMIN_GROUPE','CAISSIER')")
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> lister(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ClientResponse> reponse =
                clientService.lister(principal, search, page, size);
        return ResponseEntity.ok(ApiResponse.ok(reponse));
    }

    /**
     * US-042 — modification des coordonnées d'un client du tenant.
     *
     * <p>Champ absent ou blanc = inchangé. Aucun champ tenant dans le corps ;
     * l'id ne peut pas être changé (il est dans le chemin).</p>
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMMERCIAL','ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<ClientResponse>> modifier(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ModifierClientRequest request) {
        ClientResponse reponse = clientService.modifier(principal, id, request);
        return ResponseEntity.ok(ApiResponse.ok(reponse));
    }

    /**
     * US-043 — archivage logique d'un client du tenant.
     *
     * <p>{@code 409 RES_005} dès qu'une commande client existe, même annulée ;
     * {@code 404 RES_001} uniforme sur id inconnu, hors tenant ou déjà
     * archivé.</p>
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMMERCIAL','ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<ClientResponse>> supprimer(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @PathVariable Long id) {
        ClientResponse reponse = clientService.supprimer(principal, id);
        return ResponseEntity.ok(ApiResponse.ok(reponse));
    }
}
