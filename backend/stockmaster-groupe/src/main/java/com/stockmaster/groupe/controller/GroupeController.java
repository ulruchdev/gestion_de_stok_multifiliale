package com.stockmaster.groupe.controller;

import com.stockmaster.groupe.dto.request.GroupeUpdateRequest;
import com.stockmaster.groupe.dto.response.GroupeResponse;
import com.stockmaster.groupe.service.GroupeService;
import com.stockmaster.shared.dto.response.ApiResponse;
import com.stockmaster.shared.security.StockMasterPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * US-015 — {@code GET /api/v1/groupe} : informations du groupe + plan actif.
 * US-014 — {@code PUT /api/v1/groupe} : modification partielle du groupe (nom, logo, infos fiscales).
 *
 * <p>Accès réservé à l'Admin Groupe (critère d'acceptation). L'isolation
 * multi-tenant est portée par le service (groupId du JWT).</p>
 */
@RestController
@RequestMapping("/api/v1/groupe")
@RequiredArgsConstructor
public class GroupeController {

    private final GroupeService groupeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<GroupeResponse>> consulter(@AuthenticationPrincipal StockMasterPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(groupeService.consulter(principal)));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<GroupeResponse>> modifier(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @Valid @ModelAttribute GroupeUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(groupeService.modifier(principal, request)));
    }
}
