package com.stockmaster.tiers.controller;

import com.stockmaster.tiers.dto.request.CreerFournisseurRequest;
import com.stockmaster.tiers.dto.response.FournisseurResponse;
import com.stockmaster.tiers.service.FournisseurService;
import com.stockmaster.shared.dto.response.ApiResponse;
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.security.StockMasterPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Fournisseurs — EPIC 6 (Tiers).
 *
 * <p>US-036 — {@code POST /api/v1/fournisseurs} : créer une fiche fournisseur.
 * <br>US-037 — {@code GET /api/v1/fournisseurs?search=&page=&size=} : lister /
 * rechercher par nom ou ville dans le tenant du JWT.</p>
 *
 * <p><b>CA1</b> : {@code @PreAuthorize("hasAnyRole('RESP_ACHATS','ADMIN_FILIALE','ADMIN_GROUPE')")}
 * — même placement que {@code CategorieController} / {@code ArticleController}
 * (sur la méthode du contrôleur, portée par {@code @EnableMethodSecurity}).</p>
 *
 * <p>Le corps ne porte aucun identifiant tenant : {@code entreprise_id} vient
 * du JWT ({@code StockMasterPrincipal}), lu côté service (CA2).</p>
 */
@RestController
@RequestMapping("/api/v1/fournisseurs")
@RequiredArgsConstructor
@Validated
public class FournisseurController {

    private final FournisseurService fournisseurService;

    @PostMapping
    @PreAuthorize("hasAnyRole('RESP_ACHATS','ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<FournisseurResponse>> creer(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @Valid @RequestBody CreerFournisseurRequest request) {
        FournisseurResponse reponse = fournisseurService.creer(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(reponse));
    }

    /**
     * US-037 — listing paginé + recherche.
     *
     * <p>{@code search} optionnel (absent ou blanc → tous les fournisseurs du
     * tenant) ; {@code page} défaut 0, {@code size} défaut 20 et borné côté
     * service à {@code stockmaster.pagination.max-page-size} (100) — le repo
     * borne silencieusement, jamais de 400. Aucun paramètre de tenant n'est
     * accepté : {@code entreprise_id} vient du JWT.</p>
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('RESP_ACHATS','ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<PageResponse<FournisseurResponse>>> lister(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<FournisseurResponse> reponse =
                fournisseurService.lister(principal, search, page, size);
        return ResponseEntity.ok(ApiResponse.ok(reponse));
    }
}
