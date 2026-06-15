package com.stockmaster.auth.controller;

import com.stockmaster.auth.dto.request.InscriptionEntrepriseUniqueRequest;
import com.stockmaster.auth.dto.request.InscriptionGroupeRequest;
import com.stockmaster.auth.dto.request.LoginRequest;
import com.stockmaster.auth.dto.request.RefreshTokenRequest;
import com.stockmaster.auth.dto.response.InscriptionResponse;
import com.stockmaster.auth.dto.response.LoginResponse;
import com.stockmaster.auth.dto.response.RefreshTokenResponse;
import com.stockmaster.auth.service.AuthService;
import com.stockmaster.shared.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/inscription/entreprise-unique")
    public ResponseEntity<ApiResponse<InscriptionResponse>> inscrireEntrepriseUnique(
            @Valid @RequestBody InscriptionEntrepriseUniqueRequest request) {

        log.debug("Inscription entreprise unique reçue pour: {}", request.getEmail());

        InscriptionResponse response = authService.inscrireEntrepriseUnique(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, response.getMessage()));
    }

    @PostMapping("/inscription/groupe")
    public ResponseEntity<ApiResponse<InscriptionResponse>> inscrireGroupe(
            @Valid @RequestBody InscriptionGroupeRequest request) {

        log.debug("Inscription groupe reçue pour: {}", request.getEmailAdmin());

        InscriptionResponse response = authService.inscrireGroupe(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, response.getMessage()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.debug("Tentative de connexion pour: {}", request.getEmail());

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok()
                .body(ApiResponse.ok(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.debug("Demande de refresh token reçue");

        RefreshTokenResponse response = authService.refreshAccessToken(request);

        return ResponseEntity.ok()
                .body(ApiResponse.ok(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {

        log.debug("Demande de déconnexion reçue");

        authService.logout();

        return ResponseEntity.ok()
                .body(ApiResponse.success("Déconnexion réussie"));
    }
}
