package com.stockmaster.utilisateur.controller;

import com.stockmaster.shared.dto.response.ApiResponse;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.dto.request.StatutUtilisateurRequest;
import com.stockmaster.utilisateur.dto.response.UtilisateurListResponse;
import com.stockmaster.utilisateur.service.UtilisateurStatutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * US-025 — {@code PATCH /api/v1/utilisateurs/{id}/statut} : activer/désactiver
 * un employé (corps {@code {"actif": false}}).
 *
 * <p>Accès : Admin Filiale (sa filiale) et Admin Groupe (ses filiales).
 * Le périmètre est vérifié côté service depuis le JWT — 404 uniforme si la
 * cible est hors périmètre (jamais révéler l'existence).</p>
 *
 * <p>Ce contrôleur complète {@link UtilisateurAdminController} (US-021),
 * {@link EmployeController} (US-022), {@link UtilisateurListController}
 * (US-023) et {@link UtilisateurUpdateController} (US-024).</p>
 */
@RestController
@RequestMapping("/api/v1/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurStatutController {

    private final UtilisateurStatutService utilisateurStatutService;

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<UtilisateurListResponse>> changerStatut(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody StatutUtilisateurRequest request) {
        UtilisateurListResponse response =
                utilisateurStatutService.changerStatut(principal, id, request.getActif());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
