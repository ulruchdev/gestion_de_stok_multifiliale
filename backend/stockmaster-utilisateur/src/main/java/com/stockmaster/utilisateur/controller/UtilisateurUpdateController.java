package com.stockmaster.utilisateur.controller;

import com.stockmaster.shared.dto.response.ApiResponse;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.dto.request.UtilisateurUpdateRequest;
import com.stockmaster.utilisateur.dto.response.UtilisateurListResponse;
import com.stockmaster.utilisateur.service.UtilisateurUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * US-024 — {@code PUT /api/v1/utilisateurs/{id}} : modifier nom, prénom,
 * email ou rôle d'un utilisateur (sémantique PATCH : champ null = inchangé).
 *
 * <p>Accès : Admin Filiale (sa filiale) et Admin Groupe (ses filiales).
 * Le périmètre est vérifié côté service depuis le JWT — 404 uniforme si la
 * cible est hors périmètre (jamais révéler l'existence).</p>
 *
 * <p>Ce contrôleur complète {@link UtilisateurAdminController} (US-021),
 * {@link EmployeController} (US-022) et {@link UtilisateurListController}
 * (US-023). L'activation/désactivation (US-025) rejoindra ces contrôleurs
 * au fil de l'EPIC 4.</p>
 */
@RestController
@RequestMapping("/api/v1/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurUpdateController {

    private final UtilisateurUpdateService utilisateurUpdateService;

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<UtilisateurListResponse>> modifier(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurUpdateRequest request) {
        UtilisateurListResponse response = utilisateurUpdateService.modifier(principal, id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
