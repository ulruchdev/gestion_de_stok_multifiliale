package com.stockmaster.catalogue.controller;

import com.stockmaster.catalogue.dto.request.ArticleCreateRequest;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Opérations articles group-scopées (DEC-002, V5 §6).
 *
 * <ul>
 *   <li>US-031 — POST /api/v1/articles : créer un article</li>
 * </ul>
 *
 * <p>RBAC partagé catalogue (GESTIONNAIRE_STOCK / ADMIN_FILIALE / ADMIN_GROUPE)
 * — identique à {@code CategorieController}.</p>
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
}
