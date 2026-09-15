package com.stockmaster.utilisateur.controller;

import com.stockmaster.shared.dto.response.ApiResponse;
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.dto.response.UtilisateurListResponse;
import com.stockmaster.utilisateur.service.UtilisateurListService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * US-023 — {@code GET /api/v1/utilisateurs} : lister les utilisateurs du
 * périmètre de l'admin connecté.
 *
 * <p>Accès : Admin Groupe (toutes les filiales de son groupe, resserrable via
 * {@code filialeId}) et Admin Filiale (sa filiale uniquement). Le périmètre
 * est résolu côté service depuis le JWT — aucun {@code groupId} accepté en
 * entrée (DEC-019).</p>
 *
 * <p>Filtres optionnels : {@code role}, {@code actif}, {@code filialeId} ;
 * pagination bornée à {@code stockmaster.pagination.max-page-size}.</p>
 *
 * <p>Ce contrôleur complète {@link UtilisateurAdminController} (US-021) et
 * {@link EmployeController} (US-022). Les endpoints US-024/025 rejoindront
 * ces contrôleurs au fil de l'EPIC 4.</p>
 */
@RestController
@RequestMapping("/api/v1/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurListController {

    private final UtilisateurListService utilisateurListService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<PageResponse<UtilisateurListResponse>>> lister(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @RequestParam(required = false) RoleUtilisateur role,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) Long filialeId,
            @PageableDefault(size = 20, sort = "dateCreation", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<UtilisateurListResponse> response =
                utilisateurListService.lister(principal, role, actif, filialeId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
