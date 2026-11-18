package com.stockmaster.reporting.controller;

import com.stockmaster.reporting.dto.response.ComparaisonFilialesResponse;
import com.stockmaster.reporting.service.ReportingService;
import com.stockmaster.shared.dto.response.ApiResponse;
import com.stockmaster.shared.security.StockMasterPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Rapports d'activité — EPIC F09.
 *
 * <ul>
 *   <li>US-F092 (GS-1255) — {@code GET /api/v1/reporting/filiales} : comparaison
 *       inter-filiales, <b>ADMIN_GROUPE</b> uniquement.</li>
 * </ul>
 *
 * <p>Isolation : aucun identifiant de groupe ni de filiale n'est accepté en
 * entrée — le périmètre est déduit du JWT ({@code StockMasterPrincipal}). La
 * période est optionnelle (mois courant par défaut) et validée par
 * {@code PeriodeReporting} : {@code 400} si {@code fin < debut} ou si la
 * période dépasse 366 jours.</p>
 */
@RestController
@RequestMapping("/api/v1/reporting")
@RequiredArgsConstructor
@Validated
public class ReportingController {

    private final ReportingService reportingService;

    /**
     * US-F092 — comparaison inter-filiales du groupe du JWT.
     */
    @GetMapping("/filiales")
    @PreAuthorize("hasRole('ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<ComparaisonFilialesResponse>> comparerFiliales(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        ComparaisonFilialesResponse reponse =
                reportingService.comparerFiliales(principal, debut, fin);
        return ResponseEntity.ok(ApiResponse.ok(reponse));
    }
}
