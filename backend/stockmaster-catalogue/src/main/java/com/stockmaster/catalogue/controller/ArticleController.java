package com.stockmaster.catalogue.controller;

import com.stockmaster.catalogue.dto.request.ArticleCreateRequest;
import com.stockmaster.catalogue.dto.request.ArticleUpdateRequest;
import com.stockmaster.catalogue.dto.response.ArticleListResponse;
import com.stockmaster.catalogue.dto.response.ArticleResponse;
import com.stockmaster.catalogue.service.ArticleService;
import com.stockmaster.shared.dto.response.ApiResponse;
import com.stockmaster.shared.security.StockMasterPrincipal;
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

import jakarta.validation.Valid;

/**
 * Opérations articles group-scopées (DEC-002, V5 §6).
 *
 * <ul>
 *   <li>US-031 — POST   /api/v1/articles : créer un article</li>
 *   <li>US-032 — GET    /api/v1/articles : liste paginée + search (designation
 *       ou code article, sans casse)</li>
 *   <li>US-033 — GET    /api/v1/articles/{id} : fiche article (404 hors groupe)</li>
 *   <li>US-034 — PUT    /api/v1/articles/{id} : modifier (TTC recalculé, DEC-003)</li>
 *   <li>US-035 — DELETE /api/v1/articles/{id} : suppression logique (404 hors groupe)</li>
 * </ul>
 *
 * <p>RBAC : écritures GESTIONNAIRE_STOCK / ADMIN_FILIALE / ADMIN_GROUPE
 * (miroir {@code CategorieController}) ; lectures « tout utilisateur
 * authentifié » (US-032/033) — le scoping groupe est appliqué côté service.</p>
 */
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
@Validated
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<ArticleResponse>> creer(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @Valid @RequestBody ArticleCreateRequest request) {
        ArticleResponse reponse = articleService.creer(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(reponse));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ArticleListResponse>> lister(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "designation") String sort,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String search) {
        ArticleListResponse reponse = articleService.lister(
                principal, page, size, sort, direction, search);
        return ResponseEntity.ok(ApiResponse.ok(reponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ArticleResponse>> consulter(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @PathVariable Long id) {
        ArticleResponse reponse = articleService.consulter(principal, id);
        return ResponseEntity.ok(ApiResponse.ok(reponse));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<ArticleResponse>> modifier(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ArticleUpdateRequest request) {
        ArticleResponse reponse = articleService.modifier(principal, id, request);
        return ResponseEntity.ok(ApiResponse.ok(reponse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<ArticleResponse>> supprimer(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @PathVariable Long id) {
        ArticleResponse reponse = articleService.supprimer(principal, id);
        return ResponseEntity.ok(ApiResponse.ok(reponse));
    }
}
