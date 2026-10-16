package com.stockmaster.catalogue.controller;

import com.stockmaster.catalogue.dto.request.CategorieCreateRequest;
import com.stockmaster.catalogue.dto.request.CategorieUpdateRequest;
import com.stockmaster.catalogue.dto.response.CategorieListResponse;
import com.stockmaster.catalogue.dto.response.CategorieResponse;
import com.stockmaster.catalogue.service.CategorieService;
import com.stockmaster.shared.dto.response.ApiResponse;
import com.stockmaster.shared.security.StockMasterPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Opérations catalogue group-scoped (DEC-002, V5 §6).
 *
 * <ul>
 *   <li>US-027 — POST /api/v1/categories : créer une catégorie</li>
 *   <li>US-028 — GET  /api/v1/categories : liste paginée + search (designation)</li>
 *   <li>US-029 — PUT  /api/v1/categories/{id} : modifier designation / taux TVA (PATCH)</li>
 *   <li>US-030 — DELETE /api/v1/categories/{id} : suppression logique, bloquée si
 *       articles attachés (DEC-028)</li>
 * </ul>
 *
 * <p>RBAC partagé (GESTIONNAIRE_STOCK / ADMIN_FILIALE / ADMIN_GROUPE).
 * La désignation recherche est un filtre optionnel (?designation=...).
 * Le tri par défaut est designation ASC.
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
public class CategorieController {

    private final CategorieService categorieService;

    @PostMapping
    @PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<CategorieResponse>> creer(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @Valid @RequestBody CategorieCreateRequest request) {
        CategorieResponse reponse = categorieService.creer(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(reponse));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<CategorieListResponse>> lister(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "designation") String sort,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String designation) {
        CategorieListResponse reponse = categorieService.lister(
                principal, page, size, sort, direction, designation);
        return ResponseEntity.ok(ApiResponse.ok(reponse));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<CategorieResponse>> modifier(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CategorieUpdateRequest request) {
        CategorieResponse reponse = categorieService.modifier(principal, id, request);
        return ResponseEntity.ok(ApiResponse.ok(reponse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<CategorieResponse>> supprimer(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @PathVariable Long id) {
        CategorieResponse reponse = categorieService.supprimer(principal, id);
        return ResponseEntity.ok(ApiResponse.ok(reponse));
    }
}
