package com.stockmaster.groupe.controller;

import com.stockmaster.groupe.dto.request.FilialeCreateRequest;
import com.stockmaster.groupe.dto.response.FilialeResponse;
import com.stockmaster.groupe.service.FilialeService;
import com.stockmaster.shared.dto.response.ApiResponse;
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.security.StockMasterPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * US-016 — {@code POST /api/v1/groupe/filiales} : créer une filiale.
 * US-017 — {@code GET /api/v1/groupe/filiales} : lister les filiales, paginé.
 *
 * <p>Accès réservé à l'Admin Groupe. L'isolation multi-tenant est portée par
 * le service (groupId du JWT) — aucun groupId n'est accepté en entrée.</p>
 */
@RestController
@RequestMapping("/api/v1/groupe/filiales")
@RequiredArgsConstructor
public class FilialeController {

    private final FilialeService filialeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<FilialeResponse>> creer(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @Valid @RequestBody FilialeCreateRequest request) {
        FilialeResponse response = filialeService.creer(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /** Défauts alignés sur {@code stockmaster.pagination.default-page-size/default-sort} (pas de resolver dédié encore). */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<PageResponse<FilialeResponse>>> lister(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) String ville,
            @PageableDefault(size = 20, sort = "dateCreation", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<FilialeResponse> response = filialeService.lister(principal, actif, ville, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
