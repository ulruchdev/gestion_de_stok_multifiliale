package com.stockmaster.utilisateur.controller;

import com.stockmaster.shared.dto.response.ApiResponse;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.dto.request.CreerEmployeRequest;
import com.stockmaster.utilisateur.dto.response.EmployeResponse;
import com.stockmaster.utilisateur.service.EmployeService;
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
 * US-022 — {@code POST /api/v1/utilisateurs/employes} : créer un employé.
 *
 * <p>Accès : Admin Filiale (crée dans sa filiale) et Admin Groupe.
 * Le rôle autorisé à créer est contrôlé côté service : seuls
 * {@code GESTIONNAIRE_STOCK}, {@code RESP_ACHATS}, {@code COMMERCIAL} et
 * {@code CAISSIER} sont acceptés.</p>
 *
 * <p>Ce contrôleur complète {@link UtilisateurAdminController} (US-021).
 * Les endpoints US-023/024/025 rejoindront ces contrôleurs au fil de l'EPIC 4.</p>
 */
@RestController
@RequestMapping("/api/v1/utilisateurs")
@RequiredArgsConstructor
public class EmployeController {

    private final EmployeService employeService;

    @PostMapping("/employes")
    @PreAuthorize("hasAnyRole('ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<EmployeResponse>> creerEmploye(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @Valid @RequestBody CreerEmployeRequest request) {
        EmployeResponse response = employeService.creer(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
