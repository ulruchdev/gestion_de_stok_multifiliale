package com.stockmaster.auth.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String role;
    private String scope;

    /**
     * Profil de l'utilisateur connecté — contrat frontend (ticket GS-1244) :
     * le store d'authentification persiste ces données pour le RBAC et le Header
     * sans avoir à décoder le JWT. Additif (ne casse pas les clients existants).
     */
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UtilisateurInfo {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String role;
        private String scope;
        private Long entrepriseId;
        private Long groupId;
    }

    private UtilisateurInfo utilisateur;
}
