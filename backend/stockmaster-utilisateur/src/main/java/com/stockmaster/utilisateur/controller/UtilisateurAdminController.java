package com.stockmaster.utilisateur.controller;

import com.stockmaster.shared.dto.response.ApiResponse;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.dto.request.AdminFilialeCreateRequest;
import com.stockmaster.utilisateur.dto.response.UtilisateurAdminResponse;
import com.stockmaster.utilisateur.service.AdminFilialeService;
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
 * US-021 — {@code POST /api/v1/utilisateurs/admin-filiale} : créer un Admin
 * Filiale. Accès réservé à l'Admin Groupe ({@code @PreAuthorize}) ; l'isolation
 * multi-tenant est portée par le service (groupId du JWT) — aucun groupId
 * n'est accepté en entrée.
 *
 * <p>Les autres endpoints de l'EPIC 4 (US-022/023/024/025) rejoindront ce
 * contrôleur au fil des branches.</p>
 */
@RestController
@RequestMapping("/api/v1/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurAdminController {

    private final AdminFilialeService adminFilialeService;

    @PostMapping("/admin-filiale")
    @PreAuthorize("hasRole('ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<UtilisateurAdminResponse>> creerAdminFiliale(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @Valid @RequestBody AdminFilialeCreateRequest request) {
        UtilisateurAdminResponse response = adminFilialeService.creer(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
