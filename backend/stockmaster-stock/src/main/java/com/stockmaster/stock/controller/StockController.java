package com.stockmaster.stock.controller;

import com.stockmaster.shared.dto.response.ApiResponse;
import com.stockmaster.shared.dto.response.PageResponse;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.stock.dto.response.MouvementStockResponse;
import com.stockmaster.stock.dto.response.StockArticleResponse;
import com.stockmaster.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Consultation du stock — EPIC 8.
 *
 * <ul>
 *   <li>US-051 — {@code GET /api/v1/stock} : stock réel par article, paginé,
 *       filtre {@code ?statutAlerte=}. Aucun {@code @PreAuthorize} : critère
 *       « tout utilisateur authentifié » (l'authentification est globale via
 *       {@code SecurityConfig.anyRequest().authenticated()}).</li>
 *   <li>US-052 — {@code GET /api/v1/stock/articles/{id}/mouvements} : historique
 *       paginé, tri date DESC, filtres {@code typeMouvement}/{@code dateDebut}/
 *       {@code dateFin} — RBAC GESTIONNAIRE_STOCK / ADMIN_FILIALE / ADMIN_GROUPE.</li>
 * </ul>
 *
 * <p>Isolation : aucun identifiant de tenant n'est accepté en entrée — tout
 * vient du JWT ({@code StockMasterPrincipal}).</p>
 */
@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
@Validated
public class StockController {

    private final StockService stockService;

    /**
     * US-051 — liste paginée du stock réel de chaque article du groupe,
     * agrégée sur l'entreprise du JWT.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StockArticleResponse>>> lister(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @RequestParam(required = false) String statutAlerte,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<StockArticleResponse> reponse =
                stockService.listerStock(principal, statutAlerte, page, size);
        return ResponseEntity.ok(ApiResponse.ok(reponse));
    }

    /**
     * US-052 — historique des mouvements d'un article (journal immuable).
     */
    @GetMapping("/articles/{articleId}/mouvements")
    @PreAuthorize("hasAnyRole('GESTIONNAIRE_STOCK','ADMIN_FILIALE','ADMIN_GROUPE')")
    public ResponseEntity<ApiResponse<PageResponse<MouvementStockResponse>>> mouvements(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @PathVariable Long articleId,
            @RequestParam(required = false) String typeMouvement,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<MouvementStockResponse> reponse = stockService.listerMouvements(
                principal, articleId, typeMouvement, dateDebut, dateFin, page, size);
        return ResponseEntity.ok(ApiResponse.ok(reponse));
    }
}
