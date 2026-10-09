package com.stockmaster.catalogue.controller;

import com.stockmaster.catalogue.dto.request.CategorieCreateRequest;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * US-027 — {@code POST /api/v1/categories} : créer une catégorie
 * (catalogue group-scoped, {@code DEC-002}).
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
        CategorieResponse response = categorieService.creer(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
