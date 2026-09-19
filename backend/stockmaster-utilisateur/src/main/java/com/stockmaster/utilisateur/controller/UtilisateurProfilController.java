package com.stockmaster.utilisateur.controller;

import com.stockmaster.shared.dto.response.ApiResponse;
import com.stockmaster.shared.security.StockMasterPrincipal;
import com.stockmaster.utilisateur.dto.request.ProfilUtilisateurRequest;
import com.stockmaster.utilisateur.dto.response.UtilisateurProfilResponse;
import com.stockmaster.utilisateur.service.UtilisateurProfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * US-026 — consulter et modifier son propre profil :
 * <ul>
 *   <li>{@code GET /api/v1/utilisateurs/profil} — consultation ;</li>
 *   <li>{@code PUT /api/v1/utilisateurs/profil} — modification en
 *       {@code multipart/form-data} : part JSON {@code profil} optionnelle
 *       (prenom/nom/adresseVille, {@code null} = inchangé) + part
 *       {@code photo} optionnelle (PNG/JPEG/WebP, ≤ 2 Mo → MinIO).</li>
 * </ul>
 *
 * <p><b>Self-service uniquement</b> : le service travaille toujours sur
 * {@code principal.getUserId()} — un admin qui modifie un autre utilisateur
 * passe par {@link UtilisateurUpdateController} (US-024), ce qui rend caduque
 * la SpEL {@code #id == principal.userId} envisagée dans le backlog.</p>
 *
 * <p>Email et rôle ne sont jamais modifiables ici (critère d'acceptation
 * US-026) : ils ne figurent pas dans {@link ProfilUtilisateurRequest}.</p>
 *
 * <p>Ce contrôleur complète {@link UtilisateurAdminController} (US-021),
 * {@link EmployeController} (US-022), {@link UtilisateurListController}
 * (US-023), {@link UtilisateurUpdateController} (US-024) et
 * {@link UtilisateurStatutController} (US-025).</p>
 */
@RestController
@RequestMapping("/api/v1/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurProfilController {

    private final UtilisateurProfilService utilisateurProfilService;

    @GetMapping("/profil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UtilisateurProfilResponse>> consulter(
            @AuthenticationPrincipal StockMasterPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(utilisateurProfilService.consulter(principal)));
    }

    @PutMapping(value = "/profil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UtilisateurProfilResponse>> mettreAJour(
            @AuthenticationPrincipal StockMasterPrincipal principal,
            @RequestPart(value = "profil", required = false) @Valid ProfilUtilisateurRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return ResponseEntity.ok(
                ApiResponse.ok(utilisateurProfilService.mettreAJour(principal, request, photo)));
    }
}
