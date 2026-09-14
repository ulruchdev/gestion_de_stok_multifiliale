package com.stockmaster.groupe.controller;

import com.stockmaster.groupe.dto.request.FilialeCreateRequest;
import com.stockmaster.groupe.dto.response.FilialeResponse;
import com.stockmaster.groupe.service.FilialeService;
import com.stockmaster.shared.dto.response.ApiResponse;
import com.stockmaster.shared.security.StockMasterPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * US-016 — {@code POST /api/v1/groupe/filiales} : créer une filiale.
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
}
