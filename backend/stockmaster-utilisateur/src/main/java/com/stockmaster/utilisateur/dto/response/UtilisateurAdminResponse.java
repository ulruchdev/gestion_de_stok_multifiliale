package com.stockmaster.utilisateur.dto.response;

import com.stockmaster.shared.domain.enums.RoleUtilisateur;
import lombok.Builder;
import lombok.Getter;

/**
 * US-021 — réponse de création d'un Admin Filiale.
 *
 * <p>Le mot de passe (provisoire ou haché) et le token d'invitation ne sont
 * JAMAIS exposés ici — l'activation passe exclusivement par le lien/token
 * reçu par email (US-075).</p>
 */
@Getter
@Builder
public class UtilisateurAdminResponse {

    private Long id;
    private String email;
    private String prenom;
    private String nom;
    private RoleUtilisateur role;
    private Boolean actif;
    private Long filialeId;
}
